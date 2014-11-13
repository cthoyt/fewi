/* Name: disease_portal_filters.js
 * Purpose: supports additional filters for the HMDC, beyond the initial set
 * 	of row & column filters.  This module initially will support a Feature
 * 	Type filter.  It will be integrated with the row/column filters in
 * 	disease_portal_summary.js.
 * Author: jsb
 * Notes: Under the hood, this library makes use of pieces of the standard
 * 	filters.js library.
 */

/* establish a hmdcFilters namespace; public functions will be defined within
 * this namespace to prevent name clashes with other modules.
 */
window.hmdcFilters = {};

/************************
 *** public functions ***
 ************************/

/* notify this module of the function that can be called to retrieve the
 * current query string (URL parameters)
 */
hmdcFilters.setQueryStringFunction = function(fn) {
    filters.setQueryStringFunction(fn);
};

/* notify this module of the base URL for the fewi
 */
hmdcFilters.setFewiUrl = function(fewiUrl) {
    filters.setFewiUrl(fewiUrl);
};

/* create filters for HMDC (beyond the traditional row/column filters)
 */
hmdcFilters.createFilters = function() {
    filters.addFilter ('featureType', 'Feature Type', 'featureTypeButton',
	'featureTypeFilter', 
	'http://cardolan.informatics.jax.org:58080/fewi/mgi/diseasePortal/facet/featureType');
};

/* get a list of strings, each of which is a filter name
 */
hmdcFilters.getFilterNames = function() {
    return filters.getFilterNames();
};

/* clear the selections for all filters
 */
hmdcFilters.clearAllFilters = function() {
    filters.clearAllFilters();
};

/* clear all selections for a single filter by name
 */
hmdcFilters.clearAllValuesForFilter = function(filterName) {
    filters.clearAllValuesForFilter(filterName);
};

/* register a function to be called whenever the value for a filter changes
 */
hmdcFilters.registerCallback = function(callbackName, callbackFn) {
    filters.registerCallback(callbackName, callbackFn);
};

/* remove a callback function
 */
hmdcFilters.removeCallback = function(callbackName) {
    filters.removeCallback(callbackName);
};

/* get the filters' fieldnames and their values, formatted to be suitable for
 * being appended to a URL string.  Note that it includes a leading ampersand
 * (&), if there are any filter values set.  (If there are no filter values,
 * this function returns an empty string.)
 */
hmdcFilters.getUrlFragment = function() {
    return filters.getUrlFragment();
};

/* get the filters' fieldnames and values as a hash, mapping from key to a
 * list of values for that key.  This is the format expected by the
 * generateRequest() method in fewi_utils.js.
 */
hmdcFilters.getFacets = function() {
    var facets = {};
    var fragment = filters.getUrlFragment();

    if (!fragment) { return facets; }

    var options = fragment.split('&');

    for (var k = 0; k < options.length; k++) {
	var items = options[k].split('=');
    	var fieldname = '';
    	var fieldvalue = '';

	if (items.length == 1) {
	    fieldname = items[0];
	} else if (items.length == 2) {
	    fieldname = items[0];
	    fieldvalue = items[1];
	} else {
	    filters.log('Skipped odd parameter: ' + options[k]);
	}

	if (fieldname) {
	    if (facets.hasOwnProperty(fieldname)) {
		if (filters.listIndexOf(facets[fieldname], fieldvalue) == -1) {
		    facets[fieldname].push(fieldvalue);
		}
	    } else {
		facets[fieldname] = [ fieldvalue ]
	    }
	}
    }
    return facets;
};

/* get all the buttons for the filter summary div (the buttons used to remove
 * the individual selected values for each filter).  returns an empty string
 * if there are no filter values currently selected.
 */
hmdcFilters.getAllSummaryButtons = function() {
    return filters.getAllSummaryButtons();
};

/* function to be called when filter values are returned via JSON.  Do not
 * call directly.
 */
hmdcFilters.filterValuesReturned = function(sRequest, oResponse, oPayload) {
    filters.log("sRequest: " + sRequest);
    filters.log("oResponse: " + oResponse);
    filters.log("oPayload: " + oPayload);
};

/* function to pull the current querystring out of the window namespace
 */
hmdcFilters.getQueryString = function() {
    return window.querystring;
};

hmdcFilters.updatePage = function() {
    filters.log('in hmdcFilters.updatePage()...');
    var request = hmdcFilters.getQueryString() + hmdcFilters.getUrlFragment();
    filters.log('  - request: ' + request);

    var facets = hmdcFilters.getFacets();

    for (var key in facets) {
	try {
    	    document.getElementById(key).value = facets[key].join('|');
	    filters.log('Set ' + key + ' value = ' + facets[key].join('|'));
	} catch (e) {
	    filters.log('Missing hidden field for: ' + key);
	}
    } 

    handleNavigation(request);
    refreshTabCounts();
};

/* do prep work needed to initialize the filters
 */
hmdcFilters.prepFilters = function() {
    prepFilters();			// from filters.js library

    hmdcFilters.setQueryStringFunction(hmdcFilters.getQueryString);
    filters.setAlternateCallback(hmdcFilters.filterValuesReturned);
    hmdcFilters.createFilters();
    hmdcFilters.registerCallback("updatePage", hmdcFilters.updatePage);
};
