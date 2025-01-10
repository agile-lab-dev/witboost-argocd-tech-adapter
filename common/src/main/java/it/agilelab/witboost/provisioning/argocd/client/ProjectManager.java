package it.agilelab.witboost.provisioning.argocd.client;

import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.model.application.Destination;
import it.agilelab.witboost.provisioning.argocd.model.application.Source;
import java.util.Collections;
import org.openapitools.client.api.ProjectServiceApi;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 * A manager class for handling projects in ArgoCD.
 * <p>
 * This class interacts with the {@link ProjectServiceApi} to perform CRUD operations on ArgoCD projects.
 * It ensures that project specifications, such as source repositories and destination servers, are updated
 * or created as required.
 * </p>
 */
public class ProjectManager {

    private final Logger logger = LoggerFactory.getLogger(ProjectManager.class);
    private final ProjectServiceApi projectServiceApi;

    /**
     * Constructs a {@code ProjectManager} with the specified {@link ProjectServiceApi}.
     *
     * @param projectServiceApi the API client for interacting with ArgoCD projects.
     */
    public ProjectManager(ProjectServiceApi projectServiceApi) {
        this.projectServiceApi = projectServiceApi;
    }

    /**
     * Creates or updates an ArgoCD project based on the provided name, destination, and source details.
     * <p>
     * If the project already exists, its specification is updated. If it does not exist,
     * a new project is created with the given details.
     * </p>
     *
     * @param name the name of the ArgoCD project to create or update.
     * @param destination the destination details (server and namespace) to add or update in the project.
     * @param source the source repository details (URL) to add or update in the project.
     * @return An {@link Either} containing the created or updated {@link V1alpha1AppProject} on success,
     * or a {@link FailedOperation} on failure.
     */
    public Either<FailedOperation, V1alpha1AppProject> createOrUpdateProject(
            String name, Destination destination, Source source) {

        try {
            V1alpha1AppProject project = projectServiceApi.projectServiceGet(name);
            V1alpha1AppProject updatedProject = updateProjectSpec(project, destination, source);

            projectServiceApi.projectServiceUpdate(name, new ProjectProjectUpdateRequest().project(updatedProject));

            logger.info(String.format("Project %s updated successfully", name));
            return Either.right(updatedProject);

        } catch (HttpClientErrorException.NotFound e) {
            return createProject(name, destination, source);
        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while creating (or updating) the project %s. Details: %s",
                    name, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error, e))));
        }
    }

    /**
     * Updates the specification of an existing ArgoCD project.
     * <p>
     * Adds source repositories and destination servers to the project specification
     * if they are not already present.
     * </p>
     *
     * @param project the existing ArgoCD project to update.
     * @param destination the destination details (server and namespace) to add to the project.
     * @param source the source repository details (URL) to add to the project.
     * @return The updated {@link V1alpha1AppProject}.
     */
    protected V1alpha1AppProject updateProjectSpec(V1alpha1AppProject project, Destination destination, Source source) {

        V1alpha1AppProjectSpec spec = project.getSpec();

        boolean destinationExists = spec.getDestinations().stream()
                .anyMatch(dest -> dest.getServer().equalsIgnoreCase(destination.getServer())
                        && ((dest.getNamespace() == null && destination.getNamespace() == null)
                                || (dest.getNamespace() != null
                                        && destination.getNamespace() != null
                                        && dest.getNamespace().equalsIgnoreCase(destination.getNamespace()))));

        if (!destinationExists) {
            spec.addDestinationsItem(new V1alpha1ApplicationDestination()
                    .server(destination.getServer())
                    .namespace(destination.getNamespace()));
        }

        if (!spec.getSourceRepos().contains(source.getRepoURL())) {
            spec.addSourceReposItem(source.getRepoURL());
        }

        return project.spec(spec).status(project.getStatus()).metadata(project.getMetadata());
    }

    /**
     * Creates a new ArgoCD project based on the provided name, destination, and source details.
     *
     * @param name the name of the ArgoCD project to create.
     * @param destination the destination details (server and namespace) to include in the project.
     * @param source the source repository details (URL) to include in the project.
     * @return An {@link Either} containing the created {@link V1alpha1AppProject} on success,
     * or a {@link FailedOperation} on failure.
     */
    protected Either<FailedOperation, V1alpha1AppProject> createProject(
            String name, Destination destination, Source source) {

        try {
            V1alpha1AppProject project = projectServiceApi.projectServiceCreate(new ProjectProjectCreateRequest()
                    .project(new V1alpha1AppProject()
                            .metadata(new V1ObjectMeta().name(name))
                            .spec(new V1alpha1AppProjectSpec()
                                    .addSourceReposItem(source.getRepoURL())
                                    .addDestinationsItem(new V1alpha1ApplicationDestination()
                                            .server(destination.getServer())
                                            .namespace(destination.getNamespace())))));

            logger.info(String.format("Project %s created successfully.", name));
            return Either.right(project);
        } catch (Exception e) {
            String error = String.format("Failed to create the project %s. Details: %s", name, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error, e))));
        }
    }
}
