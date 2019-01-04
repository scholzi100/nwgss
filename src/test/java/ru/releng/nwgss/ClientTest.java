// Copyright 2019 rel-eng
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package ru.releng.nwgss;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

@RunWith(JUnit4.class)
public class ClientTest {

    @Test
    public void test() {
        Provider provider = new SspiKrb5SaslProvider();
        Security.insertProviderAt(provider, 1);

        LdapContext ldapContext;
        try {
            ldapContext = new InitialLdapContext(getGSSAPIProperties(), null);
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[] {"cn", "sn", "userPrincipalName", "memberOf", "name"});
        try {
            for (int i = 0; i < 10; i++) {
                NamingEnumeration<SearchResult> result = ldapContext.search("CN=Users,DC=CONTOSO,DC=COM",
                        "(objectclass=organizationalPerson)", searchControls);
                try {
                    while (result.hasMore()) {
                        SearchResult oneResult = result.next();
                        System.out.println("Result: " + oneResult);
                    }
                } finally {
                    result.close();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        try {
            ldapContext.close();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties getGSSAPIProperties() {
        Properties result = new Properties();
        result.put("javax.security.sasl.qop", "auth-conf");
        result.put("javax.security.sasl.server.authentication", "true");
        result.putAll(getLdapSettings());
        return result;
    }

    private static Properties getLdapSettings() {
        Properties result = new Properties();
        result.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        result.put("java.naming.referral", "ignore");
        result.put("java.naming.provider.url", "ldap://WIN-0JVM6LI2RKT.contoso.com:389");
        result.put("java.naming.security.authentication", "GSSAPI");
        return result;
    }

}
