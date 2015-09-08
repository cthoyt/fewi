package org.jax.mgi.fewi.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import mgi.frontend.datamodel.Allele;
import mgi.frontend.datamodel.Disease;
import mgi.frontend.datamodel.Marker;
import mgi.frontend.datamodel.Reference;
import mgi.frontend.datamodel.Sequence;

import org.jax.mgi.fewi.finder.AlleleFinder;
import org.jax.mgi.fewi.finder.DiseaseFinder;
import org.jax.mgi.fewi.finder.MarkerFinder;
import org.jax.mgi.fewi.finder.ReferenceFinder;
import org.jax.mgi.fewi.finder.SequenceFinder;
import org.jax.mgi.fewi.forms.ReferenceQueryForm;
import org.jax.mgi.fewi.searchUtil.FacetConstants;
import org.jax.mgi.fewi.searchUtil.Filter;
import org.jax.mgi.fewi.searchUtil.MetaData;
import org.jax.mgi.fewi.searchUtil.Paginator;
import org.jax.mgi.fewi.searchUtil.SearchConstants;
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.jax.mgi.fewi.searchUtil.Sort;
import org.jax.mgi.fewi.searchUtil.SortConstants;
import org.jax.mgi.fewi.summary.JsonSummaryResponse;
import org.jax.mgi.fewi.summary.ReferenceSummary;
import org.jax.mgi.fewi.util.Highlighter;
import org.jax.mgi.shr.fe.IndexConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/*
 * This controller maps all /reference/ uri's
 */
@Controller
@RequestMapping(value="/reference")
public class ReferenceController {
	
	// logger for the class
	private final Logger logger = LoggerFactory.getLogger(ReferenceController.class);
	
	// get the finders used by various methods
	@Autowired
	private ReferenceFinder referenceFinder;
	
	@Autowired
	private SequenceFinder sequenceFinder;
	
	@Autowired
	private MarkerFinder markerFinder;
	
	@Autowired
	private DiseaseFinder diseaseFinder;
	
	@Autowired
	private AlleleFinder alleleFinder;
	
    @Value("${solr.factetNumberDefault}")
    private Integer facetLimit; 
	
	// add a new ReferenceQueryForm and MySirtPaginator objects to model for QF 
	@RequestMapping(method=RequestMethod.GET)
	public String getQueryForm(Model model) {
		model.addAttribute(new ReferenceQueryForm());
		model.addAttribute("sort", new Paginator());
		return "reference_query";
	}
	
	/* 
	 * This method maps requests for the reference summary view. Note that this 
	 * method does not process the actual query, but rather maps the request
	 * to the apropriate view and returns any Model objects needed by the
	 * view. The view is responsible for issuing the ajax query that will
	 * return the results to populate the data table.
	 */
	@RequestMapping("/summary")
	public String referenceSummary(HttpServletRequest request, Model model,
			@ModelAttribute ReferenceQueryForm queryForm,
			BindingResult result) {

		model.addAttribute("referenceQueryForm", queryForm);
		parseReferenceQueryForm(queryForm, result);
		
		if (result.hasErrors()) {
			return "reference_query";
		}
		
		model.addAttribute("queryString", request.getQueryString());		

		String text = request.getParameter("text");
		String sort = "year";
		if (text != null && !"".equals(text)){
			sort = "score";
		}
		
		model.addAttribute("defaultSort", sort);
		logger.debug("queryString: " + request.getQueryString());

		return "reference_summary";
	}
	
	/* 
	 * This method maps ajax requests from the reference summary page.  It 
	 * parses the ReferenceQueryForm, generates SearchParams object, and issues
	 * the query to the ReferenceFinder.  The results are returned as JSON
	 */
	@RequestMapping("/report*")
	public String referenceSummaryReport(
			HttpServletRequest request, Model model,
			@ModelAttribute ReferenceQueryForm query,
			@ModelAttribute Paginator page,
			BindingResult result) {
				
		logger.debug("summaryReport");		
		SearchResults<Reference> searchResults;
		try {
			searchResults = this.getSummaryResults(request, query, page, result);
	        model.addAttribute("results", searchResults.getResultObjects());
			return "referenceSummaryReport";			
		} catch (BindException be) {
			logger.debug("bind error");
			return "reference_query";
		}
	}
	
