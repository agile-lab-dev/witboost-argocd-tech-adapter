package it.agilelab.witboost.provisioning.argocd.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "git")
public class GitConfiguration {
    private String username;
    private String token;
}
