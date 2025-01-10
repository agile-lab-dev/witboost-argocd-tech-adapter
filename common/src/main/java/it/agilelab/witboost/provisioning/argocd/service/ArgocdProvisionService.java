package it.agilelab.witboost.provisioning.argocd.service;

import com.witboost.provisioning.framework.service.ProvisionService;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.client.ApplicationManager;
import it.agilelab.witboost.provisioning.argocd.client.ProjectManager;
import it.agilelab.witboost.provisioning.argocd.client.RepoManager;
import it.agilelab.witboost.provisioning.argocd.model.application.ArgoCDApplicationSpecific;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ArgocdProvisionService implements ProvisionService {

    private final Logger logger = LoggerFactory.getLogger(ArgocdProvisionService.class);
    private final RepoManager repoManager;
    private final ApplicationManager applicationManager;
    private final ProjectManager projectManager;

    public ArgocdProvisionService(
            RepoManager repoManager, ApplicationManager applicationManager, ProjectManager projectManager) {
        this.repoManager = repoManager;
        this.applicationManager = applicationManager;
        this.projectManager = projectManager;
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> provision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        ArgoCDApplicationSpecific appSpecific = getArgoCDSpecific(operationRequest);

        var project = projectManager.createOrUpdateProject(
                appSpecific.getProject(), appSpecific.getDestination(), appSpecific.getSource());
        if (project.isLeft()) return Either.left(project.getLeft());

        var repo = repoManager.createOrUpdateRepository(appSpecific.getSource().getRepoURL(), appSpecific.getProject());
        if (repo.isLeft()) return Either.left(repo.getLeft());

        var application = applicationManager.createOrUpdateApplication(appSpecific);
        if (application.isLeft()) return Either.left(application.getLeft());

        var info = Map.of(
                "name",
                Map.of(
                        "type", "string",
                        "label", "Application name",
                        "value", application.get().getMetadata().getName()),
                "project",
                Map.of(
                        "type", "string",
                        "label", "Application project",
                        "value", application.get().getSpec().getProject()),
                "repository",
                Map.of(
                        "type", "string",
                        "label", "Application repository",
                        "value", application.get().getSpec().getSource().getRepoURL()),
                "health status",
                Map.of(
                        "type",
                        "string",
                        "label",
                        "Application Health Status",
                        "value",
                        application.get().getStatus() != null
                                        && application.get().getStatus().getHealth() != null
                                        && application
                                                        .get()
                                                        .getStatus()
                                                        .getHealth()
                                                        .getStatus()
                                                != null
                                ? application.get().getStatus().getHealth().getStatus()
                                : "unknown"),
                "sync status",
                Map.of(
                        "type",
                        "string",
                        "label",
                        "Application Sync Status",
                        "value",
                        application.get().getStatus() != null
                                        && application.get().getStatus().getHealth() != null
                                        && application
                                                        .get()
                                                        .getStatus()
                                                        .getSync()
                                                        .getStatus()
                                                != null
                                ? application.get().getStatus().getSync().getStatus()
                                : "unknown"));

        ProvisionInfo provisionInfo = ProvisionInfo.builder()
                .privateInfo(Optional.of(info))
                .publicInfo(Optional.of(info))
                .build();

        logger.info(String.format(
                "Provisioning of %s completed successfully",
                operationRequest.getComponent().get().getName()));
        return Either.right(provisionInfo);
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> unprovision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        var appSpecific = getArgoCDSpecific(operationRequest);

        var deletedApp = applicationManager.deleteApplication(appSpecific.getName(), appSpecific.getProject());
        if (deletedApp.isLeft()) return Either.left(deletedApp.getLeft());

        var info = Map.of(
                "result",
                Map.of(
                        "type", "string",
                        "label", "Operation result",
                        "value",
                                String.format(
                                        "Application: %s successfully deleted. Project: %s",
                                        appSpecific.getName(), appSpecific.getProject())));

        ProvisionInfo provisionInfo = ProvisionInfo.builder()
                .privateInfo(Optional.of(info))
                .publicInfo(Optional.of(info))
                .build();

        logger.info(String.format(
                "Unprovisioning of %s completed successfully",
                operationRequest.getComponent().get().getName()));

        return Either.right(provisionInfo);
    }

    private ArgoCDApplicationSpecific getArgoCDSpecific(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        var componentSpecific = operationRequest.getComponent().get().getSpecific();

        return (ArgoCDApplicationSpecific) componentSpecific;
    }
}
