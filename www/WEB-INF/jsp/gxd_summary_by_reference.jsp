<%@ page import = "org.jax.mgi.fewi.util.FormatHelper" %>
<%@ page import = "mgi.frontend.datamodel.*" %>
<%@ page import = "org.jax.mgi.fewi.util.StyleAlternator" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<% StyleAlternator stripe  = (StyleAlternator)request.getAttribute("stripe"); %>
${templateBean.templateHeadHtml}

<title>Mouse Gene Expression Data Search</title>

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<style>
.left { float: left; }
.right { float: right; }

#resultSummary {border:0;padding-left:5px; text-align: left; font-size: 12px;}
#resultSummary .selected a,
#resultSummary .selected a:focus,
#resultSummary .selected a:hover{ margin-left: 0px;border:1px solid #808080;border-bottom:solid 1px #eee; color:black; background:none; background-color:#eee;}
#resultSummary .yui-content{background-color:#eee;border:1px solid #808080; border-top: none;}
#resultSummary .yui-nav {border-bottom:solid 1px black;}

#expressionSearch {border:0;padding-left:5px; text-align: left; font-size: 12px;}
#expressionSearch .selected a,
#expressionSearch .selected a:focus,
#expressionSearch .selected a:hover{ margin-left: 0px;border:1px solid #808080;border-bottom:solid 1px #eee; color:black; background:none; background-color:#eee;}
#expressionSearch .yui-content{background-color:#eee;border:1px solid #808080; border-top: none;}
#expressionSearch .yui-nav {border-bottom:solid 1px black;}

table.noborder, table.noborder td , table.noborder th { border: none; }

#gxdQueryForm table tr td table tr td ul li span { font-size: 10px; font-style: italic; }

body.yui-skin-sam .yui-panel .hd,
body.yui-skin-sam .yui-ac-hd { background:none; background-color:#025; color:#fff; font-weight: bold;}
body.yui-skin-sam .yui-ac-hd {padding: 5px;}
body.yui-skin-sam div#outer {overflow:visible;}

.yui-dt table {width: 100%;}

td.yui-dt-col-assayID div.yui-dt-liner span {font-size: 75%;}

.yui-skin-sam .yui-tt .bd
{
	background-color:#ddf; 
	color:#005;
	border:2px solid #005;
}

.yui-skin-sam th.yui-dt-asc .yui-dt-liner { 
	    background:none;
} 
.yui-skin-sam th.yui-dt-desc .yui-dt-liner { 
	    background:none; 
} 

</style>
<!--[if IE]>
<style>

#toggleImg{height:1px;}

/*
body.yui-skin-sam div#outer {position:relative;}
#qwrap, #expressionSearch .yui-navset {position:absolute;}
#toggleQF { overflow:auto; }
*/
body.yui-skin-sam div#outer {position:relative;}
#expressionSearch .yui-navset {position:absolute;}
</style>
<![endif]-->

<!--begin custom header content for this example-->
<script type="text/javascript">
    document.documentElement.className = "yui-pe";
</script>

${templateBean.templateBodyStartHtml}


<!-- iframe for history manager's use -->
<iframe id="yui-history-iframe" src="/fewi/blank.html"></iframe>
<input id="yui-history-field" type="hidden">


<!-- header bar -->
<div id="titleBarWrapper" userdoc="EXPRESSION_help.shtml#summary">	
	<span class="titleBarMainTitle">Gene Expression Data</span>
</div>



<!-- header table -->
<table class="summaryHeader">
<tr >
  <td class="summaryHeaderCat1">
       <b>Reference</b>
  </td>
  <td class="summaryHeaderData1">
    
    <a style="font-size:x-large;  font-weight: bold; padding-bottom:10px;" 
      href="${configBean.FEWI_URL}reference/${reference.jnumID}">
      ${reference.jnumID}
    </a>
    
    <div style="padding:4px;"> </div>
    
    ${reference.shortCitation}
  </td>
</tr>
</table>


<!-- GXD Summary -->
<div class="summaryControl" style="">
	<%@ include file="/WEB-INF/jsp/gxd_summary.jsp" %>
</div>

<script type="text/javascript">
    var fewiurl = "${configBean.FEWI_URL}";
    var mgiMarkerId = "${marker.primaryID}";
    var searchedStage = "${theilerStage}";
    var searchedAssayType = "${assayType}";
    var querystring = "jnum=${reference.jnumID}";
    var assemblyBuild = "${configBean.ASSEMBLY_VERSION}";
</script>

<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/gxd_by_marker_query.js">
</script>
<script type="text/javascript" src="${configBean.FEWI_URL}assets/js/gxd_summary.js"></script>


${templateBean.templateBodyStopHtml}