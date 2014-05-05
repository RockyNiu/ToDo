'use strict';

/* Filters */

angular.module('myApp.filters', []).
  filter('hideCompleted', function () {
    return function (tasks) {
      var filteredTasks = [];
        for ( var cnt = 0; cnt < tasks.length; cnt++ ) {
          var task = tasks[cnt];
          if ( task.status == "needsAction" ) {
              filteredTasks.push(task);
          }
      }
      return filteredTasks;
    };
  }).
    filter('hideActive', function () {
        return function (tasks) {
            var filteredTasks = [];
            for ( var cnt = 0; cnt < tasks.length; cnt++ ) {
                var task = tasks[cnt];
                if ( task.status == "completed" ) {
                    filteredTasks.push(task);
                }
            }
            return filteredTasks;
        };
    });
