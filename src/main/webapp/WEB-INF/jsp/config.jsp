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
</c:if>
<link type="text/css" rel="stylesheet" href="<c:url value="/css/email.css"/>"/>

<c:set var="n"><portlet:namespace/></c:set>
<portlet:actionURL var="formUrl"><portlet:param name="action" value="updateConfiguration"/></portlet:actionURL>
<c:url var="parametersUrl" value="/ajax/parameters"><c:param name="action" value="getParameters"/></c:url>

<div class="fl-widget portlet" role="section">

    <!-- Portlet Body -->
    <div class="fl-widget-content portlet-body" role="main">

        <form:form action="${ formUrl }" method="POST" commandName="form">
    
            <!-- General Configuration Section -->
            <div class="portlet-section" role="region">
                <h3 class="portlet-section-header" role="heading">Basic Account Configuration</h3>
    
                <div class="portlet-section-body">

                    <table>
                        <thead>
                            <tr>
                                <th>Preference Name</th>
                                <th>Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="host">Host:</form:label>
                                </td>
                                <td class="value"><form:input path="host"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="port">Port:</form:label>
                                </td>
                                <td class="value"><form:input path="port"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="protocol">Protocol:</form:label>
                                </td>
                                <td class="value"><form:input path="protocol"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="inboxFolderName">Inbox folder name:</form:label>
                                </td>
                                <td class="value"><form:input path="inboxFolderName"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="timeout">Timeout:</form:label>
                                </td>
                                <td class="value"><form:input path="timeout"/></td>
                            </tr>
                            <tr>
                                <td class="preference-name">
                                    <form:label path="connectionTimeout">Connection Timeout:</form:label>
                                </td>
                                <td class="value"><form:input path="connectionTimeout"/></td>
                            </tr>
                        </tbody>
                    </table>

                </div>
            </div>
            
            <div class="portlet-section" role="region">
                <h3 class="portlet-section-header" role="heading">Linking</h3>
                <div class="portlet-section-body">
                    <form:label path="linkServiceKey">Link Service:</form:label>
                    <form:select path="linkServiceKey" cssClass="link-service-input">
                        <c:forEach items="${ linkServices }" var="service">
                            <form:option value="${ service.key }"/>
                        </c:forEach>
                    </form:select>
                    
                    <table class="link-service-parameters">
                        <thead>
                            <tr>
                                <th>Preference Name</th>
                                <th>Value</th>
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
                <h3 class="portlet-section-header" role="heading">Authentication</h3>
                <div class="portlet-section-body">
                    <form:label path="authenticationServiceKey">Authentication Service:</form:label>
                    <form:select path="authenticationServiceKey" cssClass="auth-service-input">
                        <c:forEach items="${ authServices }" var="service">
                            <form:option value="${ service.key }"/>
                        </c:forEach>
                    </form:select>
                    
                    <table class="auth-service-parameters">
                        <thead>
                            <tr>
                                <th>Preference Name</th>
                                <th>Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${ serviceParameters.authParameters }" var="parameter">
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

            <div class="buttons">
                <input type="submit" class="button primary" value="Save"/>
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
