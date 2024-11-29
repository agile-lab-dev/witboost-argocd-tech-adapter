package it.agilelab.witboost.provisioning.argocd.client;

import it.agilelab.witboost.provisioning.argocd.configuration.ArgoCdConfiguration;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ApplicationServiceApi;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiClientProvider {

    private final ArgoCdConfiguration argoCdConfiguration;

    /**
     * Constructor for injecting the ArgoCD configuration.
     *
     * @param argoCdConfiguration Configuration containing token and base path for ArgoCD.
     */
    public ApiClientProvider(ArgoCdConfiguration argoCdConfiguration) {
        this.argoCdConfiguration = argoCdConfiguration;
    }

    /**
     * Creates a configured {@link ApplicationServiceApi} instance.
     *
     * @return A ready-to-use {@link ApplicationServiceApi}.
     */
    public ApplicationServiceApi createApiClient() {
        ApiClient apiClient = new ApiClient(new RestTemplate());

        // Set headers and base path
        apiClient.addDefaultHeader("Authorization", "Bearer " + argoCdConfiguration.getToken());
        apiClient.addDefaultHeader("Content-Type", "application/json");
        apiClient.setBasePath(argoCdConfiguration.getBasePath());

        return new ApplicationServiceApi(apiClient);
    }
}
