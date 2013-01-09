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
package org.jasig.portlet.emailpreview;

import org.jasig.portlet.emailpreview.dao.EmailAccountDaoImplIntegrationTest;
import org.jasig.portlet.emailpreview.dao.SimpleServiceBrokerTest;
import org.jasig.portlet.emailpreview.service.auth.CachedPasswordAuthenticationServiceTest;
import org.jasig.portlet.emailpreview.service.link.LinkServiceRegistryImplTest;
import org.jasig.portlet.emailpreview.service.link.SimpleEmailLinkServiceImplTest;
import org.jasig.portlet.emailpreview.util.EmailAccountUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses( { EmailMessageTest.class, 
                 EmailAccountDaoImplIntegrationTest.class, 
                 SimpleServiceBrokerTest.class,
                 SimpleServiceBrokerTest.class,
                 CachedPasswordAuthenticationServiceTest.class,
                 LinkServiceRegistryImplTest.class,
                 SimpleEmailLinkServiceImplTest.class,
                 EmailAccountUtilsTest.class
               })
public class PortletTestSuite {}
