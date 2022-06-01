# Lesson 11. Practice.

Test Doubles

## Overview
In these practices, you create unit tests for a Helidon MP Application that uses oci vault sdk integration.  new Helidon MP service (or use one you already have from a previous lesson), modifying some existing classes and adding new ones to implement health checks, metrics, tracing, and logging features that are specific to your service. You also use built-in Helidon features and other products to see your changes in action.

The observability lesson is divided into sections, and the observability practices follow that same pattern. You can approach the practices however you wish, but you might find it most useful to work on the practice for each observability feature immediately after going through the corresponding lesson for that feature.
Common to All Practices in this Lesson

## Assumptions
* JDK 11 is installed
* IntelliJ IDEA is installed


## Prerequisite: 
Download this project. Note: The one they should download should not have the src/test directory as we will have to ask them
to create this in the lesson

## Practice 1. Create Unit Test using @HelidonTest and Fake Test Double approach
1. Open the project in IntelliJ by using `File->Open` from the menu and choose the directory of the project.
2. Update `pom.xml` from the project root directory to add JUnit5 dependency only for `test` scope by adding this entry inside the `dependencies` clause.
    ```xml
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <scope>test</scope>
    </dependency>
    ```
3. Add Helidon JUnit5 dependency for testing MicroProfile applications, such as `@HelidonTest`, `@AddBean`, `Configuration`, etc.
    ```xml
    <dependency>
        <groupId>io.helidon.microprofile.tests</groupId>
        <artifactId>helidon-microprofile-tests-junit5</artifactId>
        <scope>test</scope>
    </dependency>
    ```
4. Create the test package directory by doing the following:
   1. Using `File->New->Directory` from the menu, select `test/java` under `Maven Source Directories` on the `New Directory` pop up window.
   2. From the `Project` pane, highlight the newly created `java` directory under `src/test` then use `File->New->Package` from the menu and enter `io.heldon.unittest`. This will create `io/helidon/unittest` directory
   4. Highlight the newly created package `io.heldon.unittest`(which translates to `io/helidon/unittest` directory) under `src/test/java` then use
   5. As an alternative in the absence of Intellij IDE, the package directory can be created manually from the root directory of the project using below command:
      ```bash
      mkdir -p src/test/java/io/helidon/unittest
      ```
5. Create a helper class called `FakeSecretsData`. The objective of this class is to be able to provide an OCI create/get secret memory simulation by providing values necessary for those calls  via a local MAP or String Constant.
   1. From the project window, highlight the newly created package `io.heldon.unittest` (or `io/helidon/unittest` path) under `src/test/java`and use `File->New->Java Class`to enter `FakeSecretsData`.
   2. Inside the `FakeSecretsData` class, insert a `CREATE_SECRET_ID` constant that would represent as the OCID value of a created secret.
      ```java
      static String CREATE_SECRET_ID = "ocid1.vaultsecret.CREATE_SECRET_ID";
      ```
   3. Add a Map called `secretsData` containing key-value pairs that would simulate secret entries on a Vault service. The values of the secrets will be stored in base64 format replicating how secrets are stored in the OCI Vault.
      ```java
      static Map<String, String> secretsData = Map.of(
               "username", Base64Value.create("Joe").toBase64(),
               "password", Base64Value.create("Mighty!").toBase64()
      );
      ```
   4. Add a get method called `getDecodedValue` that accepts key as a parameter which then gets the value from the `secretsData` Map and return it as a base64 decoded value.
      ```java
      static String getDecodedValue(String key) {
          return Base64Value.createFromEncoded(secretsData.get(key)).toDecodedString();
      }
      ```
6. Create a new JUnit test class called `CdiBeanFakeTest` by highlighting the `io.heldon.unittest` (or `io/helidon/unittest` path) under `src/test/java` and use `File->New->Java Class` from the menu to enter `CdiBeanFakeTest`
7. Add an inner class that simulates a `VaultsClient` performing a `createSecret()` call: 
   1. Inside the new test class, create `FakeVaultsBean` as a static class that implements `Vaults` interface.
      ```
      static class FakeVaultsBean implements Vaults {
      }
      ```
   2. Resolve the Vaults class by hovering over it and clicking on `Import class`, so it will automatically add the corresponding import for that.
   3. Let the IDE implement all the methods of the interface and making sure that @Override is inserted.
   4. Locate `FakeVaultsBean.createSecret()` method and modify/replace it with code that would simulate a successful 200 response which includes a Secret object with an Id coming from `FakeSecretsData.CREATE_SECRET_ID`.
      ```java
      @Override
      public CreateSecretResponse createSecret(CreateSecretRequest createSecretRequest) {
          return CreateSecretResponse.builder()
              .__httpStatusCode__(200)
              .secret(Secret.builder().id(FakeSecretsData.CREATE_SECRET_ID).build())
              .build();
      }
      ```
