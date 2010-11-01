<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

${templateBean.templateHeadHtml}

<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<title>Cre Allele Summary</title>

<style>
body {z-index=-2;}

.yui-skin-sam .yui-dt th{
  background:url(${configBean.WEBSHARE_URL}images/cre/SpriteYuiOverRide.png)
  repeat-x 0 -1300px;
}
.yui-skin-sam th.yui-dt-asc,.yui-skin-sam th.yui-dt-desc{
  background:url(${configBean.WEBSHARE_URL}images/cre/SpriteYuiOverRide.png)
  repeat-x 0 -1400px;
}

.yui-skin-sam th.yui-dt-sortable .yui-dt-liner{
  background:url(${configBean.WEBSHARE_URL}images/cre/creSortableArrow.png)
  no-repeat right;
}
.yui-skin-sam th.yui-dt-asc .yui-dt-liner{
  background:url(${configBean.WEBSHARE_URL}images/cre/creDownArrow.png)
  no-repeat right;
}
.yui-skin-sam th.yui-dt-desc .yui-dt-liner{
  background:url(${configBean.WEBSHARE_URL}images/cre/creUpArrow.png)
  no-repeat right;
}
.yui-dt a {
  text-decoration: none;
}
.yui-dt img {
  border: none;
}

.pageAdvice {
  font-size: 11px;
  font-style: italic;
  color: #002255;
  padding:2px;
}
.selectText {
  font-size:10px;
}
.smallerCellText{
  font-size:10px;
}
.colSelectContainer{
  width:700px;
  position: relative;
  height:200px;
  border: 1px #AAA solid;
}
.colSelectSubContainer{
  /*border: 1px #999999 solid;*/
}
#summaryResetButton {
font-size: 12px;
font-family: Verdana,Arial,Helvetica;
color: #002255;
font-weight: bolder;
background-color: #eeeeee;
border: 1px #7D95B9 solid;
padding: 2px;
cursor: pointer;
}

table.checkBoxSelectTable{
    border-collapse:collapse;
    border:1px solid #AAA;
    border-spacing:2px;
    padding:2px;
    white-space:nowrap;
    width:auto;
    line-height:1.1;
    line-height:110%;
}
table.checkBoxSelectTable td{
    font-size:11px;
    white-space:nowrap;
}
</style>
<script src="/fewi/js/rowexpansion.js"></script>

<!-- Browser History Manager source file -->
<script src="http://yui.yahooapis.com/2.8.1/build/history/history-min.js"></script>

${templateBean.templateBodyStartHtml}

<iframe id="yui-history-iframe" src="/fewi/js/blank.html"></iframe>
<input id="yui-history-field" type="hidden">


<!-- begin header bar -->
<div id="titleBarWrapper" style="max-width:1200px" userdoc="marker_help.shtml">	
	<!--myTitle -->
	<span class="titleBarMainTitle">Recombinase Alleles - Tissue Specificity Summary</span>
</div>
<!-- end header bar -->

<script type="text/javascript">
YAHOO.namespace ('mgiData');
YAHOO.mgiData.selectedSystem = "";

