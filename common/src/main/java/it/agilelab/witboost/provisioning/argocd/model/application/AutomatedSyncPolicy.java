package it.agilelab.witboost.provisioning.argocd.model.application;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutomatedSyncPolicy {
    @NotNull
    Boolean prune;

    @NotNull
    Boolean selfHeal;

    public AutomatedSyncPolicy(Boolean prune, Boolean selfHeal) {
        this.prune = prune;
        this.selfHeal = selfHeal;
    }
}