8. Decorate the test class with `@HelidonTest` to start the microprofile server and `@AddBean(CdiBeanFakeTest.FakeVaultsBean.class)` to add the newly implemented fake Vault interface. These annotations need to be inserted before `class CdiBeanFakeTest`.
   ```java
   @HelidonTest
   @AddBean(CdiBeanFakeTest.FakeVaultsBean.class)
   ```
9. Inject `WebTarget` that will be used as the http interface to invoke the endpoint from the server started by `@HelidonTest`. This should be placed right after `class CdiBeanFakeTest` declaration.
   ```java
   @Inject
   private WebTarget webTarget;
   ```
10. Now we are ready to add a test method that would exercise the resource that will perform create secret call via `SecretsResource.createSecret()`. The http request would need to issue a post method on `secret/NewSecretKey` path and deliver a payload content that would represent the secret value.
    ```java
     @Test
     void testCreateSecret() {
         try (Response r = webTarget
                 .path("secret/NewSecretKey")
                 .request()
                 .post(Entity.text("NewSecretValue"))) {
             Assertions.assertEquals(FakeSecretsData.CREATE_SECRET_ID, r.readEntity(String.class));
             Assertions.assertEquals(200, r.getStatus());
         }
     }
    ```
11. Run the test by using  `Run->Run CdiBeanFakeTest`  and expect Test Results to be successful.
12. Next, add an inner class that simulates a `SecretsClient` successfully performing a `createSecret()` call:
    1. Add a new static class called `FakeSecretsBean` that implements `Secrets` interface as an internal class in `CdiBeanFakeTest`. 
       ```
       static class FakeSecretsBean implements Secrets {
       }
       ```
    2. Let the IDE implement all the methods of the interface and making sure that @Override is inserted. 
    3. Locate `FakeSecretsBean.getSecretBundleByName()` method and modify/replace it with code that would do the following sequence:
       1. Get the value of the secret key from the getSecretBundleByNameRequest parameter.
       2. Use that secret key to retrieve the corresponding base64 secret value from `FakeSecretsData.secretsData` Map.
       3. If not found, throw a RuntimeException simulating an almost similar behaviour when encountering failure on OCI production.
       4. Otherwise, if found, simulate a successful 200 response which includes a `SecretBundle` object containing the retrieved base64 secret value.
       5. And the code would look like this:
          ```java
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
          ```
13. Insert an `AddBean` annotation that adds `FakeSecretsBean` as a new Bean in the Helidon container. This should be placed anywhere before `class CdiBeanFakeTest` declaration.
    ```java
    @AddBean(CdiBeanFakeTest.FakeVaultsBean.class)
    ```
14. Add a junit test method that would simulate retrieving username and password secrets. Using `HelidonTest` The http request would need to issue a post method on `secret/NewSecretKey` path and deliver a payload content that would represent the secret value
    ```java
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
    ```
15. Run the test by using  `Run->Run CdiBeanFakeTest`  and expect all test results to be successful.
16. Add a negative junit test method that would simulate a failure
   ```java
   @Test
   void testUnknownSecret() {
       String secretKey = "unknown";
       boolean callFailed = false;
       try {
           webTarget
                   .path("secret/" + secretKey)
                   .request()
                   .get(String.class);
       } catch (Throwable t) {
           callFailed = true;
       }
       Assertions.assertTrue(callFailed, "Expecting a failure on the getSecret() call");
   }
   ```
17. Run the test by using  `Run->Run CdiBeanFakeTest` and expect all test results to be successful.
18. Congratulations! You have successfully completed this Practice.
    
