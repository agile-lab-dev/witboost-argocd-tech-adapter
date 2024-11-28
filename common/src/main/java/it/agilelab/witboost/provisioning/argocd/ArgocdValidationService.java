package it.agilelab.witboost.provisioning.argocd;

import com.witboost.provisioning.framework.service.validation.ComponentValidationService;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.OperationRequest;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import java.util.ArrayList;

public class ArgocdValidationService implements ComponentValidationService {
    @Override
    public Either<FailedOperation, Void> validate(
            @Valid OperationRequest<?, ? extends Specific> operationRequest, OperationType operationType) {
        return Either.left(new FailedOperation("Not implemented", new ArrayList<>()));
    }
}
