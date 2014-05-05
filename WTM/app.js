
/**
 * Module dependencies
 */

var express	= require('express'),
  routes = require('./routes'),
  http = require('http'),
  path = require('path'),
  config = require('./oauth.js'),
  GoogleStrategy = require('passport-google-oauth').OAuth2Strategy,
  mongoose = require('mongoose'),
  passport = require('passport');

var app = module.exports = express();
var api_key = 'AIzaSyD_lg5I32CR3zRB4CC52jQfc8IOD18JF6Q';

// Connect to our MongoDB instance we're going to use to store user session information 
mongoose.connect('mongodb://pdenney:Test1@ds039037.mongolab.com:39037/todogatech');
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:'));
var User = null;
db.once('open', function callback () {
    console.log('Connected to users database');

    // Create the user object schema
    var userSchema = mongoose.Schema({
        token: String,
        uuid: String,
        given_name: String
    });

    User = mongoose.model('User', userSchema);
});

// serialize and deserialize user object for passport
passport.serializeUser(function(user, done) {
  console.log("Serializing user "+user);
  done(null, user.id);    // id stored in a delicious cookie, mmmm cookies
});

passport.deserializeUser(function(id, done) {
  User.findOne({ uuid:id })
    .exec(function(err, user) {
      if ( err ) {
        console.log("Unable to find user. Error[" + err + "]");
      }

      console.log("Deserialized User Token" + user.token);
      done(err, user);    // Now req.user == user
    });
});

/**
 * Configuration
 */

app.configure(function() {
	app.set('port', process.env.PORT || 8088);
	app.set('views', __dirname + '/views');
	app.set('view engine', 'jade');
	app.use(express.logger());
  app.use(express.json());
	app.use(express.cookieParser());
	app.use(express.bodyParser());
	app.use(express.methodOverride());
	app.use(express.session({ secret: 'gatech_team3.06' }));
	app.use(passport.initialize());
	app.use(passport.session());
	app.use(app.router);
	app.use(express.static(__dirname + '/public'));
});

// development only
if (app.get('env') === 'development') {
  app.use(express.errorHandler());
  app.set('site url', 'http://localhost:8088');
}

// production only
if (app.get('env') === 'production') {
    app.set('site url', 'http://powerful-everglades-4337.herokuapp.com');
}

passport.use(new GoogleStrategy({
    clientID: '1095559941785-o929b0piv15052hij02j78ch6fn4dc47.apps.googleusercontent.com',
    clientSecret: 'QpKdaazcQth2x0KylIAa86vy',
    callbackURL: app.get('site url') + '/auth/google/callback',
    scope: 'https://www.googleapis.com/auth/tasks https://www.googleapis.com/auth/userinfo.profile'
  },
    function(accessToken, refreshToken, profile, done) {
        User.findOne({email: profile._json.email},function(err,usr) {

            console.log('User access token ' + accessToken);

            if ( !usr ) {

                console.log('Creating user');
                usr = new User({
                    token: accessToken,
                    uuid: profile.id,
                    given_name: profile._json.given_name
                });

            } else {
                usr.token      = accessToken;
                usr.uuid       = profile.id;
                usr.given_name = profile._json.given_name;
            }

            usr.save(function(err,usr,num) {
                if(err) {
                    console.log('error saving token ' + err);
                }
            });

            process.nextTick(function() {
                return done(null,profile);
            });
        });
    }
));

/**
 * Routes
 */

// serve index and view partials
app.get('/tasks', ensureAuthenticated , function(req, res){
  res.render('tasks', { token: req.user.token, api_key: api_key, given_name: req.user.given_name });
});

// route to check if user is logged in or not
app.get('/loggedin', function(req, res) {
	res.send(req.isAuthenticated() ? req.user : '0'); 
});

// route to log user in
app.get('/auth/google', passport.authenticate('google') );

app.get('/auth/google/callback',
	passport.authenticate('google', { failureRedirect: '/' }),
	function(req, res) {
		console.log('Redirecting to account details' + req.user);
 		res.redirect('/tasks');
});

app.get('/logout', function(req, res){
	req.logout();
	res.redirect('/');
});

app.get('/', routes.index);

// test authentication
function ensureAuthenticated(req, res, next) {
if (req.isAuthenticated()) { return next(); }
	console.log('User is not logged in, redirecting to login page.' + req.user)
	res.redirect('/');
}

/**
 * Start Server
 */

  http.createServer(app).listen(app.get('port'), function () {
  console.log('Express server listening on port ' + app.get('port'));
});
    

