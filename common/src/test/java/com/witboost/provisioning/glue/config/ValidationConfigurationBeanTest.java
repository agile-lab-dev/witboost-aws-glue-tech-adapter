package com.witboost.provisioning.glue.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.service.validation.WorkloadValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.s3.S3Client;

class ValidationConfigurationBeanTest {

    @Autowired
    private GlueJobClientWrapper glueJobClientWrapper;

    @Mock
    private ObjectProvider<S3Client> s3ClientObjectProvider;

    @Test
    void beanCreation() {

        var workload = new WorkloadValidationService(s3ClientObjectProvider, glueJobClientWrapper);
        var bean = new ConfigurationBean().validationConfiguration(workload);
        assertEquals(workload, bean.getWorkloadValidationService());
    }
}
