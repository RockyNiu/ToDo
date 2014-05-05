module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    concat: {
      options: {
        separator: ';'
      },
      dist: {
        src: ['public/js/*.js', 'routes/*.js', 'views/*.js', 'views/*.jade', '!docroot/js/*.min.js'],
        dest: 'dist/<%= pkg.name %>.js'
      }
    },
    uglify: {
      options: {
        banner: '/*! <%= pkg.name %> <%= grunt.template.today("dd-mm-yyyy") %> */\n'
      },
      dist: {
        files: {
          'dist/<%= pkg.name %>.min.js': ['<%= concat.dist.dest %>']
        }
      }
    },
    qunit: {
      files: ['test/**/*.html']
    },
    jshint: {
      files: ['Gruntfile.js', 'app.js', 'public/js/*.js', 'routes/*.js', 'views/*.js', 'views/*.jade', 'test/**/*.js', '!docroot/js/bootstrap.min.js'],
      options: {
        globals: {
          jQuery: true,
          console: true,
          module: true,
          document: true
        }
      }
    },
    watch: {
      options: {
      livereload: true,
      nospawn: true,
      },
      files: ['<%= jshint.files %>'],
      tasks: ['jshint', 'qunit']
    }
  });

  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-qunit');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-concat');

  // The 'test' task will run hinting and any unit tests
  grunt.registerTask('test', ['jshint', 'qunit']);

  // The 'default' task will run hinting, any unit tests, and uglify the css files
  grunt.registerTask('default', ['jshint', 'qunit', 'concat', 'uglify']);
  
  // The 'start' task will start listening for changes in associated files and reload the app when anything changes
  grunt.registerTask('start', function () {
    grunt.task.run('watch');
    require('./app.js');
  });
};