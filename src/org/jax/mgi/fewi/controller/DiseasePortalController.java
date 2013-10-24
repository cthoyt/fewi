package org.jax.mgi.fewi.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mgi.frontend.datamodel.HdpGenoCluster;
import mgi.frontend.datamodel.HdpGenoClusterAnnotation;
import mgi.frontend.datamodel.HdpGridAnnotation;

import org.apache.commons.lang.StringUtils;
import org.jax.mgi.fewi.finder.DiseasePortalBatchFinder;
import org.jax.mgi.fewi.finder.DiseasePortalFinder;
import org.jax.mgi.fewi.forms.DiseasePortalQueryForm;
import org.jax.mgi.fewi.searchUtil.Filter;
import org.jax.mgi.fewi.searchUtil.Paginator;
import org.jax.mgi.fewi.searchUtil.SearchConstants;
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.jax.mgi.fewi.searchUtil.Sort;
import org.jax.mgi.fewi.searchUtil.SortConstants;
import org.jax.mgi.fewi.searchUtil.entities.SolrDiseasePortalMarker;
import org.jax.mgi.fewi.searchUtil.entities.SolrDpGenoInResult;
import org.jax.mgi.fewi.searchUtil.entities.SolrDpGridCluster;
import org.jax.mgi.fewi.searchUtil.entities.SolrDpGridCluster.SolrDpGridClusterMarker;
import org.jax.mgi.fewi.searchUtil.entities.SolrVocTerm;
import org.jax.mgi.fewi.summary.DiseasePortalDiseaseSummaryRow;
import org.jax.mgi.fewi.summary.DiseasePortalMarkerSummaryRow;
import org.jax.mgi.fewi.summary.HdpGenoBySystemPopupRow;
import org.jax.mgi.fewi.summary.HdpGridClusterSummaryRow;
import org.jax.mgi.fewi.summary.JsonSummaryResponse;
import org.jax.mgi.fewi.util.AjaxUtils;
import org.jax.mgi.fewi.util.FormatHelper;
import org.jax.mgi.fewi.util.ImageUtils;
import org.jax.mgi.fewi.util.QueryParser;
import org.jax.mgi.shr.fe.indexconstants.DiseasePortalFields;
import org.jax.mgi.shr.fe.query.SolrLocationTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/*
 * This controller maps all /diseasePortal/ uri's
 */
@Controller
@RequestMapping(value="/diseasePortal")
public class DiseasePortalController
{

	// logger for the class
	private Logger logger = LoggerFactory.getLogger(DiseasePortalController.class);

	// get the finders used by various methods
	@Autowired
	private DiseasePortalFinder hdpFinder;

    @Value("${solr.factetNumberDefault}")
    private Integer facetLimit;


    //--------------------------//
    // Disease Portal Query Form
    //--------------------------//

	@RequestMapping(method=RequestMethod.GET)
	public String getQueryForm(Model model) {
		model.addAttribute(new DiseasePortalQueryForm());
		return "disease_portal_query";
	}



	// Support for generating the grid columns in vertical/rotated format
   Cache<String, String> rotatedTextCache = CacheBuilder.newBuilder()
	       .maximumSize(10000)
	       .expireAfterWrite(30, TimeUnit.MINUTES).build();
   	public String getRotatedTextImgTag(String text)
   	{
   		return getRotatedTextImgTag(text,30);
   	}
	public String getRotatedTextImgTag(String text,int maxChars)
	{
		String rotatedText = rotatedTextCache.getIfPresent(text);
		if(rotatedText != null) return rotatedText;

		try{
			String rotatedTextTag = ImageUtils.getRotatedTextImageTagAbbreviated(text,310.0,maxChars);

			rotatedTextCache.put(text,rotatedTextTag);

			return rotatedTextTag;
		}catch(Exception e){}

		return "";
	}

    //----------------------------//
    // Disease Portal Summary Page
    //----------------------------//

    @RequestMapping("/summary")
    public ModelAndView genericSummary(
		    @ModelAttribute DiseasePortalQueryForm query,
            HttpServletRequest request) {

    	logger.debug("generating generic DiseasePortal summary");
		logger.debug("query string: " + request.getQueryString());
		logger.debug("query form: " + query);

		ModelAndView mav = new ModelAndView("disease_portal_query");
		String querystring = request.getQueryString();
		if(notEmpty(querystring) && !querystring.contains("tab=")) querystring += "&tab=genestab";
		mav.addObject("querystring", querystring);
		return mav;

    }


    //-----------------------//
    // Disease Portal Grid
    //-----------------------//

