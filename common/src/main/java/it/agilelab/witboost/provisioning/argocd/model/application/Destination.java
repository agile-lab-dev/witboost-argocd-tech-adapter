package it.agilelab.witboost.provisioning.argocd.model.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Destination {
    @NotBlank
    String server;

    @NotNull
    String namespace;

    public Destination(@NotBlank String server, @NotNull String namespace) {
        this.server = server;
        this.namespace = namespace;
    }
}
