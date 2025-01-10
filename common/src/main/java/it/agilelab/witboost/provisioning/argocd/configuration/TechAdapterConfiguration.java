package it.agilelab.witboost.provisioning.argocd.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import it.agilelab.witboost.provisioning.argocd.client.ApplicationManager;
import it.agilelab.witboost.provisioning.argocd.client.ProjectManager;
import it.agilelab.witboost.provisioning.argocd.client.RepoManager;
import it.agilelab.witboost.provisioning.argocd.service.ArgocdProvisionService;
import it.agilelab.witboost.provisioning.argocd.service.ArgocdValidationService;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ApplicationServiceApi;
import org.openapitools.client.api.ProjectServiceApi;
import org.openapitools.client.api.RepositoryServiceApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({ArgoCdConfiguration.class, GitConfiguration.class})
class TechAdapterConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        return builder.messageConverters(customJacksonMessageConverter(objectMapper))
                .build();
    }

    private MappingJackson2HttpMessageConverter customJacksonMessageConverter(ObjectMapper objectMapper) {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public ApiClient apiClient(RestTemplate restTemplate, ArgoCdConfiguration argoCdConfiguration) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.addDefaultHeader("Authorization", "Bearer " + argoCdConfiguration.getToken());
        apiClient.addDefaultHeader("Content-Type", "application/json");
        apiClient.setBasePath(argoCdConfiguration.getBasePath());
        return apiClient;
    }

    @Bean
    public ApplicationServiceApi applicationServiceApi(ApiClient apiClient) {
        return new ApplicationServiceApi(apiClient);
    }

    @Bean
    public RepositoryServiceApi repositoryServiceApi(ApiClient apiClient) {
        return new RepositoryServiceApi(apiClient);
    }

    @Bean
    public ProjectServiceApi projectServiceApi(ApiClient apiClient) {
        return new ProjectServiceApi(apiClient);
    }

    @Bean
    public RepoManager repoManager(RepositoryServiceApi repositoryServiceApi, GitConfiguration gitConfiguration) {
        return new RepoManager(repositoryServiceApi, gitConfiguration);
    }

    @Bean
    public ApplicationManager applicationManager(ApplicationServiceApi applicationServiceApi) {
        return new ApplicationManager(applicationServiceApi);
    }

    @Bean
    public ProjectManager projectManager(ProjectServiceApi projectServiceApi) {
        return new ProjectManager(projectServiceApi);
    }

    @Bean
    @Primary
    public ArgocdProvisionService argocdProvisionService(
            RepoManager repoManager, ApplicationManager applicationManager, ProjectManager projectManager) {
        return new ArgocdProvisionService(repoManager, applicationManager, projectManager);
    }

    @Bean
    @Primary
    public ProvisionConfiguration provisionConfiguration(ArgocdProvisionService argocdProvisionService) {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(argocdProvisionService)
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
