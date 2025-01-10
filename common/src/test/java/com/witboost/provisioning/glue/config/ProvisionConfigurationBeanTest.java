package com.witboost.provisioning.glue.config;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.glue.service.provision.WorkloadProvisionService;
import org.junit.jupiter.api.Test;

class ProvisionConfigurationBeanTest {

    @Test
    void beanCreation() {
        var workload = new WorkloadProvisionService();
        var bean = new ProvisionConfigurationBean().provisionConfiguration(workload);
        assertEquals(workload, bean.getWorkloadProvisionService());
    }
}
