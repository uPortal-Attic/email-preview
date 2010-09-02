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
package org.jasig.portlet.emailpreview.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.security.IStringEncryptionService;
import org.jasig.portlet.emailpreview.service.ConfigurationParameter;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Component
public class PortletPreferencesMailStoreDaoImpl implements IMailStoreDao {
    
    private IAuthenticationServiceRegistry authServiceRegistry;
    private IStringEncryptionService stringEncryptionService;

    protected static final List<String> RESERVED_PROPERTIES = Arrays.asList(
                new String[] { 
                    MailPreferences.HOST.getKey(), MailPreferences.PORT.getKey(), 
                    MailPreferences.INBOX_NAME.getKey(), MailPreferences.PROTOCOL.getKey(), 
                    MailPreferences.TIMEOUT.getKey(), MailPreferences.CONNECTION_TIMEOUT.getKey(), 
                    MailPreferences.LINK_SERVICE_KEY.getKey(), MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey(), 
                    MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey()
                });    
    
    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao#getConfiguration(javax.portlet.PortletRequest)
     */
    public MailStoreConfiguration getConfiguration(PortletRequest request) {
        
        PortletPreferences preferences = request.getPreferences();
        
        MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost(preferences.getValue(MailPreferences.HOST.getKey(), null));
        config.setInboxFolderName(preferences.getValue(MailPreferences.INBOX_NAME.getKey(), null));
        config.setProtocol(preferences.getValue(MailPreferences.PROTOCOL.getKey(), null));
        config.setLinkServiceKey(preferences.getValue(MailPreferences.LINK_SERVICE_KEY.getKey(), null));
        config.setAuthenticationServiceKey(preferences.getValue(MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey(), null));
        String[] authServiceKeys = preferences.getValues(MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey(), new String[0]);
        config.setAllowableAuthenticationServiceKeys(Arrays.asList(authServiceKeys));
        
        // set the port number
        try {
            int port = Integer.parseInt(preferences.getValue(MailPreferences.PORT.getKey(), "25"));
            config.setPort(port);
        } catch (NumberFormatException e) {
        }

        // set the connection timeout
        try {
            int connectionTimeout = Integer.parseInt(preferences.getValue(MailPreferences.CONNECTION_TIMEOUT.getKey(), "-1"));
            config.setConnectionTimeout(connectionTimeout);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        // set the timeout
        try {
            int timeout = Integer.parseInt(preferences.getValue(MailPreferences.TIMEOUT.getKey(), "-1"));
            config.setTimeout(timeout);
        } catch (NumberFormatException e) {
        }
        
        
        /*
         * Iterate through the preferences map, adding all preferences not 
         * handled above to either the java mail properties map or the 
         * arbitrary properties map as appropriate.
         * 
         * This code assumes that all java mail properties begin with
         * "mail." and does now allow administrators to define arbitrary
         * properties beginning with that string.
         */
        Map<String,ConfigurationParameter> allParams = Collections.emptyMap();  // default
        String authKey = config.getAuthenticationServiceKey();
        IAuthenticationService authServ = authKey != null 
                            ? authServiceRegistry.getAuthenticationService(authKey)
                            : null;  // need Elvis operator ?:
        if (authServ != null) {
            allParams = authServ.getConfigurationParametersMap();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String[]> preferenceMap = preferences.getMap();
        for (Map.Entry<String, String[]> entry : preferenceMap.entrySet()) {
            
            String key = entry.getKey();
            if (!RESERVED_PROPERTIES.contains(key) && entry.getValue().length > 0) {
                String value = entry.getValue()[0];

                if (key.startsWith("mail.")) {
                    config.getJavaMailProperties().put(key, value);
                } else {
                    // AuthN properties may require encryption
                    ConfigurationParameter param = allParams.get(key);
                    if (param != null && param.isEncryptionRequired()) {
                        value = stringEncryptionService.decrypt(value);
                    }
                    config.getAdditionalProperties().put(key, value);
                }
            }
            
        }

        return config;
    }
    
    public boolean isReadOnly(PortletRequest req, MailPreferences mp) {
        PortletPreferences prefs = req.getPreferences();
        return prefs.isReadOnly(mp.getKey());
    }

    
    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao#saveConfiguration(javax.portlet.ActionRequest, org.jasig.portlet.emailpreview.MailStoreConfiguration)
     */
    @SuppressWarnings("unchecked")
    public void saveConfiguration(ActionRequest request, MailStoreConfiguration config) {
        
        PortletPreferences prefs = request.getPreferences();
        
        try {
            
            // Start with a clean slate
            for (Enumeration<String> prefNames = prefs.getNames(); prefNames.hasMoreElements();) {
                String name = prefNames.nextElement();
                if (!prefs.isReadOnly(name)) {
                    prefs.reset(name);
                }
            }
            
            // Reserved Properties
            if (!prefs.isReadOnly(MailPreferences.HOST.getKey())) {
                prefs.setValue(MailPreferences.HOST.getKey(), config.getHost());
            }
            if (!prefs.isReadOnly(MailPreferences.PROTOCOL.getKey())) {
                prefs.setValue(MailPreferences.PROTOCOL.getKey(), config.getProtocol());
            }
            if (!prefs.isReadOnly(MailPreferences.INBOX_NAME.getKey())) {
                prefs.setValue(MailPreferences.INBOX_NAME.getKey(), config.getInboxFolderName());
            }
            if (!prefs.isReadOnly(MailPreferences.PORT.getKey())) {
                prefs.setValue(MailPreferences.PORT.getKey(), String.valueOf(config.getPort()));
            }
            if (!prefs.isReadOnly(MailPreferences.CONNECTION_TIMEOUT.getKey())) {
                prefs.setValue(MailPreferences.CONNECTION_TIMEOUT.getKey(), String.valueOf(config.getConnectionTimeout()));
            }
            if (!prefs.isReadOnly(MailPreferences.TIMEOUT.getKey())) {
                prefs.setValue(MailPreferences.TIMEOUT.getKey(), String.valueOf(config.getTimeout()));
            }
            if (!prefs.isReadOnly(MailPreferences.LINK_SERVICE_KEY.getKey())) {
                prefs.setValue(MailPreferences.LINK_SERVICE_KEY.getKey(), String.valueOf(config.getLinkServiceKey()));
            }
            if (!prefs.isReadOnly(MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey())) {
                prefs.setValue(MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey(), config.getAuthenticationServiceKey());
            }
            if (!prefs.isReadOnly(MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey())) {
                prefs.setValues(MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey(), config.getAllowableAuthenticationServiceKeys().toArray(new String[0]));
            }

            // JavaMail properties
            for (Map.Entry<String, String> entry : config.getJavaMailProperties().entrySet()) {
                if (!prefs.isReadOnly(entry.getKey())) {
                    prefs.setValue(entry.getKey(), entry.getValue());
                }
            }

            // Additional properties (authN, etc.)
            Map<String,ConfigurationParameter> allParams = Collections.emptyMap();  // default
            String authKey = config.getAuthenticationServiceKey();
            IAuthenticationService authServ = authKey != null 
                                ? authServiceRegistry.getAuthenticationService(authKey)
                                : null;  // need Elvis operator ?:
            if (authServ != null) {
                allParams = authServ.getConfigurationParametersMap();
            }
            for (Map.Entry<String, String> entry : config.getAdditionalProperties().entrySet()) {
                if (!prefs.isReadOnly(entry.getKey())) {
                    String value = entry.getValue();
                    ConfigurationParameter param = allParams.get(entry.getKey());
                    if (param != null && param.isEncryptionRequired()) {
                        value = stringEncryptionService.encrypt(value);
                    }
                    prefs.setValue(entry.getKey(), value);
                }
            }

            prefs.store();

        } catch (Exception e) {
            throw new RuntimeException("Failed to store configuration", e);
        }
        
    }

    @Autowired(required = true)
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    @Autowired(required = true)
    public void setStringEncryptionService(IStringEncryptionService stringEncryptionService) {
        this.stringEncryptionService = stringEncryptionService;
    }

}
