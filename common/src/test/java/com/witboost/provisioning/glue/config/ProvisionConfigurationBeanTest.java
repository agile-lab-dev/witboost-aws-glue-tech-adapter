package com.witboost.provisioning.glue.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.service.provision.WorkloadProvisionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.s3.S3Client;

class ProvisionConfigurationBeanTest {

    @Autowired
    private GlueJobClientWrapper glueJobClientWrapper;

    @Mock
    private ObjectProvider<S3Client> s3ClientObjectProvider;

    @Mock
    private ObjectProvider<GlueClient> glueClientObjectProvider;

    @Test
    void beanCreation() {
        var workload =
                new WorkloadProvisionService(glueClientObjectProvider, s3ClientObjectProvider, glueJobClientWrapper);
        var bean = new ConfigurationBean().provisionConfiguration(workload);
        assertEquals(workload, bean.getWorkloadProvisionService());
    }
}