    @RequestMapping(value="/grid")
    public ModelAndView diseaseGrid(
      HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute DiseasePortalQueryForm query,
      @ModelAttribute Paginator page) throws Exception
    {
    	logger.debug("->diseaseGrid started");

      	// add headers to allow AJAX access
      	AjaxUtils.prepareAjaxHeaders(response);

      	// setup view object
      	ModelAndView mav = new ModelAndView("disease_portal_grid");

      	// search for grid cluster objects
      	SearchResults<SolrDpGridCluster> searchResults = this.getGridClusters(request, query, page);
      	List<SolrDpGridCluster> gridClusters = searchResults.getResultObjects();

      	// search for diseases in result set - make column headers and ID list
      	List<SolrVocTerm> diseases = this.getGridDiseaseColumns(request, query);
		List<String> diseaseColumnsToDisplay = new ArrayList<String>();
		List<String> diseaseIds = new ArrayList<String>();
		List<String> diseaseNames = new ArrayList<String>();
		for(SolrVocTerm vt : diseases)
		{
			String headerText = vt.getTerm();
			diseaseColumnsToDisplay.add(this.getRotatedTextImgTag(headerText));
			diseaseIds.add(vt.getPrimaryId());
			diseaseNames.add(vt.getTerm());
		}

      	// search for mp headers in result set & make column headers
      	List<String> mpHeaders = this.getGridMpHeaderColumns(request,query);
      	List<String> mpHeaderColumnsToDisplay = new ArrayList<String>();
		for(String mpHeader : mpHeaders)
		{
			mpHeaderColumnsToDisplay.add(this.getRotatedTextImgTag(mpHeader));
		}

      	// Search for the genotype clusters used to generate the result set
      	// and save as map for later
      	List<SolrDpGenoInResult> annotationsInResults
      	  = this.getAnnotationsInResults(query,searchResults.getResultKeys());
        Map<Integer,List<SolrDpGenoInResult>> gridClusterToGenoInResults
          = new HashMap<Integer,List<SolrDpGenoInResult>>();
        for(SolrDpGenoInResult dpa : annotationsInResults)
        {
        	// map each genocluster/header combo to its corresponding gridcluster key
            if (!gridClusterToGenoInResults.containsKey(dpa.getGridClusterKey()))
            {
            	gridClusterToGenoInResults.put(dpa.getGridClusterKey(),new ArrayList<SolrDpGenoInResult>());
            }
            gridClusterToGenoInResults.get(dpa.getGridClusterKey()).add(dpa);
        }

		// create grid row objects
		List<HdpGridClusterSummaryRow> summaryRows = new ArrayList<HdpGridClusterSummaryRow>();
		for(SolrDpGridCluster gc : gridClusters)
		{
            // ensure the cross-reference genoInResults list exists for this row
            if (gridClusterToGenoInResults.containsKey(gc.getGridClusterKey()))
            {
            	//logger.info("mapping gc key "+gc.getGridClusterKey());
            	// for this grid cluster, gather (from pre-computed set) of which
            	// genotype clusters were involved
            	List<SolrDpGenoInResult> genoInResults = gridClusterToGenoInResults.get(gc.getGridClusterKey());
            	GridMapper mpHeaderMapper = new GridMapper(mpHeaders, genoInResults);

            	// map the diseases & mp headers for this grid row
            	GridMapper diseaseMapper = new GridMapper(diseaseIds, genoInResults);

            	// add this row
            	HdpGridClusterSummaryRow summaryRow = new HdpGridClusterSummaryRow(gc,diseaseMapper,mpHeaderMapper);
            	summaryRows.add(summaryRow);
            } else {
              logger.debug("->ERROR:: grid cluster key "+gc.getGridClusterKey()+" not found in solr set");
            }
		}

        // derive the query string to pass along
        String queryString = FormatHelper.queryStringFromPost(request);

		mav.addObject("queryString", queryString);
		mav.addObject("gridClusters", summaryRows);
		mav.addObject("diseaseColumns", diseaseColumnsToDisplay);
		mav.addObject("diseaseIds", diseaseIds); // for testing
		mav.addObject("diseaseNames",diseaseNames);
		mav.addObject("mpHeaderColumns", mpHeaderColumnsToDisplay);
		mav.addObject("mpHeaders", mpHeaders);

   		return mav;
    }


    //--------------------------//
    // Grid - System Cell Popup
    //--------------------------//

