(function() {
  'use strict';
  angular.module('civic.search')
    .controller('SearchController', SearchController)
    .config(SearchView);

  // @ngInject
  function SearchView($stateProvider) {
    $stateProvider
      .state('search', {
        url: '/',
        reloadOnSearch: false,
        controller: 'SearchController',
        templateUrl: 'search/views/search.tpl.html'
      });
  }

  // @ngInject
  function SearchController($scope, $log, Search) {
    var vm = $scope.vm = {};

    vm.onSubmit = onSubmit;

    function onSubmit() {
      $log.debug(JSON.stringify(vm.model));
      Search.post(vm.model);
    }

    vm.operatorField = [
      {
        key: 'operator',
        type: 'queryBuilderSelect',
        data: {
          defaultValue: 'AND'
        },
        templateOptions: {
          label: '',
          options: [
            { value: 'AND', name: 'all' },
            { value: 'OR', name: 'any' }
          ]
        }
      }
    ];

    vm.buttonLabel = 'Search';

    vm.model = {
      operator: 'AND',
      queries: [
        {
          field: '',
          condition: {
            name: undefined,
            parameters: []
          }
        }
      ]
    };

    vm.fields = 
      [
		{
        type: 'queryRow',
        key: 'queries',
        templateOptions: {
          rowFields: [
            {
              key: 'field',
              type: 'queryBuilderSelect',
              templateOptions: {
                label: '',
                required: true,
                options: [
                  { value: '', name: 'Please select a field' },
                  { value: 'description', name: 'Description' },
                  { value: 'disease_doid', name: 'Disease DOID' },
                  { value: 'disease_name', name: 'Disease Name' },
                  { value: 'drug_id', name: 'Drug PubChem ID' },
                  { value: 'drug_name', name: 'Drug Name' },
                  { value: 'id', name: 'Evidence ID'},
                  { value: 'evidence_type', name: 'Evidence Type' },
                  { value: 'evidence_level', name: 'Evidence Level' },
                  { value: 'gene_name', name: 'Gene Name' },
                  { value: 'pubmed_id', name: 'Pubmed ID' },
                  { value: 'suggested_changes_count', name: 'Suggested Revisions' },
                  { value: 'status', name: 'Status' },
                  { value: 'variant_name', name: 'Variant Name' },
                  { value: 'submitter', name: 'Submitter Display Name' },
                  { value: 'submitter_id', name: 'Submitter ID' }
                ],
                onChange: function(value, options, scope) {
                  scope.model.condition = {
                    name: undefined,
                    parameters: []
                  };
                }
              }
            }
          ],
          conditionFields: {
            pubmed_id: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field inline-field-md',
                data: {
                  defaultValue: 'is'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'is_not', name: 'is not'}
                  ]
                }
              },
              {
                key: 'parameters[0]', // pubmed id
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            description: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'contains'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'contains', name: 'contains'},
                    {value: 'begins_with', name: 'begins with'},
                    {value: 'does_not_contain', name: 'does not contain'},
                    {value: 'is_empty', name: 'is empty'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                hideExpression: 'model.name === "is_empty"',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            disease_name: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'contains'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'contains', name: 'contains'},
                    {value: 'begins_with', name: 'begins with'},
                    {value: 'does_not_contain', name: 'does not contain'},
                    {value: 'is_empty', name: 'is empty'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                hideExpression: 'model.name === "is_empty"',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            disease_doid: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field inline-field-small',
                data: {
                  defaultValue: 'is'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'is_not', name: 'is not'},
                    {value: 'is_empty', name: 'is empty'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                hideExpression: 'model.name === "is_empty"',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            drug_name: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'contains'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'contains', name: 'contains'},
                    {value: 'begins_with', name: 'begins with'},
                    {value: 'does_not_contain', name: 'does not contain'}

                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            drug_id: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field inline-field-small',
                data: {
                  defaultValue: 'is'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'is_not', name: 'is not'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            evidence_type: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field inline-field-md',
                data: {
                  defaultValue: 'is_equal_to'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is_equal_to', name: 'is'},
                    {value: 'is_not_equal_to', name: 'is not'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'Predictive'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    { value: 'Predictive', name: 'Predictive' },
                    { value: 'Diagnostic', name: 'Diagnostic' },
                    { value: 'Prognostic', name: 'Prognostic' }
                  ]
                }
              }
            ],
            evidence_level: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'is_above'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is_equal_to', name: 'is'},
                    {value: 'is_above', name: 'is above'},
                    {value: 'is_below', name: 'is below'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'C'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    { value: 'A', name: 'A - Validated'},
                    { value: 'B', name: 'B - Clinical'},
                    { value: 'C', name: 'C - Case Study'},
                    { value: 'D', name: 'D - Preclinical'},
                    { value: 'E', name: 'E - Inferential'}
                  ]
                }
              }
            ],
            status: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field inline-field-small',
                data: {
                  defaultValue: 'is'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'is_not', name: 'is not'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'submitted'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    { value: 'submitted', name: 'Submitted'},
                    { value: 'accepted', name: 'Accepted'},
                    { value: 'rejected', name: 'Rejected'}
                  ]
                }
              }
            ],
            suggested_changes_count: [
              {
                template: 'with status',
                className: 'inline-field'
              },
              {
                key: 'parameters[0]', // status
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'new'
                },
                templateOptions: {
                  required: true,
                  label: '',
                  options: [
                    { value: 'new', name: 'new' },
                    { value: 'applied', name: 'applied' },
                    { value: 'rejected', name: 'rejected' }
                  ]
                }
              },
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'is_greater_than_or_equal_to'
                },
                templateOptions: {
                  required: true,
                  label: '',
                  options: [
                    { value: 'is_greater_than_or_equal_to', name: 'is greater than or equal to' },
                    { value: 'is_greater_than', name: 'is greater than' },
                    { value: 'is_less_than', name: 'is less than' },
                    { value: 'is_less_than_or_equal_to', name: 'is less than or equal to' },
                    { value: 'is_equal_to', name: 'is equal to' },
                    { value: 'is_in_the_range', name: 'is in the range'}
                  ],
                  onChange: function(value, options, scope) {
                  }
                }
              },
              {
                key: 'parameters[1]', // from value
                type: 'input',
                className: 'inline-field inline-field-xs',
                templateOptions: {
                  label: '',
                  required: true
                }
              },
              {
                template: 'to',
                className: 'inline-field',
                hideExpression: 'model.name.length > 0 && model.name !== "is_in_the_range"'
              },
              {
                key: 'parameters[2]', // to value
                type: 'input',
                className: 'inline-field inline-field-xs',
                hideExpression: 'model.name.length > 0 && model.name !== "is_in_the_range"',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            id: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'is_equal_to'
                },
                templateOptions: {
                  required: true,
                  label: '',
                  options: [
                    { value: 'is_greater_than_or_equal_to', name: 'is greater than or equal to' },
                    { value: 'is_greater_than', name: 'is greater than' },
                    { value: 'is_less_than', name: 'is less than' },
                    { value: 'is_less_than_or_equal_to', name: 'is less than or equal to' },
                    { value: 'is_equal_to', name: 'is equal to' },
                    { value: 'is_in_the_range', name: 'is in the range'}
                  ],
                  onChange: function(value, options, scope) {
                  }
                }
              },
              {
                key: 'parameters[1]', // from value
                type: 'input',
                className: 'inline-field inline-field-xs',
                templateOptions: {
                  label: '',
                  required: true
                }
              },
              {
                template: 'to',
                className: 'inline-field',
                hideExpression: 'model.name.length > 0 && model.name !== "is_in_the_range"'
              },
              {
                key: 'parameters[2]', // to value
                type: 'input',
                className: 'inline-field inline-field-xs',
                hideExpression: 'model.name.length > 0 && model.name !== "is_in_the_range"',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            gene_name: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'contains'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'contains', name: 'contains'},
                    {value: 'begins_with', name: 'begins with'},
                    {value: 'does_not_contain', name: 'does not contain'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            variant_name: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'contains'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is', name: 'is'},
                    {value: 'contains', name: 'contains'},
                    {value: 'begins_with', name: 'begins with'},
                    {value: 'does_not_contain', name: 'does not contain'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            submitter: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'contains'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'contains', name: 'contains'},
                    {value: 'begins_with', name: 'begins with'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ],
            submitter_id: [
              {
                key: 'name',
                type: 'queryBuilderSelect',
                className: 'inline-field',
                data: {
                  defaultValue: 'is_equal_to'
                },
                templateOptions: {
                  label: '',
                  required: true,
                  options: [
                    {value: 'is_equal_to', name: 'is'},
                    {value: 'is_not_equal_to', name: 'is not'}
                  ]
                }
              },
              {
                key: 'parameters[0]',
                type: 'input',
                className: 'inline-field',
                templateOptions: {
                  label: '',
                  required: true
                }
              }
            ]
          }
        }
      }
	];

  }
})();
