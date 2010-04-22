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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.springframework.stereotype.Component;

/**
 * A Data Access Object (DAO) for retrieving email account information.
 * Currently all the information retrieved is related to the user's
 * inbox.
 *
 * @author Andreas Christoforides
 *
 */
@Component
public class EmailAccountDaoImpl implements IEmailAccountDao {

    protected final Log log = LogFactory.getLog(getClass());
    
	/* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.IAccountInfoDAO#retrieveEmailAccountInfo(org.jasig.portlet.emailpreview.MailStoreConfiguration, java.lang.String, java.lang.String, int)
     */
    public AccountInfo retrieveEmailAccountInfo (MailStoreConfiguration storeConfig, Authenticator auth, int messageCount)
    throws EmailPreviewException {

        Folder inbox = null;
        try {

            // Retrieve user's inbox
            inbox = getUserInbox(storeConfig, auth);
            inbox.open(Folder.READ_ONLY);
            long startTime = System.currentTimeMillis();
            List<EmailMessage> unreadMessages = getEmailMessages(inbox, messageCount);
            int totalMessageCount = getTotalEmailMessageCount(inbox);
            int unreadMessageCount = getUnreadEmailMessageCount(inbox);
            
            if ( log.isDebugEnabled() ) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int unreadMessageToDisplayCount = unreadMessages.size();
                log.debug("Finished looking up email messages. Inbox size: " + totalMessageCount + 
                        " Unread message count: " + unreadMessageToDisplayCount + 
                        " Total elapsed time: " + elapsedTime + "ms " +
                        " Time per message in inbox: " + (totalMessageCount == 0 ? 0 : (elapsedTime / totalMessageCount)) + "ms" +
                        " Time per unread message: " + (unreadMessageToDisplayCount == 0 ? 0 : (elapsedTime / unreadMessageToDisplayCount)) + "ms");
            }
            inbox.close(false);
            
            // Initialize account information with information retrieved from inbox
            AccountInfo acountInfo = new AccountInfo();
            acountInfo.setMessages(unreadMessages);
            acountInfo.setUnreadMessageCount(unreadMessageCount);
            acountInfo.setTotalMessageCount(totalMessageCount);

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved email account info");
            }

            return acountInfo;

        } catch (MessagingException me) {

            StringBuilder errorMsg = new StringBuilder(128);
            errorMsg.append("An error occured while retrieving ");
            errorMsg.append("email account information for user with username ");

            log.error(errorMsg.toString(), me);

            throw new EmailPreviewException(me);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
                } catch ( Exception e ) {}
            }
        }

        
    }
    
    private Folder getUserInbox(MailStoreConfiguration storeConfig, Authenticator authenticator)
            throws MessagingException {

        // Initialize connection properties
        Properties mailProperties = new Properties();
        mailProperties.put("mail.store.protocol", storeConfig.getProtocol());
        mailProperties.put("mail.host", storeConfig.getHost());
        mailProperties.put("mail.port", storeConfig.getPort());

        String protocolPropertyPrefix = "mail." + storeConfig.getProtocol() + ".";

        // Set connection timeout property
        int connectionTimeout = storeConfig.getConnectionTimeout();
        if (connectionTimeout >= 0) {
            mailProperties.put(protocolPropertyPrefix +
                    "connectiontimeout", connectionTimeout);
        }
        
        // Set timeout property
        int timeout = storeConfig.getTimeout();
        if (timeout >= 0) {
            mailProperties.put(protocolPropertyPrefix + "timeout", timeout);
        }

        // add each additional property
        for (Map.Entry<String, String> property : storeConfig.getProperties().entrySet()) {
            mailProperties.put(property.getKey(), property.getValue());
        }

        // Connect/authenticate to the configured store
        Session session = Session.getInstance(mailProperties, authenticator);
        Store store = session.getStore();
        store.connect();

        if (log.isDebugEnabled()) {
            log.debug("Mail store created");
        }

        // Retrieve user's inbox folder
        Folder root = store.getDefaultFolder();
        Folder inboxFolder = root.getFolder(storeConfig.getInboxFolderName());

        return inboxFolder;
    }

    private List<EmailMessage> getEmailMessages(Folder mailFolder,
            int messageCount) throws MessagingException {

        int totalMessageCount = mailFolder.getMessageCount();
        int start = Math.max(1, totalMessageCount - (messageCount - 1));

        Message[] messages = mailFolder.getMessages(start, totalMessageCount);

        long startTime = System.currentTimeMillis();

        // Fetch only necessary headers for each message
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);
        profile.add(FetchProfile.Item.FLAGS);
        mailFolder.fetch(messages, profile);

        if (log.isDebugEnabled()) {
            log.debug("Time elapsed while fetching message headers:"
                    + (System.currentTimeMillis() - startTime));
        }

        List<EmailMessage> unreadEmails = new LinkedList<EmailMessage>();

        /*
         * Retrieving maxUnreadMessages and the unread message count. Not using
         * the getUnreadMessageCount() method since the method also traverses
         * all messages which we need to do anyway to retrieve
         * maxUnreadMessages.
         */
        for (Message currentMessage : messages) {

            EmailMessage emailMessage = new EmailMessage();

            // Set sender address
            Address[] addresses = currentMessage.getFrom();
            String sender = addresses[0].toString();
            emailMessage.setSender(sender);

            // Set subject and sent date
            emailMessage.setSubject(currentMessage.getSubject());
            emailMessage.setSentDate(currentMessage.getSentDate());
            
            emailMessage.setUnread(!currentMessage.isSet(Flag.SEEN));
            emailMessage.setAnswered(currentMessage.isSet(Flag.ANSWERED));
            emailMessage.setDeleted(currentMessage.isSet(Flag.DELETED));

            unreadEmails.add(emailMessage);
        }

        Collections.reverse(unreadEmails);

        return unreadEmails;

    }

    private int getTotalEmailMessageCount(Folder inbox)
            throws MessagingException {
        return inbox.getMessageCount();
    }


    private int getUnreadEmailMessageCount(Folder inbox)
            throws MessagingException {
        return inbox.getUnreadMessageCount();
    }

}
