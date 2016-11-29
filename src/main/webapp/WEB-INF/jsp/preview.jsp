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
    <script src="<rs:resourceURL value="/rs/jquery/1.11.0/jquery-1.11.0.min.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/jquery-migrate/jquery-migrate-1.2.1.min.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/jqueryui/1.8.24/jquery-ui-1.8.24.min.js"/>" type="text/javascript"></script>
    <script src="<rs:resourceURL value="/rs/fluid/1.4.0/js/fluid-all-1.4.0.min.js"/>" type="text/javascript"></script>
</c:if>
<script src="<c:url value="/js/jquery.quickselect.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/batched-pager.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/email-browser.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email-preview.css"/>"/>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/quickselect.css"/>"/>

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


    <div id="${n}container" class="email-container" xmlns:rsf="http://ponder.org.uk">
        <div class="row">
            <!-- Loading and error messages -->
            <div class="col-sm-12">
                <!-- spinner background on load and on reflow - aka showLoadingMessage() in email-browser.js -->
                <div class="loading-message"><!-- this box is the background div of the initial loading spinner... -->
                   <span class="invisible">&nbsp;</span><!-- invisible span in box... shouldn't be empty because of Chrome and Edge -->
                </div>
                <!-- error message -->
                <div class="alert alert-danger error-message" role="alert" style="display:none">
                    <p id="error-text"></p>
                    <c:if test="${supportsEdit}">
                        <p><spring:message code="preview.errorMessage.changePreferences.preLink"/> <a href="<portlet:renderURL portletMode="EDIT"/>"><spring:message code="preview.errorMessage.changePreferences.linkText"/></a> <spring:message code="preview.errorMessage.changePreferences.postLink"/></p>
                    </c:if>
                </div> <!-- end .alert div -->
            </div>

            <!-- Configure portlet button -->
            <div class="col-sm-12">
                <c:if test="${showConfigLink}">
                    <portlet:renderURL var="configUrl" portletMode="CONFIG"/>
                    <a class="btn btn-primary pull-right" tabindex="0" role="link" href="${ configUrl }"><i class="fa fa-gear" aria-hidden="true"></i> <spring:message code="preview.configure"/></a>
                </c:if>
            </div>
        </div> <!-- end .row div -->

        
        <!-- Email list div -->
        <div id="outer-wrapper" style="width:100%;display:none;">
           <div id="inner-wrapper" style="width:100%">

        <div class="row email-list">
            <form name="inboxForm" class="form-inline">
              <span id="envelope" class="glyphicon glyphicon-inbox" aria-hidden="true"></span>
              <nav id="left-menu" class="off-canvas-menu">
                <div class="navbar navbar-inverse text-align-right">
                   <!-- Inbox button -->
                   <c:if test="${not empty inboxUrl}">
                   <a target="_blank" href="javascript:void(0);" tabindex="0" role="link" class="inbox-link btn btn-inverse btn-xs pull-left">
                      <span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>&nbsp;<spring:message code="preview.toolbar.inbox"/>&nbsp;
                      <span class="badge unread-message-count" aria-label="<spring:message code='preview.toolbar.unreadMessages'/>" title="<spring:message code='preview.toolbar.unreadMessages'/>">10</span>
                   </a>
                   </c:if>

                   <!-- Close preview button -->
                   <a href="${showRollupUrl}" role="link" tabindex="0" class="btn btn-inverse btn-xs pull-left" aria-label="<spring:message code='preview.toolbar.closePreview'/>" title="<spring:message code='preview.toolbar.closePreview'/>">
                      <span class="glyphicon glyphicon-log-out" aria-hidden="true"></span>
                      <span class="sr-only"><spring:message code="preview.toolbar.closePreview"/></span>
                   </a>

                   <!-- Params toggle (NEW)-->
                   <a target="javascript:void(0);" tabindex="0" class="btn btn-inverse btn-xs dropdown" role="link" data-toggle="collapse" data-target="#collapse-preferences" aria-expanded="false" aria-controls="collapse-preferences" aria-label="<spring:message code='preview.toolbar.parameters'/>" title="<spring:message code='preview.toolbar.parameters'/>" tabindex="0">
                      <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                      <span class="sr-only"><spring:message code="preview.toolbar.parameters"/></span>
                   </a>

                   <div class="collapse text-align-left" id="collapse-preferences">
                       <div role="alert" class="alert-quota">
                            <div class="container-fluid stats">
                                <div>

                                  <!-- Quota alert and info -->
                                   <div class="row bd-top bd-bottom" tabindex="0"><strong class="col-xs-8 space-used-label"><spring:message code="preview.toolbar.usedStorage"/></strong><span class="col-xs-4 email-quota-usage space-used-value"></span></div>
                                   <div class="row bd-bottom" tabindex="0"><strong class="col-xs-8 quota-label"><spring:message code="common.quota"/></strong><span class="col-xs-4 email-quota-limit"></span></div>

                                   <!-- Items per page dropdown -->
                                   <div class="row bd-bottom">
                                       <label class="sr-only" for="${n}pager-page-size"><spring:message code="preview.pager.perPage"/></label>
                                       <div class="input-group email-select">
                                          <select id="${n}pager-page-size" class="form-control pager-page-size flc-pager-page-size">
                                             <option value="5">5</option>
                                             <option value="10">10</option>
                                             <option value="20">20</option>
                                             <%-- James W - Removed option for 50 because it has some issues with behavior needing
                                             addressing.  See EMAILPLT-119
                                             <option value="50">50</option> --%>
                                          </select>
                                          <span class="input-group-addon"><spring:message code="preview.pager.perPage"/></span>
                                       </div>
                                    </div>

                                    <!-- Preferences button -->
                                    <c:if test="${supportsEdit}">
                                    <div class="row">
                                        <a tabindex="0" class="btn btn-default preference-button" role="link" href="<portlet:renderURL portletMode="EDIT"/>">
                                           <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="preview.toolbar.preferences"/></a>
                                    </div>
                                    </c:if>
                                  </div>
                              </div>
                          </div>
                     </div>
                 </div>
                 <div style="margin-top:15px;">
                    <div class="form-group" style="width:100%">
                       <label for="allFolders" class="sr-only"><spring:message code="preview.inboxFolder.choose"/></label>
                       <div clas="container-fluid btn-group-vertical">
                          <select id="allFolders" name="allFolders" class="form-control input-sm">
                             <option></option>
                          </select>
                       </div>
                    </div>
                 </div>
              </nav>
              
              <!-- Email list main content (middle) -->
              <div id="content-middle-area" class="navbar navbar-default navbar-middle" role="main" tabindex="0">
                 <div class="container-fluid">

                    <h3 id="selected-folder-title" class="navbar-brand" tabindex="0"><span></span></h3>

                    <!-- left-menu Toggle -->
                    <a href="javascript:void(0);" tabindex="0" role="link" id="left-menu-toggle" class="left-menu-toggle btn btn-default btn-xs pull-left off-canvas-menu-toggle" aria-label="<spring:message code='preview.toolbar.toggle.menu'/>" title ="<spring:message code='preview.toolbar.toggle.menu'/>" aria-flowto="left-menu">
                        <span class="glyphicon glyphicon-menu-hamburger" aria-hidden="true"></span>
                        <span class="sr-only"><spring:message code="preview.toolbar.toggle.menu"/></span>
                    </a>

                    <!-- Refresh button -->
                    <a href="javascript:void(0);" tabindex="0" role="link" class="refresh-link btn btn-success btn-xs pull-left" aria-label="<spring:message code='preview.toolbar.refresh'/>" title="<spring:message code='preview.toolbar.refresh'/>">
                       <span class="glyphicon glyphicon-refresh" aria-hidden="true"></span>
                       <span class="sr-only"><spring:message code="preview.toolbar.refresh"/></span>
                    </a>

                    <!-- Help button -->
                    <c:if test="${supportsHelp}">
                    <a  tabindex="0" class="btn btn-primary btn-xs pull-left" role="link" aria-label="<spring:message code='preview.toolbar.help'/>" title="<spring:message code='preview.toolbar.help'/>" href="<portlet:renderURL portletMode="HELP"/>">
                       <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>
                       <span class="sr-only"><spring:message code="preview.toolbar.help"/></span>
                    </a>
                    </c:if>
                    
                 </div>
              </div> <!-- end navbar-middle -->
              <!-- begin toolbar-middle -->
              <div class="navbar navbar-default toolbar-middle">
                 <div class="container-fluid">
                    
                    <div class="pull-left">
                       <a href="javascript:void(0);" tabindex="0" role="link" class="hide-toolbar-middle btn btn-default btn-xs" aria-label="<spring:message code='preview.toolbar.cancel'/>" title="<spring:message code='preview.toolbar.cancel'/>">
                          <spring:message code="preview.toolbar.cancel"/>
                       </a>
                       <input type="checkbox" class="select-all" aria-label="<spring:message code='preview.toolbar.select.all'/>" title="<spring:message code='preview.toolbar.select.all'/>">
                    </div>
                    <!-- Delete button -->          
                    <c:if test="${allowDelete}">
                    <div class="pull-right">
                       <a href="javascript:void(0);" tabindex="0" role="link" class="delete-link btn btn-danger btn-xs" aria-label="<spring:message code='preview.toolbar.deleteSelected'/>" title="<spring:message code='preview.toolbar.deleteSelected'/>">
                          <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
                          <span class="sr-only"><spring:message code="preview.toolbar.deleteSelected"/></span>
                       </a>
                    </div>
                    </c:if>
                 </div>
              </div><!-- end toolbar-middle -->

              <!-- Pagination, items per page, current folder -->
              <div class="container-list" aria-labelledby="selected-folder-title" aria-describedby="container-middle-description" tabindex="-1">
                <p id="container-middle-description" class="sr-only"><spring:message code='preview.aria.emaillist'/></p>
                <div class="fl-pager email-pager">
                    <!-- Pagination -->
                    <div class="flc-pager-top">
                        
                            <ul id="pager-top" class="pager pagination pagination-sm" style="position:relative;margin:0px;margin-right:-5px;">
                                <li class="flc-pager-previous">
                                    <a href="javascript:void(0);" tabindex="0" role="link" aria-label="<spring:message code='preview.pager.previous'/>" title="<spring:message code='preview.pager.previous'/>">
                                       <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
                                       <span class="sr-only"><spring:message code="preview.pager.previous"/></span>
                                    </a>
                                </li>
                            </ul>
                                    <ul class="fl-pager-links flc-pager-links pager pagination pagination-sm" style="position:relative;margin:0px;">
                                        <li class="flc-pager-pageLink">
                                            <a href="javascript:void(0);" role="link" tabindex="0">1</a>
                                        </li>
                                        <!--<li class="flc-pager-pageLink-disabled" role="link" tabindex="0">2</li>-->
                                        <li class="flc-pager-pageLink-skip"><a href="javascript:void(0);">...</a></li>
                                        <li class="flc-pager-pageLink">
                                            <a href="javascript:void(0);"  role="link" tabindex="0">3</a>
                                        </li>
                                    </ul>
                            <ul class="pager pagination pagination-sm" style="position:relative;margin:0px;margin-left:-5px;">
                                <li class="flc-pager-next">
                                    <a href="javascript:void(0);" tabindex="0" role="link" aria-label="<spring:message code='preview.pager.next'/>" title="<spring:message code='preview.pager.next'/>">
                                       <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                                       <span class="sr-only"><spring:message code="preview.pager.next"/></span>
                                    </a>
                                </li>
                            </ul>
                        
                            <!-- Fluid Pager Summary -->
                            <div>
                               <div class="form-group">
                                <span class="flc-pager-summary" tabindex="0">page</span>
                               </div>
                            </div>
                        
                    </div> <!-- end .flc-pager-top div -->

                    <!-- Email preview table -->
                    <table id="email-list-table" class="list-group" role="list">
                        <tr rsf:id="row:" class="email-row list-group-item" role="listitem">
                           <td class="right-content-email-toggle" role="presentation">
                            <label rsf:id="selectlabel" aria-label="<spring:message code='preview.row.checkbox'/>" class="sr-only checkbox pull-left"><spring:message code='preview.row.checkbox'/></label>
                            <div rsf:id="select" class="select"></div>
                            <span rsf:id="sender" class="sender"></span>
                            <span rsf:id="subject-link" class="subject"></span>
                            <div rsf:id="attachments"><span class="attached-span glyphicon glyphicon-paperclip" aria-hidden="true"></span></div>
                            <span rsf:id="sentDate" class="sentDate"></span>
                            <div rsf:id="flags"><span class="answered-span"></span></div>
                           </td>
                        </tr>
                    </table>
                </div>
              </div>
            </form>
        </div> <!-- end .row .email-list div -->

        <c:if test="${allowRenderingEmailContent}">
            <div id="right-content-email" class="container-fluid off-canvas-menu email-message" role="contentinfo" aria-label="<spring:message code='preview.aria.selectedmessage'/>" tabindex="-1">
              <div class="navbar navbar-default">
                <div class="container-fluid">
                <!-- Email message toolbar -->
                  <div class="row un-row">
                    <div class="pull-left col-xs-5" style="padding:0">
                        <span class="previous-msg" style="display: inline;">
                            <a href="javascript:void(0);" tabindex="0" role="link" class="btn btn-primary btn-xs" aria-label="<spring:message code='preview.pager.message.previous'/>" title="<spring:message code='preview.pager.message.previous'/>">
                                <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
                                <span class="sr-only"><spring:message code="preview.pager.message.previous"/></span>
                            </a>
                        </span>
                        <span class="next-msg">
                            <a href="javascript:void(0);" tabindex="0" role="link" class="btn btn-primary btn-xs" aria-label="<spring:message code='preview.pager.message.next'/>" title="<spring:message code='preview.pager.message.next'/>">
                                <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                                <span class="sr-only"><spring:message code="preview.pager.message.next"/></span>
                            </a>
                        </span>
                    </div>
                    <div class="pull-right col-xs-7" style="padding:0">
                        <form name="messageForm" class="pull-right">
                            <input class="message-uid" type="hidden" name="selectMessage" value=""/>
                            
                            <c:if test="${allowDelete}">
                                <button tabindex="0" class="delete-message-button btn btn-danger btn-xs" type="button" title="<spring:message code='preview.message.delete'/>"><span class="glyphicon glyphicon-trash" aria-label="<spring:message code='preview.message.delete'/>"></span></button>
                            </c:if>
                            <c:if test="${supportsToggleSeen}">
                                <button class="mark-read-button btn btn-success btn-xs" type="button" aria-label="<spring:message code='preview.message.markRead'/>" title="<spring:message code='preview.message.markRead'/>" style="display: none;" tabindex="0"><span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span></button>
                                <button class="mark-unread-button btn btn-warning btn-xs" type="button" aria-label="<spring:message code='preview.message.markUnread'/>" title="<spring:message code='preview.message.markUnread'/>" style="display: none;" tabindex="0"><span class="glyphicon glyphicon-eye-close" aria-hidden="true"></span> </button>
                            </c:if>
                            <a href="javascript:void(0);" tabindex="0" role="link" id="right-content-email-close" class="btn btn-default return-link btn-xs" aria-label="<spring:message code='preview.message.returnToMessages'/>" title="<spring:message code='preview.message.returnToMessages'/>" aria-flowto="content-middle-area" tabindex="0">
                               <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                               <span class="sr-only"><spring:message code="preview.message.returnToMessages"/></span>
                            </a>
                        </form>
                     </div>
                  </div><!-- End Email message toolbar -->

                <!-- Email message header -->
                
                        <table class="table table-condensed message-headers">
                            <caption class="sr-only"><spring:message code='preview.aria.selectedmessage'/></caption>
                            <tbody>
                                <tr>
                                    <th tabindex="0" class="message-header-name"><spring:message code="preview.message.from"/></td>
                                    <td tabindex="0" class="from"></td>
                                </tr>
                                <tr>
                                    <th tabindex="0" class="message-header-name"><spring:message code="preview.message.subject"/></td>
                                    <td tabindex="0" class="subject"></td>
                                </tr>
                                <tr>
                                    <th tabindex="0" class="message-header-name"><spring:message code="preview.message.date"/></td>
                                    <td tabindex="0" class="sentDate"></td>
                               </tr>
                               <tr>
                                    <th tabindex="0" class="message-header-name"><spring:message code="preview.message.to"/></td>
                                    <td tabindex="0" class="toRecipients"></td>
                               </tr>
                               <tr class="ccInfo">
                                    <th tabindex="0" class="message-header-name"><spring:message code="preview.message.cc"/></td>
                                    <td tabindex="0" class="ccRecipients"></td>
                               </tr>
                               <tr class="bccInfo">
                                    <th tabindex="0" class="message-header-name"><spring:message code="preview.message.bcc"/></td>
                                    <td tabindex="0" class="bccRecipients"></td>
                               </tr>
                           </tbody>
                        </table>
                    </div>
                  </div>
                <!-- Email message content -->
                <div>
                    <div>
                        <div class="message-content" tabindex="0"></div>
                        <br />
                        <div class="right-content-email-scroll-up">
                            <a href="#right-content-email" tabindex="0" role="link" class="btn btn-primary btn-xs" aria-label="<spring:message code='preview.message.scroll.top'/>" title="<spring:message code='preview.message.scroll.top'/>">
                                <span class="glyphicon glyphicon-arrow-up" aria-hidden="true"></span>
                                <span class="sr-only"><spring:message code="preview.message.scroll.top"/></span>
                            </a>
                        </div>
                    </div>
                </div>
            </div> <!-- end #right-content-email div -->
        </c:if>
        </div>
      </div>

      <!-- Spinner - it must be at the end of the code -->
       <div class="outer-spinner" role="progressbar" aria-valuetext="<spring:message code='preview.spinner'/>">
          <div class="spinner-container">
             <div class="spinner swoosh"></div>
          </div>
       </div><!-- end Spinner -->

    </div> <!-- End .email-container div -->
