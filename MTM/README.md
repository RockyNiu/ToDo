# ToDo Mobile App

ToDo Mobile App is a Task Manager application that is integrated into the [Google Tasks API](https://developers.google.com/google-apps/tasks/) and adds additional features on top of the Google Task view, like the ability to set task due time and priority. It is linked to your Google+ account and requires you to first log in with your Google+ account before you are allowed to use the application. The application has the following features:

- Add items to your task list; (press menu icon on top)
- Set a task name, priority, due date, due time;
- Edit an existing task; (long press)
- Mark a task a completed;
- Delete a task; (swipe)
- Sort tasks by due or priority;
- Reminder you the task past due;
- Synchronize the tasks with mobile version, web version, and Google Tasks.

## Example App

A working version of the app can be found [here](https://play.google.com/store/apps/details?id=com.rockyniu.todolist).

## Build your own project

1) put support\v4 package into /lib folder
the package is here, ANDRIOD_SDK_BUNDEL\Eclipse\sdk\extras\android\support\v4, if you use SDK manager download it.

2) import google_play_services library, you can find the instruction here : http://developer.android.com/google/play-services/setup.html

3) update android_sdk_bundle to latest version, import google_tasks_api