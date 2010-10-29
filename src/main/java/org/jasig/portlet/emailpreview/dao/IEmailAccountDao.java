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

import javax.mail.Authenticator;

import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;

/**
 * IEmailAccountDao is repsonsible for retrieving email messages from an
 * arbitrary email store.
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 * @version $Revision$
 */
public interface IEmailAccountDao {

    public void clearCache(String username, String mailAccount);

    /**
     * Obtains the {@link AccountInfo} object from the email store itself or
     * possibly from cache.  <strong>WARNING:</strong>  the object returned will
     * always correctly reflect the specified 'username' and 'mailAccount'
     * arguments;  the other arguments, however, are more like suggestions.
     * They will only be applied if the object must be build from the store
     * itself.  This behavior is understandably somewhat surprising (and a great
     * candidate for re-design at a later time).
     *
     * @param username
     * @param storeConfig
     * @param auth
     * @param start
     * @param maxMessages
     * @return
     * @throws MailAuthenticationException When authentication with the mail server fails
     * @throws EmailPreviewException On other errors
     */
    public AccountInfo fetchAccountInfoFromStore(String username, String mailAccount,
            MailStoreConfiguration storeConfig, Authenticator auth, int start,
            int maxMessages) throws EmailPreviewException;

    /**
     * Retrieve an individual message from the mail store.
     *
     * @param storeConfig
     * @param auth
     * @param messageNum
     * @return
     */
    public EmailMessage retrieveMessage(MailStoreConfiguration storeConfig, Authenticator auth, int messageNum);

    /**
     * Delete and expunge the specified massages from the store.  Supported for
     * UIDFolder implementations only.
     *
     * @param storeConfig
     * @param auth
     * @param uids
     * @return
     */
    boolean deleteMessages(MailStoreConfiguration storeConfig, Authenticator auth, long[] uids);

}