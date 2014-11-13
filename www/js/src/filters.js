/* Name: filters.js
 * Purpose: to provide a basic library supporting the addition of various
 *	types of filters to MGI summary pages
 * Assumptions:
 *	1. The filters are designed for pages with YUI DataTables, backed by
 *		data retrieved as JSON from the fewi.
 * Goals:
 * 	1. should support, or at least not preclude, multiple filter UI
 * 		paradigms: check boxes, type-in fields, sliders, filters with
 * 		multiple fields (e.g., data_source and score)
 * 	2. should support application of zero or more filters (not just 1)
 * 	3. should be easy to integrate with browser history manager
 * 	4. should not be specific to a certain QF
 * Usage:
 * 	TBD
 */

/*************************/
/*** library namespace ***/
/*************************/

/* establish a filters namespace; public functions will be defined within this
 * namespace to prevent name clashes with other modules.  For example, a
 * function getX would be called as filters.getX()
 */
window.filters = {};

/************************/
/*** global variables ***/
/************************/

filters.filterNames = [];	// list of filter names
filters.filtersByName = {};	// maps from filter name to dict of filter info
filters.callbackNames = [];	// list of functions to be called when the
				// ...values for a filter change
filters.callbacksByName = {};	// maps from callback name to function to call
filters.callbacksActive = true;	// are the callback functions active?

filters.logging = true;		// write log messages to browser error log?

filters.queryStringFunction = null;	// funtion to call to get parameter
					// ...string for general query form
					// ...parameters

filters.dialogBox = null;	// the actual dialog box the user sees

filters.fieldnameToFilterName = {}	// maps from a field name to the name
					// ...of the filter having the field

filters.historyModule = null;	// set this to be the name of your module for
				// ...history management purposes

filters.navigateFn = null;	// function to be called by the history
				// ...manager to do data updates

filters.dataTable = null;	// the YUI DataTable managed by the filters

filters.filterSummary = null;	// name of div containing the whole filter
				// ...summary

filters.filterList = null;	// name of the span containing the filter
				// ...buttons

filters.callbacksInProgress = false;	// are we currently handling callbacks?

filters.fewiUrl = null;		// base URL to fewi, used to pick up images

filters.alternateCallback = null;	// if we are not managing a dataTable,
					// ...what should we call when a
					// ...filter's values are returned?

/************************/
/*** public functions ***/
/************************/

/* notify this module of the function to call to retrieve the parameter string
 * for the general query form parameters
 */
filters.setQueryStringFunction = function(fn) {
    filters.queryStringFunction = fn;
};

/* notify this module of the name of the module to use for history management
 * purposes and the name of the function for the history manager to call to
 * update the data table.
 */
filters.setHistoryManagement = function(module, navigateFunction) {
    filters.navigateFn = navigateFunction;

    /* disabled (not using browser history for now)
     *
    filters.historyModule = module;
    YAHOO.util.History.register(module,
	YAHOO.util.History.getBookmarkedState(module) || "",
	filters.navigateFn);
    filters.registerCallback("navigateFn", filters.navigateFn);
     *
     */
};

/* set URL to fewi
 */
filters.setFewiUrl = function(fewiUrl) {
    filters.fewiUrl = fewiUrl;
};

/* notify this module of the name of the YUI DataTable managed by the filters
 */
filters.setDataTable = function(dataTable) {
    filters.dataTable = dataTable;
};

/* if we are not managing a dataTable, then what function should we call when
 * a filter's set of values is returned?
 * The alternateFn will be called with three parameters:
 *    1. sRequest <String> - original request
 *    2. oResponse <Object> YUI Response object
 *    3. oPayload <MIXED, optional> additional argument(s)
 */
filters.setAlternateCallback = function(alternateFn) {
    filters.alternateCallback = alternateFn;
};

/* notify this module of the names for the filter summary div and the span
 * within it that will contain the filter removal buttons.
 */
filters.setSummaryNames = function(filterSummary, filterList) {
    filters.filterSummary = filterSummary;
    filters.filterList = filterList;
};

/* builds and returns list of DOM elements, one for each buttons to remove
 * values from this filter.  If this filter has no values selected, then this
 * method returns an empty list.  This is the default formatter, for the
 * traditional filters which allow a selection list of values and we want a
 * 'remove' button for each of the values.  Other methods may be used in place
 * of this one, as long as they follow the same pattern of inputs and outputs.
 * 'mydata' is a hash of data about the filter, as from filters.filtersByName.
 */
