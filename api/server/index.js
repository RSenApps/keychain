var express = require('express')
var bodyParser = require('body-parser')
 
var app = express()
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({extended: false}))

var levelup = require('levelup')
var leveldown = require('leveldown')

var admin = require("firebase-admin");

// 1) Create our store
var keysToUID = levelup(leveldown('./keysToUID'))

app.post('/registerUID', function(req, res) {
  if(!req.body.uid || typeof req.body.uid != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  
  if(!req.body.public_key || typeof req.body.public_key != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  keysToUID.put(req.body.public_key, req.body.uid);
  res.status(200).end()
});
 
app.post('/register_username', function(req, res) {
  if(!req.body.username || typeof req.body.username != "string") {
    res.status(400).send("400 Bad Request");
    return
  }

  if(!req.body.public_key || typeof req.body.public_key != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  //TODO: Add username to blockchain

  if(!req.body.uid || typeof req.body.uid != "string") {
   keysToUID.put(req.body.public_key, req.body.uid); 
  }
  res.status(200).end()
});

app.post('/query_access', function(req, res) {
  if(!req.body.entity || typeof req.body.entity != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  if(!req.body.resource || typeof req.body.resource != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  if(!req.body.public_key || typeof req.body.public_key != "string") {
    res.status(400).send("400 Bad Request");
    return
  }

  //TODO: check on blockchain

  

}
app.listen(3000)