function getCheckboxID (fieldname) {
	var abbrev = fieldname.substr(0,3).toLowerCase();
	if (abbrev == "adi") { return "adiposeTissueCheckbox"; }
	else if (abbrev == "ali") { return "alimentarySystemCheckbox"; }
	else if (abbrev == "bra") { return "branchialArchesCheckbox"; }
	else if (abbrev == "car") { return "cardiovascularSystemCheckbox"; }
	else if (abbrev == "cav") { return "cavitiesAndLiningsCheckbox"; }
	else if (abbrev == "end") { return "endocrineSystemCheckbox"; }
	else if (abbrev == "hea") { return "headCheckbox"; }
	else if (abbrev == "hem") { return "hemolymphoidSystemCheckbox"; }
	else if (abbrev == "int") { return "integumentalSystemCheckbox"; }
	else if (abbrev == "lim") { return "limbsCheckbox"; }
	else if (abbrev == "liv") { return "liverAndBiliarySystemCheckbox"; }
	else if (abbrev == "mes") { return "mesenchymeCheckbox"; }
	else if (abbrev == "mus") { return "muscleCheckbox"; }
	else if (abbrev == "ner") { return "nervousSystemCheckbox"; }
	else if (abbrev == "ren") { return "renalAndUrinarySystemCheckbox"; }
	else if (abbrev == "rep") { return "reproductiveSystemCheckbox"; }
	else if (abbrev == "res") { return "respiratorySystemCheckbox"; }
	else if (abbrev == "sen") { return "sensoryOrgansCheckbox"; }
	else if (abbrev == "ske") { return "skeletalSystemCheckbox"; }
	else if (abbrev == "tai") { return "tailCheckbox"; }
	else if (abbrev == "ear") { return "earlyEmbryoCheckbox"; }
	else if (abbrev == "ext") { return "extraEmbryonicCheckbox"; }
	else if (abbrev == "emb") { return "embryoOtherCheckbox"; }
	else if (abbrev == "pos") { return "postnatalOtherCheckbox"; }
	else if (abbrev == "all") {
		if (fieldname == "Allele Synonyms") { return "synonymsCheckbox"; }
		else if (fieldname == "Allele Type") { return "alleleTypeCheckbox"; }
	}
	else if (abbrev == "ind") { return "inducibleCheckbox"; }
	else if (abbrev == "ims") { return "imsrCheckbox"; }
	else if (abbrev == "ref") { return "referenceCheckbox"; }
	return "";
}
function getColumnName (fieldname) {
	var abbrev = fieldname.substr(0,3).toLowerCase();
	if (abbrev == "adi") { return "inAdiposeTissue"; }
	else if (abbrev == "ali") { return "inAlimentarySystem"; }
	else if (abbrev == "bra") { return "inBranchialArches"; }
	else if (abbrev == "car") { return "inCardiovascularSystem"; }
	else if (abbrev == "cav") { return "inCavitiesAndLinings"; }
	else if (abbrev == "end") { return "inEndocrineSystem"; }
	else if (abbrev == "hea") { return "inHead"; }
	else if (abbrev == "hem") { return "inHemolymphoidSystem"; }
	else if (abbrev == "int") { return "inIntegumentalSystem"; }
	else if (abbrev == "lim") { return "inLimbs"; }
	else if (abbrev == "liv") { return "inLiverAndBiliarySystem"; }
	else if (abbrev == "mes") { return "inMesenchyme"; }
	else if (abbrev == "mus") { return "inMuscle"; }
	else if (abbrev == "ner") { return "inNervousSystem"; }
	else if (abbrev == "ren") { return "inRenalAndUrinarySystem"; }
	else if (abbrev == "rep") { return "inReproductiveSystem"; }
	else if (abbrev == "res") { return "inRespiratorySystem"; }
	else if (abbrev == "sen") { return "inSensoryOrgans"; }
	else if (abbrev == "ske") { return "inSkeletalSystem"; }
	else if (abbrev == "tai") { return "inTail"; }
	else if (abbrev == "ear") { return "inEarlyEmbryo"; }
	else if (abbrev == "ext") { return "inExtraembryonicComponent"; }
	else if (abbrev == "emb") { return "inEmbryoOther"; }
	else if (abbrev == "pos") { return "inPostnatalOther"; }
	else if (abbrev == "all") {
		if (fieldname == "Allele Synonyms") { return "synonyms"; }
		else if (fieldname == "Allele Type") { return "alleleType"; }
	}
	else if (abbrev == "ind") { return "inducibleNote"; }
	else if (abbrev == "ims") { return "imsrCount"; }
	else if (abbrev == "ref") { return "countOfReferences"; }
	return "";
}
function flipColumn (fieldname) {
	if (fieldname == "") { return; }
	var checkboxID = getCheckboxID (fieldname);
	var thisCheckBox = document.getElementById(checkboxID);
	if (YAHOO.util.Dom.hasClass(thisCheckBox, "checkboxSelected")) {
		hideColumn (fieldname);
	} else {
		showColumn (fieldname);
	}
}
function hideColumn (fieldname) {
	if (fieldname == "") { return; }
	var checkboxID = getCheckboxID (fieldname);
	var thisCheckBox = document.getElementById(checkboxID);
	var columnName = getColumnName (fieldname);
	var myDataTable = YAHOO.mgiData.myDataTable;
//	if (YAHOO.util.Dom.hasClass(thisCheckBox, "checkboxSelected")) {
		myDataTable.hideColumn (columnName);
		YAHOO.util.Dom.removeClass(thisCheckBox, "checkboxSelected");
		thisCheckBox.checked = false;
//	}
}
function showColumn (fieldname) {
	if ((fieldname == "") || (fieldname == null)) { return; }
	var checkboxID = getCheckboxID (fieldname);
	var thisCheckBox = document.getElementById(checkboxID);
	var columnName = getColumnName (fieldname);
	var myDataTable = YAHOO.mgiData.myDataTable;
//	if (!YAHOO.util.Dom.hasClass(thisCheckBox, "checkboxSelected")) {
		myDataTable.showColumn (columnName);
		YAHOO.util.Dom.addClass(thisCheckBox, "checkboxSelected");
		thisCheckBox.checked = true;
//	}
}
function hide (i) {
    var elem = document.getElementById(i);
    if (elem == null) { return false; }

    elem.style.display = 'none';
    return true;
}	
function show (i)
{
    var elem = document.getElementById(i);
    if (elem == null) { return false; }

    elem.style.display = '';
    return true;
}
</script>

