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

import java.util.Collections;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

/**
 * Encapsulates basic information about the email INBOX.  Typicaly sent to the 
 * browser via AJAX.
 *
 * @author Andreas Christoforides
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 * @version $Revision$
 */
public class AccountSummary {

    private final int numUnreadMessages;
    private final int numTotalMessages;
    private final List<EmailMessage> messages;
    private final int messagesStart;
    private final int messagesCount;
    private final boolean deleteSupported;
    private final Throwable errorCause;

    public AccountSummary(Folder inbox, List<EmailMessage> messages,
                        int messagesStart, int messagesCount) throws MessagingException {

        // Instance Members
        this.numUnreadMessages = inbox.getUnreadMessageCount();
        this.numTotalMessages = inbox.getMessageCount();
        this.messages = Collections.unmodifiableList(messages);
        this.messagesStart = messagesStart;
        this.messagesCount = messagesCount;
        this.deleteSupported = inbox instanceof UIDFolder;
        this.errorCause = null;

    }
    
    /**
     * Indicates the account fetch did not succeed and provides the cause.  
     * Previously we would communicate this fact by throwing an exception, but 
     * we learned that (1) the caching API we use writes these exceptions to 
     * Catalina.out, and (2) occurances of {@link MailAuthenticationException} 
     * are very common, and the logs were being flooded with them.
     * 
     * @param errorCause
     */
    public AccountSummary(Throwable errorCause) {
        
        // Assertions.
        if (errorCause == null) {
            String msg = "Argument 'errorCause' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        // Instance Members
        this.numUnreadMessages = -1;
        this.numTotalMessages = -1;
        this.messages = null;
        this.messagesStart = -1;
        this.messagesCount = -1;
        this.deleteSupported = false;
        this.errorCause = errorCause;

    }
    
    /**
     * Indicates if this object contains valid account details.  Otherwise, it 
     * will contain an error payload. 
     * 
     * @return False if this object represents an error condition instead of an 
     * account summary
     */
    public boolean isValid() {
        return errorCause == null;
    }
    
    public Throwable getErrorCause() {
        return errorCause;
    }

    /**
     * Returns the number of unread messages in the user's inbox.
     *
     * @return The number of unread messages in the user's inbox.
     */
    public int getUnreadMessageCount() {
        return this.numUnreadMessages;
    }

    /**
     * Returns the total number messages in the user's inbox.
     *
     * @return The total number of messages in the user's inbox.
     */
    public int getTotalMessageCount() {
        return this.numTotalMessages;
    }

    /**
     * Returns a list that contains the emails bound by <code>messagesStart</code>
     * and <code>messagesCode</code>.
     *
     * @return A <code>List<EmailMessage></code> containing information about
     * emails in the user's inbox
     */
    public List<EmailMessage> getMessages() {

        return this.messages;
    }

    /**
     * Returns the index of the first message in the Messages list.
     *
     * @return
     */
    public int getMessagesStart() {
        return this.messagesStart;
    }

    /**
     * Returns the number of messages requested for the Messages list.  The
     * actual size of the list may be lower.
     *
     * @return
     */
    public int getMessagesCount() {
        return this.messagesCount;
    }

    public boolean isDeleteSupported() {
        return deleteSupported;
    }

}
