package org.jasig.portlet.emailpreview.dao.impl;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.jasig.portlet.emailpreview.MailStoreConfiguration;

public interface IMailStoreDao {

    public abstract MailStoreConfiguration getConfiguration(
            PortletRequest request);

    public abstract void saveConfiguration(ActionRequest request,
            MailStoreConfiguration config);

}