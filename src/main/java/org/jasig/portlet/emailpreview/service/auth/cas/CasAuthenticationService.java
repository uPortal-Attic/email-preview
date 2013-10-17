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
package org.jasig.portlet.emailpreview.service.auth.cas;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.auth.BaseCredentialsAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.SimplePasswordAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

public class CasAuthenticationService extends BaseCredentialsAuthenticationService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    protected String key = "cas";
    
    protected ICASProxyTicketService casTicketService;
    protected String serviceUrl;  
    
    protected String CAS_ASSERTION_KEY = "CAS_ASSERTION_KEY";
    
    public void setCasTicketService(ICASProxyTicketService casTicketService) {
		this.casTicketService = casTicketService;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	protected String getProxyPrincipalname(PortletRequest request) {
		Assertion casAssertion = getCasAssertion(request);
		String proxyPrincipalname = casAssertion.getPrincipal().getName();
		return proxyPrincipalname;
	}
		
	protected Assertion getCasAssertion(PortletRequest request) {
		PortletSession session = request.getPortletSession(true);
	    Assertion casAssertion = (Assertion) session.getAttribute(CAS_ASSERTION_KEY);
	    if(casAssertion == null) {
	    	casAssertion = casTicketService.getProxyTicket(request);
			if(casAssertion == null)
				throw new RuntimeException("CasAssertion is null : Auth ProxyCAS failed. Please check your CAS configuration.");
		    session.setAttribute(CAS_ASSERTION_KEY, casAssertion);     
	    }
	    return casAssertion;
	}

    @Override
    public boolean isConfigured(PortletRequest request, MailStoreConfiguration config) {
        return true;
    }

    public Authenticator getAuthenticator(PortletRequest request, MailStoreConfiguration config) {
    	Assertion casAssertion = getCasAssertion(request);
    	String proxyPrincipalname = getProxyPrincipalname(request);
    	String proxyTicket = casTicketService.getCasServiceToken(casAssertion, serviceUrl);    
        return new SimplePasswordAuthenticator(proxyPrincipalname, proxyTicket);
    }

    /**
     * CAS ticket doesn't make sense with Exchange Integration.  However this method is invoked as part of the
     * services layer initialization and is used to obtain the username so create a credential with just the username.
     * We also don't want to create a proxy ticket because that would be a 2nd trip to the CAS server (or we'd need to
     * obtain the proxy ticket and cache it to the thread so the initialization code can call both getAuthenticator
     * and getCredentials).
     */
    public Credentials getCredentials(PortletRequest request, MailStoreConfiguration config) {
        String proxyPrincipalname = getProxyPrincipalname(request);
        log.debug("CAS ticket doesn't make sense with Exchange integration, creating null password credential for "
                + proxyPrincipalname);
        return new UsernamePasswordCredentials(proxyPrincipalname, null);
    }

    public String getMailAccountName(PortletRequest request, MailStoreConfiguration config) {
    	String proxyPrincipalname = getProxyPrincipalname(request);
        return proxyPrincipalname;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
