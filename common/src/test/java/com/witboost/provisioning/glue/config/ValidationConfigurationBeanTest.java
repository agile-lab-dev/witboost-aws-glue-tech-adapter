package com.witboost.provisioning.glue.config;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.glue.service.validation.WorkloadValidationService;
import org.junit.jupiter.api.Test;

class ValidationConfigurationBeanTest {

    @Test
    void beanCreation() {

        var workload = new WorkloadValidationService();
        var bean = new ValidationConfigurationBean().validationConfiguration(workload);
        assertEquals(workload, bean.getWorkloadValidationService());
    }
}
