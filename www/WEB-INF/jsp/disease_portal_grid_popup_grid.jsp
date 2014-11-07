<%@ page import = "mgi.frontend.datamodel.Genotype" %>
<%@ page import = "org.jax.mgi.fewi.util.*" %>

<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/jsp/google_analytics_pageview.jsp" %>

<!-- Assumes title and header are set -->

<style type="text/css">
 
</style>


<!-- Table and Wrapping div -->
<div id="hdpSystemPopupWrap">
<table id="hdpSystemPopupTable">

<!-- Column Headers -->
<tr style="height: 240px;">
	<c:choose>
		<c:when  test="${empty popupRows and not empty humanPopupRows}">
			<!-- if we have genes, but no genotypes, display "Gene" header -->
			<th>Human Gene</th>
		</c:when>
		<c:otherwise>
	  		<th>Mouse Genotype</th>
	  	</c:otherwise>
	</c:choose>
  <!--  header columns -->
  <c:set var="lastColImage" />
  <c:forEach var="headerCol" items="${termColumns}" varStatus="status">
    <th class="vb">
      <div class="rotate45"><c:out value="${headerCol}" escapeXml="false" /></div>
      <div style="position:relative;"><div class="partialRight"></div>
    </th>
    <c:if test="${status.last}"><c:set var="lastColImage" value="${headerCol}"/></c:if>
  </c:forEach>
</tr>

<c:forEach var="popupRow" items="${popupRows}" varStatus="status">
    <c:set var="genotype" value="${popupRow.genotype}" scope="request"/>
	<tr title="click row to see phenotype details" class="genoRow ${status.index % 2 == 0 ? 'row1' : 'row2'}" onClick="window.open('${configBean.FEWI_URL}diseasePortal/genoCluster/view/${popupRow.genoClusterKey}'); return true;">
    <td style="color:blue;"> 
      <fewi:genotype value="${genotype}" noLink="${true}" />
      <span style="color:black;"> 
        <c:if test="${genotype.existsAs eq 'Chimeric'}"> (Chimeric)</c:if><c:if test="${genotype.isConditional eq 1}"> (conditional)</c:if>
      </span>
    </td>

    <c:forEach var="cell" items="${popupRow.gridCells}" varStatus="status">
      <td class="cc mpBin_${cell.mpBin}"><div class="cell_outer">
	    <c:if test="${cell.hasPopup}">
		    <div class="gridCellLink">
			    <div class="mk">${cell.mpPopupMark}</div>
		    </div>
		<c:if test="${cell.hasBackgroundNote}"><div class="bsn">!</div></c:if>
	    </c:if>
      </div></td>
    </c:forEach>
    </tr>
</c:forEach>

<c:if test="${not empty humanPopupRows}">
	<!-- Add human data if it has been set -->
	<c:if test="${not empty popupRows}">
		<!-- Add "Gene" header if we have Genotypes above -->
		<tr class="humanHeaderRow"><th>Human Gene</th>
			<c:forEach var="termColumn" items="${termColumns}"><td></td></c:forEach>
		</tr>
	</c:if>
	<c:forEach var="popupRow" items="${humanPopupRows}" varStatus="status">
	
		<c:set var="hasHomologeneId" value="${not empty popupRow.marker.homologeneId}"/>
		
		<tr <c:if test="${hasHomologeneId}">title="click gene for homology details"</c:if> class="genoRow ${status.index % 2 == 0 ? 'row1' : 'row2'}" 
			<c:if test="${hasHomologeneId}">onClick="window.open('${configBean.FEWI_URL}homology/${popupRow.marker.homologeneId}'); return true;"</c:if> >
	    
	    <td <c:if test="${hasHomologeneId}">style="color:blue;"</c:if> > 
	    ${popupRow.marker.symbol}
	    </td>
	
	    <c:forEach var="cell" items="${popupRow.gridCells}" varStatus="status">
	      <td class="cc dHBin_${cell.diHumanBin}"><div class="cell_outer">
                <c:if test="${cell.hasPopup}">
                    <div class="gridCellLink">
						<div class="mk">${cell.diseaseMark}</div>
					</div>
                </c:if>
          </div></td>
	    </c:forEach>
	    </tr>
	
	</c:forEach>
</c:if>

</table>
<div id="hdpSystemPopupTablePadder"></div>
</div>
