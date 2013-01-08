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
package org.jasig.portlet.emailpreview.dao.javamail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Quota;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.SharedByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailMessageContent;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.EmailQuota;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.exception.MailAuthenticationException;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.PartialCacheKey;
import com.googlecode.ehcache.annotations.Property;
import com.googlecode.ehcache.annotations.TriggersRemove;
import com.sun.mail.imap.IMAPFolder;

/**
 * This class does the heavy-lifting for Javamail integration and implements the
 * caching.
 *
 * @author awills
 */
@Component
public final class JavamailAccountDaoImpl implements IJavamailAccountDao, InitializingBean, ApplicationContextAware {

    private static final String CONTENT_TYPE_ATTACHMENTS_PATTERN = "multipart/mixed;";
    private static final String INTERNET_ADDRESS_TYPE = "rfc822";

    @Autowired(required = true)
    private ILinkServiceRegistry linkServiceRegistry;

    /**
     * Value for the 'mail.debug' setting in JavaMail
     */
    private boolean debug = false;

    private String filePath = "classpath:antisamy.xml";  // default
    private ApplicationContext ctx;
    private Policy policy;

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Set the file path to the Anti-samy policy file to be used for cleaning
     * strings.
     *
     * @param path
     */
    public void setSecurityFile(String path) {
            this.filePath = path;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

    @Override
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
    @Override
    public void clearCache(String username, String mailAccount) {
        if (log.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Removing cached AccountSummary for [mailAccount=")
                        .append(mailAccount).append(", username=")
                        .append(username).append("]");
            log.debug(msg.toString());
        }
    }

    @Cacheable(cacheName="inboxCache", selfPopulating=true,
        keyGenerator = @KeyGenerator(
            name="StringCacheKeyGenerator",
            properties = @Property(name="includeMethod", value="false")
        )
    )
    @Override
    public AccountSummary fetchAccountSummaryFromStore(MailStoreConfiguration config,
            Authenticator auth, @PartialCacheKey String username,
            @PartialCacheKey String mailAccount, int start, int max)
            throws EmailPreviewException {

        if (log.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Creating new AccountSummary for [mailAccount=")
                        .append(mailAccount).append(", username=")
                        .append(username).append(", start=")
                        .append(start).append(", max=")
                        .append(max).append("]");
            log.debug(msg.toString());
        }

        Folder inbox = null;
        try {

            // Retrieve user's inbox
            Session session = openMailSession(config, auth);
            inbox = getUserInbox(session, config.getInboxFolderName());
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
            AccountSummary rslt = new AccountSummary(inboxUrl, messages,
                    inbox.getUnreadMessageCount(), inbox.getMessageCount(),
                    start, max, isDeleteSupported(inbox), getQuota(inbox));

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved email AccountSummary");
            }

            return rslt;

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

    @Override
    public Session openMailSession(MailStoreConfiguration config, Authenticator auth) {

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
    public Folder getUserInbox(Session session, String folderName)
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
                log.debug("Mail store connection established");
            }

            // Retrieve user's inbox folder
            Folder root = store.getDefaultFolder();
            Folder inboxFolder = root.getFolder(folderName);

            return inboxFolder;
        } catch (AuthenticationFailedException e) {
            throw new MailAuthenticationException(e);
        }

    }

    @Override
    public EmailMessage wrapMessage(Message msg, boolean populateContent, Session session) throws MessagingException, IOException, ScanException, PolicyException {

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
        Long uid = null;  // default
        if (msg.getFolder() instanceof UIDFolder) {
            uid = ((UIDFolder) msg.getFolder()).getUID(msg);
        }

        Address[] addr = msg.getFrom();
        String sender = null;
        if (addr != null && addr.length != 0) {
            Address a = addr[0];
            if (INTERNET_ADDRESS_TYPE.equals(a.getType())) {
                InternetAddress inet = (InternetAddress) a;
                sender = inet.toUnicodeString();
            } else {
                sender = a.toString();
            }
        }
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
            log.debug("Message content unabailable (digitally signed?);  " +
                        "message will appear in the preview table correctly, " +
                        "but the body will not be viewable");
            log.trace(me.getMessage(), me);
        }

        return new EmailMessage(messageNumber, uid, sender, subject, sentDate,
                unread, answered, deleted, multipart, contentType, msgContent);

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
        mailFolder.fetch(messages, profile);

        if (log.isDebugEnabled()) {
            log.debug("Time elapsed while fetching message headers:"
                    + (System.currentTimeMillis() - startTime));
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
}
