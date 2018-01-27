var request = require("request");

module.exports = function(app, passport) {

    // =====================================
    // HOME PAGE (with login links) ========
    // =====================================
    app.get('/', function(req, res) {
        res.render('index.ejs'); // load the index.ejs file
    });

    // =====================================
    // LOGIN ===============================
    // =====================================
    // show the login form
    app.get('/login', function(req, res) {

        // render the page and pass in any flash data if it exists
        res.render('login.ejs', { message: req.flash('loginMessage') }); 
    });

    // process the login form
    // app.post('/login', do all our passport stuff here);

    // =====================================
    // SIGNUP ==============================
    // =====================================
    // show the signup form
    app.get('/signup', function(req, res) {

        // render the page and pass in any flash data if it exists
        res.render('signup.ejs', { message: req.flash('signupMessage') });
    });

    // process the signup form
    // app.post('/signup', do all our passport stuff here);

    // =====================================
    // PROFILE SECTION =====================
    // =====================================
    // we will want this protected so you have to be logged in to visit
    // we will use route middleware to verify this (the isLoggedIn function)
    app.get('/profile', isLoggedIn, function(req, res) {
        res.render('profile.ejs', {
            user : req.user // get the user out of session and pass to template
        });
    });

    // =====================================
    // LOGOUT ==============================
    // =====================================
    app.get('/logout', function(req, res) {
        req.logout();
        res.redirect('/');
    });

    // process the signup form
    app.post('/signup', passport.authenticate('local-signup', {
        successRedirect : '/profile', // redirect to the secure profile section
        failureRedirect : '/signup', // redirect back to the signup page if there is an error
        failureFlash : true // allow flash messages
    }));

    app.post('/login', passport.authenticate('local', {
        successRedirect : '/profile', // redirect to the secure profile section
        failureRedirect : '/login', // redirect back to the signup page if there is an error
        failureFlash : true // allow flash messages
    }));
};

// route middleware to make sure a user is logged in
function isLoggedIn(req, res, next) {

    // if user is authenticated in the session, carry on 
    var contract = web3.eth.contract(interface);
    var instance = contract.at(contractAddress);
    console.log("HELLO");
    var email = req["user"]["local"]["email"];
    console.log(email);
    instance.Query_access_username.call("bank", email, callback=function(err, result) {
        if(err) {
            console.log(err);
        } else {
            console.log('Query result:');
            console.log(result);

            if(result) {
                
                var options = { 
                  method: 'POST',
                  url: 'http://ec2-54-224-142-62.compute-1.amazonaws.com:3000/query_access',
                  headers: 
                     {'Cache-Control': 'no-cache',
                     'Content-Type': 'application/x-www-form-urlencoded' },
                  form: 
                   { entity: 'Test',
                     public_key: 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLTODWwO+DOVr2S9KB+NBGV4oTsBfoqMlqIWMZ\nbmSsRW5gs93Ue4PFjyF0POlUEI/YZa04gvAQZGoPv0OhLobmTw9IUmFto1AOcX4/iHwSnMmj3u11\nyckiCqWUap06w4dbqLz9pWfekIxLP2S35z1hhebFWKKi0XBMCo/5YXZBzQIDAQAB',
                     challenge: 'nHRnaorhjzrDfAIDxL3J10G6YrEmHS43cAlLkvPdv+WrG+qVe/SE0TtJpdPFeDFWxJh0O/wnl3B30fMVdQJvMvb83pan2lOm4+1eXIuoq9KGW6niLMQEXkYTjXzJyIa17lRG1xXKiNfxH4vLhS3y18EuWQdO/5rru/AaI9AE2Mw=',
                     callback_url: 'callback',
                     resource: 'MIT' } };
                
                request(options, function (error, response, body) {
                  if (error) throw new Error(error);
                
                  console.log(body);
                });
                return next();
            }
            //User.findOne({ 'local.email' :  email }, function(err, user) {
            //    return done(null, user);
            //});
        }
        res.redirect('/login');
    });
 
}

