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
package org.jasig.portlet.emailpreview.service;

import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;

/**
 * Provides access to the various services (Email, AuthN, etc) in the portlet.
 * This interfce simplifies Spring configuration in that controllers need only
 * have access to this broker, not a blend of 3-4 independent services.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 * @version $Revision: 21577 $
 */
public interface IServiceBroker {

    /**
     * 
     * 
     * @param request
     * @return
     */
    MailStoreConfiguration getConfiguration(PortletRequest request);

    /**
     * 
     * @param request
     * @param config
     */
    void saveConfiguration(ActionRequest request, MailStoreConfiguration config);

    /**
     * Returns the appropriate email account service for this request.
     * @param request Request
     * @return Email account service.
     */
    IEmailAccountService getEmailAccountService(PortletRequest request);

    Set<String> getSupportedProtocols();
}