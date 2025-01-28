package com.witboost.provisioning.glue.service.provision;

import static io.vavr.control.Either.right;

import com.fasterxml.jackson.databind.JsonNode;
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
import io.vavr.control.Option;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class WorkloadProvisionService implements ProvisionService {

    static Logger logger = LoggerFactory.getLogger(GlueJobClientWrapper.class);

    private final GlueJobClientWrapper glueJobClientWrapper;
    private final ObjectProvider<GlueClient> glueClientProvider;
    private final ObjectProvider<S3Client> s3ClientProvider;

    public WorkloadProvisionService(
            ObjectProvider<GlueClient> glueClientProvider,
            ObjectProvider<S3Client> s3ClientProvider,
            @Autowired GlueJobClientWrapper glueJobClientWrapper) {
        this.glueJobClientWrapper = glueJobClientWrapper;
        this.glueClientProvider = glueClientProvider;
        this.s3ClientProvider = s3ClientProvider;
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
                    List.of(new Problem("The component descriptor is empty"))));

        component = operationRequest.getComponent().get();

        GlueSpecific specific = (GlueSpecific) component.getSpecific();
        // Get the GlueClient from the provider (cached or new)
        GlueClient glueClient = glueClientProvider.getObject(specific.getRegion());

        String computedJobName = GlueJobUtils.computeName(operationRequest.getDataProduct(), component);

        Option<JsonNode> privateInfoJN =
                operationRequest.getDataProduct().getComponentToProvision(specific.getStorageAreaId());

        if (privateInfoJN.isEmpty()) {
            return Either.left(new FailedOperation(
                    "Could not extract the dependant storage component",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem(
                            "The specific.storageAreaId field does not match any component in the descriptor"))));
        }

        Option<String> scriptBasePath = privateInfoJN.map(x ->
                x.get("info").get("privateInfo").get("location").get("value").asText());

        if (scriptBasePath.isEmpty()) {
            return Either.left(new FailedOperation(
                    "The dependant storage component is not including the location",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem("Missing location at info.privateInfo.location.value"))));
        }

        // Assembly and validate script location
        String scriptLocation = scriptBasePath + "/" + specific.getScriptName();

        S3Client s3Client = s3ClientProvider.getObject(specific.getRegion());

        logger.info("Checking computed script location {}", scriptLocation);

        try {
            if (!glueJobClientWrapper.checkLocation(s3Client, scriptLocation)) {
                return Either.left(new FailedOperation(
                        "Prerequisites are not met.",
                        Optional.empty(),
                        Optional.empty(),
                        List.of(new Problem("The S3 script location does not exist"))));
            }
        } catch (Exception e) {
            return Either.left(new FailedOperation(
                    "Unexpected error.",
                    Optional.empty(),
                    Optional.empty(),
                    List.of(new Problem("An unexpected error occurred while validating the script location", e))));
        }

        try {
            glueJobClientWrapper.createJob(
                    glueClient,
                    computedJobName,
                    specific.getIamRole(),
                    scriptLocation,
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
