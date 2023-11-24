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

package io.mapsmessaging.security.identity.impl.unix;

import io.mapsmessaging.security.identity.GroupEntry;
import io.mapsmessaging.security.identity.IdentityEntry;
import io.mapsmessaging.security.identity.IdentityLookup;
import io.mapsmessaging.security.identity.NoSuchUserFoundException;
import io.mapsmessaging.security.identity.impl.base.FileBaseIdentities;
import java.io.File;
import java.util.Map;

public class UnixAuth implements IdentityLookup {

  private FileBaseIdentities passwordFileIdentities;
  private GroupFileManager groupFileManager;
  private PasswordFileManager userDetailsManager;

  public UnixAuth(){}

  public UnixAuth(String shadowPath, String passwordPath, String groupPath){
    passwordFileIdentities = new ShadowFileManager(shadowPath);
    if(groupPath != null){
      groupFileManager = new GroupFileManager(groupPath);
    }
    if(passwordPath != null){
      userDetailsManager = new PasswordFileManager(passwordPath);
    }
  }

  @Override
  public String getName() {
    return "unix";
  }

  @Override
  public String getDomain() {
    return getName();
  }

  @Override
  public char[] getPasswordHash(String username) throws NoSuchUserFoundException {
    return passwordFileIdentities.getPasswordHash(username);
  }

  @Override
  public IdentityEntry findEntry(String username) {
    IdentityEntry identityEntry = passwordFileIdentities.findEntry(username);
    if(identityEntry != null && userDetailsManager != null && groupFileManager != null){
      PasswordEntry passwordEntry = userDetailsManager.findUser(username);
      if(passwordEntry != null){
        int groupId = passwordEntry.getGroupId();
        ((ShadowEntry)identityEntry).setPasswordEntry(passwordEntry);
        GroupEntry groupEntry = groupFileManager.findGroup(groupId);
        identityEntry.clearGroups();
        if(groupEntry != null){
          identityEntry.addGroup(groupEntry);
        }
      }
    }
    return identityEntry;
  }

  @Override
  public IdentityLookup create(Map<String, ?> config) {
    if (config.containsKey("passwordFile")) {
      String filePath = (String)config.get("passwordFile");
      String groupFile = (String)config.get("groupFile");
      String passwordFile = (String)config.get("passwd");

      return new UnixAuth(filePath, passwordFile, groupFile);
    }
    if(config.containsKey("configDirectory")){
      String directory = config.get("configDirectory").toString();
      File file = new File(directory);
      if(file.isDirectory()){
        return new UnixAuth(file.getAbsolutePath()+File.separator+"shadow",  file.getAbsolutePath()+File.separator+"passwd",file.getAbsolutePath()+File.separator+"group");
      }
    }
    return null;
  }

}
