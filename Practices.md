# Practices for Lesson 11. Creating Unit Test

## Overview
In this lab exercise, you will be presented with a project that contains the complete source code of a Helidon 
Microprofile (MP) application that uses OCI Vault SDK integration, but does not have a unit test included. By following
and completing the steps in all the included practices, the goal is for you to understand how to create a unit test
that uses `test doubles`. A `test double` is a generic term used for cases where some production objects, usually
those that represent external dependencies, are replaced with simplified versions that simulate and behave like its 
production equivalent. By reducing complexity on those external dependencies in the form of `test doubles`, this allows
code to be verified independently from them and at the same time, test the system's interaction with them.

In this project, our external dependency is the OCI Vault service interaction used for creating and retrieving secrets.
To simplify testing, we will use `test doubles` to replace the production implementation of the OCI Vault integration 
using the following `test double` approaches:

1. Fake objects with `HelidonTest` - for this approach, we will be using various components of HelidonTest:
   * `@HelidonTest` - a class level annotation that will cause the test extension to start a Helidon microprofile server 
   for you, so that you do not need to manage the server lifecycle in your test.
   * `@AddBean(Sample.class)` - a class level annotation that loads the provided class as a CDI bean in a Helidon 
   container and effectively replacing the production version of the bean.
   * `WebTarget` - an injectable object that can be used as the http interface to invoke the server endpoint started 
   by `@HelidonTest`
2. Mocking/stubbing using `Mockito` - for this approach, we will rely on Mockito as a mocking framework which internally
uses java reflection to achieve this goal. Specific to this tutorial, we will only use these Mockito components:
   * `mock(Sample.class)` - create a mock object of a specified class or an interface that will be used by a Mockito 
   directive to specify the specific method to stub.
   * `when(...).thenReturn(...)` - is a directive used to return a particular hardcoded value whenever we invoke a 
   specific method on a mock object.
   * `doAnswer(...).when(...)` - is a directive used when we need to do some additional logic (for example, return 
   values are based on code logic around input parameters) when the method from a mocked object is called.

**When to use one approach over the other?** Use `HelidonTest` when the test relies on the Helidon container and a bean 
is needed to be replaced with a fake simulated version. This is specially useful if the target object is a bean that is 
not accessible from the unit test. Otherwise, use `Mockito` if the intention is to directly mock an object that is 
directly accessible from the unit test and allow stubbing directives to define the behavior of specific methods.  

## Assumptions
* JDK 11 is installed
* IntelliJ IDEA is installed
* An understanding of Java programming language.
* A basic knowledge on how to use java OCI SDK

## Practice 1. Create Unit Test using @HelidonTest and Fake Objects
1. Set up the project by unzipping the distribution [helidon-mp-oci-unittest-practice.zip](helidon-mp-oci-unittest-practice.zip) to any directory of your choice.
      ```bash
      $ unzip helidon-mp-oci-unittest-practice.zip
      ```
2. Open the project in IntelliJ.
   1. If no projects are active in the IDE, Use `Open` button from the Welcome screen and choose the directory of the project.
   2. If other projects were previously opened in the IDE, use `File->Open` from the menu bar and choose the directory of the project.
3. Open and update `pom.xml` from the project's root directory to add dependencies that are required for this exercise:
   1. Add junit5 dependency with test scope inside the `dependencies` clause. Place it at the bottom of the dependency declarations.
       ```xml
       <dependency>
           <groupId>org.junit.jupiter</groupId>
           <artifactId>junit-jupiter-api</artifactId>
           <scope>test</scope>
       </dependency>
       ```
   2. Add Helidon junit5 dependency with test scope for testing MicroProfile applications, such as `@HelidonTest`, `@AddBean`, `Configuration`, etc. Place it after `org.junit.jupiter:junit-jupiter-api` dependency declaration.
       ```xml
       <dependency>
           <groupId>io.helidon.microprofile.tests</groupId>
           <artifactId>helidon-microprofile-tests-junit5</artifactId>
           <scope>test</scope>
       </dependency>
       ```
   3. Click the Maven tool window on the right side of the IDE and choose the toolbar button with 2 circular arrows icon. Clicking this button will reload the Maven projects that includes the newly added dependencies. 
