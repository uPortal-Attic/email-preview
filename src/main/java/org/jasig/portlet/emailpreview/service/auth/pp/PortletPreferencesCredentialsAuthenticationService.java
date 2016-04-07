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
package org.jasig.portlet.emailpreview.service.auth.pp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.jasig.portlet.emailpreview.service.auth.BaseCredentialsAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.SimplePasswordAuthenticator;

public class PortletPreferencesCredentialsAuthenticationService extends BaseCredentialsAuthenticationService {

    public static final String KEY = "portletPreferences";

    private static final String ACCOUNT_NAME_ATTRIBUTE_KEY = "PortletPreferencesCredentialsAuthenticationService.ACCOUNT_NAME_ATTRIBUTE";
    private static final String ACCOUNT_NAME_ATTRIBUTE_LABEL = "If specified, use this user attribute as the mail account name (users won't be able to edit it)";
    private static final ConfigurationParameter ACCOUNT_NAME_ATTRIBUTE = new ConfigurationParameter(
                ACCOUNT_NAME_ATTRIBUTE_KEY,    // key
                ACCOUNT_NAME_ATTRIBUTE_LABEL,  // label
                null,                          // defaultValue
                false                          // requiresEncryption
            );

    public PortletPreferencesCredentialsAuthenticationService() {
        List<ConfigurationParameter> params = new ArrayList<ConfigurationParameter>();

        ConfigurationParameter usernameParam = new ConfigurationParameter();
        usernameParam.setKey(MailPreferences.MAIL_ACCOUNT.getKey());
        usernameParam.setLabel("Mail account name");
        usernameParam.setEncryptionRequired(true);
        params.add(usernameParam);

        ConfigurationParameter passwordParam = new ConfigurationParameter();
        passwordParam.setKey(MailPreferences.PASSWORD.getKey());
        passwordParam.setLabel("Password");
        passwordParam.setEncryptionRequired(true);
        params.add(passwordParam);

        setUserParameters(Collections.unmodifiableList(params));
        setAdminParameters(Collections.<ConfigurationParameter>singletonList(PortletPreferencesCredentialsAuthenticationService.ACCOUNT_NAME_ATTRIBUTE));

        Map<String,ConfigurationParameter> m = new HashMap<String,ConfigurationParameter>();
        for (ConfigurationParameter param : userParameters) {
            m.put(param.getKey(), param);
        }
        setConfigParams(Collections.unmodifiableMap(m));

    }

    @Override
    public boolean isConfigured(PortletRequest req, MailStoreConfiguration config) {
        String mailAccount = getMailAccountName(req, config);
        String password = config.getAdditionalProperties().get(MailPreferences.PASSWORD.getKey());
        return (StringUtils.isNotBlank(mailAccount) && StringUtils.isNotBlank(password));
    }

    public Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config) {
        String password = config.getAdditionalProperties().get(MailPreferences.PASSWORD.getKey());
        return new SimplePasswordAuthenticator(getMailAccountName(request, config), password);
    }

    public Credentials getCredentials(PortletRequest req, MailStoreConfiguration config) {
        String ntlmDomain = config.getAdditionalProperties().get(MailPreferences.EXCHANGE_DOMAIN.getKey());
        String password = config.getAdditionalProperties().get(MailPreferences.PASSWORD.getKey());

        // If the domain is specified, we are authenticating to a domain so we need to return NT credentials
        if (StringUtils.isNotBlank(ntlmDomain)) {
            String username = getMailAccountName(req, config);
            return createNTCredentials(ntlmDomain, username, password);
        }

        return new UsernamePasswordCredentials(getMailAccountName(req, config), password);
    }

    public String getMailAccountName(PortletRequest req, MailStoreConfiguration config) {

        String accountName = null;

        /*
         * Does the account name come from user input or an attribute chosen by the admin?
         */
        final String accountNameAttribute = config.getAdditionalProperties().get(ACCOUNT_NAME_ATTRIBUTE_KEY);
        if (StringUtils.isNotBlank(accountNameAttribute)) {
            // Chosen attribute
            @SuppressWarnings("unchecked")
            final Map<String,String> userInfo = (Map<String, String>) req.getAttribute(PortletRequest.USER_INFO);
            accountName = userInfo.get(accountNameAttribute);
        } else {
            // User input
            accountName = config.getAdditionalProperties().get(MailPreferences.MAIL_ACCOUNT.getKey());
        }
        return createMailAccountName(accountName, req, config);
    }

    public String getKey() {
        return KEY;
    }

}
