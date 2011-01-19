package org.jax.mgi.fewi.hunter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jax.mgi.fewi.propertyMapper.PropertyMapper;
import org.jax.mgi.fewi.searchUtil.Filter;
import org.jax.mgi.fewi.searchUtil.MetaData;
import org.jax.mgi.fewi.searchUtil.ResultSetMetaData;
import org.jax.mgi.fewi.searchUtil.SearchParams;
import org.jax.mgi.fewi.searchUtil.SearchResults;
import org.jax.mgi.fewi.searchUtil.Sort;
import org.jax.mgi.fewi.sortMapper.SolrSortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * This is the Solr specific hunter.  It is responsible for mapping the higher
 *  level join type queries that are coming from the WI and translating them
 *  into something that Solr can understand.  It is also responsible for
 *  mapping the fe's idea of an in or not in clause to something that makes
 *  sense for solr.
 *
 * @author mhall
 *
 */

public class SolrHunter implements Hunter {

    /**
     * These strings all have their values set in the extending classes.
     */

    protected String solrUrl;
    protected String keyString;
    protected String otherString;
    protected String facetString;    
    
    // Gather the solr server settings from configuration
    
    @Value("${solr.soTimeout}")
    private Integer solrSoTimeout;
    @Value("${solr.connectionTimeout}")
    private Integer connectionTimeout;
    @Value("${solr.maxConnectionsPerHost}")
    private Integer maxConnectionsPerHost;
    @Value("${solr.maxTotalConnections}")
    private Integer maxTotalConnections; 
    @Value("${solr.maxRetries}")
    private Integer maxRetries; 
    
    // Gather the default sizes for facets and results.  We will use these
    // unless they are overidden by the request
    
    @Value("${solr.resultsDefault}")
    private Integer resultsDefault; 
    @Value("${solr.factetNumberDefault}")
    private Integer factetNumberDefault; 

    protected HashMap <String, PropertyMapper> propertyMap =
        new HashMap<String, PropertyMapper>();
    
    protected HashMap <String, String> fieldToParamMap = 
        new HashMap<String, String>();
    
    protected HashMap <String, SolrSortMapper> sortMap =
        new HashMap<String, SolrSortMapper>();

    protected List <String> highlightFields = new ArrayList<String> ();
    
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Here we map the higher level join clauses to their
     * respective Solr values.
     */

    private static HashMap<Integer, String> filterClauseMap =
        new HashMap<Integer, String>();
    
    
    public SolrHunter() {
        // Setup the mapping of the logical ands and or to the vendor
        // specific ones.

        filterClauseMap.put(Filter.FC_AND, " AND ");
        filterClauseMap.put(Filter.FC_OR, " OR ");

    }

    /**
     * Classes of the hunter interface must implement the hunt method.
     * This method is primarily responsible for breaking down a Filter
     * object into its component parts.  Once we are at that lowest level
     * we then invoke the SolrProperyMapper to get back the lowest level
     * query clauses.  These will then in turn be joined together by this
     * class, with the result being a query to place against solr.
     *
     * This method then runs the query, packages the results and exits.
     *
     */

