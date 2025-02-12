package com.witboost.provisioning.glue.aws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class GlueJobClientWrapper {

    static Logger logger = LoggerFactory.getLogger(GlueJobClientWrapper.class);

    /**
     * This method lists the Glue jobs
     * @return the list of existing job names
     */
    public List<String> listJobs(GlueClient glueClient) {
        ListJobsRequest request = ListJobsRequest.builder().build();
        ListJobsResponse response = glueClient.listJobs(request);
        return response.jobNames();
    }

    /**
     * This method creates a new Glue job
     * @param glueClient an aws instance of a GlueClient
     * @param jobName Job name
     * @param iam the IAM role used to execute the job
     * @param scriptLocation the S3 script location
     * @param timeout Job timeout, optionally
     * @param description Job description
     * @param catalogName the catalog name, default is `glue_catalog`
     * @param warehouseLocation catalog warehouse location
     */
    public void createJob(
            GlueClient glueClient,
            String jobName,
            String iam,
            String scriptLocation,
            Optional<Integer> timeout,
            WorkerType workerType,
            Integer numberOfWorkers,
            ExecutionClass executionClass,
            String description,
            String catalogName,
            String warehouseLocation) {

        JobCommand command = JobCommand.builder()
                .pythonVersion("3")
                .name("glueetl")
                .scriptLocation(scriptLocation)
                .build();

        // Set up job parameters
        Map<String, String> defaultArguments = new HashMap<>();

        // Add Spark configuration parameters
        defaultArguments.put(
                "--conf",
                "spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions"
                        + " --conf spark.sql.catalog.glue_catalog.io-impl=org.apache.iceberg.aws.s3.S3FileIO"
                        + " --conf spark.sql.catalog.glue_catalog=org.apache.iceberg.spark.SparkCatalog"
                        + " --conf spark.sql.catalog.glue_catalog.warehouse="
                        + warehouseLocation
                        + " --conf spark.sql.catalog.glue_catalog.catalog-impl=org.apache.iceberg.aws.glue.GlueCatalog"
                        + " --conf spark.sql.defaultCatalog="
                        + catalogName);
        defaultArguments.put("--datalake-formats", "iceberg");

        // Common Glue job parameters
        defaultArguments.put("--enable-job-insights", "true");
        defaultArguments.put("--enable-metrics", "true");
        defaultArguments.put("--enable-continuous-cloudwatch-log", "true");
        defaultArguments.put("--job-language", "python");
        defaultArguments.put("--enable-spark-ui", "true");

        if (!doesJobExist(glueClient, jobName)) {

            logger.info("Creating job {}", jobName);

            CreateJobRequest jobRequest = CreateJobRequest.builder()
                    .name(jobName)
                    .description(description)
                    .glueVersion("5.0")
                    .workerType(workerType)
                    .numberOfWorkers(numberOfWorkers)
                    .timeout(timeout.orElse(5))
                    .executionClass(executionClass)
                    .role(iam)
                    .command(command)
                    .defaultArguments(defaultArguments)
                    .build();
            glueClient.createJob(jobRequest);

        } else {

            logger.info("Updating job {}", jobName);

            JobUpdate update = JobUpdate.builder()
                    .description(description)
                    .glueVersion("5.0")
                    .workerType(workerType)
                    .numberOfWorkers(numberOfWorkers)
                    .timeout(timeout.orElse(5))
                    .executionClass(executionClass)
                    .role(iam)
                    .command(command)
                    .defaultArguments(defaultArguments)
                    .build();

            UpdateJobRequest jobRequest = UpdateJobRequest.builder()
                    .jobName(jobName)
                    .jobUpdate(update)
                    .build();

            UpdateJobResponse r = glueClient.updateJob(jobRequest);
        }
    }

    /**
     * This method checks if the job exists
     * @param glueClient an aws instance of a GlueClient
     * @param jobName Glue job name
     * @return if the job exists or not
     */
    public boolean doesJobExist(GlueClient glueClient, String jobName) {
        return listJobs(glueClient).stream().anyMatch(j -> j.equals(jobName));
    }

    /**
     * This method deletes a Job
     * @param glueClient an aws instance of a GlueClient
     * @param jobName Glue job name
     */
    public void deleteJob(GlueClient glueClient, String jobName) {
        DeleteJobRequest jobRequest =
                DeleteJobRequest.builder().jobName(jobName).build();
        glueClient.deleteJob(jobRequest);
    }

    /**
     * Checks if the specified object exists in the given S3 directory bucket.
     *
     * @param s3Client an aws instance of a S3Client
     * @param location the object url
     * @return True if the object exists, false otherwise
     */
    public Boolean checkLocation(S3Client s3Client, String location) {
        try {
            URI uri = new URI(location);
            String bucketName = uri.getHost();
            String objectKey = uri.getPath().substring(1);

            logger.info("Checking script location for bucket {} and key {}", bucketName, objectKey);

            // Attempt to retrieve the object's metadata. If this request succeeds, the object exists
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
            return true; // If no exception is thrown, the object exists
        } catch (NoSuchKeyException e) {
            return false; // If NoSuchKeyException is thrown, the object does not exist
        } catch (NullPointerException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
