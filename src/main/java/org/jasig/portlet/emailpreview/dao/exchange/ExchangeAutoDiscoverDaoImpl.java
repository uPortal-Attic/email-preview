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


package org.jasig.portlet.emailpreview.dao.exchange;

import com.microsoft.exchange.autodiscover.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.caching.EWSEndpointUriCacheKeyGeneratorImpl;
import org.jasig.portlet.emailpreview.caching.IEWSEndpoingUriCacheKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceOperations;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringResult;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data accessor that uses Exchange Web Services's autodiscovery process.  See
 * http://msdn.microsoft.com/en-us/library/exchange/jj900169%28v=exchg.150%29.aspx (SOAP, spec on steps for autodiscover),
 *
 * Also see:
 * http://msdn.microsoft.com/en-us/library/exchange/dd877090%28v=exchg.150%29.aspx
 * http://msdn.microsoft.com/en-us/library/exchange/gg591268%28v=exchg.140%29.aspx (Exchange Online),
 * http://msdn.microsoft.com/en-us/library/ee332364.aspx (this is for the Plain-old XML approach, not implemented).
 *
 * The current implementation:
 * - Supports only the SOAP protocol, not the Plain-old XML approach
 * - Does not use the Active Directory Service Connection Point (SCP) strategy to lookup Autodiscover URLs
 * - Does not look up records in DNS
 * - The servername to first contact is configured in the portlet preferences.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

public class ExchangeAutoDiscoverDaoImpl implements IExchangeAutoDiscoverDao {

    protected final static String AUTODISCOVER_SCHEMA = "http://schemas.microsoft.com/exchange/autodiscover/outlook/requestschema/2006";
    protected final static String AUTODISCOVER_RESPONSE_SCHEMA = "http://schemas.microsoft.com/exchange/autodiscover/outlook/responseschema/2006a";
    protected final static QName REQUEST_SERVER_VERSION_QNAME = new QName(
            "http://schemas.microsoft.com/exchange/2010/Autodiscover", "RequestedServerVersion", "a");
    protected final static QName SOAP_ACTION_HEADER_QNAME = new QName("http://www.w3.org/2005/08/addressing", "Action", "wsa");

    protected final static String SOAP_ACTION_BASE = "http://schemas.microsoft.com/exchange/2010/Autodiscover/Autodiscover/";
    protected final static String GET_USER_SETTINGS_ACTION = SOAP_ACTION_BASE + "GetUserSettings";
    private final static String INTERNAL_EWS_SERVER = "InternalEwsUrl";
    private final static String EXTERNAL_EWS_SERVER = "ExternalEwsUrl";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private String EWSServerURI = "https://{server}/EWS/exchange.asmx";
    private final ObjectFactory objectFactory = new ObjectFactory();
    private Marshaller marshaller;

    @Autowired
    @Qualifier("mailboxServernameCache")
    private Cache ewsEndpointUriCache;
    private IEWSEndpoingUriCacheKeyGenerator ewsEndpointUriCacheKeyGenerator = new EWSEndpointUriCacheKeyGeneratorImpl();

    @Autowired
    @Qualifier(value="exchangeAutodiscover")
    private WebServiceOperations webServiceOperations;

    // From http://msdn.microsoft.com/en-us/library/exchange/jj900169%28v=exchg.150%29.aspx
    private List<String> autoDiscoverURIs = Arrays.asList(new String[] {
            "https://{server}/autodiscover/autodiscover.svc",
            "https://autodiscover.{server}/autodiscover/autodiscover.svc"
    });

    public void setWebServiceOperations(WebServiceOperations webServiceOperations) {
        this.webServiceOperations = webServiceOperations;
    }

    public void setEwsEndpointUriCache(Cache ewsEndpointUriCache) {
        this.ewsEndpointUriCache = ewsEndpointUriCache;
    }

    public void setEwsEndpointUriCacheKeyGenerator(IEWSEndpoingUriCacheKeyGenerator ewsEndpointUriCacheKeyGenerator) {
        this.ewsEndpointUriCacheKeyGenerator = ewsEndpointUriCacheKeyGenerator;
    }

    public void setAutoDiscoverURIs(List<String> autoDiscoverURIs) {
        this.autoDiscoverURIs = autoDiscoverURIs;
    }

