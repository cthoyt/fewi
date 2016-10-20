/* Name: gxdht_summary.js
 * Purpose: supports the high-throughput expression summary display
 */
 
/*** logging support for debugging ***/

// assumes gxdht_query.js is also loaded, which includes a log(msg) function

/*** module-level variables ***/

/*** functions ***/

var instantiatedPaginator = false;

// update the pagination info in the querystring
var updatePaginationParameters = function(page, rowsPerPage) {
	if (querystring == null) { return; }
	var startIndex = (page - 1) * rowsPerPage;

	if (querystring.indexOf('startIndex') < 0) {
		querystring = querystring + '&startIndex=' + startIndex
	} else {
		querystring = querystring.replace(/startIndex=[0-9]+/, 'startIndex=' + startIndex);
	}
	
	if (querystring.indexOf('results') < 0) {
		querystring = querystring + '&results=' + rowsPerPage;
	} else {
		querystring = querystring.replace(/results=[0-9]+/, 'results=' + rowsPerPage);
	}
	updateResultsDiv(startIndex, rowsPerPage);
};

// update the results div
var updateResultsDiv = function(startIndex, rowsPerPage) {
	var xPos = window.scrollX;		// remember page position, so we can return to it
	var yPos = window.scrollY;
	
	var top = $('#paginationTop').position().top;
	var bottom = $('#paginationBottom').position().top;
	
	// if we've scrolled closer to the bottom pagination than the top, assume the user clicked the bottom set and
	// go back to the top
	if (Math.abs(top - yPos) > Math.abs(bottom - yPos)) {
		yPos = $('#resultbar').position().top;
	}
	
	log("entered updateResultsDiv()");
	$("#resultSummary").html("<img src='" + fewiurl + "assets/images/loading.gif' height='24' width='24'> Searching...");
	log("added searching message");
	$.ajax({
		url: fewiurl + "gxdht/table?" + querystring,	// can take state as param and append here for pagination
		datatype : "html",
		success: function(data) {
			log("successful response");

			// need to pull the initial integer count off the front of the data
			var lines = data.split('\n');
			var count = parseInt(lines[0]);
			var totalCount = parseInt(lines[1]);
			
			if ((count == 0) || (totalCount == 0)) {
				$("#resultSummary").html("No experiments meet your search criteria.");
				updatePageReport(null, null, null, "experiment");
				updatePaginator(totalCount, null, updatePaginationParameters);
				return;
			}
			
			if (count < rowsPerPage) { rowsPerPage = count; }
			var start = startIndex + 1;
			var end = startIndex + rowsPerPage;

			// remove first two lines, then merge back into single string
			lines.splice(0,2);
			var newtext = lines.join('\n');

			$("#resultSummary").html(newtext);
			log("updated div on page");
			
			for (var i = 0; i < count; i++) {
				var sw = "#row" + i + "samplesWrapper";
				var vw = "#row" + i + "variablesWrapper";
				var tw = "#row" + i + "typeWrapper";
				var mw = "#row" + i + "methodWrapper";
				var sp = "#row" + i + "spacer";
				
				// synchronize heights of experiment info columns to match the tallest, ensuring uniform right border height
				if ((sw != null) && (vw != null) && (tw != null) && (mw != null) && (sp != null)) {
					var height = Math.max($(sw).height(), $(vw).height(), $(tw).height(), $(mw).height());
					$(sw).height(height);
					$(vw).height(height);
					$(tw).height(height);
					$(mw).height(height);
					$(sp).height(height);
				}
				
				// adjust height of title div to ensure right border is tall enough
				var iw = '#row' + i + 'idWrapper';
				var ew = '#row' + i + 'title';
				
				if ($(iw).height() > $(ew).height()) {
					$(ew).height($(iw).height());
				}
			}
			log("updated heights");
			
			updatePageReport(start, end, totalCount, "experiment");
			updatePaginator(totalCount, null, updatePaginationParameters);
			log("updated paginator");
			
			window.scrollTo(xPos, yPos);
		}
	});
};

// update divs with the pageReport class to show a message about which items are displayed currently
var updatePageReport = function(start, end, totalCount, item) {
	if ((start == null) || (end == null) || (totalCount == null) || (item == null)) {
		$(".pageReport").html("");
		return;
	}

	var message = "Showing ";
	if (start == 0) {
		message = message + item + "s 0 of " + totalCount;
	} else if (start == end) {
		message = message + item + " " + start + " of " + totalCount;
	} else if (start < end) {
		message = message + item + "s " + start + " - " + end + " of " + totalCount;
	} else {
		message = "Searching...";
	}
	$(".pageReport").html(message);
};

// update the paginator; assume we will replace the contents of any HTML
// elements (presumably DIVs) that have a "paginator" class with an 
// updated paginator
var updatePaginator = function(totalCount, pageLimit, callback) {
	if (instantiatedPaginator) { return; }
	instantiatedPaginator = true;
	
	if (totalCount == 0) {
		$(".paginator").html("");
		return;
	}
	
	log('instantiating paginator with ' + totalCount + ' rows');
	if (totalCount == null) {
		alert("You cannot have a paginator with a null totalCount.");
	}
	if (pageLimit == null) { pageLimit = 50; }
	if (callback == null) {
		callback = function(page) {
			alert("You forgot to define a callback for the paginator (page " + page + ")");
		}
	}
	
	$(".paginator").paging(totalCount, {
        format: '[< nncnn >]',		// first, prev, five page numbers with current in middle, next, last
        perpage: pageLimit,
        onSelect: function(page) {
            callback(page, pageLimit);
        },
        onFormat: function(type) {
            switch (type) {
                case 'block': // n and c
                   	if (this.active) {
	                   	if (this.value != this.page) return '<a href="#">' + this.value + '</a> ';
	                   	else return '<span class="current">' + this.value + '</span> ';
                   	}
	                return '<span class="disabled">' + this.value + '</span> ';
                case 'next': // >
                    if (this.active) return '<a href="#">next&gt;</a> ';
	                return '<span class="disabled">next&gt;</span> ';
                case 'prev': // <
                	if (this.active) return '<a href="#">&lt;prev</a> ';
	                return '<span class="disabled">&lt;prev</span> ';
                case 'first': // [
                    if (this.active) return '<a href="#">&lt;&lt;first</a> ';
	                return '<span class="disabled">&lt;&lt;first</span> ';
                case 'last': // ]
                    if (this.active) return '<a href="#">last&gt;&gt;</a>';
	                return '<span class="disabled">last&gt;&gt;</span> ';
            }
        }
    });
};

// execute a search and update the page
var gs_search = function() {
	if ((typeof querystring != 'undefined') && querystring != '') {
		log("search with querystring: " + querystring);
		updateResultsDiv();
	} else {
		log("no querystring, no search");
	}
};


/*** to execute on being loaded ***/

log("loaded gxdht_summary.js");