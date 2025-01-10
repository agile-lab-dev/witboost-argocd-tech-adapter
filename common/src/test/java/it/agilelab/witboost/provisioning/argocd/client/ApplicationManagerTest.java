package it.agilelab.witboost.provisioning.argocd.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.model.common.FailedOperation;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.model.application.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.api.ApplicationServiceApi;
import org.openapitools.client.model.V1alpha1Application;
import org.openapitools.client.model.V1alpha1ApplicationStatus;
import org.openapitools.client.model.V1alpha1HealthStatus;
import org.openapitools.client.model.V1alpha1SyncStatus;
import org.springframework.web.client.HttpClientErrorException;

class ApplicationManagerTest {

    private ApplicationServiceApi applicationServiceApi;
    private ApplicationManager applicationManager;

    @BeforeEach
    void setUp() {
        applicationServiceApi = mock(ApplicationServiceApi.class);
        applicationManager = new ApplicationManager(applicationServiceApi);
    }

    @Test
    void createOrUpdateApplication_Success_WhenApiCallSucceeds_ManualSync() {
        ArgoCDApplicationSpecific specific = createValidSpecific();
        V1alpha1Application expectedResponse = new V1alpha1Application();

        when(applicationServiceApi.applicationServiceCreate(any(), eq(true), eq(true)))
                .thenReturn(expectedResponse);

        Either<FailedOperation, V1alpha1Application> result = applicationManager.createOrUpdateApplication(specific);

        assertTrue(result.isRight());
        assertEquals(expectedResponse, result.get());
        verify(applicationServiceApi).applicationServiceCreate(any(), eq(true), eq(true));
    }

    @Test
    void createOrUpdateApplication_Success_WhenApiCallSucceeds_AutomatedSync() {
        ArgoCDApplicationSpecific specific = createValidSpecific();
        specific.setSyncPolicy(new SyncPolicy(new AutomatedSyncPolicy(false, false)));
        V1alpha1Application expectedResponse = new V1alpha1Application();

        when(applicationServiceApi.applicationServiceCreate(any(), eq(true), eq(true)))
                .thenReturn(expectedResponse);

        Either<FailedOperation, V1alpha1Application> result = applicationManager.createOrUpdateApplication(specific);

        assertTrue(result.isRight());
        assertEquals(expectedResponse, result.get());
        verify(applicationServiceApi).applicationServiceCreate(any(), eq(true), eq(true));
    }

    @Test
    void createOrUpdateApplication_Error_WhenApiCallFails() {
        ArgoCDApplicationSpecific specific = createValidSpecific();
        when(applicationServiceApi.applicationServiceCreate(any(), eq(true), eq(true)))
                .thenThrow(new RuntimeException("API error"));

        Either<FailedOperation, V1alpha1Application> result = applicationManager.createOrUpdateApplication(specific);

        assertTrue(result.isLeft());
        assertNotNull(result.getLeft());
        assertEquals(
                "An unexpected error occurred while creating the application ValidApp. Please try again later. If the issue still persists, contact the platform team for assistance! Details: API error",
                result.getLeft().message());
    }

    @Test
    void deleteApplication_Success_WhenApiCallSucceeds() {
        String appName = "ValidApp";
        String project = "ValidProject";

        Either<FailedOperation, Void> result = applicationManager.deleteApplication(appName, project);

        assertTrue(result.isRight());
        verify(applicationServiceApi).applicationServiceDelete(appName, false, null, null, project);
    }

    @Test
    void deleteApplication_Success_WhenApplicationNotFound() {
        String appName = "NonExistentApp";
        String project = "ValidProject";

        doThrow(HttpClientErrorException.NotFound.class)
                .when(applicationServiceApi)
                .applicationServiceDelete(appName, false, null, null, project);

        Either<FailedOperation, Void> result = applicationManager.deleteApplication(appName, project);

        assertTrue(result.isRight());
    }

    @Test
    void deleteApplication_Error_WhenApiCallFails() {
        String appName = "ValidApp";
        String project = "ValidProject";

        doThrow(new RuntimeException("API error"))
                .when(applicationServiceApi)
                .applicationServiceDelete(appName, false, null, null, project);

        Either<FailedOperation, Void> result = applicationManager.deleteApplication(appName, project);

        assertTrue(result.isLeft());
        assertNotNull(result.getLeft());
        assertEquals(
                "An unexpected error occurred while deleting the application ValidApp. Please try again later. If the issue still persists, contact the platform team for assistance! Details: API error",
                result.getLeft().message());
    }

    @Test
    void getApplicationStatus_Success_WhenApiCallSucceeds() {
        String appName = "ValidApp";
        String namespace = "ValidNamespace";
        String project = "ValidProject";

        V1alpha1ApplicationStatus expectedStatus = new V1alpha1ApplicationStatus()
                .health(new V1alpha1HealthStatus().status("Healthy"))
                .sync(new V1alpha1SyncStatus().status("Synced"));

        V1alpha1Application application = new V1alpha1Application().status(expectedStatus);

        when(applicationServiceApi.applicationServiceGet(
                        eq(appName), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(List.of(project))))
                .thenReturn(application);

        Either<FailedOperation, V1alpha1ApplicationStatus> result =
                applicationManager.getApplicationStatus(appName, namespace, project);

        assertTrue(result.isRight());
        assertNotNull(result.get());
        assertEquals(expectedStatus, result.get());
        verify(applicationServiceApi)
                .applicationServiceGet(
                        eq(appName), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(List.of(project)));
    }

    @Test
    void getApplicationStatus_Error_WhenApiCallFails() {
        String appName = "ValidApp";
        String namespace = "ValidNamespace";
        String project = "ValidProject";

        when(applicationServiceApi.applicationServiceGet(
                        eq(appName), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(List.of(project))))
                .thenThrow(new RuntimeException("API error"));

        Either<FailedOperation, V1alpha1ApplicationStatus> result =
                applicationManager.getApplicationStatus(appName, namespace, project);

        assertTrue(result.isLeft());
        assertNotNull(result.getLeft());
        assertEquals(
                "An unexpected error occurred while getting the status of ValidApp. Please try again later. If the issue still persists, contact the platform team for assistance! Details: API error",
                result.getLeft().message());
        verify(applicationServiceApi)
                .applicationServiceGet(
                        eq(appName), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(List.of(project)));
    }

    private ArgoCDApplicationSpecific createValidSpecific() {
        ArgoCDApplicationSpecific specific = new ArgoCDApplicationSpecific();
        specific.setName("ValidApp");
        specific.setProject("ValidProject");
        specific.setDestination(new Destination("server", "namespace"));
        specific.setSource(new Source("repoURL", "path", "revision"));
        specific.setSyncPolicy(new SyncPolicy(null));
        return specific;
    }
}
