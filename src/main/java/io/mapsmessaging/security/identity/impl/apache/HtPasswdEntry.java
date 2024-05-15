/*
 * Copyright [ 2020 - 2024 ] [Matthew Buckton]
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

package io.mapsmessaging.security.identity.impl.apache;

import io.mapsmessaging.security.identity.IdentityEntry;
import io.mapsmessaging.security.passwords.PasswordBuffer;
import io.mapsmessaging.security.passwords.PasswordHandlerFactory;

public class HtPasswdEntry extends IdentityEntry {

  public HtPasswdEntry(String username, char[] password) {
    this.username = username;
    this.password = new PasswordBuffer(password);
    passwordHasher = PasswordHandlerFactory.getInstance().parse(password);
  }

  public HtPasswdEntry(String line) {
    int usernamePos = line.indexOf(":");
    username = line.substring(0, usernamePos);
    line = line.substring(usernamePos + 1);
    password = new PasswordBuffer(line.toCharArray());
    passwordHasher = PasswordHandlerFactory.getInstance().parse(password.getHash());
  }

}
