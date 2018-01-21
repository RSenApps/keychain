
var Web3 = require('web3');
var util = require('ethereumjs-util');
var tx = require('ethereumjs-tx');
var lightwallet = require('eth-lightwallet');
var txutils = lightwallet.txutils;

var web3 = new Web3(
            new Web3.providers.HttpProvider('https://ropsten.infura.io/')
        );


var address = '0xD1E90a9E4Cd458CfFe191FCa01fF641832a9C0dB';
var key = 'd82530369ecc87b2d9adc9821a4d7246f2bb3cf5c5b55cb3d2224f0138235599';

function sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
}


var contractAddress = "0x891736198645551A7B1C4632d7F431F47DE01e65";

var bytecode = '6060604052341561000f57600080fd5b6114328061001e6000396000f3006060604052600436106100985763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166310e5c4d9811461009d57806340692995146100db5780636f97fbd11461016e578063970a9080146101bf5780639f7c01221461025c578063afd032ed14610331578063b7986b841461034f578063bb77ee041461036d578063f6e3d59514610400575b600080fd5b34156100a857600080fd5b6100c76024600480358281019290820135918135918201910135610434565b604051901515815260200160405180910390f35b34156100e657600080fd5b6100c760046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284375094965061048d95505050505050565b341561017957600080fd5b6100c760046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965061075e95505050505050565b34156101ca57600080fd5b6101e5602460048035828101929101359060ff9035166108f6565b60405160208082528190810183818151815260200191508051906020019080838360005b83811015610221578082015183820152602001610209565b50505050905090810190601f16801561024e5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561026757600080fd5b6100c760046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496506109fd95505050505050565b341561033c57600080fd5b6101e56004803560248101910135610e72565b341561035a57600080fd5b6101e56004803560248101910135610f40565b341561037857600080fd5b6100c760046024813581810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f016020809104026020016040519081016040528181529291906020840183838082843750949650610fd695505050505050565b341561040b57600080fd5b61041e6004803560248101910135611326565b60405160ff909116815260200160405180910390f35b600060078585604051808383808284378201915050925050509081526020016040519081900390208383604051808383808284378201915050925050509081526020016040519081900390205460ff1695945050505050565b600080836040518082805190602001908083835b602083106104c05780518252601f1990920191602091820191016104a1565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff168061056957506001826040518082805190602001908083835b602083106105305780518252601f199092019160209182019101610511565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff165b1561057657506000610758565b60016000846040518082805190602001908083835b602083106105aa5780518252601f19909201916020918201910161058b565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805460ff1916911515919091179055816003846040518082805190602001908083835b602083106106205780518252601f199092019160209182019101610601565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020908051610664929160200190611359565b50600180836040518082805190602001908083835b602083106106985780518252601f199092019160209182019101610679565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805460ff1916911515919091179055826004836040518082805190602001908083835b6020831061070e5780518252601f1990920191602091820191016106ef565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020908051610752929160200190611359565b50600190505b92915050565b60006002826040518082805190602001908083835b602083106107925780518252601f199092019160209182019101610773565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff16156107d7575060006108f1565b336008836040518082805190602001908083835b6020831061080a5780518252601f1990920191602091820191016107eb565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805473ffffffffffffffffffffffffffffffffffffffff191673ffffffffffffffffffffffffffffffffffffffff9290921691909117905560016002836040518082805190602001908083835b602083106108aa5780518252601f19909201916020918201910161088b565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805460ff19169115159190911790555060015b919050565b6108fe6113d7565b60018260ff1610156109205760206040519081016040526000815290506109f6565b600584846040518083838082843782019150509250505090815260200160405190819003902060ff6000198401166064811061095857fe5b018054600260001961010060018416150201909116046020601f820181900481020160405190810160405280929190818152602001828054600181600116156101000203166002900480156109ee5780601f106109c3576101008083540402835291602001916109ee565b820191906000526020600020905b8154815290600101906020018083116109d157829003601f168201915b505050505090505b9392505050565b60006002846040518082805190602001908083835b60208310610a315780518252601f199092019160209182019101610a12565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff168015610adb57506001836040518082805190602001908083835b60208310610aa25780518252601f199092019160209182019101610a83565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff165b8015610b4d57506001826040518082805190602001908083835b60208310610b145780518252601f199092019160209182019101610af5565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff165b15610e68576007846040518082805190602001908083835b60208310610b845780518252601f199092019160209182019101610b65565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020836040518082805190602001908083835b60208310610be85780518252601f199092019160209182019101610bc9565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff1615610e685760016007856040518082805190602001908083835b60208310610c595780518252601f199092019160209182019101610c3a565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020836040518082805190602001908083835b60208310610cbd5780518252601f199092019160209182019101610c9e565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805460ff19169115159190911790553073ffffffffffffffffffffffffffffffffffffffff1663bb77ee0485846000604051602001526040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808060200180602001838103835285818151815260200191508051906020019080838360005b83811015610d93578082015183820152602001610d7b565b50505050905090810190601f168015610dc05780820380516001836020036101000a031916815260200191505b50838103825284818151815260200191508051906020019080838360005b83811015610df6578082015183820152602001610dde565b50505050905090810190601f168015610e235780820380516001836020036101000a031916815260200191505b50945050505050602060405180830381600087803b1515610e4357600080fd5b6102c65a03f11515610e5457600080fd5b5050506040518051905050600190506109f6565b5060009392505050565b610e7a6113d7565b600383836040518083838082843782019150509250505090815260200160405180910390208054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610f335780601f10610f0857610100808354040283529160200191610f33565b820191906000526020600020905b815481529060010190602001808311610f1657829003601f168201915b5050505050905092915050565b610f486113d7565b600483836040518083838082843782019150509250505090815260200160405180910390208054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610f335780601f10610f0857610100808354040283529160200191610f33565b6000806002846040518082805190602001908083835b6020831061100b5780518252601f199092019160209182019101610fec565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff161561131a576006836040518082805190602001908083835b6020831061107a5780518252601f19909201916020918201910161105b565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff169050602060405190810160405280858152506005846040518082805190602001908083835b602083106110f65780518252601f1990920191602091820191016110d7565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405190819003902060ff83166064811061113857fe5b0181518190805161114d929160200190611359565b509050506006836040518082805190602001908083835b602083106111835780518252601f199092019160209182019101611164565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205460ff166001016006846040518082805190602001908083835b602083106111f05780518252601f1990920191602091820191016111d1565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805460ff191660ff9290921691909117905560016007856040518082805190602001908083835b6020831061126a5780518252601f19909201916020918201910161124b565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020846040518082805190602001908083835b602083106112ce5780518252601f1990920191602091820191016112af565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020805460ff19169115159190911790556001915061131f565b600091505b5092915050565b600060068383604051808383808284378201915050925050509081526020016040519081900390205460ff169392505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061139a57805160ff19168380011785556113c7565b828001600101855582156113c7579182015b828111156113c75782518255916020019190600101906113ac565b506113d39291506113e9565b5090565b60206040519081016040526000815290565b61140391905b808211156113d357600081556001016113ef565b905600a165627a7a72305820f479e977ed7763d14c09fe6c6409c57ff07b442d5927e69f52e0e716a91349a30029';

