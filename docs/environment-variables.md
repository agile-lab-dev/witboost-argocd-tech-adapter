# Environment Variables

The following environment variables are required to configure and run the ArgoCD Tech Adapter. Ensure these variables are properly set in your environment or configuration management tool.

## ArgoCD Configuration

| **Variable**             | **Description**                                                                 | **Example**                     |
|---------------------------|---------------------------------------------------------------------------------|---------------------------------|
| `ARGOCD_BASE_PATH`        | The base URL for accessing the ArgoCD API.                                      | `https://argo.example.com:8080` |
| `ARGOCD_TOKEN`            | The authentication token used to access the ArgoCD API.                        | `eyJhbGciOiJIUzI1NiIs...`       |
| `ARGOCD_TRUSTSTORE_PATH`  | The file path to the Java Keystore (JKS) containing trusted certificates.       | `/path/to/mytruststore.jks`       |
| `ARGOCD_TRUSTSTORE_PASSWORD` | The password for the Java Keystore (JKS) file used to manage trusted certificates. | `mypassword`                    |


## Git Configuration

| **Variable**     | **Description**                                                             | **Example**                  |
|-------------------|-----------------------------------------------------------------------------|------------------------------|
| `GIT_TOKEN`       | The access token used for authenticating with private Git repositories.    | `ghp_xxx...`                |
| `GIT_USERNAME`    | The username associated with the Git access token.                         | `git-user`                  |