<div id="checkboxes">
  <table class="checkBoxSelectTable">
    <tr><td colspan="4" class="pageAdvice">You can control the data displayed below.</td>
      <td colspan="2">&nbsp;</td></tr>
    <tr><td colspan="4" class="pageAdvice">Check the boxes to show Anatomical System
      columns containing links to data and images.</td>
      <td colspan="2" class="pageAdvice">Hide or show other columns.</td></tr>
	<tr>
	  <td><input type="checkbox" id="adiposeTissueCheckbox" 
        onClick="flipColumn('Adipose Tissue');">Adipose&nbsp;Tissue</input>
	  </td>
	  <td><input type="checkbox" id="headCheckbox"
        onClick="flipColumn('Head');">Head</input>
	  </td>
	  <td><input type="checkbox" id="muscleCheckbox"
        onClick="flipColumn('Muscle');">Muscle</input>
	  </td>
	  <td><input type="checkbox" id="skeletalSystemCheckbox"
        onClick="flipColumn('Skeletal System');">Skeletal&nbsp;System</input>
	  </td>
	  <td colspan="2"><input type="checkbox" id="synonymsCheckbox" checked="checked"
		class="checkboxSelected"
        onClick="flipColumn('Allele Synonyms');">Allele&nbsp;Synonyms</input>
	  </td>
	</tr>
	<tr>
	  <td><input type="checkbox" id="alimentarySystemCheckbox"
        onClick="flipColumn('Alimentary System');">Alimentary&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="hemolymphoidSystemCheckbox"
        onClick="flipColumn('Hemolymphoid System');">Hemolymphoid&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="nervousSystemCheckbox"
        onClick="flipColumn('Nervous System');">Nervous&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="tailCheckbox"
        onClick="flipColumn('Tail');">Tail</input>
	  </td>
	  <td><input type="checkbox" id="alleleTypeCheckbox"
        onClick="flipColumn('Allele Type');">Allele&nbsp;Type</input>
	  </td>
	  <td><input type="checkbox" id="imsrCheckbox" checked="checked"
		class="checkboxSelected"
        onClick="flipColumn('IMSR');">IMSR</input>
	  </td>
	</tr>
	<tr>
	  <td><input type="checkbox" id="branchialArchesCheckbox"
        onClick="flipColumn('Branchial Arches');">Branchial&nbsp;Arches</input>
	  </td>
	  <td><input type="checkbox" id="integumentalSystemCheckbox"
        onClick="flipColumn('Integumental System');">Integumental&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="renalAndUrinarySystemCheckbox"
        onClick="flipColumn('Renal and Urinary System');">Renal&nbsp;and&nbsp;Urinary&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="earlyEmbryoCheckbox"
        onClick="flipColumn('Early Embryo');">Early&nbsp;Embryo,&nbsp;All&nbsp;Tissues</input>
	  </td>
	  <td><input type="checkbox" id="inducibleCheckbox"
        onClick="flipColumn('Inducible');">Inducible</input>
	  </td>
	  <td><input type="checkbox" id="referenceCheckbox" checked="checked"
		class="checkboxSelected"
        onClick="flipColumn('References');">References</input>
	  </td>
	</tr>
	<tr>
	  <td><input type="checkbox" id="cardiovascularSystemCheckbox"
        onClick="flipColumn('Cardiovascular System');">Cardiovascular&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="limbsCheckbox"
        onClick="flipColumn('Limbs');">Limbs</input>
	  </td>
	  <td><input type="checkbox" id="reproductiveSystemCheckbox"
        onClick="flipColumn('Reproductive System');">Reproductive&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="extraEmbryonicCheckbox"
        onClick="flipColumn('Extraembryonic Component');">Extraembryonic&nbsp;Component</input>
	  </td>
	  <td colspan="2">&nbsp;</td>
	</tr>
	<tr>
	  <td><input type="checkbox" id="cavitiesAndLiningsCheckbox"
        onClick="flipColumn('Cavities and Linings');">Cavities&nbsp;And&nbsp;Linings</input>
	  </td>
	  <td><input type="checkbox" id="liverAndBiliarySystemCheckbox"
        onClick="flipColumn('Liver and Biliary System');">Liver&nbsp;and&nbsp;Biliary&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="respiratorySystemCheckbox"
        onClick="flipColumn('Respiratory System');">Respiratory&nbsp;System</input>
	  </td>
	  <td colspan="3"><input type="checkbox" id="embryoOtherCheckbox"
        onClick="flipColumn('Embryo Other');">Embryo-other&nbsp;(Embryonic&nbsp;structures&nbsp;not&nbsp;listed&nbsp;above)</input>
	  </td>
	</tr>
	<tr>
	  <td><input type="checkbox" id="endocrineSystemCheckbox"
        onClick="flipColumn('Endocrine System');">Endocrine&nbsp;System</input>
	  </td>
	  <td><input type="checkbox" id="mesenchymeCheckbox"
        onClick="flipColumn('Mesenchyme');">Mesenchyme</input>
	  </td>
	  <td><input type="checkbox" id="sensoryOrgansCheckbox"
        onClick="flipColumn('Sensory Organs');">Sensory&nbsp;Organs</input>
	  </td>
	  <td colspan="2"><input type="checkbox" id="postnatalOtherCheckbox"
        onClick="flipColumn('Postnatal Other');">Postnatal-other&nbsp;(Postnatal&nbsp;structures&nbsp;not&nbsp;listed&nbsp;above)</input>
	  </td>
	  <td><span id="summaryResetButton" onClick="resetCheckboxes(); window.location.reload();">Reset Page</span></td>
	</tr>
  </table>