filters.defaultFilterFormatter = function(mydata) {
    var list = [];	// list of DOM objects to return, one per button

    // this formatter is only appropriate for single-field filters; bail out
    // otherwise
    if (mydata.fields.length > 1) {
	filters.log('too many fieldnames for defaultFilterFormatter()');
	return list;
    }

    var fieldname = mydata.fields[0];	// name of the field for this filter

    // don't need any removal buttons if there are no values specified
    
    if (fieldname in mydata.values) {
	filters.log(fieldname + ' has values in ' + mydata.name);
    } else {
	filters.log(fieldname + ' has no values in ' + mydata.name);
	return list;
    }

    var valueCount = mydata.values[fieldname].length;

    if (valueCount <= 0) {
	return list;
    }

    // build the buttons
    var myValues = mydata.values[fieldname];
    if (typeof(myValues) === 'string') {
	myValues = [ myValues ];
    }

    for (var pos in myValues) {

	var value = myValues[pos];
	var id = mydata.name + ':' + fieldname + ':' + value;
	var text = mydata.nameForUser + ': ' + filters.decode(value);

	var el = document.createElement('a');
	el.setAttribute('class', 'filterItem'); 
	el.setAttribute('id', id);
	el.setAttribute('style', 'line-height: 2.2');
	el.setAttribute('title', 'click to remove this filter');
	setText(el, text);

	list.push(el);
    } 
    filters.log('returning ' + list.length + ' buttons for ' + mydata.name);
    return list;
};

/* builds and returns list of DOM elements, one for each buttons to remove
 * values from this filter.  If this filter has no values selected, then this
 * method returns an empty list.  This is the formatter for filters which use
 * a single-value slider,  where a 'remove' button will remove this filter
 * entirely.  'mydata' is a hash of data about the filter, as from
 * filters.filtersByName.
 */
filters.sliderFormatter = function(mydata) {
    var list = [];	// list of DOM objects to return, one per button

    // this formatter is only appropriate for single-field filters; bail out
    // otherwise
    if (mydata.fields.length > 1) {
	filters.log('too many fieldnames for sliderFormatter()');
	return list;
    }

    var fieldname = mydata.fields[0];	// name of the field for this filter

    // don't need any removal buttons if there are no values specified
    
    if (fieldname in mydata.values) {
	filters.log(fieldname + ' has values in ' + mydata.name);
    } else {
	filters.log(fieldname + ' has no values in ' + mydata.name);
	return list;
    }

    var valueCount = mydata.values[fieldname].length;

    if (valueCount <= 0) {
	return list;
    }

    // build the buttons
    var myValue = mydata.values[fieldname];
    if (typeof(myValue) === 'object') {
	myValue = myValue[0];
    }

    var id = mydata.name + ':' + fieldname + ':' + myValue;
    var text = mydata.nameForUser + ': >= ' + filters.decode(myValue);

    var el = document.createElement('a');
    el.setAttribute('class', 'filterItem'); 
    el.setAttribute('id', id);
    el.setAttribute('style', 'line-height: 2.2');
    el.setAttribute('title', 'click to remove this filter');
    setText(el, text);

    list.push(el);

    filters.log('returning ' + list.length + ' buttons for ' + mydata.name);
    return list;
};

/* create a new filter and register it with this library; note that we replace
 * any existing filter with the same 'filterName'.  Also hooks the "click"
 * event up to the DOM object with the given 'buttonID'.
 */
