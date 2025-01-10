package it.agilelab.witboost.provisioning.argocd.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.OperationRequest;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.model.application.ArgoCDApplicationSpecific;
import it.agilelab.witboost.provisioning.argocd.model.application.Destination;
import it.agilelab.witboost.provisioning.argocd.model.application.Source;
import it.agilelab.witboost.provisioning.argocd.model.application.SyncPolicy;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ArgocdValidationServiceTest {

    private ArgocdValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ArgocdValidationService();
    }

    @Test
    void validate_KO_ComponentIsEmpty() {
        OperationRequest<?, ArgoCDApplicationSpecific> request = Mockito.mock(OperationRequest.class);
        when(request.getComponent()).thenReturn(Optional.empty());

        Either<FailedOperation, Void> result = validationService.validate(request, OperationType.PROVISION);

        assertTrue(result.isLeft());
        FailedOperation error = result.getLeft();
        assertNotNull(error);
        assertEquals("Invalid operation request: Component is missing. Request: " + request, error.message());
    }

    @Test
    void validate_KO_SpecificTypeIsInvalid() {
        OperationRequest<?, Specific> request = Mockito.mock(OperationRequest.class);
        var invalidSpecific = mock(Specific.class);

        var component = mock(Workload.class);
        when(component.getSpecific()).thenReturn(invalidSpecific);
        when(component.getName()).thenReturn("TestComponent");
        when(request.getComponent()).thenReturn(Optional.of(component));

        Either<FailedOperation, Void> result = validationService.validate(request, OperationType.PROVISION);

        assertTrue(result.isLeft());
        FailedOperation error = result.getLeft();
        assertNotNull(error);
        assertEquals("Invalid Specific type of TestComponent. Expected ArgoCDApplicationSpecific.", error.message());
    }

    @Test
    void validate_OK() {
        OperationRequest<?, ArgoCDApplicationSpecific> request = Mockito.mock(OperationRequest.class);
        ArgoCDApplicationSpecific validSpecific = new ArgoCDApplicationSpecific();
        validSpecific.setName("ValidName");
        validSpecific.setProject("ValidProject");
        validSpecific.setDestination(new Destination());
        validSpecific.setSource(new Source());
        validSpecific.setSyncPolicy(new SyncPolicy());

        var component = mock(com.witboost.provisioning.model.Component.class);
        when(component.getSpecific()).thenReturn(validSpecific);
        when(request.getComponent()).thenReturn(Optional.of(component));

        Either<FailedOperation, Void> result = validationService.validate(request, OperationType.PROVISION);

        assertTrue(result.isRight());
    }
}
