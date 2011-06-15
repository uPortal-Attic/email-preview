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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component("cachedPasswordAuthenticationService")
public class CachedPasswordAuthenticationServiceImpl implements IAuthenticationService {
    
    private static final String KEY = "cachedPassword";
    private static final String USERNAME_ATTRIBUTE = "user.login.id";
    private static final String PASSWORD_ATTRIBUTE = "password";
    
    private Map<String,ConfigurationParameter> configParams;
    
    public CachedPasswordAuthenticationServiceImpl() {
        Map<String,ConfigurationParameter> m = new HashMap<String,ConfigurationParameter>();
        for (ConfigurationParameter param : getAdminConfigurationParameters()) {
            m.put(param.getKey(), param);
        }
        for (ConfigurationParameter param : getUserConfigurationParameters()) {
            m.put(param.getKey(), param);
        }
        this.configParams = Collections.unmodifiableMap(m);
    }

    @Override
    public Map<String, ConfigurationParameter> getConfigurationParametersMap() {
        return configParams;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.service.IAuthenticationService#getAuthenticator(javax.portlet.PortletRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    public Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config) {
        
        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String password = userInfo.get(PASSWORD_ATTRIBUTE);

        return new SimplePasswordAuthenticator(getMailAccountName(request, config), password);

    }

    public String getMailAccountName(PortletRequest request, MailStoreConfiguration config) {

        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String username = userInfo.get(USERNAME_ATTRIBUTE);
        
        String usernameSuffix = config.getUsernameSuffix();
        if (!StringUtils.isBlank(usernameSuffix)) {
            username = username.concat(usernameSuffix);
        }
        
        return username;

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
