<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>

<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<c:if test="${includeJQuery}">
    <script src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2-v2.min.js"/>" type="text/javascript"></script>
</c:if>
<script src="<rs:resourceURL value="/rs/fluid/1.1.3/js/fluid-all-1.1.3.min.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/batched-pager.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/email-browser.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>
<portlet:resourceURL id="accountSummary" var="accountSummaryUrl" />
<portlet:actionURL var="showRollupUrl" windowState="normal">
    <portlet:param name="action" value="showRollup"/>
</portlet:actionURL>
<portlet:resourceURL id="emailMessage" var="messageUrl" />
<portlet:resourceURL id="deleteMessages" var="deleteUrl" />
<portlet:resourceURL id="toggleSeen" var="toggleSeenUrl" />
<portlet:resourceURL id="updatePageSize" var="updatePageSizeUrl" />

<c:if test="${showConfigLink}">
    <portlet:renderURL var="configUrl" portletMode="CONFIG"/>
    <p style="text-align: right;"><a href="${ configUrl }">Configure portlet</a></p>
</c:if>

<div id="${n}container" class="email-container portlet" xmlns:rsf="http://ponder.org.uk">

    <div class="loading-message"></div>

    <div class="error-message portlet-msg-error portlet-msg error" role="alert" style="display:none">
        <p id="error-text"></p>
        <c:if test="${supportsEdit}">
            <p><spring:message code="preview.errorMessage.changePreferences.preLink"/> <a href="<portlet:renderURL portletMode="EDIT"/>"><spring:message code="preview.errorMessage.changePreferences.linkText"/></a> <spring:message code="preview.errorMessage.changePreferences.postLink"/></p>
        </c:if>
    </div>

    <div class="email-list" style="display:none;">
    
        <form name="inboxForm">
        
            <p>
                <c:choose>
                    <c:when test="${not empty inboxUrl}">
                        <a class="inbox-link email-action-link" href="" target="_blank"><img alt="Refresh" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/email.png"/>"/>&nbsp;<spring:message code="preview.toolbar.inbox"/></a>
                    </c:when>
                    <c:otherwise>
                        <img alt="Refresh" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/email.png"/>"/>&nbsp;<spring:message code="preview.toolbar.inbox"/>
                    </c:otherwise>
                </c:choose>
                (<span class="unread-message-count"></span> <spring:message code="preview.toolbar.unreadMessages"/>)
                | <a class="refresh-link email-action-link" href="javascript:;"><img alt="Refresh" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/arrow_refresh_small.png"/>"/>&nbsp;<spring:message code="preview.toolbar.refresh"/></a>
                <c:if test="${allowDelete}">
                | <a class="delete-link email-action-link" href="javascript:;"><img alt="Delete Selected" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/delete.png"/>"/>&nbsp;<spring:message code="preview.toolbar.deleteSelected"/></a>
                </c:if>
                | <a class="email-action-link" href="${showRollupUrl}"><img alt="Close" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/door_out.png"/>"/>&nbsp;<spring:message code="preview.toolbar.closePreview"/></a>
                <c:if test="${supportsEdit}">
                | <a class="email-action-link" href="<portlet:renderURL portletMode="EDIT"/>"><img alt="Preferences" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/cog_edit.png"/>"/>&nbsp;<spring:message code="preview.toolbar.preferences"/></a>
                </c:if>
                <c:if test="${supportsHelp}">
                | <a class="email-action-link" href="<portlet:renderURL portletMode="HELP"/>"><img alt="Preferences" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/help.png"/>"/> <spring:message code="preview.toolbar.help"/></a>
                </c:if>
            </p>

            <div class="fl-pager">
                
                <div class="flc-pager-top">
                    <ul id="pager-top" class="fl-pager-ui">
                        <li class="flc-pager-previous"><a href="javascript:;">&lt; <spring:message code="preview.pager.previous"/></a></li>
                        <li>
                            <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                                <li class="flc-pager-pageLink"><a href="javascript:;">1</a></li>
                                <li class="flc-pager-pageLink-disabled">2</li>
                                <li class="flc-pager-pageLink-skip">...</li>
                                <li class="flc-pager-pageLink"><a href="javascript:;">3</a></li>
                            </ul>
                        </li>
                        <li class="flc-pager-next"><a href="javascript:;"><spring:message code="preview.pager.next"/> &gt;</a></li>
                        <li>
                            <span class="flc-pager-summary">page</span>
                            <span> <select class="pager-page-size flc-pager-page-size">
                                <option value="5">5</option>
                                <option value="10">10</option>
                                <option value="20">20</option>
                                <option value="50">50</option>
                            </select></span> <spring:message code="preview.pager.perPage"/>
                        </li>
                    </ul>
                </div>
            
                <table cellpadding="3" cellspacing="0" class="email-portlet-table portlet-font">
                    <tr>
                        <th class="select"><input type="checkbox" class="select-all"></th>
                        <th class="flags-header">
                            <span class="flags-span">&nbsp;</span>
                        </th>
                        <th class="flags-header">
                            <span class="attached-span">&nbsp;</span>
                        </th>
                        <th><spring:message code="preview.column.subject"/></th>
                        <th><spring:message code="preview.column.sender"/></th>
                        <th><spring:message code="preview.column.dateSent"/></th>
                    </tr>
                    <tr rsf:id="row:" class="email-row">
                        <td rsf:id="select" class="select"></td>
                        <td rsf:id="flags" class="flags">
                            <span class="answered-span">&nbsp;</span>
                        </td>
                        <td rsf:id="attachments" class="flags">
                            <span class="attached-span">&nbsp;</span>
                        </td>
                        <td rsf:id="subject" class="subject"></td>
                        <td rsf:id="sender" class="sender"></td>
                        <td rsf:id="sentDate" class="sentDate"></td>
                    </tr>
                </table>
            </div>
            
        </form>
        
    </div>
    
    <div class="email-message" style="display:none">
        <table cellpadding="0" cellspacing="0" class="message-headers">
            <tr><td class="message-header-name"><spring:message code="preview.message.from"/></td><td class="sender"></td></tr>
            <tr><td class="message-header-name"><spring:message code="preview.message.subject"/></td><td class="subject"></td></tr>
            <tr><td class="message-header-name"><spring:message code="preview.message.date"/></td><td class="sentDate"></td></tr>
        </table>
        <hr/>
        <div class="message-content">
        </div>
        <form name="messageForm">
            <input class="message-uid" type="hidden" name="selectMessage" value=""/>
            <a class="return-link" style="margin-right: 1.5em;" href="javascript:;"><spring:message code="preview.message.returnToMessages"/></a>
            <c:if test="${allowDelete}">
                <input class="delete-message-button" type="button" value=" <spring:message code="preview.message.delete"/> "/>
            </c:if>
            <c:if test="${supportsToggleSeen}">
                <input class="mark-read-button" type="button" value=" <spring:message code="preview.message.markRead"/> " style="display: none;"/>
                <input class="mark-unread-button" type="button" value=" <spring:message code="preview.message.markUnread"/> " style="display: none;"/>
            </c:if>
        </form>
    </div>    

