function log(msg) {
    // log a message to the browser console
    try {
	console.log(msg);
    } catch (c) {
        setTimeout(function() { throw new Error(msg); }, 0);
    }
}

// Form display toggle
//   false = form is displayed
var qDisplay = false;

// The string that gets passed via the AJAX call
var querystring = "";

// HTML/YUI page widgets
YAHOO.namespace("gxd.container");

YAHOO.gxd.container.panelVocab = new YAHOO.widget.Panel("gxdVocabHelp", { width:"520px", draggable:false, visible:false, constraintoviewport:true } );
YAHOO.gxd.container.panelVocab.render();
YAHOO.util.Event.addListener("gxdVocabHelpImage", "mouseover", YAHOO.gxd.container.panelVocab.show, YAHOO.gxd.container.panelVocab, true);

YAHOO.gxd.container.panelStructure = new YAHOO.widget.Panel("gxdStructureHelp", { width:"320px", draggable:false, visible:false, constraintoviewport:true } );
YAHOO.gxd.container.panelStructure.render();
YAHOO.util.Event.addListener("gxdStructureHelpImage", "mouseover", YAHOO.gxd.container.panelStructure.show, YAHOO.gxd.container.panelStructure, true);

YAHOO.gxd.container.panelDifStruct1 = new YAHOO.widget.Panel("gxdDifStruct1Help", { width:"320px", draggable:false, visible:false, constraintoviewport:true,close:false } );
YAHOO.gxd.container.panelDifStruct1.render();
YAHOO.util.Event.addListener("gxdDifStruct1HelpImage", "mouseover", YAHOO.gxd.container.panelDifStruct1.show, YAHOO.gxd.container.panelDifStruct1, true);
YAHOO.util.Event.addListener("gxdDifStruct1HelpImage", "mouseout", YAHOO.gxd.container.panelDifStruct1.hide, YAHOO.gxd.container.panelDifStruct1, true);
YAHOO.gxd.container.panelDifStage = new YAHOO.widget.Panel("gxdDifStageHelp", { width:"320px", draggable:false, visible:false, constraintoviewport:true,close:false } );
YAHOO.gxd.container.panelDifStage.render();
YAHOO.util.Event.addListener("gxdDifStageHelpImage", "mouseover", YAHOO.gxd.container.panelDifStage.show, YAHOO.gxd.container.panelDifStage, true);
YAHOO.util.Event.addListener("gxdDifStageHelpImage", "mouseout", YAHOO.gxd.container.panelDifStage.hide, YAHOO.gxd.container.panelDifStage, true);
YAHOO.gxd.container.panelDifBoth = new YAHOO.widget.Panel("gxdDifStructStageHelp", { width:"320px", draggable:false, visible:false, constraintoviewport:true,close:false } );
YAHOO.gxd.container.panelDifBoth.render();
YAHOO.util.Event.addListener("gxdDifStructStageHelpImage", "mouseover", YAHOO.gxd.container.panelDifBoth.show, YAHOO.gxd.container.panelDifBoth, true);
YAHOO.util.Event.addListener("gxdDifStructStageHelpImage", "mouseout", YAHOO.gxd.container.panelDifBoth.hide, YAHOO.gxd.container.panelDifBoth, true);

//GXD tooltips
var tsTooltips = {
		1:"One cell stage",
		2:"Beginning of cell division; 2-4 cells",
		3:"Morula; 4-16 cells",
		4:"Blastocyst (inner cell mass apparent); 16-40 cells",
		5:"Blastocyst (zona free)",
		6:"Implantation",
		7:"Formation of egg cylinder",
		8:"Differentiation of egg cylinder",
		9:"Prestreak; early streak",
		10:"Midstreak; late streak; allantoic bud first appears; amnion forms",
		11:"Neural plate stage; elongated allantoic bud; early headfold; late headfold",
		12:"1-7 somites",
		13:"8-12 somites; turning of embryo",
		14:"13-20 somites; formation and closure of anterior neuropore",
		15:"21-29 somites; formation of posterior neuropore and forelimb bud",
		16:"30-34 somites; closure of posterior neuropore; formation of hindlimb and tail bud",
		17:"35-39 somites; deep indentation of lens vesicle",
		18:"40-44 somites; closure of lens vesicle",
		19:"45-47 somites; complete separation of lens vesicle",
		20:"48-51 somites; earliest sign of handplate digits",
		21:"52-55 somites; indentation of handplate",
		22:"56-~60 somites; distal separation of handplate digits",
		23:"Separation of footplate digits",
		24:"Reposition of umbilical hernia",
		25:"Digits joined together; skin wrinkled",
		26:"Long whiskers",
		27:"Newborn mouse",
		28:"Postnatal development"
		};

// only include those with special handling (to be added before right paren)
var tsTooltipTitles = {
	27:"; P0-P3",
	28:"; P4-adult"
};

var tsBoxIDs = ["theilerStage","difTheilerStage1","difTheilerStage2",
    "difTheilerStage3","difTheilerStage4"];
for(var j=0;j<tsBoxIDs.length;j++)
{
	var tsBox = YAHOO.util.Dom.get(tsBoxIDs[j]);
	if(tsBox!=null)
	{
		for(var i=0; i< tsBox.children.length; i++)
		{
			var option = tsBox.children[i];
			// check if we've defined the tooltip for this option
			if(tsTooltips[option.value])
			{
				var ttTitle = option.text;
				if (tsTooltipTitles[option.value]) {
				    ttTitle = ttTitle.replace(")",
					tsTooltipTitles[option.value] + ")");
				}
				var ttText = "<b>" + ttTitle + "</b>"+
					"<br/>"+tsTooltips[option.value];
				var tt = new YAHOO.widget.Tooltip("tsTT_"+j+"_"+i,{context:option, text:ttText,showdelay:1000});
			}
		}
	}
}

var QFHeight = 704;
var DifQFHeight = 150;
var BatchQFHeight = 230;
var currentQF = "standard";
var currentDifQF = "structure";
// GXD form tab control
YAHOO.widget.Tab.prototype.ACTIVE_TITLE = '';
var formTabs = new YAHOO.widget.TabView('expressionSearch');

formTabs.addListener("activeTabChange", function(e){
	if(formTabs.get('activeIndex')==0) currentQF = "standard";
	else if(formTabs.get('activeIndex')==2) currentQF = "batch";
	else currentQF = "differential";
});
//basic functions to manage the form tabs
var showStandardForm = function()
{
	currentQF = "standard";
	formTabs.selectTab(0);
};
var showDifferentialForm = function()
{
	currentQF = "differential";
	formTabs.selectTab(1);
};
var showBatchSearchForm = function()
{
	currentQF = "batch";
	formTabs.selectTab(2);
};

//set up toggle for differential ribbons
function showDifStructuresQF()
{
	currentDifQF = "structure";
	showDifferentialForm();
	$("#difStructClosed").hide();
	$("#difStructOpen").show();
	$("#difStageClosed").show();
	$("#difStageOpen").hide();
	$("#difStructStageClosed").show();
	$("#difStructStageOpen").hide();
}
function showDifStagesQF()
{
	currentDifQF = "stage";
	showDifferentialForm();
	$("#difStructClosed").show();
	$("#difStructOpen").hide();
	$("#difStageClosed").hide();
	$("#difStageOpen").show();
	$("#difStructStageClosed").show();
	$("#difStructStageOpen").hide();
}
function showDifBothQF()
{
	currentDifQF = "both";
	showDifferentialForm();
	$("#difStructClosed").show();
	$("#difStructOpen").hide();
	$("#difStageClosed").show();
	$("#difStageOpen").hide();
	$("#difStructStageClosed").hide();
	$("#difStructStageOpen").show();
}
// attach the click handlers for ribbon toggle
$("#difStructClosed").click(showDifStructuresQF);
$("#difStageClosed").click(showDifStagesQF);
$("#difStructStageClosed").click(showDifBothQF);

