	<c:set var="otherMgiIDs" value="${marker.otherMgiIDs}"/>
	<c:if test="${not empty otherMgiIDs}">
		<div class="row">
			<div class="header <%=leftTdStyles.getNext() %>" >
				Other<br/>Accession&nbsp;IDs
			</div>
			<div class="detail <%=rightTdStyles.getNext() %>" >
				<div id="toggleOtherAccessRibbon" title="Show More" class="toggleImage hdCollapse"></div>

				<div class="extra" style="padding-left: 35px;">
					<c:forEach var="item" items="${otherMgiIDs}" varStatus="status">
						${item.accID}<c:if test="${not status.last}">, </c:if>
					</c:forEach>
				</div>

			</div>
		</div>
	</c:if>
