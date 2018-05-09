// get the packages we need ========================================
// =================================================================
var express 	= require('express');
var app         = express();
var bodyParser  = require('body-parser');
var morgan      = require('morgan');
//var mongoose    = require('mongoose');
var randomstring = require("randomstring");


const secp256k1 = require('secp256k1');

const { randomBytes } = require('crypto');
var EC = require('elliptic').ec;
var ec = new EC('p256');

var qr = require('qr-image');
var util = require('ethereumjs-util');
var tx = require('ethereumjs-tx');
var lightwallet = require('eth-lightwallet');
var txutils = lightwallet.txutils;
var request = require("request");
var QRCode = require('qrcode')
var Web3 = require('web3');
var url = require('url')
var EventEmitter = require('events').EventEmitter;
var messageBus = new EventEmitter()
messageBus.setMaxListeners(100)

var jwt    = require('jsonwebtoken'); // used to create, sign, and verify tokens
var config = require('./config'); // get our config file
//var User   = require('./app/models/user'); // get our mongoose model

// =================================================================
// configuration ===================================================
// =================================================================
var port = process.env.PORT || 8080; // used to create, sign, and verify tokens
web3 = new Web3(new Web3.providers.HttpProvider('https://ropsten.infura.io/'));
//mongoose.connect(config.database); // connect to database
app.set('secret', config.secret); // secret variable
app.set('bytecode', config.bytecode);
app.set('interface', config.interface);
app.set('contractAddress', config.contractAddress);
app.set('address', config.address);
app.set('key', config.key);

// use body parser so we can get info from POST and/or URL parameters
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

// use morgan to log requests to the console
app.use(morgan('dev'));


// =================================================================
// routes ==========================================================
// =================================================================
//app.get('/setup', function(req, res) {
//
//	// create a sample user
//	var nick = new User({ 
//		name: 'Nick Cerminara', 
//		password: 'password',
//		admin: true 
//	});
//	nick.save(function(err) {
//		if (err) throw err;
//
//		console.log('User saved successfully');
//		res.json({ success: true });
//	});
//});

// basic route (http://localhost:8080)
app.get('/', function(req, res) {
    res.render('index.ejs');
	//res.send('Hello! The API is at http://localhost:' + port + '/api');
});

app.get('/authenticate', function(req, res) {
    var randomNonce = randomBytes(32).toString('hex');
    var token = jwt.sign({nonce: randomNonce}, app.get('secret'), {
                        expiresIn: 86400 // expires in 24 hours
                    });

    //Rendering code will send information to QR code
    //long poll to AWS instance
    res.render('authenticate.ejs', { data:randomNonce, token: token});
})

app.get('/qr/:text', function(req,res){
    var callback_url = "http://ec2-52-91-15-45.compute-1.amazonaws.com:8080/test_auth";
    var input = 'keychain,'+ callback_url + ',' + req.params.text;
    var code = qr.image(input, { type: 'png', ec_level: 'H', size:5, margin: 0});
     res.setHeader('Content-type', 'image/png');
     code.pipe(res);
});


//change to be some callback URL from android!!!!
//app.post('/callback', function(req, res) {
//    // create a sample user
//	var user = new User({ 
//		id: req.body.id, 
//	});
//	user.save(function(err) {
//		if (err) throw err;
//
//		console.log('User saved successfully');
//		res.json({ success: true });
//	});
//});
function sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
}

