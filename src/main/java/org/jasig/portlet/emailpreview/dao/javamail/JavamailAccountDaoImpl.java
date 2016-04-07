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
package org.jasig.portlet.emailpreview.dao.javamail;

import com.sun.mail.imap.IMAPFolder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailMessageContent;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.EmailQuota;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IMailAccountDao;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.jasig.portlet.emailpreview.service.ICredentialsProvider;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Quota;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.SharedByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class accesses the Javamail host for mailbox operations.
 *
 * NOTE: With Javamail, uuids are not globally-unique within the user's mail store as they are with Exchange for
 * instance.  With Javamail the messageIds are index numbers within the mailbox store (which is really per-folder).
 * Thus, many mail folders for a user may have a messageId=1.  This does not present many problems as long as we
 * insure we are only dealing with one folder at a time.
 *
 * @author awills
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Component
public final class JavamailAccountDaoImpl implements IMailAccountDao {

    private static final String CONTENT_TYPE_ATTACHMENTS_PATTERN = "multipart/mixed;";
    private static final String INTERNET_ADDRESS_TYPE = "rfc822";

    @Autowired(required = true)
    private ILinkServiceRegistry linkServiceRegistry;

    @Autowired
    private ICredentialsProvider credentialsProvider;

    /**
     * Value for the 'mail.debug' setting in JavaMail
     */
    private boolean debug = false;

