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

import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class EmailMessageTest {

    @Test
    public void testSenderNamePruneEmail() throws MessagingException {

        Address addr = mock(Address.class);
        when(addr.toString()).thenReturn("Test User <testuser@nowhere.net>");
        Message msg = mock(Message.class);
        when(msg.getFrom()).thenReturn(new Address[] {addr});
        when(msg.getSentDate()).thenReturn(new Date());
        when(msg.isSet(Flag.SEEN)).thenReturn(false);
        when(msg.isSet(Flag.ANSWERED)).thenReturn(false);
        when(msg.isSet(Flag.DELETED)).thenReturn(false);
        when(msg.getContentType()).thenReturn("text/plain");

        EmailMessage message = new EmailMessage(msg, null, "Test Subject", null);

        assert "Test User".equals(message.getSenderName());

    }

    @Test
    public void testSenderNameNoEmail() throws MessagingException {

        Address addr = mock(Address.class);
        when(addr.toString()).thenReturn("Test User");
        Message msg = mock(Message.class);
        when(msg.getFrom()).thenReturn(new Address[] {addr});
        when(msg.getSentDate()).thenReturn(new Date());
        when(msg.isSet(Flag.SEEN)).thenReturn(false);
        when(msg.isSet(Flag.ANSWERED)).thenReturn(false);
        when(msg.isSet(Flag.DELETED)).thenReturn(false);
        when(msg.getContentType()).thenReturn("text/plain");

        EmailMessage message = new EmailMessage(msg, null, "Test Subject", null);

        assert "Test User".equals(message.getSenderName());

    }

}
