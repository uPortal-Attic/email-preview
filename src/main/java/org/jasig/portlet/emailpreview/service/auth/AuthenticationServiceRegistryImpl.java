/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.emailpreview.service.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationServiceRegistryImpl implements IAuthenticationServiceRegistry {
    
    private Map<String, IAuthenticationService> serviceMap = new HashMap<String, IAuthenticationService>();

    @Resource(name = "authServices")
    @Required
    public void setServices(Collection<IAuthenticationService> services) {
        serviceMap.clear();
        for (IAuthenticationService service : services) {
            registerService(service);
        }
    }

    public IAuthenticationService getAuthenticationService(String key) {
        return serviceMap.get(key);
    }

    public Collection<IAuthenticationService> getServices() {
        return serviceMap.values();
    }

    public void registerService(IAuthenticationService authService) {
        serviceMap.put(authService.getKey(), authService);
    }

}