	/* 
	 * This method maps ajax requests from the reference summary page.  It 
	 * parses the ReferenceQueryForm, generates SearchParams object, and issues
	 * the query to the ReferenceFinder.  The results are returned as JSON
	 */
	@RequestMapping("/json")
	public @ResponseBody JsonSummaryResponse<ReferenceSummary> referenceSummaryJson(
			HttpServletRequest request, 
			@ModelAttribute ReferenceQueryForm query,
			@ModelAttribute Paginator page,
			BindingResult result) throws BindException {

		SearchResults<Reference> searchResults = this.getSummaryResults(request, query, page, result);	
        List<Reference> refList = searchResults.getResultObjects();

        Map<String, Set<String>> highlighting = searchResults.getResultSetMeta().getSetHighlights();
        Set<String> textHl = new HashSet<String>(), authorHL = new HashSet<String>();
        if (highlighting != null){
	        if (query.getAuthor() != null && !"".equals(query.getAuthor())){	
	        	if (highlighting.containsKey(SearchConstants.REF_AUTHOR)){
					for (String auth: highlighting.get(SearchConstants.REF_AUTHOR)) {
						authorHL.add(auth.replace(" ", "[\\s\\-']"));
					}
	        	}

	        }
	        if (query.getText() != null && !"".equals(query.getText())){
				if (query.isInAbstract()){
					if (highlighting.containsKey(SearchConstants.REF_TEXT_TITLE_ABSTRACT)){
						textHl = highlighting.get(SearchConstants.REF_TEXT_TITLE_ABSTRACT);
					} else if (highlighting.containsKey(SearchConstants.REF_TEXT_ABSTRACT)){
						textHl= highlighting.get(SearchConstants.REF_TEXT_ABSTRACT);
					}
				}
				if (query.isInTitle()){
					if (highlighting.containsKey(SearchConstants.REF_TEXT_TITLE_ABSTRACT)){
						textHl = highlighting.get(SearchConstants.REF_TEXT_TITLE_ABSTRACT);
					} else if (highlighting.containsKey(SearchConstants.REF_TEXT_TITLE)){
						textHl= highlighting.get(SearchConstants.REF_TEXT_TITLE);
					}
				}
	        }
        }
        List<ReferenceSummary> summaryRows = new ArrayList<ReferenceSummary>();
        ReferenceSummary row;
        MetaData rowMeta;

        for (Reference ref : refList) {
			if (ref != null){
				row = new ReferenceSummary(ref);
				rowMeta = searchResults.getMetaMapping().get(String.valueOf(ref.getReferenceKey()));
				if (rowMeta != null){
					row.setScore(rowMeta.getScore());
				} else {
					row.setScore("0");
				}
				if (query.isInAbstract()){
					row.setAbstractHL(new Highlighter(textHl));
				}
				if (query.isInTitle()){
					row.setTitleHL(new Highlighter(textHl));
				}
				if (query.getAuthor() != null && !"".equals(query.getAuthor())){					
					row.setAuthorHL(new Highlighter(authorHL));
				}
				summaryRows.add(row);
			} else {
				logger.debug("--> Null Object");
			}
		}
        JsonSummaryResponse<ReferenceSummary> jsonResponse
        		= new JsonSummaryResponse<ReferenceSummary>();
        jsonResponse.setSummaryRows(summaryRows);       
        jsonResponse.setTotalCount(searchResults.getTotalCount());
        
        return jsonResponse;
	}
	
	private SearchResults<Reference> getSummaryResults(
			HttpServletRequest request, 
			@ModelAttribute ReferenceQueryForm query,
			@ModelAttribute Paginator page,
			BindingResult result) throws BindException{
		
		logger.debug("getSummaryResults query: " + query.toString());
		
		String refKey = query.getKey();
		
		if (refKey != null && !"".equalsIgnoreCase(refKey)) {
			return  referenceFinder.getReferenceByKey(refKey);
		} else {
			// parse the various query parameter to generate SearchParams object
			SearchParams params = new SearchParams();
			params.setIncludeSetMeta(true);
			params.setIncludeMetaHighlight(true);
			params.setIncludeRowMeta(true);
			params.setIncludeMetaScore(true);
			params.setPaginator(page);		
			params.setSorts(this.parseSorts(request));
			params.setFilter(this.parseReferenceQueryForm(query, result));
			
			// perform query and return results as json
			logger.debug("params parsed");		
			
			if (result.hasErrors()){
				logger.debug("bind error");
				throw new BindException(result);
			} else {
				return referenceFinder.searchSummaryReferences(params);
			}
		}
	}
	
	@RequestMapping("/key")
	public String referenceByKeyParam( HttpServletRequest request, Model model) {		
		String query = request.getQueryString();
		logger.info("referenceByKeyParam: " + query);
		model.addAttribute("queryString", "key=" + query);		
		return "reference_detail";		
	}
	
