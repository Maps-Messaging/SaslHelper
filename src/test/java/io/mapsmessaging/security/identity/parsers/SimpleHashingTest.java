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

package io.mapsmessaging.security.identity.parsers;

import io.mapsmessaging.security.identity.PasswordGenerator;
import io.mapsmessaging.security.identity.parsers.multi.MultiPasswordParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleHashingTest {

  private static final char[] PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+=-\\|][{};:\"'/?.>,<`~".toCharArray();

  private static Stream<PasswordParser> knownParsers() {
    return PasswordParserFactory.getInstance().getPasswordParsers().stream().filter(passwordParser -> !(passwordParser instanceof MultiPasswordParser));
  }

  @Test
  void testMultiParser() {
    String password = generatePassword(16);
    String salt = PasswordGenerator.generateSalt(16);

    List<PasswordParser> parsers = knownParsers().collect(Collectors.toList());
    MultiPasswordParser parser = new MultiPasswordParser(parsers);
    byte[] hash = parser.computeHash(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8), 0);
    String storeHash = new String(hash);
    System.err.println(storeHash);
    PasswordParser lookup = PasswordParserFactory.getInstance().parse(new String(hash));
    Assertions.assertEquals(lookup.getClass().toString(), parser.getClass().toString());
    byte[] computed = lookup.computeHash(generatePassword(16).getBytes(StandardCharsets.UTF_8), lookup.getSalt(), lookup.getCost());
    String computedString = new String(computed);
    Assertions.assertNotEquals(storeHash, computedString);
  }

  @ParameterizedTest
  @MethodSource("knownParsers")
  void testHashAndValidateBadPassword(PasswordParser base) {
    String password = generatePassword(16);
    String salt = PasswordGenerator.generateSalt(16);
    PasswordParser parser = base.create("");
    byte[] hash = parser.computeHash(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8), parser.getCost());
    String storeHash = new String(hash);
    PasswordParser lookup = PasswordParserFactory.getInstance().parse(storeHash);
    Assertions.assertEquals(lookup.getClass().toString(), parser.getClass().toString());
    byte[] computed = lookup.computeHash(generatePassword(16).getBytes(StandardCharsets.UTF_8), lookup.getSalt(), lookup.getCost());
    String computedString = new String(computed);
    Assertions.assertNotEquals(storeHash, computedString);
  }

  @ParameterizedTest
  @MethodSource("knownParsers")
  void testHashAndValidate(PasswordParser base) {
    String password = generatePassword(16);
    String salt = PasswordGenerator.generateSalt(16);
    PasswordParser parser = base.create("");
    byte[] hash = parser.computeHash(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8), parser.getCost());
    String storeHash = new String(hash);
    PasswordParser lookup = PasswordParserFactory.getInstance().parse(storeHash);
    Assertions.assertEquals(lookup.getClass().toString(), parser.getClass().toString());
    byte[] computed = lookup.computeHash(password.getBytes(StandardCharsets.UTF_8), lookup.getSalt(), lookup.getCost());
    String computedString = new String(computed);
    Assertions.assertEquals(storeHash, computedString);
  }

  @ParameterizedTest
  @MethodSource("knownParsers")
  void testFileLoadAndParse(PasswordParser base) throws IOException {
    FileOutputStream fileOutputStream = new FileOutputStream("hash.txt", false);
    String password = generatePassword(16);
    String salt = PasswordGenerator.generateSalt(16);
    PasswordParser parser = base.create("");
    byte[] hash = parser.computeHash(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8), parser.getCost());
    String storeHash = new String(hash);
    fileOutputStream.write(hash);
    fileOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));
    fileOutputStream.close();


    List<String> hashes = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader("hash.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        hashes.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (String received : hashes) {
      PasswordParser lookup = PasswordParserFactory.getInstance().parse(received);
      byte[] computed = lookup.computeHash(password.getBytes(StandardCharsets.UTF_8), lookup.getSalt(), lookup.getCost());
      Assertions.assertArrayEquals(received.getBytes(StandardCharsets.UTF_8), computed);
    }
  }

  private String generatePassword(int len){
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    int x=0;
    while(x<len){
      int y = Math.abs(random.nextInt(PASSWORD_CHARS.length));
      sb.append(PASSWORD_CHARS[y]);
      x++;
    }
    return sb.toString();
  }

}
