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
package org.jasig.portlet.emailpreview.dao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.impl.PortletPreferencesMailStoreDaoImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PortletPreferencesMailStoreDaoImplTest {
    
    private @Mock PortletPreferences preferences;
    private @Mock ActionRequest request;
    private MailStoreConfiguration configuration;
    private IMailStoreDao mailStoreDao;
    
    private String host = "imap.gmail.com";
    private int port = 993;
    private String protocol = "imaps";
    private String inboxName = "INBOX";
    private int timeout = -1;
    private int connectionTimeout = -1;
    private String inboxUrl = "http://mail.google.com";
    private String mailDebug = "true";
    private String linkServiceKey = "default";
    private String authServiceKey = "cachedPassword";
    
    private Map<String, String> preferenceMap;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mailStoreDao = new PortletPreferencesMailStoreDaoImpl();
        
        configuration = new MailStoreConfiguration();
        configuration.setHost("imap.gmail.com");
        configuration.setPort(993);
        configuration.setProtocol("imaps");
        configuration.setInboxFolderName("INBOX");
        configuration.setLinkServiceKey(linkServiceKey);
        configuration.setAuthenticationServiceKey(authServiceKey);
        configuration.setTimeout(-1);
        configuration.setConnectionTimeout(-1);
        configuration.getAdditionalProperties().put("inboxUrl", inboxUrl);
        configuration.getJavaMailProperties().put("mail.debug", mailDebug);
        
        preferenceMap = new HashMap<String, String>();
        preferenceMap.put("inboxUrl", inboxUrl);
        preferenceMap.put("mail.debug", mailDebug);
        
        when(request.getPreferences()).thenReturn(preferences);
        when(preferences.getValue("host", null)).thenReturn(host);
        when(preferences.getValue("protocol", null)).thenReturn(protocol);
        when(preferences.getValue("inboxName", null)).thenReturn(inboxName);
        when(preferences.getValue("linkServiceKey", null)).thenReturn(linkServiceKey);
        when(preferences.getValue("authenticationServiceKey", null)).thenReturn(authServiceKey);
        when(preferences.getValue("port", "25")).thenReturn(String.valueOf(port));
        when(preferences.getValue("timeout", "-1")).thenReturn(String.valueOf(timeout));
        when(preferences.getValue("connectionTimeout", "-1")).thenReturn(String.valueOf(connectionTimeout));
        when(preferences.getMap()).thenReturn(preferenceMap);
        
    }
    
    @Test
    public void testGetConfiguration() {
        MailStoreConfiguration config = mailStoreDao.getConfiguration(request);
        assert (config.equals(configuration));
    }
    
    @Test
    public void testStoreConfiguration() throws ReadOnlyException {
        mailStoreDao.saveConfiguration(request, configuration);
        
        verify(preferences).setValue("host", host);
        verify(preferences).setValue("protocol", protocol);
        verify(preferences).setValue("inboxName", inboxName);
        verify(preferences).setValue("port", String.valueOf(port));
        verify(preferences).setValue("timeout", String.valueOf(timeout));
        verify(preferences).setValue("connectionTimeout", String.valueOf(connectionTimeout));
        verify(preferences).setValue("inboxUrl", inboxUrl);
        verify(preferences).setValue("mail.debug", mailDebug);
    }

}