	@RequestMapping("/key/{refKey}")
	public String referenceByKey(@PathVariable("refKey") String refKey, 
			Model model) {		
		logger.info("referenceByKey: " + refKey);
		model.addAttribute("queryString", "key=" + refKey);		
		return "reference_detail";		
	}
	
	/*
	 * This method maps requests for a reference detail page by id.
	 */
	@RequestMapping("/{refID}")
	public String referenceById(@PathVariable("refID") String refID, 
			Model model) {
		logger.info("ref by id: " + refID);
		model.addAttribute("queryString", "id=" + refID);		
		return "reference_detail";		
	}
	

	
	/* 
	 * This method maps requests for the reference summary for an allele.  
	 * Note that this method does not process the actual query, but rather maps
	 * the request to the apropriate view and returns any Model objects needed
	 * by the view.  The view is responsible for issuing the ajax query that 
	 * will return the results to populate the data table.
	 */
	@RequestMapping("/allele/{alleleID}")
	public ModelAndView referenceSummaryByAlleleId(			
			@PathVariable("alleleID") String alleleID,
			@ModelAttribute ReferenceQueryForm queryForm,
			HttpServletRequest request, Model model) {		
		
        // setup search parameters object
        SearchParams searchParams = new SearchParams();
        Filter seqIdFilter = new Filter(SearchConstants.ALL_ID, alleleID);
        searchParams.setFilter(seqIdFilter);

        // find the requested sequence
        SearchResults<Allele> searchResults
          = alleleFinder.getAlleleByID(searchParams);

        return referenceSummaryByAllele(searchResults.getResultObjects(), alleleID, queryForm);
	}
	
    @RequestMapping(value="/summary",  params={"_Allele_key"})
    public ModelAndView referenceSummaryByAlleleKey(@RequestParam("_Allele_key") String alleleKey,
		@ModelAttribute ReferenceQueryForm queryForm) {
        logger.debug("->referenceSummaryByAlleleKey started: " + alleleKey);
       
        // find the requested reference
        SearchResults<Allele> searchResults
        	= alleleFinder.getAlleleByKey(alleleKey);

        return referenceSummaryByAllele(searchResults.getResultObjects(), alleleKey, queryForm);
    }
    
    private ModelAndView referenceSummaryByAllele(List<Allele> alleleList, String allele, ReferenceQueryForm query){
    	ModelAndView mav = new ModelAndView("reference_summary_allele");
    	
        if (alleleList.size() < 1) {
            // forward to error page
            mav = new ModelAndView("error");
            mav.addObject("errorMsg", "No Allele Found");
            return mav;
        } else if (alleleList.size() > 1) {
            // forward to error page
            mav = new ModelAndView("error");
            mav.addObject("errorMsg", "Duplicate ID");
            return mav;
        }
        
        mav.addObject("allele", alleleList.get(0));
	mav.addObject("queryString", "alleleKey=" + alleleList.get(0).getAlleleKey());
	addFieldsFromQF(mav, query);
    	
    	return mav;   	
    }
	
	/*
	 * This method maps requests for the reference summary for a sequence. 
	 * Note that this method does not process the actual query, but rather maps
	 * the request to the apropriate view and returns any Model objects needed
	 * by the view.  The view is responsible for issuing the ajax query that 
	 * will return the results to populate the data table.
	 */
	@RequestMapping("/sequence/{seqID}")
	public String referenceSummaryForSequence(
			@PathVariable("seqID") String seqID,
			HttpServletRequest request, Model model) {
		
		logger.debug("reference_summary_sequence");
		
        // setup search parameters object
        SearchParams searchParams = new SearchParams();
        Filter seqIdFilter = new Filter(SearchConstants.SEQ_ID, seqID);
        searchParams.setFilter(seqIdFilter);

        // find the requested sequence
        SearchResults<Sequence> searchResults
          = sequenceFinder.getSequenceByID(searchParams);
        
        List<Sequence> seqList = searchResults.getResultObjects();

        if (seqList.size() < 1) {
            // forward to error page
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("errorMsg", "No Sequence Found");
            return "error";
        } else if (seqList.size() > 1) {
            // forward to error page
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("errorMsg", "Duplicate ID");
            return "error";
        }

        model.addAttribute("sequence", seqList.get(0));
		model.addAttribute("queryString", "seqKey=" + seqList.get(0).getSequenceKey());
		
		return "reference_summary_sequence";
	}