    @Override
    public void hunt(SearchParams searchParams, SearchResults searchResults) {

        /**
         * Setup our interface into solr.  Some of these values might be better
         * off in a configuration object somewhere.
         */

        searchParams = this.preProcessSearchParams(searchParams);
        
        CommonsHttpSolrServer server = null;

        try { server = new CommonsHttpSolrServer(solrUrl);}
        catch (Exception e) {System.out.println("THERE IS AN ERROR.  PLEASE TO FIND IT.");
            e.printStackTrace();}

        logger.info("SolrTimeout:" + solrSoTimeout);
        
        server.setSoTimeout(solrSoTimeout);  // socket read timeout
        server.setConnectionTimeout(connectionTimeout);
        server.setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        server.setMaxTotalConnections(maxTotalConnections);
        server.setFollowRedirects(false);  // defaults to false
        server.setAllowCompression(true);
        server.setMaxRetries(maxRetries);

        SolrQuery query = new SolrQuery();

        String queryString =
            translateFilter(searchParams.getFilter(), propertyMap);
        logger.info(queryString);
        query.setQuery(queryString);

        
        
        /**
         * Tear apart the sort objects and add them to the query string.  This currently
         * maps to a sorMapper object, which can turn a conceptual single column sort
         * from the wi's perspective to its multiple column sort in the indexes.
         */

        query.setFields("score"); // Always pack the score
        
        for (Sort sort: searchParams.getSorts()) {
            if (sort.isDesc()) {
                if (sortMap.containsKey(sort.getSort())) {
                    for (String ssm: sortMap.get(sort.getSort()).getSortList()) {
                        query.addSortField(ssm, SolrQuery.ORDER.desc);
                    }
                }
                else {
                	query.addSortField(sort.getSort(), SolrQuery.ORDER.desc);
                }
            }
            else {
                if (sortMap.containsKey(sort.getSort())) {
                    for (String ssm: sortMap.get(sort.getSort()).getSortList()) {
                        query.addSortField(ssm, SolrQuery.ORDER.asc);
                    }
                }
                else {
                	query.addSortField(sort.getSort(), SolrQuery.ORDER.asc);
                }
            }
        }
        
        if (! highlightFields.isEmpty()) {
            for (String field: highlightFields) {
                query.addHighlightField(field);
            }
            query.setHighlight(Boolean.TRUE);
            query.setHighlightFragsize(30000);
            query.setHighlightRequireFieldMatch(Boolean.TRUE);
            query.setParam("hl.simple.pre", "!FRAG!");
            query.setParam("hl.simple.post", "!FRAG!");

        }
        
        /**
         * Set the pagination parameters.
         */

        query.setRows(searchParams.getPageSize());

        if (searchParams.getStartIndex() != -1) {
            query.setStart(searchParams.getStartIndex());
        }
        else {
            query.setStart(resultsDefault);
        }
        
        if (facetString != null) {
            query.addFacetField(facetString);
            query.setFacetMinCount(1);
            query.setFacetSort("lex");
            query.setFacetLimit(factetNumberDefault);
        }

        logger.info("This is the Solr query:" + query + "\n");

        /**
         * Run the query.
         */

        QueryResponse rsp = null;
        logger.debug("Got here, right before the try loop.");
        
        try {
        rsp = server.query( query );
        SolrDocumentList sdl = rsp.getResults();
        
        /**
         * Package the results into the searchResults object.
         * We do this in a generic manner via the packInformation method.
         */

        packInformation(rsp, searchResults, searchParams);

        
        logger.debug("metaMapping: " + searchResults.getResultSetMeta().toString());
        
        /**
         * Set the total number found.
         */

        searchResults.setTotalCount(new Integer((int) sdl.getNumFound()));

        /**
         * TODO: Set the metadata.
         * This will be handles in a similar way as the packKeys method, making
         * this entire process generic.
         *
         */

        }
        catch (Exception e) {e.printStackTrace();}

        return ;

    }
    
    /**
     * This is a hook, any class that needs to modify the searchParams before doing its work will
     * override this method.
     * 
     * @param searchParams
     * @return
     */
    protected SearchParams preProcessSearchParams(SearchParams searchParams) {
        return searchParams;
    }

    /**
     * packKeys
     * @param sdl
     * @return List of keys
     * This generic method is available to all extending classes.  All
     * that they need to do to take advantage of it is to set the keyString
     * variable.  This will then be used to extract a given field from the
     * returned documents as the key we want to return to the wi.
     *
     * If something more complex is requires, the implementer is expected
     * to override this method with their own version.
     */

/*    List<String> packKeys(SolrDocumentList sdl) {
        List<String> keys = new ArrayList<String>();

        logger.debug("Keys: ");

        for (Iterator iter = sdl.iterator(); iter.hasNext();)
        {
            SolrDocument doc = (SolrDocument) iter.next();

            keys.add((String) doc.getFieldValue(keyString));
            logger.debug("" + doc.getFieldValue(keyString));
        }

        return keys;
    }*/
    
/*    Map<String, MetaData> packMetadata(SolrDocumentList sdl, QueryResponse rsp) {
        Map<String, MetaData> metaList = new HashMap<String, MetaData> ();
        
        Map<String, Map<String, List<String>>> highlights = rsp.getHighlighting();
        
        for (Iterator iter = sdl.iterator(); iter.hasNext();) {
            SolrDocument doc = (SolrDocument) iter.next();
            
            MetaData tempMeta = new MetaData();
            tempMeta.setScore("" + doc.getFieldValue("score"));
            
            Set<String> highlightKeys = highlights.get(doc.getFieldValue(keyString)).keySet();
            Map<String, List<String>> highlightsMap = highlights.get(doc.getFieldValue(keyString));
            for (Iterator iter2 = highlightKeys.iterator(); iter2.hasNext();) {
                String key = (String) iter2.next();
                List <String> highlights2 = highlightsMap.get(key);
                for (String highlightWord: highlights2) {
                    
                    Boolean inAHL = Boolean.FALSE; 
                    String [] fragments = highlightWord.split("!FRAG!");
                    
                    for (String frag: fragments) {
                        if (inAHL) {
                            tempMeta.addHighlightWords(frag);
                            inAHL = Boolean.FALSE;
                        }
                        else {
                            inAHL = Boolean.TRUE;
                        }
                    }
                }
            }
            metaList.put((String) doc.getFieldValue(keyString), tempMeta);                        
        }
        
        return metaList;
    }*/
    
