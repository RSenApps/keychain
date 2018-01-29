var express = require('express')
var bodyParser = require('body-parser')
 
var app = express()
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({extended: true}))

var levelup = require('levelup')
var leveldown = require('leveldown')

var admin = require("firebase-admin");
var crypto = require("crypto");
var NodeRSA = require('node-rsa');

// 1) Create our store
var keysToUID = levelup(leveldown('./keysToUID'))

var firebaseAccount = {
  "type": "service_account",
  "project_id": "keychain-ce6fd",
  "private_key_id": "99b8e3204beb81af075a8f5833a7d152d6a5de99",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCszsfIjW+5DY6b\nnjhgAjP2Gc4UBUhBo3yqhBE3I7Vzj16Y5iHfgStPXTLcblBy9x4USAted7kFenoU\nYC54MjSWVwAeBAdzRw30bmVKEF+lz6y3/Df8Y1flVkmp+vvArI9/1NNEsec+HEC7\niIQsDQGLJkf/sbb7UkZKPDsXIlH2I1jXgiDjQlwAejG9MVqh6xNK4tqfzqbzZrIu\nNr6YF10Up66DCtii/F6n4M0JNCPwEOoJfENIundxNUSiIy44NYcLIGwPd1YJbEpz\n/4BIBj+DSjwCJ4qpPHDng+Q4jg/qhjI2c+qjGA5EqFBMeseeWBPM2xVBOlpUsKnF\naGJxHQELAgMBAAECggEAShtvT9FfzSNEOfZTRNENCYgteSZ+wy9iQhna7COTKBie\nv8FJW3kgYqucKS3hsDvLmXT/8yYyoWfRvLU2mHMnXCW5NiYSL9yT3nQNWgLFke0K\nq+gs9j6ALiif9JZeqlUWQQK7C2Wjvl4NYJ7914pIVC05iSzkRMkWvbUwvg4Iyh3P\nxBV9c/IWMe/FpQw38QCRVvEUCS6B2cxYhcCfTKCOh+5KWQnLntfjkSLVFmuPvktP\nLHzCnXXGcYpB8eJfNgagYdgwKsnEJINlIjZucqtuazz1GYQc7O/N6Q6tVNkB5Gz/\ngTBPYhARijeFvmASQeznNMV19gW+tVZx+3NqRrByaQKBgQDexyP2uACj+Hs6jSH8\nmaBurwfdG7n2f0uclqclT/stohO72/ddVA3Mo1ZcpvYWxzukYM5/YtTRNivAMblk\nEVzjs7a1u+hPJYDuiynrWFwybBizNNipdjc2FpN8zaJsCP5dJkDN64uwf6N0v4LO\nEx+HYgMfzBBUTbNdQnE5sQxd3QKBgQDGk/V5On/wafOCub1AoviLDeR9qfMo7CNx\ndiPJp9yv6Yexsgz/19u9LK3eq2pvxYeSFS6krjZHyetA+pI0PwpZgIY/6dypNsIS\nj4ux8kw7ELIQbRv1QZZzfePERFeP+Xp2GneDZ7KEY0zfEUrbsdTJ8MNB1r09pO7S\nfg1gtpEwBwKBgQCqGJTeEPn0OIomV5Imo0n6mMvBSCVCD0m1ItY2SA45dnHo0vfZ\nG495uxD6p0RoefCQ/pVMcDKcFudq1Mx/mj45YYNU2Udz/ueluz3jgtDKcvyc//GZ\n2jgnpS8xcHTeVjOY/dcnIvzCY4JzZrJCFBnTrcNC6PVi2PzlbRC0gvk1RQKBgHkt\n51mgSlyyu0gmAoiEKznoM2xAHsP1PjfKCVLQ3gp3bzgP2ID0AAM0VbIRVvV4TB/o\nTzttmHA8lxRjFH9PjQhLNyGfm+mjIdFjybLAkqZkrNT7UFldOeumgizOK8UPUBzs\nT8xfpn/FfS6PBeiFX74P74eo/Hi2woqvI4jGQFlXAoGAFQLwcntUuRWko3Sv4C+Z\nIC3NgVMqFKZMw0UGqasorfbiQaviMDkiinGNVk8kY1TVW37jxOsxN+oseyEkzsJO\n/29lHsOFNHSCGcq1Po7AH80kIUwDvVVAw2fKJaEqvtGLLCrGLXAmJGHKYfdccrZP\n7I+sJVhFABfzczAYMXjc8CQ=\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-ut93n@keychain-ce6fd.iam.gserviceaccount.com",
  "client_id": "110748611293395310454",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://accounts.google.com/o/oauth2/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-ut93n%40keychain-ce6fd.iam.gserviceaccount.com"
}

admin.initializeApp({
        credential: admin.credential.cert(firebaseAccount)
});
    