4. Create the test package directory by doing the following:
   1. Using `File->New->Directory` from the menu, select `test/java` under `Maven Source Directories` on the `New Directory` pop-up window.
   2. From the `Project` pane, highlight the newly created `java` directory under `src/test` then use `File->New->Package` from the menu and enter `io.heldon.unittest`. Under the covers, this will create `io/helidon/unittest` directory.
5. Create a helper class called `FakeSecretsData`. The objective of this class is to be able to provide help in performing an OCI create/get calls by being able to simulate access to secrets from memory through a local Map or String Constant, rather than from the actual OCI cloud service.
   1. From the Project tool window, highlight the newly created package `io.heldon.unittest` (or `io/helidon/unittest` path) under `src/test/java`and use `File->New->Java Class` and enter `FakeSecretsData` which will then create `public class FakeSecretsData`. The class only needs package visibility scope so remove the `public` keyword from the class declaration. 
   2. Inside the `FakeSecretsData` class, insert a `CREATE_SECRET_ID` constant just below the class declaration. This constant will represent as the OCID value of a created secret.
      ```java
      static String CREATE_SECRET_ID = "ocid1.vaultsecret.CREATE_SECRET_ID";
      ```
   3. Add a Map called `secretsData` containing key-value pairs that would simulate secret entries on a Vault service. The values of the secrets will be stored in base64 format replicating how actual secrets are stored in the OCI Vault.
      ```java
      static Map<String, String> secretsData = Map.of(
               "username", Base64Value.create("Joe").toBase64(),
               "password", Base64Value.create("Mighty!").toBase64()
      );
      ```
   4. Add a get method called `getDecodedValue` that accepts key as a parameter which then helps in searching the corresponding value from the `secretsData` Map and return it as a base64 decoded value.
      ```java
      static String getDecodedValue(String key) {
          return Base64Value.createFromEncoded(secretsData.get(key)).toDecodedString();
      }
      ```
   5. Resolve all symbols that are in red indicating that they are currently unresolved:
      1. Hover the mouse cursor over `Map` and click `Import class` from the tooltip pop-up and select `java.util` if presented with multiple choices. 
      2. Do the same for Base64Value and select `io.helidon.common` if presented with multiple choices.
      3. If having difficulty with above steps, you can also just manually add these import statements right after the package declaration (*Note: As much as possible, arrange the imports in alphabtical order and group them based on package name*):
         ```java
         import io.helidon.common.Base64Value;
        
         import java.util.Map;
         ```
