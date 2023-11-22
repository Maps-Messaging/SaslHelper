/*
 * Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mapsmessaging.security.identity;

import com.sun.security.auth.UserPrincipal;
import io.mapsmessaging.security.identity.parsers.PasswordParser;
import io.mapsmessaging.security.identity.principals.GroupPrincipal;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;
import lombok.Getter;

/**
 * Represents an identity entry, which can be a user or a machine-to-machine username.
 *
 * <p>The {@code IdentityEntry} class encapsulates the properties and behavior of an identity entry, including the username,
 * password parser, password, and the associated groups.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 *     // Create a new identity entry
 *     IdentityEntry identity = new IdentityEntry();
 *
 *     // Set the username
 *     identity.setUsername("user1");
 *
 *     // Set the password parser
 *     identity.setPasswordParser(passwordParser);
 *
 *     // Set the password
 *     identity.setPassword("password123");
 *
 *     // Add a group to the identity
 *     identity.addGroup(groupEntry);
 *
 *     // Check if the identity is in a specific group
 *     boolean isInGroup = identity.isInGroup("group1");
 *
 *     // Get the list of groups associated with the identity
 *     List<GroupEntry> groups = identity.getGroups();
 *
 *     // Get the subject representing the identity
 *     Subject subject = identity.getSubject();
 * }</pre>
 *
 * @see GroupEntry
 * @see Subject
 */
public class IdentityEntry {

  @Getter
  protected String username;

  @Getter
  protected PasswordParser passwordParser;

  @Getter
  protected String password;

  protected final Map<String, GroupEntry> groupList = new LinkedHashMap<>();


  public boolean isInGroup(String group){
    return groupList.containsKey(group);
  }

  public void addGroup(GroupEntry group){
    groupList.put(group.name, group);
  }

  public void clearGroups(){
    groupList.clear();
  }

  public List<GroupEntry> getGroups(){
    return new ArrayList<>(groupList.values());
  }

  public Subject getSubject(){
    return new Subject(true, getPrincipals(), new TreeSet<>(), new TreeSet<>());
  }

  protected Set<Principal> getPrincipals(){
    Set<Principal> principals = new HashSet<>();
    principals.add(new UserPrincipal(username));
    for(GroupEntry group:groupList.values()){
      principals.add(new GroupPrincipal(group.getName(), group.getUuid()));
    }
    principals.add(
        new GroupPrincipal("everyone", UUID.fromString("00000000-0000-0000-0000-000000000000")));
    return principals;
  }

  @Override
  public String toString() {
    return username + ":" + password;
  }

}