filters.addFilter = function(
    filterName,	// string; name of the filter in code
    nameForUser,// string; name of the filter as displayed to user
    buttonID,	// string; DOM ID for the button that opens the filter
    fieldnames,	// string or list of strings; names of form fields
		// ...managed by this filter
    url,	// url for where the filter should get its options
    formatter,	// function for formatting name:value pairs for button; if not
    		// ...specified, will use a default one value per button one
    parser,	// function for parsing results that come back with filter
    		// ...values
    popupTitle	// string; specify to override the standard title for
    		// ...the popup dialog
    ) {

    // remove an old filter, if one exists

    filters.removeFilter(filterName);

    // convert a single fieldname (a string) to a list

    var fields = fieldnames;
    if (typeof fields === 'string') {
	fields = [ fields ];
    }

    // build the dialog box, if it isn't already built
    filters.buildDialogBox();

    // fall back on default formatter and parser

    formatter = formatter ||  filters.defaultFilterFormatter;
    parser = parser || filters.parseResponse;
    popupTitle = popupTitle || ('Filter by ' + nameForUser);

    // how to handle errors when retrieving data from the data source

    var handleError = function (oRequest, oResponse, oPayload) {
	buttons = filters.dialogBox.getButtons();
	for (var k in buttons) {
	    buttons[k].set('disabled', 'true');
	}
	filters.populateDialogForFilter(filterName);
    };

    // build a callback for when retrieving data from the data source
 
    var buildCallback = function (filterName, popupTitle) {
	return { success: parser,
	    failure: handleError,
	    scope: this,
	    argument: { name: filterName, title: popupTitle }
	};
    };

    // create the filter
 
    var filterDS = filters.buildFilterDataSource(filterName, url);

    filters.filterNames.push(filterName);
    filters.filtersByName[filterName] = {
	'name' : filterName,
	'nameForUser' : nameForUser,
	'buttonID' : buttonID,
	'url' : url,
	'formatter' : formatter,
	'fields' : fields,
	'values' : {},		// fieldname -> [ value 1, ... value n ]
	'dataSource' : filterDS,
	'callback' : buildCallback(filterName, nameForUser),
	'parser' : parser,
	'title' : popupTitle
    	};

    // remember which fields are managed by this filter
    for (var i in fields) {
	filters.fieldnameToFilterName[fields[i]] = filterName;
    }

    // remove any old handling for the click, and hook it to the new button
    YAHOO.util.Event.removeListener(buttonID, 'click')
    YAHOO.util.Event.addListener(buttonID, 'click', function() {
	filters.populateDialogForFilter(filterName);
	}, true);

    filters.log('Added filter: ' + filterName);
};

/* remove the filter with the given 'filterName'
 */
filters.removeFilter = function(
    filterName) {	// string; name of the filter to delete

    // remove any existing filter by the same name
 
    var pos = filters.listIndexOf(filters.filterNames, filterName);
    if (pos >= 0) {
	var hadValues = filters.filtersByName[filterName].values.length;

	filters.filterNames.splice(pos, 1);
	delete filters.filtersByName[filterName];

	filters.log('Removed filter: ' + filterName);

	if (hadValues > 0) {
	    filters.issueCallbacks();
	}
    }

    // remove fieldnames mapping to this filterName

    for (var fieldname in filters.fieldnameToFilterName) {
	if (filters.fieldnameToFilterName[fieldname] == filterName) {
	    delete filters.fieldnameToFilterName[fieldname];
	}
    }
};

/* return a list of strings, each of which is the name of a filter
 */
filters.getFilterNames = function() {
    // return a copy, so the original cannot be modified by the caller

    return filters.filterNames.slice();
};

/* clear the selected values for all filters
 */
filters.clearAllFilters = function() {
    var i = 0;
    var hadValues = 0;	// number of filters with values before clearing

    filters.callbacksOff();

    for (var i = 0; i < filters.filterNames.length; i++) {
	hadValues = hadValues + filters.clearAllValuesForFilter(
	    filters.filterNames[i]);
    }
    
    filters.callbacksOn();

    if (hadValues > 0) {
	filters.issueCallbacks();
    }
};

/* clear the values selected for the filter with the given 'filterName'.
 * returns 1 if the filter had values selected, 0 if not.
 */
filters.clearAllValuesForFilter = function(filterName) {
    if (filterName in filters.filtersByName) {
	var hadValues = 0;
	for (var i in filters.filtersByName[filterName].values) {
	    hadValues = hadValues + 1;
	}

	filters.filtersByName[filterName]['values'] = {}

	if (hadValues > 0) {
	    filters.issueCallbacks();
	    return 1;
	}
    }
    return 0;
};

/* register a new callback function ('callbackFn') and associate it with the
 * given 'callbackName'.  This function will be called when the value of a
 * filter changes.  If the given 'callbackName' is already registered, then
 * the new 'callbackFn' will replace the old one.
 */
filters.registerCallback = function(callbackName, callbackFn) {

    // remove any existing callback by that name
    filters.removeCallback(callbackName);

    // add the new callback
    filters.callbackNames.push(callbackName);
    filters.callbacksByName[callbackName] = callbackFn;

    filters.log('Added callback: ' + callbackName);
};

/* remove the callback function with the given 'callbackName' from the list
 * of callback functions to be called when a filter value changes.
 */
filters.removeCallback = function(callbackName) {
    var pos = filters.listIndexOf(filters.callbackNames, callbackName);
    if (pos >= 0) {
	filters.callbackNames.splice(pos, 1);
	delete filters.callbacksByName[callbackName];

	filters.log('Removed callback: ' + callbackName);
    }
};

/* get the filters' fieldnames and their values, formatted to be suitable for
 * being appended to a URL string.  Note that it includes a leading ampersand
 * (&), if there are any filter values set.  (If there are no filter values,
 * this function returns an empty string.)
 */