    @RequestMapping(value="/gridSystemCell")
    public ModelAndView gridSystemCell(
      HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute DiseasePortalQueryForm query)
    {
    	logger.debug("->gridSystemCell started");

    	//  TODO: this hack could be way simpler. I just hacked this together for Kim real quick - kstone
    	String gridClusterString = "";
    	DiseasePortalQueryForm dpQf = new DiseasePortalQueryForm();
    	dpQf.setGridClusterKey(query.getGridClusterKey());
    	SearchResults<SolrDpGridCluster> gridClusters = this.getGridClusters(request,dpQf,new Paginator(1));
    	if(gridClusters.getResultObjects().size()>0)
    	{
    		SolrDpGridCluster gridCluster = gridClusters.getResultObjects().get(0);
    		List<String> symbols = new ArrayList<String>();
    		for(String humanSymbol : gridCluster.getHumanSymbols())
    		{
    			symbols.add(humanSymbol);
    		}
    		for(SolrDpGridClusterMarker m : gridCluster.getMouseMarkers())
    		{
    			symbols.add(m.getSymbol());
    		}
    		gridClusterString = StringUtils.join(symbols,", ");
    	}
    	// end hack

        SearchResults<HdpGenoCluster> searchResults = this.getGenoClusters(request, query);
        List<HdpGenoCluster> genoClusters = searchResults.getResultObjects();
    	logger.debug("->gridSystemCell; number of genoClusters=" + genoClusters.size());

    	List<SolrVocTerm> mpTerms = this.getGridMpTermColumns(request,query);
      	List<String> mpTermColumnsToDisplay = new ArrayList<String>();
		List<String> termColIds = new ArrayList<String>();
		List<String> termColNames = new ArrayList<String>(); // needed to automated tests

		for(SolrVocTerm mpTerm : mpTerms)
		{
			// use 30 max characters for the popup
			mpTermColumnsToDisplay.add(this.getRotatedTextImgTag(mpTerm.getTerm(),30));
			termColIds.add(mpTerm.getPrimaryId());
			termColNames.add(mpTerm.getTerm());
		}


		List<HdpGenoBySystemPopupRow> popupRows = new ArrayList<HdpGenoBySystemPopupRow>();
		// map the columns with the data
		for(HdpGenoCluster gc : genoClusters)
		{
			// map the diseases with column info
			GridMapper mpMapper = new GridMapper(termColIds, gc.getMpTerms());
			HdpGenoBySystemPopupRow popupRow = new HdpGenoBySystemPopupRow(gc, mpMapper);
			popupRows.add(popupRow);
		}

      	// setup view object
      	ModelAndView mav = new ModelAndView("disease_portal_grid_system_popup");
		mav.addObject("popupRows", popupRows);
		mav.addObject("genoClusters", genoClusters);
		mav.addObject("mpTermColumns", mpTermColumnsToDisplay);
		mav.addObject("mpTerms", termColNames);
		mav.addObject("mpHeader", query.getMpHeader());
		mav.addObject("gridClusterString",gridClusterString);

   		return mav;
    }


    //--------------------------//
    // Grid - Disease Cell Popup
    //--------------------------//

    @RequestMapping(value="/gridDiseaseCell")
    public ModelAndView gridDiseaseCell(
      HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute DiseasePortalQueryForm query,
      @RequestParam("term") String term)
    {
    	logger.debug("->gridDiseaseCell started");

    	//  TODO: this hack could be way simpler. I just hacked this together for Kim real quick - kstone
    	String gridClusterString = "";
    	DiseasePortalQueryForm dpQf = new DiseasePortalQueryForm();
    	dpQf.setGridClusterKey(query.getGridClusterKey());
    	SearchResults<SolrDpGridCluster> gridClusters = this.getGridClusters(request,dpQf,new Paginator(1));
    	if(gridClusters.getResultObjects().size()>0)
    	{
    		SolrDpGridCluster gridCluster = gridClusters.getResultObjects().get(0);
    		List<String> symbols = new ArrayList<String>();
    		for(String humanSymbol : gridCluster.getHumanSymbols())
    		{
    			symbols.add(humanSymbol);
    		}
    		for(SolrDpGridClusterMarker m : gridCluster.getMouseMarkers())
    		{
    			symbols.add(m.getSymbol());
    		}
    		gridClusterString = StringUtils.join(symbols,", ");
    	}
    	// end hack

        SearchResults<HdpGenoCluster> searchResults = this.getGenoClusters(request, query);
        List<HdpGenoCluster> genoClusters = searchResults.getResultObjects();
    	logger.debug("->gridDiseaseCell; number of genoClusters=" + genoClusters.size());

    	List<SolrDiseasePortalMarker> humanMarkers = this.getGridHumanMarkers(request,query);

      	// setup view object
      	ModelAndView mav = new ModelAndView("disease_portal_grid_disease_popup");
		mav.addObject("genoClusters", genoClusters);
		mav.addObject("humanMarkers",humanMarkers);
		mav.addObject("diseaseName",term);
		mav.addObject("diseaseId",query.getTermId());
		mav.addObject("gridClusterString",gridClusterString);

   		return mav;
    }

    @RequestMapping(value="genoCluster/view/{genoClusterKey:.+}", method = RequestMethod.GET)
    public ModelAndView genoClusterView(@PathVariable("genoClusterKey") String genoClusterKey) 
    {
    	List<HdpGenoCluster> genoClusters = hdpFinder.getGenoClusterByKey(genoClusterKey);
    	// there can be only one...
        if (genoClusters.size() < 1) { // none found
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("errorMsg", "No GenoCluster Found");
            return mav;
        }
        HdpGenoCluster genoCluster = genoClusters.get(0);
        
        ModelAndView mav = new ModelAndView("disease_portal_all_geno_popups");
        mav.addObject("genoCluster",genoCluster);
    	return mav;
    }
    
    //----------------------------//
    // Disease Portal Marker Tab
    //----------------------------//

