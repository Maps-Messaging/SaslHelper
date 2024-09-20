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

package io.mapsmessaging.security.certificates.jvm;

import io.mapsmessaging.configuration.ConfigurationProperties;
import io.mapsmessaging.security.certificates.CertificateManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import lombok.Getter;

public class JvmKeyStoreManager implements CertificateManager {

  protected static final String KEYSTORE_PASSWORD = "passphrase";

  @Getter
  private final KeyStore keyStore;

  public JvmKeyStoreManager() {
    keyStore = null;
  }

  public boolean isValid(ConfigurationProperties config) {
    return config.containsKey(KEYSTORE_PASSWORD);
  }

  protected JvmKeyStoreManager(ConfigurationProperties config) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
    String t = config.getProperty(KEYSTORE_PASSWORD);
    if (t == null) {
      t = "";
    }
    String type = KeyStore.getDefaultType();
    keyStore = KeyStore.getInstance(type);

    // Get path to the JVM's cacerts file
    String javaHome = System.getProperty("java.home");
    String cacertsPath = javaHome + "/lib/security/cacerts";

    try (FileInputStream fis = new FileInputStream(cacertsPath)) {
      keyStore.load(fis, t.toCharArray());
    }
  }

  public CertificateManager create(ConfigurationProperties config) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
    return new JvmKeyStoreManager(config);
  }

  public List<String> getAliases() throws KeyStoreException {
    List<String> aliasList = new ArrayList<String>();
    Enumeration<String> aliases = keyStore.aliases();
    while (aliases.hasMoreElements()) {
      aliasList.add(aliases.nextElement());
    }
    return aliasList;
  }

  @Override
  public Certificate getCertificate(String alias) throws CertificateException {
    try {
      if (keyStore.containsAlias(alias)) {
        return keyStore.getCertificate(alias);
      }
    } catch (KeyStoreException e) {
      throw new CertificateException("Error retrieving certificate", e);
    }
    throw new CertificateException("Alias does not exist");
  }

  @Override
  public void addCertificate(String alias, Certificate certificate) throws CertificateException {
    throw new CertificateException("Unable to add certificates to JVM Key Store");
  }

  @Override
  public void deleteCertificate(String alias) throws CertificateException {
    throw new CertificateException("Unable to delete certificates to JVM Key Store");
  }

  @Override
  public PrivateKey getKey(String alias, char[] keyPassword) throws CertificateException {
    try {
      Key key = keyStore.getKey(alias, keyPassword);
      if (key instanceof PrivateKey) {
        return (PrivateKey) key;
      } else {
        throw new KeyStoreException("No private key found for alias: " + alias);
      }
    } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
      throw new CertificateException(e);
    }
  }

  @Override
  public void addPrivateKey(
      String alias, char[] password, PrivateKey privateKey, Certificate[] certChain)
      throws CertificateException {
    throw new CertificateException("Unable to add private key certificates to JVM Key Store");
  }

  @Override
  public boolean getExists() {
    return true;
  }

}