function getCurrentQF()
{
	if(currentQF=="differential")
	{
		if(currentDifQF=="stage") return YAHOO.util.Dom.get("gxdDifferentialQueryForm2");
		else if(currentDifQF=="both") return YAHOO.util.Dom.get("gxdDifferentialQueryForm3");
		return YAHOO.util.Dom.get("gxdDifferentialQueryForm1");
	} else if (currentQF == 'batch') {
		return YAHOO.util.Dom.get("gxdBatchQueryForm1");
	}

	return YAHOO.util.Dom.get("gxdQueryForm");
}

//GXD age/stage tab control
// COMMENTING OUT THE YUI2 way of doing this, because it breaks in IE on windows 7
//YAHOO.widget.Tab.prototype.ACTIVE_TITLE = '';
//var Tabs = new YAHOO.widget.TabView('ageStage');

// general purpose function for changing tabs
function changeTab(tabElement,parentId)
{
    var eSelector = '#'+parentId;
     // remove the active-tab and place it on current object;
    $(eSelector+' .active-tab').removeClass("active-tab").
		addClass("inactive-tab");
    $(tabElement).removeClass("inactive-tab")
		.addClass("active-tab");

    // remove active content
    $(eSelector+' .active-content').removeClass("active-content")
        .addClass("inactive-content");

    // use tab index to find matching content and set it to active
    var tab_index = $(tabElement).index();
    $(eSelector+' .inactive-content').eq(tab_index).removeClass("inactive-content")
        .addClass("active-content");
}
//Script to set up and control the ageStage tab widget (using jquery)
var ageStageID = "ageStage";
function selectTheilerStage()
{ changeTab($('#'+ageStageID+' .tab-nav')[0],ageStageID); }
function selectAge()
{ changeTab($('#'+ageStageID+' .tab-nav')[1],ageStageID); }
function ageStageChange(e)
{ if(!$(this).hasClass("active-tab")) changeTab(this,ageStageID); }
// Init the event listener for clicking tabs
$('#'+ageStageID+' .tab-nav').click(ageStageChange);

// init the age/stage widgets for diff query form (tie the two together)
//var difAgeStage1ID = "ageStage2";
//var difAgeStage2ID = "ageStage3";
//function selectDifTheilerStage()
//{
//	changeTab($('#'+difAgeStage1ID+' .tab-nav')[0],difAgeStage1ID);
//	changeTab($('#'+difAgeStage2ID+' .tab-nav')[0],difAgeStage2ID);
//}
//function selectDifAge()
//{
//	changeTab($('#'+difAgeStage1ID+' .tab-nav')[1],difAgeStage1ID);
//	changeTab($('#'+difAgeStage2ID+' .tab-nav')[1],difAgeStage2ID);
//}
//function difAgeStageChange1(e)
//{
//	if(!$(this).hasClass("active-tab"))
//	{
//		if(this.id=="stagesTab2") selectDifTheilerStage();
//		else if(this.id=="agesTab2") selectDifAge();
//	}
//}
//// Init the event listener for clicking tabs
//$('#'+difAgeStage1ID+' .tab-nav').click(difAgeStageChange1);
//
//function difAgeStageChange2(e)
//{
//	if(!$(this).hasClass("active-tab"))
//	{
//		if(this.id=="stagesTab3") selectDifTheilerStage();
//		else if(this.id=="agesTab3") selectDifAge();
//	}
//}
//// Init the event listener for clicking tabs
//$('#'+difAgeStage2ID+' .tab-nav').click(difAgeStageChange2);

// returns either "Any" or a list of the selected options
function parseStageOptions(id,anyValue)
{
	if(anyValue==undefined) anyValue="0";
	var stage = YAHOO.util.Dom.get(id);
	var stages =[]
	for(var key in stage.children)
	{
		if(stage[key]!=undefined && stage[key].selected)
		{
			// set to "Any" if stage "0" appears anywhere in the list
			if(stage[key].value==anyValue)
			{
				return "Any";
			}
			stages.push(stage[key].value);
		}
	}
	return stages;
}

