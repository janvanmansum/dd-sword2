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
package nl.knaw.dans.sword2.config;

import lombok.Data;
import nl.knaw.dans.lib.util.ExecutorServiceFactory;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.util.List;

@Data
public class Sword2Config {

    @NotNull
    private URI baseUrl;
    @Email
    @NotEmpty
    private String emailAddress;
    @NotEmpty
    private List<@Valid CollectionConfig> collections;
    @Valid
    @NotNull
    private Duration rescheduleDelay;
    @Valid
    @NotNull
    private ExecutorServiceFactory finalizingQueue;
    @Valid
    @NotNull
    private ExecutorServiceFactory rescheduleQueue;
}


