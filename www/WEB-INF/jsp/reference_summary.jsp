<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

${templateBean.templateHeadHtml}

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<!--begin custom header content for this example-->
<script type="text/javascript">
	document.documentElement.className = "yui-pe";
</script>

<script src="${configBean.FEWI_URL}assets/js/rowexpansion.js"></script>

<title>References</title>

${templateBean.templateBodyStartHtml}

<iframe id="yui-history-iframe" src="${configBean.FEWI_URL}assets/blank.html"></iframe>
<input id="yui-history-field" type="hidden">

<!-- begin header bar -->
<div id="titleBarWrapper" userdoc="reference_help.shtml">	
	<!--myTitle -->
	<span class="titleBarMainTitle">References</span>
</div>
<!-- end header bar -->


<div style="position:relative;width:100%">
	<div id="summDiv">
		<div id="querySummary">
			<span class="title">You searched for:</span><br/>
			<c:if test="${not empty referenceQueryForm.author}">
				<span class="label">Author:</span> 
				${referenceQueryForm.author}<br/></c:if>
			<c:if test="${not empty referenceQueryForm.journal}">
				<span class="label">Journal:</span>
				${referenceQueryForm.journal}<br/></c:if>
			<c:if test="${not empty referenceQueryForm.year}">
				<span class="label">Year:</span> 
				${referenceQueryForm.year}<br/></c:if>
			<c:if test="${not empty referenceQueryForm.text}">
				<span class="label">Text:</span> 
				${referenceQueryForm.text}<br/></c:if>
			<c:if test="${not empty referenceQueryForm.id}">
				<span class="label">ID:</span> 
				${referenceQueryForm.id}<br/></c:if>
			<div id="filterSummary" style="display:none;">
				<span class="label">Filters:</span>
				<span id="fsList"></span>
			</div>	
			<span id="totalCount" class="count"></span>		
		</div>
	</div>	
</div>

<table style="width:100%;">
	<tr>
	<td id="filterDiv" class="filters">
		<a id="authorFilter" class="filterButton">Author Filter <img src="${configBean.FEWI_URL}images/filter.png" width="8" height="8" /></a> 
		<a id="journalFilter" class="filterButton">Journal Filter <img src="${configBean.FEWI_URL}images/filter.png" width="8" height="8" /></a> 
		<a id="yearFilter" class="filterButton">Year Filter <img src="${configBean.FEWI_URL}images/filter.png" width="8" height="8" /></a> 
		<a id="curatedDataFilter" class="filterButton">Data Filter <img src="${configBean.FEWI_URL}images/filter.png" width="8" height="8" /></a>
	</td>
	<td class="paginator">
		<div id="paginationTop">&nbsp;</div>
	</td>
	</tr>
</table>

<div id="dynamicdata"></div>

<div class="facetFilter">
	<div id="facetDialog">
		<div class="hd">Filter</div>	
		<div class="bd">
			<form:form method="GET" action="${configBean.FEWI_URL}reference/summary">
			<img src="/fewi/mgi/assets/images/loading.gif">	
			</form:form>
		</div>
	</div>
</div>

<script type="text/javascript">


</script>


<script type="text/javascript">
	<%@ include file="/js/reference_summary.js" %>
</script>

${templateBean.templateBodyStopHtml}
