// config/passport.js

// load all the things we need
var LocalStrategy   = require('passport-local').Strategy;
var Web3 = require('web3');
//var web3 = new Web3();
//var web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//var web3 = new Web3(new Web3.providers.HttpProvider('https://ropsten.infura.io/'));
//web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));

var util = require('ethereumjs-util');
var tx = require('ethereumjs-tx');
var lightwallet = require('eth-lightwallet');
var txutils = lightwallet.txutils;

var web3 = new Web3(
    new Web3.providers.HttpProvider('https://ropsten.infura.io/')
);

var address = "0xD1E90a9E4Cd458CfFe191FCa01fF641832a9C0dB";
var key = "d82530369ecc87b2d9adc9821a4d7246f2bb3cf5c5b55cb3d2224f0138235599";
//
//if (typeof web3 !== 'undefined') {
//  web3 = new Web3(web3.currentProvider);
//} else {
//  // set the provider you want from Web3.providers
//  web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//}
console.log(web3.isConnected());

web3.version.getNetwork((err, netId) => {
  switch (netId) {
    case "1":
      console.log('This is mainnet')
      break
    case "2":
      console.log('This is the deprecated Morden test network.')
      break
    case "3":
      console.log('This is the ropsten test network.')
      break
    default:
      console.log('This is an unknown network.')
  }
})
// load up the user model
var User            = require('../app/models/user');

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
                newUser.local.password = newUser.generateHash(password);

                // save the user
                newUser.save(function(err) {
                    if (err)
                        throw err;
                    return done(null, newUser);
                });

                var contract_address = "0xb27C54D55877Bd5758Df81382C4e93a4061Bc606";

function sendRaw(rawTx) {
    var privateKey = new Buffer(key, 'hex');
    var transaction = new tx(rawTx);
    transaction.sign(privateKey);
    var serializedTx = transaction.serialize().toString('hex');
    web3.eth.sendRawTransaction(
    '0x' + serializedTx, function(err, result) {
        if(err) {
            console.log(err);
        } else {
            console.log(result);
        }
    });
}

                //send blockchain transaction
                var abi = 
[{"constant":true,"inputs":[{"name":"resource","type":"string"},{"name":"key","type":"string"}],"name":"Query_access","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"username","type":"string"},{"name":"key","type":"string"}],"name":"Create_username","outputs":[{"name":"","type":"bool"}],"payable":true,"stateMutability":"payable","type":"function"},{"constant":true,"inputs":[{"name":"resource","type":"string"}],"name":"Create_resource","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"resource","type":"string"},{"name":"my_pub_key","type":"string"},{"name":"their_pub_key","type":"string"}],"name":"Share_access","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"name":"resource","type":"string"},{"name":"pub_key","type":"string"}],"name":"Give_access_to_public_key","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"name":"key","type":"string"}],"name":"List_access_for_user","outputs":[{"components":[{"name":"resource","type":"string"}],"name":"","type":"tuple[]"}],"payable":false,"stateMutability":"view","type":"function"}];
                var myContract =  web3.eth.contract(abi); // '0x8F37c0cF45641bB04eB6c0c4F983bCAD6C3519c4');
                var myContractInstance = myContract.at(contract_address);
                console.log(newUser.local.email);
                var address = "0xD1E90a9E4Cd458CfFe191FCa01fF641832a9C0dB";
                console.log(web3.isAddress(address));


                var txOptions = {
                    nonce: web3.toHex(web3.eth.getTransactionCount(address)),
                    gasLimit: web3.toHex(800000),
                    gasPrice: web3.toHex(20000000000),
                    to: contractAddress
                }
                var rawTx = txutils.functionTx(interface, 'vote', [4], txOptions);
                sendRaw(rawTx);
                //var r = myContractInstance.List_access_for_user("sarah", "fake key blah blah");

                //myContract.Register('address', {from: address});
                //web3.eth.defaultAccount=web3.eth.accounts[0]
                var result = myContractInstance.Create_username("sarah", address, {value: 0, gas: 2000, from: address});

                console.log(result)
               //define callback later 
                
            }

        });    

        });

    }));

    passport.use('local-login', new LocalStrategy({
        // by default, local strategy uses username and password, we will override with email
        usernameField : 'email',
        passwordField : 'password',
        passReqToCallback : true // allows us to pass back the entire request to the callback
    },
    function(req, email, password, done) { // callback with email and password from our form

        // find a user whose email is the same as the forms email
        // we are checking to see if the user trying to login already exists
        User.findOne({ 'local.email' :  email }, function(err, user) {
            // if there are any errors, return the error before anything else
            if (err)
                return done(err);

            // if no user is found, return the message
            if (!user)
                return done(null, false, req.flash('loginMessage', 'No user found.')); // req.flash is the way to set flashdata using connect-flash

            // if the user is found but the password is wrong
            if (!user.validPassword(password))
                return done(null, false, req.flash('loginMessage', 'Oops! Wrong password.')); // create the loginMessage and save it to session as flashdata

            // all is well, return successful user
            return done(null, user);
        });

    }));

};

