package it.agilelab.witboost.provisioning.argocd.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.OperationRequest;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArgocdValidationServiceTest {

    private ArgocdValidationService argocdValidationService;

    @BeforeEach
    public void setUp() {
        argocdValidationService = new ArgocdValidationService();
    }

    @Test
    public void testValidateSuccess() {
        OperationRequest<?, Specific> operationRequest = mock(OperationRequest.class);

        Either<FailedOperation, Void> result =
                argocdValidationService.validate(operationRequest, OperationType.PROVISION);

        assertTrue(result.isRight());
        assertNull(result.get());
    }
}
