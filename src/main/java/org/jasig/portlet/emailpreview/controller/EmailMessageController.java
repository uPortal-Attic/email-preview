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

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.dao.IEmailAccountService;
import org.jasig.portlet.emailpreview.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 *
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class EmailMessageController {

    private static final String CONTENT_TYPE_TEXT_PREFIX = "text/plain;";
    
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired(required = true)
    private IEmailAccountService accountDao;

    @ResourceMapping(value = "emailMessage")
    public ModelAndView showMessage(ResourceRequest req, ResourceResponse res,
            @RequestParam("messageNum") int messageNum){

        Map<String, Object> model = new HashMap<String, Object>();

        try {

            // Get current user's account information
            EmailMessage message = accountDao.getMessage(req, messageNum);
            
            /*
             * A bit of after-market work on messages in certain circumstances
             */

            // Make links embedded in text/plain messages clickable
            String contentType = message.getContentType();  // might be null if there was an error
            if (contentType != null && contentType.startsWith(CONTENT_TYPE_TEXT_PREFIX)) {
                String messageBody = message.getContent().getContentString();
                message.getContent().setContentString(MessageUtils.addClickableUrlsToMessageBody(messageBody));
            }

            model.put("message", message);

        } catch (Exception ex) {
            log.error("Error encountered while attempting to retrieve message", ex);
        }
        
        return new ModelAndView("json", model);

    }

    @ResourceMapping(value = "deleteMessages")
    public ModelAndView deleteMessages(ResourceRequest req, ResourceResponse res,
                @RequestParam(value="selectMessage", required=false) long[] uids) {

        Map<String, Object> model = new HashMap<String, Object>();

        try {

            String deletePermitted = req.getPreferences().getValue(EmailSummaryController.ALLOW_DELETE_PREFERENCE, "true");
            if (!Boolean.valueOf(deletePermitted)) {
                String msg = "The delete function is not permitted for this portlet";
                throw new RuntimeException(msg);
            }

            if (uids != null && uids.length != 0) {
                accountDao.deleteMessages(req, uids);
            }

            model.put("success", "success");

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete specified messages", e);
        }

        return new ModelAndView("json", model);

    }

    @ResourceMapping(value = "toggleSeen")
    public ModelAndView toggleSeen(ResourceRequest req, ResourceResponse res,
                @RequestParam(value="selectMessage", required=false) long[] messages,
                @RequestParam("seenValue") boolean seenValue) {

        Map<String, Object> model = new HashMap<String, Object>();

        try {

            if (messages != null && messages.length != 0) {

                // Opportunity for improvement:  respond to return value 
                // of 'false' with some user-facing message 
                accountDao.setSeenFlag(req, messages, seenValue);

            }

            model.put("success", "success");

        } catch (Exception e) {
            throw new RuntimeException("Failed to update the seen flag for specified messages", e);
        }

        return new ModelAndView("json", model);

    }

}
