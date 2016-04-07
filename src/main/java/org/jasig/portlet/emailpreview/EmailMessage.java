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
package org.jasig.portlet.emailpreview;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * An entity abstraction for representing information
 * related to an email message.
 *
 * @author Andreas Christoforides
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
public class EmailMessage {

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("h:mm a MMM d, yyyy");
    
    // Instance Members
    private final int messageNumber;
    private final String uid;
    private final String sender;
    private final String subject;  // Passed in separately AntiSamy treatment 
    private final Date sentDate;
    private boolean unread;
    private final boolean answered;
    private final boolean deleted;
    private boolean multipart;
    private String contentType;
    private final EmailMessageContent content;  // Optional;  passed in separately AntiSamy treatment
    private final String toRecipients;
    private final String ccRecipients;
    private final String bccRecipients;


	/*
	 * Public API.
	 */

    /**
     * Creates a new {@link EmailMessage} based on the specified 
     * <code>Message</code> and {@link EmailMessageContent}.
     */
    public EmailMessage(int messageNumber, String uid, String sender, String subject,
                Date sentDate, boolean unread, boolean answered, boolean deleted, 
                boolean multipart, String contentType, EmailMessageContent content, 
                String toRecipients, String ccRecipients, String bccRecipients) {

	    // Instance Members.
        this.messageNumber = messageNumber;
        this.uid = uid;  // NB:  may be null
        this.sender = sender;
        this.subject = subject;
        this.sentDate = sentDate;  // NB:  possibly null in some circumstances
        this.unread = unread;
        this.answered = answered;
        this.deleted = deleted;
        this.multipart = multipart;
        this.contentType = contentType;  // NB:  may be null
        this.content = content;
        this.toRecipients = toRecipients;
        this.ccRecipients = ccRecipients;
        this.bccRecipients = bccRecipients;
	}

    public EmailMessage(int messageNumber, String uid, String sender, String subject,
                        Date sentDate, boolean unread, boolean answered, boolean deleted,
                        boolean multipart, String contentType, EmailMessageContent content) {
        this(messageNumber,uid,sender,subject,sentDate,unread,answered,deleted,multipart,contentType,content,null,null,null);
    }

	public int getMessageNumber() {
        return messageNumber;
    }

	/**
	 * Returns the UID of the message as set by the Folder or <code>null</code> 
	 * if the Folder does not implement UIDFolder. 
	 * 
	 * @return The UID provided by the Folder for this message or null
	 */
	public String getUid() {
	    return uid;
	}

    /**
     * Returns the UID of the message if it is present, else the message number.
     *
     * @return UID if present else message number
     */
    public String getMessageId() {
        return StringUtils.isNotBlank(uid) ? uid : new Integer(messageNumber).toString();
    }
	
    /**
	 * Returns the date the email message was sent or <code>null</code> if the 
	 * server did not provide one.
	 * 
	 * @return Date the email message was sent or <code>null</code>
     * @throws MessagingException 
	 */
	public Date getSentDate() {
		return sentDate != null ? new Date(sentDate.getTime()) : null;
	}

	public String getSentDateString() {
	    return sentDate != null ? DATE_FORMAT.format(sentDate) : "";
	}

	/**
	 * Returns the sender of this email message.
	 *
	 * @return The sender of the email message.
	 */
	public String getSender() {
	    return sender != null ? sender : "(No Sender)";
	}

    public String getSenderName() {
        String senderName = getSender();
        if (getSender().contains("&lt;")) {
            senderName = getSender().split("\\s*&lt;")[0];
        } else if (getSender().contains("<")) {
            senderName = getSender().split("\\s*<")[0];
        }
        return senderName;
    }

    /**
	 * Returns the email message subject.
	 *
	 * @return The email message subject.
	 */
	public String getSubject() {
		if (this.subject == null) {
            return "(No Subject)";
		} else {
            return this.subject.replace("<", "&lt;").replace(">", "&gt;");
		}
	}

    public boolean isUnread() {
        return unread;
    }
    
    public void setUnread(boolean unread) {
        this.unread = unread;
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
    
    /**
     * 
     * @return The content type (e.g. "text/plain") of the message body or 
     * <code>null</code> if the content cannot be read
     */
    public String getContentType() {
        return contentType;
    }

    public EmailMessageContent getContent() {
        return content;
    }

	public String getToRecipients() {
		return toRecipients;
	}

	public String getCcRecipients() {
		return ccRecipients;
	}

	public String getBccRecipients() {
		return bccRecipients;
	}

}
