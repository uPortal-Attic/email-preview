/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.emailpreview.service.link;

import java.util.Collection;

import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.jasig.portlet.emailpreview.service.link.LinkServiceRegistryImpl;
import org.jasig.portlet.emailpreview.service.link.SimpleEmailLinkServiceImpl;
import org.junit.Test;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class LinkServiceRegistryImplTest {
    
    @Test
    public void testRegistry() {
        IEmailLinkService linkService = new SimpleEmailLinkServiceImpl();
        ILinkServiceRegistry registry = new LinkServiceRegistryImpl();
        registry.registerService(linkService);
        
        Collection<IEmailLinkService> services = registry.getServices();
        assert services.size() == 1;
        
        IEmailLinkService defaultService = registry.getEmailLinkService("default");
        assert defaultService != null;
        
    }

}
