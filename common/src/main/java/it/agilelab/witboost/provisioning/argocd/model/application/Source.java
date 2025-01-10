package it.agilelab.witboost.provisioning.argocd.model.application;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Source {

    @NotBlank
    String path;

    @NotBlank
    String repoURL;

    @NotBlank
    String targetRevision;

    public Source(@NotBlank String path, @NotBlank String repoURL, @NotBlank String targetRevision) {
        this.path = path;
        this.repoURL = repoURL;
        this.targetRevision = targetRevision;
    }
}