filters.getUrlFragment = function() {
    var s = '';		// return string we're compiling
    var i = 0;		// walks through filters

    for (var i = 0; i < filters.filterNames.length; i++) {
	s = s + filters.getUrlFragmentForFilter(filters.filterNames[i]);
    }

    filters.log('Got URL fragment: ' + s);
    return s;
};

/* get all the buttons for the filter summary div (the buttons which show the
 * currently selected filter values and allow you to click and remove them).
 * returns an empty string if there are no filter values currently selected.
 */
filters.getAllSummaryButtons = function() {
    var list = [];	// list of DOM elements to return
    var i = 0;		// walks through filters
    var f;		// formatting function for each filter
    var data;		// hash of data for each filter
    var elements;	// list of DOM elements for a single filter

    filters.log('in getAllSummaryButtons()');
    for (var i = 0; i < filters.filterNames.length; i++) {
	f = filters.filtersByName[filters.filterNames[i]]['formatter'];
	data = filters.filtersByName[filters.filterNames[i]];

	var results = f(data);
	if (results) {
	    list = list.concat(results);
	}
    }

    if (list.length > 0) {
	// if there were some filters selected, need to add a 'clear all'
	// button
	
	var el = document.createElement('a');
	el.setAttribute('class', 'filterItem'); 
	el.setAttribute('id', 'clearAllFilters');
	el.setAttribute('style', 'line-height: 2.2');
	el.setAttribute('title', 'click to remove all filters');
	setText(el, 'Remove All Filters');
	list.push(el); 

	// wire up all the buttons to the clearFilter() function

	for (var i = 0; i < list.length; i++) {
	    YAHOO.util.Event.addListener(list[i], 'click', filters.clearFilter);
	}
    }
    return list;
};

/* set the values for all filters to be those specified in the given 'url'
 */
filters.setAllFiltersFromUrl = function(url) {
    var i = 0;		// walks through filters

    for (var i = 0; i < filters.filterNames.length; i++) {
	filters.setFilterFromUrl(filters.filterNames[i], url);
    }
    return;
};

/* set the value for the filter with the given name to be those specified in
 * the given 'url'
 */
filters.setFilterFromUrl = function(filterName, url) {
	// TBD
};

/* set the filters from a pRequest object
 */
filters.setAllFilters = function(pRequest) {
    filters.clearAllFilters();

    for (var field in pRequest) {
	if ((pRequest[field]) && (field in filters.fieldnameToFilterName)) {
	    var filterName = filters.fieldnameToFilterName[field];
	    filters.filtersByName[filterName]['values'][field] = [pRequest[field]];
	}
    }
    filters.populateFilterSummary();
};

/* do prep work needed to initialize the filters (call from onDOMReady)
 */
var prepFilters = function() {
    filters.log("Entered prepFilters()");
    filters.buildDialogBox();
};

/*************************/
/*** private functions ***/
/*************************/

/* note that these are not actually private, but are intended to be treated
 * as private functions
 */

/* return position of 'myItem' in 'myList', or -1 if not present.  (This is
 * useful because IE8 and prior do not have indexOf() natively.)
 */
filters.listIndexOf = function(myList, myItem) {
    // if browser's engine provides indexOf(), then use it
 
    if (!myList) { return -1; }

    if (typeof Array.prototype.indexOf === 'function') {
	return myList.indexOf(myItem);
    }

    // otherwise, do it manually
    
    var i = 0;
    for (var i = 0; i < myList.length; i++) {
	if (myList[i] === myItem) {
	    return i;
	}
    }
    return -1;
}

/* set this library so that it will not issue callbacks temporarily when
 * filters change.  This is to allow us to set or clear multiple filters at a
 * time, without issuing the callbacks multiple times.
 */
filters.callbacksOff = function() {
    filters.callbacksActive = false;
    filters.log('Turned off callbacks');
}

/* set this library so that it will issue callbacks when filters change.
 */
filters.callbacksOn = function() {
    filters.callbacksActive = true;
    filters.log('Turned on callbacks');
}

/* call each of the various functions that have been registered for callbacks,
 * in the order that they were registered
 */
