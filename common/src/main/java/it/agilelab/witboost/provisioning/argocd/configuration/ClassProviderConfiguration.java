package it.agilelab.witboost.provisioning.argocd.configuration;

import com.witboost.provisioning.framework.service.ComponentClassProvider;
import com.witboost.provisioning.framework.service.SpecificClassProvider;
import com.witboost.provisioning.framework.service.impl.ComponentClassProviderImpl;
import com.witboost.provisioning.framework.service.impl.SpecificClassProviderImpl;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class ClassProviderConfiguration {
    @Bean
    @Primary
    public ComponentClassProvider componentClassProvider() {
        return ComponentClassProviderImpl.builder()
                .withDefaultClass(Workload.class)
                .build();
    }

    @Bean
    @Primary
    public SpecificClassProvider specificClassProvider() {
        return SpecificClassProviderImpl.builder()
                .withDefaultSpecificClass(Specific.class)
                .build();
    }
}
