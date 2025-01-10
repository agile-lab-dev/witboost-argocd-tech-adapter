package it.agilelab.witboost.provisioning.argocd.client;

import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.model.application.ArgoCDApplicationSpecific;
import java.util.Collections;
import java.util.List;
import org.openapitools.client.api.ApplicationServiceApi;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 * A manager class for handling ArgoCD applications.
 * <p>
 * This class provides methods to create, update, and delete applications in ArgoCD.
 * </p>
 */
public class ApplicationManager {

    private final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);
    private final ApplicationServiceApi applicationServiceApi;

    public ApplicationManager(ApplicationServiceApi applicationServiceApi) {
        this.applicationServiceApi = applicationServiceApi;
    }

    /**
     * Creates or updates an ArgoCD application.
     *
     * @param applicationSpecific The application details to be created or updated.
     * @return An {@link Either} containing the created/updated {@link V1alpha1Application} on success
     *         or a {@link FailedOperation} on failure.
     */
    public Either<FailedOperation, V1alpha1Application> createOrUpdateApplication(
            ArgoCDApplicationSpecific applicationSpecific) {
        try {

            V1alpha1Application applicationDetails = buildApplicationDetails(applicationSpecific);
            V1alpha1Application v1alpha1Application =
                    applicationServiceApi.applicationServiceCreate(applicationDetails, true, true);

            logger.info(
                    "Application {} created or updated successfully (project {}).",
                    applicationSpecific.getName(),
                    applicationSpecific.getProject());
            return Either.right(v1alpha1Application);
        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while creating the application %s. Please try again later. If the issue still persists, contact the platform team for assistance! Details: %s",
                    applicationSpecific.getName(), e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error))));
        }
    }

    /**
     * Deletes an ArgoCD application.
     *
     * @param appName The name of the application to delete.
     * @param project The ArgoCD project where the application is located.
     * @return An {@link Either} containing {@code null} on success or a {@link FailedOperation} on failure.
     */
    public Either<FailedOperation, Void> deleteApplication(String appName, String project) {
        try {

            applicationServiceApi.applicationServiceDelete(appName, false, null, null, project);
            logger.info("Application {} deleted successfully.", appName);
            return Either.right(null);

        } catch (HttpClientErrorException.NotFound e) {
            logger.info("Application {} deleted successfully.", appName);
            return Either.right(null);
        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while deleting the application %s. Please try again later. If the issue still persists, contact the platform team for assistance! Details: %s",
                    appName, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error))));
        }
    }

    /**
     * Gets the status of an ArgoCD application.
     *
     * @param name The name of the application.
     * @param project The ArgoCD project where the application is located.
     * @return An {@link Either} containing {@link V1alpha1ApplicationStatus} on success or a {@link FailedOperation} on failure.
     */
    public Either<FailedOperation, V1alpha1ApplicationStatus> getApplicationStatus(
            String name, String namespace, String project) {
        try {

            V1alpha1Application application = applicationServiceApi.applicationServiceGet(
                    name, null, null, null, null, null, null, List.of(project));
            V1alpha1ApplicationStatus status = application.getStatus();

            logger.info(String.format(
                    "Application: %s. Project: %s. Status: [Health: %s; Sync: %s]",
                    name,
                    project,
                    status.getHealth().getStatus(),
                    status.getSync().getStatus()));
            return Either.right(status);

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while getting the status of %s. Please try again later. If the issue still persists, contact the platform team for assistance! Details: %s",
                    name, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error))));
        }
    }

    /**
     * Builds the application details for creating or updating an ArgoCD application.
     *
     * @param appSpecific The specific application configuration.
     * @return A fully constructed {@link V1alpha1Application}.
     */
    private V1alpha1Application buildApplicationDetails(ArgoCDApplicationSpecific appSpecific) {
        V1alpha1ApplicationSpec spec = new V1alpha1ApplicationSpec()
                .project(appSpecific.getProject())
                .destination(new V1alpha1ApplicationDestination()
                        .server(appSpecific.getDestination().getServer())
                        .namespace(appSpecific.getDestination().getNamespace()))
                .source(new V1alpha1ApplicationSource()
                        .path(appSpecific.getSource().getPath())
                        .repoURL(appSpecific.getSource().getRepoURL())
                        .targetRevision(appSpecific.getSource().getTargetRevision()));

        if (appSpecific.getSyncPolicy().getAutomated() != null) {
            spec.setSyncPolicy(new V1alpha1SyncPolicy()
                    .automated(new V1alpha1SyncPolicyAutomated()
                            .selfHeal(appSpecific.getSyncPolicy().getAutomated().getSelfHeal())
                            .prune(appSpecific.getSyncPolicy().getAutomated().getPrune())));
        }

        return new V1alpha1Application()
                .metadata(new V1ObjectMeta().name(appSpecific.getName()))
                .spec(spec);
    }
}