filters.issueCallbacks = function() {
    if (filters.callbacksActive) {
	if (filters.callbacksInProgress) {
	    return;
	}

        filters.log('Issuing callbacks...');
	filters.callbacksInProgress = true;
	var i = 0;
	for (var i = 0; i < filters.callbackNames.length; i++) {
	    filters.log('invoking callback: ' + filters.callbackNames[i]);
	    filters.callbacksByName[filters.callbackNames[i]]();
	    filters.log('returned from callback: ' + filters.callbackNames[i]);
	}
        filters.log('Issued ' + filters.callbackNames.length + ' callbacks');
	filters.callbacksInProgress = false;
    }
}

/* get the fieldnames and values for this particular filters, formatted to be
 * suitable for being appended to a URL string.  If this filter has no
 * selected values, it will return an empty string.  Otherwise, it will have a
 * leading ampersand (&).
 */
filters.getUrlFragmentForFilter = function(filterName) {
    var j = 0;		// walks through fieldnames for this filter
    var k = 0;		// walks through values for a given fieldname
    var s = '';		// return string we're compiling
    var fieldname;	// current fieldname being examined
    var numValues = 0;	// number of values selected for this fieldname

    for (var j = 0; j < filters.filtersByName[filterName].fields.length; j++) {
	fieldname = filters.filtersByName[filterName].fields[j];

	var items = [];

	if (fieldname in filters.filtersByName[filterName].values) {
	    items = filters.filtersByName[filterName].values[fieldname];
	    if (typeof(items) === 'string') {
		items = [ items ];
	    }
	}

	if (!filters.filtersByName[filterName].values[fieldname]) {
	    return '';
	}

	if (filters.filtersByName[filterName].values[fieldname] === undefined) {
	    return '';
	}

	numValues = items.length;

	if (numValues > 0) {
	    for (var k = 0; k < numValues; k++) {
		var item = items[k];

		if (item.toString().length > 0) {
	            s = s + '&' + fieldname + '=' + item;
		}
	    }
	}
    }
    return s;
};

/* convert certain hex-coded pieces of the value to their ASCII counterparts
 */
filters.decode = function(val) {
    if (typeof(val) !== 'string') {
	val = '' + val;
    }
    return val.replace('%26', '&');
};

/* convert certain ASCII characters to their hex-coded counterparts, for 
 * possible inclusion in a URL
 */
filters.encode = function(val) {
    return val.replace('&', '%26');
};

/* write the given string 'msg' out to the browser's error log
 */
filters.log = function(msg) {
    if (filters.logging) {
	setTimeout(function() { throw new Error('filters.js: ' + msg); }, 0);
    }
};

/* return URL-encoded form parameters (but not filter values) as a string
 */
filters.getQueryString = function() {
    if (filters.queryStringFunction) {
	return filters.queryStringFunction();
    }
    return "";
};

/* build and return a data source object for retrieving filter values
 */
filters.buildFilterDataSource = function(name, url) {
    filters.log("Building data source for " + name);

    var oCallback = null;

    if (filters.dataTable) {
    	oCallback = {
	    success : filters.dataTable.onDataReturnInitializeTable,
	    failure : filters.dataTable.onDataReturnInitializeTable,
	    scope : this
	};
    } else if (filters.alternateCallback) {
    	oCallback = {
	    success : filters.alternateCallback,
	    failure : filters.alternateCallback,
	    scope : this
	};
    } else {
	filters.log("Must set either dataTable or alternateCallback");
    }

    var qs = filters.getQueryString();

    if (qs) {
	qs = qs + '&';
    }

    var dsUrl = url + "?" + qs;
    filters.log("Data source URL: " + dsUrl);

    var facetDS = new YAHOO.util.DataSource(dsUrl);

    facetDS.responseType = YAHOO.util.DataSource.TYPE_JSON;
    facetDS.responseSchema = { resultsList: "resultFacets",
	metaFields: { message : "message" } };

    facetDS.maxCacheEntries = 3;

    facetDS.doBeforeParseData = function (oRequest, oFullResponse, oCallback) {
	oCallback.argument.error = oFullResponse.error;
	return oFullResponse;
    };

    return facetDS;
};

/* log a new entry in browser history for the current state of things
 */
filters.addHistoryEntry = function() {
    if (filters.dataTable) {
	var state = filters.dataTable.getState();
	var newState = is_generateRequest (0, state.sortedBy.key, 
	    state.sortedBy.dir,
	    filters.dataTable.get("paginator").getRowsPerPage());


	if (filters.historyModule) {
	    YAHOO.util.History.navigate(filters.historyModule, newState);
	} else if (filters.navigateFn) {
	    filters.navigateFn(newState);
    	    filters.populateFilterSummary();
	} else {
	    filters.log('filters.historyModule is missing');
	}
    } else if (!filters.alternateCallback) {
	filters.log('filters.dataTable is missing');
    }
};

