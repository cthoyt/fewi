<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ include file="/WEB-INF/jsp/templates/templateHead.html" %>

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<c:set var="titlePrefix" value=""/>
<c:if test="${not empty marker}">
	<c:set var="titlePrefix" value="${marker.symbol} "/>
</c:if>

<fewi:simpleseo
	title="${titlePrefix}Mouse SNP Summary"
	description="${seoDescription}"
	keywords="${seoKeywords}"
/>

<%@ include file="/WEB-INF/jsp/templates/templateBodyStart.html" %>

<link rel="stylesheet" href="${configBean.FEWI_URL}assets/css/snp.css">
<link rel="stylesheet" href="${configBean.FEWI_URL}assets/css/dragtable.css">

<!-- iframe for history managers use -->
<iframe id="yui-history-iframe" src="${configBean.FEWI_URL}assets/blank.html"></iframe>
<input id="yui-history-field" type="hidden">

<!-- begin header bar -->
<c:if test="${empty marker}">
<div id="titleBarWrapper" userdoc="SNP_help.shtml">	
</c:if>
<c:if test="${not empty marker}">
<div id="titleBarWrapper" userdoc="SNP_summary_help.shtml">	
</c:if>
	<!--myTitle -->
	<span class="titleBarMainTitle">Mouse SNP Summary</span>
</div>
<!-- end header bar -->

<c:if test="${empty marker}">
<div id="outer">
	<span id="toggleImg" class="qfExpand"></span>
	<div id="toggleQF"><span id="toggleLink" class="filterButton">Click to modify search</span></div>
	<br>
	<div id="qwrap">
		<%@ include file="/WEB-INF/jsp/snp/form.jsp" %>
	</div>
</div>

<div id="resultbar" class="bluebar">Results</div>
</c:if>
<c:if test="${not empty marker}">
<%@ include file="/WEB-INF/jsp/marker_header.jsp" %><br>
</c:if>

<style>
.facetFilter .yui-panel .bd {
max-height: 30em;
overflow-x: hidden;
overflow-y: auto;
text-align: left;
width: 285px;
}
.fixedWidth {
width: 1250px;
}
</style>
<div id="wrapper" class="fixedWidth">
  <div id="breadbox">
    <div id="contentcolumn">
	<div id="filterDiv">
		<span id="filterLabel" class="label">Filter SNPs by:</span>
		<a id="functionClassFilter" class="filterButton">dbSNP&nbsp;Function&nbsp;Class<img src="${configBean.WEBSHARE_URL}images/filter.png" width="8" height="8" /></a>
	</div>
	<div style="max-width:700px; margin-top: 10px;">
	    <div id="filterSummary" class="filters" style="display: none">
		<span class="label">Filtered by:</span>&nbsp; <span id="defaultText" style="display: none;">No filters selected.</span> <span id="filterList"></span><br/>
	    </div>
	</div>
    </div>
  </div>

  <c:if test="${empty marker}">
    <div id="querySummary">
	<div class="innertube">
	<div style='display: inline-block; width: 470px'>
		<span class='ysf'>You Searched For...</span><br/>
		<c:forEach var="ysfLine" items="${snpQueryForm.youSearchedFor}">
		${ysfLine}<br/>
		</c:forEach>
	    
	</div>
	</div>
    </div>
  </c:if>

  <div id="rightcolumn">
    <div class="innertube">
	<div id="paginationTop" style="float: right">&nbsp;</div>
    </div>
  </div>
</div>

<div id="snpSummaryDiv"><img src="${configBean.FEWI_URL}assets/images/loading.gif" height="24" width="24"> Searching...</div>
<div id="dynamicdata"></div>
<div id="bottomWrapper" class="fixedWidth">
<div id="paginationBottom" style="float: right">&nbsp;</div>
</div>

<div class="facetFilter">
	<div id="facetDialog">
		<div class="hd">Filter</div>
		<div class="bd">
			<form:form method="GET"
				action="${configBean.FEWI_URL}snp/summary">
				<img src="/fewi/mgi/assets/images/loading.gif">
			</form:form>
		</div>
	</div>
</div>

<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/filters.js"></script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/jquery.dragtable.js"></script>

<c:if test="${empty marker}">
<script type="text/javascript">
	// adjust the height of the wrapper div based on the YSF text
	$("#wrapper").height($("#querySummary").height())
</script>
</c:if>

<script type="text/javascript">
	var fewiurl = "${configBean.FEWI_URL}";
	var snpQuerystring = "${queryString}";
	var assemblyVersion = "${assemblyVersion}";
	var buildNumber = "${buildNumber}";
	function getQuerystring() {
		return snpQuerystring + filters.getUrlFragment() + "&hideStrains=" + getHideStrains();
	}
</script>

<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/snp_query.js"></script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/snp_summary.js"></script>

<script type="text/javascript">
	// if coming from QF, toggle it.  (if from marker detail, do not)
	if (document.getElementById('toggleLink') != null) {
		snpqry.hideQF();
	}

	filters.setFewiUrl(fewiurl);
	filters.setQueryStringFunction(getQuerystring);
	filters.setSummaryNames('filterSummary', 'filterList');
	filters.setHistoryManagement('historyModule', handleNavigation);
	filters.addFilter('functionClassFilter', 'dbSNP Function Class', 'functionClassFilter', 'functionClassFilter', fewiurl + 'snp/facet/functionClass');
	filters.registerCallback("pfs", updateRequest);

	// need to wait a half-second before wiring up the checkbox updating functions, making sure to
	// allow time for the tab buttons to be created
	window.setTimeout(function() {
		$("#ui-id-1").click(snpqry.updateQF1);
		$("#ui-id-2").click(snpqry.updateQF2); 
		}, 500);
</script>

<%@ include file="/WEB-INF/jsp/templates/templateBodyStop.html" %>
