// config/passport.js

var LocalStrategy   = require('passport-local').Strategy;
var util = require('ethereumjs-util');
var tx = require('ethereumjs-tx');
var lightwallet = require('eth-lightwallet');
var txutils = lightwallet.txutils;
// load up the user model
var User = require('../app/models/user');
var request = require("request");
var QRCode = require('qrcode')

var pending = 0;


function sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
}

function sendRaw(rawTx) {
    var privateKey = new Buffer(key, 'hex');
    var transaction = new tx(rawTx);
    transaction.sign(privateKey);
    var serializedTx = transaction.serialize().toString('hex');
    web3.eth.sendRawTransaction(
    '0x' + serializedTx, async function(err, result) {
        if(err) {
            console.log(err);
        } else {
            //console.log('Success! result below');
            console.log(result);
            var x = null;
            console.log("Waiting for transaction reciept...");
            while(x == null) {
                x = web3.eth.getTransactionReceipt(result);
                console.log('waiting');
                await sleep(10000);
            }
            console.log("Transaction confirmed!");
            console.log(x)
        }
    });
}

// expose this function to our app using module.exports
module.exports = function(passport) {

    // =========================================================================
    // passport session setup ==================================================
    // =========================================================================
    // required for persistent login sessions
    // passport needs ability to serialize and unserialize users out of session

    // used to serialize the user for the session
    passport.serializeUser(function(user, done) {
        done(null, user.id);
    });

    // used to deserialize the user
    passport.deserializeUser(function(id, done) {
        User.findById(id, function(err, user) {
            done(err, user);
        });
    });

    // =========================================================================
    // LOCAL SIGNUP ============================================================
    // =========================================================================
    // we are using named strategies since we have one for login and one for signup
    // by default, if there was no name, it would just be called 'local'

    passport.use('local-signup', new LocalStrategy({
        // by default, local strategy uses username and password, we will override with email
        usernameField : 'email',
        passwordField : 'password',
        passReqToCallback : true // allows us to pass back the entire request to the callback
    },
    function(req, email, password, done) {

        // asynchronous
        // User.findOne wont fire unless data is sent back
        process.nextTick(function() {

        // find a user whose email is the same as the forms email
        // we are checking to see if the user trying to login already exists
        User.findOne({ 'local.email' :  email }, function(err, user) {
            // if there are any errors, return the error
            if (err)
                return done(err);

            // check to see if theres already a user with that email
            if (user) {
                return done(null, false, req.flash('signupMessage', 'That email is already taken.'));
            } else {

                // if there is no user with that email
                // create the user
                var newUser            = new User();

                // set the user's local credentials
                newUser.local.email    = email;
                newUser.local.password = password;

                pending = 0;
                //store iin blockchain

                var txOptions = {
                    nonce: web3.toHex(web3.eth.getTransactionCount(address, 'pending')),
                    gasLimit: web3.toHex(800000),
                    gasPrice: web3.toHex(40000000000),
                    to: contractAddress
                }
                console.log(txOptions);
                var rawTx = txutils.functionTx(interface, 'Create_username', [newUser.local.email, newUser.local.password], txOptions);
                console.log('create username');
                sendRaw(rawTx);



                //var txOptions1 = {
                //    nonce: web3.toHex(1 + web3.eth.getTransactionCount(address, 'pending')),
                //    gasLimit: web3.toHex(800000),
                //    gasPrice: web3.toHex(40000000000),
                //    to: contractAddress
                //}
                //console.log(txOptions1);
                //var rawTx1 = txutils.functionTx(interface, 'Create_resource', ["bank"], txOptions1);
                //console.log('create resource');
                //sendRaw(rawTx1);
                //console.log('key access');
                
                var txOptions2 = {
                    nonce: web3.toHex(1 + web3.eth.getTransactionCount(address, 'pending')),
                    gasLimit: web3.toHex(800000),
                    gasPrice: web3.toHex(40000000000),
                    to: contractAddress
                }
                console.log(txOptions2);
                var rawTx2 = txutils.functionTx(interface, 'Give_access_to_public_key', ["bank", newUser.local.password], txOptions2);
                sendRaw(rawTx2);
                
          
                // save the user
                newUser.save(function(err) {
                    if (err)
                        throw err;
                    return done(null, newUser);
                });

           }

        });    

        });

    }));

    passport.use( new LocalStrategy({
        // by default, local strategy uses username and password, we will override with email
        usernameField : 'email',
        passwordField : 'email',
        passReqToCallback : true // allows us to pass back the entire request to the callback
    },
    function(req, email, password, done) { // callback with email and password from our form
        // find a user whose email is the same as the forms email
        // we are checking to see if the user trying to login already exists
        //
        //
        console.log('trying to log in');

        // QUERY FROM BLOCKCHAIN
        var contract = web3.eth.contract(interface);
        var instance = contract.at(contractAddress);
        instance.Query_access_username.call("bank", email, callback=function(err, result) {
            if(err) 
                return done(err);

            console.log('Other Query result:');
            console.log(result);

            if(result) {
                User.findOne({ 'local.email' :  email }, function(err, user) {
                    console.log("finding user");
                    if(err) 
                        return done(err);
                    if(!user)
                        return done(null, false, req.flash('loginMessage', 'No user found.'));
                                
                    var options = {
                        method: 'POST',
                        url: 'http://ec2-54-173-230-137.compute-1.amazonaws.com:3000/query_access',
                        headers:
                          {'Cache-Control': 'no-cache',
                          'Content-Type': 'application/x-www-form-urlencoded' },
                        form: { 
                            entity: 'Test',
                            public_key: 'test',
                            challenge: 'nHRnaorhjzrDfAIDxL3J10G6YrEmHS43cAlLkvPdv+WrG+qV    e/SE0TtJpdPFeDFWxJh0O/wnl3B30fMVdQJvMvb83pan2lOm4+1eXIuoq9KGW6niLMQEXkYTjXzJyIa17    lRG1xXKiNfxH4vLhS3y18EuWQdO/5rru/AaI9AE2Mw=',
                            callback_url: 'callback',
                            resource: 'MIT' 
                        } 
                    };

    
                    request(options, function (error, response, body) {
                        if (error) throw new Error(error);
                        console.log('making request');
                        console.log(body);
                        return done(null, user);
                    });
                });
            } else {
                return done(null, false, req.flash('loginMessage', 'Result=False. No user found.    '));            
            }
        });

    }));

};