## Practice 2. Create Unit Test using Mockito
1. This exercise assumes that you have completed `Practice 1` as it requires some components that had already been created there. If this is not the case, please ensure that you have performed steps 1 through 5 of `Practice 1` to be able to continue.
2. In the `pom.xml` from the project root directory, add Mockito dependency inside of the the `dependencies` clause.
    ```
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    ```
3. Create a new JUnit test class called `MockitoMockTest` by highlighting the `io.heldon.unittest` under `src/test/java` and use `File->New->Java Class` from the menu to enter `MockitoMockTest`
4. Using Mockito, mock Vaults and Secrets interface. This will be used later to test create and get secret.
   ```java
   private final static Vaults VAULTS_CLIENT = mock(Vaults.class);
   private final static Secrets SECRETS_CLIENT = mock(Secrets.class);
   ```   
5. Add a `@BeforeAll` JUnit annotated method called `beforeAll()` that gets invoked before all tests in the current test class.
   ```java
   @BeforeAll
   static void beforeAll() {
   }
   ```
6. In the beforeAll callback method, add code to stub `Secrets.getSecretBundleByName()` using mockito's doAnswer().
   The stubbing code will have the following sequence:
   1. Retrieve `GetSecretBundleByNameRequest` object which is passed as a parameter from `SECRETS_CLIENT.getSecretBundleByName()
   2. Get the secret key value from `GetSecretBundleByNameRequest`.
   3. Use that secret key to retrieve the corresponding base64 secret value from `FakeSecretsData.secretsData` Map. 
   4. If not found, throw a RuntimeException simulating an almost similar behaviour when encountering failure on OCI production.
   5. Otherwise, if found, simulate a successful 200 response which includes a `SecretBundle` object containing the retrieved base64 secret value.
   6. Code will look like below:
   ```java
   doAnswer(invocationOnMock -> {
       GetSecretBundleByNameRequest getSecretBundleByNameRequest = invocationOnMock.getArgument(0);
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
   }).when(SECRETS_CLIENT).getSecretBundleByName(any());
   ```
7. Add a helper method called `getSecretsResource()` that performs the following: 
   1. Instantiates `SecretsProvider` and passes the mocked `SECRETS_CLIENT` and `VAULTS_CLIENT` along with dummy values for vault Id, vault compartment Id and vault Key Id as parameters of the object.
   2. Instantiates `SecretsResource` and Pass the instantiated `SecretsProvider` as an argument to the object:
   ```java
   private SecretsResource getSecretsResource() {
       SecretsProvider secretsProvider = new SecretsProvider(
               SECRETS_CLIENT, VAULTS_CLIENT, "vaultId", "vaultCompartmentId", "vaultKeyId");
       return new SecretsResource(secretsProvider);
   }
   ```
8. Now we are ready to add a junit test that would exercise create secret call. Add a junit test that instantiates SecretsResource via `getSecretsResource()` and calls createSecret() from `SecretsResource` object and passing in any key/value pair as parameters.
   ```java
   @Test
   void testCreateSecret() {
       SecretsResource secretsResource = getSecretsResource();
       Assertions.assertEquals(FakeSecretsData.CREATE_SECRET_ID, secretsResource.createSecret("NewSecret", "Value"));
   }  
   ```
9. Run the test by using  `Run->Run MockitoMockTest`  and expect test result to be successful.
10. hh
   ```java
   @Test
   void testGetUsernameAndPassword() {
       SecretsResource secretsResource = getSecretsResource();
       String secretKey = "username";
       Assertions.assertEquals(FakeSecretsData.getDecodedValue(secretKey), secretsResource.getSecret(secretKey));
       secretKey = "password";
       Assertions.assertEquals(FakeSecretsData.getDecodedValue(secretKey), secretsResource.getSecret(secretKey));
   }
   ```
11. Run the test by using  `Run->Run MockitoMockTest`  and expect all test results to be successful.
12. hh
   ```java
   @Test
   void testGetUnknownSecret() {
       SecretsResource secretsResource = getSecretsResource();
       boolean callFailed = false;
       try {
           secretsResource.getSecret("Unknown");
       } catch(Throwable t) {
           callFailed = true;
       }
       Assertions.assertTrue(callFailed, "Expecting a failure on the getSecret() call");
   }
   ```
13. Run the test by using  `Run->Run MockitoMockTest`  and expect all test results to be successful.





