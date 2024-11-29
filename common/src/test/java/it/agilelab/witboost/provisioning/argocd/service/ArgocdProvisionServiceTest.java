package it.agilelab.witboost.provisioning.argocd.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.client.ApiClientProvider;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openapitools.client.api.ApplicationServiceApi;
import org.openapitools.client.model.V1ObjectMeta;
import org.openapitools.client.model.V1alpha1Application;
import org.openapitools.client.model.V1alpha1ApplicationList;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ArgocdProvisionServiceTest {

    @Mock
    private ApiClientProvider apiClientProvider;

    @InjectMocks
    private ArgocdProvisionService argocdProvisionService;

    @Test
    public void testProvisionSuccess() {

        ApplicationServiceApi applicationServiceApi = mock(ApplicationServiceApi.class);
        when(apiClientProvider.createApiClient()).thenReturn(applicationServiceApi);

        V1alpha1ApplicationList appList = mock(V1alpha1ApplicationList.class);
        when(applicationServiceApi.applicationServiceList(null, null, null, null, null, null, null, null))
                .thenReturn(appList);

        // Create a simulated list of application names
        List<String> appNames = List.of("app1", "app2", "app3");
        List<V1alpha1Application> mockApps = appNames.stream()
                .map(appName -> {
                    // Mock the application object
                    V1alpha1Application app = mock(V1alpha1Application.class);
                    V1ObjectMeta metadata = mock(V1ObjectMeta.class);

                    // Stubbing the methods to return predefined values
                    when(metadata.getName()).thenReturn(appName);
                    when(app.getMetadata()).thenReturn(metadata);

                    return app;
                })
                .collect(Collectors.toList());

        when(appList.getItems()).thenReturn(mockApps);
        ProvisionOperationRequest<?, Specific> request = mock(ProvisionOperationRequest.class);

        // Call the method under test
        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(request);

        // Verify the result of the provisioning operation
        assertTrue(result.isRight());
        assertNotNull(result.get());
        assertEquals(
                "First result. Application list: [app1, app2, app3]",
                result.get().getPrivateInfo().get());
    }

    @Test
    public void testProvisionFailure() {
        ApplicationServiceApi applicationServiceApi = mock(ApplicationServiceApi.class);
        when(apiClientProvider.createApiClient()).thenReturn(applicationServiceApi);
        when(applicationServiceApi.applicationServiceList(null, null, null, null, null, null, null, null))
                .thenThrow(new RuntimeException("exception"));

        ProvisionOperationRequest<?, Specific> request = mock(ProvisionOperationRequest.class);

        // Call the method under test
        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.provision(request);

        // Verify the result of the failed provisioning operation
        assertTrue(result.isLeft());
        assertEquals(
                "An unexpected error occurred while processing the request. Please try again later. If the issue still persists, contact the platform team for assistance! Details: exception",
                result.getLeft().message());
    }

    @Test
    public void testUnprovision() {
        ProvisionOperationRequest<?, Specific> request = mock(ProvisionOperationRequest.class);

        // Call the method under test
        Either<FailedOperation, ProvisionInfo> result = argocdProvisionService.unprovision(request);

        // Verify the result of the unprovisioning operation
        assertTrue(result.isLeft()); // Verify the result is a failure
        assertEquals("Not implemented", result.getLeft().message());
    }
}
