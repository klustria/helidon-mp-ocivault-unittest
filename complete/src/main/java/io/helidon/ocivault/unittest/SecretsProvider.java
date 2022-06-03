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
package io.helidon.ocivault.unittest;


import com.oracle.bmc.vault.model.SecretContentDetails;
import com.oracle.bmc.vault.requests.CreateSecretRequest;
import io.helidon.common.Base64Value;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.oracle.bmc.vault.Vaults;
import com.oracle.bmc.vault.model.Base64SecretContentDetails;
import com.oracle.bmc.vault.model.CreateSecretDetails;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;

/**
 * Provider of Secrets from OCI Vault.
 */
@ApplicationScoped
public class SecretsProvider {
    private final Vaults vaultsClient;
    private final Secrets secretsClient;
    private final String vaultId;
    private final String vaultCompartmentId;
    private final String vaultKeyId;

    /**
     * Create a new secrets provider, injecting an Oci vaults and secrets client and reading various
     * related parameter values from configuration to be used for retrieving or storing secrets.
     *
     * @param vaultsClient Oci vaults client
     * @param secretsClient Oci secrets client
     * @param vaultKeyId Vault OCID
     * @param vaultCompartmentId Vault compartment OCID
     * @param vaultKeyId Vault key OCID
     */
    @Inject
    public SecretsProvider(Secrets secretsClient,
                           Vaults vaultsClient,
                           @ConfigProperty(name = "oci.vault.id") String vaultId,
                           @ConfigProperty(name = "oci.vault.compartment-id") String vaultCompartmentId,
                           @ConfigProperty(name = "oci.vault.key-id") String vaultKeyId) {
        this.vaultsClient = vaultsClient;
        this.secretsClient = secretsClient;
        this.vaultId = vaultId;
        this.vaultCompartmentId = vaultCompartmentId;
        this.vaultKeyId = vaultKeyId;
    }

    /**
     * Get existing secret
     *
     * @param secretKey name of the secret
     * @return secretValue of the retrieved secret
     */
    public String getSecret(String secretKey) {
        SecretBundleContentDetails content = secretsClient.getSecretBundleByName(GetSecretBundleByNameRequest.builder()
                        .secretName(secretKey)
                        .vaultId(vaultId)
                        .build())
                .getSecretBundle()
                .getSecretBundleContent();
        if (content != null && content instanceof Base64SecretBundleContentDetails) {
            return Base64Value.createFromEncoded(((Base64SecretBundleContentDetails) content).getContent()).toDecodedString();
        } else {
            throw new RuntimeException("Unable to retrieve Secret content");
        }
    }

    /**
     * Create a new secret.
     *
     * @param secretKey name of the secret
     * @param secretText secret content
     * @return OCID of the created secret
     */
    public String createSecret(String secretKey, String secretText) {
        SecretContentDetails content = Base64SecretContentDetails.builder()
                .content(Base64Value.create(secretText).toBase64())
                .build();

        return vaultsClient.createSecret(CreateSecretRequest.builder()
                        .createSecretDetails(CreateSecretDetails.builder()
                                .secretName(secretKey)
                                .vaultId(vaultId)
                                .compartmentId(vaultCompartmentId)
                                .keyId(vaultKeyId)
                                .secretContent(content)
                                .build())
                        .build())
                .getSecret()
                .getId();
    }
}
