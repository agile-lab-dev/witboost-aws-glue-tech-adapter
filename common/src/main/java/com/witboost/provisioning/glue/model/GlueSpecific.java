package com.witboost.provisioning.glue.model;

import static io.vavr.control.Either.right;

import com.witboost.provisioning.model.Specific;
import io.vavr.control.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.model.ExecutionClass;
import software.amazon.awssdk.services.glue.model.WorkerType;

public class GlueSpecific extends Specific {

    private String scriptLocation;
    private String iamRole; // This can actually be set at DP level
    private Optional<Integer> timeout;
    private String region;
    private String workerType;
    private String executionClass;
    private Integer numberOfWorkers;

    public GlueSpecific() {}

    public String getScriptLocation() {
        return scriptLocation;
    }

    public String getIamRole() {
        return iamRole;
    }

    public Optional<Integer> getTimeout() {
        return timeout;
    }

    public Region getRegion() {
        return Region.of(region);
    }

    public WorkerType getWorkerType() {
        return WorkerType.fromValue(workerType);
    }

    public ExecutionClass getExecutionClass() {
        return ExecutionClass.fromValue(executionClass);
    }

    public Integer getNumberOfWorkers() {
        return numberOfWorkers;
    }
    ;

    public void setIamRole(String iamRole) {
        this.iamRole = iamRole;
    }

    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    public void setTimeout(Optional<Integer> timeout) {
        this.timeout = timeout;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public void setExecutionClass(String executionClass) {
        this.executionClass = executionClass;
    }

    public void setNumberOfWorkers(Integer numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
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
