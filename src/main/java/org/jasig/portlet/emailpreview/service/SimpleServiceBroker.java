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
package org.jasig.portlet.emailpreview.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.dao.MailPreferences;
import org.jasig.portlet.emailpreview.security.IStringEncryptionService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 * @version $Revision: 24053 $
 */
public class SimpleServiceBroker implements IServiceBroker {
    
    private IAuthenticationServiceRegistry authServiceRegistry;
    private IStringEncryptionService stringEncryptionService;
    private Map<String,IEmailAccountService> services;
    private Set<String> protocols;
    private final Log log = LogFactory.getLog(this.getClass());

    protected static final List<String> RESERVED_PROPERTIES = Arrays.asList(
                new String[] { 
                    MailPreferences.HOST.getKey(), MailPreferences.PORT.getKey(), 
                    MailPreferences.INBOX_NAME.getKey(), MailPreferences.PROTOCOL.getKey(), 
                    MailPreferences.TIMEOUT.getKey(), MailPreferences.CONNECTION_TIMEOUT.getKey(), 
                    MailPreferences.LINK_SERVICE_KEY.getKey(), MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey(), 
                    MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey(), MailPreferences.USERNAME_SUFFIX.getKey(),
                    MailPreferences.EXCHANGE_USER_DOMAIN.getKey(), MailPreferences.EXCHANGE_AUTODISCOVER.getKey(),
                    MailPreferences.MARK_MESSAGES_AS_READ.getKey()
                });    
    
    public MailStoreConfiguration getConfiguration(PortletRequest request) {
        PortletPreferences prefs = request.getPreferences();
        
        MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost(prefs.getValue(MailPreferences.HOST.getKey(), null));
        config.setInboxFolderName(prefs.getValue(MailPreferences.INBOX_NAME.getKey(), null));
        config.setProtocol(prefs.getValue(MailPreferences.PROTOCOL.getKey(), "imaps"));
        config.setLinkServiceKey(prefs.getValue(MailPreferences.LINK_SERVICE_KEY.getKey(), "default"));
        config.setAuthenticationServiceKey(prefs.getValue(MailPreferences.AUTHENTICATION_SERVICE_KEY.getKey(), null));
        String[] authServiceKeys = prefs.getValues(MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey(), new String[0]);
        config.setAllowableAuthenticationServiceKeys(Arrays.asList(authServiceKeys));
        config.setUsernameSuffix(prefs.getValue(MailPreferences.USERNAME_SUFFIX.getKey(), null));
        config.setMarkMessagesAsRead(Boolean.valueOf(prefs.getValue(MailPreferences.MARK_MESSAGES_AS_READ.getKey(), "true")));
        
        String allowContent = prefs.getValue(MailPreferences.ALLOW_RENDERING_EMAIL_CONTENT.getKey(), "true");
        config.setAllowRenderingEmailContent(Boolean.valueOf(allowContent));

        config.setExchangeDomain(prefs.getValue(MailPreferences.EXCHANGE_USER_DOMAIN.getKey(), null));
        config.setExchangeAutodiscover(Boolean.valueOf(prefs.getValue(MailPreferences.EXCHANGE_AUTODISCOVER.getKey(), "false")));
        
        // set the port number
        try {
            int port = Integer.parseInt(prefs.getValue(MailPreferences.PORT.getKey(), "-1"));
            config.setPort(port);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        // set the connection timeout
        try {
            int connectionTimeout = Integer.parseInt(prefs.getValue(MailPreferences.CONNECTION_TIMEOUT.getKey(), "25000"));
            config.setConnectionTimeout(connectionTimeout);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        // set the timeout
        try {
            int timeout = Integer.parseInt(prefs.getValue(MailPreferences.TIMEOUT.getKey(), "25000"));
            config.setTimeout(timeout);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
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
        
        Map<String, String[]> preferenceMap = prefs.getMap();
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
                        if (stringEncryptionService == null) {
                            final String msg = "The following setting requires " +
                                    "encryption but the 'stringEncryptionService' " +
                                    "bean is not configured:  " + entry.getKey();
                            throw new IllegalStateException(msg);
                        }
                        try {
                            value = stringEncryptionService.decrypt(value);
                        } catch (EncryptionOperationNotPossibleException eonpe) {
                            log.warn("Failed to decrypt a configuration " +
                            		"parameter -- did the encrytion password " +
                            		"change?  (it shouldn't)", eonpe);
                            // provide a dummy value for safety (a blank value 
                            //would make the portlet seem unconfigured)
                            value = "xxx";  
                        }
                    }
                    config.getAdditionalProperties().put(key, value);
                }
            }
            
        }

        return config;
    }
    
    public void saveConfiguration(ActionRequest request, MailStoreConfiguration config) {
        
        PortletPreferences prefs = request.getPreferences();
        
        try {
            
            // Start with a clean slate
            Set<String> prefNames = new HashSet<String>(prefs.getMap().keySet());
            for (String name : prefNames) {
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
            if (!prefs.isReadOnly(MailPreferences.MARK_MESSAGES_AS_READ.getKey())) {
                prefs.setValue(MailPreferences.MARK_MESSAGES_AS_READ.getKey(), String.valueOf(config.getMarkMessagesAsRead()));
            }
            if (!prefs.isReadOnly(MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey())) {
                prefs.setValues(MailPreferences.ALLOWABLE_AUTHENTICATION_SERVICE_KEYS.getKey(),
                        config.getAllowableAuthenticationServiceKeys().toArray(new String[0]));
            }
            if (!prefs.isReadOnly(MailPreferences.USERNAME_SUFFIX.getKey())) {
                prefs.setValue(MailPreferences.USERNAME_SUFFIX.getKey(), config.getUsernameSuffix());
            }
            
            if (!prefs.isReadOnly(MailPreferences.ALLOW_RENDERING_EMAIL_CONTENT.getKey())) {
                prefs.setValue(MailPreferences.ALLOW_RENDERING_EMAIL_CONTENT.getKey(), 
                                String.valueOf(config.getAllowRenderingEmailContent()));
            }
            if (!prefs.isReadOnly(MailPreferences.EXCHANGE_USER_DOMAIN.getKey())) {
                prefs.setValue(MailPreferences.EXCHANGE_USER_DOMAIN.getKey(), config.getExchangeDomain());
            }
            if (!prefs.isReadOnly(MailPreferences.EXCHANGE_AUTODISCOVER.getKey())) {
                prefs.setValue(MailPreferences.EXCHANGE_AUTODISCOVER.getKey(), String.valueOf(config.isExchangeAutodiscover()));
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
                        if (stringEncryptionService == null) {
                            final String msg = "The following setting requires " +
                            		"encryption but the 'stringEncryptionService' " +
                            		"bean is not configured:  " + entry.getKey();
                            throw new IllegalStateException(msg);
                        }
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

    @Override
    public IEmailAccountService getEmailAccountService(PortletRequest request) {
        PortletPreferences prefs = request.getPreferences();
        String protocol = prefs.getValue(MailPreferences.PROTOCOL.getKey(), null);
        return services.get(protocol);
    }

    @Autowired
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }

    @Autowired(required = false)
    public void setStringEncryptionService(IStringEncryptionService stringEncryptionService) {
        this.stringEncryptionService = stringEncryptionService;
    }

    public Map<String, IEmailAccountService> getServices() {
        return services;
    }

    public void setServices(Map<String, IEmailAccountService> services) {
        this.services = services;
        this.protocols = Collections.unmodifiableSortedSet(new TreeSet<String>(services.keySet()));
    }

    public Set<String> getSupportedProtocols() {
        return protocols;
    }

}