// Updates the "You searched for" section
var updateQuerySummary = function() {
	var summaryDiv = new YAHOO.util.Element('searchSummary');
	summaryDiv.innerHTML = "";
	var searchParams = summaryDiv.getElementsByTagName('div');

	// if this is the first load of the page, the children will not have
	// been created yet.
	if (searchParams.length == 0) {
		// Create the place holder
		var el = new YAHOO.util.Element(document.createElement('div'));
		el.appendTo(summaryDiv);
		searchParams = summaryDiv.getElementsByTagName('div')[0];
	} else {
		searchParams = searchParams[0];
	}

	// Remove all the existing summary items
	searchParams.innerHTML = "";
	var el = new YAHOO.util.Element(document.createElement('span'));
	var b = new YAHOO.util.Element(document.createElement('b'));
	var newContent = document.createTextNode("You searched for: ");
	b.appendChild(newContent);
	el.appendChild(b);
	el.appendChild(new YAHOO.util.Element(document.createElement('br')));
	el.appendTo(searchParams);

	// handle the differential stuff first
	var isDifStructure = currentQF=="differential" && currentDifQF=="structure";
	var isDifStage = currentQF=="differential" && currentDifQF=="stage";
	var isDifBoth = currentQF=="differential" && currentDifQF=="both";
	if(isDifStructure)
	{
		// Differential Structures Section
		var el = new YAHOO.util.Element(document.createElement('span'));
		el.set('innerHTML',"Detected in <b>"+YAHOO.util.Dom.get('difStructure1').value+"</b>" +
				"<span class=\"smallGrey\"> includes synonyms & substructures</span>"+
				"<br/>but not detected or assayed in <b>"+
					YAHOO.util.Dom.get('difStructure2').value+"</b>"+
				"<span class=\"smallGrey\"> includes synonyms & substructures</span>");
		el.appendTo(searchParams);
	}
	else if(isDifStage)
	{
		// Differential Stages Section
		var el = new YAHOO.util.Element(document.createElement('span'));
		var selectedStages = parseStageOptions("difTheilerStage1","0");
		var detectedStages = [];
		var detectedStagesText = "developmental stage(s):";
		if(selectedStages=="Any") detectedStagesText = "<b>Any</b> developmental stage";
		else
		{
			for(var i=0;i<selectedStages.length;i++)
			{
				detectedStages.push("<b>TS:"+selectedStages[i]+"</b>");
			}
			detectedStagesText += " ("+detectedStages.join(" or ")+")";
		}
		var selectedDifStages = parseStageOptions("difTheilerStage2","-1");
		var notDetectedStages = [];
		var notDetectedStagesText = "developmental stage(s):";
		if(selectedDifStages=="Any") notDetectedStagesText = "<b>Any developmental stage not selected above</b>";
		else
		{
			for(var i=0;i<selectedDifStages.length;i++)
			{
				notDetectedStages.push("<b>TS:"+selectedDifStages[i]+"</b>");
			}
			notDetectedStagesText += " ("+notDetectedStages.join(", ")+")";
		}

		var htmlText = "Detected at " +detectedStagesText+
				"<br/>but not detected or assayed in any of the "+notDetectedStagesText;
		el.set('innerHTML',htmlText);
		el.appendTo(searchParams);
	}
	else if(isDifBoth)
	{
		// 3rd ribbon query

		var el = new YAHOO.util.Element(document.createElement('span'));
		// parse the stages input
		var selectedStages = parseStageOptions("difTheilerStage3","0");
		var detectedStages = [];
		var detectedStagesText = "developmental stage(s):";
		if(selectedStages=="Any") detectedStagesText = "<b>Any</b> developmental stage";
		else
		{
			for(var i=0;i<selectedStages.length;i++)
			{
				detectedStages.push("<b>TS:"+selectedStages[i]+"</b>");
			}
			detectedStagesText += " ("+detectedStages.join(" or ")+")";
		}
		var selectedDifStages = parseStageOptions("difTheilerStage4","-1");
		var notDetectedStages = [];
		var notDetectedStagesText = "developmental stage(s):";
		if(selectedDifStages=="Any") notDetectedStagesText = "<b>Any developmental stage not selected above</b>";
		else
		{
			for(var i=0;i<selectedDifStages.length;i++)
			{
				notDetectedStages.push("<b>TS:"+selectedDifStages[i]+"</b>");
			}
			notDetectedStagesText += " ("+notDetectedStages.join(", ")+")";
		}

		el.set('innerHTML',"Detected in <b>"+YAHOO.util.Dom.get('difStructure3').value+"</b>" +
				"<span class=\"smallGrey\"> includes synonyms & substructures</span>"+
				"<br/>at "+detectedStagesText+
				"<br/>but not detected or assayed in <b>"+
					YAHOO.util.Dom.get('difStructure4').value+"</b>"+
				"<span class=\"smallGrey\"> includes synonyms & substructures</span>"+
				"<br/>in any of the "+notDetectedStagesText);
		el.appendTo(searchParams);
	}
	else
	{
		// Standard QF Section

		// Create all the relevant search parameter elements
		if (YAHOO.util.Dom.get('nomenclature').value != "") {

			// Create a span
			var el = new YAHOO.util.Element(document.createElement('span'));
			//add the text node to the newly created span
			el.appendChild(document.createTextNode("Gene nomenclature: "));

			// Create a bold
			var b = new YAHOO.util.Element(document.createElement('b'));
			var newContent = document.createTextNode(YAHOO.util.Dom.get('nomenclature').value);
			// Build and append the nomenclature query parameter section
			b.appendChild(newContent);
			el.appendChild(b);

			var sp = new YAHOO.util.Element(document.createElement('span'));
			sp.addClass("smallGrey");
			sp.appendChild(document.createTextNode(" current symbol, name, synonyms"));
			el.appendChild(sp);

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		}

		if (YAHOO.util.Dom.get('locations').value != "") {

			// Create a span
			var el = new YAHOO.util.Element(document.createElement('span'));
			//add the text node to the newly created span
			el.appendChild(document.createTextNode("Genome location(s): "));

			// Create a bold
			var b = new YAHOO.util.Element(document.createElement('b'));
			var newContent = document.createTextNode(YAHOO.util.Dom.get('locations').value);
			// Build and append the nomenclature query parameter section
			b.appendChild(newContent);
			el.appendChild(b);

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		}

		if (YAHOO.util.Dom.get('vocabTerm').value != "" && YAHOO.util.Dom.get("annotationId").value!="") {

			// Create a span
			var el = new YAHOO.util.Element(document.createElement('span'));
			//add the text node to the newly created span
			el.appendChild(document.createTextNode("Genes annotated to "));

			// Create a bold
			var b = new YAHOO.util.Element(document.createElement('b'));
			var c = YAHOO.util.Dom.get('vocabTerm').value;
			var s = c.split(" - ");
			var newValue = document.createTextNode(s[1] + ": "+s[0]);
			// Build and append the nomenclature query parameter section
			b.appendChild(newValue);
			el.appendChild(b);

			var sp = new YAHOO.util.Element(document.createElement('span'));
			sp.addClass("smallGrey");
			sp.appendChild(document.createTextNode(" includes subterms"));
			el.appendChild(sp);

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		}
		// do detected
		var detectedText = "Assayed";
		// 1 = Yes, 2 = No
		if(YAHOO.util.Dom.get("detected1").checked)
		{
			detectedText = "Detected";
		}
		if(YAHOO.util.Dom.get("detected2").checked)
		{
			detectedText = "Not detected";
		}

		// do structure
		// Create all the relevant search parameter elements
		if (YAHOO.util.Dom.get('structure').value != "") {

			el = new YAHOO.util.Element(document.createElement('span'));

			b = new YAHOO.util.Element(document.createElement('b'));
			b.appendChild(document.createTextNode(detectedText));
			el.appendChild(b);

			el.appendChild(document.createTextNode(" in "));

			b = new YAHOO.util.Element(document.createElement('b'));
			b.appendChild(document.createTextNode(YAHOO.util.Dom.get('structure').value));
			el.appendChild(b);

			var sp = new YAHOO.util.Element(document.createElement('span'));
			sp.addClass("smallGrey");
			sp.appendChild(document.createTextNode(" includes synonyms & substructures"));
			el.appendChild(sp);

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);


		} else {

			el = new YAHOO.util.Element(document.createElement('span'));

			b = new YAHOO.util.Element(document.createElement('b'));
			b.appendChild(document.createTextNode(detectedText));
			el.appendChild(b);

			el.appendChild(document.createTextNode(" in "));

			b = new YAHOO.util.Element(document.createElement('b'));
			b.appendChild(document.createTextNode("any structures"));
			el.appendChild(b);

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		}



		// If the user selected age to search by, show the age display,
		// otherwise show the TS display as default
		var age = YAHOO.util.Dom.get('age');

		if (age.parentNode.className != "inactive-content") {

			// do age
			var ages="";
			var _ages = [];
			for(var key in age.children)
			{
				if(age[key]!=undefined && age[key].selected)
				{
					// set to "Any" if stage "ANY" appears anywhere in the list
					if(age[key].value=="ANY") ages = "Any";
					else _ages.push(age[key].innerHTML);
				}
			}

			el = new YAHOO.util.Element(document.createElement('span'));
			el.appendChild(document.createTextNode("at age(s): "));

			// ensure that "Any" is displayed if somehow no ages are selected
			if (_ages.length == 0 || ages != "") {
				b = new YAHOO.util.Element(document.createElement('b'));
				b.appendChild(document.createTextNode("Any"));
				el.appendChild(b);
			} else {
				var cnt = 0;
				for(var i in _ages) {
					b = new YAHOO.util.Element(document.createElement('b'));
					var ageText = "";
					if(cnt == 0) ageText += "(";
					ageText += _ages[i];
					if(cnt == _ages.length-1) ageText+=")";
					b.appendChild(document.createTextNode(ageText));
					el.appendChild(b);
					if(cnt < _ages.length-1) el.appendChild(document.createTextNode(" or "));
					cnt+=1;
				}
			}

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		} else {

			// Build and append the theiler stage query parameter section
			var ts = YAHOO.util.Dom.get('theilerStage');
			var structureID = YAHOO.util.Dom.get('structureID');

			if(structureID.value == "" || (structureID.value != "" && ts.value != 0)) {

				var _stages = [];
				for(var key in ts.children)
				{
					if(ts[key]!=undefined && ts[key].selected)
					{
						// set to "Any" if stage "0" appears anywhere in the list
						if(ts[key].value=="0") stages = "Any";
						else _stages.push("TS:"+ts[key].value);
					}
				}

				el = new YAHOO.util.Element(document.createElement('span'));
				el.appendChild(document.createTextNode("at developmental stage(s): "));

				// ensure that "Any" is displayed if somehow no ages are selected
				if (_stages.length == 0) {
					b = new YAHOO.util.Element(document.createElement('b'));
					b.appendChild(document.createTextNode("Any"));
					el.appendChild(b);
				} else {
					var cnt = 0;
					for(var i in _stages) {
						b = new YAHOO.util.Element(document.createElement('b'));
						var stageText = "";
						if(cnt == 0) stageText +="(";
						stageText += _stages[i];
						if(cnt == _stages.length-1) stageText += ")";
						b.appendChild(document.createTextNode(stageText));
						el.appendChild(b);
						if(cnt < _stages.length-1) el.appendChild(document.createTextNode(" or "));
						cnt+=1;
					}
				}
				el.appendChild(new YAHOO.util.Element(document.createElement('br')));
				el.appendTo(searchParams);
			}
		}


		// do genetic background
		var gbText = "Specimens: ";
		if(YAHOO.util.Dom.get("mutatedSpecimen").checked)
		{
			var mutatedIn = YAHOO.util.Dom.get("mutatedIn");
			if (mutatedIn.value != "")
			{
				// mutated in specific nomenclature
				el = new YAHOO.util.Element(document.createElement('span'));
				el.appendChild(document.createTextNode(gbText));
				b = new YAHOO.util.Element(document.createElement('b'));
				b.appendChild(document.createTextNode("Mutated in "+mutatedIn.value));
				el.appendChild(b);

				var sp = new YAHOO.util.Element(document.createElement('span'));
				sp.addClass("smallGrey");
				sp.appendChild(document.createTextNode(" current symbol, name, synonyms"));
				el.appendChild(sp);

				el.appendChild(new YAHOO.util.Element(document.createElement('br')));
				el.appendTo(searchParams);
			}
		}
		else if (YAHOO.util.Dom.get("isWildType").checked)
		{
			// only wild type specimens
			el = new YAHOO.util.Element(document.createElement('span'));
			el.appendChild(document.createTextNode(gbText));
			b = new YAHOO.util.Element(document.createElement('b'));
			b.appendChild(document.createTextNode("Wild type only"));
			el.appendChild(b);
			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		}

		// do assay types
		var boxes = YAHOO.util.Selector.query(".assayType");
		var assayTypes = [];
		for(var key in boxes)
		{
			var box = boxes[key];
			if(box.checked)
			{
				assayTypes.push(box.value);
			}
		}
		if(assayTypes.length > 0 && !YAHOO.util.Dom.get("assayType-ALL").checked)
		{

			el = new YAHOO.util.Element(document.createElement('span'));
			el.appendChild(document.createTextNode("Assayed by "));

			var cnt = 0;
			for(var i in assayTypes) {
				b = new YAHOO.util.Element(document.createElement('b'));
				var assayTypeText = "";
				if(cnt == 0) assayTypeText +="(";
				assayTypeText += assayTypes[i];
				if(cnt == assayTypes.length-1) assayTypeText += ")";
				b.appendChild(document.createTextNode(assayTypeText));
				el.appendChild(b);
				if(cnt < assayTypes.length-1) el.appendChild(document.createTextNode(" or "));
				cnt+=1;
			}

			el.appendChild(new YAHOO.util.Element(document.createElement('br')));
			el.appendTo(searchParams);
		}
	}
};


