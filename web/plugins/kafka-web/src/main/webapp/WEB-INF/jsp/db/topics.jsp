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
	include file="../include/include.jspf"
%><fmt:message key="fileBrowser" var="pageTitle"/><%@
	include file="../include/header.jspf"
%><c:set var="targetElement" value="explorer-right"
/>
	<script type="text/javascript">/*<![CDATA[*/
	
	function submitForm(frm, p) {
		$('resulttype').value = p;
		getFormInto(frm, 'result');
		return false;
	}
	
	function submitDownloadForm(event, format) {
		return postForm('submitform', event, 'db/${currentConnection.linkName}/submit-simple-export.html', { resultformat: format }, '_blank');
	}
	
	function confirmSubmit(frm, p, title, content) {
		return showJSConfirmDialog(title, content, function() {
			return submitForm(frm, p);
		});
	}
	
	function reloadPage() {
		return refreshDir();
	}
	
	/*]]>*/</script>
	
	<ui:headline1 label="${pageTitle}">
	<div class="h1-actions">
		<span class="action" title="<fmt:message key="refresh"/>" onclick="return reloadPage();">&#x21ba;</span>
		<a class="action" title="<fmt:message key="newWindow"/>" href="db/${currentConnection.linkName}/files.html" target="_blank">&#x2750;</a>
	</div>
	</ui:headline1>
	
	<div id="explorer-left"><ui:combo items="${tabs}" var="rs" varKey="label" varParams="detailParams" varParamString="detailParamString" name="combo"
		><div class="tab-body"><c:forEach items="${rs}" var="row"
		><c:set var="rowid" value="${row.key}"
		/><div class="treerow" id="treerow-${label}-${rowid}"><div class="treebutton"><span class="action" title="<fmt:message key="expand"/>" onclick="return toggleStaticTreeItem(event, '${label}', '${rowid}');">&#x25bc;</span></div>
		<div class="treelabel">${row.key}</div><c:forEach items="${row.value}" var="t"
			><c:set var="rowid" value="${t.topic}-${t.partition}"
			/><div class="treerow" id="treerow-${label}-${rowid}"><div class="treebutton">&#x25b7;</div>
			<div class="treelabel"><a href="db/${currentConnection.linkName}/partition.html?topic=${t.topic}&amp;partition=${t.partition}" onclick="return showPartition(event, '${t.topic}', '${t.partition}');">Partition ${t.partition}</a></div></div>
		</c:forEach></div>
		</c:forEach></div></ui:combo></div>
	
	<div id="explorer-right"></div>
	<hr/>
<%@
	include file="../include/footer.jspf"
%>
