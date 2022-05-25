/*
 * Copyright (c) 2022 Oracle and/or its affiliates.
 *
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
package io.helidon.test.quickstart.mp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * A simple JAX-RS resource to allow retrieval or creation of OCI vault secrets. Examples:
 *
 * Get existing secret
 * curl -X GET http://localhost:8080/secret/SecretKey
 *
 * Create a new secret
 * curl -X PUT -H -d 'SecretValue' http://localhost:8080/secret/SecretKey
 *
 * The response is returned as a String object.
 */
@Path("/secret")
@ApplicationScoped
public class SecretsResource {
    /**
     * The greeting message provider.
     */
    private SecretsProvider secretsProvider;

    /**
     * Using constructor injection instantiate OCI vault and secret clients and gets various configuration properties.
     * By default this gets the value from META-INF/microprofile-config
     *
     * @param secretsProvider the provider of secrets from OCI Vault
     */
    @Inject
    public SecretsResource(SecretsProvider secretsProvider) {
        this.secretsProvider = secretsProvider;
    }

    /**
     * Return a secret value using secretName that was provided.
     *
     * @param secretName the name to greet
     * @return secretValue of the retrieved secret
     */
    @Path("/{secretName}")
    @GET
    public String getSecret(@PathParam("secretName") String secretName) {
        return secretsProvider.getSecret(secretName);
    }

    /**
     * Create a new secret.
     *
     * @param secretName name of the secret
     * @param secretText secret content
     * @return OCID of the created secret
     */
    @POST
    @Path("/{secretName}")
    public String createSecret(@PathParam("secretName") String secretName, String secretText) {
        return secretsProvider.createSecret(secretName, secretText);
    }
}
