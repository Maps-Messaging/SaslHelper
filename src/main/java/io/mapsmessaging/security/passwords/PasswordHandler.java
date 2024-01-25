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

package io.mapsmessaging.security.passwords;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface PasswordHandler {

  byte[] transformPassword(byte[] password, byte[] salt, int cost)
      throws GeneralSecurityException, IOException;

  PasswordHandler create(String password);

  String getKey();

  boolean hasSalt();

  byte[] getSalt();

  byte[] getPassword() throws GeneralSecurityException, IOException;

  char[] getFullPasswordHash();

  String getName();

  default int getCost() {
    return 0;
  }

}