/* build the dialog box once and only once
 */
filters.buildDialogBox = function() {
    if (filters.dialogBox) {
	filters.log("using existing dialogBox");
	return;
    }
    filters.log("building new dialogBox");

    // function to be called when submit button is clicked
    var handleSubmit = function() {
	filters.log("entered handleSubmit()");

	var selections = this.getData();
	filters.log('selections: ' + selections);

	var list = [];
	var filterName;

	for (var i in selections) {
	    filterName = filters.fieldnameToFilterName[i];

	    var selectionValue = selections[i];
	    if (typeof(selectionValue) === 'string') {
		selectionValue = [ selectionValue ];
	    }

	    // special case -- YUI treats a set of only one checkbox
	    // differently than it does a set with two or more possibilities

	    if (selectionValue === true) {
		selectionValue = document.getElementsByName(i)[0].value;
	    } else if (selectionValue === false) {
		selectionValue = null;
	    }

	    if (selectionValue !== null) {
	        filters.filtersByName[filterName]['values'][i] = selectionValue;
	        filters.log('set filtersByName[' + filterName + ']["values"]['
		    + i + '] = ' + selectionValue + '');
	    } else {
	        filters.filtersByName[filterName]['values'][i] = [];
		filters.log('skipped selectionValue = null');
	    }
	}

	filters.addHistoryEntry();
        filters.dialogBox.hide();
	filters.issueCallbacks();

        filters.populateFilterSummary();
	this.submit(); 
    };

    // function to be called on successful submission
    var handleSuccess = function(o) {
	filters.log('entered handleSuccess()');
	var response = o.responseText;
	response = response.split("<!")[0];
	filters.log("handleSuccess() response: " + response);
    };

    // function to be called on failed submission
    var handleFailure = function(o) {
	filters.log('entered handleFailure()');
	this.form.innerHTML = '<img src="' + filters.fewiUrl + 'assets/images/loading.gif">';
	alert("Submission failed: " + o.status);
    };

    // build the dialog box itself
    filters.dialogBox = new YAHOO.widget.Dialog("facetDialog", {
	visible : false,
	context : [ "filterDiv", "tl", "bl", [ "beforeShow" ] ],
	constraintoviewport : true,
	width: "290px",
	buttons : [{ text:"Filter", handler: handleSubmit, isDefault: true} ]
    } );

    filters.dialogBox.hideEvent.subscribe(function() {
	this.form.innerHTML = '<img src="' + filters.fewiUrl + 'assets/images/loading.gif">';
    } );

    filters.dialogBox.callback = { success: handleSuccess,
	failure: handleFailure };

    filters.dialogBox.render();
    filters.log ("Built global filters.dialogBox");
};

/* populate the dialog box for the filter with the given name.  Specify either
 * by 'filterName', or use 'title', 'body', and 'error'.
 */
filters.populateDialogForFilter = function(filterName) {
    filters.log('populating dialog for ' + filterName);

    if (filters.listIndexOf(filters.filterNames, filterName) >= 0) {
	var dataSource = filters.filtersByName[filterName]['dataSource'];
	dataSource.flushCache();
	dataSource.sendRequest(filters.getUrlFragment(),
	    filters.filtersByName[filterName]['callback']);
    } else {
	filters.log("Unknown filterName in populateDialogForFilter(" + filterName + ")");
    }
};

/* fill the dialog box with the given title and body, then show it to the
 * user.  (error is a flag to indicate if the button should be disabled)
 */
filters.fillAndShowDialog = function (title, body, error) {
    filters.log('in fillAndShowDialog()');

    if (filters.dialogBox === null) {
	filters.buildDialogBox();
    }

    filters.dialogBox.setHeader(title);
    filters.dialogBox.form.innerHTML = body;

    var buttons = filters.dialogBox.getButtons();

    for (var k in buttons) {
	buttons[k].set('disabled', error);
    }

    filters.log('showing dialogBox');
    filters.dialogBox.show();
};

/* parse the response from a data source then use it to populate the dialog
 * box and show it to the user.  This works for a filter with a single field
 * which has a list of value choices.  Override this if you need something
 * different.
 */
