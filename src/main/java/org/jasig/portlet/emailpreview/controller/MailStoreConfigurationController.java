package org.jasig.portlet.emailpreview.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.dao.impl.IMailStoreDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("CONFIG")
public class MailStoreConfigurationController {

    private IMailStoreDao mailStoreDao;
    
    @Autowired(required = true)
    public void setAccountDao(IMailStoreDao mailStoreDao) {
        this.mailStoreDao = mailStoreDao;
    }
    
    private List<String> protocols;
    
    @Resource(name="protocols")
    @Required
    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }

    @RequestMapping
    public String getAccountFormView() {
        return "config";
    }
    
    @RequestMapping(params = "action=updateConfiguration")
    public void saveAccountConfiguration(ActionRequest request,
            ActionResponse response,
            @ModelAttribute("form") MailStoreConfiguration config)
            throws PortletModeException {
        
        mailStoreDao.saveConfiguration(request, config);
        
        response.setPortletMode(PortletMode.VIEW);
    }
    
    @RequestMapping(params = "action=cancelConfiguration")
    public void cancelAccountConfiguration(ActionRequest request,
            ActionResponse response) throws PortletModeException {
        
        response.setPortletMode(PortletMode.VIEW);
    }
    
    @ModelAttribute("form")
    public MailStoreConfiguration getConfigurationForm(PortletRequest request) {
        return mailStoreDao.getConfiguration(request);
    }
    
    @ModelAttribute("protocols")
    public List<String> getProtocols() {
        return this.protocols;
    }

}
