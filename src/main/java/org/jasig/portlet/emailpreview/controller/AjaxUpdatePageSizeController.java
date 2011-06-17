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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.servlet.HttpErrorResponseController;
import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Responsible for remembering the user's pageSize selection with 
 * <code>PortletPreferences</code>.
 *
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("VIEW")
public class AjaxUpdatePageSizeController {
    
    private static final String STATUS_KEY = "success";

    private AjaxPortletSupportService ajaxPortletSupportService;
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Set the service for handling portlet AJAX requests.
     *
     * @param ajaxPortletSupportService
     */
    @Autowired(required = true)
    public void setAjaxPortletSupportService(AjaxPortletSupportService ajaxPortletSupportService) {
            this.ajaxPortletSupportService = ajaxPortletSupportService;
    }

    @RequestMapping(params = "action=updatePageSize")
    public void updatePageSize(ActionRequest req, ActionResponse res, 
                @RequestParam("newPageSize") int newPageSize) throws Exception {

        PortletPreferences prefs = req.getPreferences();

        // Define view and generate model
        Map<String, Object> model = new HashMap<String, Object>();

        if (!prefs.isReadOnly(EmailSummaryController.PAGE_SIZE_PREFERENCE)) {
            prefs.setValue(EmailSummaryController.PAGE_SIZE_PREFERENCE, Integer.toString(newPageSize));
            prefs.store();
            model.put(STATUS_KEY, true);
            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, req, res);
        } else {
            if (log.isDebugEnabled()) {
                String msg = "Ignoring chanhe to pageSize for the following " 
                        + "user because the preference is read only:  " 
                        + req.getRemoteUser();
                log.debug(msg);
            }
            model.put(HttpErrorResponseController.HTTP_ERROR_CODE, 500);
            ajaxPortletSupportService.redirectAjaxResponse("ajax/error", model, req, res);
        }

    }

}