filters.parseResponse = function(oRequest, oResponse, oPayload) {
    filters.log('parseResponse() : ' + oPayload.name);

    var list = [];
    var res = oResponse.results;
    var options = [];
    var title = "Filter";

    var fieldname = null;
    if (oPayload.name in filters.filtersByName) {
	var fields = filters.filtersByName[oPayload.name].fields;
	if (fields.length > 0) {
	    fieldname = fields[0];
	}
        filters.log('fieldname: ' + fieldname); 
	title = filters.filtersByName[oPayload.name].title;
    } else {
	filters.log('Unknown filter name: ' + oPayload.name);
	return;
    }

    var filteredValues = filters.filtersByName[oPayload.name].values[fieldname];
    filters.logObject(filteredValues, 'filteredValues');
    if (typeof(filteredValues) == 'string') {
	filteredValues = [ filteredValues ];
    } else if (!filteredValues) {
	filteredValues = [];
    }

    var selectedList = [];
    for (var y in filteredValues) {
	var fvList = filteredValues[y];
	if (typeof(fvList) == 'string') {
	    fvList = fvList.split(',');
	}

//	var fvList = filteredValues[y].split(',');

	for (var z in fvList) {
	    selectedList.push(fvList[z]);
	}
    }

    for (var x in res) {
	var checked = '';
	var fVal = filters.encode(res[x]);

	var i = selectedList.length;
	while (i--) {
	    if (selectedList[i] == fVal) {
		checked = ' CHECKED';
		break;
	    }
	}

	if (checked != '') {
	    list.push(res[x] + ' (checked)');
	} else {
	    list.push(res[x]);
	}

	options[x] = '<label><input type="checkbox" name="'
	    + fieldname + '" value="'
	    + res[x].replace(/,/g, '(') + '"'
	    + checked + '> '
	    + res[x] + '</label>';

    }
    filters.log('parseResponse() found: ' + list.join(', '));
    filters.fillAndShowDialog(title, options.join('<br/>'), false);
};

/* parse the response from a data source then use it to populate the dialog
 * box and show it to the user.  This works for a slider filter with a single
 * field which will return a single value.  Assumes two values will be
 * returned, which are the minimum and maximum values for the slider.  Also
 * assumes these will be numeric.
 */
filters.sliderParser = function(oRequest, oResponse, oPayload) {
    filters.log('sliderParser() : ' + oPayload.name);

    var list = [];
    var res = oResponse.results;
    var options = [];
    var title = "Filter";

    var fieldname = null;
    if (oPayload.name in filters.filtersByName) {
	var fields = filters.filtersByName[oPayload.name].fields;
	if (fields.length > 0) {
	    fieldname = fields[0];
	}
        filters.log('fieldname: ' + fieldname); 
	title = filters.filtersByName[oPayload.name].title;
    } else {
	filters.log('Unknown filter name: ' + oPayload.name);
	return;
    }

    var filteredValue = filters.filtersByName[oPayload.name].values[fieldname];
    filters.logObject(filteredValue, 'filteredValue');
    if (typeof(filteredValue) == 'object') {
	filteredValue = filteredValue[0];
    }

    if (typeof(filteredValue) === 'undefined') {
	filteredValue = 0;
    } else if (typeof(filteredValue) === 'string') {
	filteredValue = Number(filteredValue);
    } else if (!filteredValue) {
	filteredValue = 0;
    }

    var values = [];
    for (var x in res) {
	values.push(Number(res[x]));
    }
    values.sort(function(a,b) { return a-b; } );

    if (values.length != 2) {
	filters.log('too few values: ' + values);
	values = [ 0, 1 ];
    }

    options.push('<div style="float:left">' + values[0] + '</div>');
    options.push('<div style="float:right; margin-right: 10px">' + values[1] + '</div>');
    options.push('<p/>');
    options.push('<div id="sliderbg" class="yui-h-slider">');
    options.push('<div id="sliderthumb"><img src="' + filters.fewiUrl + 'assets/images/slider_thumb_n.gif">');
    options.push('</div>');
    options.push('Minimum score: <span id="sliderValueShown"></span>');
    options.push('<input type="hidden" id="sliderValueHidden" name="' +
	fieldname + '" value="">');

    filters.fillAndShowDialog(title, options.join(''), false);


    var sliderbg = YAHOO.util.Dom.get('sliderbg');
    var slider = YAHOO.widget.Slider.getHorizSlider(sliderbg, 'sliderthumb',
	0, 235);

    filters.filtersByName[oPayload.name]['slider'] = slider;

    var minValue = values[0];
    var maxValue = values[1];

    slider.setValue(0);

    slider.subscribe('change', function() {
	var value = filters.filtersByName[oPayload.name].slider.getValue();

	var sliderValueShown = YAHOO.util.Dom.get('sliderValueShown');
	var sliderValueHidden = YAHOO.util.Dom.get('sliderValueHidden');

	filters.log('value: ' + value);
	var realValue = minValue + (value / 235) * (maxValue - minValue);
	realValue = Math.round(realValue * 1000) / 1000.0;
	filters.log('realValue: ' + realValue);

	sliderValueShown.innerHTML = realValue;
	sliderValueHidden.value = realValue;
    });

    filters.log('after building slider');
};

