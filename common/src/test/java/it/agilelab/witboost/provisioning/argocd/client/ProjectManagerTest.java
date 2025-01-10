package it.agilelab.witboost.provisioning.argocd.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.model.common.FailedOperation;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.model.application.ArgoCDApplicationSpecific;
import it.agilelab.witboost.provisioning.argocd.model.application.Destination;
import it.agilelab.witboost.provisioning.argocd.model.application.Source;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.api.ProjectServiceApi;
import org.openapitools.client.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
public class ProjectManagerTest {

    @Mock
    private ProjectServiceApi projectServiceApi;

    @InjectMocks
    private ProjectManager projectManager;

    private ArgoCDApplicationSpecific applicationSpecific;

    @BeforeEach
    public void setUp() {
        applicationSpecific = new ArgoCDApplicationSpecific();
        applicationSpecific.setProject("test-project");

        Destination destination = new Destination();
        destination.setServer("https://kubernetes.default.svc");
        destination.setNamespace("default");
        applicationSpecific.setDestination(destination);

        Source source = new Source();
        source.setRepoURL("https://github.com/test-repo.git");
        applicationSpecific.setSource(source);
    }

    @Test
    public void testCreateProject_Success() throws Exception {
        V1alpha1AppProject newProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addSourceReposItem("https://github.com/test-repo.git")
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("default")));
        when(projectServiceApi.projectServiceCreate(any())).thenReturn(newProject);

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals("test-project", result.get().getMetadata().getName());
        verify(projectServiceApi, times(1)).projectServiceCreate(any());
    }

    @Test
    public void testCreateProject_Failure() throws Exception {
        when(projectServiceApi.projectServiceCreate(any())).thenThrow(new RuntimeException("API failure"));

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isLeft());
        assertEquals(
                "Failed to create the project test-project. Details: API failure",
                result.getLeft().message());
        verify(projectServiceApi, times(1)).projectServiceCreate(any());
    }

    @Test
    public void testCreateOrUpdateProject_NamespaceBothNull() throws Exception {
        V1alpha1AppProject existingProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace(null)));
        when(projectServiceApi.projectServiceGet("test-project")).thenReturn(existingProject);
        when(projectServiceApi.projectServiceUpdate(anyString(), any())).thenReturn(existingProject);
        applicationSpecific.getDestination().setNamespace(null);

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals(1, result.get().getSpec().getDestinations().size());
        verify(projectServiceApi, times(1)).projectServiceGet("test-project");
        verify(projectServiceApi, times(1)).projectServiceUpdate(anyString(), any());
    }

    @Test
    public void testCreateOrUpdateProject_OneNamespaceNull() throws Exception {
        V1alpha1AppProject existingProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace(null)));

        when(projectServiceApi.projectServiceGet("test-project")).thenReturn(existingProject);
        applicationSpecific.getDestination().setNamespace("default");
        V1alpha1AppProject updatedProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace(null))
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("default")));

        when(projectServiceApi.projectServiceUpdate(anyString(), any())).thenReturn(updatedProject);

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals(2, result.get().getSpec().getDestinations().size());
        verify(projectServiceApi, times(1)).projectServiceGet("test-project");
        verify(projectServiceApi, times(1)).projectServiceUpdate(anyString(), any());
    }

    @Test
    public void testCreateOrUpdateProject_NamespaceMatch() throws Exception {
        V1alpha1AppProject existingProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("default")));

        when(projectServiceApi.projectServiceGet("test-project")).thenReturn(existingProject);
        when(projectServiceApi.projectServiceUpdate(anyString(), any())).thenReturn(existingProject);
        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals(1, result.get().getSpec().getDestinations().size());
        verify(projectServiceApi, times(1)).projectServiceGet("test-project");
        verify(projectServiceApi, times(1)).projectServiceUpdate(anyString(), any());
    }

    @Test
    public void testCreateOrUpdateProject_NamespaceMismatch() throws Exception {
        V1alpha1AppProject existingProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("another-namespace")));

        when(projectServiceApi.projectServiceGet("test-project")).thenReturn(existingProject);
        V1alpha1AppProject updatedProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("another-namespace"))
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("default")));

        when(projectServiceApi.projectServiceUpdate(anyString(), any())).thenReturn(updatedProject);

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals(2, result.get().getSpec().getDestinations().size());
        verify(projectServiceApi, times(1)).projectServiceGet("test-project");
        verify(projectServiceApi, times(1)).projectServiceUpdate(anyString(), any());
    }

    @Test
    public void testCreateOrUpdateProject_DestinationExists() throws Exception {
        V1alpha1AppProject existingProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec()
                        .addSourceReposItem("https://github.com/test-repo.git")
                        .addDestinationsItem(new V1alpha1ApplicationDestination()
                                .server("https://kubernetes.default.svc")
                                .namespace("default")));

        when(projectServiceApi.projectServiceGet(anyString())).thenReturn(existingProject);

        when(projectServiceApi.projectServiceUpdate(anyString(), any())).thenReturn(existingProject);

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals(1, result.get().getSpec().getDestinations().size());
        verify(projectServiceApi, times(1)).projectServiceGet("test-project");
        verify(projectServiceApi, times(1)).projectServiceUpdate(anyString(), any());
    }

    @Test
    public void testCreateOrUpdateProject_CreateNewProject() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        byte[] body = "Not Found".getBytes(StandardCharsets.UTF_8);
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", headers, body, StandardCharsets.UTF_8);

        when(projectServiceApi.projectServiceGet("test-project")).thenThrow(exception);
        V1alpha1AppProject newProject = new V1alpha1AppProject()
                .metadata(new V1ObjectMeta().name("test-project"))
                .spec(new V1alpha1AppProjectSpec());
        when(projectServiceApi.projectServiceCreate(any())).thenReturn(newProject);

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isRight());
        assertEquals("test-project", result.get().getMetadata().getName());
        verify(projectServiceApi, times(1)).projectServiceCreate(any());
        verify(projectServiceApi, times(1)).projectServiceGet("test-project");
    }

    @Test
    public void testCreateOrUpdateProject_Failure() throws Exception {

        when(projectServiceApi.projectServiceGet("test-project")).thenThrow(new RuntimeException("runtime exception"));

        Either<FailedOperation, V1alpha1AppProject> result = projectManager.createOrUpdateProject(
                "test-project", applicationSpecific.getDestination(), applicationSpecific.getSource());

        assertTrue(result.isLeft());
        assertEquals(
                "An unexpected error occurred while creating (or updating) the project test-project. Details: runtime exception",
                result.getLeft().message());
        verify(projectServiceApi, times(0)).projectServiceCreate(any());
        verify(projectServiceApi, times(0)).projectServiceUpdate(anyString(), any());
    }
}
