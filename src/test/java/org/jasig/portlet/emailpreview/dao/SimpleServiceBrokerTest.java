/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.emailpreview.dao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.SimpleServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.CachedPasswordAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SimpleServiceBrokerTest {

  private @Mock PortletPreferences preferences;
  private @Mock ActionRequest request;
  private MailStoreConfiguration configuration;
  private SimpleServiceBroker serviceBroker;

  private String host = "imap.gmail.com";
  private int port = 993;
  private String protocol = "imaps";
  private String inboxName = "INBOX";
  private int timeout = 5000;
  private int connectionTimeout = 6000;
  private String inboxUrl = "http://mail.google.com";
  private String mailDebug = "true";
  private String linkServiceKey = "default";
  private String authServiceKey = "cachedPassword";
  private String[] allowableAuthServiceKeys = new String[] {"cachedPassword"};

  private Map<String, String[]> preferenceMap;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    serviceBroker = new SimpleServiceBroker();
    final IAuthenticationService authServ = new CachedPasswordAuthenticationService();
    IAuthenticationServiceRegistry authServiceRegistry =
        new IAuthenticationServiceRegistry() {
          @Override
          public IAuthenticationService getAuthenticationService(String key) {
            return authServ.getKey().equalsIgnoreCase(key) ? authServ : null;
          }

          @Override
          public Collection<IAuthenticationService> getServices() {
            return Arrays.asList(new IAuthenticationService[] {authServ});
          }
        };
    serviceBroker.setAuthenticationServiceRegistry(authServiceRegistry);

    configuration = new MailStoreConfiguration();
    configuration.setHost("imap.gmail.com");
    configuration.setPort(993);
    configuration.setProtocol("imaps");
    configuration.setInboxFolderName("INBOX");
    configuration.setLinkServiceKey(linkServiceKey);
    configuration.setAllowableAuthenticationServiceKeys(
        Arrays.asList(new String[] {authServiceKey}));
    configuration.setAuthenticationServiceKey(authServiceKey);
    configuration.setTimeout(timeout);
    configuration.setConnectionTimeout(connectionTimeout);
    configuration.getAdditionalProperties().put("inboxUrl", inboxUrl);
    configuration.getJavaMailProperties().put("mail.debug", mailDebug);
    configuration.setAllowRenderingEmailContent(true);

    preferenceMap = new HashMap<String, String[]>();
    preferenceMap.put("inboxUrl", new String[] {inboxUrl});
    preferenceMap.put("mail.debug", new String[] {mailDebug});

    when(request.getPreferences()).thenReturn(preferences);
    when(preferences.getNames())
        .thenReturn(
            new Enumeration<String>() {
              public boolean hasMoreElements() {
                return false;
              }

              public String nextElement() {
                return null;
              }
            });
    when(preferences.getValue("host", null)).thenReturn(host);
    when(preferences.getValue("port", "-1")).thenReturn(String.valueOf(port));
    when(preferences.getValue("protocol", "imaps")).thenReturn(protocol);
    when(preferences.getValue("inboxName", null)).thenReturn(inboxName);
    when(preferences.getValue("linkServiceKey", "default")).thenReturn(linkServiceKey);
    when(preferences.getValues("allowableAuthenticationServiceKeys", new String[0]))
        .thenReturn(allowableAuthServiceKeys);
    when(preferences.getValue("authenticationServiceKey", null)).thenReturn(authServiceKey);
    when(preferences.getValue("timeout", "25000")).thenReturn(String.valueOf(timeout));
    when(preferences.getValue("connectionTimeout", "25000"))
        .thenReturn(String.valueOf(connectionTimeout));
    when(preferences.getMap()).thenReturn(preferenceMap);
  }

  @Test
  public void testGetConfiguration() {
    MailStoreConfiguration config = serviceBroker.getConfiguration(request);
    assert (config.equals(configuration));
  }

  @Test
  public void testStoreConfiguration() throws ReadOnlyException {
    serviceBroker.saveConfiguration(request, configuration);

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
