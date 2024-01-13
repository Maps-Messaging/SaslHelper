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

package io.mapsmessaging.security.identity.impl;

import io.mapsmessaging.security.identity.IdentityLookup;
import io.mapsmessaging.security.identity.IdentityLookupFactory;
import io.mapsmessaging.security.identity.NoSuchUserFoundException;
import io.mapsmessaging.security.identity.impl.ldap.LdapAuth;
import io.mapsmessaging.security.jaas.PropertiesLoader;
import io.mapsmessaging.security.passwords.PasswordHandler;
import io.mapsmessaging.security.passwords.PasswordParserFactory;
import io.mapsmessaging.security.passwords.hashes.md5.Md5UnixPasswordHasher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.Context;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class LdapIdentifierTest {

  static Properties properties;

  @BeforeAll
  static void loadProperties() throws IOException {
    properties = PropertiesLoader.getProperties("ldap.properties");
  }

  @Test
  void simpleLoad() throws NoSuchUserFoundException {
    if (properties == null || properties.isEmpty()) {
      return;
    }
    Map<String, String> map = new LinkedHashMap<>();
    map.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    map.put(Context.SECURITY_AUTHENTICATION, "simple");

    map.put(Context.PROVIDER_URL, properties.getProperty("ldapUrl"));
    map.put(Context.SECURITY_PRINCIPAL, properties.getProperty("ldapUser"));
    map.put(Context.SECURITY_CREDENTIALS, properties.getProperty("ldapPassword"));

    map.put("passwordKeyName", "userpassword");

    map.put("searchBase", properties.getProperty("searchBase"));
    map.put("searchFilter", properties.getProperty("searchFilter"));

    map.put("groupSearchBase", properties.getProperty("groupSearchBase"));
    map.put("groupSearchFilter", properties.getProperty("groupSearchFilter"));

    IdentityLookup lookup = IdentityLookupFactory.getInstance().get("ldap", map);
    Assertions.assertEquals(lookup.getClass(), LdapAuth.class);
    char[] hash = lookup.getPasswordHash(properties.getProperty("username"));
    Assertions.assertNotNull(hash);
    Assertions.assertNotEquals(0, hash.length);
    String pwd = new String(hash);
    Assertions.assertEquals(properties.getProperty("hashedPassword"), pwd);
    PasswordHandler passwordHasher = PasswordParserFactory.getInstance().parse(pwd);
    Assertions.assertEquals(Md5UnixPasswordHasher.class, passwordHasher.getClass());
  }

}
