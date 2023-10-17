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

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import nl.knaw.dans.sword2.config.UserConfig;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SwordAuthenticator implements Authenticator<HeaderCredentials, Depositor> {

    private static final Logger log = LoggerFactory.getLogger(SwordAuthenticator.class);

    private final List<UserConfig> userConfigs;

    private final AuthenticationService authenticationService;

    public SwordAuthenticator(List<UserConfig> userConfigs, AuthenticationService authenticationService) {
        this.userConfigs = userConfigs;
        this.authenticationService = authenticationService;
    }

    @Override
    public Optional<Depositor> authenticate(HeaderCredentials credentials) throws AuthenticationException {
        Optional<Depositor> depositor = Optional.empty();
        var basicCredentials = credentials.getBasicCredentials();
        if (basicCredentials != null) {
            log.debug("Basic credentials found, checking if user is configured locally");
            var user = getUserByName(basicCredentials.getUsername());

            if (user.isPresent()) {
                var userConfig = user.get();
                if (userConfig.getPasswordHash() != null) {
                    log.debug("User is configured with a password hash, validating password for user {}", userConfig.getName());

                    if (BCrypt.checkpw(basicCredentials.getPassword(), userConfig.getPasswordHash())) {
                        depositor = Optional.of(new Depositor(userConfig.getName(), userConfig.getFilepathMapping(), Set.copyOf(userConfig.getCollections())));
                    }
                }
                else {
                    log.debug("User is not configured with a password hash, forwarding request to passwordDelegate");
                    depositor = delegateAuthentication(credentials);
                }
            }
            else {
                log.debug("User is not found in config file and therefore not allowed to deposit");
            }
        }
        else {
            log.debug("No basic credentials provided, forwarding request to passwordDelegate");
            depositor = delegateAuthentication(credentials);
        }

        return depositor;
    }

    private Optional<Depositor> delegateAuthentication(HeaderCredentials credentials) throws AuthenticationException {
        Optional<Depositor> depositor = Optional.empty();
        if (authenticationService != null) {
            log.debug("No basic credentials provided, or not configured with a local password; forwarding request to passwordDelegate");

            depositor = authenticationService.authenticateWithHeaders(credentials.getHeaders())
                .map(this::getUserByName)
                .flatMap(f -> f)
                .map(u -> new Depositor(u.getName(), u.getFilepathMapping(), new HashSet<>(u.getCollections())));
        }
        return depositor;
    }

    Optional<UserConfig> getUserByName(String name) {
        return userConfigs.stream()
            .filter(u -> u.getName().equals(name))
            .findFirst();
    }
}
