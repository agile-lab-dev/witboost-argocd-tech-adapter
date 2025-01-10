package it.agilelab.witboost.provisioning.argocd.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.client.ApplicationManager;
import it.agilelab.witboost.provisioning.argocd.client.ProjectManager;
import it.agilelab.witboost.provisioning.argocd.client.RepoManager;
import it.agilelab.witboost.provisioning.argocd.model.application.ArgoCDApplicationSpecific;
import it.agilelab.witboost.provisioning.argocd.model.application.Destination;
import it.agilelab.witboost.provisioning.argocd.model.application.Source;
import it.agilelab.witboost.provisioning.argocd.model.application.SyncPolicy;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ArgocdProvisionServiceTest {

    @Mock
    private RepoManager repoManager;

    @Mock
    private ApplicationManager applicationManager;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private ProvisionOperationRequest<?, ? extends Specific> operationRequest;

    @Mock
    private ArgoCDApplicationSpecific appSpecific;

    private ArgocdProvisionService argocdProvisionService;
    private Workload workload;

    @BeforeEach
    public void setUp() {
        argocdProvisionService = new ArgocdProvisionService(repoManager, applicationManager, projectManager);

        workload = new Workload<>();
        workload.setName("workloadName");
        ArgoCDApplicationSpecific applicationSpecific = new ArgoCDApplicationSpecific();
        applicationSpecific.setName("application");
        applicationSpecific.setProject("project");
        Destination destination = new Destination();
        destination.setNamespace("namespace");
        destination.setServer("https://kubernetes.default.svc");
        applicationSpecific.setDestination(destination);
        Source source = new Source();
        source.setPath("folderPath");
        source.setRepoURL("https://gitlab.com/this.is.a.test/argocd.demo.git");
        source.setTargetRevision("HEAD");
        applicationSpecific.setSource(source);
        SyncPolicy syncPolicy = new SyncPolicy();
        syncPolicy.setAutomated(null);
        applicationSpecific.setSyncPolicy(syncPolicy);
        workload.setSpecific(applicationSpecific);
    }

    @Test
    public void testProvisionSuccess() {

        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(projectManager.createOrUpdateProject(any(), any(), any())).thenReturn(Either.right(null));
        when(repoManager.createOrUpdateRepository(anyString(), anyString())).thenReturn(Either.right(null));
        V1alpha1Application newApplication = new V1alpha1Application()
                .metadata(new V1ObjectMeta().name("application"))
                .spec(new V1alpha1ApplicationSpec()
                        .project("project")
                        .source(new V1alpha1ApplicationSource().repoURL("https://fake-url.com")))
                .status(new V1alpha1ApplicationStatus()
                        .health(new V1alpha1HealthStatus().status("Healthy"))
                        .sync(new V1alpha1SyncStatus().status("Synced")));

        when(applicationManager.createOrUpdateApplication(any())).thenReturn(Either.right(newApplication));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(operationRequest);

        assertTrue(result.isRight());
        assertNotNull(result.get());
    }

    @Test
    public void testProvisionSuccess_NullStatus() {

        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(projectManager.createOrUpdateProject(any(), any(), any())).thenReturn(Either.right(null));
        when(repoManager.createOrUpdateRepository(anyString(), anyString())).thenReturn(Either.right(null));
        V1alpha1Application newApplication = new V1alpha1Application()
                .metadata(new V1ObjectMeta().name("application"))
                .spec(new V1alpha1ApplicationSpec()
                        .project("project")
                        .source(new V1alpha1ApplicationSource().repoURL("https://fake-url.com")))
                .status(new V1alpha1ApplicationStatus()
                        .health(new V1alpha1HealthStatus().status(null))
                        .sync(new V1alpha1SyncStatus().status(null)));

        when(applicationManager.createOrUpdateApplication(any())).thenReturn(Either.right(newApplication));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(operationRequest);

        assertTrue(result.isRight());
        assertNotNull(result.get());
    }

    @Test
    public void testProvisionFailure_ProjectError() {
        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(projectManager.createOrUpdateProject(any(), any(), any()))
                .thenReturn(Either.left(new FailedOperation(
                        "Project creation failed", Collections.singletonList(new Problem("Project error")))));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(operationRequest);

        assertTrue(result.isLeft());
        assertEquals("Project creation failed", result.getLeft().message());
    }

    @Test
    public void testProvisionFailure_RepositoryError() {
        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(projectManager.createOrUpdateProject(any(), any(), any())).thenReturn(Either.right(null));
        when(repoManager.createOrUpdateRepository(anyString(), anyString()))
                .thenReturn(Either.left(new FailedOperation(
                        "Repo creation failed", Collections.singletonList(new Problem("Repo error")))));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(operationRequest);

        assertTrue(result.isLeft());
        assertEquals("Repo creation failed", result.getLeft().message());
    }

    @Test
    public void testProvisionFailure_ApplicationError() {

        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(projectManager.createOrUpdateProject(any(), any(), any())).thenReturn(Either.right(null));
        when(repoManager.createOrUpdateRepository(anyString(), anyString())).thenReturn(Either.right(null));

        when(applicationManager.createOrUpdateApplication(any()))
                .thenReturn(Either.left(new FailedOperation(
                        "Application creation failed", Collections.singletonList(new Problem("Application error")))));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(operationRequest);

        assertTrue(result.isLeft());
        assertEquals("Application creation failed", result.getLeft().message());
    }

    @Test
    public void testUnprovisionSuccess() {
        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(applicationManager.deleteApplication(anyString(), anyString())).thenReturn(Either.right(null));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.unprovision(operationRequest);

        assertTrue(result.isRight());
        assertNotNull(result.get());

        Map resultInfo = (Map) result.get().getPrivateInfo().get();
        assertTrue(resultInfo
                .get("result")
                .toString()
                .contains("Application: application successfully deleted. Project: project"));
    }

    @Test
    public void testUnprovisionFailure() {
        when(operationRequest.getComponent()).thenReturn(Optional.of(workload));
        when(applicationManager.deleteApplication(anyString(), anyString()))
                .thenReturn(Either.left(new FailedOperation(
                        "Deletion failed", Collections.singletonList(new Problem("Deletion error")))));

        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.unprovision(operationRequest);

        assertTrue(result.isLeft());
        assertEquals("Deletion failed", result.getLeft().message());
    }
}
