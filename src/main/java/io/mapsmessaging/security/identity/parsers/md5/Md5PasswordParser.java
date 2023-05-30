/*
 * Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mapsmessaging.security.identity.parsers.md5;

import io.mapsmessaging.security.identity.parsers.PasswordParser;
import org.apache.commons.codec.digest.Md5Crypt;

public class Md5PasswordParser implements PasswordParser {

  protected final byte[] password;
  protected final byte[] salt;

  public Md5PasswordParser() {
    password = new byte[0];
    salt = new byte[0];
  }

  protected Md5PasswordParser(String password) {
    String sub = password.substring(getKey().length());
    int split = sub.indexOf("$");
    String pw = "";
    String sl = "";
    if (split != -1) {
      sl = sub.substring(0, split);
      pw = sub.substring(split + 1);
    }
    this.password = pw.getBytes();
    this.salt = sl.getBytes();
  }

  public PasswordParser create(String password) {
    return new Md5PasswordParser(password);
  }

  @Override
  public String getKey() {
    return "$apr1$";
  }

  @Override
  public boolean hasSalt() {
    return salt.length > 0;
  }

  @Override
  public byte[] computeHash(byte[] password, byte[] salt, int cost) {
    return Md5Crypt.apr1Crypt(password, new String(salt)).getBytes();
  }

  @Override
  public byte[] getSalt() {
    return salt;
  }

  @Override
  public byte[] getPassword() {
    return password;
  }

  @Override
  public char[] getFullPasswordHash() {
    return (getKey() + new String(salt) + "$" + new String(password)).toCharArray();
  }

  @Override
  public String getName() {
    return "MD5";
  }
}
