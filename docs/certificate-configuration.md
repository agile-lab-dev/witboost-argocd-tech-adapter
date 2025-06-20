## Configuring the JVM to Accept Self-Signed Certificates

This document explains how to configure the Tech Adapter's Java Virtual Machine (JVM) to accept self-signed certificates required for communication with ArgoCD or similar services.

---

### **Steps**

#### 1. Retrieve the Certificate
Extract the certificate from the server running the following `openssl` command:

```bash
openssl s_client -connect <ARGOCD_BASE_PATH> -showcerts
```

- Copy the certificate section starting with `-----BEGIN CERTIFICATE-----` and ending with `-----END CERTIFICATE-----` (including these delimiters).
- Save the certificate to a file, e.g., `server.crt`.


#### 2. Import the Certificate into the TrustStore

Use the keytool command to import the server certificate into a Java TrustStore:
```bash
keytool -importcert \
  -file </path/to/server.crt> \
  -keystore </path/to/mytruststore.jks> \
  -storepass <mypassword> \
  -alias <myservercert> \
  -noprompt
```

Options Explained:

- file: Path to the certificate file (e.g., server.crt).
- keystore: Path where the TrustStore will be created or updated (e.g., mytruststore.jks).
- storepass: Password to protect the TrustStore.
- alias: Unique name to identify the certificate.
- noprompt: Avoid confirmation prompts during import.


#### 3. Setup relative environment variables

Save TrustStore path and password in environment variables respectively named `ARGOCD_TRUSTSTORE_PATH` and `ARGOCD_TRUSTSTORE_PASSWORD`.
These variables are used by the Tech Adapter's Java Virtual Machine (JVM), and you can check their usage in [pom.xml (common)](../common/pom.xml).
