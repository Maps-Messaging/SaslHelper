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

package io.mapsmessaging.security.sasl.provider.debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import javax.security.sasl.SaslException;

public class DebugSasl {

  private static final int RANDOM_SIZE = 128;

  private final SecureRandom secureRandom;
  protected int loopCount;
  protected int state;

  protected DebugSasl() {
    secureRandom = new SecureRandom();
  }

  protected byte[] handleClientChallenge(byte[] challenge) throws SaslException {
    try {
      Properties properties = new Properties();
      if (state == 0) {
        byte[] initialRandom = new byte[RANDOM_SIZE];
        secureRandom.nextBytes(initialRandom);
        properties.put("Client", Base64.getEncoder().encodeToString(initialRandom));
      } else {
        Properties challengeProp = new Properties();
        challengeProp.load(new ByteArrayInputStream(challenge));
        byte[] server = Base64.getDecoder().decode(challengeProp.getProperty("Server"));
        byte[] initialRandom = new byte[RANDOM_SIZE];
        secureRandom.nextBytes(initialRandom);
        properties.put("Client", Base64.getEncoder().encodeToString(initialRandom));
        properties.put("Server", Base64.getEncoder().encodeToString(server));
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      properties.store(baos, "SASL Debug::" + state);
      loopCount--;
      state++;
      return baos.toByteArray();
    } catch (IOException e) {
      throw new SaslException(e.getMessage());
    }
  }

  protected byte[] handleServerChallenge(byte[] challenge) throws SaslException {
    try {
      if (challenge != null && challenge.length > 0) {
        Properties properties = new Properties();
        Properties challengeProp = new Properties();
        challengeProp.load(new ByteArrayInputStream(challenge));
        byte[] client = Base64.getDecoder().decode(challengeProp.getProperty("Client"));
        byte[] initialRandom = new byte[RANDOM_SIZE];
        secureRandom.nextBytes(initialRandom);
        properties.put("Client", Base64.getEncoder().encodeToString(client));
        properties.put("Server", Base64.getEncoder().encodeToString(initialRandom));
        loopCount--;
        state++;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        properties.store(baos, "SASL Debug::" + state);
        return baos.toByteArray();
      }
      return null;
    } catch (IOException e) {
      throw new SaslException(e.getMessage());
    }
  }
}
