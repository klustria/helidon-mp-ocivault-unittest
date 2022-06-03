# Helidon MP & OCI Vault SDK Integration

Sample Helidon MP project that includes multiple REST operations providing capability to create or retrieve Oci 
Vault secrets data. The project will be used to demonstrate how to create unit tests.

## Setup
1. Create OCI configuration file which defaults to `~/.oci/config`. Refer to [SDK and CLI Configuration File](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm) for more details.
2. Create required OCI components for managing secrets:
   1. Create an OCI Vault. Refer to [Mananaging Vaults](https://docs.oracle.com/en-us/iaas/Content/KeyManagement/Tasks/managingvaults.htm) for more guidance.
   2. Create Keys that will be used to encrypt secrets. Refer to [Managing Keys](https://docs.oracle.com/en-us/iaas/Content/KeyManagement/Tasks/managingkeys.htm) for more details.
   3. Create a Secret from the OCI Vault that can be retrieved using its secret name. Refer to [Managing Secrets](https://docs.oracle.com/en-us/iaas/Content/KeyManagement/Tasks/managingsecrets.htm) for more guidance.
3. Update `OCI Vault parameters` section in [microprofile-config.properties](src/main/resources/META-INF/microprofile-config.properties)  by using the the vault, vault's compartment and vault's key OCIDs created in prior steps.

## Build and run
With JDK11+
```bash
mvn package
java -jar target/helidon-mp-ocivault-unittest.jar
```

## Exercise the application
1. Create a new secret on the OCI Vault:
   ```bash
   curl -X POST -d "Encrypt3d" http://localhost:8080/secret/new_password
   ocid1.vaultsecret.oc1.iad.amaaaaaaytgkucya5l2kkh5akvpt2eo6teed3jak7zqrp2jqt73brcce56vq
   ```
2. Retrieve a secret from the OCI Vault:
   ```bash
   curl http://localhost:8080/secret/database_password
   Password123!
   ```
