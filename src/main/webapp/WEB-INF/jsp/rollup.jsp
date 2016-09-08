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
    <script src="<rs:resourceURL value="/rs/jqueryui/1.8.24/i18n/jquery-ui-i18n.min.js"/>" type="text/javascript"></script>
</c:if>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email-preview.css"/>"/>


    <c:set var="n"><portlet:namespace/></c:set>
<portlet:resourceURL id="accountSummary" var="accountSummaryUrl" />
<portlet:actionURL var="showPreviewUrl">
    <portlet:param name="action" value="showPreview"/>
</portlet:actionURL>
<portlet:actionURL var="showPreviewUrlMaximized" windowState="maximized">
    <portlet:param name="action" value="showPreview"/>
</portlet:actionURL>
<c:set var="focusOnPreview" value="${renderRequest.preferences.map['focusOnPreview'][0]}"/>

<div class="container-fluid email-container">
    <div id="${n}splash" class="emailSplash email-preview-rollup">
        <div class="row">
            <div class="jumbotron">
               <div class="container-fluid">
                  <div class="col-xs-12">
                     <h3><c:out value="${emailAddress}"/></h3>
                  </div>
                  <div class="col-xs-12 col-sm-1 col-md-1 col-lg-1">
                      <div class="email-animation">
                        <span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>
                        <span class="unreadContainer unreadCountCircle label label-primary" style="display: none;"></span>
                      </div>
                  </div>
                  <div class="col-xs-12 col-sm-11 col-md-11 col-lg-11">
                      <div>
                          <p class="unreadContainer" style="display: none;">
                             <spring:message code="rollup.summary.preLink"/> <strong><span class="unreadCount"></span> <spring:message code="rollup.summary.linkText"/></strong> <spring:message code="rollup.summary.postLinkPreTotal"/> <span class="totalCount"></span> <spring:message code="rollup.summary.postTotal"/><br />
                             <span class="stats"><spring:message code="common.quota"/>: <span class="email-quota-usage"></span> / <span class="email-quota-limit"></span></span>
                             <ul>
                             <c:if test="${not empty inboxUrl}">
                                <li><a href="${inboxUrl}" target="_blank" title="<spring:message code="rollup.summary.inboxLink.tooltip"/>"><spring:message code="rollup.summary.inboxLink"/></a> <spring:message code="rollup.summary.inboxPostLink"/></li>
                             </c:if>
                                <li><a href="<c:out value="${focusOnPreview == 'true' ?  showPreviewUrlMaximized : showPreviewUrl}"/>" title="<spring:message code="rollup.summary.previewLink.tooltip"/>"><spring:message code="rollup.summary.previewLink"/></a> <spring:message code="rollup.summary.previewPostLink"/></li>
                             </ul>
                          </p>
                      </div>
                  </div>
                  <div class="col-xs-3 col-sm-12">
                     <c:if test="${not empty inboxUrl}">
                        <a href="${inboxUrl}" class="btn btn-default" target="_blank" title="<spring:message code="rollup.inbox.linkText.tooltip"/>">
                            <span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>&nbsp;<spring:message code="rollup.inbox.linkText"/>
                        </a>
                        <div class="col-xs-12 button-spacer">&nbsp;</div>
                     </c:if>
                     <a href="<c:out value="${focusOnPreview == 'true' ?  showPreviewUrlMaximized : showPreviewUrl}"/>" title="<spring:message code="rollup.summary.previewLink.tooltip"/>" class="btn btn-default">
                         <span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>&nbsp;<spring:message code="rollup.summary.previewLink"/>
                     </a>
                     <div class="col-xs-12 button-spacer">&nbsp;</div>
                     <c:if test="${supportsEdit}">
                        <a href="<portlet:renderURL portletMode="EDIT"/>" class="btn btn-default" title="<spring:message code="rollup.inbox.preferences.tooltip"/>">
                           <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<spring:message code="rollup.inbox.preferences"/>
                        </a>
                        <div class="col-xs-12 button-spacer">&nbsp;</div>
                     </c:if>
                     <c:if test="${supportsHelp}">
                        <a href="<portlet:renderURL portletMode="HELP"/>" class="btn btn-primary" title="<spring:message code="rollup.inbox.help.tooltip"/>">
                           <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>&nbsp;<spring:message code="rollup.inbox.help"/>
                        </a>
                     </c:if>
                  </div>
               </div>
            </div>
        </div>
    </div> <!-- end .email-preview-rollup div -->

    <div id="${n}error-message" class="error-message portlet-msg-error portlet-msg error" role="alert" style="display:none">
        <p class="error-text"></p>
        <c:if test="${supportsEdit}">
            <p><spring:message code="rollup.errorMessage.changePreferences.preLink"/> <a href="<portlet:renderURL portletMode="EDIT"/>"><spring:message code="rollup.errorMessage.changePreferences.linkText"/></a> <spring:message code="rollup.errorMessage.changePreferences.postLink"/></p>
        </c:if>
    </div>
</div> <!-- end .container-fluid div -->

<script type="text/javascript"><rs:compressJs>

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${includeJQuery}">.noConflict(true)</c:if>;

    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;

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

        var showErrorMessage = function(httpStatus, customMessage) {
            if (httpStatus == 200) {
                /* We assume 200 AS AN ERROR means the mapge timed out (on uPortal
                 * this event means the ACTION timed out and improperly went to
                 * RENDER, where it should have resulted in a redirect).
                 */
                httpStatus = 504;
            }
            var errorText = jsErrorMessages[httpStatus] || jsErrorMessages['default'];
            if (customMessage) {
                // Add a server-specified custom message to the end
                errorText += '<br/>' + customMessage;
            }
            $("#${n}error-message .error-text").html(httpStatus + ": " + errorText);
            $("#${n}error-message").slideDown(500);
        };

        var account = null;
        $.ajax({
            url: '${accountSummaryUrl}',
            data: { pageStart: 0, numberOfMessages: 20 /* matches batchSize elsewhere to increase cache hits */, forceRefresh: false },
            type: 'POST',
            dataType: "json",
            success: function(data) {
                if (data.errorMessage != null) {
                    showErrorMessage('900', data.errorMessage);
                }
                if (data.accountSummary) {
                    var count = data.accountSummary.unreadMessageCount;
                    $("#${n}splash .unreadCount").text(count);
                    $("#${n}splash .unreadCountCircle").text(count < 100 ? count : "#");
                    $("#${n}splash .totalCount").text(data.accountSummary.totalMessageCount);
                    $("#${n}splash .unreadContainer").slideDown(500);
                    if(data.spaceUsed=="-1"){
                    	$("#${n}splash .stats").remove();
                    }else{
                    	$("#${n}splash .email-quota-usage").text(data.emailQuotaUsage);
                      $("#${n}splash .email-quota-limit").text(data.emailQuotaLimit);
                        
                      if (data.emailQuotaUsage <= 0 || data.emailQuotaLimit <= 0) {
                        $("#${n}splash .stats").hide();
                      } else {
                        $("#${n}splash .stats").show();
                      }
                    }
                }
            },
            error: function(request, textStatus, error) {
                showErrorMessage(request.status);
            }
        });

    });

</rs:compressJs></script>