    private String filePath = "classpath:antisamy.xml";  // default
    @Autowired(required = true)
    private ApplicationContext ctx;
    private Policy policy;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public void setCredentialsProvider(ICredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Set the file path to the Anti-samy policy file to be used for cleaning
     * strings.
     *
     * @param path
     */
    public void setSecurityFile(String path) {
            this.filePath = path;
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        InputStream stream = ctx.getResource(filePath).getInputStream();
        policy = Policy.getInstance(stream);
    }

    @Override
    public AccountSummary fetchAccountSummaryFromStore(MailStoreConfiguration config, String username,
                                                String mailAccount, String folder, int start, int max) {

        Authenticator auth = credentialsProvider.getAuthenticator();

        AccountSummary summary;
        Folder inbox = null;
        try {

            // Retrieve user's folder
            Session session = openMailSession(config, auth);
            inbox = getUserInbox(session, folder);
            inbox.open(Folder.READ_ONLY);
            long startTime = System.currentTimeMillis();
            List<EmailMessage> messages = getEmailMessages(inbox, start, max, session);

            if ( log.isDebugEnabled() ) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int messagesToDisplayCount = messages.size();
                log.debug("Finished looking up email messages. Inbox size: " + inbox.getMessageCount() +
                        " Unread message count: " + inbox.getUnreadMessageCount() +
                        " Total elapsed time: " + elapsedTime + "ms " +
                        " Time per message in inbox: " + (inbox.getMessageCount() == 0 ? 0 : (elapsedTime / inbox.getMessageCount())) + "ms" +
                        " Time per displayed message: " + (messagesToDisplayCount == 0 ? 0 : (elapsedTime / messagesToDisplayCount)) + "ms");
            }

            IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
            String inboxUrl = null;
            if (linkService != null) {
                inboxUrl = linkService.getInboxUrl(config);
            }

            // Initialize account information with information retrieved from inbox
            summary = new AccountSummary(inboxUrl, messages,
                    inbox.getUnreadMessageCount(), inbox.getMessageCount(),
                    start, max, isDeleteSupported(inbox), getQuota(inbox));

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved email AccountSummary");
            }

            return summary;

        } catch (MailAuthenticationException mae) {
            // We used just to allow this exception to percolate up the chain,
            // but we learned that the entire stack trace gets written to
            // Catalina.out (by 3rd party code).  Since this is a common
            // occurrence, it causes space issues.
            return new AccountSummary(mae);
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
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
                }
                try {
                    inbox.getStore().close();
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail store connection");
                }
            }
        }
    }

    private Session openMailSession(MailStoreConfiguration config, Authenticator auth) {

        // Assertions.
        if (config == null) {
            String msg = "Argument 'config' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (auth == null) {
            String msg = "Argument 'auth' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        // Initialize connection properties
        Properties mailProperties = new Properties();
        mailProperties.put("mail.store.protocol", config.getProtocol());
        mailProperties.put("mail.host", config.getHost());
        mailProperties.put("mail.port", config.getPort());
        mailProperties.put("mail.debug", debug ? "true" : "false");

        String protocolPropertyPrefix = "mail." + config.getProtocol() + ".";

        // Set connection timeout property
        int connectionTimeout = config.getConnectionTimeout();
        if (connectionTimeout >= 0) {
            mailProperties.put(protocolPropertyPrefix +
                    "connectiontimeout", connectionTimeout);
        }

        // Set timeout property
        int timeout = config.getTimeout();
        if (timeout >= 0) {
            mailProperties.put(protocolPropertyPrefix + "timeout", timeout);
        }

        // add each additional property
        for (Map.Entry<String, String> property : config.getJavaMailProperties().entrySet()) {
            mailProperties.put(property.getKey(), property.getValue());
        }

        // Connect/authenticate to the configured store
        return Session.getInstance(mailProperties, auth);

    }

    @Override
    public EmailMessage getMessage(MailStoreConfiguration config, String messageId) {
        Authenticator auth = credentialsProvider.getAuthenticator();
        Folder inbox = null;
        try {
            int mode = config.getMarkMessagesAsRead() ? Folder.READ_WRITE : Folder.READ_ONLY;

            // Retrieve user's inbox
            Session session = openMailSession(config, auth);
            inbox = getUserInbox(session, config.getInboxFolderName());
            inbox.open(mode);

            Message message;
            if (inbox instanceof UIDFolder) {
                message = ((UIDFolder)inbox).getMessageByUID(Long.parseLong(messageId));
            } else {
                message = inbox.getMessage(Integer.parseInt(messageId));
            }
            boolean unread = !message.isSet(Flags.Flag.SEEN);
            if (config.getMarkMessagesAsRead()) {
                message.setFlag(Flag.SEEN, true);
            }
            EmailMessage emailMessage = wrapMessage(message, true, session);
            if (!config.getMarkMessagesAsRead()) {
                // NOTE:  This is more than a little bit annoying.  Apparently
                // the mere act of accessing the body content of a message in
                // Javamail flags the in-memory representation of that message
                // as SEEN.  It does *nothing* to the mail server (the message
                // is still unread in the SOR), but it wreaks havoc on local
                // functions that key off that value and expect it to be
                // accurate.  We're obligated, therefore, to restore the value
                // to what it was before the call to wrapMessage().
                emailMessage.setUnread(unread);
            }

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
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
                }
                try {
                    inbox.getStore().close();
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail store connection");
                }
            }
        }

        return null;
    }

    private Folder getUserInbox(Session session, String folderName)
            throws MessagingException {

        // Assertions.
        if (session == null) {
            String msg = "Argument 'session' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        try {
            Store store = session.getStore();
            store.connect();

            if (log.isDebugEnabled()) {
                log.debug("Mail store connection established to get user inbox");
            }

            // Retrieve user's inbox folder
            Folder root = store.getDefaultFolder();
            Folder inboxFolder = root.getFolder(folderName);

            return inboxFolder;
        } catch (AuthenticationFailedException e) {
            throw new MailAuthenticationException(e);
        }

    }

    private EmailMessage wrapMessage(Message msg, boolean populateContent, Session session)
            throws MessagingException, IOException, ScanException, PolicyException {

        // Prepare subject
        String subject = msg.getSubject();
        if (!StringUtils.isBlank(subject)) {
            AntiSamy as = new AntiSamy();
            CleanResults cr = as.scan(subject, policy);
            subject = cr.getCleanHTML();
        }

        // Prepare content if requested
        EmailMessageContent msgContent = null;  // default...
        if (populateContent) {
            // Defend against the dreaded: "Unable to load BODYSTRUCTURE"
            try {
                msgContent = getMessageContent(msg.getContent(), msg.getContentType());
            } catch (MessagingException me) {
                // We are unable to read digitally-signed messages (perhaps
                // others?) in the API-standard way;  we have to use a work around.
                // See: http://www.oracle.com/technetwork/java/faq-135477.html#imapserverbug
                // Logging as DEBUG because this behavior is known & expected.
                log.debug("Difficulty reading a message (digitally signed?). Attempting workaround...");
                try {
                    MimeMessage mm = (MimeMessage) msg;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mm.writeTo(bos);
                    bos.close();
                    SharedByteArrayInputStream bis = new SharedByteArrayInputStream(bos.toByteArray());
                    MimeMessage copy = new MimeMessage(session, bis);
                    bis.close();
                    msgContent = getMessageContent(copy.getContent(), copy.getContentType());
                } catch (Throwable t) {
                    log.error("Failed to read message body", t);
                    msgContent = new EmailMessageContent("UNABLE TO READ MESSAGE BODY: " + t.getMessage(), false);
                }
            }

            // Sanitize with AntiSamy
            String content = msgContent.getContentString();
            if (!StringUtils.isBlank(content)) {
                AntiSamy as = new AntiSamy();
                CleanResults cr = as.scan(content, policy);
                content = cr.getCleanHTML();
            }
            msgContent.setContentString(content);
        }

        int messageNumber = msg.getMessageNumber();

        // Prepare the UID if present
        String uid = null;  // default
        if (msg.getFolder() instanceof UIDFolder) {
            uid = Long.toString(((UIDFolder) msg.getFolder()).getUID(msg));
        }

        Address[] addr = msg.getFrom();
        String sender = getFormattedAddresses(addr);
        Date sentDate = msg.getSentDate();

        boolean unread = !msg.isSet(Flag.SEEN);
        boolean answered = msg.isSet(Flag.ANSWERED);
        boolean deleted = msg.isSet(Flag.DELETED);
        // Defend against the dreaded: "Unable to load BODYSTRUCTURE"
        boolean multipart = false;  // sensible default;
        String contentType = null;  // sensible default
        try {
            multipart = msg.getContentType().toLowerCase().startsWith(CONTENT_TYPE_ATTACHMENTS_PATTERN);
            contentType = msg.getContentType();
        } catch (MessagingException me) {
            // Message was digitally signed and we are unable to read it;
            // logging as DEBUG because this issue is known/expected, and
            // because the user's experience is in no way affected (at this point)
            log.debug("Message content unavailable (digitally signed?);  " +
                        "message will appear in the preview table correctly, " +
                        "but the body will not be viewable");
            log.trace(me.getMessage(), me);
        }
        String to = getTo(msg);
        String cc = getCc(msg);
        String bcc = getBcc(msg);
        return new EmailMessage(messageNumber, uid, sender, subject, sentDate, unread, answered, deleted, multipart, contentType, msgContent, to, cc, bcc);
    }

    /*
     * Implementation
     */

    private List<EmailMessage> getEmailMessages(Folder mailFolder, int pageStart,
            int messageCount, Session session) throws MessagingException, IOException, ScanException, PolicyException {

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
        if (mailFolder instanceof UIDFolder) {
            profile.add(UIDFolder.FetchProfileItem.UID);
        }
        mailFolder.fetch(messages, profile);

        if (log.isDebugEnabled()) {
            log.debug("Time elapsed while fetching message headers; {}ms", System.currentTimeMillis() - startTime);
        }

        List<EmailMessage> emails = new LinkedList<EmailMessage>();
        for (Message currentMessage : messages) {
            EmailMessage emailMessage = wrapMessage(currentMessage, false, session);
            emails.add(emailMessage);
        }

        Collections.reverse(emails);
        return emails;
    }

    private EmailMessageContent getMessageContent(Object content, String mimeType) throws IOException, MessagingException {

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
                    result = getMessageContent(partContent, contentType);
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
    private boolean isHtml(String mimeType) {

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

    @Override
    public boolean deleteMessages(MailStoreConfiguration config, String[] uuids) {
        Authenticator auth = credentialsProvider.getAuthenticator();
        Folder inbox = null;
        try {

            // Retrieve user's inbox
            Session session = openMailSession(config, auth);
            inbox = getUserInbox(session, config.getInboxFolderName());

            // Verify that we can even perform this operation
            if (!(inbox instanceof UIDFolder)) {
                String msg = "Delete feature is supported only for UIDFolder instances";
                throw new UnsupportedOperationException(msg);
            }

            inbox.open(Folder.READ_WRITE);

            Message[] msgs = ((UIDFolder) inbox).getMessagesByUID(getMessageUidsAsLong(uuids));
            inbox.setFlags(msgs, new Flags(Flag.DELETED), true);

            return true;  // Indicate success

        } catch (MessagingException e) {
            log.error("Messaging exception while deleting messages", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
                }
                try {
                    inbox.getStore().close();
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail store connection");
                }
            }
        }

        return false;  // We failed if we reached this point
    }

    private long[] getMessageUidsAsLong(String[] messageIds) {
        long[] ids = new long[messageIds.length];
        int i = 0;
        for (String id : messageIds) {
            ids[i++] = Long.parseLong(id);
        }
        return ids;
    }

    @Override
    public boolean setMessageReadStatus(MailStoreConfiguration config, String[] uuids, boolean read) {
        Authenticator auth = credentialsProvider.getAuthenticator();
        Folder inbox = null;
        try {
            // Retrieve user's inbox
            Session session = openMailSession(config, auth);
            inbox = getUserInbox(session, config.getInboxFolderName());

            // Verify that we can even perform this operation log info if it isn't capable of operation
            if (!(inbox instanceof UIDFolder)) {
                String msg = "Toggle unread feature is supported only for UIDFolder instances";
                log.info(msg);
                return false;
            }

            inbox.open(Folder.READ_WRITE);

            Message[] msgs = ((UIDFolder) inbox).getMessagesByUID(getMessageUidsAsLong(uuids));
            inbox.setFlags(msgs, new Flags(Flag.SEEN), read);

            return true;  // Indicate success

        } catch (MessagingException e) {
            log.error("Messaging exception while deleting messages", e);
        } finally {
            if ( inbox != null ) {
                try {
                    inbox.close(false);
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail inbox connection");
                }
                try {
                    inbox.getStore().close();
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail store connection");
                }
            }
        }

        return false;  // We failed if we reached this point
    }

    @Override
    public List<Folder> getAllUserInboxFolders(MailStoreConfiguration config) {
        Authenticator auth = credentialsProvider.getAuthenticator();
        Store store = null;
        try {
            Session session = openMailSession(config, auth);

            // Assertions.
            if (session == null) {
                String msg = "Argument 'session' cannot be null";
                throw new IllegalArgumentException(msg);
            }

            store = session.getStore();
            store.connect();

            if (log.isDebugEnabled()) {
                log.debug("Mail store connection established to get all user inbox folders");
            }

            // Retrieve user's inbox folder
            return Arrays.asList(store.getDefaultFolder().list("*"));
        } catch ( Exception e ) {
            log.error("Can't get all user Inbox folders");
            return null;
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch ( Exception e ) {
                    log.warn("Can't close correctly javamail store connection");
                }
            }
        }
    }

    private boolean isDeleteSupported(Folder f) {
        return f instanceof UIDFolder;
    }

    private EmailQuota getQuota(Folder folder) {
        if(!(folder instanceof IMAPFolder)) {
            return null;
        }
    	try {
    	    // Make sure the account is activated and contains messages
    	    if (folder.exists() && folder.getMessageCount() > 0) {
    		Quota[] quotas = ((IMAPFolder)folder).getQuota();

    		for (Quota quota : quotas) {
    		    for (Quota.Resource resource : quota.resources) {
    		        if(resource.name.equals("STORAGE")) {
    		            return new EmailQuota(resource.limit,resource.usage);
    		        }
    		    }
    		  }
    	    }
    	} catch (MessagingException e) {
    	    log.error("Failed to connect or get quota for mail user ");
    	}
    	return null;
    }
    
    private String getTo(Message message) throws MessagingException {
        Address[] toRecipients = message.getRecipients(RecipientType.TO);
        return getFormattedAddresses(toRecipients);
    }
    
    private String getCc(Message message) throws MessagingException {
        Address[] ccRecipients = message.getRecipients(RecipientType.CC);
        return getFormattedAddresses(ccRecipients);
    }
    
    private String getBcc(Message message) throws MessagingException {
        Address[] bccRecipients = message.getRecipients(RecipientType.BCC);
        return getFormattedAddresses(bccRecipients);
    }

	private String getFormattedAddresses(Address[] addresses) {
		List <String> recipientsList = new ArrayList <String>();
        String receiver = null;
        if (addresses != null && addresses.length != 0) {
            for (Address adress : addresses){
                if (INTERNET_ADDRESS_TYPE.equals(adress.getType())) {
                    InternetAddress inet = (InternetAddress) adress;
                    receiver = inet.toUnicodeString();
                } else {
                    receiver = adress.toString();
                }          
                recipientsList.add(receiver);
            }
        }
        return StringUtils.join(recipientsList, "; ").replaceAll("<","&lt;").replaceAll(">","&gt;");
	}
}
