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

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.Credentials;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.service.auth.BaseCredentialsAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.SimplePasswordAuthenticator;

public class CasAuthenticationService extends BaseCredentialsAuthenticationService {

    protected final Log log = LogFactory.getLog(getClass());
    
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
				throw new RuntimeException("CasAssertion is null : Auth ProxyCAS failed ? Please check yours CAS configurations.");
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

    public Credentials getCredentials(PortletRequest req, MailStoreConfiguration config) {
        log.trace("CAS ticket doesn't make sense with Exchange integration");
        return null;
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