//
// Handle the animation for the queryform
//
var toggleQF = function(oCallback,noAnimate) {
	if(noAnimate==undefined) noAnimate=false;

	// ensure popups are hidden
	stagePopupPanel.hide();
	genePopupPanel.hide();
	stagePopupPanel.hide();

    var outer = YAHOO.util.Dom.get('outer');
	YAHOO.util.Dom.setStyle(outer, 'overflow', 'hidden');
    var qf = YAHOO.util.Dom.get('qwrap');
    var toggleLink = YAHOO.util.Dom.get('toggleLink');
    var toggleImg = YAHOO.util.Dom.get('toggleImg');
    attributes =  { height: { to: 0 }};

    var toHeight = QFHeight;
    if (currentQF == "differential") { toHeight = DifQFHeight; }
    else if (currentQF == "batch") { toHeight = BatchQFHeight; }

    if (!YAHOO.lang.isNull(toggleLink) && !YAHOO.lang.isNull(toggleImg)
    		) {
	    attributes = { height: { to: toHeight }};
		if (!qDisplay){
			attributes = { height: { to: 0  }};
			setText(toggleLink, "Click to modify search");
	    	YAHOO.util.Dom.removeClass(toggleImg, 'qfCollapse');
	    	YAHOO.util.Dom.addClass(toggleImg, 'qfExpand');
	    	qDisplay = true;
		} else {
	    	YAHOO.util.Dom.setStyle(qf, 'height', '0px');
	    	YAHOO.util.Dom.setStyle(qf, 'display', 'none');
	    	YAHOO.util.Dom.removeClass(toggleImg, 'qfExpand');
	    	YAHOO.util.Dom.addClass(toggleImg, 'qfCollapse');
	    	setText(toggleLink, "Click to hide search");
	    	qDisplay = false;
	    	changeVisibility('qwrap');
		}
	}
    var animComplete;
    // define what to do after the animation finishes
    if(qDisplay)
    {
    	animComplete = function(){
    		changeVisibility('qwrap');
    		$("#qwrap").css("height","auto");
    	};
    }
    else
    {
    	animComplete = function(){
    		YAHOO.util.Dom.setStyle(outer, 'overflow', 'visible');

			if (/MSIE (\d+.\d+);/.test(navigator.userAgent)) {
				// Reduce the ie/yui quirky behavior
		    	YAHOO.util.Dom.setStyle(qf, 'display', 'none');
		    	YAHOO.util.Dom.setStyle(qf, 'display', 'block');
		    	YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('expressionSearch'), 'display', 'none');
		    	YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('expressionSearch'), 'display', 'block');
		    	YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('toggleQF'), 'display', 'none');
		    	YAHOO.util.Dom.setStyle(YAHOO.util.Dom.get('toggleQF'), 'display', 'block');
			}
    		$("#qwrap").css("height","auto");
    	};
    }
    if(!noAnimate)
    {
		var myAnim = new YAHOO.util.Anim('qwrap', attributes);

		myAnim.onComplete.subscribe(animComplete);

		if (!YAHOO.lang.isNull(oCallback)){
			myAnim.onComplete.subscribe(oCallback);
		}

		myAnim.duration = 0.75;
		myAnim.animate();
    }
    else
    {
    	YAHOO.util.Dom.setStyle("qwrap", 'height', attributes["height"]["to"]+'px');
    	animComplete();
    }
};


