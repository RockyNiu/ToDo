'use strict';

/* Services */

var BASE_URL  = 'https://www.googleapis.com/tasks';
var TASK_LIST = 'cs6300ToDoList';

angular.module('myApp.services', []).
	service('taskListService', function ($http) {

        // Private support functions
        var updateTaskNameFromPriority = function(priority, taskName) {

            if ( (!taskName || /^\s*$/.test(taskName)) ) {
                // ignore an empty title
                return '';
            }

            switch(priority) {
                case "Low":
                    taskName = taskName.replace(/[\:.!?]+$/, '');
                    taskName = taskName + "."
                    break;
                case "Medium":
                    taskName = taskName.replace(/[\:.!?]+$/, '');
                    taskName = taskName + "!"
                    break;
                case "High":
                    taskName = taskName.replace(/[\:.!?]+$/, '');
                    taskName = taskName + "!!"
                    break;
            }

            return taskName;
        }

		// Returns a promise to fetch all task lists
		this.fetchAll = function (token, api_key) {
			console.log("fetching task lists...");
			var url = BASE_URL + "/v1/users/@me/lists/" + "?key=" + api_key;

			return $http({
				method: 'GET',
				url: url,
				headers: {'Authorization': 'Bearer '+ token }
			});
		};


		// If the list hasn't been created yet, this will create it
		this.create = function (token, api_key) {
			console.log("creating task list...");
			var url = BASE_URL + "/v1/users/@me/lists/" + "?key=" + api_key;
			return $http({
				method: 'POST',
				url: url,
				headers: {'Authorization': 'Bearer '+ token },
				data: {
					'kind': 'tasks#taskList',
					'title': TASK_LIST
				} 
			});
		};

		// Given JSON data from the all lists fetch, return a promise
		// to either fetch our task list or create a new one.
		this.fetchOrCreate = function (data, token, api_key, setList) {

			if (data == undefined || !data) {
				throw 'data in undefined';
				return null;
			}

			console.log("searching for our task list...");
			for (var cnt = 0; cnt < data.items.length; cnt++ ) {
                var list = data.items[cnt];
				console.log("Found list" + JSON.stringify(list));
				if ( list.title == TASK_LIST ) {
					// Yah! we found our list.
                    setList(list.id);
					var url = BASE_URL + "/v1/lists/" + list.id + "/tasks?key=" + api_key;
					return $http({
						method: 'GET',
						url: url,
						headers: {'Authorization': 'Bearer '+ token }
					});
				}
			}

			// Our task list hasn't been found, create it
			return this.create(token, api_key);
		};

        this.addCustomTaskProperties = function (task, $timeout) {

            task.checked = (task.status == "completed"); // true if task is completed

            // Figure out task priority
            if ( task.title.match(/!!$/) ) {
                task.priority = "High Priority";
            } else if ( task.title.match(/!$/) ) {
                task.priority = "Medium Priority";
            } else {
                task.priority = "Low Priority";
            }

            var dueDate = null;
            if ( task.notes && task.notes.match(/^Due /) ) {
                dueDate = task.notes.replace(/^Due /, '');
                dueDate = (new Date()).setISO8601(dueDate);
            }

            if ( !dueDate && task.due ) {
                dueDate = (new Date()).setISO8601(task.due);
            }

            if ( dueDate ) {
                task.dueString  = dueDate.toLocaleString().replace(/:\d{2}\s/,' ');
                task.hasDueDate = true;
            } else {
                task.dueString  = null;
                task.hasDueDate = false;
            }

            if ( task.completed ) {
                task.completedString = (new Date()).setISO8601(task.completed).toLocaleString().replace(/:\d{2}\s/,' ');
            }

            var title = task.title;
            task.isEmpty = (!title || /^\s*$/.test(title));

            if ( task.isEmpty ) {
                // Empty task waiting to be done
                task.title = "To be done";
            }

            // Check to see if the task is over due already or not,
            // If not, set a timer to fire when it is
            var now = new Date();
            if ( task.hasDueDate && dueDate < now && !task.completed ) {
                task.pastDue = true;
            } else if ( task.hasDueDate && task.pastDueTimer == null && !task.completed ) {
                task.pastDue = false;
                var diff = (dueDate - now);

                task.pastDueTimer =  $timeout( function() {

                    if ( !task.completed ) {
                        task.pastDue = true;
                    }

                }, diff);

            } else {

                // Cancel any old due date timers
                if ( task.pastDueTimer ) {
                    $timeout.cancel(task.pastDueTimer);
                }

                task.pastDue = false;
            }

            return;
        };

        // Add a task to the given list
        this.addTask = function (listId, dueDate, dueTime, priority, taskName, token, api_key) {

            if (listId == undefined || listId == '') {
                throw 'invalid task list id';
                return null;
            }

            // Combine the due date + due time
            var newDueDateTime = new Date(dueDate.toDateString());
            newDueDateTime.setHours(dueTime.getHours());
            newDueDateTime.setMinutes(dueTime.getMinutes());

            taskName = updateTaskNameFromPriority(priority, taskName);
            var newTask = {
                kind: "tasks#task",
                title: taskName,
                status: 'needsAction',
                due: newDueDateTime.getISODateString(),
                notes: "Due "+newDueDateTime.getISODateString()
            };

            console.log("adding new task "+ newTask);
            var url = BASE_URL + "/v1/lists/" + listId + "/tasks?key=" + api_key;
            return $http({
                method: 'POST',
                url: url,
                headers: {'Authorization': 'Bearer '+ token },
                data: newTask
            });

        };

        // Save an edited task to the given list
        this.saveTask = function (listId, dueDate, dueTime, priority, title, task, token, api_key) {

            if (listId == undefined || listId == '') {
                throw 'invalid task list id';
                return null;
            }

            // Combine the due date + due time
            var newDueDateTime = new Date(dueDate.toDateString());
            newDueDateTime.setHours(dueTime.getHours());
            newDueDateTime.setMinutes(dueTime.getMinutes());

            task.title = updateTaskNameFromPriority(priority, title);
            task.due   = newDueDateTime.getISODateString();
            task.notes = "Due "+newDueDateTime.getISODateString();

            var url = BASE_URL + "/v1/lists/" + listId + "/tasks/" + task.id + "?key=" + api_key;
            return $http({
                method: 'PUT',
                url: url,
                headers: {'Authorization': 'Bearer '+ token },
                data: task
            });

        };

        // Delete a task in the given list
        this.deleteTask = function (listId, taskId, token, api_key) {

            if (listId == undefined || listId == '') {
                throw 'invalid task list id';
                return null;
            }

            if (taskId == undefined || taskId == '') {
                throw 'invalid task id';
                return null;
            }

            var url = BASE_URL + "/v1/lists/" + listId + "/tasks/" + taskId + "?key=" + api_key;
            return $http({
                method: 'DELETE',
                url: url,
                headers: {'Authorization': 'Bearer '+ token }
            });

        };


        // Marks task as completed
        this.markCompleted = function (listId, task, token, api_key) {

            if ( task.status == "completed" ) {
                task.status    = "needsAction";
                task.completed = null;
            } else {
                task.status    = "completed";
                task.completed = (new Date()).getISODateString();
            }

            var url = BASE_URL + "/v1/lists/" + listId + "/tasks/" + task.id + "?key=" + api_key;
            return $http({
                method: 'PUT',
                url: url,
                headers: {'Authorization': 'Bearer '+ token },
                data: task
            });

        };
	});
