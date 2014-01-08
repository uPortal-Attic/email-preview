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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import com.microsoft.exchange.messages.BaseRequestType;
import com.microsoft.exchange.messages.BaseResponseMessageType;
import com.microsoft.exchange.messages.DeleteItem;
import com.microsoft.exchange.messages.FindFolder;
import com.microsoft.exchange.messages.FindFolderResponseMessageType;
import com.microsoft.exchange.messages.FindItem;
import com.microsoft.exchange.messages.FindItemResponseMessageType;
import com.microsoft.exchange.messages.FolderInfoResponseMessageType;
import com.microsoft.exchange.messages.GetFolder;
import com.microsoft.exchange.messages.GetItem;
import com.microsoft.exchange.messages.ItemInfoResponseMessageType;
import com.microsoft.exchange.messages.ResponseMessageType;
import com.microsoft.exchange.messages.UpdateItem;
import com.microsoft.exchange.messages.UpdateItemResponseMessageType;
import com.microsoft.exchange.types.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasig.portlet.emailpreview.AccountSummary;
import org.jasig.portlet.emailpreview.EmailMessage;
import org.jasig.portlet.emailpreview.EmailMessageContent;
import org.jasig.portlet.emailpreview.EmailPreviewException;
import org.jasig.portlet.emailpreview.ExchangeEmailMessage;
import org.jasig.portlet.emailpreview.ExchangeFolderDto;
import org.jasig.portlet.emailpreview.MailStoreConfiguration;
import org.jasig.portlet.emailpreview.caching.IMailAccountCacheKeyGenerator;
import org.jasig.portlet.emailpreview.caching.IMessageCacheKeyGenerator;
import org.jasig.portlet.emailpreview.caching.MailAccountCacheKeyGeneratorImpl;
import org.jasig.portlet.emailpreview.caching.UsernameItemCacheKeyGeneratorImpl;
import org.jasig.portlet.emailpreview.dao.IMailAccountDao;
import org.jasig.portlet.emailpreview.service.ICredentialsProvider;
import org.jasig.portlet.emailpreview.service.link.IEmailLinkService;
import org.jasig.portlet.emailpreview.service.link.ILinkServiceRegistry;
import org.jasig.portlet.emailpreview.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceOperations;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringResult;