// Attache the animation handler to the toggleQF div
var toggleLink = YAHOO.util.Dom.get("toggleQF");
if (!YAHOO.lang.isUndefined(toggleLink)){
	YAHOO.util.Event.addListener("toggleQF", "click", toggleQF);
}

// Open all the controls tagged with the summaryControl class
function openSummaryControl()
{
	var summaryControls = YAHOO.util.Selector.query(".summaryControl");
	for(var i=0;i<summaryControls.length;i++)
	{
		YAHOO.util.Dom.setStyle(summaryControls[i],"display","block");
	}
	// also ensure that the qf is closed
	// call the toggle function with no animation
	if(qDisplay==false) toggleQF(null,true);
}

// Close all the controls tagged with the summaryControl class
function closeSummaryControl()
{
	var summaryControls = YAHOO.util.Selector.query(".summaryControl");
	for(var i=0;i<summaryControls.length;i++)
	{
		YAHOO.util.Dom.setStyle(summaryControls[i],"display","none");
	}
	// also ensure that qf is open
	// call the toggle function with no animation
	if(qDisplay==true) toggleQF(null,true);
};

// Instead of submitting the form, do an AJAX request
var interceptSubmit = function(e) {
	log("in interceptSubmit()...");
	YAHOO.util.Event.preventDefault(e);

	if (!runValidation()){
		log("in if clause")
		// Do not allow any content to overflow the outer
		// div when it is hiding
		var outer = YAHOO.util.Dom.get('outer');
		YAHOO.util.Dom.setStyle(outer, 'overflow', 'hidden');

		if(typeof clearAllFilters != 'undefined')
		{
			clearAllFilters();
			prepFilters();
		}

		// Set the global querystring to the form values
		window.querystring = getQueryString(this);

		newQueryState = true;
		if(typeof resultsTabs != 'undefined')
		{
			// go to genes tab for differential, and results tab for anything else
			if(currentQF=="differential") resultsTabs.selectTab(0);
			else resultsTabs.selectTab(2);
		}
		if(gxdDataTable != undefined)
			gxdDataTable.setAttributes({ width: "100%" }, true);

		toggleQF(openSummaryControl);

		if (currentQF == 'batch') {
			// convert spaces to escaped version before submission
			YAHOO.util.Dom.get('ids').value = YAHOO.util.Dom.get('ids').value.replace(/ /g, '%20');
		}
		log("exiting if clause");
	}
	log("ending interceptSubmit()")
};

YAHOO.util.Event.addListener("gxdQueryForm", "submit", interceptSubmit);
YAHOO.util.Event.addListener("gxdBatchQueryForm1", "submit", interceptSubmit);
YAHOO.util.Event.addListener("gxdDifferentialQueryForm1","submit",interceptSubmit);
YAHOO.util.Event.addListener("gxdDifferentialQueryForm2","submit",interceptSubmit);
YAHOO.util.Event.addListener("gxdDifferentialQueryForm3","submit",interceptSubmit);

/*
 * The following functions handle form validation/restriction
 */

var setVisibility = function(id, isVisible) {
    if (isVisible){
        YAHOO.util.Dom.setStyle(id, 'display', 'block');
    } else {
        YAHOO.util.Dom.setStyle(id, 'display', 'none');
    }
};

var setSubmitDisabled = function(isVisible) {
	// toggle the submit buttons
	var sub1 = YAHOO.util.Dom.get("submit1");
	var sub2 = YAHOO.util.Dom.get("submit2");
	sub1.disabled = isVisible;
	sub2.disabled = isVisible;
};

// Validation function for preventing submit of both vocab and nomen searches
var geneRestriction  = function() {
	var form = YAHOO.util.Dom.get("gxdQueryForm");
	var nomen = form.nomenclature.value;
	var vocab = form.vocabTerm.value;

	// determine error state
	var setVisible = false;
	if (nomen.replace(/^\s+|\s+$/g, '') != '' && vocab.replace(/^\s+|\s+$/g, '') != ''){
		setVisible = true;
	}
	setSubmitDisabled(setVisible);
	// hide/show error message
	setVisibility('geneError', setVisible);
	return setVisible;
};

var mutationRestriction  = function() {

	var form = YAHOO.util.Dom.get("gxdQueryForm");
	var mutated = form.mutatedIn.value;
	var selected = '';
	var selectVisible = false;
	var geneVisible = false;

    var radios = document.getElementsByName('geneticBackground');
    for (i = 0; i < radios.length; i++) {
        if (radios[i].checked) {
            selected =  radios[i].id;
        }
    }
	if (mutated.replace(/^\s+|\s+$/g, '') != '') {
		if (selected != 'mutatedSpecimen') {
			selectVisible = true;
		}
	}  else {
		if (selected == 'mutatedSpecimen'){
			geneVisible = true;
		}
	}

	setVisibility('mutatedSelectError', selectVisible);
	setVisibility('mutatedGeneError', geneVisible);
	return selectVisible || geneVisible;
};

var difStructureRestriction  = function()
{
	var form = YAHOO.util.Dom.get("gxdDifferentialQueryForm1");
	var structure = form.structure.value;
	var difStructure = form.difStructure.value;

	var setVisible = structure == '' || difStructure == '';

	setVisibility('difStructureError', setVisible);
	return setVisible;
};

var difStageRestriction  = function()
{
	var form = YAHOO.util.Dom.get("gxdDifferentialQueryForm2");
	var stage = form.theilerStage;
	var difStage = form.difTheilerStage;

	var hasAnyStage=false;
	for(var key in stage.children)
	{
		if(stage[key]!=undefined && stage[key].selected)
		{
			// set to "Any" if stage "-1" appears anywhere in the list
			if(stage[key].value=="0")
			{
				hasAnyStage=true;
				break;
			}
		}
	}

	var hasAnyStageAbove=false;
	for(var key in difStage.children)
	{
		if(difStage[key]!=undefined && difStage[key].selected)
		{
			// set to "Any" if stage "-1" appears anywhere in the list
			if(difStage[key].value=="-1")
			{
				hasAnyStageAbove=true;
				break;
			}
		}
	}
	var setVisible = hasAnyStage && hasAnyStageAbove;

	setVisibility('difStageError', setVisible);
	return setVisible;
};

var difBothRestriction  = function()
{
	var form = YAHOO.util.Dom.get("gxdDifferentialQueryForm3");
	var structure = form.structure.value;
	var difStructure = form.difStructure.value;

	var setVisible = structure == '' || difStructure == '';

	setVisibility('difStructStageError', setVisible);
	return setVisible;
};

/* returns false if there are NO validation errors, true if there are some
 */
var runValidation  = function(){
	var result=false;
	log("in runValidation() - currentQF: " + currentQF);

	if(currentQF == "standard")
	{
		result = geneRestriction() || mutationRestriction();
		setSubmitDisabled(result);
	}
	else if (currentQF == 'batch') {
		result = false;			// no current validations
	}
	else if(currentDifQF=="structure")
	{
		result = difStructureRestriction();
		//YAHOO.util.Dom.get("submit3").disabled=result;
	}
	else if(currentDifQF=="stage")
	{
		result = difStageRestriction();
	}
	else if(currentDifQF=="both")
	{
		result = difBothRestriction();
	}
	log("exiting runValidation() --> " + result);
	return result;
};
var clearValidation = function()
{
	if(currentQF == "standard") runValidation();
	else
	{
		setVisibility('difStructureError', false);
		setVisibility('difStageError',false);
		setVisibility('difStructStageError',false);
	}
}

