# Helidon MP Unit Test

This project provides a sample Helidon MP application that demonstrates creation and retrieval of Oci Vault secrets data and how 
this functionalities can be unit tested by employing different mock approaches:

1. Mocking/Stubbing using Mockito - Mockito is a popular JAVA-based mocking framework. [src/test/java/io/helidon/test/quickstart/mp/MockitoMockingTest.java](src/test/java/io/helidon/test/quickstart/mp/MockitoMockingTest.java)
2. Faking using CDI Bean - Creating a Fake implementation of an Interface [src/test/java/io/helidon/test/quickstart/mp/CdiBeanMockingTest.java](src/test/java/io/helidon/test/quickstart/mp/CdiBeanMockingTest.java)

Mock testing is an approach to unit testing that lets you make assertions about how the code under test is interacting with other system modules. In mock testing, the dependencies are replaced with objects that simulate the behaviour of the real ones. The purpose of mocking is to isolate and focus on the code being tested and not on the behaviour or state of external dependencies.

Mocking is generally useful during unit testing so that external dependencies are no longer a constraint to the unit under test. Often those dependencies may themselves be under development. Without mocking, if a test case fails, it will be hard to know if the failure is due to our code unit or due to dependencies.

## Run test
With JDK11+
```bash
mvn test
```



## Create a simple app that creates and retrieves secrets from the OCI vault
1. Create a provider class that has a method
    ```java
    @ApplicationScoped
    public class SecretsProvider {
        private final Vaults vaultsClient;
        private final Secrets secretsClient;
        private final String vaultId;
        private final String vaultCompartmentId;
        private final String vaultKeyId;
    
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
    ```
2. Any
    ```java
    @Path("/secret")
    @ApplicationScoped
    public class SecretsResource {
        /**
         * The greeting message provider.
         */
        private SecretsProvider secretsProvider;
    
        /**
         * Using constructor injection to get a configuration property.
         * By default this gets the value from META-INF/microprofile-config
         *
         * @param secretsProvider the configured greeting message
         */
        @Inject
        public SecretsResource(SecretsProvider secretsProvider) {
            this.secretsProvider = secretsProvider;
        }
        
        @Path("/{secretName}")
        @GET
        public String getSecret(@PathParam("secretName") String secretName) {
            return secretsProvider.getSecret(secretName);
        }
        
        @POST
        @Path("/{secretName}")
        public String createSecret(@PathParam("secretName") String secretName, String secretText) {
            return secretsProvider.createSecret(secretName, secretText);
        }
    }
    ```

## Mocking/Stubbing using Mockito
