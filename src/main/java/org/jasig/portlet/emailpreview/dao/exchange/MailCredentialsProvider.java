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
package org.jasig.portlet.emailpreview.dao.exchange;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.ICredentialsProvider;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

@Component
public class MailCredentialsProvider implements CredentialsProvider, ICredentialsProvider {

    private static final String EXCHANGE_CREDENTIALS_ATTRIBUTE = "exchangeCredentials";
    private static final String JAVAMAIL_CREDENTIALS_ATTRIBUTE = "javamailCredentials";

    @Override
    public void clear() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // Exchange
            requestAttributes.setAttribute(
                    MailCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE,
                    null, RequestAttributes.SCOPE_REQUEST);

            // Javamail
            requestAttributes.setAttribute(
                    MailCredentialsProvider.JAVAMAIL_CREDENTIALS_ATTRIBUTE,
                    null, RequestAttributes.SCOPE_REQUEST);
        }
    }

    @Override
    public Credentials getCredentials() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final Credentials credentials = (Credentials) requestAttributes.getAttribute(
                MailCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST);
        return credentials;
    }

    @Override
    public Credentials getCredentials(AuthScope authscope) {
        return getCredentials();
    }

        @Override
    public void setCredentials(AuthScope authscope, Credentials credentials) {
     throw new UnsupportedOperationException("Unsupported method - use initialize");
    }

    @Override
    public Authenticator getAuthenticator() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final Authenticator authenticator = (Authenticator) requestAttributes.getAttribute(
                MailCredentialsProvider.JAVAMAIL_CREDENTIALS_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST);
        return authenticator;
    }

    @Override
    public void initialize(PortletRequest request, MailStoreConfiguration config, IAuthenticationService authService) {

        // Exchange
        Credentials credentials = authService.getCredentials(request, config);

        // cache the credentials object to this thread
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            requestAttributes = new PortletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
        requestAttributes.setAttribute(
                MailCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE,
                credentials, RequestAttributes.SCOPE_REQUEST);

        // Javamail
        Authenticator authenticator = authService.getAuthenticator(request, config);
        requestAttributes.setAttribute(
                MailCredentialsProvider.JAVAMAIL_CREDENTIALS_ATTRIBUTE,
                authenticator, RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    public String getUsername() {
        return getCredentials(null).getUserPrincipal().getName();
    }

}