var interface = 
[{"constant":true,"inputs":[{"name":"resource","type":"string"},{"name":"key","type":"string"}],"name":"Query_access","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"username","type":"string"},{"name":"key","type":"string"}],"name":"Create_username","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"name":"resource","type":"string"}],"name":"Create_resource","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"name":"key","type":"string"},{"name":"index","type":"uint8"}],"name":"Get_access_for_index","outputs":[{"name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"resource","type":"string"},{"name":"my_pub_key","type":"string"},{"name":"their_pub_key","type":"string"}],"name":"Share_access","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"name":"user","type":"string"}],"name":"Key_for_user","outputs":[{"name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"name":"key","type":"string"}],"name":"User_for_key","outputs":[{"name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"resource","type":"string"},{"name":"pub_key","type":"string"}],"name":"Give_access_to_public_key","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"name":"key","type":"string"}],"name":"Num_access_for_user","outputs":[{"name":"","type":"uint8"}],"payable":false,"stateMutability":"view","type":"function"}]


function query_data() {
    var contract = web3.eth.contract(interface);
    var instance = contract.at(contractAddress);
    instance.Query_access.call("facebook", "keychain", callback=function(err, result) {
        if(err) {
            console.log(err);
        } else {
            console.log('Query result:');
            console.log(result);
        }
    });
    instance.Num_access_for_user.call("keychain", callback=function(err, size) {
        if(err) {
            console.log(err);
        } else {
            console.log('Read size:');
            console.log(size);
            for(i = 0; i < size; i ++) {
                instance.Get_access_for_index("keychain", i, callback=function(err, res) {
                    if(err) {
                        console.log(err);
                    } else {
                        console.log("permission for ");
                        console.log(res);
                    }
                });
            }
        }
    });
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
            while(x == null) {
                x = web3.eth.getTransactionReceipt(result);
                await sleep(10000);
            }
            console.log(x)
            query_data();
        }
    });
}


var txOptions = {
    nonce: web3.toHex(web3.eth.getTransactionCount(address)),
    gasLimit: web3.toHex(800000),
    gasPrice: web3.toHex(40000000000),
    to: contractAddress
}
var rawTx = txutils.functionTx(interface, 'Create_username', ["bob", "keychain"], txOptions);
sendRaw(rawTx);
var rawTx1 = txutils.functionTx(interface, 'Create_resource', ["facebook"], txOptions);
sendRaw(rawTx1);
var rawTx2 = txutils.functionTx(interface, 'Give_access_to_public_key', ["facebook", "keychain"], txOptions);
sendRaw(rawTx2);


