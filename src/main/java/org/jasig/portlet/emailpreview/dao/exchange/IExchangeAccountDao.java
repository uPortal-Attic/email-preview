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
package org.jasig.portlet.emailpreview.dao.exchange;

import java.util.List;

import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.ExchangeFolderDto;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * This interface exists to work with the caching annotations.  This interface 
 * is package-private by intent.
 * 
 * @author James Wennmacher, jwennmacher@unicon.net
 */
interface IExchangeAccountDao {

    void clearAccountSummaryCache(String username, String mailAccount);

    /**
     * Performs the heavy-lifting of {@link IEmailAccountService} but in a way
     * that exposes Exchange implementation details.  Caching annotations can be
     * wrapped around these methods.
     *
     *
     * @param storeConfig
     * @param username Portal username
     * @param mailAccount Email account
     * @param start Index of the first expected message
     * @param max Maximum size of the collection of messages in the
     * returned {@link org.jasig.portlet.emailpreview.AccountSummary} object
     * @param folder
     * @return A representation of mail account details suitable for displaying
     * in the view
     * @throws org.jasig.portlet.emailpreview.EmailPreviewException
     */
    AccountSummary fetchAccountSummaryFromStore(MailStoreConfiguration storeConfig,
                                                String username, String mailAccount, int start,
                                                int max, String folder) throws EmailPreviewException;


    /**
     * Gets a message from the Exchange server.
     * @param uuid Message
     * @param storeConfig mail configuration
     * @return message
     */
    EmailMessage getMessage(MailStoreConfiguration storeConfig, String uuid);

    /**
     * Delete the list of messages from Exchange.
     * @param uuids uuids if the messages to delete
     */
    void deleteMessages(MailStoreConfiguration storeConfig, String[] uuids);

    /**
     * Sets the isRead status of the indicated messages to the indicated value.
     * @param uuids uuids of the messages to change the read status of
     * @param read true to indicate message has been read
     */
    void setMessageReadStatus(MailStoreConfiguration storeConfig, String[] uuids, boolean read);

    /**
     * Gets all the user's inbox folders.
     * @return List of user's folders.
     */
    List<ExchangeFolderDto> getAllUserInboxFolders(MailStoreConfiguration storeConfig);

}
