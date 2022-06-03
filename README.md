# Lab exercise for creating Unit Test against a Helidon MP App that uses OCI Vault SDK Integration

Sample Helidon MP project that includes multiple REST operations performs that creates or retrieves Oci 
Vault secrets data. The project will be used to demonstrate how to create a unit tests.

## Lab Exercises/Practices
Refer to [Practices.md](Practices.md) for the Lab Exercises/Practices complete steps.

## Distribution
1. Create a complete zip distribution of the Helidon MP project which will be used as reference after the practice.
   ```bash
   ./distribution.sh complete
   ```
2. Create a zip distribution of the Helidon MP project without the test which will be used for the practice.
   ```bash
   ./distribution.sh practice
   ```
3. Create both complete and practice zip distributions.
   ```bash
   ./distribution.sh both
   ```
4. Delete all existing complete and practice zip distributions.
   ```bash
   ./distribution.sh clean
   ```   



