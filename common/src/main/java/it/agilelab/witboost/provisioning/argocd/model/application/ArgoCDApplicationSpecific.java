package it.agilelab.witboost.provisioning.argocd.model.application;

import com.witboost.provisioning.model.Specific;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArgoCDApplicationSpecific extends Specific {
    @NotNull
    String name;

    @NotNull
    String project;

    @Valid
    @NotNull
    Destination destination;

    @Valid
    @NotNull
    Source source;

    @Valid
    @NotNull
    SyncPolicy syncPolicy;
}