function sendRaw(rawTx) { //this is a very bad method - not sure if waiting is still needed?
    var privateKey = new Buffer(app.get('key'), 'hex');
    var transaction = new tx(rawTx);
    transaction.sign(privateKey);
    var serializedTx = transaction.serialize().toString('hex');
    web3.eth.sendRawTransaction('0x' + serializedTx, async function(err, result) {
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


//Verify that returned key/id are valid in blockchain 
function verifyUser(id, key, instance) {
    instance.User_for_key.call(id, key, callback=function(err, result) {
        if(err) {
            console.log(err);
            return false;
        } 
	    console.log(result); //CHECK THAT RESULT IS A STRING!!!!!
        if(result == id) return true;
        return false;
    });
}

//Create new user and submit blockchain transaction for new key_id and pubkey
function addUser(id, key, instance) {
    var id; //GENERATE KEYCHAIN ID??

    var user = new User({ 
        id: id, 
    });

    //submit user to the blockchain - keychain_id -> pubkey 
    var txOptions = {
        nonce: web3.toHex(web3.eth.getTransactionCount(app.get('address'), 'pending')),
        gasLimit: web3.toHex(800000),
        gasPrice: web3.toHex(40000000000),
        to: app.get('contractAddress')
    }
    var rawTx = txutils.functionTx(app.get('interface'), 'Create_username', [id, key], txOptions);
    sendRaw(rawTx);

    user.save(function(err) {
        if (err) throw err;
        console.log('User saved successfully');
    });
}

app.post('/test_auth', function(req, res) {
    //var key = ec.keyFromPublic({x: req.body.public_keyx, y: req.body.public_keyy}, 'hex')
    //var isValid = key.verify(req.body.nonce + req.body.keychain_id, {r: req.body.signatureR, s: req.body.signatureS});

    //check blockchain here
    var contract = web3.eth.contract(app.get('interface'));
    var instance = contract.at(app.get('contractAddress'));

    // fail if any parameters are null
    //if(req.body.nonce == null || req.body.keychain_id == null || req.body.public_keyx == null || req.body.public_keyy == null) {
    //    console.log("here");
    //    throw err;
    //    isValid = false;
    //}

    var nonce = String(req.body.nonce);
    var keychain_id = String(req.body.keychain_id)
    var public_key = String(req.body.public_key);
    var address = String(req.body.address);
    var v = parseInt(req.body.v);
    var r = new Buffer(req.body.r, 'hex');
    var s = new Buffer(req.body.s, 'hex');
    var msg = web3.sha3(nonce+keychain_id)
    //var key = ec.keyFromPublic({x: req.body.public_keyx, y: req.body.public_keyy}, 'hex');
    //var isValid = key.verify(req.body.nonce + req.body.keychain_id, {r: req.body.signatureR, s: req.body.signatureS})
    const pubKey = util.ecrecover(util.toBuffer(msg), v, r, s);
    const addrBuf = util.pubToAddress(pubKey);
    const addr = util.bufferToHex(addrBuf);
    //console.log("TEST");
    //console.log(addr);
    //console.log(v);
    //console.log(req.body.r);
    //console.log(req.body.s);
    //console.log(nonce);
    //console.log(address);
    var isValid = (addr == address);


    if(!isValid) {
      res.send(false).end();
      console.log("INVALID SIGNATURE");
      return;
    }
    console.log("Valid signature")
    var encoded_key = key.getPublic().encode('hex');

    instance.Query_user_keys.call(keychain_id, callback=function(err, result) {
        if(err) {
            console.log(err);
        }
        console.log("RESULT");
        console.log(result);
    });
	// find the user
	//User.findOne({
	//	id: keychain_id
	//}, function(err, user) {

	//	if (err) throw err;

	//	if (!user) { //make new user is user not found
    //                 console.log('adding user');
    //                 addUser(keychain_id, encoded_key, instance);
    //                 messageBus.emit(req.body.nonce, req.body.keychain_id)
	//             res.send(true).end();
    //        //res.json({ success: false, message: 'Authentication failed. User not found.' });
	//	} else if (user) {

    //                //query if keychain_id -> pubkey on blockchain
	//                
    //                //var ok = verifyUser(keychain_id, encoded_key, instance);
    //                instance.User_for_key.call(encoded_key, callback=function(err, result) {
    //                    if(err) {
    //                        console.log(err);
    //                    } 
	//		console.log('RESULT FROM QUERY');
    //                    console.log(encoded_key);
    //                    console.log(result); //CHECK THAT RESULT IS A STRING!!!!!
    //                    if(result == keychain_id) {
    //                        messageBus.emit(req.body.nonce, req.body.keychain_id)
	//                    res.send(true).end()
    //                    } else {
    //                        res.send(false).end();
    //                    }
    //                });
	//    }

	//});

})

// ---------------------------------------------------------
// authentication (no middleware necessary since this isnt authenticated)
// ---------------------------------------------------------
// http://localhost:8080/api/authenticate
// Request username, query blockchain, and send/recieve push here
//app.post('/callback/:nonce/:public_keyx/:public_keyy/:signature/:keychain_id', function(req, res) {
//    console.log(req.params);
//    var isValid = secp256k1.verify(new Buffer(req.params.nonce, 'base64'), new Buffer(req.params.signature, 'base64'), new Buffer(req.params.public_key, 'base64'));
//    if(!isValid) {
//        return res.status(403).send({ 
//			success: false, 
//			message: 'Authentication invalid.'
//		});
//    }
//    console.log(req.body.public_key.toString("hex"));
//	
//    var contract = web3.eth.contract(interface);
//    var instance = contract.at(contractAddress);
//	// find the user
//	User.findOne({
//		id: req.body.id
//	}, function(err, user) {
//
//		if (err) throw err;
//
//		if (!user) { //make new user is user not found
//            addUser(req.params.keychain_id, instance);
//            //res.json({ success: false, message: 'Authentication failed. User not found.' });
//		} else if (user) {
//
//            //query if keychain_id -> pubkey on blockchain
//            var ok = verifyUser(req.params.keychain_id, req.params.public_keyx, instance);
//
//			// check if password matches
//			if (!ok) {
//				return res.json({ success: false, message: 'Authentication failed.' });
//			} else {
//                messageBus.emit(req.params.nonce, 'MESSAGE');
//                res.send(isValid).end();
//    		}		
//		}
//	});
//});
//

// ---------------------------------------------------------
// get an instance of the router for api routes
// ---------------------------------------------------------
var apiRoutes = express.Router(); 

// ---------------------------------------------------------
// route middleware to authenticate and check token
// ---------------------------------------------------------
apiRoutes.use(function(req, res, next) {

	// check header or url parameters or post parameters for token
	var token = req.body.token || req.param('token') || req.headers['x-access-token'];
	// decode token
	if (token) {

		// verifies secret and checks exp
		jwt.verify(token, app.get('secret'), function(err, decoded) {			
			if (err) {
				return res.json({ success: false, message: 'Failed to authenticate token.' });		
			} else {
				// if everything is good, save to request for use in other routes
				req.decoded = decoded;	
				console.log(req.decoded)
			 	next();

			}
		});

	} else {

		// if there is no token
		// return an error
		return res.status(403).send({ 
			success: false, 
			message: 'No token provided.'
		});
		
	}
	
});

// ---------------------------------------------------------
// authenticated routes
// ---------------------------------------------------------
apiRoutes.get('/', function(req, res) {
    console.log('test')//res.render('profile.ejs');
});

apiRoutes.get('/users', function(req, res) {
	User.find({}, function(err, users) {
		res.json(users);
	});
});

apiRoutes.get('/check', function(req, res) {
	res.json(req.decoded);
});

app.get('/api', [apiRoutes], function(req, res) {
   res.send('Logged in!').end()
})

app.get('/poll', [apiRoutes], function(req, res) {
 console.log('pollrec')
  var listener = function(res) {
        console.log(req.decoded.nonce)
        messageBus.once(req.decoded.nonce, function(data) {
                    // create a token
            console.log("EVENT CALLED");
                    var token = jwt.sign({keychain_id: req.query.message}, app.get('secret'), {
                        expiresIn: 86400 // expires in 24 hours
                    });

                    res.send('/api?token=' + token).end();
      })
    }
    listener(res);
});


// =================================================================
// start the server ================================================
// =================================================================
app.listen(port);
console.log('Magic happens at http://localhost:' + port);
