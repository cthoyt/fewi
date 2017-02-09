<c:set var="functionID" value="${fn:replace(diseaseRow.get('diseaseId'), ':', '_')}_dialog" />

<div id="${functionID}" class="facetFilter; bottomBorder" style="display:none">
	<div class="hd">${diseaseRow.get('diseaseTerm')}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
	<div class="bd" style="overflow: auto; max-height: 150px; max-width: 750px;">
		<a></a><!-- this empty 'a' tag is to keep Chrome and Safari from putting a selection box aroudn the first link displayed -->
		<p/>
		<c:forEach var="secondardId" items="${allAnnotations.get(diseaseRow.get('diseaseId')).vocabTerm.secondaryIds}">
			<c:if test="${diseaseRow.get('diseaseId') == secondardId.accID}">
				<span style="font-size: smaller;">${idLinker.getLink(secondardId, secondardId.accID, 'MP')}1</span><br>
			</c:if>
			<c:if test="${diseaseRow.get('diseaseId') != secondardId.accID}">
				<span style="font-size: smaller;">${idLinker.getLink(secondardId, secondardId.accID, 'MP')}2</span><br>
			</c:if>
		</c:forEach>
	</div>
</div>

 <script type="text/javascript">
	YAHOO.namespace("diseaseDetail.container");

	var show_${functionID} = function(e) {
		YAHOO.diseaseDetail.container.panel${functionID}.show(YAHOO.diseaseDetail.container.panel${functionID});
		pageTracker._trackEvent("MarkerDetailPageEvent", "disease name popup", "${functionID}");
	};

	YAHOO.util.Event.onDOMReady(function() {
		YAHOO.diseaseDetail.container.panel${functionID} = new YAHOO.widget.Panel ("${functionID}", { visible:false, constraintoviewport:true, context:['show_${functionID}', 'tl', 'br', ['beforeShow', 'windowResize'] ] } );
		YAHOO.diseaseDetail.container.panel${functionID}.render();
		YAHOO.util.Event.addListener("show_${functionID}", "click", show_${functionID});
		YAHOO.util.Event.addListener("YAHOO.diseaseDetail.container.panel${functionID}", "move", YAHOO.diseaseDetail.container.panel${functionID}.forceContainerRedraw);
		YAHOO.util.Event.addListener("YAHOO.diseaseDetail.container.panel${functionID}", "mouseover", YAHOO.diseaseDetail.container.panel${functionID}.forceContainerRedraw);
		var elem = document.getElementById("${functionID}");
		if (elem != null) {
			elem.style.display = '';	// make the div visible
		}
	});
</script>