/* loop through items in obj and output values to the log
 */
filters.logObject = function(obj, objectName, level) {
    var myLevel = 3;
    if (level) {
	myLevel = level;
    }
    
    if (myLevel <= 0) {
	return;
    }

    if (obj === null) {
	filters.log(objectName + ' is null');
	return;
    } else if (obj === undefined) {
	filters.log(objectName + ' is undefined');
	return;
    }

    if (typeof(obj) === 'object') {
	for (var name in obj) {
	    if (typeof(obj[name]) === 'function') {
		filters.log('skipping ' + name.toString());
		continue;
	    }

	    var nameStr = name.toString();

	    if (typeof(obj[name]) === 'object') {
		filters.logObject (obj[name], objectName + '.' + nameStr,
		    myLevel - 1);
	    } else {
	        filters.log(objectName + '.' + nameStr + ' = '
		    + obj[name].toString());
	    }
	}
    } else {
	filters.log(objectName + ' = ' + obj.toString());
    }

    if (myLevel >= 3) {
	filters.log('logObject() finished');
    }
};

/* handle the click event for a 'remove filter' button.
 * assumes id of 'this' object is of format:
 * 	filter name:field name:value
 * or to remove all values for a given filter, use:
 * 	filter name:clear
 * or to remove all values for all filters, use:
 * 	clearAllFilters
 */
filters.clearFilter = function() {

    // special case where we want to clear all filters

    if (this.id === 'clearAllFilters') {
	filters.clearAllFilters();
	filters.addHistoryEntry();
	return;
    }
    var kv = this.id.split(':');

    // special case where we want to clear all values for a filter

    if (kv.length == 2) {
	if (kv[1] === 'clear') {
	    filters.clearAllValuesForFilter(kv[0]);
	    filters.addHistoryEntry();
	    return;
	}
    }

    // normal case -- clear a value for a single field of a single filter

    if (kv.length != 3) {
	filters.log('unexpected button ID: ' + this.id);
	return;
    }

    var filterName = kv[0];
    var fieldName = kv[1];
    var fieldValue = kv[2];

    if (filterName in filters.filtersByName) {
	var pairs = filters.filtersByName[filterName]['values'];

	if (fieldName in pairs) {
	    var pos = filters.listIndexOf (pairs[fieldName], fieldValue);

	    if (pos >= 0) {
		if (typeof(pairs[fieldName]) === 'string') {
		    pairs[fieldName] = [];
		} else {
		    pairs[fieldName].splice(pos, 1);
		}
		filters.log('removed ' + fieldValue + ' from field '
		    + fieldName + ' in filter ' + filterName);

	    } else {
		filters.log('value ' + fieldValue + ' not selected for field '
		    + fieldName + ' in filter ' + filterName);
	    }
	} else {
	    filters.log('field ' + fieldName + ' unknown for filter '
		+ filterName);
	}
    } else {
	filters.log('unknown filter name: ' + filterName);
    }

    filters.addHistoryEntry();
    filters.issueCallbacks();
};

/* populate the filter summary on the form
 */
filters.populateFilterSummary = function() {
    filters.log('in populateFilterSummary()');
    if ((filters.filterSummary === null) || (filters.filterList === null)) {
	filters.log('need to call setSummaryNames()');
	return;
    }

    var fSum = YAHOO.util.Dom.get(filters.filterSummary);
    if (fSum === null) {
	filters.log('filterSummary is unrecognized: ' + filters.filterSummary);
	return;
    }

    var fList = new YAHOO.util.Element(filters.filterList);

    if (!YAHOO.lang.isNull(YAHOO.util.Dom.get(filters.filterList))) {
	// clean out any existing buttons from the filter list

	while (fList.hasChildNodes()) {
	    fList.removeChild(fList.get('firstChild'));
        }
    }

    var buttons = filters.getAllSummaryButtons();

    filters.log('adding ' + buttons.length + ' buttons');
    for (var i in buttons) {
	var button = buttons[i];

	fList.appendChild(button);
	fList.appendChild(document.createTextNode(' '));
    }

    if (buttons.length > 0) {
	YAHOO.util.Dom.setStyle(fSum, 'display', 'inline');
    } else {
	YAHOO.util.Dom.setStyle(fSum, 'display', 'none');
    }
};
