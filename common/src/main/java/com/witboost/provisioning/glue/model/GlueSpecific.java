package com.witboost.provisioning.glue.model;

import static io.vavr.control.Either.right;

import com.witboost.provisioning.model.Specific;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.model.ExecutionClass;
import software.amazon.awssdk.services.glue.model.WorkerType;

@NoArgsConstructor
@Getter
@Setter
public class GlueSpecific extends Specific {

    @NotBlank
    private String scriptName;

    @NotBlank
    private String iamRole; // This can actually be set at DP level

    private Optional<Integer> timeout;

    @NotBlank
    private String region;

    @NotBlank
    private String workerType;

    @NotBlank
    private String executionClass;

    @Positive
    private Integer numberOfWorkers;

    @NotBlank
    private String catalogName;

    @NotBlank
    private String warehouseLocation;

    @NotBlank
    private String storageAreaId;

    @Valid
    @NotNull
    private List<JobParameters> additionalJobParameters;

    @Valid
    @NotNull
    private List<JobParameters> additionalSparkProperties;

    public WorkerType getWorkerType() {
        return WorkerType.fromValue(workerType);
    }

    public ExecutionClass getExecutionClass() {
        return ExecutionClass.fromValue(executionClass);
    }

    public Region getRegion() {
        return Region.of(region);
    }

    /**
     * This method checks the validity of the parameters of this Specific object
     * @return the list of problems in the left branch, or nothing in the right branch
     */
    public Either<List<String>, Void> isValid() {
        ArrayList<String> problems = new ArrayList<>();
        if (this.getExecutionClass().compareTo(ExecutionClass.UNKNOWN_TO_SDK_VERSION) == 0)
            problems.add(("The specified executionClass is unknown"));
        if (this.getWorkerType().compareTo(WorkerType.UNKNOWN_TO_SDK_VERSION) == 0)
            problems.add(("The specified workerType is unknown"));
        if (!Region.regions().contains(this.getRegion())) problems.add(("The specified region is unknown"));

        if (problems.isEmpty()) return right(null);
        else return Either.left(problems);
    }
}
