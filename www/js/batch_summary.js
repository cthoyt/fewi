
var myDataSource;
var myDataTable;
var generateRequest;
var totalCount = 0;
var numConfig = {thousandsSeparator: ','};

// Integrate with Browser History Manager
var History = YAHOO.util.History;

(function () {		
    // Column definitions -- sortable:true enables sorting
    // These are our actual columns, in the default ordering.
	
	// default columns
    var myColumnDefs = [
        {key:"term", 
            label:"Input",
            sortable:false},
        {key:"type", 
            label:"Input<br/>Type",
            sortable:false},
        {key:"markerId", 
            label:"MGI Gene/Marker ID",
            sortable:false}
    ];
    
    // optional columns
    if (nomenclature){
    	myColumnDefs.push({label:"Nomenclature",
            sortable:false,
            children: [{key: "symbol",
            		label: "Symbol"},
                {key: "name",
                	label: "Name"},
                {key: "feature",
                    label: "Feature Type"}]});
    }
    if (loco){
    	myColumnDefs.push({label:"Genome Location",
    	    sortable:false,
    	    children: [{key: "chromosome",
    	    		label: "Chr"},
    	        {key: "strand",
    	        	label: "Strand"},
    	        {key: "start",
    	            label: "Start"},
    	        {key: "end",
    	        	label: "End"}]});
    }
    if (ensembl){
    	myColumnDefs.push({key:"ensemblIds", 
    	    label:"Ensembl IDs",
    	    sortable:false});
    }
    if (entrez){
    	myColumnDefs.push({key:"entrezIds", 
    	    label:"Entrez Gene IDs",
    	    sortable:false});
    }
    if (vega){
    	myColumnDefs.push({key:"vegaIds", 
    	    label:"Vega IDs",
    	    sortable:false});
    }
    if (go){
    	myColumnDefs.push({label:"GO IDs",
    	    sortable:false,
    	    children: [{key: "goIds",
	    		label: "ID"},
	    	{key: "goTerms",
		    	label: "Term"},
    		{key: "goCodes",
	    		label: "Code"}]});
    }
    if (mp){
    	myColumnDefs.push({label:"MP IDs",
    	    sortable:false,
    	    children: [{key: "mpIds",
    	    	label: "ID"},
	    	{key: "mpTerms",
		    		label: "Term"}]});
    }
    if (omim){
    	myColumnDefs.push({label:"OMIM IDs",
    	    sortable:false,
    	    children: [{key: "omimIds",
	    		label: "ID"},
	    	{key: "omimTerms",
		    	label: "Term"}]});
    }
    if (allele){
    	myColumnDefs.push({label:"Alleles",
    	    sortable:false,
    	    children: [{key: "alleleIds",
	    		label: "ID"},
	    	{key: "alleleSymbols",
		    	label: "Symbol"}]});
    }
    if (exp){
    	myColumnDefs.push({label:"Gene Expression",
    	    sortable:false,
    	    children: [{key: "expressionStructure",
	    		label: "Anatomical Structure"},
	        {key: "expressionResultCount",
	        	label: "Assay Results"},
	        {key: "expressionDetectedCount",
	            label: "Detected"},
	        {key: "expressionNotDetectedCount",
	        	label: "Not Detected"}]});
    }
    if (refseq){
    	myColumnDefs.push({key:"refseqIds", 
    	    label:"GenBank/RefSeq IDs",
    	    sortable:false});
    }
    if (uniprot){
    	myColumnDefs.push({key:"uniprotIds", 
    	    label:"Uniprot IDs",
    	    sortable:false});
    }

    // DataSource instance
    var myDataSource = new YAHOO.util.DataSource(fewiurl + "batch/json?" + querystring + "&");

    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    myDataSource.responseSchema = {
        resultsList: "summaryRows",
        fields: [
            {key:"term"},
            {key:"type"},
            {key:"markerId"},
            {key:"symbol"},
            {key:"name"},
            {key:"feature"},
            {key:"chromosome"},
            {key:"strand"},
            {key:"start"},
            {key:"end"},
        	{key:"ensemblIds"},
        	{key:"entrezIds"},
        	{key:"vegaIds"},
        	{key:"goIds"},
        	{key:"goTerms"},
        	{key:"goCodes"},
        	{key:"mpIds"},
        	{key:"mpTerms"},
        	{key:"omimIds"},
        	{key:"omimTerms"},
        	{key:"alleleIds"},
        	{key:"alleleSymbols"},
        	{key:"expressionStructure"},
        	{key:"expressionResultCount"},
        	{key:"expressionDetectedCount"},
        	{key:"expressionNotDetectedCount"},
        	{key:"refseqIds"},
        	{key:"uniprotIds"}
        ],
        metaFields: {
	        totalRecords: "totalCount",
	        paginationRecordOffset : "startIndex",
	        paginationRowsPerPage : "pageSize"
        }
    };
    
    myDataSource.maxCacheEntries = 3;
    myDataSource.connXhrMode = "cancelStaleRequests";

    // Create the Paginator
    var myPaginator = new YAHOO.widget.Paginator({
        template : "{FirstPageLink} {PreviousPageLink}<strong>{PageLinks}</strong> {NextPageLink} {LastPageLink} <span style=align:right;>{RowsPerPageDropdown}</span><br/>{CurrentPageReport}",
        pageReportTemplate : "Showing row(s) {startRecord} - {endRecord} of {totalRecords}",
        rowsPerPageOptions : [10,25,50,100],
        containers   : ["paginationTop", "paginationBottom"],
        rowsPerPage : 25,
        pageLinks: 3,
        recordOffset: 1
    });

    // DataTable configurations
    var myConfigs = {
        paginator : myPaginator,
        rowExpansionTemplate : '<div class="refAbstract">{abstract}</div>',
        dynamicData : true,
        initialLoad : false,
        MSG_LOADING:  '<img src="/fewi/mgi/assets/images/loading.gif" height="24" width="24"> Searching...',
        MSG_EMPTY:    'No markers found.'
    };   
    
    // DataTable instance
    var myDataTable = new YAHOO.widget.DataTable("dynamicdata", myColumnDefs, 
    	    myDataSource, myConfigs);
    
    // Show loading message while page is being rendered
    myDataTable.showTableMessage(myDataTable.get("MSG_LOADING"), YAHOO.widget.DataTable.CLASS_LOADING);    

    // Define a custom function to route pagination through the Browser History Manager
    var handlePagination = function(state) {
        // The next state will reflect the new pagination values
        // while preserving existing sort values
        var newState = generateRequest(state.recordOffset, state.rowsPerPage);
        //myPaginator.setState(newState);
        // Pass the state along to the Browser History Manager
        History.navigate("myDataTable", newState);
    };
    // First we must unhook the built-in mechanism...
    myPaginator.unsubscribe("changeRequest", myDataTable.onPaginatorChangeRequest);
    // ...then we hook up our custom function
    myPaginator.subscribe("changeRequest", handlePagination, myDataTable, true);

    // Update payload data on the fly for tight integration with latest values from server     
    myDataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
		var pRequest = parseRequest(oRequest);
        var meta = oResponse.meta;

        oPayload.totalRecords = meta.totalRecords || oPayload.totalRecords;

        updateCount('totalCount', oPayload.totalRecords);
        updateCount('markerCount', oPayload.totalRecords);
        
        var filterCount = YAHOO.util.Dom.get('filterCount');
        if (!YAHOO.lang.isNull(filterCount)){
        	setText(filterCount, YAHOO.util.Number.format(oPayload.totalRecords, numConfig));
        }
        
        oPayload.pagination = {
            rowsPerPage: Number(pRequest['results'][0]) || 25,
            recordOffset: Number(pRequest['startIndex'][0]) || 0
        };

        var reportButton = YAHOO.util.Dom.get('textDownload');
        if (!YAHOO.lang.isNull(reportButton)){      	
	        facetQuery = generateRequest(0, totalCount);
	        reportButton.setAttribute('href', fewiurl + 'batch/report.txt?' + querystring + '&' + facetQuery);
        }

        return true;
    };
    
	updateCount = function (elName, newCount) {
		var countEl = YAHOO.util.Dom.get(elName);
		if (!YAHOO.lang.isNull(countEl)){
			setText(countEl, YAHOO.util.Number.format(newCount, numConfig)); 
	    	totalCount = newCount;
		}
	};
    
    // Returns a request string for consumption by the DataSource
    generateRequest = function(startIndex, results) {
        results = results || 25;
        return "results="+results+"&startIndex="+startIndex;
    };
    
    // Called by Browser History Manager to trigger a new state
    handleHistoryNavigation = function (request) {
    	myDataTable.showTableMessage(myDataTable.get("MSG_LOADING"), YAHOO.widget.DataTable.CLASS_LOADING);   
        // Sends a new request to the DataSource
        myDataSource.sendRequest(request,{
            success : myDataTable.onDataReturnSetRows,
            failure : myDataTable.onDataReturnSetRows,
            scope : myDataTable,
            argument : {} // Pass in container for population at runtime via doBeforeLoadData
        });
    };

    // Calculate the first request
    var initialRequest = History.getBookmarkedState("myDataTable") || // Passed in via URL
                       generateRequest(0, 25); // Get default values

    // Register the module
    History.register("myDataTable", initialRequest, handleHistoryNavigation);

    // Render the first view
    History.onReady(function() {
        // Current state after BHM is initialized is the source of truth for what state to render
        var currentState = History.getCurrentState("myDataTable");
        handleHistoryNavigation(currentState);
    });

    // Initialize the Browser History Manager.
    YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");

})();

function parseRequest(request){
	var reply = {};
	var kvPairs = request.split('&');
	for (pair in kvPairs) {
		var kv = kvPairs[pair].split('=');
		if(!reply[kv[0]]){
			reply[kv[0]] = [];
		}
		reply[kv[0]].push(kv[1]);
	}
	return reply;
};

