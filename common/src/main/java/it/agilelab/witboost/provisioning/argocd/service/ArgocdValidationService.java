package it.agilelab.witboost.provisioning.argocd.service;

import com.witboost.provisioning.framework.service.validation.ComponentValidationService;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.OperationRequest;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class ArgocdValidationService implements ComponentValidationService {
    @Override
    public Either<FailedOperation, Void> validate(
            @Valid OperationRequest<?, ? extends Specific> operationRequest, OperationType operationType) {
        return Either.right(null);
        //        return Either.left(new FailedOperation("Not implemented", new ArrayList<>()));
    }
}
