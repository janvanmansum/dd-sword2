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
package nl.knaw.dans.sword2.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;

class RequestClientBuilder {

    public static Client buildClient() {

        return new JerseyClientBuilder()
            .register(new JacksonFeature(new ObjectMapper()))
            .property(ClientProperties.CONNECT_TIMEOUT, 5000)
            .property(ClientProperties.READ_TIMEOUT, 5000)
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .build();
    }
}
