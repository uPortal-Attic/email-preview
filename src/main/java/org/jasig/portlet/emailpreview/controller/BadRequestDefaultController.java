package org.jasig.portlet.emailpreview.controller;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * Every meaningful method throws an exception.  This controller is mapped to 
 * the default handler for the Email Preview portlet.  You can only get here by 
 * sending a bad request.  If we didn't have this default handler, the portlet 
 * would enter an unusable state, be de-provisioned by the container, and become 
 * unavailable for ALL USERS.
 * 
 * @author awills
 */
public class BadRequestDefaultController extends AbstractController {
    
    private static final String ERROR_MESSAGE = "Bad Request:  Unable to map " +
    		                "a controller to the request URL.  Check " +
    		                "to be sure you're not using MSIE 6.";

    /*
     * Public API.
     */
    
    protected void handleActionRequestInternal(ActionRequest req, ActionResponse res) throws PortletException {
        throw new PortletException(ERROR_MESSAGE);
    }
    
    protected ModelAndView handleRenderRequestInternal(RenderRequest req, RenderResponse res) throws PortletException {
        throw new PortletException(ERROR_MESSAGE);
    }


}
