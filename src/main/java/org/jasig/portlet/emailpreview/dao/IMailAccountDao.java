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
package org.jasig.portlet.emailpreview.dao;

import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

import javax.mail.Folder;
import java.util.List;

/**
 * This interface exists to shield the service layer from various DAO implementations.
 * 
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public interface IMailAccountDao<T extends Folder> {

    /**
     * Obtains a summary of the email messages from the configured store (to/from/subject/date) and general
     * mailbox summary.
     *
     * @param storeConfig mailstore configuration
     * @param username Portal username
     * @param mailAccount Email account
     * @param start Index of the first expected message
     * @param max Maximum number of messages to return
     * @param folder mailbox folder to use
     * @return A representation of mail account details suitable for displaying in the view
     */
    AccountSummary fetchAccountSummaryFromStore(MailStoreConfiguration storeConfig, String username,
                                                String mailAccount, String folder, int start, int max);

    /**
     * Gets a message from the mail server.
     * @param uuid Message
     * @param storeConfig mail configuration
     * @return message
     */
    EmailMessage getMessage(MailStoreConfiguration storeConfig, String uuid);

    /**
     * Delete the list of messages from Exchange.
     * @param uuids uuids if the messages to delete
     * @return True if messages were deleted
     */
    boolean deleteMessages(MailStoreConfiguration storeConfig, String[] uuids);

    /**
     * Sets the isRead status of the indicated messages to the indicated value.
     * @param uuids uuids of the messages to change the read status of
     * @param read true to indicate message has been read
     * @return true if read status was successfully set
     */
    boolean setMessageReadStatus(MailStoreConfiguration storeConfig, String[] uuids, boolean read);

    /**
     * Gets all the user's inbox folders.
     * @return List of user's folders.
     */
    List<T> getAllUserInboxFolders(MailStoreConfiguration storeConfig);

}
