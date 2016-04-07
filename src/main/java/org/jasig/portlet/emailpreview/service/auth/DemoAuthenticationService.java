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
package org.jasig.portlet.emailpreview.service.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.Credentials;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;

/**
 * 
 * @author Landis Casner
 * @author Drew Wills
 * @version $Revision$
 */
public class DemoAuthenticationService implements IAuthenticationService {
    
    private static final String KEY = "demoAuthService";
    private static final String USERNAME_ATTRIBUTE = "user.login.id";
    
    @Override
    public Map<String, ConfigurationParameter> getConfigurationParametersMap() {
        Map<String,ConfigurationParameter> rslt = new HashMap<String,ConfigurationParameter>();
        for (ConfigurationParameter param : getAdminConfigurationParameters()) {
            rslt.put(param.getKey(), param);
        }
        for (ConfigurationParameter param : getUserConfigurationParameters()) {
            rslt.put(param.getKey(), param);
        }
        return rslt;
    }

    @Override
    public boolean isConfigured(PortletRequest req, MailStoreConfiguration config) {
        return true;
    }

    public Authenticator getAuthenticator(PortletRequest req, MailStoreConfiguration config) {
        throw new UnsupportedOperationException();
    }

    public Credentials getCredentials(PortletRequest req, MailStoreConfiguration config) {
        throw new UnsupportedOperationException();
    }

    public String getMailAccountName(PortletRequest request, MailStoreConfiguration config) {

        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String rslt = userInfo.get(USERNAME_ATTRIBUTE);
        String usernameSuffix = config.getUsernameSuffix();
        if (rslt != null && !StringUtils.isBlank(usernameSuffix)) {
            rslt = rslt.concat(usernameSuffix);
        }
        return rslt;

    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IAuthenticationService#getKey()
     */
    public String getKey() {
        return KEY;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.auth.IAuthenticationService#getAdminConfigurationParameters()
     */
    public List<ConfigurationParameter> getAdminConfigurationParameters() {
        return Collections.<ConfigurationParameter>emptyList();
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.auth.IAuthenticationService#getUserConfigurationParameters()
     */
    public List<ConfigurationParameter> getUserConfigurationParameters() {
        return Collections.<ConfigurationParameter>emptyList();
    }

}
