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
<portlet:actionURL var="formUrl"><portlet:param name="action" value="updateConfiguration"/></portlet:actionURL>


<form:form action="${ formUrl }" method="POST" commandName="form">
    <p>
        <form:label path="host">Host:</form:label>
        <form:input path="host"/>
    </p>
    <p>
        <form:label path="port">Port:</form:label>
        <form:input path="port"/>
    </p>
    <p>
        <form:label path="protocol">Protocol:</form:label>
        <form:input path="protocol"/>
    </p>
    <p>
        <form:label path="inboxFolderName">Inbox folder name:</form:label>
        <form:input path="inboxFolderName"/>
    </p>
    <p>
        <form:label path="timeout">Timeout:</form:label>
        <form:input path="timeout"/>
    </p>
    <p>
        <form:label path="connectionTimeout">Connection Timeout:</form:label>
        <form:input path="connectionTimeout"/>
    </p>
    <div class="buttons">
        <input type="submit" class="button primary" value="Save"/>
    </div>
    
</form:form>