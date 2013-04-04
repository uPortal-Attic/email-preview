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

import javax.portlet.PortletRequest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;

public class ExchangeWsCredentialsProvider implements CredentialsProvider, IExchangeCredentialsService {

    protected static final String EXCHANGE_CREDENTIALS_ATTRIBUTE = "exchangeCredentials";

    @Override
    public void clear() { /* no-op */}

    @Override
    public Credentials getCredentials(AuthScope authscope) {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final Credentials credentials = (Credentials) requestAttributes.getAttribute(
                ExchangeWsCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE, 
                RequestAttributes.SCOPE_SESSION);            
        return credentials;
    }

    @Override
    public void setCredentials(AuthScope authscope, Credentials credentials) {
     throw new UnsupportedOperationException("Exchange does not support this method - use initialize");
    }

    @Override
    public void initialize(PortletRequest request, MailStoreConfiguration config, IAuthenticationService authService) {

        Credentials credentials = authService.getCredentials(request, config);

        // cache the credentials object to this thread
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            requestAttributes = new PortletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
        requestAttributes.setAttribute(
                ExchangeWsCredentialsProvider.EXCHANGE_CREDENTIALS_ATTRIBUTE,
                credentials, RequestAttributes.SCOPE_SESSION);
    }

    @Override
    public String getUsername() {
        return getCredentials(null).getUserPrincipal().getName();
    }

}