YAHOO.util.Event.addListener(YAHOO.util.Dom.get("nomenclature"), "keyup", runValidation);
YAHOO.util.Event.addListener(YAHOO.util.Dom.get("nomenclature"), "change", runValidation);
YAHOO.util.Event.addListener(YAHOO.util.Dom.get("vocabTermAutoComplete"), "keyup", runValidation);

YAHOO.util.Event.addListener(YAHOO.util.Dom.get("mutatedIn"), "keyup", runValidation);
YAHOO.util.Event.addListener(YAHOO.util.Dom.get("mutatedIn"), "change", runValidation);
YAHOO.util.Event.addListener(YAHOO.util.Dom.get("mutatedSpecimen"), "click", runValidation);
YAHOO.util.Event.addListener(YAHOO.util.Dom.get("isWildType"), "click", runValidation);
YAHOO.util.Event.addListener(YAHOO.util.Dom.get("allSpecimen"), "click", runValidation);

// clear the vocab input box when the user changes the value after selecting
// a term
var vocabACState = "";

var clearVocabTerm = function(e) {
	// don't do anything if vocabACState is not set, or if enter key (or TAB) is pressed
	var keyCode = e.keyCode;
	if(e.charCode) keyCode = e.charCode;
	// ignore keycodes between 9 and 40 (ctrl, shift, enter, tab, arrows, etc)
	if (vocabACState != "" && (keyCode <9 || keyCode >40)){
		var termIdInput = YAHOO.util.Dom.get("annotationId");
		var vocabInput = YAHOO.util.Dom.get("vocabTerm");
		if (vocabInput.value!="" && vocabInput.value == window.vocabACState) {
			// clear the state, the visual input, and the hidden term field input
			window.vocabACState = "";
			vocabInput.value = "";
			termIdInput.value = "";
		}
	}
};

YAHOO.util.Event.addListener(YAHOO.util.Dom.get("vocabTerm"), "keypress", clearVocabTerm);


/*
 * Vocab Term Auto Complete Section
 */

(function() {
    // Use an XHRDataSource
    var oDS = new YAHOO.util.XHRDataSource(fewiurl + "autocomplete/vocabTerm");
    // Set the responseType
    oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    // Define the schema of the JSON results
    oDS.responseSchema = {resultsList: "summaryRows",
    		fields:["markerCount", "termId", "formattedTerm","inputTerm","hasExpression"]};
    //oDS.maxCacheEntries = 10;
    oDS.connXhrMode = "cancelStaleRequests";

    var oAC = new YAHOO.widget.AutoComplete("vocabTerm", "vocabTermContainer", oDS);

    //HACK: maybe we should figure out how to subclass this widget if we want to reuse it.
    // override behavior of selectItem event
    oAC._selectItem = function(elListItem) {
	    this._bItemSelected = true;
	    this._updateValue(elListItem);
	    this._sPastSelections = this._elTextbox.value;
	    this._clearInterval();
	    this.itemSelectEvent.fire(this, elListItem, elListItem._oResultData);
	    YAHOO.log("Item selected: " + YAHOO.lang.dump(elListItem._oResultData), "info", this.toString());
	    // turning off the automatic toggle, so that we can do it in the select event.
	    //this._toggleContainer(false);
	};

    // Throttle requests sent
    oAC.queryDelay = .03;
    oAC.minQueryLength = 2;
    oAC.maxResultsDisplayed = 500;
    oAC.forceSelection = true;

    // Do _something_ to the tabs for IE to prevent
    // the over
    //oAC.containerExpandEvent.subscribe(function(){if (/MSIE (\d+.\d+);/.test(navigator.userAgent)) {YAHOO.util.Dom.setStyle('ageStage', 'z-index', '0');}});
    //oAC.containerCollapseEvent.subscribe(function(){if (/MSIE (\d+.\d+);/.test(navigator.userAgent)) {YAHOO.util.Dom.setStyle('ageStage', 'z-index', '1000');}});

    //oAC.alwaysShowContainer=true;
    //oAC.delimChar = ";";

    //go back to autocomplete after warning is closed
    var refocusAC = function(e) {
    	var inputBox = YAHOO.util.Dom.get("vocabTerm");
    	inputBox.focus();
    	oAC.sendQuery(inputBox.value);
    };

	YAHOO.gxd.container.panelAlert = new YAHOO.widget.Panel("vocabWarning",
			{ visible:false,
			context:["vocabTermAutoComplete","tl","bl", ["beforeShow"]],
			width:"400px", draggable:false, constraintoviewport:true } );
	YAHOO.gxd.container.panelAlert.render();

    // try to set the input field after itemSelect event
    oAC.suppressInputUpdate = true;
    var selectionHandler = function(sType, aArgs) {
	    var myAC = aArgs[0]; // reference back to the AC instance
	    var elLI = aArgs[1]; // reference to the selected LI element
	    var oData = aArgs[2]; // object literal of selected item's result data

	    //populate input box with another value (the base structure name)
	    var markerCount = oData[0];
	    var termId = oData[1];
	    var formattedTerm = oData[2];
	    var inputTerm = oData[3];
	    var hasExpression = oData[4];

	    var inputBox = YAHOO.util.Dom.get("vocabTerm");
	    var idBox = YAHOO.util.Dom.get("annotationId");
	    idBox.value = "";



	    // does the term have expression data associated?
	    if (hasExpression)
	    {
	    	vocabACState = inputTerm;
	    	inputBox.value = inputTerm;
	    	idBox.value = termId;
	    	oAC._toggleContainer(false);
	    }
	    else
	    {
	    	// check the count of associated markers
	    	if(markerCount > 0)
	    	{
	    		oAC._toggleContainer(false);
		    	var warningBox = YAHOO.util.Dom.get("vocabWarningText");
		    	warningBox.innerHTML = "";
		    	var alertText = "The genes annotated with the vocabulary term you selected (<b>"+inputTerm+"</b>) do not have any gene expression data associated with them.";
		    	warningBox.innerHTML = alertText;

		    	YAHOO.gxd.container.panelAlert.show();
	    		var nodes = YAHOO.util.Selector.query("#vocabWarning .container-close");
	    		YAHOO.util.Event.on(nodes, 'click', refocusAC);
	    	}
	    	else
	    	{
	    		oAC._toggleContainer(true);
	    		//oAC.sendQuery ( inputBox.value );
	    	}
	    }

	};
    oAC.itemSelectEvent.subscribe(selectionHandler);
    oAC.selectionEnforceEvent.subscribe(runValidation);

    oAC.formatResult = function(oData, sQuery, sResultMatch) {
	    //var markerCount = oData[0];
	    //var termKey = oData[1];
	    var formattedTerm = oData[2];
	   // var inputTerm = oData[3];
	    //var hasExpression = oData[4];
    	 return formattedTerm;
    };

    return {
        oDS: oDS,
        oAC: oAC
    };
})();

/* get a string for a Theiler Stage range
 */
function tsRange(startStage, endStage) {
    var ts = "TS" + startStage;
    if (startStage != endStage) {
	ts = ts + "-" + endStage;
    }
    return ts;
}

/*
 * Anatomical Dictionary Auto Complete Section
 */
