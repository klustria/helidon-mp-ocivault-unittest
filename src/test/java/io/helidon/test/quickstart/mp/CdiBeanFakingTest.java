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

import com.oracle.bmc.Region;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.SecretsPaginators;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundle;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.requests.ListSecretBundleVersionsRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import com.oracle.bmc.secrets.responses.ListSecretBundleVersionsResponse;

import com.oracle.bmc.vault.Vaults;
import com.oracle.bmc.vault.VaultsPaginators;
import com.oracle.bmc.vault.VaultsWaiters;
import com.oracle.bmc.vault.model.Secret;
import com.oracle.bmc.vault.requests.CancelSecretDeletionRequest;
import com.oracle.bmc.vault.requests.CancelSecretVersionDeletionRequest;
import com.oracle.bmc.vault.requests.ChangeSecretCompartmentRequest;
import com.oracle.bmc.vault.requests.CreateSecretRequest;
import com.oracle.bmc.vault.requests.GetSecretRequest;
import com.oracle.bmc.vault.requests.GetSecretVersionRequest;
import com.oracle.bmc.vault.requests.ListSecretVersionsRequest;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.requests.ScheduleSecretDeletionRequest;
import com.oracle.bmc.vault.requests.ScheduleSecretVersionDeletionRequest;
import com.oracle.bmc.vault.requests.UpdateSecretRequest;
import com.oracle.bmc.vault.responses.CancelSecretDeletionResponse;
import com.oracle.bmc.vault.responses.CancelSecretVersionDeletionResponse;
import com.oracle.bmc.vault.responses.ChangeSecretCompartmentResponse;
import com.oracle.bmc.vault.responses.CreateSecretResponse;
import com.oracle.bmc.vault.responses.GetSecretResponse;
import com.oracle.bmc.vault.responses.GetSecretVersionResponse;
import com.oracle.bmc.vault.responses.ListSecretVersionsResponse;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretDeletionResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretVersionDeletionResponse;
import com.oracle.bmc.vault.responses.UpdateSecretResponse;

import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@HelidonTest
@AddBean(CdiBeanFakingTest.FakeSecretsBean.class)
@AddBean(CdiBeanFakingTest.FakeVaultsBean.class)
class CdiBeanFakingTest {
    @Inject
    private WebTarget webTarget;

    @Test
    void testUsernameAndPassword() {
        String secretKey = "username";
        String response = webTarget
                .path("secret/" + secretKey)
                .request()
                .get(String.class);
        Assertions.assertEquals(FakeSecretsData.getDecodedValue(secretKey), response);

        secretKey = "password";
        response = webTarget
                .path("secret/" + secretKey)
                .request()
                .get(String.class);
        Assertions.assertEquals(FakeSecretsData.getDecodedValue(secretKey), response);
    }

    @Test
    void testUnknownSecret() {
        String secretKey = "unknown";
        try {
            webTarget
                    .path("secret/" + secretKey)
                    .request()
                    .get(String.class);
            Assertions.fail("Expecting a failure in this call");
        } catch (Throwable t) {
        }
    }

    @Test
    void testCreateSecret() {
        try (Response r = webTarget
                .path("secret/NewSecretKey")
                .request()
                .post(Entity.text("NewSecretValue"))) {
            Assertions.assertEquals(FakeSecretsData.createSecretId, r.readEntity(String.class));
            Assertions.assertEquals(200, r.getStatus());
        }
    }

    @Alternative
    @Priority(50)
    static class FakeVaultsBean implements Vaults {
        @Override
        public void setEndpoint(String s) {

        }

        @Override
        public String getEndpoint() {
            return null;
        }

        @Override
        public void setRegion(Region region) {

        }

        @Override
        public void setRegion(String s) {

        }

        @Override
        public CancelSecretDeletionResponse cancelSecretDeletion(CancelSecretDeletionRequest cancelSecretDeletionRequest) {
            return null;
        }

        @Override
        public CancelSecretVersionDeletionResponse cancelSecretVersionDeletion(CancelSecretVersionDeletionRequest cancelSecretVersionDeletionRequest) {
            return null;
        }

        @Override
        public ChangeSecretCompartmentResponse changeSecretCompartment(ChangeSecretCompartmentRequest changeSecretCompartmentRequest) {
            return null;
        }

        @Override
        public CreateSecretResponse createSecret(CreateSecretRequest createSecretRequest) {
            return CreateSecretResponse.builder()
                    .__httpStatusCode__(200)
                    .secret(Secret.builder().id(FakeSecretsData.createSecretId).build())
                    .build();
        }

        @Override
        public GetSecretResponse getSecret(GetSecretRequest getSecretRequest) {
            return null;
        }

        @Override
        public GetSecretVersionResponse getSecretVersion(GetSecretVersionRequest getSecretVersionRequest) {
            return null;
        }

        @Override
        public ListSecretVersionsResponse listSecretVersions(ListSecretVersionsRequest listSecretVersionsRequest) {
            return null;
        }

        @Override
        public ListSecretsResponse listSecrets(ListSecretsRequest listSecretsRequest) {
            return null;
        }

        @Override
        public ScheduleSecretDeletionResponse scheduleSecretDeletion(ScheduleSecretDeletionRequest scheduleSecretDeletionRequest) {
            return null;
        }

        @Override
        public ScheduleSecretVersionDeletionResponse scheduleSecretVersionDeletion(ScheduleSecretVersionDeletionRequest scheduleSecretVersionDeletionRequest) {
            return null;
        }

        @Override
        public UpdateSecretResponse updateSecret(UpdateSecretRequest updateSecretRequest) {
            return null;
        }

        @Override
        public VaultsWaiters getWaiters() {
            return null;
        }

        @Override
        public VaultsPaginators getPaginators() {
            return null;
        }

        @Override
        public void close() throws Exception {

        }
    }

    static class FakeSecretsBean implements Secrets {
        @Override
        public void setEndpoint(String s) {

        }

        @Override
        public String getEndpoint() {
            return null;
        }

        @Override
        public void setRegion(Region region) {

        }

        @Override
        public void setRegion(String s) {

        }

        @Override
        public GetSecretBundleResponse getSecretBundle(GetSecretBundleRequest getSecretBundleRequest) {
            return null;
        }

        @Override
        public GetSecretBundleByNameResponse getSecretBundleByName(GetSecretBundleByNameRequest getSecretBundleByNameRequest) {
            String secretKey = getSecretBundleByNameRequest.getSecretName();
            String base64Data =  FakeSecretsData.secretsData.get(secretKey);
            if (base64Data == null) {
                throw new RuntimeException("Unknown secret key");
            }
            return GetSecretBundleByNameResponse.builder()
                    .__httpStatusCode__(200)
                    .secretBundle(
                            SecretBundle.builder().secretBundleContent(
                                    Base64SecretBundleContentDetails.builder().content(base64Data).build()).build())
                    .build();
        }

        @Override
        public ListSecretBundleVersionsResponse listSecretBundleVersions(ListSecretBundleVersionsRequest listSecretBundleVersionsRequest) {
            return null;
        }

        @Override
        public SecretsPaginators getPaginators() {
            return null;
        }

        @Override
        public void close() throws Exception {
        }
    }
}
