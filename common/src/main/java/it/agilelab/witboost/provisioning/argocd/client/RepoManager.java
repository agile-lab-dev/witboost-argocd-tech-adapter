package it.agilelab.witboost.provisioning.argocd.client;

import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.configuration.GitConfiguration;
import java.util.Collections;
import java.util.List;
import org.openapitools.client.api.RepositoryServiceApi;
import org.openapitools.client.model.V1alpha1Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A manager class for handling Git repositories in ArgoCD.
 * <p>
 * This class provides methods for creating or updating repositories in ArgoCD,
 * leveraging the {@link RepositoryServiceApi} and Git configuration provided
 * by {@link GitConfiguration}.
 * </p>
 */
@Service
public class RepoManager {

    private final Logger logger = LoggerFactory.getLogger(RepoManager.class);
    private final RepositoryServiceApi repositoryServiceApi;
    private final GitConfiguration gitConfiguration;

    /**
     * Constructor for injecting the required dependencies.
     *
     * @param repositoryServiceApi API client for interacting with ArgoCD repository services.
     * @param gitConfiguration Configuration containing credentials and settings for Git repositories.
     */
    public RepoManager(RepositoryServiceApi repositoryServiceApi, GitConfiguration gitConfiguration) {
        this.repositoryServiceApi = repositoryServiceApi;
        this.gitConfiguration = gitConfiguration;
    }

    /**
     * Creates or updates a Git repository in ArgoCD.
     * <p>
     * This method sends a request to create or update the specified Git repository,
     * then retrieves the updated list of repositories to verify the operation's success.
     * </p>
     *
     * @param repoURL The URL of the Git repository to create or update.
     * @param project The ArgoCD project in which the repository should exist.
     * @return An {@link Either} containing the created/updated {@link V1alpha1Repository} on success,
     *         or a {@link FailedOperation} describing the failure.
     */
    public Either<FailedOperation, V1alpha1Repository> createOrUpdateRepository(String repoURL, String project) {
        try {

            repositoryServiceApi.repositoryServiceCreateRepositoryWithHttpInfo(
                    new V1alpha1Repository()
                            .type("git")
                            .password(gitConfiguration.getToken())
                            .repo(repoURL)
                            .username(gitConfiguration.getUsername())
                            .project(project),
                    true,
                    false);

            List<V1alpha1Repository> repoList = repositoryServiceApi
                    .repositoryServiceListRepositories(null, true, null)
                    .getItems();

            return repoList.stream()
                    .filter(repo -> repo.getRepo().equalsIgnoreCase(repoURL))
                    .findFirst()
                    .<Either<FailedOperation, V1alpha1Repository>>map(repo -> {
                        if (repo.getConnectionState().getStatus().equalsIgnoreCase("Successful")) {
                            logger.info(
                                    "Repository {} created or updated successfully (project {}).", repoURL, project);
                            return Either.right(repo);
                        } else {
                            String error = String.format(
                                    "Failed to create or update the repository %s. Details: [Connection status: %s. Message: %s]",
                                    repoURL,
                                    repo.getConnectionState().getStatus(),
                                    repo.getConnectionState().getMessage());
                            logger.error(error);
                            return Either.left(new FailedOperation(
                                    error,
                                    Collections.singletonList(new Problem(
                                            repo.getConnectionState().getMessage()))));
                        }
                    })
                    .orElseGet(() -> {
                        String error = String.format(
                                "Repository %s not found after creation or update. This might indicate an internal error.",
                                repoURL);
                        logger.error(error);
                        return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error))));
                    });

        } catch (Exception e) {
            String error = String.format(
                    "An unexpected error occurred while creating the repository %s. Details: %s",
                    repoURL, e.getMessage());
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error, e))));
        }
    }
}