	/* references for GO annotations for a marker
	 */
	@RequestMapping("/go/marker/{mrkID}")
	public ModelAndView goReferencesByMarkerId (
		@PathVariable("mrkID") String mrkID,
		HttpServletRequest request, Model model) {		

	    logger.debug("->goReferencesByMarkerId started: " + mrkID);

	    // find the requested marker
            List<Marker> markerList = markerFinder.getMarkerByPrimaryId(mrkID);

            ModelAndView mav = new ModelAndView("reference_summary_marker");
        
            if (markerList.size() < 1) {
		// forward to error page
		mav = new ModelAndView("error");
		mav.addObject("errorMsg", "No Marker Found");
		return mav;
	    } else if (markerList.size() > 1) {
		// forward to error page
		mav = new ModelAndView("error");
		mav.addObject("errorMsg", "Duplicate ID");
		return mav;
	    }
        
	    mav.addObject("marker", markerList.get(0));
                
            // pre-generate query string
            mav.addObject("queryString", "goMarkerId=" + mrkID);
            mav.addObject("isGOSummary", true);
    	
	    return mav;   	
	}

	/* references for alleles of a marker
	 */
	@RequestMapping("/phenotype/marker/{mrkID}")
	public ModelAndView phenoReferencesByMarkerId (
		@PathVariable("mrkID") String mrkID,
		HttpServletRequest request, Model model) {		

	    logger.debug("->phenoReferencesByMarkerId started: " + mrkID);

	    // find the requested marker
            List<Marker> markerList = markerFinder.getMarkerByPrimaryId(mrkID);

            ModelAndView mav = new ModelAndView("reference_summary_marker");
        
            if (markerList.size() < 1) {
		// forward to error page
		mav = new ModelAndView("error");
		mav.addObject("errorMsg", "No Marker Found");
		return mav;
	    } else if (markerList.size() > 1) {
		// forward to error page
		mav = new ModelAndView("error");
		mav.addObject("errorMsg", "Duplicate ID");
		return mav;
	    }
        
	    mav.addObject("marker", markerList.get(0));
                
            // pre-generate query string
            mav.addObject("queryString", "phenoMarkerId=" + mrkID);
            mav.addObject("isPhenotypeSummary", true);
    	
	    return mav;   	
	}

	/*
	 * This method maps requests for the reference summary for a marker.
	 * Note that this method does not process the actual query, but rather maps
	 * the request to the apropriate view and returns any Model objects needed
	 * by the view.  The view is responsible for issuing the ajax query that 
	 * will return the results to populate the data table.
	 */
	@RequestMapping("/marker/{markerID:[A-Za-z0-9\\:_]+}")
	public ModelAndView referenceSummaryByMarkerId(
			@PathVariable("markerID") String markerID,
			@ModelAttribute ReferenceQueryForm queryForm,
			HttpServletRequest request, Model model) {		
		logger.debug("->referenceSummaryByMarkerId started: " + markerID);
		
        // setup search parameters object
        SearchParams searchParams = new SearchParams();
        Filter markerIdFilter = new Filter(SearchConstants.MRK_ID, markerID);
        searchParams.setFilter(markerIdFilter);

        // find the requested sequence
        SearchResults<Marker> searchResults
          = markerFinder.getMarkerByID(searchParams);

        return referenceSummaryByMarker(searchResults.getResultObjects(), markerID, queryForm);
	}
	
    @RequestMapping(value="/summary",  params={"_Marker_key"})
    public ModelAndView referenceSummaryByMarkerKey(@RequestParam("_Marker_key") String markerKey,
	@ModelAttribute ReferenceQueryForm queryForm) {

        logger.debug("->referenceSummaryByMarkerKey started: " + markerKey);

        // find the requested reference
        SearchResults<Marker> searchResults
          = markerFinder.getMarkerByKey(markerKey);
        List<Marker> markerList = searchResults.getResultObjects();

        return referenceSummaryByMarker( markerList, markerKey, queryForm);
    }
    
    /* add a subset of fields from the query form to the mav
     */
    private ModelAndView addFieldsFromQF(ModelAndView mav,
	ReferenceQueryForm query) {
	    List<String> typeFilters = query.getCleanedTypeFilter();

	    // assumes we only filter by one value
	    if ((typeFilters != null) && (typeFilters.size() > 0)) {
		mav.addObject("typeFilter", typeFilters.get(0));
	    }
	    return mav;
    }

