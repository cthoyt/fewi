<%@ page import = "org.jax.mgi.fewi.util.FormatHelper" %>
<%@ page import = "mgi.frontend.datamodel.Allele" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

${templateBean.templateHeadHtml}

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<!--begin custom header content for this example-->
<script type="text/javascript">
	document.documentElement.className = "yui-pe";
</script>

<script src="${configBean.FEWI_URL}assets/js/rowexpansion.js"></script>

<title>References</title>

${templateBean.templateBodyStartHtml}

<iframe id="yui-history-iframe" src="${configBean.FEWI_URL}assets/js/blank.html"></iframe>
<input id="yui-history-field" type="hidden">

<!-- begin header bar -->
<div id="titleBarWrapper" userdoc="reference_help.shtml#results_refqf ">	
	<!--myTitle -->
	<span class="titleBarMainTitle">References associated with this Allele</span>
</div>
<!-- end header bar -->

<%@ include file="/WEB-INF/jsp/allele_header.jsp" %>

<div id="summary">

	<div id="breadbox">
		<div id="contentcolumn">
			<div class="innertube">
				<div id="filterSummary" class="filters">
					<span class="label">Filters:</span>
					&nbsp;<span id="defaultText"  style="display:none;">No filters selected. Filter these references below.</span>
					<span id="filterList"></span><br/>
					<span id="fCount" style="display:none;" ><span id="filterCount">0</span> reference(s) match after applying filter(s)</span>
				</div>
			</div>
		</div>
	</div>

	<div id="querySummary">
		<div class="innertube">
			<span id="totalCount" class="count">0</span> reference(s)<br/>
		</div>
	</div>

	<div id="rightcolumn">
		<div class="innertube">
			<div id="paginationTop">&nbsp;</div>
		</div>
	</div>
</div>
	
<%@ include file="/WEB-INF/jsp/reference_summary_toolbar.jsp" %>

<div id="dynamicdata"></div>
<div id="paginationWrap">
	<div id="paginationBottom">&nbsp;</div>
</div>

<div class="facetFilter">
	<div id="facetDialog">
		<div class="hd">Filter</div>	
		<div class="bd">
			<form:form method="GET" action="${configBean.FEWI_URL}reference/summary">		
			</form:form>
		</div>
	</div>
</div>

<script type="text/javascript">
	var fewiurl = "${configBean.FEWI_URL}";
	var querystring = "${queryString}";
	var defaultSort = "${defaultSort}";
</script>


<script type="text/javascript">
	<%@ include file="/js/reference_summary.js" %>
</script>

${templateBean.templateBodyStopHtml}

<c:if test="${not empty typeFilter}">
  <script>
    var messageSpan = document.getElementById('defaultText');
    var savedText = messageSpan.innerHTML;

    messageSpan.innerHTML = 'Retrieving full data set before filtering...';
    messageSpan.style.display = '';

    setTimeout(function() {
      var filterVal = "${typeFilter}";
      facets['typeFilter'] = [ filterVal ];
      var filteredState = generateRequest(myDataTable.getState().sortedBy, 0,
        myDataTable.get('paginator').getRowsPerPage());
      handleHistoryNavigation(filteredState);
      setTimeout(function() {
        messageSpan.innerHTML = savedText;
      }, 1500);
    }, 1500);
  </script>
</c:if>
