package com.witboost.provisioning.glue.service.provision;

import static io.vavr.control.Either.right;

import com.witboost.provisioning.framework.service.ProvisionService;
import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.model.GlueSpecific;
import com.witboost.provisioning.glue.utils.GlueJobUtils;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.ProvisionOperationRequest;
import com.witboost.provisioning.model.status.ProvisionInfo;
import io.vavr.control.Either;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.glue.GlueClient;

@Component
public class WorkloadProvisionService implements ProvisionService {

    private final GlueJobClientWrapper glueJobClientWrapper;
    private final ObjectProvider<GlueClient> glueClientProvider;

    public WorkloadProvisionService(
            ObjectProvider<GlueClient> glueClientProvider, @Autowired GlueJobClientWrapper glueJobClientWrapper) {
        this.glueJobClientWrapper = glueJobClientWrapper;
        this.glueClientProvider = glueClientProvider;
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> provision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        com.witboost.provisioning.model.Component component;

        // Extract component
        if (operationRequest.getComponent().isEmpty())
            return Either.left(new FailedOperation(
                    "The component descriptor is malformed",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem(" The component descriptor is empty"))));

        component = operationRequest.getComponent().get();

        GlueSpecific specific = (GlueSpecific) component.getSpecific();
        // Get the GlueClient from the provider (cached or new)
        GlueClient glueClient = glueClientProvider.getObject(specific.getRegion());

        String computedJobName = GlueJobUtils.computeName(operationRequest.getDataProduct(), component);

        try {
            glueJobClientWrapper.createJob(
                    glueClient,
                    computedJobName,
                    specific.getIamRole(),
                    specific.getScriptLocation(),
                    specific.getTimeout(),
                    specific.getWorkerType(),
                    specific.getNumberOfWorkers(),
                    specific.getExecutionClass(),
                    component.getDescription());
        } catch (Exception e) {
            return Either.left(new FailedOperation(
                    "Job creation failed",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem(e.getMessage(), e))));
        }

        return right(null);
    }

    @Override
    public Either<FailedOperation, ProvisionInfo> unprovision(
            ProvisionOperationRequest<?, ? extends Specific> operationRequest) {

        com.witboost.provisioning.model.Component component;

        // Extract component
        if (operationRequest.getComponent().isEmpty())
            return Either.left(new FailedOperation(
                    "The component descriptor is malformed",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem(" The component descriptor is empty"))));

        component = operationRequest.getComponent().get();
        GlueSpecific specific = (GlueSpecific) component.getSpecific();

        String computedJobName = GlueJobUtils.computeName(operationRequest.getDataProduct(), component);

        // Get the GlueClient from the provider (cached or new)
        GlueClient glueClient = glueClientProvider.getObject(specific.getRegion());

        try {
            glueJobClientWrapper.deleteJob(glueClient, computedJobName);
        } catch (Exception e) {
            return Either.left(new FailedOperation(
                    "Job deletion failed",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem(e.getMessage(), e))));
        }

        return right(null);
    }
}
