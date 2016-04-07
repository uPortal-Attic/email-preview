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
package org.jasig.portlet.emailpreview.dao.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.Folder;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailMessageContent;
import org.jasig.portlet.emailpreview.EmailQuota;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.service.IServiceBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Demo implementation of {@link IEmailAccountService}.
 *
 * @author Landis Casner
 * @author Drew Wills, drew@unicon.net
 */
@Component
public final class DemoAccountService implements IEmailAccountService {

    private final Log log = LogFactory.getLog(getClass());

    private static final String ACCOUNT_SUMMARY_KEY = "DemoAccountService.ACCOUNT_SUMMARY_KEY";
    private static final String INBOX_URL = "http://www.jasig.org/";
    private static final int DEFAULT_BATCH_SIZE = 20;
    private static EmailQuota quota = new EmailQuota(10561140,239702);
    private static final String INBOX_FOLDER = "INBOX";

    private String jsonLocation = "/SampleJSON.json";

    @Autowired(required = true)
    private IServiceBroker serviceBroker;

    /*
     * Public API
     */

    public void setJsonLocation(String jsonLocation) {
        this.jsonLocation = jsonLocation;
    }

    public AccountSummary getAccountSummary(PortletRequest req, int start,
            int max, boolean refresh, String folder) {

        // Try PortletSession first
        PortletSession session = req.getPortletSession();
        AccountSummary rslt = (AccountSummary) session.getAttribute(ACCOUNT_SUMMARY_KEY);

        //Set the right JSON!
        if(INBOX_FOLDER.equals(folder)){
        	setJsonLocation("/SampleJSON.json");
        }else{
        	setJsonLocation("/".concat(folder).concat(".json"));
        }  

        if (rslt == null) {
            // First time;  build from scratch...
            List<EmailMessage> messages = getEmailMessages(req);
            rslt = new AccountSummary(INBOX_URL, messages, getUnreadMessageCount(messages),
                                                messages.size(), start, max, true, quota);
            req.getPortletSession().setAttribute(ACCOUNT_SUMMARY_KEY, rslt);
        }

        return rslt;

    }

    public EmailMessage getMessage(PortletRequest req, String messageId) {

        PortletSession session = req.getPortletSession();
        AccountSummary summary = (AccountSummary) session.getAttribute(ACCOUNT_SUMMARY_KEY);
        if (summary == null) {
            // Probably shouldn't happen...
            summary = getAccountSummary(req, 0, DEFAULT_BATCH_SIZE, false, INBOX_FOLDER);
        }

        EmailMessage rslt = null;

        List<? extends EmailMessage> messages = summary.getMessages();
        for (EmailMessage m : messages) {
            if (m.getMessageId().equals(messageId)) {
                rslt = m;
                break;
            }
        }

        if (rslt == null) {
            throw new RuntimeException("No such message:  " + messageId);
        }

        // Set the SEEN flag if configured to do so
        if(serviceBroker.getConfiguration(req).getMarkMessagesAsRead()) {
            List<EmailMessage> newList = new ArrayList<EmailMessage>();
            for (EmailMessage m : messages) {
                EmailMessage msg = !m.equals(rslt) ? m
                        : new EmailMessage(m.getMessageNumber(), m.getUid(), m.getSender(), m.getSubject(),
                            m.getSentDate(), false, m.isAnswered(), m.isDeleted(),
                            m.isMultipart(), m.getContentType(), m.getContent(), m.getToRecipients(),
                            m.getCcRecipients(), m.getBccRecipients());

                newList.add(msg);
            }
            session.setAttribute(ACCOUNT_SUMMARY_KEY, new AccountSummary(INBOX_URL,
                    newList, getUnreadMessageCount(newList), newList.size(),
                    0, DEFAULT_BATCH_SIZE, true, quota));
        }

        return rslt;

    }

