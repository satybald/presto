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

import com.facebook.presto.server.security.SecurityConfig.AuthenticationType;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import io.airlift.configuration.AbstractConfigurationAwareModule;
import io.airlift.http.server.TheServlet;

import javax.servlet.Filter;

import java.util.Set;

import static com.facebook.presto.server.security.SecurityConfig.AuthenticationType.CERTIFICATE;
import static com.facebook.presto.server.security.SecurityConfig.AuthenticationType.KERBEROS;
import static com.facebook.presto.server.security.SecurityConfig.AuthenticationType.LDAP;
import static com.facebook.presto.server.security.SecurityConfig.AuthenticationType.OAUTH;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static io.airlift.configuration.ConfigBinder.configBinder;

public class ServerSecurityModule
        extends AbstractConfigurationAwareModule
{
    @Override
    protected void setup(Binder binder)
    {
        newSetBinder(binder, Filter.class, TheServlet.class).addBinding()
                .to(AuthenticationFilter.class).in(Scopes.SINGLETON);

        Set<AuthenticationType> authTypes = buildConfigObject(SecurityConfig.class).getAuthenticationTypes();
        Multibinder<Authenticator> authBinder = newSetBinder(binder, Authenticator.class);

        if (authTypes.contains(CERTIFICATE)) {
            authBinder.addBinding().to(CertificateAuthenticator.class).in(Scopes.SINGLETON);
        }

        if (authTypes.contains(KERBEROS)) {
            configBinder(binder).bindConfig(KerberosConfig.class);
            authBinder.addBinding().to(KerberosAuthenticator.class).in(Scopes.SINGLETON);
        }

        if (authTypes.contains(LDAP)) {
            configBinder(binder).bindConfig(LdapConfig.class);
            authBinder.addBinding().to(LdapAuthenticator.class).in(Scopes.SINGLETON);
        }
        if (authTypes.contains(OAUTH)) {
            authBinder.addBinding().to(OAuthAuthenticator.class).in(Scopes.SINGLETON);
        }
    }
}
