<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>

<c:set var="includeJQuery" value="${renderRequest.preferences.map['includeJQuery'][0]}"/>
<c:if test="${includeJQuery}">
    <script src="<rs:resourceURL value="/rs/jquery/1.8.3/jquery-1.8.3.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/jqueryui/1.7.2/jquery-ui-1.7.2-v2.min.js"/>" type="text/javascript"></script>
</c:if>
<script src="<rs:resourceURL value="/rs/fluid/1.1.3/js/fluid-all-1.1.3.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/batched-pager.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/email-browser.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email-preview.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>
<portlet:resourceURL id="accountSummary" var="accountSummaryUrl" />
<portlet:actionURL var="showRollupUrl" windowState="normal">
    <portlet:param name="action" value="showRollup"/>
</portlet:actionURL>
<portlet:resourceURL id="emailMessage" var="messageUrl" />
<portlet:resourceURL id="deleteMessages" var="deleteUrl" />
<portlet:resourceURL id="toggleSeen" var="toggleSeenUrl" />
<portlet:resourceURL id="updatePageSize" var="updatePageSizeUrl" />
<portlet:resourceURL id="inboxFolder" var="inboxFolderUrl" />


    <div id="${n}container" class="email-container container-fluid" xmlns:rsf="http://ponder.org.uk">
        <div class="row">
            <!-- Loading and error messages -->
            <div class="col-sm-10">
                <div class="loading-message" role="alert"></div>
                <div class="alert alert-danger error-message" role="alert">
                    <p id="error-text"></p>
                    <c:if test="${supportsEdit}">
                        <p><spring:message code="preview.errorMessage.changePreferences.preLink"/> <a href="<portlet:renderURL portletMode="EDIT"/>"><spring:message code="preview.errorMessage.changePreferences.linkText"/></a> <spring:message code="preview.errorMessage.changePreferences.postLink"/></p>
                    </c:if>
                </div> <!-- end .alert div -->
            </div>

            <!-- Configure portlet button -->
            <div class="col-sm-2">
                <c:if test="${showConfigLink}">
                    <portlet:renderURL var="configUrl" portletMode="CONFIG"/>
                    <a class="btn btn-primary pull-right" href="${ configUrl }"><i class="fa fa-gear"></i> Configure portlet</a>
                </c:if>
            </div>
        </div> <!-- end .row div -->

        <!-- Email list div -->
        <div class="row email-list" style="display:none;">
            <form name="inboxForm" class="form-inline">
                <!-- Email preview portlet toobar -->
                <div class="row email-preview-portlet-toolbar">
                    <div class="col-sm-9">
                        <div class="btn-toolbar" role="toolbar">
                            <div class="btn-group" role="group">
                                <!-- Inbox button -->
                                <c:if test="${not empty inboxUrl}">
                                    <a class="inbox-link btn btn-primary" href="javascript:;" target="_blank">
                                        <i class="fa fa-envelope"></i>&nbsp;
                                            <spring:message code="preview.toolbar.inbox"/>&nbsp;
                                            <span class="badge unread-message-count">10</span>
                                    </a>
                                </c:if>
                                <!-- Refresh button -->
                                <a class="refresh-link btn btn-success" href="javascript:;">
                                    <i class="fa fa-refresh"></i>&nbsp;
                                    <spring:message code="preview.toolbar.refresh"/>
                                </a>
                                <!-- Delete button -->
                                <c:if test="${allowDelete}">
                                    <a class="delete-link btn btn-danger" href="javascript:;">
                                        <i class="fa fa-trash-o"></i>&nbsp;
                                        <spring:message code="preview.toolbar.deleteSelected"/>
                                    </a>
                                </c:if>
                            </div> <!-- End .btn-group div -->
                            <div class="btn-group" role="group">
                                <!-- Close preview button -->
                                <a class="btn btn-default" href="${showRollupUrl}">
                                    <i class="fa fa-sign-out"></i>&nbsp;
                                    <spring:message code="preview.toolbar.closePreview"/>
                                </a>
                                <!-- Preferences button -->
                                <c:if test="${supportsEdit}">
                                    <a class="btn btn-info" href="<portlet:renderURL portletMode="EDIT"/>">
                                        <i class="fa fa-gears"></i>&nbsp;
                                        <spring:message code="preview.toolbar.preferences"/>
                                    </a>
                                </c:if>
                                <!-- Help button -->
                                <c:if test="${supportsHelp}">
                                    <a class="btn btn-warning" href="<portlet:renderURL portletMode="HELP"/>">
                                        <i class="fa fa-question-circle"></i>&nbsp;
                                        <spring:message code="preview.toolbar.help"/>
                                    </a>
                                </c:if>
                            </div> <!-- end .btn-group div -->
                        </div> <!-- end .btn-toolbar div -->
                    </div> <!-- end .col-sm-9 div -->
                    <div class="col-sm-3">
                        <div class="alert-quota alert alert-success" role="alert">
                            <span class="stats">
                                <i class="fa fa-bar-chart-o"></i>&nbsp;
                                <strong><spring:message code="common.quota"/>: </strong>
                                <span class="email-quota-usage"></span> /
                                <span class="email-quota-limit"></span>
                            </span>
                        </div>
                    </div> <!-- end .col-sm-3 div -->
                </div> <!-- end .row .email-preview-portlet-toolbar div -->

                <!-- Pagination, items per page, current folder -->
                <div class="fl-pager">
                    <!-- Pagination -->
                    <div class="row flc-pager-top">
                        <div class="col-md-6">
                            <ul id="pager-top" class="pager">
                                <li class="flc-pager-previous">
                                    <a href="javascript:;">&lt; <spring:message code="preview.pager.previous"/></a>
                                </li>
                                <li>
                                    <ul class="fl-pager-links flc-pager-links pager">
                                        <li class="flc-pager-pageLink">
                                            <a href="javascript:;">1</a>
                                        </li>
                                        <li class="flc-pager-pageLink-disabled">2</li>
                                        <li class="flc-pager-pageLink-skip">...</li>
                                        <li class="flc-pager-pageLink">
                                            <a href="javascript:;">3</a>
                                        </li>
                                    </ul>
                                </li>
                                <li class="flc-pager-next">
                                    <a href="javascript:;"><spring:message code="preview.pager.next"/> &gt;</a>
                                </li>
                            </ul> <!-- end #pager-top ul -->
                        </div> <!-- end .col-md-6 div -->

                        <!-- Items per page dropdown -->
                        <div class="col-md-3">
                            <div class="form-group">
                                <span class="flc-pager-summary">page</span>
                                <span>
                                    <select class="form-control input-sm pager-page-size flc-pager-page-size">
                                        <option value="5">5</option>
                                        <option value="10">10</option>
                                        <option value="20">20</option>
                                        <%-- James W - Removed option for 50 because it has some issues with behavior needing
                                             addressing.  See EMAILPLT-119
                                        <option value="50">50</option> --%>
                                    </select>
                                </span> <spring:message code="preview.pager.perPage"/>
                            </div>
                        </div> <!-- end .col-md-3 div -->

                        <!-- Current folder dropdown -->
                        <div class="col-md-3">
                            <div class="form-group pull-right">
                                <label for="allFolders"><spring:message code="preview.inboxFolder.choose"/></label>
                                <select id="allFolders" name="allFolders" class="form-control input-sm">
                                    <option></option>
                                </select>
                            </div>
                        </div> <!-- end .col-md-3 div -->
                    </div> <!-- end .flc-pager-top div -->

                    <!-- Email preview table -->
                    <table class="table table-hover table-striped">
                        <tr>
                            <th class="select"><input type="checkbox" class="select-all"></th>
                            <th class="flags-header"><span class="flags-span"></span></th>
                            <th class="flags-header"><span class="attached-span"></span></th>
                            <th><spring:message code="preview.column.subject"/></th>
                            <th><spring:message code="preview.column.sender"/></th>
                            <th><spring:message code="preview.column.dateSent"/></th>
                        </tr>
                        <tr rsf:id="row:" class="email-row">
                            <td rsf:id="select" class="select"></td>
                            <td rsf:id="flags"><span class="answered-span"></span></td>
                            <td rsf:id="attachments"><span class="attached-span"></span></td>
                            <td rsf:id="subject" class="subject"></td>
                            <td rsf:id="sender" class="sender"></td>
                            <td rsf:id="sentDate" class="sentDate"></td>
                        </tr>
                    </table>
                </div>

            </form>
        </div> <!-- end .row .email-list div -->

        <c:if test="${allowRenderingEmailContent}">
            <div class="email-message">
                <!-- Email message toolbar -->
                <div class="row email-message-toolbar">
                    <div class="col-md-6">
                        <span class="previous-msg">
                            <a href="javascript:;" class="btn btn-primary btn-small">
                                <i class="fa fa-arrow-left"></i> <spring:message code="preview.pager.previous"/>
                            </a>
                        </span>
                        <span class="next-msg">
                            <a href="javascript:;" class="btn btn-primary btn-small">
                                <spring:message code="preview.pager.next"/> <i class="fa fa-arrow-right"></i>
                            </a>
                        </span>
                    </div>
                    <div class="col-md-6">
                        <form name="messageForm" class="pull-right">
                            <input class="message-uid" type="hidden" name="selectMessage" value=""/>
                            <a href="javascript:;" class="btn btn-default return-link"><i class="fa fa-undo"></i> <spring:message code="preview.message.returnToMessages"/></a>
                            <c:if test="${allowDelete}">
                                <button class="delete-message-button btn btn-danger" type="button"><i class="fa fa-trash-o"></i> <spring:message code="preview.message.delete"/></button>
                            </c:if>
                            <c:if test="${supportsToggleSeen}">
                                <button class="mark-read-button btn btn-success" type="button" style="display: none;"><i class="fa fa-eye"></i> <spring:message code="preview.message.markRead"/></button>
                                <button class="mark-unread-button btn btn-warning" type="button" style="display: none;"><i class="fa fa-eye-slash"></i> <spring:message code="preview.message.markUnread"/></button>
                            </c:if>
                        </form>
                    </div>
                </div> <!-- end .row .email-message-toolbar div -->

                <!-- Email message header -->
                <div class="row">
                    <div class="col-md-12">
                        <table class="table table-condensed message-headers">
                            <tr>
                                <td class="message-header-name"><spring:message code="preview.message.from"/></td>
                                <td class="from"></td>
                            </tr>
                            <tr>
                                <td class="message-header-name"><spring:message code="preview.message.subject"/></td>
                                <td class="subject"></td>
                            </tr>
                            <tr>
                                <td class="message-header-name"><spring:message code="preview.message.date"/></td>
                                <td class="sentDate"></td>
                            </tr>
                            <tr>
                                <td class="message-header-name"><spring:message code="preview.message.to"/></td>
                                <td class="toRecipients"></td>
                            </tr>
                            <tr class="ccInfo">
                                <td class="message-header-name"><spring:message code="preview.message.cc"/></td>
                                <td class="ccRecipients"></td>
                            </tr>
                            <tr class="bccInfo">
                                <td class="message-header-name"><spring:message code="preview.message.bcc"/></td>
                                <td class="bccRecipients"></td>
                            </tr>
                        </table>
                    </div> <!-- end .email-message .col-md-12 div -->
                </div> <!-- end .row div -->

                <!-- Email message content -->
                <div class="row">
                    <div class="col-md-12">
                        <div class="message-content"></div>
                    </div>
                </div>
            </div> <!-- end .email-content-container div -->
        </c:if>
    </div> <!-- End .email-container div -->
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

        var jsMessages = {
            <c:forEach items="${jsMessages}" var="entry" varStatus="status">
                '${entry.key}': '<spring:message code="${entry.value}"/>'<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        var options = {
            inboxFolderUrl: "${inboxFolderUrl}",
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
            jsMessages: jsMessages,
            allowRenderingEmailContent: <c:out value="${allowRenderingEmailContent ? 'true' : 'false'}"/>,
            markMessagesAsRead: <c:out value="${markMessagesAsRead ? 'true' : 'false'}"/>
        };
        // Initialize the display asynchronously
        setTimeout(function() {
            jasig.EmailBrowser("#${n}container", options);
        }, 1);

    });

</script>