6. Create a new junit test class by highlighting the `io.heldon.unittest` (or `io/helidon/unittest` path) under `src/test/java` from the Project tool window and use `File->New->Java Class` from the menu and enter `CdiBeanFakeTest` which will then create `public class CdiBeanFakeTest`. The class only needs package visibility scope so remove the `public` keyword from the class declaration.
7. Add an inner class that simulates a `VaultsClient` performing a `createSecret()` call: 
   1. Inside the new test class, create `FakeVaultsBean` as a static class that implements `Vaults` interface. Place this code snippet at the end of the `CdiBeanFakeTest's` body
      ```java
      static class FakeVaultsBean implements Vaults {
      }
      ```
   2. Resolve the `Vaults` interface by hovering mouse cursor over it and clicking on `Import class` from the tooltip pop-up which will automatically add the corresponding import statement.
   3. If having difficulty with this step, manually add this import statement (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```java
      import com.oracle.bmc.vault.Vaults;
      ```
   4. Hover mouse cursor over `static class FakeVaultsBean` declaration and click `implement methods` which will then show `Select Methods to Implement` window. Make sure that the `Insert @Override` check box is ticked and press `OK` button to proceed.
   5. From the generated methods inside `FakeVaultsBean`, locate `createSecret()` method and modify/replace it with code that would simulate a successful 200 response which includes a Secret object with an Id coming from `FakeSecretsData.CREATE_SECRET_ID`.
      ```java
      @Override
      public CreateSecretResponse createSecret(CreateSecretRequest createSecretRequest) {
          return CreateSecretResponse.builder()
              .__httpStatusCode__(200)
              .secret(Secret.builder().id(FakeSecretsData.CREATE_SECRET_ID).build())
              .build();
      }
      ```
   6. Hover mouse cursor over `Secret` and click `Import class` from the tooltip pop-up which will automatically add the corresponding import statement.
   7. If having difficulty with the prior step, manually add this import statement (*Note: As much as possible, arrange the imports in alphabtical order and group them based on package name*):
      ```java
      import com.oracle.bmc.vault.model.Secret;
      ```
8. Add Helidon junit5 annotations to the test class:
   1. Insert `@HelidonTest` to start the microprofile server and `@AddBean(CdiBeanFakeTest.FakeVaultsBean.class)` to add the newly implemented fake Vault interface into the Helidon container and override the production implementation. These annotations need to be inserted before `class CdiBeanFakeTest` declaration.
      ```java
      @HelidonTest
      @AddBean(CdiBeanFakeTest.FakeVaultsBean.class)
      ```
   2. Perform successive mouse cursor hovers over `@HelidonTest` and `@AddBean` annotations and click `Import class` from the tooltip pop-up to automatically generate corresponding import statements.
   3. If having difficulty with the previous step, just manually add these import statements (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```java
      import io.helidon.microprofile.tests.junit5.AddBean;
      import io.helidon.microprofile.tests.junit5.HelidonTest;
      ```
9. Inject `WebTarget` that will be used as the http interface to invoke the endpoint from the server started by `@HelidonTest`.
   1. Place the following code snippet right after `class CdiBeanFakeTest` declaration.
      ```java
      @Inject
      private WebTarget webTarget;
      ```
   2. Perform successive mouse cursor hovers over `@Inject` and `WebTarget` and click `Import class` from the tooltip pop-up to automatically generate corresponding import statements.
   3. If having difficulty with the previous step, just manually add these import statements (*Note: As much as possible, arrange the imports in alphabtical order and group them based on package name*):
      ```java
      import javax.inject.Inject;
      import javax.ws.rs.client.WebTarget;
      ```
10. Now it is time to write our first test method that will exercise the JaxRS resource and perform create secret call via `SecretsResource.createSecret()`. 
    1. Use the injected `webTarget` object to make a http request that needs to issue a post method on `secret/NewSecretKey` path and deliver a payload content that represents the secret value.
    2. Http response entity should match FakeSecretsData.CREATE_SECRET_ID.
    3. Http response code should match 200.
    4. The code snippet will look like below, copy and place right after the `private WebTarget webTarget` declaration:
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
    5. Perform successive mouse cursor hovers over `@Test`, `Response` and `Assertions` and click `Import class` from the tooltip pop-up to automatically generate corresponding import statements.
    6. If having difficulty with the previous step, just manually add these import statements (*Note: As much as possible, arrange the imports in alphabtical order and group them based on package name*)::
       ```java
       import org.junit.jupiter.api.Assertions;
       import org.junit.jupiter.api.Test;

       import javax.ws.rs.client.Entity;
       import javax.ws.rs.core.Response;
       ```
    7. Run the test by doing either of the following:
        1. Right-click on `CdiBeanFakeTest` found on the Project tool window and choosing `Run 'CdiBeanFakeTest'` from the pull down menu.
        2. Click on the right pointing green arrow head on the left side of the `class CdiBeanFakeTest` declaration and choose `Run 'CdiBeanFakeTest'`
    8. The test will begin execution and all test results should be successful once completed.
11. Next, add an inner class that simulates a `SecretsClient` performing a `getSecretBundleByName()` call:
    1. Add a new static class called `FakeSecretsBean` that implements `Secrets` interface as an internal class in `CdiBeanFakeTest` and place it right after `FakeVaultsBean` inner class. 
       ```java
       static class FakeSecretsBean implements Secrets {
       }
       ```
    2. Hover mouse curson over `Secrets` and click  `Import class` from the tooltip pop-up which will automatically add the corresponding import statement.
    3. If having difficulty with the prior step, manually add this import statement (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
       ```java
       import com.oracle.bmc.secrets.Secrets;
       ```
    4. Hover mouse cursor over the newly added `static class FakeSecretsBean implements Secrets {` declaration and click `implement methods` which will then show `Select Methods to Implement` window. Make sure that the `Insert @Override` check box is ticked and press `OK` button to proceed.
    5. From the generated methods inside `FakeSecretsBean`, locate `getSecretBundleByName()` method and modify/replace it with code that would do the following sequence:
       1. Get the value of the secret key from the getSecretBundleByNameRequest parameter.
       2. Use that secret key to retrieve the corresponding base64 secret value from `FakeSecretsData.secretsData` Map.
       3. If not found, throw a RuntimeException simulating an identical behaviour when encountering failure on OCI production.
       4. Otherwise, if found, simulate a successful 200 response which includes a `SecretBundle` object containing the retrieved base64 secret value.
       5. Below is the code snippet and use this to replace the existing `getSecretBundleByName()` method:
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
    6. Perform successive mouse cursor hovers over `SecretBundle` and `Base64SecretBundleContentDetails` and click `Import class` from the tooltip window which will automatically add the corresponding import statements.
    7. If having difficulty with the prior step, manually add these import statements (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
       ```java
       import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
       import com.oracle.bmc.secrets.model.SecretBundle;
       ```
12. Insert an `AddBean` annotation that adds the newly implemented `FakeSecretsBean` as a new cdi bean in the Helidon container that will override the production implementation. Place this before `class CdiBeanFakeTest` declarations.
    ```java
    @AddBean(CdiBeanFakeTest.FakeSecretsBean.class)
    ```
13. Now we are ready to add another test method that will exercise the JaxRS resource that retrieves secrets via `SecretsResource.getSecret()`.
    1. Use the injected `webTarget` object to make 2 http requests that use get method on `secret/{SecretName}` path. `{SecretName}` on the path will be replaced with `username` and `password`. Place this code snippet right after the `testCreateSecret()` method:
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
    2. Run the test by doing either of the following:
        1. Right-click on `CdiBeanFakeTest` found on the Project window tool and choosing `Run 'CdiBeanFakeTest'` from the pull down menu.
        2. Click on the green arrow head on the left side of the `class CdiBeanFakeTest` declaration and choose `Run 'CdiBeanFakeTest'`
    3. The test will begin execution and all test results should be successful once completed.
14. Our final test method will implement a negative scenario that would replicate a failure case where a non-existent secret is being retrieved.
    1. Use the injected `webTarget` object to make an http requests that use get method on `secret/unknown` path. The `unknown` part on the path represents the secret name and hence should result to failure. Place this code snippet right after the `testUsernameAndPassword()` method:
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
    2. Run the test by doing either of the following:
       1. Right-click on `CdiBeanFakeTest` found on the Project window tool and choosing `Run 'CdiBeanFakeTest'` from the pull down menu.
       2. Click on the green arrow head on the left side of the `class CdiBeanFakeTest` declaration and choose `Run 'CdiBeanFakeTest'` 
    3. The test will begin execution and all test results should be successful once completed.
15. Congratulations! You have successfully completed this exercise.
    
## Practice 2. Create Unit Test using Mockito
1. This exercise assumes that you have completed `Practice 1` as it requires some components that had already been created in that exercise. If this is not the case, ensure that you have performed steps 1 through 6 of `Practice 1` to be able to continue.
2. Update `pom.xml` to from the project root directory to add required dependency:
   1. Add Mockito dependency with test scope inside the `dependencies` clause. Place it after `io.helidon.microprofile.tests:helidon-microprofile-tests-junit5` dependency declaration.
       ```
       <dependency>
           <groupId>org.mockito</groupId>
           <artifactId>mockito-core</artifactId>
           <scope>test</scope>
       </dependency>
       ```
   2. Click the Maven tool window on the right side of the IDE and choose the toolbar button with arrow cycle icon. Clicking this button will reload the Maven projects along with the newly added dependency.
3. Create a new junit test class by highlighting the `io.heldon.unittest` (or `io/helidon/unittest` path) under `src/test/java` and use `File->New->Java Class` from the menu to enter `MockitoMockTest` which will then create `public class MockitoMockTest`. The class only needs visibility scope from within the package so remove the `public` keyword.
4. Using Mockito, mock Vaults and Secrets interface. This will be used later to test create and get secret.
   1. Add this code snippet right after the `class MockitoMockTest` declaration.
      ```java
      private final static Vaults VAULTS_CLIENT = mock(Vaults.class);
      private final static Secrets SECRETS_CLIENT = mock(Secrets.class);
      ```
   2. Resolve all object types that are in red indicating that they cannot be resolved:
      1. Hover mouse cursor over `Vaults` and click `Import class` from the tooltip pop-up.
      2. Do the same as above for `Secrets`.
      3. Do the same for `mock` and click on `Import static method org.mockito.Mockito.mock` from the tooltip pop-up.
      4. All above steps will generate corresponding import statements, but if problem is encountered, you can just manually add these import statements (*Note: As much as possible, arrange the imports in alphabtical order and group them based on package name*):
         ```java
         import com.oracle.bmc.secrets.Secrets;
         import com.oracle.bmc.vault.Vaults;

         import static org.mockito.Mockito.mock;
         ```
5. Add a `@BeforeAll` junit annotated method called `setUp()` that will get invoked before all tests in the current test class.
   1. Insert below code snippet right after the `private final static Secrets SECRETS_CLIENT...` constant declaration:
      ```java
      @BeforeAll
      static void setUp() {
      }
      ```
   2. Hover mouse cursor over `@BeforeAll` annotation and click on `Import class` to automatically add the corresponding import statement.
   3. If having difficulty with prior step, manually add this import statement (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```java
      import org.junit.jupiter.api.BeforeAll;
      ```
   4. Inside `setUp()` method, add code to stub `Vaults.createSecret()` using mockito's `when()`. The stubbing code will simply return a response that contains a 200 http code and a `Secret` object with an Id coming from `FakeSecretsData.CREATE_SECRET_ID`.
      ```java
      when(VAULTS_CLIENT.createSecret(any())).thenReturn(
              CreateSecretResponse.builder()
                      .__httpStatusCode__(200)
                      .secret(Secret.builder().id(FakeSecretsData.CREATE_SECRET_ID).build())
                      .build());
   
      ```
   5. From the snippet above, hover mouse cursor over `when` and click on `Import static method...` and choose `Mockito.when(org.mockito)` from the `Method to Import` window. This will automatically generate corresponding import statement.
   6. Hover mouse cursor over `any` and click on `Import static method org.mockito.ArgumentMatchers.any`. This will automatically generate corresponding import statement. 
   7. Do the same for `CreateSecretResponse` and `Secret` and click on `Import class`, one after the other. This will automatically generate corresponding import statement.
   8. If having difficulties with prior steps for automatic import resolution, manually add these import statements (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```java
      import com.oracle.bmc.vault.model.Secret;
      import com.oracle.bmc.vault.responses.CreateSecretResponse;

      import static org.mockito.ArgumentMatchers.any;

      import static org.mockito.Mockito.when;
      ```
6. Add a helper method called `getSecretsResource()` that returns `SecretsResource` with instantiated `SecretsProvider` as the method argument. `SecretsProvider` in turn will use the mocked `SECRETS_CLIENT` and `VAULTS_CLIENT` along with dummy values for vault Id, vault compartment Id and vault Key Id as parameters. 
   1. Place this code snippet at the very end of the `MockitoMockTest` clause.
      ```java
      private SecretsResource getSecretsResource() {
          SecretsProvider secretsProvider = new SecretsProvider(
                  SECRETS_CLIENT, VAULTS_CLIENT, "vaultId", "vaultCompartmentId", "vaultKeyId");
          return new SecretsResource(secretsProvider);
      }
      ```
   2. Perform successive mouse cursor hovers over `SecretsResource` and `SecretsProvider` and click on `Import class`. Choose `io.helidon.unittest` if presented with multiple choices. This will automatically generate corresponding import statements.
   3. If having difficulty with prior step, manually add these import statements (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```java
      import io.helidon.unittest.SecretsProvider;
      import io.helidon.unittest.SecretsResource;
      ```
7. Now we are ready to add a junit test that would exercise create secret call. 
   1. Add a junit test that instantiates `SecretsResource` via `getSecretsResource()` and calls `createSecret()` passing in any key/value pair as parameters. Place this code snippet right after the `setUp()` method:
      ```java
      @Test
      void testCreateSecret() {
          SecretsResource secretsResource = getSecretsResource();
          Assertions.assertEquals(FakeSecretsData.CREATE_SECRET_ID, secretsResource.createSecret("NewSecret", "Value"));
      }  
   2. Perform successive mouse cursor hovers over `@Test` and `Assertions` and click on `Import class`. This will automatically generate corresponding import statements.
   3. If having difficulty with prior step, manually add these import statements (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```
      import org.junit.jupiter.api.Assertions;
   
      import org.junit.jupiter.api.Test;
      ```
   4. Run the test by doing either of the following:
       1. Right-click on `MockitoMockTest` found on the `Project` window pane and choosing `Run 'MockitoMockTest'` from the pull down menu.
       2. Click on the green arrow head on the left side of the `class MockitoMockTest` declaration and choose `Run 'MockitoMockTest'`
   5. The test will begin execution and test result should be successful once completed.
8. Inside `setUp()` method, add code to stub `Secrets.getSecretBundleByName()` using mockito's `doAnswer()`. The stubbing code will have the following sequence:
   1. Retrieve `GetSecretBundleByNameRequest` object which is passed as a parameter from `SECRETS_CLIENT.getSecretBundleByName()
   2. Get the secret key value from `GetSecretBundleByNameRequest`.
   3. Use that secret key to retrieve the corresponding base64 secret value from `FakeSecretsData.secretsData` Map.
   4. If not found, throw a RuntimeException simulating an identical behaviour when encountering failure on OCI production.
   5. Otherwise, if found, simulate a successful 200 response which includes a `SecretBundle` object containing the retrieved base64 secret value.
   6. Code snippet will look like below and should be placed after the `when` call inside of the `setUp()` method:
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
   7. Hover mouse cursor over `doAnswer` and click on `Import static method org.mockito.doAnswer`. This will automatically generate corresponding import statement.
   8. Perform successive mouse cursor hovers over `GetSecretBundleByNameRequest`, `GetSecretBundleByNameResponse`, `SecretBundle` and `Base64SecretBundleContentDetails` and click on `Import class`. This will automatically generate corresponding import statements.
   9. If having difficulties with prior steps for automatic import resolution, manually add these import statements instead (*Note: As much as possible, arrange the imports in alphabetical order and group them based on package name*):
      ```java
      import com.oracle.bmc.secrets.model.SecretBundle;
      import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;
      import com.oracle.bmc.secrets.responses.GetSecretBundleByNameResponse;
   
      import static method org.mockito.Mockito.doAnswer;
      ```
9. Add a junit test method that would simulate retrieving username and password secrets. The 
   1. Copy below code snippet and place it after `testCreateSecret()` method:
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
   2. Run the test by doing either of the following:
       1. Right-click on `MockitoMockTest` found on the `Project` window pane and choosing `Run 'MockitoMockTest'` from the pull down menu.
       2. Click on the green arrow head on the left side of the `class MockitoMockTest` declaration and choose `Run 'MockitoMockTest'`
   3. The test will begin execution and all test results should be successful once completed.
10. Implement a negative junit test method that would simulate a failure scenario where a non-existent secret is being retrieved. The test will only succeed if the call to `secretsResource.getSecret("Unknown")` throws an exception indicating that the secret was not found.
    1. Copy below code snippet and place it after `testGetUsernameAndPassword()` method:
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
    2. Run the test by doing either of the following:
        1. Right-click on `MockitoMockTest` found on the `Project` window pane and choosing `Run 'MockitoMockTest'` from the pull down menu.
        2. Click on the green arrow head on the left side of the `class MockitoMockTest` declaration and choose `Run 'MockitoMockTest'`
    3. The test will begin execution and all test results should be successful once completed.





