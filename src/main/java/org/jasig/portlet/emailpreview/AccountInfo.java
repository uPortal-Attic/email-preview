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

import java.util.List;

/**
 * An entity object that abstracts the email account information
 * of a user. Currently, all the information retrieved is related
 * to the user's inbox.
 *
 * @author Andreas Christoforides
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class AccountInfo {

	private int unreadMessageCount;
	private int totalMessageCount;

	private List<EmailMessage> messages;

	/**
	 * Returns the number of unread messages in the user's inbox. The number
	 * of unread messages is bounded by the
	 * {@link #inspectedMessageCount inspectedMessageCount} attribute.
	 *
	 * @return The number of unread messages in the user's inbox.
	 */
	public int getUnreadMessageCount() {
		return this.unreadMessageCount;
	}

	/**
	 * Sets the number of unread messages in the user's inbox. The number
	 * of unread messages is bounded by the
	 * {@link #inspectedMessageCount inspectedMessageCount} attribute.
	 *
	 * @param unreadMessageCount The number of unread messages in
	 * the user's inbox.
	 */
	public void setUnreadMessageCount(int unreadMessageCount) {
		this.unreadMessageCount = unreadMessageCount;
	}

	/**
	 * Returns a list that contains a certain number of the most recent
	 * unread emails in the user's inbox. The number of unread messages
	 * is controlled by a property in <code>mail.properties</code>.
	 *
	 * @return A <code>List<EmailMessage></code> containing
	 * information about the most recent unread emails in the user's inbox.
	 */
	public List<EmailMessage> getMessages() {

		return this.messages;
	}

	/**
	 * Sets a list that contains a certain number of the most recent
	 * unread emails in the user's inbox. The number of unread messages
	 * is controlled by a property in <code>mail.properties</code>.
	 *
	 * @param unreadMessages A <code>List<EmailMessage></code> containing
	 * information about the most recent unread emails in the user's inbox.
	 */
	public void setMessages(List<EmailMessage> unreadMessages) {
		this.messages = unreadMessages;
	}

	/**
	 * Returns the number of total messages in the user's inbox. The number is not
	 * bounded by the value of the {@link #inspectedMessageCount inspectedMessageCount}
	 * attribute.
	 *
	 * @return The total number of messages in the user's inbox.
	 */
	public int getTotalMessageCount() {
		return this.totalMessageCount;
	}

	/**
	 * Sets the number of total messages in the user's inbox. The number is not
	 * bounded by the value of the {@link #inspectedMessageCount inspectedMessageCount}
	 * attribute.
	 *
	 * @param totalMessagecount The number of total messages in the user's inbox.
	 */
	public void setTotalMessageCount(int totalMessagecount) {
		this.totalMessageCount = totalMessagecount;
	}

}
