	<c:if test="${not empty interactions}">
		<div class="row">
			<div class="header <%=leftTdStyles.getNext() %>">
				<span style="white-space: nowrap; vertical-align: top;">
					Interactions
				</span>
			</div>
			<div class="detail <%=rightTdStyles.getNext() %>">
				<table>
					<tr>
						<td class="top">
							<div id="toggleInteractionRibbon" title="Show More" class="toggleImage hdCollapse"></div>
						</td>
						<td>
							<div id="openedInteractionRibbon" class="extra open" style="display:block;">
								<span style="display: inline; line-height: 130%">
									<c:forEach var="interaction" items="${interactions}" varStatus="status">
										${interaction}<c:if test="${!status.last}"><br/></c:if>
									</c:forEach>
									<div style="display: inline; margin-left: 5px;">
										<a id="interactionLink" href="${configBean.FEWI_URL}interaction/explorer?markerIDs=${marker.primaryID}" class="markerNoteButton" style="display:inline;">View All</a>
									</div>
								</span>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</div>
	</c:if>
