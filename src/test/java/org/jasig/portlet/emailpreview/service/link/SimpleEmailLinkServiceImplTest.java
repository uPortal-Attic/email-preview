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
package org.jasig.portlet.emailpreview.service.link;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/** @author Jen Bourey, jbourey@unicon.net */
public class SimpleEmailLinkServiceImplTest {

  SimpleEmailLinkServiceImpl linkService;
  MailStoreConfiguration configuration;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    linkService = new SimpleEmailLinkServiceImpl();

    configuration = new MailStoreConfiguration();
    configuration
        .getAdditionalProperties()
        .put(SimpleEmailLinkServiceImpl.INBOX_URL_PROPERTY, "http://mail.google.com");
  }

  @Test
  public void testGetInboxUrl() {
    String inboxUrl = linkService.getInboxUrl(configuration);
    assert "http://mail.google.com".equals(inboxUrl);
  }
}
