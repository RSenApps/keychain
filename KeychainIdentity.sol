pragma solidity ^0.4.23;

contract KeychainIdentity {

  mapping(bytes32 => bool) user_set;
  mapping(bytes32 => uint8) user_to_recovery_threshold;
  mapping(bytes32 => bytes32[]) user_to_recovery_users;
  mapping(bytes32 => address) user_to_key;
  mapping(bytes32 => mapping(bytes32 => address)) recovery_users_approval;
  mapping(bytes32 => mapping(bytes32 => bool)) is_recovery_user;
  mapping(bytes32 => mapping(address => uint24)) user_to_past_keys;
  mapping(bytes32 => uint24) past_keys_version;

  // Setup
  function Create_username(bytes32 username, address key) public returns (bool) {
    require(!user_set[username]);
    user_set[username] = true;
    user_to_key[username] = key;
    user_to_past_keys[username][key] = 0;
    past_keys_version[username] = 0;
    return true;
  }
  
  function Create_web_of_trust(bytes32 username, bytes32[] username_list, uint8 size, uint8 threshold) public returns (bool) {
    require(user_to_key[username] == msg.sender);
    require(user_to_recovery_users[username].length < 1);
    require(size < 10);
    require(threshold <= size);
    require(threshold >= 1);
    for (uint8 i = 0; i < size; i++) {
        user_to_recovery_users[username].push(username_list[i]);
        is_recovery_user[username][username_list[i]] = true;
    }
    user_to_recovery_threshold[username] = threshold;
    return true;
  }

  // Updates
  function Do_change_key(bytes32 username, address new_key) public returns (bool) {
    require(user_to_key[username] == msg.sender);
    user_to_past_keys[username][msg.sender] = past_keys_version[username];
    user_to_past_keys[username][new_key] = past_keys_version[username];
    user_to_key[username] = new_key;
    return true;
  }
  
  function Do_recover_address(bytes32 username, bytes32 recovery_user, address new_address) public returns (bool) {
    require(user_to_key[recovery_user] == msg.sender);
    require(is_recovery_user[username][recovery_user] == true);
    recovery_users_approval[username][recovery_user] = new_address;
    return true;
  }
  
  function Do_recover(bytes32 username, address new_address) public returns (uint8) {
    uint8 recov_ct =  0;
    for (uint8 i = 0; i < user_to_recovery_users[username].length; i++) {
        if (recovery_users_approval[username][user_to_recovery_users[username][i]] == new_address) {
            recov_ct += 1;
        }
    }
    if (recov_ct >= user_to_recovery_threshold[username]) {
        past_keys_version[username] += 1;
        user_to_key[username] = new_address;
        return 0;
    } else {
        return user_to_recovery_threshold[username] - recov_ct;
    }
  }
  
  function Do_revoke(bytes32 username) public returns (bool) {
    require(user_to_past_keys[username][msg.sender] == past_keys_version[username]);
    user_to_key[username] = address(0);
    return true;
  }

  // Read only
  function Query_user_exists(bytes32 username) public view returns (bool) {
    return user_set[username];
  }
  
  function Query_user_key(bytes32 username) public view returns (address) {
    return user_to_key[username];
  }
  
  function Query_recovery_users(bytes32 username) public view returns (bytes32[]) {
    bytes32[] storage res = user_to_recovery_users[username];
    return res;
  }
  
  function Query_recovery_user_approval(bytes32 username, bytes32 recovery_user) public view returns (address) {
    return recovery_users_approval[username][recovery_user];
  }
  
}
