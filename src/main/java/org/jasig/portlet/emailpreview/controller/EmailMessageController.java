/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.emailpreview.controller;

import java.util.HashMap;
import java.util.Map;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.util.MessageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class EmailMessageController extends BaseEmailController {

  private static final String CONTENT_TYPE_TEXT_PREFIX = "text/plain;";

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @ResourceMapping(value = "emailMessage")
  public ModelAndView showMessage(
      ResourceRequest req, ResourceResponse res, @RequestParam("messageId") String messageId) {

    Map<String, Object> model = new HashMap<String, Object>();

    try {

      // Get current user's account information
      EmailMessage message = getEmailAccountService(req).getMessage(req, messageId);

      /*
       * A bit of after-market work on messages in certain circumstances
       */

      String messageBody = message.getContent().getContentString();

      // Make links embedded in text/plain messages clickable
      String contentType = message.getContentType(); // might be null if there was an error
      if (contentType != null && contentType.startsWith(CONTENT_TYPE_TEXT_PREFIX)) {
        messageBody = MessageUtils.addClickableUrlsToMessageBody(messageBody);
      }

      messageBody = MessageUtils.addMissingTargetToAnchors(messageBody);

      message.getContent().setContentString(messageBody);

      model.put("message", message);

    } catch (Exception ex) {
      log.error("Error encountered while attempting to retrieve message", ex);
    }

    return new ModelAndView("json", model);
  }

  @ResourceMapping(value = "deleteMessages")
  public ModelAndView deleteMessages(
      ResourceRequest req,
      ResourceResponse res,
      @RequestParam(value = "selectMessage", required = false) String[] uids) {

    Map<String, Object> model = new HashMap<String, Object>();

    try {

      String deletePermitted =
          req.getPreferences().getValue(EmailSummaryController.ALLOW_DELETE_PREFERENCE, "true");
      if (!Boolean.valueOf(deletePermitted)) {
        String msg = "The delete function is not permitted for this portlet";
        throw new RuntimeException(msg);
      }

      if (uids != null && uids.length != 0) {
        getEmailAccountService(req).deleteMessages(req, uids);
      }

      model.put("success", "success");

    } catch (Exception e) {
      throw new RuntimeException("Failed to delete specified messages", e);
    }

    return new ModelAndView("json", model);
  }

  @ResourceMapping(value = "toggleSeen")
  public ModelAndView toggleSeen(
      ResourceRequest req,
      ResourceResponse res,
      @RequestParam(value = "selectMessage", required = false) String[] messageIds,
      @RequestParam("seenValue") boolean seenValue) {

    Map<String, Object> model = new HashMap<String, Object>();

    try {

      if (messageIds != null && messageIds.length != 0) {

        // Opportunity for improvement:  respond to return value
        // of 'false' with some user-facing message
        getEmailAccountService(req).setSeenFlag(req, messageIds, seenValue);
      }

      model.put("success", "success");

    } catch (Exception e) {
      throw new RuntimeException("Failed to update the seen flag for specified messageIds", e);
    }

    return new ModelAndView("json", model);
  }
}
