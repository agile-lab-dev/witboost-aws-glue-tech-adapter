package com.witboost.provisioning.glue.service.validation;

import static io.vavr.control.Either.right;

import com.witboost.provisioning.framework.service.validation.ComponentValidationService;
import com.witboost.provisioning.glue.model.GlueSpecific;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.OperationRequest;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class WorkloadValidationService implements ComponentValidationService {

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

        if (!(component.getSpecific() instanceof @Valid GlueSpecific specific)) {
            String error = String.format("Invalid Specific type of %s. Expected GlueSpecific.", component.getName());
            return Either.left(new FailedOperation(error, Collections.singletonList(new Problem(error))));
        }

        Either<List<String>, Void> t = specific.isValid();
        if (t.isLeft()) {
            return Either.left(new FailedOperation(
                    "The specific section of the descriptor contains one or more invalid values",
                    Optional.empty(),
                    Optional.empty(),
                    t.getLeft().stream().map(Problem::new).toList()));
        }

        return right(null);
    }
}
