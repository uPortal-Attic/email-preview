package org.jasig.portlet.emailpreview.controller;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Authenticator;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.emailpreview.AccountInfo;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.SimplePasswordAuthenticator;
import org.jasig.portlet.emailpreview.dao.IEmailAccountDao;
import org.jasig.portlet.emailpreview.dao.IMailStoreDao;
import org.jasig.web.service.AjaxPortletSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("VIEW")
public class EmailAccountSummaryController {
    
    protected final Log log = LogFactory.getLog(getClass());

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
    
    @RequestMapping(params = "action=accountSummary")
    public void getAccountSummary(ActionRequest request, ActionResponse response){

        try {

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
            
            ajaxPortletSupportService.redirectAjaxResponse("ajax/json", model, request, response);
            
        } catch (Exception ex) {
            log.error(ex);
        }
        
    }

    
}
