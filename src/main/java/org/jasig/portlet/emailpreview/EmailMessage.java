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

import org.apache.commons.lang.time.FastDateFormat;

/**
 * An entity abstraction for representing information
 * related to an email message.
 *
 * @author Andreas Christoforides
 * @version $Revision$
 */
public class EmailMessage {

    private int messageNumber;
	private String sender;
	private String subject;
	private Date sentDate;
	private boolean isUnread = false;
	private boolean isAnswered = false;
	private boolean isDeleted = false;
	
	private EmailMessageContent content;
	
	private static final FastDateFormat df = FastDateFormat.getInstance("h:mm a MMM d, yyyy");

	
	public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    /**
	 * Returns the date the email message was sent.
	 * @return The sent date of the email message as a <code>java.util.Date</code>.
	 */
	public Date getSentDate() {
		return this.sentDate;
	}

	/**
	 * Sets the date the email message was sent.
	 *
	 * @param sentDate The sent date of the email message.
	 */
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}
	
	public String getSentDateString() {
	    return df.format(this.sentDate);
	}

	/**
	 * Returns the sender of this email message.
	 *
	 * @return The sender of the email message.
	 */
	public String getSender() {
		return this.sender;
	}

	/**
	 * Sets the sender of this email message.
	 *
	 * @param sender The email message sender.
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * Returns the email message subject.
	 *
	 * @return The email message subject.
	 */
	public String getSubject() {
		return this.subject;
	}

	/**
	 * Sets the email message subject.
	 *
	 * @param subject The subject of the email message.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getSenderName() {
	    return this.sender.split("\\s*<")[0];
	}

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean isUnread) {
        this.isUnread = isUnread;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        this.isAnswered = answered;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public EmailMessageContent getContent() {
        return content;
    }

    public void setContent(EmailMessageContent content) {
        this.content = content;
    }

}
