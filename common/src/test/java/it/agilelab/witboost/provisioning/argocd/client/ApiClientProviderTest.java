package it.agilelab.witboost.provisioning.argocd.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openapitools.client.api.ApplicationServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiClientProviderTest {

    @Autowired
    private ApiClientProvider apiClientProvider;

    @Test
    void testCreateApiClient() {
        ApplicationServiceApi apiClient = apiClientProvider.createApiClient();

        assertNotNull(apiClient);
        assertNotNull(apiClient.getApiClient());
        assertEquals("https://fake-url.com", apiClient.getApiClient().getBasePath());
    }
}