    public boolean deleteMessages(PortletRequest req, String[] messageIds) {

        List<String> excluded = Arrays.asList(messageIds);

        PortletSession session = req.getPortletSession(true);
        AccountSummary summary = (AccountSummary) session.getAttribute(ACCOUNT_SUMMARY_KEY);
        if (summary == null) {
            // Probably shouldn't happen...
            summary = getAccountSummary(req, 0, DEFAULT_BATCH_SIZE, false, INBOX_FOLDER);
        }
        List<? extends EmailMessage> messages = summary.getMessages();
        List<EmailMessage> newList = new ArrayList<EmailMessage>();

        for (EmailMessage m : messages) {
            if (!excluded.contains(m.getMessageId())) {
                newList.add(m);
            }
        }

        session.setAttribute(ACCOUNT_SUMMARY_KEY, new AccountSummary(INBOX_URL,
                newList, getUnreadMessageCount(newList), newList.size(),
                0, DEFAULT_BATCH_SIZE, true, quota));

        return true;  // Indicate success

    }

    public boolean setSeenFlag(PortletRequest req, String[] messageIds, boolean seenValue) {

        List<String> changed = Arrays.asList(messageIds);

        PortletSession session = req.getPortletSession(true);
        AccountSummary summary = (AccountSummary) session.getAttribute(ACCOUNT_SUMMARY_KEY);
        if (summary == null) {
            // Probably shouldn't happen...
            summary = getAccountSummary(req, 0, DEFAULT_BATCH_SIZE, false, INBOX_FOLDER);
        }
        List<? extends EmailMessage> messages = summary.getMessages();
        List<EmailMessage> newList = new ArrayList<EmailMessage>();

        for (EmailMessage m : messages) {
            EmailMessage msg = !changed.contains(m.getMessageId()) ? m
                    : new EmailMessage(m.getMessageNumber(), m.getUid(), m.getSender(), m.getSubject(),
                        m.getSentDate(), !seenValue, m.isAnswered(), m.isDeleted(),
                        m.isMultipart(), m.getContentType(), m.getContent(), m.getToRecipients(),
                        m.getCcRecipients(), m.getBccRecipients());

            newList.add(msg);
        }

        session.setAttribute(ACCOUNT_SUMMARY_KEY, new AccountSummary(INBOX_URL,
                newList, getUnreadMessageCount(newList), newList.size(),
                0, DEFAULT_BATCH_SIZE, true, quota));

        return true;  // success

    }

    /**
     * Builds a fresh collection of messages.
     */
    private List<EmailMessage> getEmailMessages(PortletRequest req) {

        File jsonFile = new File(getClass().getResource(jsonLocation).getFile());
        List<EmailMessage> messages = new ArrayList<EmailMessage>();

        try {

            ObjectMapper mapper = new ObjectMapper();
            ArrayNode json = mapper.readValue(jsonFile, ArrayNode.class);

            // Creates a Mime Message because Email Message depends on a "message" variable.
            for (JsonNode msg : json) {

                String uid = msg.path("uid").asText();
                String sender = msg.path("from").textValue();
                String subject = msg.path("subject").textValue();
                Date sentDate = new Date(msg.path("sentDate").longValue());
                boolean unread = msg.path("unread").booleanValue();
                boolean answered = msg.path("answered").booleanValue();
                boolean multipart = msg.path("multipart").booleanValue();
                boolean deleted = false; // more testing is available here
                EmailMessageContent content = new EmailMessageContent(msg.path("body").textValue(), true);
                String toRecipients = "toTest@test.univ.eu; toTest1@test1.univ.eu";
                String ccRecipients = "ccTest@test.univ.eu; ccTest1@test1.univ.eu";
                String bccRecipients = "bccTest@test.univ.eu; bccTest1@test1.univ.eu";
                messages.add(new EmailMessage(messages.size(), uid,
                        sender, subject, sentDate, unread, answered, deleted,
                        multipart, "text/plain", content, toRecipients, ccRecipients, bccRecipients));

            }
        } catch (Exception e) {
            log.error("Failed to load messages collection", e);
        }

        return messages;

    }

    /**
     * replacement for 'inbox.getUnreadMessageCount()
     */
    private int getUnreadMessageCount(List<EmailMessage> messages)
    {
        int unreadCount = 0;

        for (EmailMessage email : messages)
        {
            if (email.isUnread())
            {
                unreadCount++;
            }
        }

        return unreadCount;
    }
    
    public List<Folder> getAllUserInboxFolders(PortletRequest req) {
     	return new ArrayList<Folder>();
    }
}
