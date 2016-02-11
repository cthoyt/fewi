<%@ page import = "org.jax.mgi.fewi.util.StyleAlternator" %>
<%@ page import = "org.jax.mgi.fewi.util.FormatHelper" %>
<%@ page import = "mgi.frontend.datamodel.*" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ include file="/WEB-INF/jsp/templates/templateHead.html" %>

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<fewi:simpleseo
	title="SNP Detail ${snp.accid}"
	canonical="${configBean.FEWI_URL}snp/${snp.accid}"
	description="${seoDescription}"
	keywords="${seoKeywords}"
/>

<link rel="stylesheet" type="text/css" href="${configBean.FEWI_URL}assets/css/marker_detail.css" />
<link rel="stylesheet" type="text/css" href="${configBean.FEWI_URL}assets/css/marker_detail_new.css" />
<link rel="stylesheet" type="text/css" href="${configBean.FEWI_URL}assets/css/snp.css" />

<%
	StyleAlternator leftTdStyles = new StyleAlternator("detailCat1","detailCat2");
	StyleAlternator rightTdStyles = new StyleAlternator("detailData1","detailData2"); 
%>

<%@ include file="/WEB-INF/jsp/templates/templateBodyStart.html" %>

<div id="titleBarWrapper" userdoc="SNP_detail_help.shtml" style="max-width: none;">
	<div name="centeredTitle">
		<span class="titleBarMainTitle"><fewi:super value="${snp.accid}"/></span>
		<span class="titleBar_sub">
			SNP Detail
		</span>
	</div>
</div>

<div class="container detailStructureTable">

	<%@ include file="SNPDetail_Summary.jsp" %>
	<%@ include file="SNPDetail_GenomeLocation.jsp" %>
	<%@ include file="SNPDetail_Assays.jsp" %>
	<%@ include file="SNPDetail_Markers.jsp" %>

</div>

<%@ include file="/WEB-INF/jsp/templates/templateBodyStop.html" %>