function makeStructureAC(inputID,containerID){

	var hiddenID = inputID + "ID"
    // Use an XHRDataSource
    var oDS = new YAHOO.util.XHRDataSource(fewiurl + "autocomplete/gxdEmapa");
    // Set the responseType
    oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    // Define the schema of the JSON results
    //oDS.responseSchema = {resultsList: "resultObjects", fields:["structure", "synonym","isStrictSynonym"]};
	oDS.responseSchema = {resultsList: "resultObjects", fields:["structure", "synonym","isStrictSynonym","accID","startStage","endStage"]};

    //oDS.maxCacheEntries = 10;
    oDS.connXhrMode = "cancelStaleRequests";

    // Instantiate the AutoCompletes
    var oAC = new YAHOO.widget.AutoComplete(inputID, containerID, oDS);

    // Throttle requests sent
    oAC.queryDelay = .03;
    oAC.minQueryLength = 2;
    oAC.maxResultsDisplayed = 500;
    oAC.forceSelection = true;
	//oAC.forceSelection = false;
    //oAC.delimChar = ";";

    // blank out the hidden ID field upon deleting the structure text in the input field
    var removeSelectedID = function(oSelf,yuiE) {
    	var ac = yuiE[0];
    	if(ac.getInputEl().value.trim() == "")
    	{
		    var idBox = YAHOO.util.Dom.get(hiddenID);
	 	    idBox.value = "";
    	}
    };
    oAC.textboxChangeEvent.subscribe(removeSelectedID);

    // try to set the input field after itemSelect event
    oAC.suppressInputUpdate = true;
    var selectionHandler = function(sType, aArgs) {
	    var myAC = aArgs[0]; // reference back to the AC instance
	    var elLI = aArgs[1]; // reference to the selected LI element
	    var oData = aArgs[2]; // object literal of selected item's result data

	    //populate input box
	    var newInputBoxValue = oData[0]; // start with the EMAPA term
		var synonym = oData[1];
		var isStrictSynonym = oData[2];
	    var accID = oData[3];
		var tsStart = oData[4];
		var tsStop  = oData[5];
		if(isStrictSynonym) {
			newInputBoxValue = newInputBoxValue + " (" + synonym + ")"
		}
		newInputBoxValue = newInputBoxValue.toLowerCase();
		newInputBoxValue = newInputBoxValue + " "
			+ tsRange(tsStart, tsStop);

	    var inputBox = YAHOO.util.Dom.get(inputID);
	    inputBox.value = newInputBoxValue;

	    //populate hidden ID for param
	    var accID = oData[3];
	    var idBox = YAHOO.util.Dom.get(hiddenID);
	    idBox.value = accID;

    };
    oAC.itemSelectEvent.subscribe(selectionHandler);

    oAC.formatResult = function(oResultData, sQuery, sResultMatch) {

	    var userInputField = YAHOO.util.Dom.get("structure");
	    var userInput = userInputField.value.toLowerCase();

		// some other piece of data defined by schema
		var term = oResultData[0];
		var synonym = oResultData[1];
		var isStrictSynonym = oResultData[2];
		var tsStart = oResultData[4];
		var tsStop  = oResultData[5];
		var value = term;
		if(isStrictSynonym) {
			value = value + " (" + synonym + ") "
		}
		value = value.toLowerCase();
		var value = value.replace(userInputField.value, "<b>" + userInputField.value + "</b>");
		value = value + " <span style=\"font-size:0.8em; font-style:normal;\"> "
			+ tsRange(tsStart, tsStop) + "</span>";

		return (value);
    };

    var toggleVis = function(){
        if (YAHOO.util.Dom.getStyle('structureHelp', 'display') == 'none'){
            YAHOO.util.Dom.setStyle('structureHelp', 'display', 'block');
        }
    };

    oAC.itemSelectEvent.subscribe(toggleVis);

    return {
        oDS: oDS,
        oAC: oAC
    };
};
makeStructureAC("structure","structureContainer");
makeStructureAC("difStructure1","difStructureContainer1");
makeStructureAC("difStructure2","difStructureContainer2");
makeStructureAC("difStructure3","difStructureContainer3");
makeStructureAC("difStructure4","difStructureContainer4");

//
// Wire up the functionality to reset the query form
//
var resetQF = function (e) {
	log("called resetQF");
	if (e) YAHOO.util.Event.preventDefault(e);
	var form = YAHOO.util.Dom.get("gxdQueryForm");
	form.nomenclature.value = "";
	form.vocabTerm.value = "";
	form.annotationId.value = "";
	form.structure.value = "";
	form.structureID.value = "";
	form.theilerStage.selectedIndex = 0;
	form.age.selectedIndex = 0;
	form.locations.value = "";
	selectTheilerStage();
	allAssayTypesBox.checked = true;
	for(var key in assayTypesBoxes)
	{
		assayTypesBoxes[key].checked = true;
	}
	form.detected3.checked=true;
	form.allSpecimen.checked=true;
	form.mutatedIn.value = "";

	var difForm1 = YAHOO.util.Dom.get("gxdDifferentialQueryForm1");
	if(difForm1)
	{
		difForm1.structure.value="";
		difForm1.structureID.value="";
		difForm1.difStructure.value="";
		difForm1.difStructureID.value="";
	}
	var difForm2 = YAHOO.util.Dom.get("gxdDifferentialQueryForm2");
	if(difForm2)
	{
		difForm2.theilerStage.selectedIndex=0;
		difForm2.difTheilerStage.selectedIndex=0;
	}
	var difForm3 = YAHOO.util.Dom.get("gxdDifferentialQueryForm3");
	if(difForm3)
	{
		difForm3.structure.value="";
		difForm3.structureID.value="";
		difForm3.difStructure.value="";
		difForm3.difStructureID.value="";
		difForm3.theilerStage.selectedIndex=0;
		difForm3.difTheilerStage.selectedIndex=0;
	}
	var batchForm = YAHOO.util.Dom.get("gxdBatchQueryForm1");
	if (batchForm) {
		batchForm.idType.selectedIndex=0;
		batchForm.ids.value="";
		batchForm.fileType.selectedIndex=0;
		batchForm.idColumn.value="1";
//		batchForm.idFile.value=null;
	}

	// clear the validation errors
	clearValidation();

	// clear facets
	resetFacets();
};

YAHOO.util.Event.addListener("gxdQueryForm", "reset", resetQF);
YAHOO.util.Event.addListener("gxdBatchQueryForm1", "reset", resetQF);
YAHOO.util.Event.addListener("gxdDifferentialQueryForm1", "reset", resetQF);
YAHOO.util.Event.addListener("gxdDifferentialQueryForm2", "reset", resetQF);
YAHOO.util.Event.addListener("gxdDifferentialQueryForm3", "reset", resetQF);

