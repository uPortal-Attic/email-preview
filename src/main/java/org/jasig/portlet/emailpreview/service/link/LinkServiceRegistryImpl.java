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
package org.jasig.portlet.emailpreview.service.link;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

/**
 * LinkServiceRegistryImpl provides the default implementation of 
 * ILinkServiceRegistry.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component
public class LinkServiceRegistryImpl implements ILinkServiceRegistry {
    
    private Map<String, IEmailLinkService> serviceMap = new HashMap<String, IEmailLinkService>();
    
    /**
     * 
     * @param services
     */
    @Resource(name = "linkServices")
    @Required
    public void setServices(Collection<IEmailLinkService> services) {
        serviceMap.clear();
        for (IEmailLinkService service : services) {
            registerService(service);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.ILinkServiceRegistry#getEmailLinkService(java.lang.String)
     */
    public IEmailLinkService getEmailLinkService(String key) {
        return serviceMap.get(key);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.ILinkServiceRegistry#getServices()
     */
    public Collection<IEmailLinkService> getServices() {
        return serviceMap.values();
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.ILinkServiceRegistry#registerService(org.jasig.portlet.emailpreview.service.IEmailLinkService)
     */
    public void registerService(IEmailLinkService linkService) {
        serviceMap.put(linkService.getKey(), linkService);
    }
    
}
