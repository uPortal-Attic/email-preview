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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailMessageContent;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.PartialCacheKey;
import com.googlecode.ehcache.annotations.Property;
import com.googlecode.ehcache.annotations.TriggersRemove;

/**
 * A Data Access Object (DAO) for retrieving email account information.
 * Currently all the information retrieved is related to the user's
 * inbox.
 *
 * @author Andreas Christoforides
 * @author Drew Wills, drew@unicon.net
 * @version $Revision$
 */
@Component
public class EmailAccountDaoImpl implements IEmailAccountDao, InitializingBean, ApplicationContextAware {

    protected final Log log = LogFactory.getLog(getClass());

    private Policy policy;

    private ApplicationContext ctx;

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext ctx)
                    throws BeansException {
            this.ctx = ctx;
    }

    private String filePath = "classpath:antisamy.xml";

    /**
     * Set the file path to the Anti-samy policy file to be used for cleaning
     * strings.
     *
     * @param path
     */
    public void setSecurityFile(String path) {
            this.filePath = path;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        InputStream stream = ctx.getResource(filePath).getInputStream();
        policy = Policy.getInstance(stream);
    }

    @TriggersRemove(cacheName="inboxCache",
        keyGenerator = @KeyGenerator(
            name="StringCacheKeyGenerator",
            properties = @Property( name="includeMethod", value="false" )
        )
    )
    public void clearCache(String username, String mailAccount) {
        // Nothing to do here;  all the work is in the annotations.
    }

    /* (non-Javadoc)
     * @see org.jasig.portlet.emailpreview.dao.IAccountInfoDAO#retrieveEmailAccountInfo(org.jasig.portlet.emailpreview.MailStoreConfiguration, java.lang.String, java.lang.String, int)
     */
    @Cacheable(cacheName="inboxCache", selfPopulating=true,
        keyGenerator = @KeyGenerator(
            name="StringCacheKeyGenerator",
            properties = @Property(name="includeMethod", value="false")
        )
    )
    public AccountInfo fetchAccountInfoFromStore(@PartialCacheKey String username,
            @PartialCacheKey String mailAccount, MailStoreConfiguration config,
            Authenticator auth, int start, int count) throws EmailPreviewException {

        if (username == null) {
            throw new MailAuthenticationException();
        }

        Folder inbox = null;
        try {

            // Retrieve user's inbox
            inbox = getUserInbox(config, auth);
            inbox.open(Folder.READ_ONLY);
            long startTime = System.currentTimeMillis();
            List<EmailMessage> messages = getEmailMessages(inbox, start, count);

            if ( log.isDebugEnabled() ) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int messagesToDisplayCount = messages.size();
                log.debug("Finished looking up email messages. Inbox size: " + inbox.getMessageCount() +
                        " Unread message count: " + inbox.getUnreadMessageCount() +
                        " Total elapsed time: " + elapsedTime + "ms " +
                        " Time per message in inbox: " + (inbox.getMessageCount() == 0 ? 0 : (elapsedTime / inbox.getMessageCount())) + "ms" +
                        " Time per displayed message: " + (messagesToDisplayCount == 0 ? 0 : (elapsedTime / messagesToDisplayCount)) + "ms");
            }

            // Initialize account information with information retrieved from inbox
            AccountInfo acountInfo = new AccountInfo(inbox, messages, start, count);

            inbox.close(false);

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved email account info");
            }

            return acountInfo;

        } catch (MessagingException me) {
            log.error("Exception encountered while retrieving account info", me);
            throw new EmailPreviewException(me);
        } catch (IOException e) {
            log.error("Exception encountered while retrieving account info", e);
            throw new EmailPreviewException(e);
        } catch (ScanException e) {
            log.error("Exception encountered while retrieving account info", e);
            throw new EmailPreviewException(e);
        } catch (PolicyException e) {
            log.error("Exception encountered while retrieving account info", e);
            throw new EmailPreviewException(e);
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

        try {
            // Initialize connection properties
            Properties mailProperties = new Properties();
            mailProperties.put("mail.store.protocol", storeConfig.getProtocol());
            mailProperties.put("mail.host", storeConfig.getHost());
            mailProperties.put("mail.port", storeConfig.getPort());
            mailProperties.put("mail.debug", true);

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
            for (Map.Entry<String, String> property : storeConfig.getJavaMailProperties().entrySet()) {
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
        } catch (AuthenticationFailedException e) {
            throw new MailAuthenticationException(e);
        }
    }

    protected List<EmailMessage> getEmailMessages(Folder mailFolder, int pageStart,
            int messageCount) throws MessagingException, IOException, ScanException, PolicyException {

        int totalMessageCount = mailFolder.getMessageCount();
        int start = Math.max(1, totalMessageCount - pageStart - (messageCount - 1));
        int end = Math.max(totalMessageCount - pageStart, 1);

        Message[] messages = totalMessageCount != 0
                                ? mailFolder.getMessages(start, end)
                                : new Message[0];

        long startTime = System.currentTimeMillis();

        // Fetch only necessary headers for each message
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);
        profile.add(FetchProfile.Item.FLAGS);
        profile.add(FetchProfile.Item.CONTENT_INFO);
        mailFolder.fetch(messages, profile);

        if (log.isDebugEnabled()) {
            log.debug("Time elapsed while fetching message headers:"
                    + (System.currentTimeMillis() - startTime));
        }

        List<EmailMessage> emails = new LinkedList<EmailMessage>();

        /*
         * Retrieving maxUnreadMessages and the unread message count. Not using
         * the getUnreadMessageCount() method since the method also traverses
         * all messages which we need to do anyway to retrieve
         * maxUnreadMessages.
         */
        for (Message currentMessage : messages) {

            EmailMessage emailMessage = wrapMessage(currentMessage, false);
            emails.add(emailMessage);
        }

        Collections.reverse(emails);

        return emails;

    }

    public EmailMessage retrieveMessage(MailStoreConfiguration storeConfig, Authenticator auth, int messageNum) {

        Folder inbox = null;
        try {

            // Retrieve user's inbox
            inbox = getUserInbox(storeConfig, auth);
            inbox.open(Folder.READ_ONLY);

            Message message = inbox.getMessage(messageNum);
            EmailMessage emailMessage = wrapMessage(message, true);

            inbox.close(false);

            return emailMessage;
        } catch (MessagingException e) {
            log.error("Messaging exception while retrieving individual message", e);
        } catch (IOException e) {
            log.error("IO exception while retrieving individual message", e);
        } catch (ScanException e) {
            log.error("AntiSamy scanning exception while retrieving individual message", e);
        } catch (PolicyException e) {
            log.error("AntiSamy policy exception while retrieving individual message", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
                } catch ( Exception e ) {}
            }
        }

        return null;
    }

    public boolean deleteMessages(MailStoreConfiguration storeConfig, Authenticator auth, long[] uids) {

        Folder inbox = null;
        try {

            // Retrieve user's inbox
            inbox = getUserInbox(storeConfig, auth);

            // Verify that we can even perform this operation
            if (!(inbox instanceof UIDFolder)) {
                String msg = "Delete feature is supported only for UIDFolder instances";
                throw new UnsupportedOperationException(msg);
            }

            inbox.open(Folder.READ_WRITE);

            Message[] msgs = ((UIDFolder) inbox).getMessagesByUID(uids);
            inbox.setFlags(msgs, new Flags(Flag.DELETED), true);

            return true;  // Indicate success

        } catch (MessagingException e) {
            log.error("Messaging exception while deleting messages", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(true);
                } catch ( Exception e ) {
                    log.error("Error closing inbox folder", e);
                }
            }
        }

        return false;  // We failed if we reached this point

    }

    protected EmailMessage wrapMessage(Message msg, boolean populateContent) throws MessagingException, IOException, ScanException, PolicyException {

        // Prepare subject
        String subject = msg.getSubject();
        if (!StringUtils.isBlank(subject)) {
            AntiSamy as = new AntiSamy();
            CleanResults cr = as.scan(subject, policy);
            subject = cr.getCleanHTML();
        }

        // Prepare content if requested
        EmailMessageContent body = null;  // default...
        if (populateContent) {
            Object content = msg.getContent();
            body = getContentString(content, msg.getContentType());
            String contentString = body.getContentString();
            if (!StringUtils.isBlank(contentString)) {
                AntiSamy as = new AntiSamy();
                CleanResults cr = as.scan(contentString, policy);
                subject = cr.getCleanHTML();
            }
            body.setContentString(contentString);
        }

        // Prepare the UID if present
        Long uid = null;  // default
        if (msg.getFolder() instanceof UIDFolder) {
            uid = ((UIDFolder) msg.getFolder()).getUID(msg);
        }

        return new EmailMessage(msg, uid, subject, body);

    }

    protected EmailMessageContent getContentString(Object content, String mimeType) throws IOException, MessagingException {

        // if this content item is a String, simply return it.
        if (content instanceof String) {
            return new EmailMessageContent((String) content, isHtml(mimeType));
        }

        else if (content instanceof MimeMultipart) {
            Multipart m = (Multipart) content;
            int parts = m.getCount();

            // iterate backwards through the parts list
            for (int i = parts-1; i >= 0; i--) {
                EmailMessageContent result = null;

                BodyPart part = m.getBodyPart(i);
                Object partContent = part.getContent();
                String contentType = part.getContentType();
                boolean isHtml = isHtml(contentType);
                log.debug("Examining Multipart " + i + " with type " + contentType + " and class " + partContent.getClass());

                if (partContent instanceof String) {
                    result = new EmailMessageContent((String) partContent, isHtml);
                }

                else if (partContent instanceof InputStream && (contentType.startsWith("text/html"))) {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy((InputStream) partContent, writer);
                    result = new EmailMessageContent(writer.toString(), isHtml);
                }

                else if (partContent instanceof MimeMultipart) {
                    result = getContentString(partContent, contentType);
                }

                if (result != null) {
                    return result;
                }

            }
        }

        return null;

    }

    /**
     * Determine if the supplied MIME type represents HTML content.  This
     * implementation assumes that the inclusion of the string "text/html"
     * in a mime-type indicates HTML content.
     *
     * @param mimeType
     * @return
     */
    protected boolean isHtml(String mimeType) {

        // if the mime-type is null, assume the content is not HTML
        if (mimeType == null) {
            return false;
        }

        // otherwise, check for the presence of the string "text/html"
        mimeType = mimeType.trim().toLowerCase();
        if (mimeType.contains("text/html")) {
            return true;
        }

        return false;
    }

}
