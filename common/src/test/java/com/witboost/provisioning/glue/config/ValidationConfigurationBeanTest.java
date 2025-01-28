package com.witboost.provisioning.glue.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.witboost.provisioning.glue.service.validation.WorkloadValidationService;
import org.junit.jupiter.api.Test;

class ValidationConfigurationBeanTest {

    @Test
    void beanCreation() {

        var workload = new WorkloadValidationService();
        var bean = new ConfigurationBean().validationConfiguration(workload);
        assertEquals(workload, bean.getWorkloadValidationService());
    }
}