    private ModelAndView referenceSummaryByMarker(List<Marker> markerList, String markerKey, ReferenceQueryForm query){
    	ModelAndView mav = new ModelAndView("reference_summary_marker");
    	
        if (markerList.size() < 1) {
            // forward to error page
            mav = new ModelAndView("error");
            mav.addObject("errorMsg", "No Marker Found");
            return mav;
        } else if (markerList.size() > 1) {
            // forward to error page
            mav = new ModelAndView("error");
            mav.addObject("errorMsg", "Duplicate ID");
            return mav;
        }
        
        Marker marker = markerList.get(0);
        mav.addObject("marker", marker);
                
        // pre-generate query string
        mav.addObject("queryString", "markerKey=" + marker.getMarkerKey());

	addFieldsFromQF(mav, query);

//        mav.addObject("queryString", "markerKey=" + marker.getMarkerKey()
//		+ query.getUrlFragment());
    	
    	return mav;   	
    }
    
    @RequestMapping("/diseaseRelevantMarker/{markerID}")
	public ModelAndView referenceSummaryByMarkerIdDiseaseRelevant(
			@PathVariable("markerID") String markerID,
			HttpServletRequest request, Model model) {		
		logger.debug("->referenceSummaryByMarkerIdDiseaseRelevant started: " + markerID);
		
        // find the requested marker
        List<Marker> markerList = markerFinder.getMarkerByPrimaryId(markerID);

        ModelAndView mav = new ModelAndView("reference_summary_marker");
        
        if (markerList.size() < 1) {
            // forward to error page
            mav = new ModelAndView("error");
            mav.addObject("errorMsg", "No Marker Found");
            return mav;
        } else if (markerList.size() > 1) {
            // forward to error page
            mav = new ModelAndView("error");
            mav.addObject("errorMsg", "Duplicate ID");
            return mav;
        }
        
        mav.addObject("marker", markerList.get(0));
                
        // pre-generate query string
        mav.addObject("queryString", "diseaseRelevantMarkerId=" + markerID);
        mav.addObject("isDiseaseRelevantSummary", true);
    	
    	return mav;   	
	}
    
    @RequestMapping("/disease/{diseaseID}")
   	public ModelAndView referenceSummaryByDiseaseID(
   			@PathVariable("diseaseID") String diseaseID,
   			HttpServletRequest request, Model model) {		
   		logger.debug("->referenceSummaryByDiseaseID started: " + diseaseID);
   		
   		List<Disease> diseaseList = diseaseFinder.getDiseaseByID(diseaseID);

           ModelAndView mav = new ModelAndView("reference_summary_disease");
           
           if (diseaseList.size() < 1) {
               // forward to error page
               mav = new ModelAndView("error");
               mav.addObject("errorMsg", "No Disease Found");
               return mav;
           } else if (diseaseList.size() > 1) {
               // forward to error page
               mav = new ModelAndView("error");
               mav.addObject("errorMsg", "Duplicate ID");
               return mav;
           }
           
           mav.addObject("disease", diseaseList.get(0));
                   
           // pre-generate query string
           mav.addObject("queryString", "diseaseId=" + diseaseID);
       	
       	return mav;   	
   	}
	
