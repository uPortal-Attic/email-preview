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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationServiceRegistryImpl implements IAuthenticationServiceRegistry, ApplicationContextAware {
    
    private ApplicationContext applicationContext;

    @Override
    public IAuthenticationService getAuthenticationService(String key) {
        IAuthenticationService rslt = null;  // default
        Collection<IAuthenticationService> services = getServices();
        for (IAuthenticationService auth : services) {
            // It's unfortunate that we have to loop this collection, but there 
            // should be max 2-3 items in it
            if (auth.getKey().equalsIgnoreCase(key)) {
                rslt = auth;
                break;
            }
        }
        return rslt;
    }

    @Override
    public Collection<IAuthenticationService> getServices() {
        return applicationContext.getBeansOfType(IAuthenticationService.class).values();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext; 
    }

}
