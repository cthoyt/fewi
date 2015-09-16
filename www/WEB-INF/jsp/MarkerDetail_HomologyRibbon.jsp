	<c:if test="${(not empty humanHomologs) or (marker.hasOneEnsemblGeneModelID) or (not empty marker.pirsfAnnotation) }">
		<div class="row" >
			<div class="header <%=leftTdStyles.getNext() %>">Homology</div>
			<div class="detail <%=rightTdStyles.getNext() %>">

				<section class="summarySec1 extra open">
					<ul>

						<c:if test="${fn:length(humanHomologs) > 0}">
							<c:set var="humanHomolog" value="${humanHomologs[0]}" />
							<li>
								<div class="label">
									<div id="toggleHomologyRibbon" title="Show More" class="toggleImage hdExpand"></div>
									Human Ortholog
								</div>
								<div class="value">
									<c:set var="humanCoords" value="${humanHomolog.preferredCoordinates}"/>
									<fmt:formatNumber value="${humanCoords.startCoordinate}" pattern="#0" var="humanStartCoord"/>
									<fmt:formatNumber value="${humanCoords.endCoordinate}" pattern="#0" var="humanEndCoord"/>
									${humanHomolog.symbol}, ${humanHomolog.name}
								</div>
							</li>
						</c:if>
				
						<c:if test="${fn:length(humanHomologs) == 0}">
							<c:forEach var="homologyClass" items="${homologyClasses}">
								<c:if test="${not empty homologyClass.primaryID}">
									<li>
										<div class="label">
											<div id="toggleHomologyRibbon" title="Show More" class="toggleImage hdExpand"></div>
											HomoloGene
										</div>
										<div class="value">
											<c:forEach var="organismOrthology" items="${homologyClass.orthologs}" varStatus="status">${organismOrthology.markerCount} ${organismOrthology.organism}<c:if test="${!status.last}">;</c:if></c:forEach>
										</div>
								</c:if>
							</c:forEach>
						</c:if>

					</ul>
				</section>

				<c:if test="${fn:length(humanHomologs) > 0}">
					<section class="summarySec2 extra open">
						<ul>
							<li>
								<c:forEach var="homologyClass" items="${homologyClasses}">
									<c:if test="${not empty homologyClass.primaryID}">
										<div class="label">
											Vertebrate&nbsp;Orthologs
										</div>
										<div class="value">
											<c:set var="organismOrthologyCount" value="0"/>
											<c:forEach var="organismOrthology" items="${homologyClass.orthologs}" varStatus="status">
												<c:set var="organismOrthologyCount" value="${organismOrthology.markerCount + organismOrthologyCount}" />
											</c:forEach>
											${organismOrthologyCount}
										</div>
									</c:if>
								</c:forEach>
							</li>
						</ul>
					</section>
				</c:if>

				<div class="homologyExtra extra closed">
					<c:forEach var="humanHomolog" items="${humanHomologs}" varStatus="humanHomologStatus">

						<section class="summarySec1 wide">
							<ul>
								<li>
									<div class="label">
										<div id="toggleHomologyRibbon" title="Show More" class="toggleImage hdExpand"></div>
										Human&nbsp;Ortholog
									</div>
									<div class="value">
										<c:set var="humanCoords" value="${humanHomolog.preferredCoordinates}"/>
										<fmt:formatNumber value="${humanCoords.startCoordinate}" pattern="#0" var="humanStartCoord"/>
										<fmt:formatNumber value="${humanCoords.endCoordinate}" pattern="#0" var="humanEndCoord"/>
						
										${humanHomolog.symbol}, ${humanHomolog.name}<br />
										Orthology source: 
										<c:forEach var="homologyCluster" items="${marker.getHomologyClusterSources(humanHomolog)}" varStatus="hstat">
											${homologyCluster.source}<c:if test="${!hstat.last}">, </c:if>
										</c:forEach>	
									</div>
								</li>

								<c:if test="${not empty humanHomolog.synonyms}">
									<li>
										<div class="label">
											Synonyms
										</div>
										<div class="value">
											<c:forEach var="synonym" items="${humanHomolog.synonyms}" varStatus="synonymStatus">
												${synonym.synonym}<c:if test="${!synonymStatus.last}">, </c:if>
											</c:forEach>
										</div>
									</li>
								</c:if>

								<li>
									<div class="label">
										Links
									</div>
									<div class="value">

										<c:if test="${not empty humanHomolog.hgncID}">
											<div style="float: left; margin-right: 20px;">
												<a href="${fn:replace(urls.HGNC, '@@@@', humanHomolog.hgncID.accID)}" target="_blank">${humanHomolog.hgncID.accID}</a>
											</div>
										</c:if>
			
										<div style="float: left; margin-right: 20px;">NCBI Gene ID: <a href="${fn:replace(urls.Entrez_Gene, '@@@@', humanHomolog.entrezGeneID.accID)}" target="_blank">${humanHomolog.entrezGeneID.accID}</a></div>

										<c:if test="${not empty humanHomolog.neXtProtIDs}">
											<div style="float: left; margin-right: 20px;">neXtProt AC:
												<c:forEach var="neXtProt" items="${humanHomolog.neXtProtIDs}" varStatus="neXtProtStatus">
													<a href="${fn:replace(urls.neXtProt, '@@@@', neXtProt.accID)}" target="_blank">${neXtProt.accID}</a><c:if test="${!neXtProtStatus.last}">, </c:if>
												</c:forEach>
											</div>
										</c:if>

										<br style="clear:left;"/>
									</div>
								</li>

								<li>
									<div class="label">
										Chr&nbsp;Location
									</div>
									<div class="value">
										<c:set var="humanCytoband" value="${humanHomolog.preferredCytoband}"/>
										<c:if test="${not empty humanCytoband}">${humanCytoband.chromosome}${humanCytoband.cytogeneticOffset}<c:if test="${not empty humanCoords}">; </c:if></c:if>
										<c:if test="${not empty humanCoords}">
											chr${humanCoords.chromosome}:${humanStartCoord}-${humanEndCoord}
											<c:if test="${not empty humanCoords.strand}">(${humanCoords.strand})</c:if>&nbsp;&nbsp;<I>${humanCoords.buildIdentifier}</I>
										</c:if>
									</div>
								</li>

							</ul>
						</section>
						<hr>
					</c:forEach>

					<section class="summarySec1 wide">
						<ul>
							<c:forEach var="homologyClass" items="${homologyClasses}">
								<c:if test="${not empty homologyClass.primaryID}">
									<li>
										<div class="label">HomoloGene</div>
										<div class="value">
											<a href="${configBean.FEWI_URL}homology/${homologyClass.primaryID}">Vertebrate Homology Class ${homologyClass.primaryID}</a><br/>
											<c:forEach var="organismOrthology" items="${homologyClass.orthologs}" varStatus="status">${organismOrthology.markerCount} ${organismOrthology.organism}<c:if test="${!status.last}">;</c:if></c:forEach><br/>
										</div>
									</li>
								</c:if>
							</c:forEach>

							<c:if test="${not empty hcopLinks}">
								<li>
									<div class="label">HCOP</div>
									<div class="value">
										<c:forEach var="organism" items="${hcopLinks}">
											<c:if test="${fn:length(organism.value) > 0}">
												${organism.key} homology predictions:
												<c:forEach var="hmarker" items="${organism.value}" varStatus="hcstat">
													<c:if test="${organism.key == 'human'}">
														<a href="${fn:replace(urls.HCOP, '@@@@', hmarker.value.symbol)}" target="_blank">${hmarker.value.symbol}</a><c:if test="${!hcstat.last}">, </c:if>
													</c:if>
												</c:forEach>
											</c:if>
										</c:forEach>
									</div>
								</li>
							</c:if>

							<c:if test="${not empty marker.pirsfAnnotation}">
								<li>
									<div class="label">Protein&nbsp;SuperFamily</div>
									<div class="value">
										<a href="${configBean.FEWI_URL}vocab/pirsf/${marker.pirsfAnnotation.termID}">${marker.pirsfAnnotation.term}</a>
									</div>
								</li>
							</c:if>

							<c:if test="${marker.hasOneEnsemblGeneModelID}">
								<c:set var="genetreeUrl" value="${configBean.GENETREE_URL}"/>
								<c:set var="genetreeUrl" value="${fn:replace(genetreeUrl, '<model_id>', marker.ensemblGeneModelID.accID)}"/>
								<li>
									<div class="label">Gene&nbsp;Tree</div>
									<div class="value">
										<a href="${configBean.GENETREE_URL}${marker.ensemblGeneModelID.accID}" target="_blank">${marker.symbol}</a><br/>
									</div>
								</li>
							</c:if>
						</ul>
					</section>
				</div>
			</div>
		</div>
	</c:if>