//
// Return the passed in form argument values in key/value URL format
//
var getQueryString = function(form) {
	if(form==undefined) form = getCurrentQF();
	var _qs = [];
	for(var i=0; i<form.elements.length; i++)
	{
		var element = form.elements[i];
		if(element.name != ""
			&& element.name !="_theilerStage" && element.name !="_age"
			&& element.name !="_difTheilerStage" && element.name !="_difAge")
		{
			if(element.tagName=="TEXTAREA")
			{
				_qs.push(element.name + "="  +element.value.replace(/\s/g,' '));
			}
			else if((element.tagName=="INPUT") && (element.name != 'idFile'))
			{
				if(element.type=="checkbox" || element.type=="radio")
				{
					// don't add any assay type params if the all box is checked
					// also ignore their "all/either" options
					if( (element.name=="assayType" && allAssayTypesBox.checked)
							|| element.id == "assayType-ALL" || element.id == "detected3") continue;
					else if(element.name=="geneticBackground")
					{
						if(element.id=="isWildType")
						{
							if(element.checked)
							{
								_qs.push(element.id + "=true");
							}
						}
					}
					else if(element.checked)
					{
						_qs.push(element.name + "="  +element.value);
					}
				}
				else
				{
					// ignore mutatedIn field if the corresponding radio button is not checked
					if(element.name=="mutatedIn" && (element.value=="" || !YAHOO.util.Dom.get("mutatedSpecimen").checked))
						continue;
					_qs.push(element.name + "="  +element.value);
				}

			}
			else if (element.tagName=="SELECT")
			{
				if (element.parentNode.className != "inactive-content") {
					for(var key in element.children)
					{
						if(element[key]!=undefined && element[key].selected)
						{
							_qs.push(element.name+"="+element[key].value);
						}
					}
				}
			}
			else
			{
				log("Unknown field: " + element.name + " (type " + element.tagName + ")");
			}
		}
	}
	return _qs.join("&");
};


// parses request parameters and resets and values found with their matching form input element
// returns false if no parameters were found
// responsible for repopulating the form during history manager changes
function reverseEngineerFormInput(request)
{
	log("called reverseEngineerFormInput");
	var params = parseRequest(request);
	var formID = "#gxdQueryForm";
	var foundDifStruct=false;
	var foundDifStage=false;
	var foundBatch=false;
	var filters = {};	// filters[filter name] = [ values ]

	for(var key in params)
	{
		if(key == "detected")
		{
			// HACK for the radio buttons
			params["detected1"] = params[key];
			params["detected2"] = params[key];
		}
		else if(key == "difStructure") foundDifStruct=true;
		else if(key == "difTheilerStage" || key=="difAge") foundDifStage=true;
		else if(key == 'idType') foundBatch=true;
	}
	// make sure correct form is visible
	// this code allows for flexibility to add third ribbon
	if(foundDifStruct && foundDifStage)
	{
		formID = "#gxdDifferentialQueryForm3";
		showDifBothQF();
	}
	else if (foundDifStruct)
	{
		formID = "#gxdDifferentialQueryForm1";
		showDifStructuresQF();
	}
	else if (foundDifStage)
	{
		formID = "#gxdDifferentialQueryForm2";
		showDifStagesQF();
	} else if (foundBatch)
	{
		formID = "#gxdBatchQueryForm1";
		showBatchSearchForm();
	}

	var foundParams = false;
	resetQF();
	for(var key in params)
	{
		// need special handling for idFile field (do not set this to
		// an empty string!)
		if (key == 'idFile') {
			log('skipping idFile');
			// no op - skip it
			// $(formID+" [name='idFile']").value = null;
		}
		else if(key!=undefined && key!="" && key!="detected" && params[key].length>0)
		{
			//var input = YAHOO.util.Dom.get(key);
			// jQuery is better suited to resolving form name parameters
			var input = $(formID+" [name='"+key+"']");
			if(input.length < 1) input = $(formID+" #"+key);
			if(input!=undefined && input!=null && input.length > 0)
			{

				input = input[0];
				if(input.tagName=="TEXTAREA")
				{
					input.value = decodeURIComponent(params[key]);
				}
				else if(input.tagName=="INPUT")
				{
					foundParams = true;
					// do radio boxes
					if(input.type == "radio")
					{
						if(key=="isWildType")
						{
							YAHOO.util.Dom.get("isWildType").checked = true;
						}
						else if(input.value == params[key])
						{
							input.checked=true;
						}
					}
					// do check boxes
					else if(input.type=="checkbox")
					{
						var options = [];
						var rawParams = [].concat(params[key]);
						for(var i=0;i<rawParams.length;i++)
						{
							options.push(decodeURIComponent(rawParams[i]));
						}
						// The YUI.get() only returns one checkbox, but we want the whole set.
						// The class should also be set to the same name.
						var boxes = YAHOO.util.Selector.query("."+key);
						for(var i=0;i<boxes.length;i++)
						{
							var box = boxes[i];
							var checked = false;
							for(var j=0;j<options.length;j++)
							{
								if(options[j] == box.value)
								{
									checked = true;
									box.checked = true;
									break;
								}
							}
							if(!checked)
							{
								box.checked = false;
							}
						}
					}
					else
					{
						if (key == "mutatedIn")
						{
							YAHOO.util.Dom.get("mutatedSpecimen").checked = true;
						}
						input.value = decodeURIComponent(params[key]);
					}
				}
				else if(input.tagName=="SELECT")
				{
					if (input.name == "age") {
						// open the age tab
						//if(foundDifStage) selectDifAge();
						//else selectAge();
						selectAge();
					}
					foundParams = true;
					var options = [];
					// decode all the options first
					var rawParams = [].concat(params[key]);
					for(var i=0;i<rawParams.length;i++)
					{
						options.push(decodeURIComponent(rawParams[i]));
					}
					// find which options need to be selected, and select them.
					for(var key in input.children)
					{
						if(input[key]!=undefined)
						{
							var selected = false;
							for(var j=0;j<options.length;j++)
							{
								if(options[j] == input[key].value)
								{
									selected = true;
									input[key].selected = true;
									break;
								}
							}
							if(!selected)
							{
								input[key].selected = false;
							}
						}
					}
				}
			} else if (typeof isFilterable != 'undefined' &&
					isFilterable(key)) {
			    // deal with filters (no form fields for them)
				// TODO: This needs to move to a different function. Filters should not be a part of this method
			    filters[key] = [].concat(params[key]);
			}
		}
	}

	if(typeof resetFacets != 'undefined')
	{
		resetFacets(filters);
		prepFilters(request);	// need to reset the URLs for the filters
	}
	assayTypesCheck();
	return foundParams;
}

// add the check box listeners for assay types
var allAssayTypesBox = YAHOO.util.Dom.get("assayType-ALL");
var assayTypesBoxes = YAHOO.util.Selector.query(".assayType");
var allAssayTypesLabel = YAHOO.util.Dom.get("allAssayTypeLabel");
var assayTypesLabels = YAHOO.util.Selector.query(".assayTypeLabel");
var allAssayTypesCheck = function(e)
{
	// set everything to the same checked value as the all box
	var allChecked = allAssayTypesBox.checked;
	for(var key in assayTypesBoxes)
	{
		assayTypesBoxes[key].checked = allChecked;
	}
};
YAHOO.util.Event.addListener(allAssayTypesLabel, "click", allAssayTypesCheck);
var assayTypesCheck = function(e)
{
	// check the current value of all check boxes to see if all needs to be checked/unchecked
	var allChecked = true;
	for(key in assayTypesBoxes)
	{
		if(assayTypesBoxes[key].checked==false)
		{
			allChecked = false;
		}
	}
	allAssayTypesBox.checked = allChecked;
};
for(var key in assayTypesLabels)
{
	YAHOO.util.Event.addListener(assayTypesLabels[key], "click", assayTypesCheck);
}

// Add the listener for mutatedIn onFocus
var mutatedInOnFocus = function(e)
{
	YAHOO.util.Dom.get("mutatedSpecimen").checked = true;
};
YAHOO.util.Event.addFocusListener(YAHOO.util.Dom.get("mutatedIn"),mutatedInOnFocus);


