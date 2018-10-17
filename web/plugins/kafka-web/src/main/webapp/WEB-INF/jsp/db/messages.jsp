<%@
	include file="../include/include.jspf"
%><fmt:message key="kafkaBrowser" var="pageTitle"/><%@
	include file="../include/header.jspf"
%><c:set var="targetElement" value="explorer-right"
/>
	<script type="text/javascript">/*<![CDATA[*/
	
	function reloadPage() {
		return refreshTopic();
	}
	
	/*]]>*/</script>
	
	<ui:headline1 label="${pageTitle}">
<c:if test="${currentConnection.writable}"
>	<div class="h1-actions">
		<span class="menu" onclick="return showElementMenu(event, 'tools-1');"><fmt:message key="actions"/></span>
	</div>
</c:if
>	<div class="h1-actions">
		<span class="action" title="<fmt:message key="refresh"/>" onclick="return reloadPage();"><fmt:message key="refreshIcon"/></span>
		<a class="action" title="<fmt:message key="newWindow"/>" href="db/${currentConnection.linkName}/files.html" target="_blank"><fmt:message key="newWindowIcon"/></a>
	</div>
	
<c:if test="${currentConnection.writable}"
>	<div id="tools-1" class="hidden"><div class="menucolumn">
		<div class="menuitem"><span onclick="return showDbDialog(event, 'send-message', { topic: '${topic}', partition: '${partition}' }, '<fmt:message key="send"/>');"><fmt:message key="send"/></span></div>
		<div class="menuitem"><span onclick="return showConfirmDialog('<fmt:message key="deleteTopic"/>', '<fmt:message key="confirmDeleteTopic"/>', 'db/${currentConnection.linkName}/delete-topic.html', '${topic}');"><fmt:message key="deleteTopic"/></span></div>
	</div></div>
</c:if
>	</ui:headline1>
	
	<div id="submitresult"></div>
	
	<c:set var="links" value="db/${currentConnection.linkName}/topics.html,db/${currentConnection.linkName}/partitions.html?topic=${topic},db/${currentConnection.linkName}/messages.html?topic=${topic}&amp;partition=${partition}"
	/><c:set var="links" value="${fn:split(links, ',')}"
	/><ui:explorer><ui:multilevel query="${query.name}" levels="${query.subQueries}" params="${params}" links="${links}" items="${tabs}" var="rs" varKey="label" varParams="detailParams" varParamString="detailParamString" name="combo"
		><%@
		include file="result/messages.jspf"
	%></ui:multilevel></ui:explorer>
<%@
	include file="../include/footer.jspf"
%>
