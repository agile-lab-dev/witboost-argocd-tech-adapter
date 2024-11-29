package it.agilelab.witboost.provisioning.argocd.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "argocd")
public class ArgoCdConfiguration {
    private String token;
    private String basePath;
}
