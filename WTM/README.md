# ToDo Web App

ToDo Web App is a Task Manager application that is integrated into the [Google Tasks API](https://developers.google.com/google-apps/tasks/)
and adds additional features on top of the Google Task view like the ability to set task due times
and priority. It is linked to your Google+ account and requires you to first log in with your Google+
account before you are allowed to use the application. The application has the following features

- Add items to your task list
- Set a task due date, due time, priority, and title
- Edit an existing task
- Mark a task a completed
- Delete a task
- Synchronize the tasks with mobile version, web version, and Google Tasks.

### Prerequisites

- [Node Package Manager](https://www.npmjs.org/)
- [Bower](http://bower.io/)
- [Grunt](http://gruntjs.com/getting-started)

### Running the app

Runs using [Grunt](http://gruntjs.com/getting-started):

    grunt start

## Directory Layout
    
    app.js                               --> app config
    package.json                         --> for npm
    public/                              --> all of the files to be used in on the client side
      css/                               --> css files
        app.css                          --> default stylesheet
        bootstrap.min.css                --> Twitter Bootstrap CSS
        bootstrap-timepicker.css         --> timer picker control for Twitter Bootstrap
      images/                            --> image files
        loading.gif                      --> loading spinner show when tasks are loading
        Red-signin-Google_base_32dp.png  --> official Google+ login button
        Red-signin-Google_hover_32dp.png --> official Google+ login button (hover state)
        Red-signin-Google_press_32dp.png --> official Google+ login button (pressed state)
      js/               --> javascript files
        lib/            --> angular and 3rd party JavaScript libraries
        app.js          --> declare top-level app module
        controllers.js  --> application controllers
        directives.js   --> custom angular directives
        filters.js      --> custom angular filters
        services.js     --> custom angular services
    routes/
      index.js          --> route for serving HTML pages
    views/
      index.jade        --> main page for app
      layout.jade       --> doctype, title, header/navbar
      tasks.jade        --> the primary tasks view

## Example App

A hosted version of the app can be found [here](http://tode-web.herokuapp.com/).
