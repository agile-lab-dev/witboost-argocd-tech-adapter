package it.agilelab.witboost.provisioning.argocd.configuration;

import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import it.agilelab.witboost.provisioning.argocd.ArgocdProvisionService;
import it.agilelab.witboost.provisioning.argocd.ArgocdValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class DemoConfiguration {
    @Bean
    @Primary
    public ProvisionConfiguration provisionConfiguration() {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(new ArgocdProvisionService())
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
