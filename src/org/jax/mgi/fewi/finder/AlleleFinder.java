package org.jax.mgi.fewi.finder;

import java.util.List;

import mgi.frontend.datamodel.Allele;

import org.jax.mgi.fewi.hunter.SolrAlleleKeyHunter;
import org.jax.mgi.fewi.objectGatherer.HibernateObjectGatherer;
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AlleleFinder {

	private Logger logger = LoggerFactory.getLogger(AlleleFinder.class);

	@Autowired
	private SolrAlleleKeyHunter alleleHunter;

	@Autowired
	private HibernateObjectGatherer<Allele> alleleGatherer;


    /////////////////////////////////////////////////////////////////////////
    //  Retrieval of a sequence, for a given ID
    /////////////////////////////////////////////////////////////////////////

	public SearchResults<Allele> getAlleleByID(SearchParams searchParams) {

		logger.info("SequenceFinder.getSequenceByID()");

		// result object to be returned
		SearchResults<Allele> searchResults = new SearchResults<Allele>();

		// ask the hunter to identify which objects to return
		alleleHunter.hunt(searchParams, searchResults);

		// gather objects identified by the hunter, add them to the results
		alleleGatherer.setType(Allele.class);
        List<Allele> alleleList = alleleGatherer.get( searchResults.getResultKeys() );
        searchResults.setResultObjects(alleleList);

		return searchResults;
	}


}
