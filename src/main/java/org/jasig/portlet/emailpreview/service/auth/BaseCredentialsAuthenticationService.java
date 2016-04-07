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
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public abstract class BaseCredentialsAuthenticationService implements IAuthenticationService {
    private static final String MAIL_ATTRIBUTE = "mail";

    protected List<ConfigurationParameter> userParameters = Collections.<ConfigurationParameter>emptyList();
    protected List<ConfigurationParameter> adminParameters = Collections.<ConfigurationParameter>emptyList();
    protected Map<String,ConfigurationParameter> configParams = Collections.<String, ConfigurationParameter>emptyMap();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, ConfigurationParameter> getConfigurationParametersMap() {
        return configParams;
    }

    @Override
    public List<ConfigurationParameter> getAdminConfigurationParameters() {
        return adminParameters;
    }

    @Override
    public List<ConfigurationParameter> getUserConfigurationParameters() {
        return userParameters;
    }

    protected void setUserParameters(List<ConfigurationParameter> params) {
        userParameters = params;
    }

    protected void setAdminParameters(List<ConfigurationParameter> params) {
        adminParameters = params;
    }

    protected void setConfigParams(Map<String, ConfigurationParameter> params) {
        configParams = params;
    }

    protected String createMailAccountName(String accountName, PortletRequest req, MailStoreConfiguration config) {
        // If using Exchange Web Services and configured for using the email attribute instead of
        // the username (optionally plus suffix), return the username.
        String emailAddress;
        if (IServiceBroker.EXCHANGE_WEB_SERVICES.equals(config.getProtocol()) && config.isEwsUseMailAttribute()) {
            final Map<String,String> userInfo = (Map<String, String>) req.getAttribute(PortletRequest.USER_INFO);
            emailAddress = userInfo.get(MAIL_ATTRIBUTE);
            if (StringUtils.isNotBlank(emailAddress)) {
                return emailAddress;
            }
            log.warn("Using EWS, configured to use mail attribute, and no email address in user attribute {}" +
                " for user {}. Falling back to constructing one from username", MAIL_ATTRIBUTE, accountName);
        }
        // Use a suffix?
        final String suffix = config.getUsernameSuffix();
        if (accountName != null && StringUtils.isNotBlank(suffix)) {
            return accountName.concat(suffix);
        }

        return accountName;
    }

    protected Credentials createNTCredentials(String ntlmDomain, String username, String password) {
        // For Exchange integration, only the username is applicable, not the email address.  If present
        // remove the @domain part of an email address in case the user or admin specified an email address
        // and a password in the user config UI.
        int index = username.indexOf("@");
        username = index > 0 ? username.substring(0, index) : username;

        // construct a credentials object from the username and password
        return new NTCredentials(username, password, "paramDoesNotSeemToMatter", ntlmDomain);
    }
}
