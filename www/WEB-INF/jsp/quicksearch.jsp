<%@ page import = "org.jax.mgi.fewi.util.StyleAlternator" %>
<%@ page import = "org.jax.mgi.fewi.util.FormatHelper" %>
<%@ page import = "mgi.frontend.datamodel.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%@ include file="/WEB-INF/jsp/templates/templateHead.html" %>

<title>MGI Quick Search Results</title>

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<%  // Pull detail object into servlet scope
    // EXAMPLE - Marker foo = (Marker)request.getAttribute("foo");

    StyleAlternator leftTdStyles = new StyleAlternator("detailCat1","detailCat2");
    StyleAlternator rightTdStyles = new StyleAlternator("detailData1","detailData2");
    
%>

<%@ include file="/WEB-INF/jsp/templates/templateBodyStart.html" %>

<!-- header bar -->
<div id="titleBarWrapper" userdoc="QUICK_SEARCH_help.shtml">	
	<span class="titleBarMainTitle">Quick Search Results for ${query}</span>
</div>

<div id="filterButtons">
   <a id="functionFilter" class="filterButton">Molecular Function <img src="${configBean.WEBSHARE_URL}images/filter.png" width="8" height="8" /></a> 
   <a id="processFilter" class="filterButton">Biological Process <img src="${configBean.WEBSHARE_URL}images/filter.png" width="8" height="8" /></a> 
   <a id="componentFilter" class="filterButton">Cellular Component <img src="${configBean.WEBSHARE_URL}images/filter.png" width="8" height="8" /></a> 
</div>
<div id="breadbox">
  <div id="filterSummary">
    <span id="filterList"></span>
  </div>
</div>

<!-- for filter popup (re-used by all filter buttons) -->
<div class="facetFilter">
	<div id="facetDialog">
		<div class="hd">Filter</div>
		<div class="bd">
			<form:form method="GET"
				action="${configBean.FEWI_URL}quicksearch/summary">
				<img src="/fewi/mgi/assets/images/loading.gif">
			</form:form>
		</div>
	</div>
</div>
	
<div id="b1Header" class="qsHeader">Genome Features
  <span id="b1Counts" class="resultCount"></span>
  <span class="helpCursor" onmouseover="return overlib('<div class=detailRowType>This list includes genes, QTL, cytogenetic markers, and other genome features whose name, symbol, synonym, or accession ID matched some or all of your search text.<br/><br/>This list also includes genome features associated with vocabulary terms matching your search text. <br/><br/></div><div class=\'detailRowType\'>See <a href=\'${configBean.USERHELP_URL}QUICK_SEARCH_help.shtml\'>Using the Quick Search Tool</a> for more information and examples.</div>', STICKY, CAPTION, 'Genome Features', HAUTO, BELOW, WIDTH, 375, DELAY, 600, CLOSECLICK, CLOSETEXT, 'Close X')" onmouseout="nd();">
       <img src="${configBean.WEBSHARE_URL}images/blue_info_icon.gif" border="0">
  </span>
</div>
<div id="b1Results"></div>

<div id="b2Header" class="qsHeader">Vocabulary Terms
  <span id="b2Counts" class="resultCount"></span>
  <span class="helpCursor" onmouseover="return overlib('<div class=detailRowType>Use the vocabulary terms listed here <ul><li>to learn MGI\'s official terms</li><li>to focus on detailed research topics</li><li>to explore related research areas</li><li>to investigate alternative areas</li></ul></div><div class=\'detailRowType\'>See <a href=\'${configBean.USERHELP_URL}QUICK_SEARCH_help.shtml\'>Using the Quick Search Tool</a> for more information and examples.</div>', STICKY, CAPTION, 'Vocabulary Terms', HAUTO, BELOW, WIDTH, 375, DELAY, 600, CLOSECLICK, CLOSETEXT, 'Close X')" onmouseout="nd();">
    <img src="${configBean.WEBSHARE_URL}images/blue_info_icon.gif" border="0">
  </span>
