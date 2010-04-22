package org.jasig.portlet.emailpreview.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/ajax/json")
public class JsonResponseController {

        @Autowired(required=true)
        private AjaxPortletSupportService ajaxPortletSupportService;
        
        public void setAjaxPortletSupportService(
                        AjaxPortletSupportService ajaxPortletSupportService) {
                this.ajaxPortletSupportService = ajaxPortletSupportService;
        }

        @RequestMapping(method = RequestMethod.GET)
        public ModelAndView getJsonResponse(HttpServletRequest request, 
                        HttpServletResponse response) throws Exception {
                Map<String, ?> model = ajaxPortletSupportService.getAjaxModel(request, response);
                return new ModelAndView("jsonView", model);
        }
}
