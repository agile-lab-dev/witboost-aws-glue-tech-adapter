package com.witboost.provisioning.glue.service.validation;

import static io.vavr.control.Either.right;

import com.witboost.provisioning.framework.service.validation.ComponentValidationService;
import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.model.GlueSpecific;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.OperationRequest;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.services.s3.S3Client;

@Component
@Validated
public class WorkloadValidationService implements ComponentValidationService {

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final GlueJobClientWrapper glueJobClientWrapper;

    @Autowired
    public WorkloadValidationService(
            ObjectProvider<S3Client> s3ClientProvider, @Autowired GlueJobClientWrapper glueJobClientWrapper) {
        this.s3ClientProvider = s3ClientProvider;
        this.glueJobClientWrapper = glueJobClientWrapper;
    }

    @Override
    public Either<FailedOperation, Void> validate(
            @Valid OperationRequest<?, ? extends Specific> operationRequest, OperationType operationType) {

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

        Either<List<String>, Void> t = specific.isValid();
        if (t.isLeft()) {
            return Either.left(new FailedOperation(
                    "The specific section of the descriptor contains one or more invalid values",
                    Optional.empty(),
                    Optional.empty(),
                    t.getLeft().stream().map(Problem::new).toList()));
        }

        S3Client s3Client = s3ClientProvider.getObject(specific.getRegion());

        // Check location
        try {
            if (!glueJobClientWrapper.checkLocation(s3Client, specific.getScriptLocation())) {
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

        return right(null);
    }
}
