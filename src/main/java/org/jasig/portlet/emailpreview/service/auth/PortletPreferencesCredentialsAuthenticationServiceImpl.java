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
import java.util.List;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;

public class PortletPreferencesCredentialsAuthenticationServiceImpl implements
        IAuthenticationService {
    
    protected static final String KEY = "portletPreferences";
    protected static final String USERNAME_KEY = "username";
    protected static final String PASSWORD_KEY = "password";
    
    private final List<ConfigurationParameter> userParameters;
    
    public PortletPreferencesCredentialsAuthenticationServiceImpl() {
        List<ConfigurationParameter> params = new ArrayList<ConfigurationParameter>();
        
        ConfigurationParameter usernameParam = new ConfigurationParameter();
        usernameParam.setKey(USERNAME_KEY);
        usernameParam.setLabel("Inbox folder name");
        usernameParam.setRequiresEncryption(true);
        params.add(usernameParam);

        ConfigurationParameter passwordParam = new ConfigurationParameter();
        passwordParam.setKey(PASSWORD_KEY);
        passwordParam.setLabel("Inbox folder name");
        passwordParam.setRequiresEncryption(true);
        params.add(passwordParam);
        
        this.userParameters = params;

    }

    public Authenticator getAuthenticator(PortletRequest request,
            MailStoreConfiguration config) {
        
        String username = config.getAdditionalProperties().get(USERNAME_KEY);
        String password = config.getAdditionalProperties().get(PASSWORD_KEY);
        
        return new SimplePasswordAuthenticator(username, password);
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
