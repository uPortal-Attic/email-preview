<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
    <script src="<rs:resourceURL value="/rs/jquery/1.8.3/jquery-1.8.3.min.js"/>" type="text/javascript"></script>
</c:if>
<script src="<rs:resourceURL value="/rs/fluid/1.1.3/js/fluid-all-1.1.3.min.js"/>" type="text/javascript"></script>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.min.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="formUrl">
    <portlet:param name="action" value="updateConfiguration"/>
</portlet:actionURL>
<portlet:resourceURL id="parameters" var="parametersUrl" />

<div class="fl-widget portlet" role="section">

    <!-- Portlet Body -->
    <div class="fl-widget-content portlet-body" role="main">

        <form:form action="${ formUrl }" method="POST" commandName="form" htmlEscape="false">
    
            <!-- General Configuration Section -->
            <div class="portlet-section" role="region">
                <h3 class="portlet-section-header" role="heading"><spring:message code="config.preferences.basic"/></h3>
    
                <div class="portlet-section-body">

                    <table>
                        <thead>
                            <tr>
                                <th><spring:message code="config.preferences.prefName"/></th>
                                <th><spring:message code="config.preferences.value"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="protocol"><spring:message code="config.preferences.protocol"/></form:label>
                                </td>
                                <td class="value"><form:input path="protocol"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="exchangeDomain"><spring:message code="config.preferences.exchange.domain"/></form:label>
                                </td>
                                <td class="value"><form:input path="exchangeDomain"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="exchangeAutodiscover"><spring:message code="config.preferences.exchange.autodiscover"/></form:label>
                                </td>
                                <td class="value"><form:checkbox path="exchangeAutodiscover"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="host"><spring:message code="config.preferences.host"/></form:label>
                                </td>
                                <td class="value"><form:input path="host"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="port"><spring:message code="config.preferences.port"/></form:label>
                                </td>
                                <td class="value"><form:input path="port"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="inboxFolderName"><spring:message code="config.preferences.inbox"/></form:label>
                                </td>
                                <td class="value"><form:input path="inboxFolderName"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="timeout"><spring:message code="config.preferences.timeout"/></form:label>
                                </td>
                                <td class="value"><form:input path="timeout"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="connectionTimeout"><spring:message code="config.preferences.connTimeout"/></form:label>
                                </td>
                                <td class="value"><form:input path="connectionTimeout"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="allowRenderingEmailContent"><spring:message code="config.preferences.allowEmailContent"/></form:label>
                                </td>
                                <td class="value"><form:checkbox path="allowRenderingEmailContent"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="usernameSuffix"><spring:message code="config.preferences.suffix"/></form:label>
                                </td>
                                <td class="value"><form:input path="usernameSuffix"/></td>
                            </tr>
                        </tbody>
                    </table>

                </div>
            </div>
            
            <div class="portlet-section" role="region">
                <h3 class="portlet-section-header" role="heading"><spring:message code="config.preferences.linking"/></h3>
                <div class="portlet-section-body">
                    <form:label path="linkServiceKey"><spring:message code="config.preferences.service"/></form:label>
                    <form:select path="linkServiceKey" cssClass="link-service-input">
                        <c:forEach items="${ linkServices }" var="service">
                            <form:option value="${ service.key }"/>
                        </c:forEach>
                    </form:select>
                    
                    <table class="link-service-parameters">
                        <thead>
                            <tr>
                                <th><spring:message code="config.preferences.prefName"/></th>
                                <th><spring:message code="config.preferences.value"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${ serviceParameters.linkParameters }" var="parameter">
                                <c:set var="path" value="additionalProperties['${ parameter.key }'].value"/>
                                <tr class="parameter-row">
                                    <td class="preference-name">
                                        <form:label path="${ path }">${ parameter.label }</form:label>
                                    </td>
                                    <td class="value"><form:input path="${ path }"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    
                </div>
            </div>
    
            <div class="portlet-section" role="region">
                <h3 class="portlet-section-header" role="heading"><spring:message code="config.preferences.authentication"/></h3>
                <p><spring:message code="config.preferences.comment"/></p>
                <div class="portlet-section-body">
	                <c:forEach items="${authServices}" var="auth">
	                    <form:checkbox path="allowableAuthenticationServiceKeys" label="${auth.key}" value="${auth.key}"/>
	                    <table class="auth-service-parameters">
	                        <thead>
	                            <tr>
	                                <th><c:out value="${auth.key}"/> <spring:message code="config.preferences.parameter"/></th>
	                                <th><spring:message code="config.preferences.value"/></th>
	                            </tr>
	                        </thead>
	                        <tbody>
	                            <c:forEach items="${auth.adminConfigurationParameters}" var="parameter">
	                                <c:set var="path" value="additionalProperties['${ parameter.key }'].value"/>
	                                <tr class="parameter-row">
	                                    <td class="preference-name">
	                                        <form:label path="${ path }">${ parameter.label }</form:label>
	                                    </td>
	                                    <td class="value"><form:input path="${ path }"/></td>
	                                </tr>
	                            </c:forEach>
	                        </tbody>
	                    </table>
	                </c:forEach>
                </div>
            </div>

            <div class="buttons">
                <input type="submit" class="button primary" name="save" value="Save"/>
                <input type="submit" class="button secondary" name="cancel" value="Cancel"/>
            </div>
            
        </form:form>
        
    </div>
</div>

<script type="text/javascript">

    var ${n} = {};
    ${n}.jQuery = jQuery<c:if test="${ includeJQuery }">.noConflict(true)</c:if>;
    ${n}.fluid = fluid;
    fluid = null;
    
    ${n}.jQuery(function(){
       var $ = ${n}.jQuery;
       var fluid = ${n}.fluid;
       fluid = null;
       fluid_1_1 = null;

       var getTree = function(parameters) {
       };
                   
       $(document).ready(function(){
           $.get("${ parametersUrl }", 
               { authService: $(".auth-service-input").val(), linkService: $(".link-service-input").val() }, 
               function(data){
//                   console.log(data);
               }, 
               "json"
           );
       }); 
    });

</script>
