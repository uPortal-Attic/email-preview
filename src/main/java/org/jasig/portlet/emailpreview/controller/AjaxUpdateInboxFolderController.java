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
package org.jasig.portlet.emailpreview.controller;


import java.util.Hashtable;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * Get all the IMAP folders from server
 *
 * @author JP TRAN
 */
@Controller
@RequestMapping("VIEW")
public class AjaxUpdateInboxFolderController extends BaseEmailController {

    public static final String SELECTED_OPTION = "selected";
    public static final String INBOX_DEFAULT_VALUE_PREF = "INBOX"; 
    public static final String AUTH_SERVICE_PREFERENCE = "authenticationServiceKey";
    public static final String AUTH_SERVICE_DEFAULT_VALUE_PREFERENCE = "dummy";
    public static final String AUTH_SERVICE_TEST_PREFERENCE = "demoAuthService";
    public static final String UNREAD_MSG_FLAG = ":unread";
    
	@ResourceMapping("inboxFolder")
    public  ModelAndView  inboxFolder(ResourceRequest req, ResourceResponse res) throws MessagingException {
        IEmailAccountService accountDao = serviceBroker.getEmailAccountService(req);
		 
     PortletPreferences prefs = req.getPreferences();
     String selectedFolder= prefs.getValue(EmailAccountSummaryController.INBOX_NAME_PREFERENCE, INBOX_DEFAULT_VALUE_PREF);
     String authenticationServiceKey = prefs.getValue(AUTH_SERVICE_PREFERENCE, AUTH_SERVICE_DEFAULT_VALUE_PREFERENCE);
     
     Map<String, String> jsonData = new Hashtable<String, String>() ;
     
     if(AUTH_SERVICE_TEST_PREFERENCE.equals(authenticationServiceKey)){
    	 jsonData.put("INBOX", "INBOX");
    	 //For demo : must have the same name that the json files in src/main/resources
    	 jsonData.put("demoTest", "demoTest");
    	 jsonData.put("Important", "Important");
    	 for (Map.Entry<String, String> entry : jsonData.entrySet() ){
			if(entry.getValue().equals(selectedFolder)){
				jsonData.put(SELECTED_OPTION, entry.getValue());
				break;
			}
    	 }
     }else{
            for (Folder folderName : accountDao.getAllUserInboxFolders(req)){
            	if ((folderName.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
            		String unreadMsgCount = "";
            		if(folderName.getUnreadMessageCount()!=0 && (!selectedFolder.equals(folderName.toString()))){
            			unreadMsgCount = " (".concat(String.valueOf(folderName.getUnreadMessageCount())).concat(")").concat(UNREAD_MSG_FLAG);
            		}
            		jsonData.put(folderName.toString().concat(unreadMsgCount), folderName.toString());
                	if(selectedFolder.equals(folderName.toString())){
                		jsonData.put(SELECTED_OPTION,folderName.toString());
                	}
                }
    		}
     }
      	return new ModelAndView("json", jsonData);           
    }
}
