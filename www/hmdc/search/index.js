(function() {
  'use strict';
  angular.module('civicClient', [
    // vendor modules
    'formly',

    'civic.config',
    'civic.services',
    'civic.search',
    'civic.style',
    'naturalSort',
  ]);

// define root modules & dependencies
  angular.module('civic.config', ['formly', 'formlyBootstrap']);
  angular.module('civic.services', ['ngResource']);
  angular.module('civic.search', ['ngRoute', 'formly', 'formlyBootstrap', 'ui.bootstrap', 'angular-table', 'ngDialog', 'smart-table', 'naturalSort']);
  angular.module('civic.style', ['ngRoute', 'formly', 'formlyBootstrap', 'ui.bootstrap', 'angular-table', 'ngDialog', 'smart-table']);


})();
