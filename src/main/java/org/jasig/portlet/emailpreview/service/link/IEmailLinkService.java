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

import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * IEmailLinkService provides links to an external webmail client.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface IEmailLinkService {
   
    /**
     * Return the unique key for this link service.  This key will be used
     * to retrieve a link service instances from the registry.
     * 
     * @return
     */
    public String getKey();

    /**
     * Get the URL of the inbox for this portlet request and mail
     * store configuration.  This method may simply provide the URL of an 
     * external webmail client.  Some implementations may wish to support
     * SSO or implement other interesting client-specific URLs.
     * 
     * @param request
     * @param config
     * @return
     */
    public String getInboxUrl(PortletRequest request, MailStoreConfiguration config);

}