</div>

<script type="text/javascript">

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${ includeJQuery }">.noConflict(true)</c:if>;
    ${n}.fluid = fluid;
    fluid = null;
    fluid_1_1 = null;

    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var fluid = ${n}.fluid;
       
        // Notify the server of changes to pageSize so they can be remembered
        var updatePageSize = function(newPageSize) {
            $.post("${updatePageSizeUrl}", {newPageSize: newPageSize});
        };

        var jsErrorMessages = {
            <c:forEach items="${jsErrorMessages}" var="entry" varStatus="status">
                '${entry.key}': '<spring:message code="${entry.value}"/>'<c:if test="${!status.last}">,</c:if>
            </c:forEach>        
        };

        var options = {
            accountSummaryUrl: "${accountSummaryUrl}",
            messageUrl: "${messageUrl}",
            messagesInfoContainer: "${messagesInfoContainer}",            
            deleteUrl: "${deleteUrl}",
            toggleSeenUrl: "${toggleSeenUrl}",
            pageSize: <c:out value="${pageSize}"/>,
            listeners: {
                initiatePageSizeChange: updatePageSize
            },
            jsErrorMessages: jsErrorMessages,
            markMessagesAsRead: <c:out value="${markMessagesAsRead ? 'true' : 'false'}"/>
        };
        // Initialize the display asynchronously
        setTimeout(function() {
            jasig.EmailBrowser("#${n}container", options);
        }, 1);

    });

</script>
