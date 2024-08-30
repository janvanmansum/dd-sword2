/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.sword2.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.auth.AuthenticationException;
import nl.knaw.dans.sword2.config.PasswordDelegateConfig;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.glassfish.jersey.message.internal.Statuses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final PasswordDelegateConfig passwordDelegateConfig;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthenticationServiceImpl(PasswordDelegateConfig passwordDelegateConfig, CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        this.passwordDelegateConfig = passwordDelegateConfig;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> authenticateWithHeaders(MultivaluedMap<String, String> headers) throws AuthenticationException {
        try {
            var request = new HttpPost(passwordDelegateConfig.getUrl().toURI());
            var allowedHeaders = passwordDelegateConfig.getForwardHeaders().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            log.debug("Filtering headers, allowed headers are {}", allowedHeaders);
            headers.entrySet().stream()
                // only set headers that are configured
                .filter(h -> allowedHeaders.contains(h.getKey().toLowerCase()))
                // only set headers that have a value
                .filter(h -> !h.getValue().isEmpty())
                .forEach(h -> request.setHeader(h.getKey().toLowerCase(), h.getValue().get(0)));

            // always add this header to have a valid request
            request.setHeader("Accept", "application/json");
            log.debug("Headers set to {}", (Object) request.getHeaders());

            return doRequest(request);
        }
        catch (URISyntaxException | IOException e) {
            throw new AuthenticationException("Unable to validate credentials", e);
        }
    }

    private Optional<String> doRequest(HttpUriRequest request) throws AuthenticationException, IOException {
        try (var response = httpClient.execute(request)) {
            final String reasonPhrase = response.getReasonPhrase();
            int statusCode = response.getCode();
            final Response.StatusType statusPhrase = Statuses.from(statusCode, reasonPhrase == null ? "" : reasonPhrase);
            log.debug("Delegate returned status code {}", statusCode);

            return switch (statusCode) {
                case 200 -> getUsernameFromResponse(response);
                case 401 -> Optional.empty();
                default -> throw new AuthenticationException(String.format("Unexpected status code returned: %s (message: %s)", statusCode, statusPhrase));
            };
        }
    }

    private Optional<String> getUsernameFromResponse(CloseableHttpResponse response) {
        try {
            var tree = objectMapper.readTree(response.getEntity().getContent());
            return Optional.ofNullable(tree.get("userId").asText());
        }
        catch (Exception e) {
            log.error("Error parsing JSON", e);
        }

        return Optional.empty();
    }
}
