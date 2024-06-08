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
package nl.knaw.dans.sword2.core.service;

import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.sword2.config.DefaultUserConfig;
import nl.knaw.dans.sword2.config.UserConfig;
import nl.knaw.dans.sword2.core.auth.Depositor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class UserManagerImpl implements UserManager {
    private final Map<String, Depositor> depositorMap = new HashMap<>();
    private final DefaultUserConfig defaultUserConfig;

    public UserManagerImpl(List<UserConfig> userConfigs, DefaultUserConfig defaultUserConfig) {
        if (userConfigs != null) {
            for (var user : userConfigs) {
                var depositor = new Depositor(user.getName(),
                    user.getFilepathMapping() == null ? defaultUserConfig.getFilepathMapping() : user.getFilepathMapping(),
                    user.getCollections() == null ? Set.copyOf(defaultUserConfig.getCollections()) : Set.copyOf(user.getCollections()));
                depositorMap.put(user.getName(), depositor);
            }
        }
        this.defaultUserConfig = defaultUserConfig;
    }

    @Override
    public Depositor getDepositorById(String id) {
        var depositor = depositorMap.get(id);
        if (depositor != null) {
            return depositor;
        } else {
            var collections = defaultUserConfig.getCollections();
            if (collections == null) {
                collections = new ArrayList<>();
                log.warn("No collections configured for default user");
            }
            return new Depositor(id, defaultUserConfig.getFilepathMapping(), Set.copyOf(collections));
        }
  }
}