    /**
     * packScores
     * @param sdl
     * @return List of scores
     * 
     * Pack a list of scores that corresponds to the list of keys.
     */

/*    List<String> packScore(SolrDocumentList sdl) {
        List<String> keys = new ArrayList<String>();

        logger.info("scores: ");

        for (Iterator iter = sdl.iterator(); iter.hasNext();)
        {
            SolrDocument doc = (SolrDocument) iter.next();

            keys.add("" + doc.getFieldValue("score"));
            logger.info("" + doc.getFieldValue("score"));
        }

        return keys;
    }*/
    
/*    List<String> packExtraInfo(SolrDocumentList sdl) {
        List<String> info = new ArrayList<String>();

        logger.debug("Other Strings: ");

        for (Iterator iter = sdl.iterator(); iter.hasNext();)
        {
            SolrDocument doc = (SolrDocument) iter.next();

            info.add((String) doc.getFieldValue(otherString));
            logger.debug("" + doc.getFieldValue(otherString));
        }

        return info;
    }*/
    
/*    List<String> packFacet(QueryResponse rsp) {
        List<String> facet = new ArrayList<String>();

        logger.debug("Facet Strings: ");

        for (Count c: rsp.getFacetField(facetString).getValues()) {
            facet.add(c.getName());
            logger.debug(c.getName());
        }

        return facet;
    }*/

    /**
     * translateFilter
     * @param filter
     * @param propertyMap
     * @return a query string
     *
     * This method is responsible for recursively tearing apart
     * the filter object and generating the query string.
     *
     * It also handles any special cases that arise for
     * a given technology. In solr there is no real concept for IN or NOT IN,
     * so we translate those into a series of joined OR clauses.
     *
     */

    protected String translateFilter(Filter filter, HashMap<String, PropertyMapper> propertyMap) {

        /**
         * This is the end case for the recursion.  If we are at a node in the
         * tree generate the chunk of query string for that node and return
         * it back to the caller.
         *
         * An important concept here is the propertyMap, this mapping handles
         * the query clause generation for a given single property.  This
         * allows us to handle any special cases for properties using a single
         * interface.
         */

        if (filter.isBasicFilter()) {

            // Check to see if the property is null or an empty string,
            // if it is, return an empty string

            if (filter.getProperty() == null || filter.getProperty().equals("")) {
                return "";
            }

            // If its not an IN or NOT IN, get the query clause and return it

            if (filter.getOperator() != Filter.OP_IN
                    && filter.getOperator() != Filter.OP_NOT_IN) {
                return propertyMap.get(filter.getProperty()).getClause(filter.getValue(), filter.getOperator());
            }

            // If its an IN or NOT IN, break the query down further, joining the
            // subclauses by OR.

            else {

                int operator;
                String joinClause = "";
                if (filter.getOperator() == Filter.OP_NOT_IN) {
                    operator = Filter.OP_NOT_EQUAL;
                    joinClause = " AND ";
                }
                else {
                    operator = Filter.OP_EQUAL;
                    joinClause = " OR ";
                }
                String output = "(";
                int first = 1;
                for (String value: filter.getValues()) {
                    if (first == 1) {
                        output += propertyMap.get(filter.getProperty()).getClause(value, operator);
                        first = 0;
                    }
                    else {
                        output += joinClause + propertyMap.get(filter.getProperty()).getClause(value, operator);
                    }
                }
                return output + ")";
            }
        }

        // We do not have a simple filter, so recurse.
        // When we get the return value join it appropriately
        // into the query string using AND's or OR's
        // as specified by the filterClause.

        else {
            
            String queryString = "";
            queryString = "(";
            int first = 1;
            List<Filter> filters = filter.getNestedFilters();

            List<String> resultsString = new ArrayList<String>();

            for (Filter f: filters) {

                String tempString = translateFilter(f, propertyMap);
                if (! tempString.equals("")) {
                    resultsString.add(tempString);
                }
            }
            return "(" + StringUtils.join(resultsString, filterClauseMap.get(filter.getFilterJoinClause())) + ")";
        }
    }

