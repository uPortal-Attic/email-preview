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
package org.jasig.portlet.emailpreview.util;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import junit.framework.Assert;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.jasig.portlet.emailpreview.service.auth.CachedPasswordAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class EmailAccountUtilsTest {

    private IAuthenticationService     authService = null;

    private @Mock final PortletRequest request     = null;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put(CachedPasswordAuthenticationService.USERNAME_ATTRIBUTE, "testuser");
        userInfo.put(CachedPasswordAuthenticationService.PASSWORD_ATTRIBUTE, "pass");
        userInfo.put("mail", "joan@jetsons.com");

        when(request.getAttribute(PortletRequest.USER_INFO)).thenReturn(userInfo);

        authService = new CachedPasswordAuthenticationService();
    }

    @Test
    public void testEmailAddressWithCachedAuthNAndDifferentUsernameSuffix() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("imap.server.edu");
        config.setUsernameSuffix("@some.other.server.edu");
        config.setAuthenticationServiceKey(authService.getKey());

        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser@some.other.server.edu", emailAddress);
    }

    @Test
    public void testEmailAddressWithCachedAuthNAndFQHostName() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("imap.server.edu");
        config.setUsernameSuffix("@server.edu");
        config.setAuthenticationServiceKey(authService.getKey());

        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser@server.edu", emailAddress);
    }

    @Test
    public void testEmailAddressWithCachedAuthNAndIPAddressAsHost() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("123.456.789.000");
        config.setUsernameSuffix("@server.edu");
        config.setAuthenticationServiceKey(authService.getKey());

        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser@server.edu", emailAddress);
    }

    @Test
    public void testEmailAddressWithCachedAuthNAndIPAddressAsHostAndNoSuffix() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("123.56.89.255");
        config.setUsernameSuffix(null);
        config.setAuthenticationServiceKey(authService.getKey());

        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser", emailAddress);
    }

    @Test
    public void testEmailAddressWithCachedAuthNAndNoUsernameSuffix() {

        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("imap.server.edu");
        config.setAuthenticationServiceKey(authService.getKey());

        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser@server.edu", emailAddress);
    }

    @Test
    public void testDisplayMailAttributeNotUsernameSuffix() {

        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("imap.server.edu");
        config.setUsernameSuffix("@some.other.server.edu");
        config.setDisplayMailAttribute(true);
        config.setAuthenticationServiceKey(authService.getKey());

        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("joan@jetsons.com", emailAddress);
    }

    @Test
    public void testEWSSpecifyingUseMailAttribute() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setEwsUseMailAttribute(true);
        config.setProtocol(IServiceBroker.EXCHANGE_WEB_SERVICES);
        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("joan@jetsons.com", emailAddress);
    }

    @Test
    public void testEWSSpecifyingUseMailAttributeHaveSuffix() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setEwsUseMailAttribute(true);
        config.setUsernameSuffix("@goo.net"); // Ignored
        config.setProtocol(IServiceBroker.EXCHANGE_WEB_SERVICES);
        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("joan@jetsons.com", emailAddress);
    }

    @Test
    public void testEWSDomainUsernameWithSuffixNotUsingMailAttribute() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setEwsUseMailAttribute(false);
        config.setUsernameSuffix("@goo.net");
        config.setProtocol(IServiceBroker.EXCHANGE_WEB_SERVICES);
        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser@goo.net", emailAddress);
    }

    // Not really a normal scenario.  You should specify a username suffix if not using mail attribute
    // but on-premise EWS would ultimately just use the username so if you don't specify a suffix the
    // email address displayed for the user should still be logical.
    @Test
    public void testEWSDomainUsernameNoSuffixNotUsingMailAttribute() {
        final MailStoreConfiguration config = new MailStoreConfiguration();
        config.setHost("exchange.server.edu");
        config.setEwsUseMailAttribute(false);
        config.setProtocol(IServiceBroker.EXCHANGE_WEB_SERVICES);
        final String emailAddress = EmailAccountUtils.determineUserEmailAddress(request, config, authService);
        Assert.assertEquals("testuser@server.edu", emailAddress);
    }

}
