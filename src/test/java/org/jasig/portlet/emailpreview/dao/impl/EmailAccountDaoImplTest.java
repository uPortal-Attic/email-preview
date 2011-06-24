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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

public class EmailAccountDaoImplTest {

    @Mock Folder folder;
    EmailAccountDaoImpl accountDao;
    private Session session = Session.getDefaultInstance(new Properties());
    
    @Before
    public void setUp() throws MessagingException {
        MockitoAnnotations.initMocks(this);
        
        accountDao = new EmailAccountDaoImpl();
        when(folder.getMessageCount()).thenReturn(93);
        when(folder.getMessages(anyInt(), anyInt())).thenReturn(new Message[]{});
        
    }
    
    @Test
    public void testPaging() throws MessagingException, IOException, ScanException, PolicyException {

        // test a typical paging use case
        when(folder.getMessageCount()).thenReturn(93);
        int pageStart = 20;
        int number = 30;
        accountDao.getEmailMessages(folder, pageStart, number, session);
        verify(folder).getMessages(44, 73);
        
        // test the beginning of the paging set
        pageStart = 0;
        number = 30;
        accountDao.getEmailMessages(folder, pageStart, number, session);
        verify(folder).getMessages(64, 93);
        
        // test the end of the paging set
        pageStart = 63;
        number = 30;
        accountDao.getEmailMessages(folder, pageStart, number, session);
        verify(folder).getMessages(1, 30);

        // test a paging window that is greater than the number of messages
        // available
        pageStart = 73;
        number = 30;
        accountDao.getEmailMessages(folder, pageStart, number, session);
        verify(folder).getMessages(1, 20);

    }
    
    @Test
    public void testIsHtml() {
        assert accountDao.isHtml("text/html;charset=iso-8859-1");
        assert !accountDao.isHtml("text/plain");
    }
    
}
