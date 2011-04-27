package org.jax.mgi.fewi.controller;

import java.util.*;

/*------------------------------*/
/* to change in each controller */
/*------------------------------*/

// fewi
import org.jax.mgi.fewi.finder.AccessionFinder;
import org.jax.mgi.fewi.finder.ReferenceFinder;
import org.jax.mgi.fewi.forms.FooQueryForm;
import org.jax.mgi.fewi.forms.AccessionQueryForm;
import org.jax.mgi.fewi.summary.AccessionSummaryRow;

// data model objects
import mgi.frontend.datamodel.Marker;
import mgi.frontend.datamodel.Accession;
import mgi.frontend.datamodel.Reference;


/*--------------------------------------*/
/* standard imports for all controllers */
/*--------------------------------------*/

// internal
import org.jax.mgi.fewi.searchUtil.Filter;
import org.jax.mgi.fewi.searchUtil.ObjectTypes;
import org.jax.mgi.fewi.searchUtil.SearchConstants;
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.jax.mgi.fewi.searchUtil.Paginator;
import org.jax.mgi.fewi.searchUtil.Sort;
import org.jax.mgi.fewi.searchUtil.SortConstants;
import org.jax.mgi.fewi.summary.JsonSummaryResponse;
import org.jax.mgi.fewi.util.FewiLinker;

// external
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/*-------*/
/* class */
/*-------*/

/*
 * This controller maps all /accession/ uri's
 */
@Controller
@RequestMapping(value="/accession")
public class AccessionController {


    //--------------------//
    // instance variables
    //--------------------//

    private Logger logger
      = LoggerFactory.getLogger(AccessionController.class);

    @Autowired
    private AccessionFinder accessionFinder;

    @Autowired
    private ReferenceFinder referenceFinder;


    //--------------------------------------------------------------------//
    // public methods
    //--------------------------------------------------------------------//


    //--------------------//
    // Accession Query Form
    //--------------------//
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView getQueryForm() {

        logger.debug("->getQueryForm started");

        ModelAndView mav = new ModelAndView("accession_query");
        mav.addObject("sort", new Paginator());
        mav.addObject(new AccessionQueryForm());
        return mav;
    }


    //-----------------------------//
    // Accession Query Form Summary
    //-----------------------------//
    @RequestMapping("/summary")
    public ModelAndView accessionSummary(HttpServletRequest request,
            @ModelAttribute AccessionQueryForm queryForm) {

        logger.debug("->accessionSummary started");
        logger.debug("queryString: " + request.getQueryString());
        
        // For accession we need to search twice, once before the summary
        // page is run, and once during the json.  If we only get back a 
        // single object, we want to go directly to the object page.
        
        SearchParams params = new SearchParams();
        
        params.setSorts(this.genSorts(request));
        params.setFilter(this.genFilters(queryForm));
        
        SearchResults searchResults = accessionFinder.getAccessions(params);
        
        logger.debug("About to check the size");
        
        if (searchResults.getResultObjects().size() == 1) {
        	logger.debug("Found only 1, should be forwarding.");
        	// We only have one object, seamlessly forward it!
        	FewiLinker linker = FewiLinker.getInstance();
        	Accession acc = (Accession) searchResults.getResultObjects().get(0);
        	String url = "";
        	
        	String objectType = acc.getObjectType();
        	
        	// Handle the Vocabulary Cases
        	
        	if (objectType.equals("Vocabulary Term")) { 
        		logger.debug("This is a vocab match, should be a forward");
        		url = linker.getFewiIDLink(acc.getDisplayType(), acc.getDisplayID());        		
        	}
        	
        	// Handle the old wi cases, but with ID
        	
        	else if (objectType.equals(ObjectTypes.ORTHOLOGY)) {
        		url = linker.getFewiIDLink(objectType, acc.getDisplayID());
        	}
        	
        	// Handle the old wi cases.
        	
        	else if (objectType.equals(ObjectTypes.PROBECLONE) || 
        			objectType.equals(ObjectTypes.ASSAY) ||
        			objectType.equals(ObjectTypes.GO) ||
        			objectType.equals(ObjectTypes.ANTIBODY) ||
        			objectType.equals(ObjectTypes.ANTIGEN)) {
        		logger.debug("Old WI Case");
        		url = linker.getFewiKeyLink(objectType, "" + acc.getObjectKey());
        	}
        	else {
        		logger.debug("Base case.");
            	url = linker.getFewiIDLink(acc.getObjectType(), acc.getDisplayID());        			
        	}

        	return new ModelAndView("redirect:" + url);
        }

        ModelAndView mav = new ModelAndView("accession_summary");
        mav.addObject("queryString", request.getQueryString());
        mav.addObject("queryForm", queryForm);

        return mav;
    }