	/*
	 * This method parses the ReferenceQueryForm bean and constructs a Filter 
	 * object to represent the query.  
	 */
	private Filter parseReferenceQueryForm(ReferenceQueryForm query, BindingResult result){
		// start filter list for query filters
		List<Filter> queryList = new ArrayList<Filter>();
		// start filter list to store facet filters
		List<Filter> facetList = new ArrayList<Filter>();
		
		logger.debug("get params");
		
		// process normal query form parameter.  the resulting filter objects
		// are added to queryList.  
		
		// build diseaseRelevantMarkerId query
		String diseaseRelevantMarkerId = query.getDiseaseRelevantMarkerId();
		if(diseaseRelevantMarkerId != null && !diseaseRelevantMarkerId.equals(""))
		{
			queryList.add(new Filter(IndexConstants.REF_DISEASE_RELEVANT_MARKER_ID,
					diseaseRelevantMarkerId,Filter.Operator.OP_EQUAL));
		}
		
		String diseaseId = query.getDiseaseId();
		if(diseaseId != null && !diseaseId.equals(""))
		{
			queryList.add(new Filter(IndexConstants.REF_DISEASE_ID,diseaseId,Filter.Operator.OP_EQUAL));
		}
		
		// search for references used for a marker's GO annotations
		String goMarkerId = query.getGoMarkerId();
		if (goMarkerId != null && !goMarkerId.equals("")) {
			queryList.add(new Filter(IndexConstants.REF_GO_MARKER_ID, goMarkerId, Filter.Operator.OP_EQUAL));
		}

		// search for references of a marker's alleles
		String phenoMarkerId = query.getPhenoMarkerId();
		if (phenoMarkerId != null && !phenoMarkerId.equals("")) {
			queryList.add(new Filter(IndexConstants.REF_PHENO_MARKER_ID, phenoMarkerId, Filter.Operator.OP_EQUAL));
		}

		//build author query filter
		String authorText = query.getAuthor().trim();
		if(authorText != null && !"".equals(authorText)){

			List<String> authors = this.parseList(authorText, ";");

			String scope = query.getAuthorScope();
			
			if ("first".equals(scope)){
				queryList.add(new Filter(SearchConstants.REF_AUTHOR_FIRST, 
						authors, Filter.Operator.OP_IN));
			} else if ("last".equals(scope)){
				queryList.add(new Filter(SearchConstants.REF_AUTHOR_LAST, 
						authors, Filter.Operator.OP_IN));
			} else {
				queryList.add(new Filter(SearchConstants.REF_AUTHOR_ANY, 
						authors, Filter.Operator.OP_IN));
			}
		}

		// build journal query filter
		String journalText = query.getJournal().trim();
		if(journalText != null && !"".equals(journalText)){
			
			List<String> journals = this.parseList(journalText, ";");

			queryList.add(new Filter(SearchConstants.REF_JOURNAL, 
					journals, Filter.Operator.OP_IN));
		}
		
		// build year query filter
		String year = query.getYear().trim();
		Integer minYear = new Integer(1800);
		Integer maxYear = Calendar.getInstance().get(Calendar.YEAR);
		if(year != null && !"".equals(year)){
			int rangeLoc = year.indexOf("-");
			if(rangeLoc > -1){
				List<String> years = this.parseList(year, "-");
				if (years.size() > 2){
					result.addError(
							new FieldError("referenceQueryForm", 
									"year", 
									"* Invalid year range"));
				} else if (years.size() == 2){
					logger.debug("year range: " + years.get(0) + "-" + years.get(1));
					try{
						Integer one = new Integer(years.get(0));
						Integer two = new Integer(years.get(1));
						
						if ( (one < minYear || one > maxYear) || 
								(two < minYear || two > maxYear) ) {
							result.addError(
									new FieldError("referenceQueryForm", 
											"year", 
											"* Invalid year"));
						}
						
						if (one > two){
							years.set(0, two.toString());
							years.set(1, one.toString());
						}
						queryList.add(new Filter(SearchConstants.REF_YEAR, 
								years.get(0), Filter.Operator.OP_GREATER_OR_EQUAL));
						queryList.add(new Filter(SearchConstants.REF_YEAR, 
								years.get(1), Filter.Operator.OP_LESS_OR_EQUAL));
					} catch (NumberFormatException nfe){
						result.addError(
								new FieldError("referenceQueryForm", 
										"year", 
										"* Invalid year"));
					}
				} else {
					Integer y = new Integer(years.get(0));
					if ( (y < minYear || y > maxYear) ) {
						result.addError(
								new FieldError("referenceQueryForm", 
										"year", 
										"* Invalid year"));
					} else {
						if (rangeLoc == 0){
							queryList.add(new Filter(SearchConstants.REF_YEAR, 
									years.get(0), Filter.Operator.OP_LESS_OR_EQUAL));
						} else {
							queryList.add(new Filter(SearchConstants.REF_YEAR, 
									years.get(0), Filter.Operator.OP_GREATER_OR_EQUAL));
						}
					}
				}
			} else {
				try{
					// only used to validate number format
					Integer one = new Integer(year);
					
					if (one < minYear || one > maxYear) {
						result.addError(
								new FieldError("referenceQueryForm", 
										"year", 
										"* Invalid year"));
					}
					
					queryList.add(new Filter(SearchConstants.REF_YEAR, 
							year, Filter.Operator.OP_EQUAL));
				} catch (NumberFormatException nfe){
					result.addError(
							new FieldError("referenceQueryForm", 
									"year", 
									"* Invalid year"));
				}
			}
		}
		
		// build text query filter
		String textField = query.getText().trim();
		if(textField != null && !"".equals(textField)){
			Filter tf = new Filter();
			List<Filter> textFilters = new ArrayList<Filter>();

			if(query.isInAbstract()){
				textFilters.add(new Filter(SearchConstants.REF_TEXT_ABSTRACT, 
						textField, Filter.Operator.OP_CONTAINS));
			}
			if(query.isInTitle()){
				textFilters.add(new Filter(SearchConstants.REF_TEXT_TITLE, 
						textField, Filter.Operator.OP_CONTAINS));
			}
			if (textFilters.size() == 1) {
				queryList.add(textFilters.get(0));
			} else {
				tf.setFilterJoinClause(Filter.JoinClause.FC_OR);
				tf.setNestedFilters(textFilters);
				queryList.add(tf);
			}
		}
		
		// process the summary for a parent object (allele, sequence) params.
		// these are added to queryList just like any regular query form param.
		
		// build sequence key query filter
		if (query.getSeqKey() != null){
			logger.info("set seqKey filter");
			queryList.add(new Filter(SearchConstants.SEQ_KEY, 
					query.getSeqKey().toString(), Filter.Operator.OP_EQUAL));
		}
		
		// build allele key query filter
		if (query.getAlleleKey() != null){
			logger.info("set alleleKey filter");
			queryList.add(new Filter(SearchConstants.ALL_KEY, 
					query.getAlleleKey().toString(), Filter.Operator.OP_EQUAL));
		}
		
		// build allele key query filter
		if (query.getMarkerKey() != null){
			logger.info("set alleleKey filter");
			queryList.add(new Filter(SearchConstants.MRK_KEY, 
					query.getMarkerKey().toString(), Filter.Operator.OP_EQUAL));
		}
		
		// process facet filters.  these filters are added to facetList as they 
		// should not be considered when validating the actual query.
		logger.debug("get filters");
		
		// build author facet query filter
		if(query.getAuthorFilter().size() > 0){
			facetList.add(new Filter(FacetConstants.REF_AUTHORS, 
					query.getAuthorFilter(), Filter.Operator.OP_IN));
		}
		// build type/grouping (literature vs. non-literature) facet query filter
		if(query.getCleanedTypeFilter().size() > 0){
			facetList.add(new Filter(FacetConstants.REF_GROUPING, 
				query.getCleanedTypeFilter(), Filter.Operator.OP_IN));
		}
		// build journal facet query filter
		if (query.getJournalFilter().size() > 0){
			facetList.add(new Filter(FacetConstants.REF_JOURNALS, 
					query.getJournalFilter(), Filter.Operator.OP_IN));
		}
		// build year facet query filter
		if (query.getYearFilter().size() > 0){
			List<String> years = new ArrayList<String>();
			for (Integer yearString : query.getYearFilter()) {
				years.add(String.valueOf(yearString));
			}
			facetList.add(new Filter(FacetConstants.REF_YEAR, 
					years, Filter.Operator.OP_IN));
		}
		// build curated data facet query filter
		if (query.getDataFilter().size() > 0){
			List<String> selections = new ArrayList<String>();
			for (String filter : query.getDataFilter()) {
				logger.debug(filter.replaceAll("\\*", ","));
				selections.add(filter.replaceAll("\\*", ","));
			}
			facetList.add(new Filter(FacetConstants.REF_CURATED_DATA, 
					selections, Filter.Operator.OP_IN));
		}
		
		logger.debug("build params");
		
		List<String> ids = new ArrayList<String>();
		String idtext = query.getId().trim();
		if (idtext != null && !"".equals(idtext)){
			ids = this.parseList(idtext, "[;,\\s]");
		}
		
		if (queryList.size() > 0){
			if (ids.size() > 0){
				result.addError(
						new FieldError("referenceQueryForm", 
								"id", 
								"* Invalid with other parameters"));
			} else {
				Filter f = new Filter();
				f.setFilterJoinClause(Filter.JoinClause.FC_AND);
				queryList.addAll(facetList);
				f.setNestedFilters(queryList);
				return f;
			}
		// none yet, so check the id query and build it
		} else if (ids.size() > 0){			
			List<String> cleanIds = new ArrayList<String>();
			for (String id : ids) {
				if (id.toLowerCase().startsWith("pmid:")){
					cleanIds.add(id.substring(5));
				} else {
					cleanIds.add(id);
				}
			}

			facetList.add(new Filter(SearchConstants.REF_ID, 
					cleanIds, Filter.Operator.OP_IN));
			Filter f = new Filter();
			f.setFilterJoinClause(Filter.JoinClause.FC_AND);
			f.setNestedFilters(facetList);
			return f;
		} else {
			result.addError(
				new ObjectError("referenceQueryForm", 
						"* Please enter some search parameters"));
		}
		
		return new Filter();
	}
	
