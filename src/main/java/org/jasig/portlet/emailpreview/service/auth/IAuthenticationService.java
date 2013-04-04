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

import java.util.List;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.http.auth.Credentials;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;

public interface IAuthenticationService {

    /**
     * Return the unique key for this authentication service.  This key will be used
     * to retrieve an authentication service instance from the registry.
     * 
     * @return
     */
    String getKey();
    
    /**
     * <code>True</code> if the specified parameters contain enough information 
     * to build a valid <code>Authenticator</code> if they were passed to the 
     * {@link #getAuthenticator(PortletRequest, MailStoreConfiguration)} method.
     * 
     * @param request
     * @param config
     * @return
     */
    boolean isConfigured(PortletRequest request, MailStoreConfiguration config);

    /**
     * 
     * @param request
     * @param config
     * @return
     */
    Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config);

    /**
     * Returns a credentials object.  This is for Exchange integration.
     * @param req
     * @param config
     * @return
     */
    Credentials getCredentials(PortletRequest req, MailStoreConfiguration config);
    
    String getMailAccountName(PortletRequest request, MailStoreConfiguration config);

    List<ConfigurationParameter> getAdminConfigurationParameters();
    
    List<ConfigurationParameter> getUserConfigurationParameters();
    
    Map<String,ConfigurationParameter> getConfigurationParametersMap();

}
