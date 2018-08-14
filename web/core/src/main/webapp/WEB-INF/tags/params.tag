<%--
 * Copyright 2018 tweerlei Wruck + Buchmeier GbR - http://www.tweerlei.de/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%><%@
	tag description="Custom tag that displays parameter input fields"
%><%@
	attribute name="items" required="true" type="java.util.Collection" rtexprvalue="true" description="ParameterDef objects"
%><%@
	attribute name="path" required="true" type="java.lang.String" rtexprvalue="true" description="Model attribute name"
%><%@
	attribute name="values" required="false" type="java.util.Map" rtexprvalue="true" description="Map: ParameterDef index to default value"
%><%@
	attribute name="fkTables" required="false" type="java.util.Map" rtexprvalue="true" description="Map: ParameterDef index to table QualifiedName"
%><%@
	taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@
	taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@
	taglib prefix="spring" uri="http://www.springframework.org/tags/form"
%><%@
	taglib prefix="ui" tagdir="/WEB-INF/tags"
%><c:forEach items="${items}" var="p" varStatus="st"
>		<dt><spring:label path="${path}[${st.index}]">${p.name}</spring:label></dt>
		<dd><c:choose
			><%--<c:when test="${values[st.index] != null}"><spring:select path="params[${st.index}]" items="${values[st.index]}"/></c:when
			>--%><c:when test="${values != null && values[st.index] != null}"><span class="select"><spring:input path="${path}[${st.index}]"/><span class="action" onclick="return showDbPopup(event, 'select-value', { q: '${values[st.index]}', id: '${path}${st.index}', v: \$F('${path}${st.index}') }, '<fmt:message key="chooseValue"/>');"> &#x25bc;</span></span></c:when
			><c:when test="${fkTables != null && fkTables[st.index] != null}"><span class="select"><spring:input path="${path}[${st.index}]"/><span class="action" onclick="return showDbPopup(event, 'select-fkvalue', { catalog: '${fkTables[st.index].catalogName}', schema: '${fkTables[st.index].schemaName}', object: '${fkTables[st.index].objectName}', id: '${path}${st.index}', v: \$F('${path}${st.index}') }, '<fmt:message key="chooseValue"/>');"> &#x25bc;</span></span></c:when
			><c:when test="${p.type == 'BOOLEAN'}"><spring:checkbox path="${path}[${st.index}]" value="true" label="&nbsp;"/></c:when
			><c:when test="${p.type == 'DATE'}"><spring:input path="${path}[${st.index}]"/><span class="action" onclick="return showDatePicker(event, '${path}${st.index}');"> &#x25bc;</span></c:when
			><c:when test="${p.type == 'TEXT'}"><spring:textarea cssClass="small" path="${path}[${st.index}]" cols="80" rows="5"/></c:when
			><c:otherwise><spring:input path="${path}[${st.index}]"/><fmt:message key="help_${p.type}" var="msg"/><c:if test="${not empty msg}"> <ui:info name="${path}${st.index}">${msg}</ui:info></c:if></c:otherwise
			></c:choose></dd>
</c:forEach
>