	/*
	 * This is a helper method to parse sort parameters from the query string
	 * and return a Sort object.  
	 */
	private List<Sort> parseSorts(HttpServletRequest request) {
		
		List<Sort> sorts = new ArrayList<Sort>();
		
		String s = request.getParameter("sort");
		String d = request.getParameter("dir");
		boolean desc = false;
		
		if ("authors".equalsIgnoreCase(s)){
			s = SortConstants.REF_AUTHORS;
		} else if ("journal".equalsIgnoreCase(s)){
			s = SortConstants.REF_JOURNAL;
		} else if ("score".equalsIgnoreCase(s)){
			s = "score";
		} else {
			s = SortConstants.REF_YEAR;
		}
		
		if("desc".equalsIgnoreCase(d)){
			desc = true;
		}
		
		logger.debug("sort: " + s + " " + d);
		Sort sort = new Sort(s, desc);
		
		sorts.add(sort);
		return sorts;
	}
	
	private List<String> parseList(String list, String delimiter){
		String parsed[] = list.split(delimiter);
		List<String> items = new ArrayList<String>();
		String item;
		for (int i = 0; i < parsed.length; i++) {
			item = parsed[i].trim();
			if (item != null && !"".equals(item) ){
				items.add(item);
			}
		}
		return items;
	}
	
