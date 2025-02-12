package com.witboost.provisioning.glue.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class GlueJobClientWrapperTest {

    @Mock
    GlueClient glueClient;

    @Mock
    S3Client s3Client;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createJob_success() {

        ListJobsResponse resp =
                ListJobsResponse.builder().jobNames(Collections.emptyList()).build();

        when(glueClient.listJobs(any(ListJobsRequest.class))).thenReturn(resp);
        when(glueClient.createJob(any(CreateJobRequest.class))).thenReturn(any(CreateJobResponse.class));

        GlueJobClientWrapper wrapper = new GlueJobClientWrapper();

        wrapper.createJob(
                glueClient,
                "jobName",
                "roleName",
                "s3://...",
                Optional.empty(),
                WorkerType.G_1_X,
                2,
                ExecutionClass.STANDARD,
                "some description",
                "cname",
                "s3://..");
    }

    @Test
    public void updateJob_success() {

        String jobName = "existingJobName";

        ListJobsResponse resp = ListJobsResponse.builder().jobNames(jobName).build();

        when(glueClient.listJobs(any(ListJobsRequest.class))).thenReturn(resp);
        when(glueClient.updateJob(any(UpdateJobRequest.class))).thenReturn(any(UpdateJobResponse.class));

        GlueJobClientWrapper wrapper = new GlueJobClientWrapper();

        wrapper.createJob(
                glueClient,
                jobName,
                "roleName",
                "s3://...",
                Optional.empty(),
                WorkerType.G_1_X,
                2,
                ExecutionClass.STANDARD,
                "some description",
                "cname",
                "s3://..");
    }

    @Test
    public void deleteJob_success() {
        GlueJobClientWrapper wrapper = new GlueJobClientWrapper();
        when(glueClient.deleteJob(any(DeleteJobRequest.class))).thenReturn(any(DeleteJobResponse.class));
        wrapper.deleteJob(glueClient, "jobName");
    }

    @Test
    public void checkLocation_exists_success() {
        GlueJobClientWrapper wrapper = new GlueJobClientWrapper();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(any(HeadObjectResponse.class));
        var res = wrapper.checkLocation(s3Client, "s3://some-location/path.py");
        assertTrue(res);
    }

    @Test
    public void checkLocation_doesntexists_success() {
        GlueJobClientWrapper wrapper = new GlueJobClientWrapper();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder()
                        .message("Obj doesn't exist")
                        .cause(new RuntimeException(""))
                        .build());

        var res = wrapper.checkLocation(s3Client, "s3://some-location/path.py");
        assertFalse(res);
    }

    @Test
    public void checkLocation_exists_fail() {
        GlueJobClientWrapper wrapper = new GlueJobClientWrapper();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(new NullPointerException());

        assertThrows(RuntimeException.class, () -> {
            wrapper.checkLocation(s3Client, "s3://some-location/path.py");
        });
    }
}