</div>
<div>
	<div id="querySummary">
		<span class="enhance">You searched for:</span><br/>
		<c:if test="${not empty recombinaseQueryForm.system}"><span class="label">Anatomical System</span> equals 
			<span class="label">${fn:replace(recombinaseQueryForm.system,";", ",") }</span><br/>
			<script type="text/javascript">
			  YAHOO.mgiData.selectedSystem="${recombinaseQueryForm.system}";
			</script></c:if>
		<c:if test="${not empty recombinaseQueryForm.driver}"><span class="label">Driver</span> equals 
			<span class="label">${fn:replace(recombinaseQueryForm.driver,";", ",") }</span><br/></c:if>
    <span class="pageAdvice" style="height: 20px;">
	    Click column headings to sort table data.  Drag headings to rearrange columns.
    </span>
	</div>
</div><br/>
<div id="paginationTop"  style="float:right;"></div><br/>
<div id="dynamicdata"></div>

<script type="text/javascript">
function resetCheckboxes() {
	var hidden = [ "Adipose Tissue", "Alimentary System", "Branchial Arches",
	    "Cardiovascular System", "Cavities and their Linings", "Endocrine System",
	    "Head", "Hemolymphoid System", "Integumental System", "Limbs",
	    "Liver and Biliary System", "Mesenchyme", "Muscle", "Nervous System",
	    "Renal and Urinary System", "Reproductive System", "Respiratory System",
	    "Sensory Organs", "Skeletal System", "Tail", "Early Embryo",
	    "Extraembryonic Component", "Embryo Other", "Postnatal Other", "Allele Type",
	    "Inducible" ];

	var visible = [ "Allele Synonyms", "IMSR", "References" ];

	for (i = 0; i < hidden.length; i++) {
		hideColumn(hidden[i]);
	}
	for (j = 0; j < visible.length; j++) {
		showColumn(visible[i]);
	}
	showColumn(YAHOO.mgiData.selectedSystem);
}
</script>
<script type="text/javascript">
(function () {	
    // Column definitions -- sortable:true enables sorting
    // These are our actual columns, in the default ordering.
    var myColumnDefs = [
        {key:"driver", 
            label:"<B>Driver</B>",
            width:90, 
            sortable:true},
        {key:"nomenclature", 
            label:"<B>Allele Symbol<br/>Gene; Allele Name</B>",
			sortable:true,
			width:245}, 
		{key:"detectedCount", 
			label:"<B>Recombinase<br/>Data</B>", 
			sortable:true, 
			width:220},
	    {key:"inAdiposeTissue", 
			label:"Adipose<br/>Tissue", 
			sortable:true,
			hidden:true,
			width:54},
		{key:"inAlimentarySystem",
			label:"Alimentary<br/>System",
			sortable:true,
			hidden:true,
			width:60},
		{key:"inBranchialArches",
			label:"Branchial<br/>Arches",
			sortable:true,
			hidden:true,
			width:60},
		{key:"inCardiovascularSystem",
			label:"Cardiovascular<br/>System",
			sortable:true,
			hidden:true,
			width:88},
		{key:"inCavitiesAndLinings",
			label:"Cavities &amp;<br/>their Linings",
			sortable:true,
			hidden:true,
			width:72},
		{key:"inEndocrineSystem",
			label:"Endocrine<br/>System",
			sortable:true,
			hidden:true,
			width:64},
		{key:"inHead",
			label:"Head",
			sortable:true,
			hidden:true,
			width:54},
		{key:"inHemolymphoidSystem",
			label:"Hemolymphoid<br/>System",
			sortable:true,
			hidden:true,
			width:88},
		{key:"inIntegumentalSystem",
			label:"Integumental<br/>System",
			sortable:true,
			hidden:true,
			width:82},
		{key:"inLimbs",
			label:"Limbs",
			sortable:true,
			hidden:true,
			width:54},
		{key:"inLiverAndBiliarySystem",
			label:"Liver &amp;<br/>Biliary System",
			sortable:true,
			hidden:true,
			width:84},
		{key:"inMesenchyme",
			label:"Mesenchyme",
			sortable:true,
			hidden:true,
			width:82},
		{key:"inMuscle",
			label:"Muscle",
			sortable:true,
			hidden:true,
			width:54},
		{key:"inNervousSystem",
			label:"Nervous<br/>System",
			sortable:true,
			hidden:true,
			width:60},
		{key:"inRenalAndUrinarySystem",
			label:"Renal &amp;<br/>Urinary System",
			sortable:true,
			hidden:true,
			width:90},
		{key:"inReproductiveSystem",
			label:"Reproductive<br/>System",
			sortable:true,
			hidden:true,
			width:80},
		{key:"inRespiratorySystem",
			label:"Respiratory<br/>System",
			sortable:true,
			hidden:true,
			width:72},
		{key:"inSensoryOrgans",
			label:"Sensory<br/>Organs",
			sortable:true,
			hidden:true,
			width:54},
		{key:"inSkeletalSystem",
			label:"Skeletal<br/>System",
			sortable:true,
			hidden:true,
			width:60},
		{key:"inTail",
			label:"Tail",
			sortable:true,
			hidden:true,
			width:54},
		{key:"inEarlyEmbryo",
			label:"Early<br/>Embryo",
			sortable:true,
			hidden:true,
			width:60},
		{key:"inExtraembryonicComponent",
			label:"Extraembryonic<br/>Component",
			sortable:true,
			hidden:true,
			width:90},
		{key:"inEmbryoOther",
			label:"Embryo<br/>Other",
			sortable:true,
			hidden:true,
			width:60},
		{key:"inPostnatalOther",
			label:"Postnatal<br/>Other",
			sortable:true,
			hidden:true,
			width:60},
        {key:"synonyms",
            label:"<B>Allele Synonym</B>",
            width:170,
            sortable:false},
   		{key:"alleleType", 
            label:"<B>Allele<br/>Type</B>",
            width:60, 
			hidden:true,
            sortable:true},
        {key:"inducibleNote", 
            label:"<B>Inducible</B>",
            width:58, 
			hidden:true,
            sortable:true},
        {key:"imsrCount", 
            label:"<B>Find Mice<br/>(IMSR)</B>",
            width:60, 
            sortable:true},
        {key:"countOfReferences", 
            label:"<B>Refs</B>",
            width:36, 
            sortable:true},
    ];

    // DataSource instance
    var myDataSource = new YAHOO.util.DataSource("json?${queryString}&");

    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    myDataSource.responseSchema = {
        resultsList: "resultObjects",
        fields: [
			{key:"driver"},
			{key:"nomenclature"},
            {key:"detectedCount"},
            {key:"inAdiposeTissue"},
            {key:"inAlimentarySystem"},
            {key:"inBranchialArches"},
            {key:"inCardiovascularSystem"},
            {key:"inCavitiesAndLinings"},
            {key:"inEndocrineSystem"},
            {key:"inHead"},
            {key:"inHemolymphoidSystem"},
            {key:"inIntegumentalSystem"},
            {key:"inLimbs"},
            {key:"inLiverAndBiliarySystem"},
            {key:"inMesenchyme"},
            {key:"inMuscle"},
            {key:"inNervousSystem"},
            {key:"inRenalAndUrinarySystem"},
            {key:"inReproductiveSystem"},
            {key:"inRespiratorySystem"},
            {key:"inSensoryOrgans"},
            {key:"inSkeletalSystem"},
            {key:"inTail"},
            {key:"inEarlyEmbryo"},
            {key:"inExtraembryonicComponent"},
            {key:"inEmbryoOther"},
            {key:"inPostnatalOther"},
            {key:"synonyms"},
            {key:"alleleType"},
            {key:"inducibleNote"},
            {key:"imsrCount"},
            {key:"countOfReferences"},
        ],
        metaFields: {
            totalRecords: "totalCount",
            paginationRecordOffset : "startIndex",
            paginationRowsPerPage : "pageSize",
            sortKey: "sort",
            sortDir: "dir"
        }
    };

    // Create the Paginator
    var myPaginator = new YAHOO.widget.Paginator({
        template : "{PreviousPageLink} <strong>{PageLinks}</strong> {NextPageLink} <span style=align:right;>{RowsPerPageDropdown}</span><br/>{CurrentPageReport}",
        pageReportTemplate : "Showing items {startRecord} - {endRecord} of {totalRecords}",
        rowsPerPageOptions : [10,25,50,100],
        rowsPerPage : 25,
        pageLinks: 5,
        recordOffset: 1
    });

    // DataTable configurations
    var myConfigs = {
        paginator : myPaginator,
        dynamicData : true,
        draggableColumns : true,
        initialLoad : false
    };  
    
    // DataTable instance
    var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, 
    	    myDataSource, myConfigs);
    YAHOO.mgiData.myDataTable = myDataTable;


    // Show loading message while page is being rendered
    myDataTable.showTableMessage(myDataTable.get("MSG_LOADING"), 
    	    YAHOO.widget.DataTable.CLASS_LOADING);    
    
    // Integrate with Browser History Manager
    var History = YAHOO.util.History;

    // Define a custom function to route sorting through the Browser History Manager
    var handleSorting = function (oColumn) {
        // Calculate next sort direction for given Column
        var sDir = this.getColumnSortDir(oColumn);
        
        // The next state will reflect the new sort values
        // while preserving existing pagination rows-per-page
        // As a best practice, a new sort will reset to page 0
        var newState = generateRequest(0, oColumn.key, sDir, 
                this.get("paginator").getRowsPerPage());

        // Pass the state along to the Browser History Manager
        History.navigate("myDataTable", newState);
    };
    myDataTable.sortColumn = handleSorting;

    // Define a custom function to route pagination through the Browser History Manager
    var handlePagination = function(state) {
        // The next state will reflect the new pagination values
        // while preserving existing sort values
        // Note that the sort direction needs to be converted from DataTable format to server value
        var sortedBy  = this.get("sortedBy"),
            newState = generateRequest(
            state.recordOffset, sortedBy.key, sortedBy.dir, state.rowsPerPage
        );
        myPaginator.setState(state);
        // Pass the state along to the Browser History Manager
        History.navigate("myDataTable", newState);
    };
    // First we must unhook the built-in mechanism...
    myPaginator.unsubscribe("changeRequest", myDataTable.onPaginatorChangeRequest);
    // ...then we hook up our custom function
    myPaginator.subscribe("changeRequest", handlePagination, myDataTable, true);

    // Update payload data on the fly for tight integration with latest values from server 
    myDataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
		var pRequest = parseRequest(oRequest);
        var meta = oResponse.meta;

        oPayload.totalRecords = meta.totalRecords || oPayload.totalRecords;
        oPayload.pagination = {
            rowsPerPage: Number(pRequest['results']) || 25,
            recordOffset: Number(pRequest['startIndex']) || 0
        };
        oPayload.sortedBy = {
            key: pRequest['sort'] || "driver",
            dir: pRequest['dir'] ? "yui-dt-" + pRequest['dir'] : "yui-dt-asc" // Convert from server value to DataTable format
        };
        return true;
    };

	// TODO -- check out these methods, as they may be useful for showing a Loading message
	//	during loads of new data
	// myDataTable.doBeforePaginatorChange()
	// myDataTable.doBeforeSortColumn()
	// myDataTable.showTableMessage()
    
    // TODO -- other useful methods
    // myDataTable.hideColumn()
    // myDataTable.showColumn()
    
    // Returns a request string for consumption by the DataSource
    var generateRequest = function(startIndex,sortKey,dir,results) {
        startIndex = startIndex || 0;
        sortKey   = sortKey || "driver";
        dir   = (dir) ? dir.substring(7) : "asc"; // Converts from DataTable format "yui-dt-[dir]" to server value "[dir]"
        results   = results || 25;
        return "results="+results+"&startIndex="+startIndex+"&sort="+sortKey+"&dir="+dir;
    };

    // Called by Browser History Manager to trigger a new state
    var handleHistoryNavigation = function (request) {
        // Sends a new request to the DataSource
        myDataSource.sendRequest(request,{
            success : myDataTable.onDataReturnSetRows,
            failure : myDataTable.onDataReturnSetRows,
            scope : myDataTable,
            argument : {} // Pass in container for population at runtime via doBeforeLoadData
        });
    };

    // Calculate the first request
    var initialRequest = History.getBookmarkedState("myDataTable") || // Passed in via URL
                       generateRequest(); // Get default values

    // Register the module
    History.register("myDataTable", initialRequest, handleHistoryNavigation);

    // Render the first view
    History.onReady(function() {
        // Current state after BHM is initialized is the source of truth for what state to render
        var currentState = History.getCurrentState("myDataTable");
        handleHistoryNavigation(currentState);
    });

    // Initialize the Browser History Manager.
    YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
})();

function parseRequest(request){
	var reply = [];
	var kvPairs = request.split('&');
	for (pair in kvPairs) {
		var kv = kvPairs[pair].split('=');
		reply[kv[0]] = kv[1];
	}
	return reply;
}

showColumn(YAHOO.mgiData.selectedSystem);
</script>

${templateBean.templateBodyStopHtml}
