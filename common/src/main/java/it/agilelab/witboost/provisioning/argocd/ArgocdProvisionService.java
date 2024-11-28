package it.agilelab.witboost.provisioning.argocd;

import com.witboost.provisioning.framework.service.ProvisionService;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import java.util.ArrayList;

public class ArgocdProvisionService implements ProvisionService {

    @Override
    public Either<FailedOperation, ProvisionInfo> provision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {
        return Either.left(new FailedOperation("Not implemented", new ArrayList<>()));
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> unprovision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {
        return Either.left(new FailedOperation("Not implemented", new ArrayList<>()));
    }
}
