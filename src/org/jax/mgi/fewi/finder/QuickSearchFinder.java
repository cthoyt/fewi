package org.jax.mgi.fewi.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jax.mgi.fewi.hunter.SolrQSVocabResultHunter;
import org.jax.mgi.fewi.hunter.SolrQSAlleleResultFacetHunter;
import org.jax.mgi.fewi.hunter.SolrQSAlleleResultHunter;
import org.jax.mgi.fewi.hunter.SolrQSFeatureResultFacetHunter;
import org.jax.mgi.fewi.hunter.SolrQSFeatureResultHunter;
import org.jax.mgi.fewi.hunter.SolrQSFeatureResultTinyHunter;
import org.jax.mgi.fewi.hunter.SolrQSLookupHunter;
import org.jax.mgi.fewi.hunter.SolrQSStrainResultFacetHunter;
import org.jax.mgi.fewi.hunter.SolrQSStrainResultHunter;
import org.jax.mgi.fewi.hunter.SolrQSVocabResultFacetHunter;
import org.jax.mgi.fewi.searchUtil.Filter;
import org.jax.mgi.fewi.searchUtil.Paginator;
import org.jax.mgi.fewi.searchUtil.SearchConstants;
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.jax.mgi.fewi.summary.QSAlleleResult;
import org.jax.mgi.fewi.summary.QSFeaturePart;
import org.jax.mgi.fewi.summary.QSFeatureResult;
import org.jax.mgi.fewi.summary.QSStrainResult;
import org.jax.mgi.fewi.summary.QSVocabResult;
import org.jax.mgi.fewi.util.LimitedSizeCache;

/*
 * This finder is responsible for finding results for the quick search
 */
@Repository
public class QuickSearchFinder {

	//--- static variables ---//
	
	// caches of symbol/name/location data for markers and alleles
	private static int cacheSize = 10000;
	private static LimitedSizeCache<QSFeaturePart> markerParts = new LimitedSizeCache<QSFeaturePart>(cacheSize);
	private static LimitedSizeCache<QSFeaturePart> alleleParts = new LimitedSizeCache<QSFeaturePart>(cacheSize);
	
	//--- instance variables ---//
	
	private Logger logger = LoggerFactory.getLogger(QuickSearchFinder.class);

	@Autowired
	private SolrQSAlleleResultHunter qsAlleleHunter;

	@Autowired
	private SolrQSStrainResultHunter qsStrainHunter;

	@Autowired
	private SolrQSVocabResultHunter qsVocabHunter;

	@Autowired
	private SolrQSFeatureResultTinyHunter qsFeatureTinyHunter;

//	@Autowired
//	private SolrQSFeatureResultHunter qsFeatureHunter;
//
	@Autowired
	private SolrQSFeatureResultFacetHunter featureFacetHunter;
	
	@Autowired
	private SolrQSAlleleResultFacetHunter alleleFacetHunter;
	
	@Autowired
	private SolrQSVocabResultFacetHunter vocabFacetHunter;
	
	@Autowired
	private SolrQSStrainResultFacetHunter strainFacetHunter;
	
	@Autowired
	private SolrQSLookupHunter lookupHunter;
	
	//--- public methods ---//

	/* return all QSAlleleResult (from Solr) objects matching the given search parameters
	 */
	public SearchResults<QSAlleleResult> getAlleleResults(SearchParams searchParams) {
		logger.debug("->getAlleleResults");

		// result object to be returned
		SearchResults<QSAlleleResult> searchResults = new SearchResults<QSAlleleResult>();

		// ask the hunter to identify which objects to return
		qsAlleleHunter.hunt(searchParams, searchResults);
		logger.debug("->hunter found " + searchResults.getResultObjects().size() + " QS allele results");

		return searchResults;
	}

	/* return all QSstrainResult (from Solr) objects matching the given search parameters
	 */
	public SearchResults<QSStrainResult> getStrainResults(SearchParams searchParams) {
		logger.debug("->getStrainResults");

		// result object to be returned
		SearchResults<QSStrainResult> searchResults = new SearchResults<QSStrainResult>();

		// ask the hunter to identify which objects to return
		qsStrainHunter.hunt(searchParams, searchResults);
		logger.debug("->hunter found " + searchResults.getResultObjects().size() + " QS strain results");

		return searchResults;
	}

	/* return all QSVocabResult (from Solr) objects matching the given search parameters
	 */
	public SearchResults<QSVocabResult> getVocabResults(SearchParams searchParams) {
		logger.debug("->getVocabResults");

		// result object to be returned
		SearchResults<QSVocabResult> searchResults = new SearchResults<QSVocabResult>();

		// ask the hunter to identify which objects to return
		qsVocabHunter.hunt(searchParams, searchResults);
		logger.debug("->hunter found " + searchResults.getResultObjects().size() + " QS vocab results");

		return searchResults;
	}