    //----------------------//
    // JSON summary results
    //----------------------//
    @RequestMapping("/json")
    public @ResponseBody JsonSummaryResponse<AccessionSummaryRow> accessionSummaryJson(
            HttpServletRequest request,
			@ModelAttribute AccessionQueryForm query,
            @ModelAttribute Paginator page) {

        logger.debug("->JsonSummaryResponse started");

        // generate search parms object;  add pagination, sorts, and filters
        SearchParams params = new SearchParams();
        params.setPaginator(page);
        params.setSorts(this.genSorts(request));
        params.setFilter(this.genFilters(query));

        // perform query, and pull out the requested objects
        SearchResults searchResults
          = accessionFinder.getAccessions(params);
        List<Accession> fooList = searchResults.getResultObjects();

        // create/load the list of SummaryRow wrapper objects
        
        logger.debug("Making the summary rows");
        
        Map <String, Integer> typeCount = new HashMap<String, Integer> ();
        
        List<AccessionSummaryRow> summaryRows = new ArrayList<AccessionSummaryRow> ();
        Iterator<Accession> it = fooList.iterator();
        while (it.hasNext()) {
        	Accession acc = it.next();
            if (acc == null) {
                logger.debug("--> Null Object");
            } else {
            	if (!typeCount.containsKey(acc.getObjectType())) {
            		typeCount.put(acc.getObjectType(), 0);
            	}
            	typeCount.put(acc.getObjectType(), typeCount.get(acc.getObjectType()) + 1);
                summaryRows.add(new AccessionSummaryRow(acc));
            }
        }

        for (AccessionSummaryRow row: summaryRows) {
        	if (typeCount.get(row.getObjectType()) > 1) {
        		row.setUseKey();
        	}
        }
        
        // The JSON return object will be serialized to a JSON response.
        // Client-side JavaScript expects this object
        JsonSummaryResponse<AccessionSummaryRow> jsonResponse
          = new JsonSummaryResponse<AccessionSummaryRow>();

        // place data into JSON response, and return
        jsonResponse.setSummaryRows(summaryRows);
        jsonResponse.setTotalCount(searchResults.getTotalCount());
        return jsonResponse;
    }


    

    //--------------------------------------------------------------------//
    // private methods
    //--------------------------------------------------------------------//

    //-----------------------------//
	// Accession By ID
	//-----------------------------//
    @RequestMapping("/{accID}")
	public ModelAndView accessionSummary(@PathVariable("accID") String accID,
			HttpServletRequest request,
	        @ModelAttribute AccessionQueryForm queryForm) {
	
	    logger.debug("->accessionSummary started");
	    logger.debug("queryString: " + request.getQueryString());
	    
	    queryForm.setId(accID);
	   	
	    return accessionSummary(request, queryForm);
	}


	// generate the sorts
    private List<Sort> genSorts(HttpServletRequest request) {

        logger.debug("->genSorts started");

        List<Sort> sorts = new ArrayList<Sort>();

        // retrieve requested sort order; set default if not supplied
        String sortRequested = request.getParameter("sort");
        if (sortRequested == null) {
            sortRequested = SortConstants.FOO_SORT;
        }

        String dirRequested  = request.getParameter("dir");
        boolean desc = false;
        if("desc".equalsIgnoreCase(dirRequested)){
            desc = true;
        }

        Sort sort = new Sort(sortRequested, desc);
        sorts.add(sort);

        logger.debug ("sort: " + sort.toString());
        return sorts;
    }

    // generate the filters
    private Filter genFilters(AccessionQueryForm query){

        logger.debug("->genFilters started");
        logger.debug("QueryForm -> " + query);


        // start filter list to add filters to
        List<Filter> filterList = new ArrayList<Filter>();

        String param1 = query.getId();

        // There can ONLY be accession at present, add it in.
        if ((param1 != null) && (!"".equals(param1))) {
            filterList.add(new Filter (SearchConstants.ACC_ID, param1,
                Filter.OP_EQUAL));
        }

        // if we have filters, collapse them into a single filter
        Filter containerFilter = new Filter();
        if (filterList.size() > 0){
            containerFilter.setFilterJoinClause(Filter.FC_AND);
            containerFilter.setNestedFilters(filterList);
        }

        return containerFilter;
    }


}
