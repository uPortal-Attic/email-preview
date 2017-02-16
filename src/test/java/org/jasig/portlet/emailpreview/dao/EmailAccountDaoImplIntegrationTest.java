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

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmailAccountDaoImplIntegrationTest {

  GreenMail greenMail;

  private MailStoreConfiguration testMailStore;

  @Before
  public void setUp() throws InterruptedException {

    testMailStore = new MailStoreConfiguration();
    testMailStore.setHost("localhost");
    testMailStore.setPort(3993);
    testMailStore.setProtocol("imaps");

    greenMail = new GreenMail(); //uses test ports by default
    greenMail.start();
    GreenMailUtil.sendTextEmailTest(
        "to@localhost.com",
        "from@localhost.com",
        "subject",
        "body"); //replace this with your send code
    assert greenMail.waitForIncomingEmail(5000, 1);
  }

  @Test
  public void test() throws UnknownHostException, IOException, InterruptedException {
    assert "body".equals(GreenMailUtil.getBody(greenMail.getReceivedMessages()[0]));
  }

  @After
  public void tearDown() {
    greenMail.stop();
  }
}
