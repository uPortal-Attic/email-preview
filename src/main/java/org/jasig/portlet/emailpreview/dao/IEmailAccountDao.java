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
 * @version $Revision$
 */
public interface IEmailAccountDao {

    /**
     * Retrieve a list of recent email from the mail store, as well as a 
     * summary of the email account's current state. 
     * 
     * @param storeConfig
     * @param auth
     * @param maxMessages
     * @return
     * @throws EmailPreviewException
     */
    public AccountInfo retrieveEmailAccountInfo(
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
    
}