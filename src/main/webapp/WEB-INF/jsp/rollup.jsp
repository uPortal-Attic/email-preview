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
<portlet:actionURL var="accountInfoUrl">
    <portlet:param name="action" value="accountSummary"/>
</portlet:actionURL>
<portlet:renderURL var="showPreviewUrl">
    <portlet:param name="action" value="showPreview"/>
</portlet:renderURL>

<!-- email splash styles -->
<style type="text/css">
    .emailSplash {
        height: 94px;
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
        top: 25px;
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
</style>

<div id="${n}error-message" class="error-message portlet-msg-error portlet-msg error" role="alert" style="display:none">
    <p class="error-text"></p>
    <c:if test="${supportsEdit}">
        <p><spring:message code="rollup.errorMessage.changePreferences.preLink"/> <a href="<portlet:renderURL portletMode="EDIT"/>"><spring:message code="rollup.errorMessage.changePreferences.linkText"/></a> <spring:message code="rollup.errorMessage.changePreferences.postLink"/></p>
    </c:if>
</div>

<div id="${n}splash" class="emailSplash">
    <div class="graphic">
        <span class="unreadContainer unreadCountCircle" style="display: none;"></span>
    </div>
    <div class="text">
        <h2 style="color: #847d76;"><c:out value="${emailAddress}"/></h2>
        <p class="unreadContainer" style="display: none;"><spring:message code="rollup.summary.preLink"/> <a href="<c:out value="${showPreviewUrl}"/>"><span class="unreadCount"></span> <spring:message code="rollup.summary.linkText"/></a> <spring:message code="rollup.summary.postLinkPreTotal"/> <span class="totalCount"></span> <spring:message code="rollup.summary.postTotal"/></p>
    </div>
</div>

<script type="text/javascript">

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${includeJQuery}">.noConflict(true)</c:if>;

    ${n}.jQuery(function() {
        var $ = ${n}.jQuery;

        var account = null;
        $.ajax({
            url: '${accountInfoUrl}',
            data: { pageStart: 1, numberOfMessages: 0, forceRefresh: false },
            type: 'POST',
            dataType: "json",
            success: function(data) { 
                if (data.errorMessage != null) {
                    $("#${n}error-message .error-text").text(data.errorMessage);
                    $("#${n}error-message").slideDown(500);
                }
                var count = data.accountInfo.unreadMessageCount;
                $("#${n}splash .unreadCount").text(count);
                $("#${n}splash .unreadCountCircle").text(count < 100 ? count : "#");
                $("#${n}splash .totalCount").text(data.accountInfo.totalMessageCount);
                $("#${n}splash .unreadContainer").slideDown(500);
            },
            error: function(request, textStatus, error) {
                $("#${n}error-message .error-text").text(textStatus);
                $("#${n}error-message").slideDown(500);
            }
        });

    });

</script>
