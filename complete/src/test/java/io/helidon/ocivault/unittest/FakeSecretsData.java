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

import io.helidon.common.Base64Value;

import java.util.Map;

class FakeSecretsData {
    static final String CREATE_SECRET_ID = "ocid1.vaultsecret.createSecretId";

    static Map<String, String> secretsData = Map.of(
            "username", Base64Value.create("Joe").toBase64(),
            "password", Base64Value.create("Mighty!").toBase64()
    );

    static String getDecodedValue(String key) {
        return Base64Value.createFromEncoded(secretsData.get(key)).toDecodedString();
    }
}
