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
package org.jasig.portlet.emailpreview.dao;

import javax.mail.Folder;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;

/**
 * IEmailAccountDao is repsonsible for retrieving email messages from an
 * arbitrary email store.
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
public interface IEmailAccountService {

    /**
     * Obtains the {@link AccountSummary} object from the data store itself, or
     * possibly from cache.
     *
     * @param req The current {@link PortletRequest}
     * @param start
     * @param max The maximum number of messages (header info) that may be 
     * returned with the summary
     * @param refresh True if the concrete service implementation must not 
     * return cached data;  otherwise false
     * @return
     * @throws MailAuthenticationException When authentication with the mail server fails
     * @throws EmailPreviewException On other errors
     */
    public AccountSummary getAccountSummary(PortletRequest req, int start,
            int max, boolean refresh, String folder) throws EmailPreviewException;

    /**
     * Retrieve an individual message from the mail store.
     *
     * @param req The current {@link PortletRequest}
     * @param messageNum Index of the selected message
     * @return
     */
    public EmailMessage getMessage(PortletRequest req, int messageNum);

    /**
     * Delete and expunge the specified massages from the store.  Supported for
     * implementations that provide UIDs (e.g. UIDFolder) only.
     *
     * @param req The current {@link PortletRequest}
     * @param uids Array of message UIDs to delete
     * @return Success or failure
     */
    boolean deleteMessages(PortletRequest req, long[] uids);

    /**
     * Switch the value of the SEEN flag for the specified messages.  Supported 
     * for implementations that provide UIDs (e.g. UIDFolder) only.
     *
     * @param req The current {@link PortletRequest}
     * @param uids
     * @param value The new value of the seen flag 
     * @return Success or failure
     */
    public boolean setSeenFlag(PortletRequest req, long[] uids, boolean value);

    Folder[] getAllUserInboxFolders(PortletRequest req);
}