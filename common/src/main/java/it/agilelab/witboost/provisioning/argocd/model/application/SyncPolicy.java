package it.agilelab.witboost.provisioning.argocd.model.application;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SyncPolicy {
    AutomatedSyncPolicy automated;

    public SyncPolicy(AutomatedSyncPolicy automatedSyncPolicy) {
        this.automated = automatedSyncPolicy;
    }
}
