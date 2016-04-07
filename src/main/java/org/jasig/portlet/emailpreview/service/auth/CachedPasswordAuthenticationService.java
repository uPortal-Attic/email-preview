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
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CachedPasswordAuthenticationService extends BaseCredentialsAuthenticationService {
    
    private static final String KEY = "cachedPassword";
    public static final String USERNAME_ATTRIBUTE = "user.login.id";
    public static final String PASSWORD_ATTRIBUTE = "password";
    
    public CachedPasswordAuthenticationService() {
        Map<String,ConfigurationParameter> m = new HashMap<String,ConfigurationParameter>();
        for (ConfigurationParameter param : getAdminConfigurationParameters()) {
            m.put(param.getKey(), param);
        }
        for (ConfigurationParameter param : getUserConfigurationParameters()) {
            m.put(param.getKey(), param);
        }
        setConfigParams(Collections.unmodifiableMap(m));
    }

    @Override
    public boolean isConfigured(PortletRequest req, MailStoreConfiguration config) {
        String mailAccount = getMailAccountName(req, config);
        String password = getPassword(req);
        return (StringUtils.isNotBlank(mailAccount) && StringUtils.isNotBlank(password));
    }

    public Authenticator getAuthenticator(PortletRequest req, MailStoreConfiguration config) {
        return new SimplePasswordAuthenticator(getMailAccountName(req, config), getPassword(req));
    }

    public Credentials getCredentials(PortletRequest req, MailStoreConfiguration config) {
        String ntlmDomain = config.getAdditionalProperties().get(MailPreferences.EXCHANGE_DOMAIN.getKey());

        // If the domain is specified, we are authenticating to a domain so we need to return NT credentials
        if (StringUtils.isNotBlank(ntlmDomain)) {
            String username = getMailAccountName(req, config);
            return createNTCredentials(ntlmDomain, username, getPassword(req));
        }
        return new UsernamePasswordCredentials(getMailAccountName(req, config), getPassword(req));
    }

    public String getMailAccountName(PortletRequest request, MailStoreConfiguration config) {

        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String accountName = userInfo.get(USERNAME_ATTRIBUTE);
        return createMailAccountName(accountName, request, config);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IAuthenticationService#getKey()
     */
    public String getKey() {
        return KEY;
    }

    private String getPassword(PortletRequest req) {
        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) req.getAttribute(PortletRequest.USER_INFO);
        return userInfo.get(PASSWORD_ATTRIBUTE);
    }

}