</div>

<script type="text/javascript"><rs:compressJs>

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${ includeJQuery }">.noConflict(true)</c:if>;
    ${n}.fluid = fluid;
    fluid = null;
    fluid_1_4 = null;

    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        var fluid = ${n}.fluid;

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
            updatePageSizeUrl: "${updatePageSizeUrl}",
            jsErrorMessages: jsErrorMessages,
            jsMessages: jsMessages,
            allowRenderingEmailContent: <c:out value="${allowRenderingEmailContent ? 'true' : 'false'}"/>,
            markMessagesAsRead: <c:out value="${markMessagesAsRead ? 'true' : 'false'}"/>,
            fluidPagerSummaryOverride:"<spring:message code='preview.fluid.pager.summary.override'/>",
        };
        // Initialize the display asynchronously
        setTimeout(function() {
            jasig.EmailBrowser("#${n}container", options);
        }, 1);

    });

            /*jslint browser: true*/
            /*global $, jQuery, alert*/

             ${n}.jQuery(document).ready(function () {
                "use strict";
                var $ = ${n}.jQuery;

               $('.toolbar-middle').hide();
 
               $('#right-content-email-close').click(function (event) {
                   $('.hide-toolbar-middle').click();
                   $('.email-container').toggleClass('show-right-menu');
                   $('#content-middle-area').focus();
                });

                function showLeftPanel() {
                    if ($('.email-container').hasClass('show-right-menu')) {
                            $('.email-container').removeClass('show-right-menu');
                        }

                        $('.email-container').toggleClass('show-left-menu');
                }

                function openAndClosePanel() {
                    var buttonPanel = $('.left-menu-toggle');
                        for (var i = 0; i < buttonPanel.length ; i++){
                            buttonPanel[i].addEventListener("click", showLeftPanel);
                        }
                }

                openAndClosePanel();
                
            });

            
</rs:compressJs></script>
