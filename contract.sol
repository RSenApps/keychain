pragma solidity ^0.4.16;

/// @title Verification and credentials on the blockchain
contract Keychain {

  struct Users {
    mapping(string => bool) users;
  }

  struct Keys {
    mapping(string => bool) keys;
  }

  struct Resources {
    mapping(string => bool) resources;
  }

  struct Resource {
    string resource;
  }


  /* lists of what currently exist in system */
  Users user_list;
  Keys key_list;
  Resources resource_list;

  mapping(string => string) user_to_key;
  mapping(string => string) key_to_user;
  mapping(string => Resource[]) key_to_resources;
  mapping(string => Keys) resource_to_keys;

  mapping(string => address) resource_to_owner;


  /* Transaction functions */

  function Create_username(string username, string key) public returns(bool) {
      if(user_list.users[username] || key_list.keys[key]) {
        return false;
      }
      user_list.users[username] = true;
      user_to_key[username] = key;
      key_list.keys[key] = true;
      key_to_user[key] = username;
      return true;
  }

  function Create_resource(string resource) public {

    require(!resource_list.resources[resource]);

    //resource_to_owner[resource] = msg.sender;
    resource_list.resources[resource] = true;
    /* assignd to empty list automatically...? */
  }

  function Give_access_to_public_key(string resource, string pub_key) public returns(bool){

    if(resource_list.resources[resource])return false;
    
    key_to_resources[pub_key].push(Resource(resource)); /* will cause duplicates */
    resource_to_keys[resource].keys[pub_key] = true;
    return true;
  }

  function Share_access(string resource, string my_pub_key, string their_pub_key) public returns(bool){
    //require(resource_to_keys[resource].keys[msg.sender]);
    if(resource_list.resources[resource] && key_list.keys[my_pub_key] && key_list.keys[their_pub_key]) {
      if(resource_to_keys[resource].keys[my_pub_key]) {
        resource_to_keys[resource].keys[their_pub_key] = true;
        return true;
      }
    }
    return false;
  }


  /* Read-only functions */

  function Query_access (string resource, string key) public view returns(bool) {
     return resource_to_keys[resource].keys[key];
  }

  function List_access_for_user(string key) public view returns(Resource[]){
    Resource[] storage res = key_to_resources[key];
    return res;
  }

}