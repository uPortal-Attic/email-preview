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

package org.jasig.portlet.emailpreview.service;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.http.auth.Credentials;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;

/**
 * Obtains the credentials and stores them into accessible location for mail server communication.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public interface ICredentialsProvider {
    /**
     * Initializes the credentials provider service.
     * @param request Portlet request
     * @param config Mail configuration
     * @param authService Authentication service
     */
    void initialize(PortletRequest request, MailStoreConfiguration config, IAuthenticationService authService);

    /**
     * Returns an Apache Credentials object for the current operation.
     * @return Credentials object
     */
    Credentials getCredentials();

    /**
     * Returns a Javamail Authenticator object for the current operation.
     * @return Javamail Authenticator object
     */
    Authenticator getAuthenticator();

    /**
     * Returns the username.
     * @return Current user's Username
     */
    String getUsername();
}
