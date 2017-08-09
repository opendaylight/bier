require.config({

    paths: {
        'angular': 'app/bierapp/vendor/angular/angular.min',
        'angular-material': 'app/bierapp/vendor/angular-material/angular-material.min',
        'angular-animate': 'app/bierapp/vendor/angular-animate/angular-animate.min',
        'angular-aria': 'app/bierapp/vendor/angular-aria/angular-aria.min',
        'angular-messages': 'app/bierapp/vendor/angular-messages/angular-messages.min',
        'next': 'app/bierapp/vendor/NeXt/js/next.min',
        'jquery': 'app/bierapp/vendor/jquery/dist/jquery.min',
        'sweetalert': 'app/bierapp/vendor/sweetalert/dist/sweetalert.min',
        //'sweetalert': 'app/bierapp/assets/js/sweetalert.min',
        'checklist-model': 'app/bierapp/vendor/checklist-model/checklist-model',
        'tools': 'app/bierapp/lib/tools',
        'Restangular': 'app/bierapp/vendor/restangular/dist/restangular',
        'lodash' : 'app/bierapp/vendor/lodash/lodash'
    },

    shim: {
        'angular' : {
            deps: ['jquery'],
            exports: 'angular'
        },
        'angular-material': ['angular'],
        //'angular-material': ['angular-animate', 'angular-aria'],
        'angular-animate': ['angular'],
        'angular-aria': ['angular'],
        'Restangular': ['angular','lodash'],
        'jquery' : {exports : '$'},
        'lodash' : {exports: '_'}
    },

});

define(['app/bierapp/src/bierapp.module']);