    /**
     * packInformation
     * @param sdl
     * @return List of keys
     * This generic method is available to all extending classes.  All
     * that they need to do to take advantage of it is to set the keyString
     * variable.  This will then be used to extract a given field from the
     * returned documents as the key we want to return to the wi.
     *
     * If something more complex is requires, the implementer is expected
     * to override this method with their own version.
     */
    
    void packInformation(QueryResponse rsp, SearchResults sr, SearchParams sp) {
        List<String> keys = new ArrayList<String>();
        List<String> scoreKeys = new ArrayList<String>();
        List<String> info = new ArrayList<String>();
        List<String> facet = new ArrayList<String>();
                
        Map<String, Set<String>> setHighlights = new HashMap<String, Set<String>> ();
        
        Map<String, MetaData> metaList = new HashMap<String, MetaData> ();
        
        Map<String, Map<String, List<String>>> highlights = rsp.getHighlighting();
        
        SolrDocumentList sdl = rsp.getResults();
    
        logger.debug("We are in the pack information section.");
        
        if (this.facetString != null) {
            for (Count c: rsp.getFacetField(facetString).getValues()) {
                facet.add(c.getName());
                logger.debug(c.getName());
            }
        }
        
        for (Iterator iter = sdl.iterator(); iter.hasNext();)
        {
            SolrDocument doc = (SolrDocument) iter.next();
            
            if (sp.includeRowMeta()) {
                MetaData tempMeta = new MetaData();
                if (sp.includeMetaScore()) {
                    tempMeta.setScore("" + doc.getFieldValue("score"));
                }
                metaList.put((String) doc.getFieldValue(keyString), tempMeta);
            }
            
            if (this.keyString != null) {            
                keys.add((String) doc.getFieldValue(keyString));
                
                scoreKeys.add("" + doc.getFieldValue("score"));
                logger.debug("key: " + doc.getFieldValue(keyString));
            }

            if (this.otherString != null) {
                info.add((String) doc.getFieldValue(otherString));
                logger.debug("OtherString: " + otherString + " -"+ doc.getFieldValue(otherString));
            }
            
            
            if (!this.highlightFields.isEmpty() && sp.includeMetaHighlight() && sp.includeSetMeta()) {
            
            Set<String> highlightKeys = highlights.get(doc.getFieldValue(keyString)).keySet();
            Map<String, List<String>> highlightsMap = highlights.get(doc.getFieldValue(keyString));
            for (Iterator iter2 = highlightKeys.iterator(); iter2.hasNext();) {
                String key = (String) iter2.next();

                List <String> highlights2 = highlightsMap.get(key);
                for (String highlightWord: highlights2) {
                    
                    Boolean inAHL = Boolean.FALSE; 
                    String [] fragments = highlightWord.split("!FRAG!");
                    
                    for (String frag: fragments) {
                        if (inAHL) {
                            if (setHighlights.containsKey(fieldToParamMap.get(key))) {
                                setHighlights.get(fieldToParamMap.get(key)).add(frag);
                            }
                            else {
                                setHighlights.put(fieldToParamMap.get(key), new HashSet <String> ());
                                setHighlights.get(fieldToParamMap.get(key)).add(frag);
                            }
                            inAHL = Boolean.FALSE;
                        }
                        else {
                            inAHL = Boolean.TRUE;
                        }
                    }
                }
            } 
        }

        }
    
        if (keys != null) {
            sr.setResultKeys(keys);
        }
        
/*        if (scoreKeys != null) {
            sr.setResultScores(scoreKeys);
        }*/
        
        if (info != null) {
            sr.setResultStrings(info);
        }
        
        if (facet != null) {
            sr.setResultFacets(facet);
        }
        
        if (sp.includeSetMeta()) {
            sr.setResultSetMeta(new ResultSetMetaData(setHighlights));
        }
        
        if (sp.includeRowMeta()) {
            sr.setMetaMapping(metaList);
        }
        
/*        if (!this.highlightFields.isEmpty()) {
            sr.setMetaMapping(metaList);
            sr.setResultSetMeta(new ResultSetMetaData(setHighlights));
        }*/
    }
}
