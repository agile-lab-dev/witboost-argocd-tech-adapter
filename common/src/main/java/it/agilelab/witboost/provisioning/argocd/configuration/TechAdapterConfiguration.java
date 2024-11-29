package it.agilelab.witboost.provisioning.argocd.configuration;

import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import it.agilelab.witboost.provisioning.argocd.client.ApiClientProvider;
import it.agilelab.witboost.provisioning.argocd.service.ArgocdProvisionService;
import it.agilelab.witboost.provisioning.argocd.service.ArgocdValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class TechAdapterConfiguration {

    @Autowired
    private ArgoCdConfiguration argoCdConfiguration;

    @Bean
    public ApiClientProvider apiClientProvider() {
        return new ApiClientProvider(argoCdConfiguration);
    }

    @Bean
    @Primary
    public ProvisionConfiguration provisionConfiguration(ApiClientProvider apiClientProvider) {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(new ArgocdProvisionService(apiClientProvider))
                .build();
    }

    @Bean
    @Primary
    public ValidationConfiguration validationConfiguration() {
        return ValidationConfiguration.builder()
                .workloadValidationService(new ArgocdValidationService())
                .build();
    }
}
