<% /* generates the subrows for markers with more than one table row */ %>
<tr>
	<td class="box snpCentered snpPadded" style="min-width: 100px"><c:if test="${not empty mrk.transcript}"><span style='font-size:80%'>${mrk.transcript}</span><br/><a href="${configBean.FEWI_URL}sequence/${mrk.transcript}">MGI Seq Detail</a></c:if></td>
	<td class="box snpCentered snpPadded" style="min-width: 100px"><c:if test="${not empty mrk.protein}"><span style='font-size:80%'>${mrk.protein}</span><br/><a href="${configBean.FEWI_URL}sequence/${mrk.protein}">MGI Seq Detail</a></c:if></td>

	<td class="box snpCentered snpPadded" style="min-width: 175px">${mrk.functionClass}</td>

	<c:set var="callClass" value="allele${fn:replace(fn:replace(mrk.contigAllele, '-', 'Dash'), '?', 'Q')}"/>
	<td class="box snpCentered snpPadded ${callClass}">${mrk.contigAllele}</td>

	<td class="box snpCentered snpPadded">${mrk.residue}</td>
	<td class="box snpCentered snpPadded">${mrk.readingFrame}</td>
	<td class="box snpCentered snpPadded">${mrk.aaPosition}</td>
</tr>
