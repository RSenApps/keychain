pragma solidity ^0.4.16;

/// @title Verification and credentials on the blockchain
contract Keychain {

  struct Users {
    mapping(string => bool) users;
  }

  struct Keys {
    mapping(address => bool) keys;
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

  mapping(string => address) user_to_key;
  mapping(address => string) key_to_user;
  mapping(address => Resource[]) key_to_resources;
  mapping(string => Keys) resource_to_keys;

  mapping(string => address) resource_to_owner;


  /* Transaction functions */

  function Create_username(string username, address key) public returns(bool) {
      if(user_list.users[username] || key_list.keys[key]) {
        return false;
      }
      user_list.users[username] = true;
      user_to_key[username] = key;
      key_list.keys[key] = true;
      key_to_user[key] = username;
      return true;
  }

  function Create_resource(string resource) public returns(bool) {

    if(resource_list.resources[resource]) {
      return false;
    }
    resource_to_owner[resource] = msg.sender;
    resource_list.resources[resource] = true;
    /* assignd to empty list automatically...? */
    return true;
  }

  function Give_access_to_public_key(string resource, address pub_key) public returns(bool){

    require(msg.sender == resource_to_owner[resource]);
    if(resource_list.resources[resource]) {
      key_to_resources[pub_key].push(Resource(resource)); /* will cause duplicates */
      resource_to_keys[resource].keys[pub_key] = true;
      return true;
    }
    return false;
  }

  function Share_access(string resource, address my_pub_key, address their_pub_key) public returns(bool){
    require(resource_to_keys[resource].keys[msg.sender]);
    if(resource_list.resources[resource] && key_list.keys[my_pub_key] && key_list.keys[their_pub_key]) {
      if(resource_to_keys[resource].keys[my_pub_key]) {
        resource_to_keys[resource].keys[their_pub_key] = true;
        return true;
      }
    }
    return false;
  }


  /* Read-only functions */

  function Query_access (string resource, address key) external view returns(bool) {
     return resource_to_keys[resource].keys[key];
  }

  function List_access_for_user(address key) external view returns(Resource[]){
    Resource[] storage res = key_to_resources[key];
    return res;
  }

}