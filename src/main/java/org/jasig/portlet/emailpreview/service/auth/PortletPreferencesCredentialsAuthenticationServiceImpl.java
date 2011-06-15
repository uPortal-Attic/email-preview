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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.springframework.stereotype.Component;

@Component("portletPreferencesCredentialsAuthenticationService")
public class PortletPreferencesCredentialsAuthenticationServiceImpl implements IAuthenticationService {
    
    public static final String KEY = "portletPreferences";
    
    private final List<ConfigurationParameter> userParameters;
    private Map<String,ConfigurationParameter> configParams;
    
    public PortletPreferencesCredentialsAuthenticationServiceImpl() {
        List<ConfigurationParameter> params = new ArrayList<ConfigurationParameter>();
        
        ConfigurationParameter usernameParam = new ConfigurationParameter();
        usernameParam.setKey(MailPreferences.MAIL_ACCOUNT.getKey());
        usernameParam.setLabel("Inbox folder name");
        usernameParam.setEncryptionRequired(true);
        params.add(usernameParam);

        ConfigurationParameter passwordParam = new ConfigurationParameter();
        passwordParam.setKey(MailPreferences.PASSWORD.getKey());
        passwordParam.setLabel("Inbox folder name");
        passwordParam.setEncryptionRequired(true);
        params.add(passwordParam);
        
        this.userParameters = Collections.unmodifiableList(params);

        Map<String,ConfigurationParameter> m = new HashMap<String,ConfigurationParameter>();
        for (ConfigurationParameter param : userParameters) {
            m.put(param.getKey(), param);
        }
        this.configParams = Collections.unmodifiableMap(m);

    }

    @Override
    public Map<String, ConfigurationParameter> getConfigurationParametersMap() {
        return configParams;
    }

    public Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config) {
        String password = config.getAdditionalProperties().get(MailPreferences.PASSWORD.getKey());
        return new SimplePasswordAuthenticator(getMailAccountName(request, config), password);
    }
    
    public String getMailAccountName(PortletRequest request, MailStoreConfiguration config) {
        String username = config.getAdditionalProperties().get(MailPreferences.MAIL_ACCOUNT.getKey());
        String usernameSuffix = config.getUsernameSuffix();
        if (!StringUtils.isBlank(usernameSuffix)) {
            username = username.concat(usernameSuffix);
        }
        return username;
    }

    public String getKey() {
        return KEY;
    }

    public List<ConfigurationParameter> getAdminConfigurationParameters() {
        return Collections.<ConfigurationParameter>emptyList();
    }

    public List<ConfigurationParameter> getUserConfigurationParameters() {
        return this.userParameters;
    }

}
