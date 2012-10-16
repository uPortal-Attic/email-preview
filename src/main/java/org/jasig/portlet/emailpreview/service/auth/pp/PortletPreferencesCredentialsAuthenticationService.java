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
package org.jasig.portlet.emailpreview.service.auth.pp;

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
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.SimplePasswordAuthenticator;
import org.springframework.stereotype.Component;

@Component("portletPreferencesCredentialsAuthenticationService")
public class PortletPreferencesCredentialsAuthenticationService implements IAuthenticationService {

    public static final String KEY = "portletPreferences";

    private static final String ACCOUNT_NAME_ATTRIBUTE_KEY = "PortletPreferencesCredentialsAuthenticationService.ACCOUNT_NAME_ATTRIBUTE";
    private static final String ACCOUNT_NAME_ATTRIBUTE_LABEL = "If specified, use this user attribute as the mail account name (users won't be able to edit it)";
    private static final ConfigurationParameter ACCOUNT_NAME_ATTRIBUTE = new ConfigurationParameter(
                ACCOUNT_NAME_ATTRIBUTE_KEY,    // key
                ACCOUNT_NAME_ATTRIBUTE_LABEL,  // label
                null,                          // defaultValue
                false                          // requiresEncryption
            );

    private final List<ConfigurationParameter> userParameters;
    private Map<String,ConfigurationParameter> configParams;

    public PortletPreferencesCredentialsAuthenticationService() {
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

    @Override
    public boolean isConfigured(PortletRequest req, MailStoreConfiguration config) {
        String mailAccount = getMailAccountName(req, config);
        String password = config.getAdditionalProperties().get(MailPreferences.PASSWORD.getKey());
        return (mailAccount != null && password != null);
    }

    public Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config) {
        String password = config.getAdditionalProperties().get(MailPreferences.PASSWORD.getKey());
        return new SimplePasswordAuthenticator(getMailAccountName(request, config), password);
    }

    public String getMailAccountName(PortletRequest req, MailStoreConfiguration config) {

        String rslt = null;

        /*
         * Does the account name come from user input or an attribute chosen by the admin?
         */
        final String accountNameAttribute = config.getAdditionalProperties().get(ACCOUNT_NAME_ATTRIBUTE_KEY);
        if (!StringUtils.isBlank(accountNameAttribute)) {
            // Chosen attribute
            @SuppressWarnings("unchecked")
            final Map<String,String> userInfo = (Map<String, String>) req.getAttribute(PortletRequest.USER_INFO);
            rslt = userInfo.get(accountNameAttribute);
        } else {
            // User input
            rslt = config.getAdditionalProperties().get(MailPreferences.MAIL_ACCOUNT.getKey());
        }

        // Use a suffix?
        final String suffix = config.getUsernameSuffix();
        if (rslt != null && !StringUtils.isBlank(suffix)) {
            rslt = rslt.concat(suffix);
        }

        return rslt;

    }

    public String getKey() {
        return KEY;
    }

    public List<ConfigurationParameter> getAdminConfigurationParameters() {
        return Collections.<ConfigurationParameter>singletonList(ACCOUNT_NAME_ATTRIBUTE);
    }

    public List<ConfigurationParameter> getUserConfigurationParameters() {
        return this.userParameters;
    }

}