    public void setEWSServerURI(String EWSServerURI) {
        this.EWSServerURI = EWSServerURI;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    // ----------------------------------------------------------
    // get Exchange Server for mailbox
    // ----------------------------------------------------------

    @Override
    public String getEndpointUri(MailStoreConfiguration config) {
        String cacheKey = ewsEndpointUriCacheKeyGenerator.getKey(config);
        Element element = ewsEndpointUriCache.get(cacheKey);
        if (element == null) {
            String endpointUri;
            if (config.isExchangeAutodiscover()) {
                log.debug("Cache miss. Autodiscover enabled. Looking up EWS endpoint for mail account {} at host {}",
                        config.getMailAccount(), config.getHost());
                endpointUri = getAccountServiceUrl(config);
                log.info("Caching Autodiscover endpoint {} for mail account {}", endpointUri, config.getMailAccount());
            } else {
                endpointUri = EWSServerURI.replace("{server}", config.getHost());
                log.debug("Cache miss. Not using Autodiscover. Caching computed EWS endpoint {} for mail account {}",
                        endpointUri, config.getMailAccount());
            }
            Element el = new Element(cacheKey, endpointUri);
            ewsEndpointUriCache.put(el);
            return endpointUri;
        } else {
            String uri = (String) element.getValue();
            log.debug("Cache hit for EWS endpoint for mail account {} to endpoint URI {}", config.getMailAccount(), uri);
            return uri;
        }
    }

    private String getAccountServiceUrl(MailStoreConfiguration config) {
        UserSettings userSettings = sendMessageAndExtractSingleResponse(
                createGetUserSettingsSoapMessage(config.getMailAccount()), GET_USER_SETTINGS_ACTION, config);

        //Give preference to Internal URL over External URL
        String internalUri = null;
        String externalUri = null;

        for (UserSetting userSetting : userSettings.getUserSettings()) {
            String potentialAccountServiceUrl = ((StringSetting) userSetting).getValue().getValue();
            if (EXTERNAL_EWS_SERVER.equals(userSetting.getName())) {
                externalUri = potentialAccountServiceUrl;
            }
            if (INTERNAL_EWS_SERVER.equals(userSetting.getName())) {
                internalUri = potentialAccountServiceUrl;
            }
        }
        if (internalUri == null && externalUri == null) {
            throw new EmailPreviewException("Unable to find EWS Server URI in properies "
                    + EXTERNAL_EWS_SERVER + " or " + INTERNAL_EWS_SERVER + " from User's Autodiscover record");
        }
        return internalUri != null ? internalUri : externalUri;
    }

    private GetUserSettingsRequestMessage createGetUserSettingsSoapMessage(String emailAddress) {
        GetUserSettingsRequest msg = objectFactory.createGetUserSettingsRequest();

        User user = objectFactory.createUser();
        user.setMailbox(emailAddress);
        Users users = objectFactory.createUsers();
        users.getUsers().add(user);
        msg.setUsers(users);

        msg.setRequestedVersion(ExchangeVersion.EXCHANGE_2010);

        RequestedSettings settings = objectFactory.createRequestedSettings();
        settings.getSettings().add(EXTERNAL_EWS_SERVER);
        settings.getSettings().add(INTERNAL_EWS_SERVER);
        msg.setRequestedSettings(settings);

        // Construct the SOAP request object to use
        GetUserSettingsRequestMessage request = objectFactory.createGetUserSettingsRequestMessage();
        request.setRequest(objectFactory.createGetUserSettingsRequestMessageRequest(msg));
        return request;
    }

    private UserSettings sendMessageAndExtractSingleResponse(GetUserSettingsRequestMessage soapRequest, String soapAction,
            MailStoreConfiguration config) {
        GetUserSettingsResponseMessage soapResponseMessage = (GetUserSettingsResponseMessage) sendSoapRequest(soapRequest, soapAction, config);
        GetUserSettingsResponse soapResponse = soapResponseMessage.getResponse().getValue();

        UserSettings userSettings = null;
        boolean warning = false;
        boolean error = false;
        StringBuilder msg = new StringBuilder();
        if (!ErrorCode.NO_ERROR.equals(soapResponse.getErrorCode())) {
            error = true;
            msg.append("Error: ").append(soapResponse.getErrorCode().value())
                    .append(": ").append(soapResponse.getErrorMessage().getValue()).append("\n");
        } else {
            JAXBElement<ArrayOfUserResponse> JAXBresponseArray = soapResponse.getUserResponses();
            ArrayOfUserResponse responseArray = JAXBresponseArray != null ? JAXBresponseArray.getValue() : null;
            List<UserResponse> responses = responseArray != null ? responseArray.getUserResponses() : new ArrayList<UserResponse>();
            if (responses.size() == 0) {
                error = true;
                msg.append("Error: Autodiscovery returned no Exchange mail server for mailbox");
            } else if (responses.size() > 1) {
                warning = true;
                msg.append("Warning: Autodiscovery returned multiple responses for Exchange server mailbox query");
            } else {
                UserResponse userResponse = responses.get(0);
                if (!ErrorCode.NO_ERROR.equals(userResponse.getErrorCode())) {
                    error = true;
                    msg.append("Received error message obtaining user mailbox's server. Error "
                            + userResponse.getErrorCode().value() + ": " + userResponse.getErrorMessage().getValue());
                }
                userSettings = userResponse.getUserSettings().getValue();
            }
        }
        if (warning || error) {
            StringBuilder errorMessage = new StringBuilder("Unexpected response from soap action: "
                    + soapAction + ".\nSoap Request: " + soapRequest.toString() + "\n");
            errorMessage.append(msg);
            if (error) {
                throw new EmailPreviewException("Error performing Exchange web service action "
                        + soapAction + ". Error code is " + errorMessage.toString());
            }
            log.warn("Received warning response to soap request " + soapAction + ". Error text is:\n" + errorMessage.toString());
            throw new EmailPreviewException("Unable to perform " + soapAction + " operation; try again later. Message text: "
                    + errorMessage.toString());
        }
        return userSettings;
    }

    // ----------------------------------------------------------
    // common send message and parse response
    // ----------------------------------------------------------

    private Object sendSoapRequest (Object soapRequest, String soapAction, MailStoreConfiguration config) {
        Exception ex = null;

        // Try each connection URI pattern until one works
        for (String pattern : autoDiscoverURIs) {
            String uri = pattern.replace("{server}", config.getHost());
            try {
                Object response = sendSoapMessageToServer(soapRequest, soapAction, uri);
                return response;
            } catch (WebServiceClientException e) {
                ex = e;
                // todo should we bother catching/wrapping? I think runtime exceptions should be caught at service layer
                throw new EmailPreviewException(e);
            }
            //todo figure out if we can catch authentication exceptions to return them separate. Useful?
        }
        throw new EmailPreviewException("Unable to use autodiscover on host " + config.getHost(), ex);
    }

    private Object sendSoapMessageToServer(Object soapRequest, final String soapAction, String uri) {
        final WebServiceMessageCallback actionCallback = new SoapActionCallback(
                soapAction);

        final WebServiceMessageCallback customCallback = new WebServiceMessageCallback() {

            @Override
            public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                actionCallback.doWithMessage(message);
                SoapMessage soap = (SoapMessage) message;
                soap.getEnvelope().getHeader().addHeaderElement(REQUEST_SERVER_VERSION_QNAME)
                        .setText(ExchangeVersion.EXCHANGE_2010.value());
                soap.getEnvelope().getHeader().addHeaderElement(SOAP_ACTION_HEADER_QNAME).setText(soapAction);
            }

        };

        if (log.isDebugEnabled()) {
            StringResult message = new StringResult();
            try {
                marshaller.marshal(soapRequest, message);
                log.debug("Attempting to send SOAP request to {}\nSoap Action: {}\nSoap message body"
                        + " (not exact, log org.apache.http.wire to see actual message):\n{}",
                        uri, soapAction, message);
            } catch (IOException ex) {
                log.debug("IOException attempting to display soap response", ex);
            }
        }

        // use the request to retrieve data from the Exchange server
        Object response = webServiceOperations.marshalSendAndReceive(uri, soapRequest, customCallback);

        if (log.isDebugEnabled()) {
            StringResult messageResponse = new StringResult();
            try {
                marshaller.marshal(response, messageResponse);
                log.debug("Soap response body (not exact, log org.apache.http.wire to see actual message):\n{}", messageResponse);
            } catch (IOException exception) {
                log.debug("IOException attempting to display soap response", exception);
            }
        }
        return response;
    }

}
