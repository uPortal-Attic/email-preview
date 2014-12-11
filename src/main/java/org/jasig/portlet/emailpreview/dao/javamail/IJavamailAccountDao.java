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
package org.jasig.portlet.emailpreview.dao.javamail;

import java.io.IOException;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

/**
 * This interface exists to work with the caching annotations.  This interface 
 * is package-private by intent.
 * 
 * @author awills
 */
interface IJavamailAccountDao {

    /**
     * Performs the heavy-lifting of {@link IEmailAccountService} but in a way 
     * that exposes Javamail implementation details.  Caching annotations can be 
     * wrapped around these methods.
     * 
     *
     * @param storeConfig
     * @param auth
     * @param username Portal username
     * @param mailAccount Email account
     * @param start Index of the first expected message
     * @param max Maximum size of the collection of messages in the
     * returned {@link org.jasig.portlet.emailpreview.AccountSummary} object
     * @param refresh true to ignore cache and fetch from target system
     * @return A representation of mail account details suitable for displaying
     * in the view
     * @throws EmailPreviewException
     */
    AccountSummary fetchAccountSummaryFromStore(MailStoreConfiguration storeConfig,
                                                Authenticator auth, String username, String mailAccount, int start,
                                                int max, boolean refresh) throws EmailPreviewException;

    Session openMailSession(MailStoreConfiguration config, Authenticator auth);

    Folder getUserInbox(Session session, String folderName) throws MessagingException;

    EmailMessage wrapMessage(Message msg, boolean populateContent,
            Session session) throws MessagingException, IOException,
            ScanException, PolicyException;

}
