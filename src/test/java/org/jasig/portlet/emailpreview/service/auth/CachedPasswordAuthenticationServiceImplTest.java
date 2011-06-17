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

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.auth.SimplePasswordAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CachedPasswordAuthenticationServiceImplTest {
    
    CachedPasswordAuthenticationServiceImpl authService;
    MailStoreConfiguration configuration;
    @Mock PortletRequest request;
        
    @Before
    public void setUp() { 
        MockitoAnnotations.initMocks(this);
        
        authService = new CachedPasswordAuthenticationServiceImpl();
        configuration = new MailStoreConfiguration();
        
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("user.login.id", "testuser");
        userInfo.put("password", "pass");
        
        when(request.getAttribute(PortletRequest.USER_INFO)).thenReturn(userInfo);
    }
    
    @Test
    public void testGetAuthenticator() {
        SimplePasswordAuthenticator auth = (SimplePasswordAuthenticator) authService.getAuthenticator(request, configuration);
        SimplePasswordAuthenticator expected = new SimplePasswordAuthenticator("testuser", "pass");
        
        assert expected.equals(auth);
    }


}
