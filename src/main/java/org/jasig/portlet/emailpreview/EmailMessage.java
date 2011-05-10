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
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * An entity abstraction for representing information
 * related to an email message.
 *
 * @author Andreas Christoforides
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 * @version $Revision$
 */
public final class EmailMessage {

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("h:mm a MMM d, yyyy");
    private static final String CONTENT_TYPE_ATTACHMENTS_PATTERN = "multipart/mixed;";
    private static final String INTERNET_ADDRESS_TYPE = "rfc822";
    
    private final Message message;
    private final Long uid;
    private final String sender;  // Evaluate in constructor to detect errors early
    private final String subject;  // Passed in separately AntiSamy treatment 
    private final Date sentDate;  // Evaluate in constructor to detect errors early
    private final boolean unread;  // Evaluate in constructor to detect errors early
    private final boolean answered;  // Evaluate in constructor to detect errors early
    private final boolean deleted;  // Evaluate in constructor to detect errors early
    private final boolean multipart;
    private final String contentType;
    private final EmailMessageContent content;  // Optional;  passed in separately AntiSamy treatment

	/*
	 * Public API.
	 */
	
    /**
     * Creates a new {@link EmailMessage} based on the specified 
     * <code>Message</code> with content of <code>null</code>.
     */
    public EmailMessage(Message message, Long uid, String subject) throws MessagingException {
        this(message, uid, subject, null);
    }

    /**
     * Creates a new {@link EmailMessage} based on the specified 
     * <code>Message</code> and {@link EmailMessageContent}.
     */
    public EmailMessage(Message message, Long uid, String subject, EmailMessageContent content) throws MessagingException {
	    
	    // Assertions.
	    if (message == null) {
	        String msg = "Argument 'message' cannot be null";
	        throw new IllegalArgumentException(msg);
	    }
	    // NB:  Argument 'uid' may be null
	    
	    // Instance Members.
        this.message = message;
        this.uid = uid;
        Address[] addr = message.getFrom();
        String sdr = null;
        if (addr != null && addr.length != 0) {
            Address a = addr[0];
            if (INTERNET_ADDRESS_TYPE.equals(a.getType())) {
                InternetAddress inet = (InternetAddress) a;
                sdr = inet.toUnicodeString();
            } else {
                sdr = a.toString();
            }
        }
        this.sender = sdr;
        this.subject = subject;
        this.sentDate = message.getSentDate();
        this.unread = !message.isSet(Flag.SEEN);
        this.answered = message.isSet(Flag.ANSWERED);
        this.deleted = message.isSet(Flag.DELETED);
        this.multipart = message.getContentType().toLowerCase().startsWith(CONTENT_TYPE_ATTACHMENTS_PATTERN);
        this.contentType = message.getContentType();
        this.content = content;
	    
	}
	
	public int getMessageNumber() {
        return message.getMessageNumber();
    }

	/**
	 * Returns the UID of the message as set by the Folder or <code>null</code> 
	 * if the Folder does not implement UIDFolder. 
	 * 
	 * @return The UID provided by the Folder for this message or null
	 */
	public Long getUid() {
	    return uid;
	}
	
    /**
	 * Returns the date the email message was sent.
	 * @return The sent date of the email message as a <code>java.util.Date</code>.
	 */
	public Date getSentDate() {
		return new Date(sentDate.getTime());
	}

	public String getSentDateString() {
	    return DATE_FORMAT.format(this.sentDate);
	}

	/**
	 * Returns the sender of this email message.
	 *
	 * @return The sender of the email message.
	 */
	public String getSender() {
	    return sender;
	}

    public String getSenderName() {
        return getSender().split("\\s*<")[0];
    }

    /**
	 * Returns the email message subject.
	 *
	 * @return The email message subject.
	 */
	public String getSubject() {
		return this.subject;
	}


    public boolean isUnread() {
        return unread;
    }

    public boolean isAnswered() {
        return answered;
    }

    public boolean isDeleted() {
        return deleted;
    }
    
    public boolean isMultipart() {
        return multipart;
    }
    
    public String getContentType() {
        return contentType;
    }

    public EmailMessageContent getContent() {
        return content;
    }

}
