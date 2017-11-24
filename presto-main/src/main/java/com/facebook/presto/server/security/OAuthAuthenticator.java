/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.server.security;

import io.airlift.http.client.HttpClient;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.security.Principal;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public class OAuthAuthenticator
        implements Authenticator
{
    final HttpClient httpClient = null;

//    @Inject
//    public OAuthAuthenticator(HttpClient httpClient)
//    {
//        this.httpClient = httpClient;
//    }

    @Override
    public Principal authenticate(HttpServletRequest request)
            throws AuthenticationException
    {
        String header = request.getHeader(AUTHORIZATION);
        System.out.println(request);
        System.out.println(header);
        return new OAuthPrincipal("cn=SATYBALD,ou=Fruits,o=Food");
    }

    static class OAuthPrincipal implements Principal {
        private final String name;

        public OAuthPrincipal(String name)
        {
            this.name = name;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (!(o instanceof OAuthPrincipal)) {
                return false;
            }

            OAuthPrincipal that = (OAuthPrincipal) o;

            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public String toString()
        {
            return "OAuthPrincipal{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public int hashCode()
        {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }
}
