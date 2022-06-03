# Helidon MP & OCI Vault SDK Integration

Sample Helidon MP project that includes multiple REST operations performs that creates or retrieves Oci 
Vault secrets data. The project will be used to demonstrate how to create a unit tests.

## Build and run
With JDK11+
```bash
mvn package
java -jar target/helidon-mp-unit-test.jar
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