	@RequestMapping("/markers/json")
	public @ResponseBody JsonSummaryResponse<DiseasePortalMarkerSummaryRow> geneSummaryJson(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{
		SearchResults<SolrDiseasePortalMarker> searchResults
		  = this.getSummaryResultsByGene(request, query, page,true);
        List<SolrDiseasePortalMarker> mList = searchResults.getResultObjects();

        List<DiseasePortalMarkerSummaryRow> summaryRows
          = new ArrayList<DiseasePortalMarkerSummaryRow>();

        Map<String,Set<String>> highlights = searchResults.getResultSetMeta().getSetHighlights();

        for (SolrDiseasePortalMarker m : mList)
        {
			if (m != null)
			{
				DiseasePortalMarkerSummaryRow summaryRow = new DiseasePortalMarkerSummaryRow(m);
				if(highlights.containsKey(m.getMarkerKey()))
				{
					summaryRow.setHighlightedFields(new ArrayList<String>(highlights.get(m.getMarkerKey())));
				}
				summaryRows.add(summaryRow);
			} else {
				logger.debug("--> Null Object");
			}
		}
        JsonSummaryResponse<DiseasePortalMarkerSummaryRow> jsonResponse
        		= new JsonSummaryResponse<DiseasePortalMarkerSummaryRow>();
        jsonResponse.setSummaryRows(summaryRows);
        jsonResponse.setTotalCount(searchResults.getTotalCount());

		logger.debug("geneSummaryJson finished");
        return jsonResponse;
	}


    //--------------------------------//
    // Disease Portal Marker Downloads
    //--------------------------------//
    @RequestMapping("marker/report*")
    public ModelAndView resultsMarkerSummaryExport(
            HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query) {

    	logger.debug("generating HDP marker report download");

		ModelAndView mav = new ModelAndView("hdpMarkersSummaryReport");
		
		Filter qf = this.parseQueryForm(query);
		SearchParams sp = new SearchParams();
		sp.setFilter(qf);
		List<Sort> sorts = this.genMarkerSorts(request);
		sp.setSorts(sorts);
		DiseasePortalBatchFinder batchFinder = new DiseasePortalBatchFinder(hdpFinder,sp);

		mav.addObject("markerFinder", batchFinder);

		logger.debug("controller finished - routing to view object");
		
		return mav;
    }


    //----------------------------//
    // Disease Portal Disease Tab
    //----------------------------//

	@RequestMapping("/diseases/json")
	public @ResponseBody JsonSummaryResponse<DiseasePortalDiseaseSummaryRow> diseaseSummaryJson(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{
		SearchResults<SolrVocTerm> searchResults = this.getSummaryResultsByDisease(request,query,page,true);
        List<DiseasePortalDiseaseSummaryRow> termList = new ArrayList<DiseasePortalDiseaseSummaryRow>();

        Map<String,Set<String>> highlights = searchResults.getResultSetMeta().getSetHighlights();

        for(SolrVocTerm term : searchResults.getResultObjects())
        {
        	DiseasePortalDiseaseSummaryRow summaryRow = new DiseasePortalDiseaseSummaryRow(term);
        	if(highlights.containsKey(term.getPrimaryId()))
			{
				summaryRow.setHighlightedFields(new ArrayList<String>(highlights.get(term.getPrimaryId())));
			}
        	termList.add(summaryRow);
        }

        JsonSummaryResponse<DiseasePortalDiseaseSummaryRow> jsonResponse
        		= new JsonSummaryResponse<DiseasePortalDiseaseSummaryRow>();
        jsonResponse.setSummaryRows(termList);
        jsonResponse.setTotalCount(searchResults.getTotalCount());

        return jsonResponse;
	}

	
    //--------------------------------//
    // Disease Portal Disease Downloads
    //--------------------------------//
    @RequestMapping("disease/report*")
    public ModelAndView resultsDiseaseSummaryExport(
            HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query) {

    	logger.debug("generating HDP disease report download");

		ModelAndView mav = new ModelAndView("hdpDiseaseSummaryReport");
    	
    	Filter qf = this.parseQueryForm(query);
		SearchParams sp = new SearchParams();
		sp.setFilter(qf);
		List<Sort> sorts = this.genDiseaseSorts(request);
		sp.setSorts(sorts);
		DiseasePortalBatchFinder batchFinder = new DiseasePortalBatchFinder(hdpFinder,sp);

		mav.addObject("diseaseFinder", batchFinder);
		return mav;
    }

    
    //----------------------------//
    // Disease Portal Pheno Tab
    //----------------------------//

	@RequestMapping("/phenotypes/json")
	public @ResponseBody JsonSummaryResponse<String> phenotypeSummaryJson(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{
		SearchResults<SolrVocTerm> searchResults = this.getSummaryResultsByPhenotype(request,query,page);
        List<String> termList = new ArrayList<String>();
        for(SolrVocTerm term : searchResults.getResultObjects())
        {
        	termList.add(term.getTerm()+" ("+term.getPrimaryId()+")");
        }

        JsonSummaryResponse<String> jsonResponse
        		= new JsonSummaryResponse<String>();
        jsonResponse.setSummaryRows(termList);
        jsonResponse.setTotalCount(searchResults.getTotalCount());

        return jsonResponse;
	}


    //--------------------------------------------------------------------//
    // Public convenience methods
    //--------------------------------------------------------------------//

	public List<SolrDpGenoInResult> getAnnotationsInResults(
			@ModelAttribute DiseasePortalQueryForm query,
			List<String> gridClusterKeys)
	{
		logger.debug("-->getAnnotationsInResults");

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();
		Filter originalQuery = this.parseQueryForm(query);
		Filter gridClusterFilter = new Filter(SearchConstants.DP_GRID_CLUSTER_KEY,gridClusterKeys,Filter.OP_IN);
		params.setFilter(Filter.and(Arrays.asList(originalQuery,gridClusterFilter)));
		params.setPageSize(10000); // in theory I'm not sure how high this needs to be. 10k is just a start.

		// perform query
		logger.debug("getAnnotationsInResults finished");
		SearchResults<SolrDpGenoInResult> results = hdpFinder.searchAnnotationsInGridResults(params);

        List<SolrDpGenoInResult> annotations = results.getResultObjects();

		return annotations;
	}

	public SearchResults<SolrDpGridCluster> getGridClusters(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{

		logger.debug("getGridClusters query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();
		params.setIncludeSetMeta(true);
		//params.setIncludeMetaHighlight(true);
		params.setIncludeRowMeta(true);
		params.setIncludeMetaScore(true);

		// determine and set the requested sorts, filters, and pagination
// TODO - setup sorts and pagination
		params.setSorts(this.genMarkerSorts(request));
		params.setPaginator(page);
		params.setFilter(this.parseQueryForm(query));

		// perform query and return results as json
		logger.debug("getSummaryResultsByGene finished");

		return hdpFinder.getGridClusters(params);
	}

	public List<SolrVocTerm> getGridDiseaseColumns(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{

		logger.debug("getGridClusters disease column query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();

		params.setSorts(Arrays.asList(new Sort(SortConstants.VOC_TERM)));
		params.setPageSize(200); // I'm not sure we want to display more than this...
		params.setFilter(this.parseQueryForm(query));

		// perform query and return results as json
		logger.debug("getGridDiseaseColumns finished");
		SearchResults<SolrVocTerm> results = hdpFinder.getGridDiseases(params);

		return results.getResultObjects();
	}

	public List<String> getGridMpHeaderColumns(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		logger.debug("getGridClusters mp header column query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();

		params.setSorts(Arrays.asList(new Sort(SortConstants.VOC_TERM_HEADER)));
		params.setPageSize(200); // I'm not sure we want to display more than this...
		params.setFilter(this.parseQueryForm(query));

		// perform query and return results as json
		logger.debug("getGridMpHeaderColumns finished");
		SearchResults<String> results = hdpFinder.huntGridMPHeadersGroup(params);

		return results.getResultObjects();
	}

	public List<SolrVocTerm> getGridMpTermColumns(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		logger.debug("getGenoClusters mp term column query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();

		params.setSorts(Arrays.asList(new Sort(DiseasePortalFields.BY_TERM_DAG)));
		params.setPageSize(300); // I'm not sure we want to display more than this...
		//params.setFilter(this.parseSystemPopup(query));
		params.setFilter(this.parseQueryForm(query));
		
		// perform query and return results as json
		logger.debug("getGridMpTermColumns finished");
		SearchResults<SolrVocTerm> results = hdpFinder.huntGridMPTermsGroup(params);
		return results.getResultObjects();
	}

	public List<SolrDiseasePortalMarker> getGridHumanMarkers(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		logger.debug("getGridHumanMarkers query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();

		params.setSorts(Arrays.asList(new Sort(SortConstants.DP_BY_MRK_SYMBOL)));
		params.setPageSize(1000);
		//params.setFilter(this.parseSystemPopup(query));
		params.setFilter(this.parseQueryForm(query));

		
		// perform query and return results as json
		logger.debug("getGridHumanMarkers finished");
		SearchResults<SolrDiseasePortalMarker> results = hdpFinder.huntGridHumanMarkerGroup(params);


		return results.getResultObjects();
	}

	public SearchResults<SolrDiseasePortalMarker> getSummaryResultsByGene(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{
			return getSummaryResultsByGene(request,query,page,false);
	}
	public SearchResults<SolrDiseasePortalMarker> getSummaryResultsByGene(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page,
			boolean doHighlight)
	{

		logger.debug("getSummaryResults query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();
		params.setIncludeSetMeta(true);
		//params.setIncludeMetaHighlight(doHighlight);
		params.setIncludeRowMeta(true);
		params.setIncludeMetaScore(true);

		// determine and set the requested sorts, filters, and pagination
		params.setSorts(this.genMarkerSorts(request));
		params.setPaginator(page);
		params.setFilter(this.parseQueryForm(query));

		// perform query and return results as json
		logger.debug("getSummaryResultsByGene finished");

		return hdpFinder.getMarkers(params);
	}

	public SearchResults<SolrVocTerm> getSummaryResultsByDisease(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{
		return getSummaryResultsByDisease(request,query,page,false);
	}
	public SearchResults<SolrVocTerm> getSummaryResultsByDisease(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page,
			boolean doHighlight)
	{

		logger.debug("getSummaryResults query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();
		params.setIncludeSetMeta(true);
		//params.setIncludeMetaHighlight(doHighlight);
		params.setIncludeRowMeta(true);
		params.setIncludeMetaScore(true);
		List<Sort> sorts = new ArrayList<Sort>();

		//sorts.add(new Sort("score",true));
		//sorts.add(new Sort(DiseasePortalFields.TERM,false));
		params.setSorts(this.genDiseaseSorts(request));
		params.setPaginator(page);
		//params.setSorts(this.parseSorts(request));
		params.setFilter(this.parseQueryForm(query));

		// perform query and return results as json
		logger.debug("params parsed");

		return hdpFinder.getDiseases(params);
	}

	// NOTE: We are only using this function for testing MP query at the momemnt
	public SearchResults<SolrVocTerm> getSummaryResultsByPhenotype(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query,
			@ModelAttribute Paginator page)
	{

		logger.debug("getSummaryResultsByPhenotype query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();
		params.setIncludeSetMeta(true);
		//params.setIncludeMetaHighlight(true);
		params.setIncludeRowMeta(true);
		params.setIncludeMetaScore(true);
		List<Sort> sorts = new ArrayList<Sort>();

		sorts.add(new Sort("score",true));
		sorts.add(new Sort(DiseasePortalFields.TERM,false));
		params.setSorts(sorts);
		params.setPaginator(page);
		//params.setSorts(this.parseSorts(request));
		params.setFilter(this.parseQueryForm(query));

		// perform query and return results as json
		logger.debug("params parsed");

		return hdpFinder.getPhenotypes(params);
	}

	public SearchResults<HdpGenoCluster> getGenoClusters(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		logger.debug("getGenoClusters query: " + query.toString());

		// parse the various query parameter to generate SearchParams object
		SearchParams params = new SearchParams();
		params.setPageSize(5000); // if this is too low, you can change it, but damn that's a lot of genotypes

		// determine and set the requested sorts, filters, and pagination
//		params.setFilter(this.parseQueryForm(query));
		//params.setFilter(this.parseSystemPopup(query));
		params.setFilter(this.parseQueryForm(query));
		
		// perform query
		logger.debug("getSummaryResultsByGene finished");

		return hdpFinder.getGenoClusters(params);
	}

    //--------------------------------------------------------------------//
    // private methods
    //--------------------------------------------------------------------//

    // generate the sorts for the marker tab
    private List<Sort> genMarkerSorts(HttpServletRequest request) {

        logger.debug("->genMarkerSorts started");

        List<Sort> sorts = new ArrayList<Sort>();

        // first, deal with sort direction
        String dirRequested  = request.getParameter("dir");
        boolean desc = false;
        boolean asc = true;
        if("desc".equalsIgnoreCase(dirRequested)){desc = true; asc=false;}

        // retrieve requested sort order; set default if not supplied
        String sortRequested = request.getParameter("sort");
        if ("organism".equalsIgnoreCase(sortRequested))
        {
          sorts.add(new Sort(SortConstants.DP_BY_ORGANISM, asc));
          sorts.add(new Sort(SortConstants.DP_BY_MRK_SYMBOL, desc));
        }
        else if ("homologeneId".equalsIgnoreCase(sortRequested))
        {
          sorts.add(new Sort(SortConstants.DP_BY_HOMOLOGENE_ID, desc));
          sorts.add(new Sort(SortConstants.DP_BY_MRK_SYMBOL, desc));
        }
        else if ("symbol".equalsIgnoreCase(sortRequested))
        {
          sorts.add(new Sort(SortConstants.DP_BY_MRK_SYMBOL, desc));
        }
        else if ("type".equalsIgnoreCase(sortRequested))
        {
          sorts.add(new Sort(SortConstants.DP_BY_MRK_TYPE, desc));
          sorts.add(new Sort(SortConstants.DP_BY_MRK_SYMBOL, false));
        }
        else if ("location".equalsIgnoreCase(sortRequested))
        {
          sorts.add(new Sort(SortConstants.DP_BY_ORGANISM, asc));
          sorts.add(new Sort(SortConstants.DP_BY_LOCATION, desc));
        }
        else
        { // default sort
          sorts.add(new Sort(SortConstants.DP_BY_ORGANISM, true));
          sorts.add(new Sort(SortConstants.DP_BY_MRK_SYMBOL, false));
		}

        return sorts;
    }

 // generate the sorts for the diseases tab
    private List<Sort> genDiseaseSorts(HttpServletRequest request) {

        logger.debug("->genDiseaseSorts started");

        List<Sort> sorts = new ArrayList<Sort>();

        // first, deal with sort direction
        String dirRequested  = request.getParameter("dir");
        boolean desc = false;
        boolean asc = true;
        if("desc".equalsIgnoreCase(dirRequested)){desc = true; asc=false;}

        // retrieve requested sort order; set default if not supplied
        String sortRequested = request.getParameter("sort");
        if ("disease".equalsIgnoreCase(sortRequested))
        {
        	sorts.add(new Sort(SortConstants.VOC_TERM,desc));
        }
        else if ("diseaseId".equalsIgnoreCase(sortRequested))
        {
        	sorts.add(new Sort(SortConstants.VOC_TERM_ID,desc));
        }

        else
        { // default sort
        	//sorts.add(new Sort("score",true));
    		sorts.add(new Sort(SortConstants.VOC_TERM,false));
		}

        return sorts;
    }

	// parses the query parameters for popups
	private Filter parseSystemPopup(DiseasePortalQueryForm query)
	{
		//return parseQueryForm(query);
		List<Filter> qFilters = new ArrayList<Filter>();

        // grid cluster key
		String gridClusterKey = query.getGridClusterKey();
		if(notEmpty(gridClusterKey))
		{
			qFilters.add(new Filter(SearchConstants.DP_GRID_CLUSTER_KEY, gridClusterKey,Filter.OP_EQUAL));
		}

        // mpHeader
		String mpHeader = query.getMpHeader();
		if(notEmpty(mpHeader))
		{
			qFilters.add(new Filter(SearchConstants.MP_HEADER, mpHeader,Filter.OP_EQUAL));
		}

        // termId
		String termId = query.getTermId();
		if(notEmpty(termId))
		{
			qFilters.add(new Filter(SearchConstants.VOC_TERM_ID, termId,Filter.OP_EQUAL));
		}

		if(qFilters.size()>0)
		{
			return Filter.and(qFilters);
		}
		return new Filter(SearchConstants.PRIMARY_KEY,"###NONE###",Filter.OP_HAS_WORD);
	}


	// parses the query parameters pass into main queries
	private Filter parseQueryForm(DiseasePortalQueryForm query)
	{
		List<Filter> qFilters = new ArrayList<Filter>();
		
		// handle any filters
		String fGenes = query.getFGene();
		if(notEmpty(fGenes))
		{
			List<String> tokens = Arrays.asList(fGenes.split("\\|"));
			qFilters.add( new Filter(SearchConstants.DP_GRID_CLUSTER_KEY, tokens, Filter.OP_IN));
		}
		
		String fHeaders = query.getFHeader();
		if(notEmpty(fHeaders))
		{
			List<String> tokens = Arrays.asList(fHeaders.split("\\|"));
			qFilters.add( new Filter(DiseasePortalFields.TERM_HEADER, tokens, Filter.OP_IN));
		}

        // grid cluster key
		String gridClusterKey = query.getGridClusterKey();
		if(notEmpty(gridClusterKey))
		{
			qFilters.add( new Filter(SearchConstants.DP_GRID_CLUSTER_KEY, gridClusterKey, Filter.OP_EQUAL));
		}

        // mpHeader
		String mpHeader = query.getMpHeader();
		if(notEmpty(mpHeader))
		{
			qFilters.add(new Filter(SearchConstants.MP_HEADER, mpHeader,Filter.OP_EQUAL));
		}

		// termId
		String termId = query.getTermId();
		if(notEmpty(termId))
		{
			qFilters.add(new Filter(SearchConstants.VOC_TERM_ID, termId,Filter.OP_EQUAL));
		}

        // phenotype entry box
		String phenotypes = query.getPhenotypes();
		if(notEmpty(phenotypes))
		{
			Filter phenoFilter = generateNomenFilter(SearchConstants.VOC_TERM, phenotypes);
			if(phenoFilter != null) qFilters.add(phenoFilter);
		}

        // genes entry box
		String genes = query.getGenes();
		if(notEmpty(genes))
		{
			Filter genesFilter = generateNomenFilter(SearchConstants.MRK_NOMENCLATURE, genes);
			if(genesFilter != null) qFilters.add(genesFilter);
		}

        // location entry box
		String locations = query.getLocations();
		if(notEmpty(locations))
		{
			List<String> tokens = QueryParser.tokeniseOnWhitespaceAndComma(locations);
			List<Filter> locationFilters = new ArrayList<Filter>();
			//logger.debug("location tokens : "+StringUtils.join(tokens,","));
			for(String token : tokens)
			{
				String spatialQueryString = SolrLocationTranslator.getQueryValue(token);
				if(notEmpty(spatialQueryString))
				{
					// decide whether to query mouse or human
					String coordField = DiseasePortalQueryForm.HUMAN.equals(query.getOrganism())
							? SearchConstants.HUMAN_COORDINATE : SearchConstants.MOUSE_COORDINATE;
					locationFilters.add(new Filter(coordField,spatialQueryString,Filter.OP_HAS_WORD));
				}
			}

			if(locationFilters.size()>0) qFilters.add(Filter.or(locationFilters));
		}

		if(qFilters.size()>0)
		{
			return Filter.and(qFilters);
		}
		return new Filter(SearchConstants.PRIMARY_KEY,"###NONE###",Filter.OP_HAS_WORD);
	}

	private boolean notEmpty(String str)
	{ return str!=null && !str.equals(""); }

	//TODO: refactor to a common place
	private Filter generateNomenFilter(String property, String query){
		//logger.debug("splitting nomenclature query into tokens");
		Collection<String> nomens = QueryParser.parseNomenclatureSearch(query,false,"\"");
		Filter nomenFilter = new Filter();
		List<Filter> nomenFilters = new ArrayList<Filter>();
		// we want to group all non-wildcarded tokens into one solr phrase search
		List<String> nomenTokens = new ArrayList<String>();

		for(String nomen : nomens) {
			if(nomen.endsWith("*") || nomen.startsWith("*")) {
				nomenTokens.add(nomen);
			} else {
				// use a phrase slop search
				nomenTokens.add("\""+nomen+"\"~100");
			}
		}

		for(String nomenToken : nomenTokens) {
			//logger.debug("token="+nomenToken);
			Filter nFilter = new Filter(property, nomenToken,Filter.OP_HAS_WORD);
			nomenFilters.add(nFilter);
		}

		if(nomenFilters.size() > 0) {
			nomenFilter.setNestedFilters(nomenFilters,Filter.FC_OR);
			// add the nomenclature search filter
			return nomenFilter;
		}
		// We don't want to return an empty filter object, because it screws up Solr.
		return null;
	}

	// -----------------------------------------------------------------//
	// Methods for getting query counts
	// -----------------------------------------------------------------//

	@RequestMapping("/grid/totalCount")
	public @ResponseBody Integer getGridClusterCount(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		SearchParams params = new SearchParams();
		params.setFilter(this.parseQueryForm(query));
		params.setPageSize(0);
		return hdpFinder.getGridClusterCount(params);
	}
	@RequestMapping("/markers/totalCount")
	public @ResponseBody Integer getMarkerCount(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		SearchParams params = new SearchParams();
		params.setFilter(this.parseQueryForm(query));
		params.setPageSize(0);
		return hdpFinder.getMarkerCount(params);
	}
	@RequestMapping("/diseases/totalCount")
	public @ResponseBody Integer getDiseaseCount(
			HttpServletRequest request,
			@ModelAttribute DiseasePortalQueryForm query)
	{
		SearchParams params = new SearchParams();
		params.setFilter(this.parseQueryForm(query));
		params.setPageSize(0);
		return hdpFinder.getDiseaseCount(params);
	}

	// a class for mapping columns to data for making rows in the Grid
	public class GridMapper
	{
		private List<String> colIdList;
		private List<HdpGridAnnotation> gridAnnotations;
		private List<GridCell> gridCells = new ArrayList<GridCell>();

		public GridMapper(List<String> colIdList,List<? extends HdpGridAnnotation> gridAnnotations)
		{
			this.colIdList = colIdList;
			this.gridAnnotations = new ArrayList<HdpGridAnnotation>(gridAnnotations);
			init();
		}


		private void init()
		{
			// map annotations by term ID
			// keep two separate maps, one for abnormal annotations, one for normal
			Map<String,HdpGridAnnotation> annotationMap = new HashMap<String,HdpGridAnnotation>();
			Map<String,HdpGridAnnotation> normalMap = new HashMap<String,HdpGridAnnotation>();

			for(HdpGridAnnotation annot : gridAnnotations)
			{
				String annotId = annot.getTermIdentifier();
				if(annotationMap.containsKey(annotId)) continue;
				if(notEmpty(annot.getQualifier()))
				{
					normalMap.put(annotId,annot);
				}
				else
				{
					annotationMap.put(annotId,annot);
				}
			}

			// create cells by looking up if termId has an annotation in
			// the map; create blank cells for no-matches
			for(String colId : colIdList)
			{
				GridCell gc = new GridCell();

				HdpGridAnnotation annot=null;
				// get annotation summary if it exists for this cell
				if(annotationMap.containsKey(colId)) annot = annotationMap.get(colId);
				else if(normalMap.containsKey(colId))
				{
					// set normal if it is normal
					annot = normalMap.get(colId);
					gc.setIsNormal();
				}
				
				if(annot!=null)
				{
					//logger.info("colID="+colId+",term="+annot.getTerm()+",qual="+annot.getQualifier()+".");
					gc.setTerm(annot.getTerm());
					gc.setTermId(annot.getTermId());
					gc.setHasPopup();
					
					// set background note if it is a HdpGenoClusterAnnotation
					if(annot instanceof HdpGenoClusterAnnotation
							&& ((HdpGenoClusterAnnotation)annot).getHasBackgroundNote())
					{
						gc.setHasBackgroundNote();
					}
				}
				gridCells.add(gc);
			}
		}

		public List<GridCell> getGridCells()
		{
			return gridCells;
		}

		public class GridCell
		{
			private String term="";
			private String termId="";
			private Boolean isNormal = false;
			private Boolean hasPopup = false;
			private Boolean hasBackgroundNote = false;

			public String getTerm()
			{
				return term;
			}
			public void setTerm(String term)
			{
				this.term=term;
			}
			public String getTermId()
			{
				return termId;
			}
			public void setTermId(String termId)
			{
				this.termId=termId;
			}
			public void setIsNormal()
			{
				this.isNormal = true;
			}
			public Boolean getIsNormal()
			{
				return isNormal;
			}
			public void setHasPopup()
			{
				this.hasPopup = true;
			}
			public Boolean getHasPopup()
			{
				return hasPopup;
			}
			public void setHasBackgroundNote()
			{
				this.hasBackgroundNote = true;
			}
			public Boolean getHasBackgroundNote()
			{
				return hasBackgroundNote;
			}

			// encapsulate how we generate a display mark
			public String getDisplayMark()
			{
				if(getHasPopup())
				{
					if(getIsNormal()) return "N";
					return "&#8730;";
				}

				return "";
			}
			
		}

	}
}
