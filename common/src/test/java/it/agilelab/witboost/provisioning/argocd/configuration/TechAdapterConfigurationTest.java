package it.agilelab.witboost.provisioning.argocd.configuration;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import it.agilelab.witboost.provisioning.argocd.client.ApiClientProvider;
import it.agilelab.witboost.provisioning.argocd.service.ArgocdProvisionService;
import it.agilelab.witboost.provisioning.argocd.service.ArgocdValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TechAdapterConfigurationTest {

    @Autowired
    private ApiClientProvider apiClientProvider;

    @Autowired
    private ProvisionConfiguration provisionConfiguration;

    @Autowired
    private ValidationConfiguration validationConfiguration;

    @Test
    void testApiClientProvider() {
        // Verify that the ApiClientProvider bean is correctly created
        assertNotNull(apiClientProvider, "ApiClientProvider should not be null");
    }

    @Test
    void testProvisionConfiguration() {
        // Verify that the ProvisionConfiguration bean is correctly created
        assertNotNull(provisionConfiguration, "ProvisionConfiguration should not be null");

        // Verify that the workloadProvisionService is an instance of ArgocdProvisionService
        assertTrue(
                provisionConfiguration.getWorkloadProvisionService() instanceof ArgocdProvisionService,
                "The workloadProvisionService should be an instance of ArgocdProvisionService");
    }

    @Test
    void testValidationConfiguration() {
        // Verify that the ValidationConfiguration bean is correctly created
        assertNotNull(validationConfiguration, "ValidationConfiguration should not be null");

        // Verify that the workloadValidationService is an instance of ArgocdValidationService
        assertTrue(
                validationConfiguration.getWorkloadValidationService() instanceof ArgocdValidationService,
                "The workloadValidationService should be an instance of ArgocdValidationService");
    }
}