	/*
	 * This method maps requests for the author facet list.  The results are
	 * returned as JSON.  
	 */
	@RequestMapping("/facet/author")
	public @ResponseBody Map<String, List<String>> facetAuthor(
			@ModelAttribute ReferenceQueryForm query,
			BindingResult result) {
			
		logger.debug(query.toString());
		
		SearchParams params = new SearchParams();
		params.setFilter(this.parseReferenceQueryForm(query, result));
	
		// perform query and return results as json
		logger.debug("params parsed");

		return this.parseFacetResponse(referenceFinder.getAuthorFacet(params));
	}
	
	/*
	 * This method maps requests for the type/grouping (literature vs. non-literature) facet list.  The results are returned as JSON.  
	 */
	@RequestMapping("/facet/type")
	public @ResponseBody Map<String, List<String>> facetType(
			@ModelAttribute ReferenceQueryForm query,
			BindingResult result) {
			
		logger.debug(query.toString());
		
		SearchParams params = new SearchParams();
		params.setFilter(this.parseReferenceQueryForm(query, result));
	
		// perform query and return results as json
		logger.debug("params parsed");

		Map<String, List<String>> response = this.parseFacetResponse(referenceFinder.getTypeFacet(params));

		if (response.containsKey("resultFacets")) {
			Collections.reverse(response.get("resultFacets"));
		}
		return response;
	}
	
	/*
	 * This method maps requests for the journal facet list.  The results are
	 * returned as JSON.  
	 */
	@RequestMapping("/facet/journal")
	public @ResponseBody Map<String, List<String>> facetJournal(
			@ModelAttribute ReferenceQueryForm query,
			BindingResult result) {
			
		logger.debug(query.toString());
		
		SearchParams params = new SearchParams();		
		params.setFilter(this.parseReferenceQueryForm(query, result));
	
		// perform query and return results as json
		logger.debug("params parsed");
		return this.parseFacetResponse(referenceFinder.getJournalFacet(params));
	}
	
	/*
	 * This method maps requests for the year facet list.  The results are
	 * returned as JSON.  
	 */
	@RequestMapping("/facet/year")
	public @ResponseBody Map<String, List<String>> facetYear(
			@ModelAttribute ReferenceQueryForm query,
			BindingResult result) {
			
		logger.debug(query.toString());
		
		SearchParams params = new SearchParams();		
		params.setFilter(this.parseReferenceQueryForm(query, result));
	
		// perform query and return results as json
		logger.debug("params parsed");
		return this.parseFacetResponse(referenceFinder.getYearFacet(params));
	}
	
	/*
	 * This method maps requests for the curated data  facet list.  The results 
	 * are returned as JSON.  
	 */
	@RequestMapping("/facet/data")
	public @ResponseBody Map<String, List<String>> facetData(
			@ModelAttribute ReferenceQueryForm query,
			BindingResult result) {
			
		logger.debug(query.toString());
		
		SearchParams params = new SearchParams();		
		params.setFilter(this.parseReferenceQueryForm(query, result));
	
		// perform query and return results as json
		logger.debug("params parsed");
		return this.parseFacetResponse(referenceFinder.getDataFacet(params));
	}
	
	private Map<String, List<String>> parseFacetResponse(
			SearchResults<String> facetResults) {
		
		Map<String, List<String>> m = new HashMap<String, List<String>>();
		List<String> l = new ArrayList<String>();
		
		if (facetResults.getSortedResultFacets().size() >= facetLimit){
			logger.debug("too many facet results");
			l.add("Too many results to display. Modify your search or try another filter first.");
			m.put("error", l);
		} else if (facetResults.getSortedResultFacets().size() == 0) {
			logger.debug("no facet results");
			l.add("No values in results to filter.");
			m.put("error", l);
		} else {
			m.put("resultFacets", facetResults.getSortedResultFacets());
		}
		return m;
	}
}
