package org.jax.mgi.fewi.finder;

import java.util.*;

/*-------------------------------*/
/* to be changed for each Finder */
/*-------------------------------*/

import mgi.frontend.datamodel.Marker;
import mgi.frontend.datamodel.MarkerTissueCount;

import org.jax.mgi.fewi.hunter.FooKeyHunter;
import org.jax.mgi.fewi.hunter.FooSummaryHunter;

/*----------------------------------------*/
/* standard classes, used for all Finders */
/*----------------------------------------*/

// fewi
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.jax.mgi.fewi.objectGatherer.HibernateObjectGatherer;

// external libs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/*-------*/
/* class */
/*-------*/

/*
 * This finder is responsible for finding foo(s)
 */

@Repository
public class MarkerTissueCountFinder {

    /*--------------------*/
    /* instance variables */
    /*--------------------*/

    private Logger logger = LoggerFactory.getLogger(MarkerTissueCountFinder.class);

    @Autowired
    private FooKeyHunter fooKeyHunter;

    @Autowired
    private FooSummaryHunter fooSummaryHunter;

    @Autowired
    private HibernateObjectGatherer<MarkerTissueCount> fooGatherer;


    /*-----------------------------------------*/
    /* Retrieval of a foo, for a given ID
    /*-----------------------------------------*/

    public SearchResults<MarkerTissueCount> getFooByID(SearchParams searchParams) {

        logger.debug("->getFooByID()");

        // result object to be returned
        SearchResults<MarkerTissueCount> searchResults = new SearchResults<MarkerTissueCount>();

        // ask the hunter to identify which objects to return
        fooKeyHunter.hunt(searchParams, searchResults);
        logger.debug("->hunter found these resultKeys - "
          + searchResults.getResultKeys());

        // gather objects identified by the hunter, add them to the results
        fooGatherer.setType(MarkerTissueCount.class);
        List<MarkerTissueCount> fooList
          = fooGatherer.get( searchResults.getResultKeys() );
        searchResults.setResultObjects(fooList);

        return searchResults;
    }


	/*--------------------------------------------*/
	/* Retrieval of a foo, for a given db key
	/*--------------------------------------------*/

    public SearchResults<MarkerTissueCount> getFooByKey(String dbKey) {

        logger.debug("->getFooByKey()");

        // result object to be returned
        SearchResults<MarkerTissueCount> searchResults = new SearchResults<MarkerTissueCount>();

        // gather objects, add them to the results
        fooGatherer.setType(MarkerTissueCount.class);
        MarkerTissueCount foo = fooGatherer.get( dbKey );
        searchResults.addResultObjects(foo);

        return searchResults;
    }


    /*---------------------------------*/
    /* Retrieval of multiple foos
    /*---------------------------------*/

    public SearchResults<MarkerTissueCount> getFoos(SearchParams searchParams) {

        logger.debug("->getFoos");

        // result object to be returned
        SearchResults<MarkerTissueCount> searchResults = new SearchResults<MarkerTissueCount>();

        // ask the hunter to identify which objects to return
        fooSummaryHunter.hunt(searchParams, searchResults);
        logger.debug("->hunter found these resultKeys - "
          + searchResults.getResultKeys());

        // gather objects identified by the hunter, add them to the results
        fooGatherer.setType(MarkerTissueCount.class);
        List<MarkerTissueCount> fooList
          = fooGatherer.get( searchResults.getResultKeys() );
        searchResults.setResultObjects(fooList);

        return searchResults;
    }



}
