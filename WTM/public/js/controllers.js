'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
  controller('TasksCtrl', function ($scope, $filter, $http, $window, $timeout, taskListService) {

    $scope.$window = $window;
    $scope.error = null;

    // Conditional states
    $scope.isLoading   = true;
    $scope.hasTasks    = false;
    $scope.hasError    = false;
    $scope.editingTask = false;

    $scope.listId = null; // UUID of list

    $scope.hideActive    = false;
    $scope.hideCompleted = false;

    // Reset all default settings for a new task
    var resetNew = function () {
        // Reset all 'add tasks' fields to the default value
        $scope.dueTime = new Date();
        $scope.dueTime.setHours($scope.dueTime.getHours() + 1);
        $scope.dueTime.setMinutes(0);

        // Set due date defaults
        $scope.minDate = new Date();
        $scope.dueDate = new Date();
        $scope.dueDate.setDate($scope.dueDate.getDate()+2);

        // Handle the priority buttons
        $scope.radioModel = 'Medium';

        $scope.taskName = "";
    }

    // Reset all scope variables for a task being edited
    var resetEditing = function (task) {

        var taskDate = null;
        if ( task.notes && task.notes.match(/^Due /) ) {
            taskDate = task.notes.replace(/^Due /, '');
            taskDate = (new Date()).setISO8601(taskDate);
        }

        if ( !taskDate && task.due ) {
            taskDate = (new Date()).setISO8601(task.due);
        }

        if ( !taskDate ) {
           taskDate = new Date();
           taskDate.setHours(taskDate.getHours() + 1);
           taskDate.setMinutes(0);
        }

        $scope.editTime = taskDate;
        $scope.editDate = taskDate;

        if ( task.isEmpty ) {
            $scope.editTitle = "";
        } else {
            $scope.editTitle = task.title;
        }

        // Handle the priority buttons
        if ( task.priority == "High Priority" ) {
            $scope.editModel = 'High';
        } else if ( task.priority == "Medium Priority" ) {
            $scope.editModel = 'Medium';
        } else {
            $scope.editModel = 'Low';
        }
    }

    // Find a task with the given task ID
    var findTask = function (taskId) {
        var task = null;
        for ( var cnt = 0; cnt < $scope.tasks.length; cnt++ ) {
            var task = $scope.tasks[cnt];
            if (task.id == taskId ) {
                return task;
            }
        }

        return null;
    }

    // Apply all filters to the task list
    var applyFilters = function () {
        var tasks = $scope.tasks;
        if ( $scope.hideActive ) {
           // Hide all the active tasks
           tasks = $filter('hideActive')(tasks);
        }

        if ( $scope.hideCompleted ) {
            // Hide all the completed tasks
            tasks = $filter('hideCompleted')(tasks);
        }

        $scope.filteredTasks = tasks;
        $scope.hasTasks      = tasks.length > 0;
    }

    // Handle the editing of an existing task and auto-magically update
    // the labels inline with the task list to reflect changes as they happen

    $scope.$watch('editModel', function() {
        if ( $scope.editingTask ) {
            $scope.editingObject.priority = $scope.editModel + " Priority";
        }
    });

    var updatedEditedItemDueString = function () {
        if ( $scope.editingTask ) {
            var editTime = $scope.editTime;
            var editDate = $scope.editDate;

            var newDueDateTime = new Date($scope.editDate.toDateString());
            newDueDateTime.setHours($scope.editTime.getHours());
            newDueDateTime.setMinutes($scope.editTime.getMinutes());

            $scope.editingObject.due       = newDueDateTime.getISODateString();
            $scope.editingObject.notes     = "Due " + $scope.editingObject.due;
            $scope.editingObject.dueString = newDueDateTime.toLocaleString().replace(/:\d{2}\s/,' ');
        }
    }

    $scope.$watch('editTime', updatedEditedItemDueString);
    $scope.$watch('editDate', updatedEditedItemDueString);

    // Set due time defaults
    $scope.hstep = 1;
    $scope.mstep = 15;
    $scope.ismeridian = true;

    $scope.openCalendar = function ($event) {
      $event.preventDefault();
      $event.stopPropagation();

      $scope.opened = true;
    };

    $scope.dateOptions = {
      'year-format': "'yy'",
      'starting-day': 1
    };
    $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
    $scope.format = $scope.formats[0];

    resetNew();

    taskListService.
    fetchAll($scope.token, $scope.api_key).
    success(function (data, status, headers, config) {

      console.log("Successfully fetched task lists...");

      // Now fetch our private task list
      taskListService.
      fetchOrCreate(data, $scope.token, $scope.api_key, function (listId) { $scope.listId = listId; return; }).
      success(function (data, status, headers, config) {
        console.log("Successfully fetched task list...[Data:" + JSON.stringify(data) + "]");

        // Add our special properties to the tasks
        if ( data.items ) {
            for ( var cnt = 0; cnt < data.items.length; cnt++ ) {
                var task = data.items[cnt];
                taskListService.addCustomTaskProperties(task, $timeout);
            }
        } else {
            data.items = [];
        }

        $scope.tasks         = data.items;
        $scope.filteredTasks = $scope.tasks;
        $scope.isLoading     = false;
        $scope.hasTasks      = data.items && data.items.length > 0;
        $scope.hasError      = false;
      }).
      error(function (data, status, headers, config) {
        console.log("Error fetching task list...[Error:" + JSON.stringify(data) + "]");
        $scope.error = 'Error! Unable to load list. [Error ' + data + ']' ;
        $scope.isLoading = false;
        $scope.hasError = true;
      });

    }).
    error(function (data, status, headers, config) {
        console.log("Error fetching tasks...[Error:" + JSON.stringify(data) + "]");
        $scope.error = 'Error! Unable to load list.' ;
        $scope.isLoading = false;
        $scope.hasError = true;

        if( status == 401 )
        {
            // Not logged in any longer
            $window.location.href = "../login";
        }
    });

    // Add a new task
    $scope.addTask = function () {
        console.log("Adding a task to the list");

        // Collect up the due date, due time, task name, and priority
        var taskNameListener = null;
        if ( $scope.taskName.trim().length == 0 ) {

            // Task name is blank, highlight the field
            $scope.taskNameIsInvalid = true;

            taskNameListener && taskNameListener();
            taskNameListener = $scope.$watch('taskName', function() {
                $scope.taskName.trim().length == 0 ? $scope.taskNameIsInvalid = true : $scope.taskNameIsInvalid = false;
            });

            return;
        }
        taskNameListener && taskNameListener();
        $scope.taskNameIsInvalid = false;


        // Make sure due date is in the future
        var newDueDateTime = new Date($scope.dueDate.toDateString());
        newDueDateTime.setHours($scope.dueTime.getHours());
        newDueDateTime.setMinutes($scope.dueTime.getMinutes());

        if (newDueDateTime < (new Date())) {
            $window.alert("Due date cannot be in the past.");

            var modifiedDate = new Date();
            modifiedDate.setDate(modifiedDate.getDate()+2);
            $scope.dueDate = modifiedDate;

            return;
        }

        // Adding our new task to the list
        taskListService.
            addTask($scope.listId, $scope.dueDate, $scope.dueTime, $scope.radioModel, $scope.taskName, $scope.token, $scope.api_key).
            success(function (data, status, headers, config) {
                console.log("Successfully added task to list...[Data:" + JSON.stringify(data) + "]");
                taskListService.addCustomTaskProperties(data, $timeout);
                $scope.tasks.push(data);
                applyFilters();

                // Reset all the fields so you can add the next task
                resetNew();
            }).
            error(function (data, status, headers, config) {
                console.log("Error unable to add task to list...[Error:" + JSON.stringify(data) + "]");
                $window.alert("An error occurred while attempting to add the task to the task list.");
            });
    };

    $scope.deleteTask = function (taskId) {
            console.log("Removing a task to the list");

            taskListService.
                deleteTask($scope.listId, taskId, $scope.token, $scope.api_key).
                success(function (data, status, headers, config) {
                    console.log("Successfully deleted task from the list...[Data:" + JSON.stringify(data) + "]");

                    for ( var cnt = 0; cnt < $scope.tasks.length; cnt++ ) {
                        var task = $scope.tasks[cnt];
                        if (task.id == taskId ) {
                            // Remove task from the list
                            $scope.tasks.splice(cnt,1);

                            // Cancel any due date timers
                            if ( task.pastDueTimer ) {
                                $timeout.cancel(task.pastDueTimer);
                            }

                            break;
                        }
                    }

                    applyFilters();
                }).
                error(function (data, status, headers, config) {
                    console.log("Error unable to remove task from list...[Error:" + JSON.stringify(data) + "]");
                    $scope.Alert="An error occurred while attempting to delete a task from the task list.";
                });
        };

    $scope.editTask = function (taskId) {
        console.log("Editing a task to the list");

        // Find the task
        var task = findTask(taskId);

        // Set the editing field to the defaults
        resetEditing(task);

        // Turn on the editing fields
        $scope.editingId          = taskId;
        $scope.editingObject      = task;
        $scope.editingTask        = true;
        $scope.editingTaskWatcher = $scope.$watch('editTitle', function() {
            // When the task title changes, we need to possibly modify the isEmpty property

            $scope.editingObject.title = $scope.editTitle;
            taskListService.addCustomTaskProperties($scope.editingObject, $timeout);

            if ( task.isEmpty ) {
                $scope.editTitle         = "";
                $scope.taskNameIsInvalid = true;
            } else {
                $scope.editTitle         = task.title;
                $scope.taskNameIsInvalid = false;
            }
        });
    };

    $scope.saveTask = function () {
        console.log("Saving an edited task to the list");

        if ( $scope.taskNameIsInvalid ) {
            return;
        }

        // Make sure due date is in the future
        var newDueDateTime = new Date($scope.editDate.toDateString());
        newDueDateTime.setHours($scope.editTime.getHours());
        newDueDateTime.setMinutes($scope.editTime.getMinutes());

        if (newDueDateTime < (new Date())) {
            $window.alert("Due date cannot be in the past.");

            var modifiedDate = new Date();
            modifiedDate.setDate(modifiedDate.getDate()+2);
            $scope.editDate = modifiedDate;

            return;
        }

        taskListService.
            saveTask($scope.listId, $scope.editDate, $scope.editTime, $scope.editModel, $scope.editTitle, $scope.editingObject, $scope.token, $scope.api_key).
            success(function (data, status, headers, config) {
                console.log("Successfully edited task...[Data:" + JSON.stringify(data) + "]");

                // Hide the editing task fields and show the new task fields
                resetNew();
                taskListService.addCustomTaskProperties($scope.editingObject, $timeout);
                $scope.editingId     = null;
                $scope.editingObject = null;
                $scope.editingTask   = false;
                $scope.editingTaskWatcher(); // Unregister the watcher
            }).
            error(function (data, status, headers, config) {
                console.log("Error unable to edit task...[Error:" + JSON.stringify(data) + "]");
                $window.alert("An error occurred while attempting to edit the task.");
            });
    };

    $scope.completeTask = function (taskId) {
        console.log("Marking task as completed");

        var task = findTask(taskId);

        taskListService.
            markCompleted($scope.listId, task, $scope.token, $scope.api_key).
            success(function (data, status, headers, config) {
                console.log("Successfully marked task as completed...[Data:" + JSON.stringify(data) + "]");

                taskListService.addCustomTaskProperties(task, $timeout);

            }).
            error(function (data, status, headers, config) {
                console.log("Error unable to mark task as completed...[Error:" + JSON.stringify(data) + "]");

                // Revert to previous status
                if ( task.status == "completed" ) {
                    task.status = "needsAction";
                } else {
                    task.status = "completed";
                }
                taskListService.addCustomTaskProperties(task, $timeout);

                $window.alert("An error occurred while attempting to mark a task as completed.");
            });
    };

    $scope.filterCompleted = function () {
        console.log("Filtering the completed tasks");
        applyFilters();
    };

    $scope.filterActive = function () {
        console.log("Filtering the active tasks");
        applyFilters();
    };

    $scope.$on( "$destroy", function( event ) {
            // Clean up any pending due date timers on the tasks
            for ( var cnt = 0; cnt < $scope.tasks.length; cnt++ ) {
                var task = $scope.tasks[cnt];
                if ( task.pastDueTimer ) {
                    $timeout.cancel(task.pastDueTimer);
                }
            }
        }
    );

  });
