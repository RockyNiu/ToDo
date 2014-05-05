'use strict';

// Declare app level module which depends on filters, and services

angular.module('myApp', [
  'myApp.controllers',
  'myApp.filters',
  'myApp.services',
  'myApp.directives',
  'ui.bootstrap',
]).
config(function ($httpProvider) {
  delete $httpProvider.defaults.headers.common["X-Requested-With"];
});
