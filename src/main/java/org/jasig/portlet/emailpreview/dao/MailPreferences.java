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
/**
 * 
 */
package org.jasig.portlet.emailpreview.dao;

public enum MailPreferences {
    
    /*
     * Settings used by the mail DAO 
     */
    
    PROTOCOL("protocol"),
    HOST("host"),
    PORT("port"),
    INBOX_NAME("inboxName"),
    CONNECTION_TIMEOUT("connectionTimeout"),
    TIMEOUT("timeout"),
    LINK_SERVICE_KEY("linkServiceKey"),
    AUTHENTICATION_SERVICE_KEY("authenticationServiceKey"),
    ALLOWABLE_AUTHENTICATION_SERVICE_KEYS("allowableAuthenticationServiceKeys"),
    USERNAME_SUFFIX("usernameSuffix"),
    MARK_MESSAGES_AS_READ("markMessagesAsRead"),
    ALLOW_RENDERING_EMAIL_CONTENT("allowRenderingEmailContent"),

    /*
     * Settings used by the Exchange DAO
     */
    EXCHANGE_USER_DOMAIN("exchangeUserDomain"),
    EXCHANGE_AUTODISCOVER("exchangeAutodiscover"),

    /*
     * Optional settings used by some auth services 
     */
    
    MAIL_ACCOUNT("username"),
    PASSWORD("password");

    /*
     * Implementation 
     */

    private final String key;
    MailPreferences(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
    
}