</div>
<div id="b2Results"></div>

<div id="b3Header" class="qsHeader">Other Results by ID
  <span id="b3Counts" class="resultCount"></span>
  <span class="helpCursor" onmouseover="return overlib('<div class=detailRowType>This section includes links to sequences, orthology relationships, SNPs and other results whose accession ID matched an item in your search text.</div><div class=\'detailRowType\'>See <a href=\'${configBean.USERHELP_URL}QUICK_SEARCH_help.shtml\'>Using the Quick Search Tool</a> for more information and examples.</div>', STICKY, CAPTION, 'Other Results By ID', HAUTO, BELOW, WIDTH, 375, DELAY, 600, CLOSECLICK, CLOSETEXT, 'Close X')" onmouseout="nd();">
       <img src="http://www.informatics.jax.org/searchtool/blue_info_icon.gif" border="0">
  </span>
</div>
<div id="b3Results"></div>

<p/>
Search MGI with Google<p/>

<style>
.helpCursor { cursor: help; }
.qsHeader { width: 100%; background-color: #F0F8FF; color: #002255; margin-top: 0.75em; 
	font-size: 18px; font-weight: bold; line-height: 1.25; vertical-align: top;
	padding-left: 5px; padding-right: 5px; padding-top: 2px; padding-bottom: 2px; 
	}
.resultCount { font-size: 10px; font-weight: normal; color: #676767; }

#b1Results { max-height: 300px; overflow-y: auto; }
#b2Results { max-height: 300px; overflow-y: auto; }

#b1Table { border-collapse: collapse; width: 100% }
#b1Table th { font-weight: bold; padding: 3px; border: 1px solid black; }
#b1Table td { padding: 3px; border: 1px solid black; }

#b2Table { border-collapse: collapse; width: 100% }
#b2Table th { font-weight: bold; padding: 3px; border: 1px solid black; }
#b2Table td { padding: 3px; border: 1px solid black; }

#b3Table { border-collapse: collapse }
#b3Table th { font-weight: bold; padding: 3px; border: 1px solid black; }
#b3Table td { padding: 3px; border: 1px solid black; }

.facetFilter .yui-panel .bd { width: 285px; }
</style>

<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/filters.js"></script>
<script>
		function getQuerystring() {
	  		return queryString + filters.getUrlFragment();
		}
</script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/fewi_utils.js"></script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/quicksearch/qs_main.js"></script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/quicksearch/qs_bucket1.js"></script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/quicksearch/qs_bucket2.js"></script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/quicksearch/qs_bucket3.js"></script>
<script>
var queryString="${e:forJavaScript(queryString)}";
var query = "${query}";
var fewiurl = "${configBean.FEWI_URL}";

qsMain();

function initializeFilterLibrary(delay) {
	if (window.filtersLoaded) {
		console.log('initializing filters');
		filters.setFewiUrl(fewiurl);
		filters.setQueryStringFunction(getQuerystring);
		filters.setSummaryNames('filterSummary', 'filterList');
		filters.addFilter('goProcessFilter', 'Process', 'processFilter', 'processFilter', fewiurl + 'quicksearch/featureBucket/process');
		filters.addFilter('goFunctionFilter', 'Function', 'functionFilter', 'functionFilter', fewiurl + 'quicksearch/featureBucket/function');
		filters.addFilter('goComponentFilter', 'Component', 'componentFilter', 'componentFilter', fewiurl + 'quicksearch/featureBucket/component');
		filters.registerCallback("filterCallback", qsProcessFilters);
		filters.registerCallback("gaLogCallback", qsLogFilters);
		filters.setRemovalDivStyle('block');
	} else {
		setTimeout(function() { initializeFilterLibrary(delay) }, delay);
	}
}
initializeFilterLibrary(250);	// check for filters.js library being loaded every 250ms
</script>
<%@ include file="/WEB-INF/jsp/templates/templateBodyStop.html" %>

