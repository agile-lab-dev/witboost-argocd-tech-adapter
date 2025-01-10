package it.agilelab.witboost.provisioning.argocd.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.model.common.FailedOperation;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.configuration.GitConfiguration;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.client.api.RepositoryServiceApi;
import org.openapitools.client.model.V1alpha1ConnectionState;
import org.openapitools.client.model.V1alpha1Repository;
import org.openapitools.client.model.V1alpha1RepositoryList;

class RepoManagerTest {

    private RepositoryServiceApi repositoryServiceApi;
    private GitConfiguration gitConfiguration;
    private RepoManager repoManager;

    @BeforeEach
    void setUp() {
        repositoryServiceApi = Mockito.mock(RepositoryServiceApi.class);
        gitConfiguration = Mockito.mock(GitConfiguration.class);
        repoManager = new RepoManager(repositoryServiceApi, gitConfiguration);

        when(gitConfiguration.getToken()).thenReturn("mockToken");
        when(gitConfiguration.getUsername()).thenReturn("mockUser");
    }

    @Test
    void createOrUpdateRepository_Success_WhenApiCallSucceeds() {
        String repoURL = "https://example.com/repo.git";
        String project = "ValidProject";

        V1alpha1Repository repository = new V1alpha1Repository().repo(repoURL).type("git");
        repository.setConnectionState(
                new V1alpha1ConnectionState().status("Successful").message("TestMessage"));

        V1alpha1RepositoryList repositoryList =
                new V1alpha1RepositoryList().items(Collections.singletonList(repository));

        when(repositoryServiceApi.repositoryServiceListRepositories(null, true, null))
                .thenReturn(repositoryList);

        Either<FailedOperation, V1alpha1Repository> result = repoManager.createOrUpdateRepository(repoURL, project);

        assertTrue(result.isRight());
        assertEquals(repoURL, result.get().getRepo());
    }

    @Test
    void createOrUpdateRepository_Error_WhenApiCallFails() {
        String repoURL = "https://example.com/repo.git";
        String project = "ValidProject";

        doThrow(new RuntimeException("API error"))
                .when(repositoryServiceApi)
                .repositoryServiceCreateRepositoryWithHttpInfo(any(), eq(true), eq(false));

        Either<FailedOperation, V1alpha1Repository> result = repoManager.createOrUpdateRepository(repoURL, project);

        assertTrue(result.isLeft());
        assertNotNull(result.getLeft());
        assertEquals(
                "An unexpected error occurred while creating the repository https://example.com/repo.git. Details: API error",
                result.getLeft().message());
    }

    @Test
    void createOrUpdateRepository_Error_WhenRepositoryNotFound() {
        String repoURL = "https://example.com/repo.git";
        String project = "ValidProject";

        V1alpha1RepositoryList emptyList = new V1alpha1RepositoryList().items(Collections.emptyList());

        when(repositoryServiceApi.repositoryServiceListRepositories(null, true, null))
                .thenReturn(emptyList);

        Either<FailedOperation, V1alpha1Repository> result = repoManager.createOrUpdateRepository(repoURL, project);

        assertTrue(result.isLeft());
        assertNotNull(result.getLeft());
        assertEquals(
                "Repository https://example.com/repo.git not found after creation or update. This might indicate an internal error.",
                result.getLeft().message());
    }

    @Test
    void createOrUpdateRepository_Error_WhenConnectionStateFails() {
        String repoURL = "https://example.com/repo.git";
        String project = "ValidProject";

        V1alpha1Repository repository = new V1alpha1Repository().repo(repoURL).type("git");
        repository.setConnectionState(
                new V1alpha1ConnectionState().status("Failed").message("Error message"));

        V1alpha1RepositoryList repositoryList =
                new V1alpha1RepositoryList().items(Collections.singletonList(repository));

        when(repositoryServiceApi.repositoryServiceListRepositories(null, true, null))
                .thenReturn(repositoryList);

        Either<FailedOperation, V1alpha1Repository> result = repoManager.createOrUpdateRepository(repoURL, project);

        assertTrue(result.isLeft());
        assertNotNull(result.getLeft());
        assertEquals(
                "Failed to create or update the repository https://example.com/repo.git. Details: [Connection status: Failed. Message: Error message]",
                result.getLeft().message());
    }
}
