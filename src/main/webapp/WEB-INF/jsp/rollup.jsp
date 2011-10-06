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

<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="accountSummaryUrl">
    <portlet:param name="action" value="accountSummary"/>
</portlet:actionURL>
<portlet:actionURL var="showPreviewUrl">
    <portlet:param name="action" value="showPreview"/>
</portlet:actionURL>
<portlet:actionURL var="showPreviewUrlMaximized" windowState="maximized">
    <portlet:param name="action" value="showPreview"/>
</portlet:actionURL>
<c:set var="focusOnPreview" value="${renderRequest.preferences.map['focusOnPreview'][0]}"/>

<!-- email splash styles -->
<style type="text/css">
    .emailSplash {
        height: 84px;
        margin: 0 auto;
        color: #847d76;
        position: relative;
    }
    .emailSplash .graphic,
    .emailSplash .graphic span,
    .emailSplash .text {
        position: absolute;
    }
    .emailSplash .graphic {
        background: url(<c:url value="/images/icon_email.png"/>) no-repeat top left;
        height: 60px;
        width: 58px;
        left: 48px;
        top: 10px;
    }
    .emailSplash .graphic span {
        background: url(<c:url value="/images/circle.png"/>) no-repeat top left;
        line-height: 28px; /* vertical centering */
        padding-left: 9px;
        height: 28px;
        width: 19px;
        right: 0;
        bottom: 0;
        color: #eae8e6;
        display: block;
    }
    .emailSplash .text {
        top: 0px;
        left: 118px;
        line-height: 20px;
    }
    .emailSplash .text h2 {
        font-size: 16px;
        font-family: Georgia, serif;
        font-weight: normal;
        font-style: italic;
        margin: 0;
    }
    .emailSplash .text p {
        font-size: 10px;
        font-family: Verdana, sans-serif;
    }
    .emailSplash .text a {
        color: #00694e;
        font-weight: bold;
    }
    .emailSplash .text a:hover {
        color: #0b4133;
    }
    .emailSplash .inbox {
        float: right;
    }
</style>

<div id="${n}splash" class="emailSplash">
    <div class="graphic">
        <span class="unreadContainer unreadCountCircle" style="display: none;"></span>
    </div>
    <div class="text">
        <h2 style="color: #847d76;"><c:out value="${emailAddress}"/></h2>
        <p class="unreadContainer" style="display: none;"><spring:message code="rollup.summary.preLink"/> <b><span class="unreadCount"></span> <spring:message code="rollup.summary.linkText"/></b> <spring:message code="rollup.summary.postLinkPreTotal"/> <span class="totalCount"></span> <spring:message code="rollup.summary.postTotal"/><br />
        <c:if test="${inboxUrl ne null}">
            &bull; <a href="${inboxUrl}" target="_blank" title="<spring:message code="rollup.summary.inboxLink.tooltip"/>"><spring:message code="rollup.summary.inboxLink"/></a> <spring:message code="rollup.summary.inboxPostLink"/><br />
        </c:if>
        &bull; <a href="<c:out value="${focusOnPreview == 'true' ?  showPreviewUrlMaximized : showPreviewUrl}"/>" title="<spring:message code="rollup.summary.previewLink.tooltip"/>"><spring:message code="rollup.summary.previewLink"/></a> <spring:message code="rollup.summary.previewPostLink"/></p>
    </div>
    <div class="inbox">
        <c:if test="${inboxUrl ne null}">
            <a href="${inboxUrl}" target="_blank" title="<spring:message code="rollup.inbox.linkText.tooltip"/>"><img alt="<spring:message code="rollup.inbox.linkText"/>" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/email.png"/>"/> <spring:message code="rollup.inbox.linkText"/></a><br/>
        </c:if>
        <c:if test="${supportsEdit}">
            <a href="<portlet:renderURL portletMode="EDIT"/>" title="<spring:message code="rollup.inbox.preferences.tooltip"/>"><img alt="Preferences" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/cog_edit.png"/>"/> <spring:message code="rollup.inbox.preferences"/></a><br/>
        </c:if>
        <c:if test="${supportsHelp}">
           <a href="<portlet:renderURL portletMode="HELP"/>" title="<spring:message code="rollup.inbox.help.tooltip"/>"><img alt="Help" src="<rs:resourceURL value="/rs/famfamfam/silk/1.3/help.png"/>"/> <spring:message code="rollup.inbox.help"/></a>
        </c:if>
    </div>
</div>

<div id="${n}error-message" class="error-message portlet-msg-error portlet-msg error" role="alert" style="display:none">
    <p class="error-text"></p>
    <c:if test="${supportsEdit}">
        <p><spring:message code="rollup.errorMessage.changePreferences.preLink"/> <a href="<portlet:renderURL portletMode="EDIT"/>"><spring:message code="rollup.errorMessage.changePreferences.linkText"/></a> <spring:message code="rollup.errorMessage.changePreferences.postLink"/></p>
    </c:if>
</div>

<script type="text/javascript">

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${includeJQuery}">.noConflict(true)</c:if>;

    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;
        
        var jsErrorMessages = {
            <c:forEach items="${jsErrorMessages}" var="entry" varStatus="status">
                '${entry.key}': '<spring:message code="${entry.value}"/>'<c:if test="${!status.last}">,</c:if>
            </c:forEach>        
        };

        var showErrorMessage = function(httpStatus, customMessage) {
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
                }
            },
            error: function(request, textStatus, error) {
                showErrorMessage(request.status);
            }
        });

    });

</script>