app.post('/registerUID', function(req, res) {
  if(!req.body.uid || typeof req.body.uid != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  
  if(!req.body.public_key || typeof req.body.public_key != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  keysToUID.put(req.body.public_key.trim(), req.body.uid);
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
  if(req.body.uid && typeof req.body.uid == "string") {
   keysToUID.put(req.body.public_key.trim(), req.body.uid);
   console.log(req.body.public_key)
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
 
  if(!req.body.challenge || typeof req.body.challenge != "string") {
    res.status(400).send("400 Bad Request");
    return
  }

  if(!req.body.callback_url || typeof req.body.callback_url != "string") {
    res.status(400).send("400 Bad Request");
    return
  }

  keysToUID.get(req.body.public_key.trim(), { asBuffer: false }, function (err, value) {
    if(err) {
      res.status(400).send("Device Not Registered"+err);
      return;
    }

    //TODO: check on blockchain

    var payload = {
      data: {
        challenge: req.body.challenge,
        callback_url: req.body.callback_url,
        resource: req.body.resource
      }
    };
    admin.messaging().sendToDevice(value, payload)
    .then(function(response) {
      res.status(200).end();
    })
    .catch(function(error) {
      res.status(400).send("Error in push notification" + error);
    });
 });   
   
})

var encryptStringWithRsaPublicKey = function(toEncrypt,publicKey) {
    var buffer = new Buffer(toEncrypt);
    var key = new NodeRSA('-----BEGIN RSA PUBLIC KEY-----\n'+ publicKey.trim() + '\n' +
                      '-----END RSA PUBLIC KEY-----');
    return key.encrypt(buffer, 'string', 'base64');
};

var uidToReturn = "";

app.post('/query_access_and_return_result', function(req, res) {
  //TODO: check on blockchain
  
   var payload = {
      data: {
        challenge: 'KeyChain',// encryptStringWithRsaPublicKey('KeyChain', req.body.public_key),
        callback_url: 'http://ec2-54-224-142-62.compute-1.amazonaws.com:3000/challenge_response',
        resource: req.body.resource
      }
    };
keysToUID.get(req.body.return_key.trim(), { asBuffer: false }, function (err, value) {
    if(err) {
      res.status(400).send("Sender Device Not Registered"+err);
      return;
    }

   uidToReturn = value;

});

keysToUID.get(req.body.public_key.trim(), { asBuffer: false }, function (err, value) {
    if(err) {
      res.status(400).send("Device Not Registered"+err);
      return;
    }

    //TODO: check on blockchain

    admin.messaging().sendToDevice(value, payload)
    .then(function(response) {
      res.status(200).end();
    })
    .catch(function(error) {
      res.status(400).send("Error in push notification" + error);
    });
 });
});

app.post('/challenge_response', function(req, res) {
  if (req.body.result.indexOf('KeyChain') > -1) {
    var payload = {
  notification: {
    title: "Requested user is authenticated",
    body: "Authenticated"
  }
};
   admin.messaging().sendToDevice(uidToReturn, payload)
    .then(function(response) {
      res.status(200).end();
    })
    .catch(function(error) {
      res.status(400).send("Error in push notification" + error);
    });

  }
})

app.get('/list_access_for_key', function(req, res) {
  if(!req.body.public_key || typeof req.body.public_key != "string") {
    res.status(400).send("400 Bad Request");
    return
  }
  
  //TODO: check on blockchain
})


const { randomBytes } = require('crypto')
var EC = require('elliptic').ec;
var ec = new EC('p256');
var EventEmitter = require('events').EventEmitter  
var messageBus = new EventEmitter()  
messageBus.setMaxListeners(100)  

app.get('/test_poll', function(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
  //generate qr code and display with nonce
  var randomNonce = randomBytes(32).toString('hex')
  var listener = function(res) {
    messageBus.once('2e41dfc4ac1e44f1a4b5931de7da8572cabca665a58787c869de66226ef6a77f', function(data) {
      res.send("Keychain id: " + data + " is logged in")
    })
  }
  listener(res)
})


app.post('/test_auth', function(req, res) {
  var key = ec.keyFromPublic({x: req.body.public_keyx, y: req.body.public_keyy}, 'hex')
  var isValid = key.verify(req.body.nonce + req.body.keychain_id, {r: req.body.signatureR, s: req.body.signatureS})
  //TODO check blockchain here
  if (isValid) {
  	messageBus.emit(req.body.nonce, req.body.keychain_id)
  }
  res.send(isValid).end()
})

/*const { randomBytes } = require('crypto')
// or require('secp256k1/elliptic')
//   if you want to use pure js implementation in node

// generate message to sign
const msg = randomBytes(32)

// generate privKey
let privKey
do {
  privKey = randomBytes(32)
} while (!secp256k1.privateKeyVerify(privKey))

// get the public key in a compressed format
const pubKey = secp256k1.publicKeyCreate(privKey)

// sign the message
const sigObj = secp256k1.sign(msg, privKey)

//console.log(msg.toString('base64'))
//console.log(pubKey.toString('base64'))
//console.log(sigObj['signature'].toString('base64'))
//console.log(secp256k1.verify(msg, sigObj.signature, pubKey))

var key = ec.genKeyPair();
// Sign the message's hash (input must be an array, or a hex-string)
//var msgHash = [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ];
var signature = key.sign(msg);

// Export DER encoded signature in Array
var derSign = signature.toDER();
console.log(msg.toString('hex'))
console.log(key.getPublic().encode('hex'))
  console.log(key.getPublic().getX().toString('hex'))
  console.log(key.getPublic().getY().toString('hex'))
var key2 = ec.keyFromPublic(key.getPublic().encode('hex'), 'hex')
console.log(Buffer.from(derSign).toString('hex'))
// Verify signature
console.log(key2.verify(msg, Buffer.from(derSign).toString('hex')));
*/
app.listen(3000)
