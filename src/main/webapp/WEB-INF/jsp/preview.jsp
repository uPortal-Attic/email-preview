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
<c:if test="${ includeJQuery }">
    <script src="<rs:resourceURL value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2-v2.min.js"/>" type="text/javascript"></script>
</c:if>
<script src="<rs:resourceURL value="/rs/fluid/1.1.3/js/fluid-all-1.1.3.min.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/batched-pager.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/email-browser.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="accountInfoUrl">
    <portlet:param name="action" value="accountSummary"/>
</portlet:actionURL>
<portlet:actionURL var="messageUrl">
    <portlet:param name="action" value="emailMessage"/>
</portlet:actionURL>
<portlet:actionURL var="deleteUrl">
    <portlet:param name="action" value="deleteMessages"/>
</portlet:actionURL>

<c:if test="${showConfigLink}">
    <portlet:renderURL var="configUrl" portletMode="CONFIG"/>
    <p style="text-align: right;"><a href="${ configUrl }">Configure portlet</a></p>
</c:if>

<div id="${n}container" class="email-container portlet">

    <div class="loading-message"></div>

    <div class="error-message portlet-msg-error portlet-msg error" role="alert" style="display:none">
        <p id="error-text"></p>
        <c:if test="${supportsEdit}">
            <p>Click <a href="<portlet:renderURL portletMode="EDIT"/>">here</a> to change your mail preferences.</p>
        </c:if>
    </div>

    <div class="email-list" style="display:none;">
    
        <form name="email">
        
            <p>
                <a class="inbox-link" href="" target="_blank"><img alt="Refresh" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/email.png"/>"/> Inbox</a> 
                (<span class="unread-message-count"></span>)
                | <a class="refresh-link email-action-link" href="javascript:;"><img alt="Refresh" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/arrow_refresh_small.png"/>"/> Refresh</a>
                <c:if test="${allowDelete}">
                | <a class="delete-link email-action-link" href="javascript:;"><img alt="Delete Selected" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/delete.png"/>"/> <span>Delete Selected</span></a>
                </c:if>
            </p>
            
            <div class="fl-pager">
                
                <div class="flc-pager-top">
                    <ul id="pager-top" class="fl-pager-ui">
                        <li class="flc-pager-previous"><a href="javascript:;">&lt; prev</a></li>
                        <li>
                            <ul class="fl-pager-links flc-pager-links" style="margin:0; display:inline">
                                <li class="flc-pager-pageLink"><a href="javascript:;">1</a></li>
                                <li class="flc-pager-pageLink-disabled">2</li>
                                <li class="flc-pager-pageLink-skip">...</li>
                                <li class="flc-pager-pageLink"><a href="javascript:;">3</a></li>
                            </ul>
                        </li>
                        <li class="flc-pager-next"><a href="javascript:;">next &gt;</a></li>
                        <li>
                            <span class="flc-pager-summary">page</span>
                            <span> <select class="pager-page-size flc-pager-page-size">
                                <option value="5">5</option>
                                <option value="10">10</option>
                                <option value="20">20</option>
                                <option value="50">50</option>
                            </select></span> per page
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
                        <th>Subject</th>
                        <th>Sender</th>
                        <th>Date Sent</th>
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
            <tr><td class="message-header-name">From</td><td class="sender"></td></tr>
            <tr><td class="message-header-name">Subject</td><td class="subject"></td></tr>
            <tr><td class="message-header-name">Date</td><td class="sentDate"></td></tr>
        </table>
        <hr/>
        <div class="message-content">
        </div>
        <p><a class="return-link" href="javascript:;">Return to messages</a></p>
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

       jasig.EmailBrowser("#${n}container", 
           {
                accountInfoUrl: "${accountInfoUrl}",
                messageUrl: "${messageUrl}",
                deleteUrl: "${deleteUrl}"
           }
       );

    });

</script>