/**
 * DAO that uses Exchange Web Services to access messages.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

    // TODO:  Efficiency of the NTLM authenticated connections can be significantly improved by saving the HttpContext
    // into HttpSession so subsequent requests might use the same connection and already have gone through the
    // multi-step authentication process.  Another approach might be to alter the http client to use NTLM auth first.
    // See http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html#ntlm

public class ExchangeAccountDaoImpl implements IMailAccountDao<ExchangeFolderDto> {

    protected final static String ROOT_SOAP_ACTION = "http://schemas.microsoft.com/exchange/services/2006/messages/";
    protected final static String FIND_FOLDER_SOAP_ACTION = ROOT_SOAP_ACTION + "FindFolder";
    protected final static String GET_FOLDER_SOAP_ACTION = ROOT_SOAP_ACTION + "GetFolder";
    protected final static String FIND_ITEM_SOAP_ACTION = ROOT_SOAP_ACTION + "FindItem";
    protected final static String GET_ITEM_SOAP_ACTION = ROOT_SOAP_ACTION + "GetItem";
    protected final static String DELETE_ITEM_SOAP_ACTION = ROOT_SOAP_ACTION + "DeleteItem";
    protected final static String UPDATE_ITEM_SOAP_ACTION = ROOT_SOAP_ACTION + "UpdateItem";
    protected final static QName REQUEST_SERVER_VERSION_QNAME = new QName(
            "http://schemas.microsoft.com/exchange/services/2006/types", "RequestServerVersion", "ns3");

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static ObjectFactory typeObjectFactory = new ObjectFactory();
    private Marshaller marshaller;

    @Autowired(required = true)
    private ILinkServiceRegistry linkServiceRegistry;

    private WebServiceOperations webServiceOperations;

    @Autowired
    private IExchangeAutoDiscoverDao autoDiscoveryDao;

    @Autowired(required = true)
    private MessageUtils messageUtils;

    @Autowired(required = true)
    private ICredentialsProvider credentialsService;

    private List<String> regexFoldernameExclusionPatterns;
    private List<Pattern> foldernameExclusions = new ArrayList<Pattern>();

    @Autowired
    @Qualifier("exchangeChangeKeyCache")
    private Cache idCache;  // Used for internal caching of itemIds to changeKeys
    private IMessageCacheKeyGenerator idCacheKeyGenerator = new UsernameItemCacheKeyGeneratorImpl();

    @Autowired
    @Qualifier("exchangeFolderCache")
    private Cache folderCache;
    private IMailAccountCacheKeyGenerator folderCacheKeyGenerator = new MailAccountCacheKeyGeneratorImpl();

    private String folderCacheKeyPrefix = "ExchangeFolders";

    public void setRegexFoldernameExclusionPatterns(List<String> regexFoldernameExclusionPatterns) {
        this.regexFoldernameExclusionPatterns = regexFoldernameExclusionPatterns;
        foldernameExclusions = new ArrayList<Pattern>();
        for (String pattern : regexFoldernameExclusionPatterns) {
            foldernameExclusions.add(Pattern.compile(pattern));
        }
    }

    public void setFolderCache(Cache folderCache) {
        this.folderCache = folderCache;
    }

    public void setFolderCacheKeyGenerator(IMailAccountCacheKeyGenerator folderCacheKeyGenerator) {
        this.folderCacheKeyGenerator = folderCacheKeyGenerator;
    }

    public void setFolderCacheKeyPrefix(String folderCacheKeyPrefix) {
        this.folderCacheKeyPrefix = folderCacheKeyPrefix;
    }

    public void setIdCache(Cache idCache) {
        this.idCache = idCache;
    }

    public void setIdCacheKeyGenerator(IMessageCacheKeyGenerator idCacheKeyGenerator) {
        this.idCacheKeyGenerator = idCacheKeyGenerator;
    }

    public void setWebServiceOperations(WebServiceOperations webServiceOperations) {
        this.webServiceOperations = webServiceOperations;
    }

    public void setLinkServiceRegistry(ILinkServiceRegistry linkServiceRegistry) {
        this.linkServiceRegistry = linkServiceRegistry;
    }

    public void setMessageUtils(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }

    public void setCredentialsService(ICredentialsProvider credentialsService) {
        this.credentialsService = credentialsService;
    }

    public void setAutoDiscoveryDao(IExchangeAutoDiscoverDao autoDiscoveryDao) {
        this.autoDiscoveryDao = autoDiscoveryDao;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    // ----------------------------------------------------------
    // getEmailSummaries
    // ----------------------------------------------------------

    @Override
    public AccountSummary fetchAccountSummaryFromStore(MailStoreConfiguration config, String username,
                                                       String mailAccount, String folder, int start, int max) {

        try {

            long startTime = System.currentTimeMillis();
            FolderType folderType = getFolder(folder, config);
            List<ExchangeEmailMessage> messages = getMailboxItemSummaries(folderType, start, max, config);

            if ( log.isDebugEnabled() ) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int messagesToDisplayCount = messages.size();
                log.debug("Finished looking up email account summary. Inbox size: " + folderType.getTotalCount() +
                        " Unread message count: " + folderType.getUnreadCount() +
                        " Total elapsed time: " + elapsedTime + "ms " +
                        " Time per displayed message: " + (messagesToDisplayCount == 0 ? 0 : (elapsedTime / messagesToDisplayCount)) + "ms");
            }
            IEmailLinkService linkService = linkServiceRegistry.getEmailLinkService(config.getLinkServiceKey());
            String inboxUrl = null;
            if (linkService != null) {
                inboxUrl = linkService.getInboxUrl(config);
            }

            insertChangeKeysIntoCache(messages);

            //todo need to get deleteSupported or assume true, maybe quota
            return new AccountSummary(inboxUrl, messages, folderType.getUnreadCount(), folderType.getTotalCount(), start, max, true, null);
        } catch (EmailPreviewException e) {
            return new AccountSummary(e);
        } catch (WebServiceClientException e) {
            return new AccountSummary(e);
        }
    }

    // ----------------------------------------------------------
    // getFolder
    // ----------------------------------------------------------

    private GetFolder createGetFolderSoapMessage(String folderName, MailStoreConfiguration config) {
        // GetFolder: see http://msdn.microsoft.com/en-us/library/aa580274%28v=exchg.80%29.aspx
        // Construct the SOAP request object to use
        GetFolder msg = new GetFolder();

        // If the folder is the inbox, look it up directly by name.  Otherwise must get the folderId
        // from cache or fetching it.
        NonEmptyArrayOfBaseFolderIdsType folderList = new NonEmptyArrayOfBaseFolderIdsType();
        if (DistinguishedFolderIdNameType.INBOX.value().equalsIgnoreCase(folderName)) {
            DistinguishedFolderIdType inboxFolderId = new DistinguishedFolderIdType();
            inboxFolderId.setId(DistinguishedFolderIdNameType.INBOX);
            folderList.getFolderIdsAndDistinguishedFolderIds().add(inboxFolderId);
        } else {
            // Retrieve folder id from cache or fetch
            String folderId = retrieveFolderId(folderName, config);
            if (folderId == null) {
                throw new EmailPreviewException("Invalid folder name '" + folderName + "'");
            }
            FolderIdType folderIdType = new FolderIdType();
            folderIdType.setId(folderId);
            folderList.getFolderIdsAndDistinguishedFolderIds().add(folderIdType);
        }
        msg.setFolderIds(folderList);

        FolderResponseShapeType shapeType = new FolderResponseShapeType();
        shapeType.setBaseShape(DefaultShapeNamesType.DEFAULT);
        msg.setFolderShape(shapeType);

        return msg;
    }

    private FolderType getFolder(String folderName, MailStoreConfiguration config) {
        FolderInfoResponseMessageType response = (FolderInfoResponseMessageType)
                sendMessageAndExtractSingleResponse(createGetFolderSoapMessage(folderName, config), GET_FOLDER_SOAP_ACTION, config);

        List<BaseFolderType> folders = response.getFolders().getFoldersAndCalendarFoldersAndContactsFolders();
        if (folders.size() != 1) {
            log.error("Expected 1 folder to find folder, received " + folders.size());
            throw new EmailPreviewException("Multiple folders returned when querying for inbox");
        }
        FolderType folder = (FolderType) folders.get(0);
        return folder;
    }

    // -------------------------- findItems ---------------------------

    private FindItem createFindItemsSoapMessage(FolderType folder, int start, int fetchSize) {
        // Construct the SOAP request object to use
        FindItem msg = new FindItem();

        NonEmptyArrayOfBaseFolderIdsType folderList = new NonEmptyArrayOfBaseFolderIdsType();
        folderList.getFolderIdsAndDistinguishedFolderIds().add(folder.getFolderId());
        msg.setParentFolderIds(folderList);

        msg.setTraversal(ItemQueryTraversalType.SHALLOW);

        ItemResponseShapeType shapeType = new ItemResponseShapeType();
        // EMAILPLT-159: Use ALL_PROPERTIES because meeting requests will not have a dateSent without it.
        // Ran tests and it seemed to add around 250ms to the response time which is not great but not bad.
        // Since we cache aggressively, it is probably not worth adding all the specific properties we need and
        // testing under all conditions.
        shapeType.setBaseShape(DefaultShapeNamesType.ALL_PROPERTIES);
        addAdditionalPropertyReplied(shapeType);
        msg.setItemShape(shapeType);

        IndexedPageViewType paging = new IndexedPageViewType();
        int totalMessageCount = folder.getTotalCount();
        int entriesToReturn = Math.min(fetchSize, totalMessageCount - start);
        entriesToReturn = entriesToReturn > 0 ? entriesToReturn : 1;
        paging.setOffset(start);
        paging.setMaxEntriesReturned(entriesToReturn);
        paging.setBasePoint(IndexBasePointType.BEGINNING);
        msg.setIndexedPageItemView(paging);

        FieldOrderType order = new FieldOrderType();
        order.setOrder(SortDirectionType.DESCENDING);
        PathToUnindexedFieldType orderField = new PathToUnindexedFieldType();
        orderField.setFieldURI(UnindexedFieldURIType.ITEM_DATE_TIME_RECEIVED);
        order.setPath(typeObjectFactory.createFieldURI(orderField));
        NonEmptyArrayOfFieldOrdersType ordersType = new NonEmptyArrayOfFieldOrdersType();
        ordersType.getFieldOrders().add(order);
        msg.setSortOrder(ordersType);

        return msg;
    }

    private void addAdditionalPropertyReplied(ItemResponseShapeType shapeType) {

        // For replied-to flag.  See PR_LAST_VERB_EXECUTED.  See
        // http://social.msdn.microsoft.com/Forums/en-US/outlookdev/thread/a965e87b-1051-45e2-b093-35cba4b82e05
        // http://msdn.microsoft.com/en-us/library/cc433482%28v=EXCHG.80%29.aspx
        // http://www.outlookforums.com/threads/24025-ews-soap-how-tell-if-message-has-been-forwarded-replied/
        NonEmptyArrayOfPathsToElementType props = new NonEmptyArrayOfPathsToElementType();
        PathToExtendedFieldType prLastVerbExecuted = new PathToExtendedFieldType();
        prLastVerbExecuted.setPropertyTag("0x1081");
        prLastVerbExecuted.setPropertyType(MapiPropertyTypeType.INTEGER);
        props.getPaths().add(typeObjectFactory.createExtendedFieldURI(prLastVerbExecuted));
        shapeType.setAdditionalProperties(props);
    }

    private List<ExchangeEmailMessage> getMailboxItemSummaries(FolderType folder, int start, int fetchSize,
            MailStoreConfiguration config) {
        FindItem soapMessage = createFindItemsSoapMessage(folder, start, fetchSize);
        FindItemResponseMessageType response = (FindItemResponseMessageType)
                sendMessageAndExtractSingleResponse(soapMessage, FIND_ITEM_SOAP_ACTION, config);

        FindItemParentType rootFolder = response.getRootFolder();
        List<ItemType> items = rootFolder.getItems().getItemsAndMessagesAndCalendarItems();

        List<ExchangeEmailMessage> messages = new ArrayList<ExchangeEmailMessage>();
        int messageNumber = start;
        String contentType = null; //sensible default
        boolean deleted = false; //sensible default

        for (ItemType itemType : items) {
            MessageType item = (MessageType) itemType;
            // From can be null if you have a draft email that isn't filled out
            String from = item.getFrom() != null ? item.getFrom().getMailbox().getName() : "";
            Date dateSent = item.getDateTimeSent() != null ?
                    new Date(item.getDateTimeSent().toGregorianCalendar().getTimeInMillis()) : new Date();
            boolean answered = false; //sensible default
            if (item.getExtendedProperties().size() > 0) {
                ExtendedPropertyType prLastVerbExecuted = item.getExtendedProperties().iterator().next();
                String propValue = prLastVerbExecuted.getValue();
                // From MS-OXOMG protocol document: 102 = ReplyToSender, 103 = ReplyToAll, 104 = Forward
                answered = "102".equals(propValue) || "103".equals(propValue);
            }
            ExchangeEmailMessage message = new ExchangeEmailMessage(messageNumber, item.getItemId().getId(),
                    item.getItemId().getChangeKey(), messageUtils.cleanHTML(from),
                    messageUtils.cleanHTML(item.getSubject()), dateSent,
                    !item.isIsRead(), answered, deleted, item.isHasAttachments(), contentType, null, null, null, null);
            // EMAILPLT-162 Can add importance someday to model using
            // boolean highImportance = item.getImportance() != null ? item.getImportance().value().equals(ImportanceChoicesType.HIGH.value()) : false;
            messages.add(message);
            messageNumber++;
        }
        return messages;
    }

    // ----------------------------------------------------------
    // get Email message
    // ----------------------------------------------------------

    @Override
    public EmailMessage getMessage(MailStoreConfiguration storeConfig, String uuid) {

        ItemInfoResponseMessageType response = (ItemInfoResponseMessageType)
                sendMessageAndExtractSingleResponse(
                        createGetItemSoapMessage(uuid, DefaultShapeNamesType.ALL_PROPERTIES), GET_ITEM_SOAP_ACTION, storeConfig);

        MessageType message = (MessageType) response.getItems().getItemsAndMessagesAndCalendarItems().get(0);
        String sender = getOriginatorEmailAddress(message);
        boolean answered = false;  // Sensible default
        boolean deleted = false; // Sensible default
        String contentType = message.getBody().getBodyType().value();
        EmailMessageContent content = new EmailMessageContent(messageUtils.cleanHTML(message.getBody().getValue()),
                BodyTypeType.HTML.equals(message.getBody().getBodyType()));
        String toRecipients = getToRecipients(message);
        String ccRecipients = getCcRecipients(message);
        String bccRecipients = getBccRecipients(message);


        ExchangeEmailMessage msg =  new ExchangeEmailMessage(0, message.getItemId().getId(), message.getItemId().getChangeKey(),
                sender, messageUtils.cleanHTML(message.getSubject()),
                new Date(message.getDateTimeSent().toGregorianCalendar().getTimeInMillis()),
                !message.isIsRead(), answered, deleted, message.isHasAttachments(), contentType, 
                content, toRecipients, ccRecipients, bccRecipients);


        // Insert the changeKey into cache in case the message read status is changed again.
        insertChangeKeyIntoCache(msg.getMessageId(), msg.getExchangeChangeKey());

        return msg;
    }


    private String getToRecipients(MessageType message) {
        return getRecipients(message.getToRecipients());
    }
    
    private String getCcRecipients(MessageType message) {
        return getRecipients(message.getCcRecipients());
    }
    
    private String getBccRecipients(MessageType message) {
        return getRecipients(message.getBccRecipients());
    }

    private String getRecipients(ArrayOfRecipientsType addrs) {
        StringBuilder str = new StringBuilder();
        if (addrs != null) {
            for (EmailAddressType addr : addrs.getMailboxes()) {
                str.append(formatEmailAddress(addr));
                str.append("; ");
            }
            // Delete the trailing ; space
            str.deleteCharAt(str.length() - 1);
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    // Returns originator's email address.  Should be the from, but if not specified check the
    // sender just in case to try and return something useful.
    private String getOriginatorEmailAddress (MessageType message) {
        if (message.getFrom() != null) {
            return formatEmailAddress(message.getFrom().getMailbox());
        } else if (message.getSender() != null) {
            return formatEmailAddress(message.getSender().getMailbox());
        }
        return "Not specified";
    }

    private String formatEmailAddress(EmailAddressType emailAddr) {
        return emailAddr.getName() + " &lt;" + emailAddr.getEmailAddress() + "&gt;";
    }

    public ItemIdType getMessageChangeKey(String uuid, MailStoreConfiguration config) {
        ItemInfoResponseMessageType response = (ItemInfoResponseMessageType)
                sendMessageAndExtractSingleResponse(
                        createGetItemSoapMessage(uuid, DefaultShapeNamesType.ID_ONLY), GET_ITEM_SOAP_ACTION, config);

        MessageType message = (MessageType) response.getItems().getItemsAndMessagesAndCalendarItems().get(0);
        return message.getItemId();
    }

    private GetItem createGetItemSoapMessage(String uuid, DefaultShapeNamesType itemShape) {
        // Construct the SOAP request object to use
        GetItem msg = new GetItem();

        NonEmptyArrayOfBaseItemIdsType itemList = new NonEmptyArrayOfBaseItemIdsType();
        ItemIdType item = new ItemIdType();
        item.setId(uuid);
        itemList.getItemIdsAndOccurrenceItemIdsAndRecurringMasterItemIds().add(item);
        msg.setItemIds(itemList);

        ItemResponseShapeType shape = new ItemResponseShapeType();
        shape.setBaseShape(itemShape);
        shape.setIncludeMimeContent(true);
        shape.setBodyType(BodyTypeResponseType.BEST);
        addAdditionalPropertyReplied(shape);
        msg.setItemShape(shape);

        return msg;
    }

    // ----------------------------------------------------------
    // delete messages
    // ----------------------------------------------------------

    @Override
    public boolean deleteMessages(MailStoreConfiguration storeConfig, String[] uuids) {
        sendMessageAndExtractSingleResponse(createDeleteItemsSoapMessage(uuids), DELETE_ITEM_SOAP_ACTION, storeConfig);
        return true;
    }

    private DeleteItem createDeleteItemsSoapMessage(String[] uuids) {
        DeleteItem msg = new DeleteItem();
        msg.setDeleteType(DisposalType.MOVE_TO_DELETED_ITEMS);

        NonEmptyArrayOfBaseItemIdsType itemList = new NonEmptyArrayOfBaseItemIdsType();
        for (String uuid : uuids) {
            ItemIdType item = new ItemIdType();
            item.setId(uuid);
            itemList.getItemIdsAndOccurrenceItemIdsAndRecurringMasterItemIds().add(item);
        }
        msg.setItemIds(itemList);

        return msg;
    }

    // ----------------------------------------------------------
    // set message read status
    // ----------------------------------------------------------

    @Override
    public boolean setMessageReadStatus(MailStoreConfiguration storeConfig, String[] uuids, boolean read) {
        UpdateItemResponseMessageType soapResponse = (UpdateItemResponseMessageType)
                sendMessageAndExtractSingleResponse(
                        createUpdateItemSoapMessage(uuids, read, storeConfig),
                        UPDATE_ITEM_SOAP_ACTION, storeConfig);
        List<ItemType> updatedItems = soapResponse.getItems().getItemsAndMessagesAndCalendarItems();

        // The changeKeys will have changed, so update them in cache.
        for (ItemType updatedItem : updatedItems) {
            ItemIdType itemId = updatedItem.getItemId();
            insertChangeKeyIntoCache(itemId);
        }
        return true;
    }

    private UpdateItem createUpdateItemSoapMessage(String[] uuids, boolean read, MailStoreConfiguration config) {
        UpdateItem soapMessage = new UpdateItem();

        // Object indicating change field isRead to value of read
        SetItemFieldType change = new SetItemFieldType();
        PathToUnindexedFieldType field = new PathToUnindexedFieldType();
        field.setFieldURI(UnindexedFieldURIType.MESSAGE_IS_READ);
        change.setPath(typeObjectFactory.createFieldURI(field));
        MessageType message = new MessageType();
        message.setIsRead(read);
        change.setMessage(message);

        NonEmptyArrayOfItemChangeDescriptionsType changes = new NonEmptyArrayOfItemChangeDescriptionsType();
        changes.getAppendToItemFieldsAndSetItemFieldsAndDeleteItemFields().add(change);

        NonEmptyArrayOfItemChangesType changeList = new NonEmptyArrayOfItemChangesType();
        for (String uuid : uuids) {
            ItemChangeType itemChange = new ItemChangeType();
            itemChange.setItemId(getItemIdType(uuid, config));
            itemChange.setUpdates(changes);
            changeList.getItemChanges().add(itemChange);
        }
        soapMessage.setItemChanges(changeList);
        soapMessage.setMessageDisposition(MessageDispositionType.SAVE_ONLY);
        soapMessage.setConflictResolution(ConflictResolutionType.ALWAYS_OVERWRITE);
        return soapMessage;
    }

    // ----------------------------------------------------------
    // get inbox folders
    // ----------------------------------------------------------

    @Override
    public List<ExchangeFolderDto> getAllUserInboxFolders(MailStoreConfiguration storeConfig) {

        String key = folderCacheKeyGenerator.getKey(credentialsService.getUsername(), storeConfig.getMailAccount(), folderCacheKeyPrefix);
        Element element = folderCache.get(key);
        if (element != null) {
            return (List<ExchangeFolderDto>) element.getObjectValue();
        }

        log.debug("User {} folders not in cache. Fetching all folders", storeConfig.getMailAccount());
        FindFolderResponseMessageType response = (FindFolderResponseMessageType)
                sendMessageAndExtractSingleResponse(createFindFoldersSoapMessage(), FIND_FOLDER_SOAP_ACTION, storeConfig);
        FindFolderParentType rootFolder = response.getRootFolder();

        List<BaseFolderType> folderList = rootFolder.getFolders().getFoldersAndCalendarFoldersAndContactsFolders();
        List<ExchangeFolderDto> folders = new ArrayList<ExchangeFolderDto>(folderList.size());

        // Create a list of the folders, removing those that match the exclusion patterns or are not BaseFolders
        // (using the class removes Calendar, Contacts, Tasks, and Search Folders).
        for (BaseFolderType baseFolder : folderList) {
            if (baseFolder.getClass().equals(FolderType.class)) {
                if (!matchesExcludedFolders(baseFolder)) {
                    FolderType exchangeFolder = (FolderType) baseFolder;
                    ExchangeFolderDto folder = new ExchangeFolderDto(exchangeFolder.getFolderId().getId(),
                            exchangeFolder.getDisplayName(),
                            exchangeFolder.getTotalCount(), exchangeFolder.getUnreadCount());
                    folders.add(folder);
                }
            }
        }

        folderCache.put(new Element(key, folders));
        return folders;
    }

    private String retrieveFolderId(String folderName, MailStoreConfiguration config) {
        List<ExchangeFolderDto> folders;

        // Retrieve from cache or fetch
        String key = folderCacheKeyGenerator.getKey(credentialsService.getUsername(), config.getMailAccount(), folderCacheKeyPrefix);
        Element element = folderCache.get(key);
        if (element != null) {
            folders = (List<ExchangeFolderDto>) element.getObjectValue();
        } else {
            folders = getAllUserInboxFolders(config);
        }
        for (ExchangeFolderDto folder : folders) {
            if (folderName.equals(folder.getName())) {
                return folder.getId();
            }
        }
        return null;
    }

    private boolean matchesExcludedFolders(BaseFolderType folder) {
        String folderName = folder.getDisplayName();
        for (Pattern pattern : foldernameExclusions) {
            Matcher matcher = pattern.matcher(folderName);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    private FindFolder createFindFoldersSoapMessage() {
        // Construct the SOAP request object to use
        FindFolder msg = new FindFolder();

        // folder = root
        NonEmptyArrayOfBaseFolderIdsType folderList = new NonEmptyArrayOfBaseFolderIdsType();
        DistinguishedFolderIdType inboxFolderId = new DistinguishedFolderIdType();
        inboxFolderId.setId(DistinguishedFolderIdNameType.MSGFOLDERROOT);
        folderList.getFolderIdsAndDistinguishedFolderIds().add(inboxFolderId);
        msg.setParentFolderIds(folderList);

        msg.setTraversal(FolderQueryTraversalType.DEEP);

        FolderResponseShapeType shapeType = new FolderResponseShapeType();
        shapeType.setBaseShape(DefaultShapeNamesType.DEFAULT);
        msg.setFolderShape(shapeType);

        return msg;
    }

    // ----------------------------------------------------------
    // common send message and parse response
    // ----------------------------------------------------------

    private BaseResponseMessageType sendSoapRequest (BaseRequestType soapRequest, String soapAction, MailStoreConfiguration config) {
        String uri = autoDiscoveryDao.getEndpointUri(config);

        try {
            final WebServiceMessageCallback actionCallback = new SoapActionCallback(
                    soapAction);

            final WebServiceMessageCallback customCallback = new WebServiceMessageCallback() {

                @Override
                public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                    actionCallback.doWithMessage(message);
                    SoapMessage soap = (SoapMessage) message;
                    SoapHeaderElement version = soap.getEnvelope().getHeader().addHeaderElement(REQUEST_SERVER_VERSION_QNAME);
                    version.addAttribute(new QName("Version"), "Exchange2007_SP1");
                }

            };

            if (log.isDebugEnabled()) {
                StringResult message = new StringResult();
                try {
                    marshaller.marshal(soapRequest, message);
                    log.trace("Attempting to send SOAP request to {}\nSoap Action: {}\nSoap message body"
                            +" (not exact, log org.apache.http.wire to see actual message):\n{}",
                            uri, soapAction, message);
                } catch (IOException ex) {
                    log.debug("IOException attempting to display soap response", ex);
                }
            }

            // use the request to retrieve data from the Exchange server
            BaseResponseMessageType response =
                    (BaseResponseMessageType) webServiceOperations.marshalSendAndReceive(uri, soapRequest, customCallback);

            if (log.isDebugEnabled()) {
                StringResult messageResponse = new StringResult();
                try {
                    marshaller.marshal(response, messageResponse);
                    log.trace("Soap response body (not exact, log org.apache.http.wire to see actual message):\n{}", messageResponse);
                } catch (IOException ex) {
                    log.debug("IOException attempting to display soap response", ex);
                }
            }

            return response;
        } catch (WebServiceClientException e) {
            // todo should we bother catching/wrapping? I think runtime exceptions should be caught at service layer
            throw new EmailPreviewException(e);
        }
        //todo figure out if we can catch authentication exceptions to return them separate. Useful?
    }

    private ResponseMessageType sendMessageAndExtractSingleResponse(BaseRequestType soapRequest, String soapAction,
                                                                    MailStoreConfiguration config) {
        BaseResponseMessageType soapResponse = sendSoapRequest(soapRequest, soapAction, config);

        boolean warning = false;
        boolean error = false;
        StringBuilder msg = new StringBuilder();
        List<JAXBElement<? extends ResponseMessageType>> responseMessages =
                soapResponse.getResponseMessages().getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages();
        for (JAXBElement<? extends ResponseMessageType> resp : responseMessages) {
            if (ResponseClassType.ERROR.equals(resp.getValue().getResponseClass())) {
                error = true;
                msg.append("Error: ").append(resp.getValue().getResponseCode().value())
                        .append(": ").append(resp.getValue().getMessageText()).append("\n");
            } else if (ResponseClassType.WARNING.equals(resp.getValue().getResponseClass())) {
                warning = true;
                msg.append("Warning: ").append(resp.getValue().getResponseCode().value())
                        .append(": ").append(resp.getValue().getMessageText()).append("\n");
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
        // Currently all message requests return only one value, except a multi-delete or multi-readUpdate
        // in which we only care about having an error or not.
        return responseMessages.get(0).getValue();
    }

    // --------------- changeKey Cache support

    private void insertChangeKeysIntoCache (List<ExchangeEmailMessage> messages) {
        for (ExchangeEmailMessage message : messages) {
            insertChangeKeyIntoCache(message.getMessageId(), message.getExchangeChangeKey());
        }
    }

    private void insertChangeKeyIntoCache(String uuid, String changeKey) {
        String key = idCacheKeyGenerator.getKey(credentialsService.getUsername(), uuid);
        idCache.put(new Element(key, changeKey));
    }

    private void insertChangeKeyIntoCache(ItemIdType itemId) {
        insertChangeKeyIntoCache(itemId.getId(), itemId.getChangeKey());
    }

    private ItemIdType getItemIdType(String uuid, MailStoreConfiguration config) {
        String key = idCacheKeyGenerator.getKey(credentialsService.getUsername(), uuid);
        Element changeKey = idCache.get(key);
        if (changeKey == null) {
            ItemIdType itemId = getMessageChangeKey(uuid, config);
            insertChangeKeyIntoCache(uuid, itemId.getChangeKey());
            return itemId;
        }
        ItemIdType itemIdType = new ItemIdType();
        itemIdType.setId(uuid);
        itemIdType.setChangeKey((String) changeKey.getObjectValue());
        return itemIdType;
    }

}
