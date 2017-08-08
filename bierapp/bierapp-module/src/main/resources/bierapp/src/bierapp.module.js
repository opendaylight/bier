define([
  'app/routingConfig', 
  'Restangular', 
  'angular-ui-router',
  'app/core/core.services',
  'angular-material',
  'lodash'], function() {

  var bierapp = angular.module('app.bierapp', [
        'restangular', 
        'ui.router.state',
        'app.core',
        'ngMaterial'
        ]);

  bierapp.register = bierapp;

  bierapp.config(function($stateProvider, $compileProvider, $controllerProvider, $provide, NavHelperProvider, $filterProvider, $urlRouterProvider, $mdThemingProvider) {
    bierapp.register = {
        controller : $controllerProvider.register,
        directive : $compileProvider.directive,
        filter: $filterProvider.register,
        factory : $provide.factory,
        service : $provide.service
    };
    console.log('bier start');

    $urlRouterProvider.otherwise('/bierapp/index');

    NavHelperProvider.addControllerUrl('app/bierapp/src/bierapp.controller');
    NavHelperProvider.addToMenu('bierapp', {
     "link" : "#/bierapp/index",
     "active" : "main.bierapp",
     "title" : "BIERAPP",
     "icon" : "icon-beer",
     "page" : {
        "title" : "BIERAPP",
        "description" : "BIERAPP"
     }
    });

    var access = routingConfig.accessLevels;
    $stateProvider.state('main.bierapp', {
      url: 'bierapp',
      abstract: true,
      views : {
        'content' : {
          templateUrl: 'src/app/bierapp/src/root.tpl.html',
        }
      }
    });

    $stateProvider.state('main.bierapp.index', {
              url: '/index',
              access: access.admin,
              views: {
                  '': {
                      controller: 'biermanCtrl',
                      templateUrl: 'src/app/bierapp/src/index.tpl.html'
                  }
              }
          });

    $mdThemingProvider.theme('default')
      .primaryPalette('blue')
      .accentPalette('light-blue');




  });

  return bierapp;
});
