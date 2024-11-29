package it.agilelab.witboost.provisioning.argocd.service;

import com.witboost.provisioning.framework.service.ProvisionService;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import it.agilelab.witboost.provisioning.argocd.client.ApiClientProvider;
import java.util.*;
import java.util.stream.Collectors;
import org.openapitools.client.api.ApplicationServiceApi;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ArgocdProvisionService implements ProvisionService {

    private final Logger logger = LoggerFactory.getLogger(ArgocdProvisionService.class);
    private final ApiClientProvider apiClientProvider;

    public ArgocdProvisionService(ApiClientProvider apiClientProvider) {
        this.apiClientProvider = apiClientProvider;
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> provision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        try {
            ApplicationServiceApi applicationServiceApi = apiClientProvider.createApiClient();

            V1alpha1ApplicationList appList =
                    applicationServiceApi.applicationServiceList(null, null, null, null, null, null, null, null);

            List<String> appNames = Optional.ofNullable(appList)
                    .map(list -> list.getItems().stream()
                            .map(v1alpha1Application ->
                                    v1alpha1Application.getMetadata().getName())
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

            ProvisionInfo provisionInfo = ProvisionInfo.builder()
                    .privateInfo(Optional.of("First result. Application list: " + appNames))
                    .build();

            return Either.right(provisionInfo);
        } catch (Exception e) {
            String error =
                    "An unexpected error occurred while processing the request. Please try again later. If the issue still persists, contact the platform team for assistance! Details: "
                            + e.getMessage();
            logger.error(error, e);
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error))));
        }
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> unprovision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {
        return Either.left(new FailedOperation("Not implemented", new ArrayList<>()));
    }
}