	/* return all QSFeatureResult (from Solr) objects matching the given search parameters
	 */
	public SearchResults<QSFeatureResult> getFeatureResults(SearchParams searchParams) {
		logger.debug("->getFeatureResults");

		// result object to be returned
		SearchResults<QSFeatureResult> searchResults = new SearchResults<QSFeatureResult>();

		// ask the hunter to identify which objects to return
		qsFeatureTinyHunter.hunt(searchParams, searchResults);
		logger.debug("->hunter found " + searchResults.getResultObjects().size() + " QS feature results");

		return searchResults;
	}
	
	/* get the specified facets for the matching feature results
	 */
	public List<String> getFeatureFacets(SearchParams searchParams, String facetField) {
		SearchResults<QSFeatureResult> results = new SearchResults<QSFeatureResult>();
		featureFacetHunter.setFacetString(facetField);
		featureFacetHunter.hunt(searchParams, results);
		return results.getResultFacets();
	}

	/* get the specified facets for the matching vocab results
	 */
	public List<String> getVocabFacets(SearchParams searchParams, String facetField) {
		SearchResults<QSVocabResult> results = new SearchResults<QSVocabResult>();
		vocabFacetHunter.setFacetString(facetField);
		vocabFacetHunter.hunt(searchParams, results);
		return results.getResultFacets();
	}

	/* get the specified facets for the matching strain results
	 */
	public List<String> getStrainFacets(SearchParams searchParams, String facetField) {
		SearchResults<QSStrainResult> results = new SearchResults<QSStrainResult>();
		strainFacetHunter.setFacetString(facetField);
		strainFacetHunter.hunt(searchParams, results);
		return results.getResultFacets();
	}

	/* get the specified facets for the matching allele results
	 */
	public List<String> getAlleleFacets(SearchParams searchParams, String facetField) {
		SearchResults<QSAlleleResult> results = new SearchResults<QSAlleleResult>();
		alleleFacetHunter.setFacetString(facetField);
		alleleFacetHunter.hunt(searchParams, results);
		return results.getResultFacets();
	}
	
	/* Get a mapping from ID to the symbol/name/location parts corresponding to each of the given marker IDs.
	 */
	public Map<String,QSFeaturePart> getMarkerParts(List<QSFeatureResult> markers) {
		List<String> markerIDs = new ArrayList<String>(markers.size());
		for (QSFeatureResult marker : markers) {
			markerIDs.add(marker.getPrimaryID());
		}
		return this.getFeatureParts(markerIDs, markerParts);
	}
	
	/* Get a mapping from ID to the symbol/name/location parts corresponding to each of the given allele IDs.
	 */
	public Map<String,QSFeaturePart> getAlleleParts(List<QSAlleleResult> alleles) {
		List<String> alleleIDs = new ArrayList<String>(alleles.size());
		for (QSAlleleResult allele : alleles) {
			alleleIDs.add(allele.getPrimaryID());
		}
		return this.getFeatureParts(alleleIDs, alleleParts);
	}
	
	/* Get a mapping from ID to the symbol/name/location parts corresponding to each of the given marker IDs.
	 * Utilize the given cache to avoid retrieving common items from Solr.
	 */
	private Map<String,QSFeaturePart> getFeatureParts(List<String> ids, LimitedSizeCache<QSFeaturePart> cache) {
		logger.info("->getFeatureParts (" + ids.size() + " IDs), cache size: " + cache.size());
		
		// mapping we're compiling to return
		Map<String,QSFeaturePart> out = new HashMap<String,QSFeaturePart>();

		// those not in cache that we need to get from Solr
		List<String> toFind = new ArrayList<String>();

		// get those that already exist in cache
		for (String id : ids) {
			if (cache.containsKey(id)) {
				out.put(id, cache.get(id));
			} else {
				toFind.add(id);
			}
		}
		logger.info("Got " + out.size() + " from cache, " + toFind.size() + " yet to find");

		int sliceSize = 500;
		Paginator page = new Paginator(500);
		
		int start = 0;
		while (start < toFind.size()) {
			int end = Math.min(start + sliceSize, toFind.size());
			List<String> slice = toFind.subList(start, end);
			
			SearchParams params = new SearchParams();
			params.setPaginator(page);
			logger.info("Looking up " + slice.size() + " IDs");
			params.setFilter(new Filter(SearchConstants.QS_PRIMARY_ID, slice, Filter.Operator.OP_IN));
			
			SearchResults<QSFeaturePart> results = new SearchResults<QSFeaturePart>();
			lookupHunter.hunt(params, results);
			
			logger.info("Looked up " + results.getResultObjects().size() + " objects from Solr");
			for (QSFeaturePart fp : results.getResultObjects()) {
				out.put(fp.getPrimaryID(), fp);
				cache.put(fp.getPrimaryID(), fp);
			}
			
			start = end;
		}
		
		logger.info("->hunter found " + out.size() + " feature parts");
		return out;
	}
}
