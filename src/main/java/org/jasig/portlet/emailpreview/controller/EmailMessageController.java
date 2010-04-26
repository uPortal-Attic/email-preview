package org.jasig.portlet.emailpreview.controller;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationService;
import org.jasig.portlet.emailpreview.service.auth.IAuthenticationServiceRegistry;
import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("VIEW")
public class EmailMessageController {

    
    protected final Log log = LogFactory.getLog(getClass());

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

    private AjaxPortletSupportService ajaxPortletSupportService;
    
    /**
     * Set the service for handling portlet AJAX requests.
     * 
     * @param ajaxPortletSupportService
     */
    @Autowired(required = true)
    public void setAjaxPortletSupportService(
                    AjaxPortletSupportService ajaxPortletSupportService) {
            this.ajaxPortletSupportService = ajaxPortletSupportService;
    }
    
    private IAuthenticationServiceRegistry authServiceRegistry;
    
    @Autowired(required = true) 
    public void setAuthenticationServiceRegistry(IAuthenticationServiceRegistry authServiceRegistry) {
        this.authServiceRegistry = authServiceRegistry;
    }
    
    @RequestMapping(params = "action=emailMessage")
    public void getAccountSummary(ActionRequest request, ActionResponse response,
            @RequestParam("messageNum") int messageNum){

        try {
            
            MailStoreConfiguration config = mailStoreDao.getConfiguration(request);

            IAuthenticationService authService = authServiceRegistry.getAuthenticationService(config.getAuthenticationServiceKey());
            Authenticator auth = authService.getAuthenticator(request, config);

            // Get current user's account information
            EmailMessage message = accountDao.retrieveMessage(config, auth, messageNum);
    
            // Define view and generate model
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("message", message);
            
            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, request, response);
            
        } catch (Exception ex) {
            log.error(ex);
        }
        
    }


}
