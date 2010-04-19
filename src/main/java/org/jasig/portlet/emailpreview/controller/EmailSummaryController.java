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
package org.jasig.portlet.emailpreview.controller;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;

/**
 * 
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class EmailSummaryController {

	private String usernameAttributeName = "user.login.id";
	private String passwordAttributeName = "password";

	private IEmailAccountDao accountDao;

	@Autowired(required = true)
	public void setAccountInfoDao(IEmailAccountDao accountInfoDao) {
		this.accountDao = accountInfoDao;
	}
	
	private IMailStoreDao mailStoreDao;
	
	@Autowired(required = true)
	public void setMailStoreDao(IMailStoreDao mailStoreDao) {
	    this.mailStoreDao = mailStoreDao;
	}

	@RequestMapping
	public ModelAndView showEmail(RenderRequest request, RenderResponse response) throws Exception {

        // TODO: provide plug-able authentication
        
        // Retrieve current user's username and password
        @SuppressWarnings("unchecked")
        Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        String username = userInfo.get(this.usernameAttributeName);
        String password = userInfo.get(this.passwordAttributeName);
        Authenticator auth = new SimplePasswordAuthenticator(username, password);
        
        MailStoreConfiguration config = mailStoreDao.getConfiguration(request);

        // Get current user's account information
        AccountInfo accountInfo =
                accountDao.retrieveEmailAccountInfo(config, auth, 10);

		// Define view and generate model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("accountInfo", accountInfo);
		ModelAndView modelAndView = new ModelAndView("preview", model);

		return modelAndView;
	}

}
