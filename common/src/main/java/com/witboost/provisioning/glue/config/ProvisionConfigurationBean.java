package com.witboost.provisioning.glue.config;

import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.glue.service.provision.WorkloadProvisionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProvisionConfigurationBean {

    @Bean
    ProvisionConfiguration provisionConfiguration(WorkloadProvisionService workloadProvisionService) {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(workloadProvisionService)
                .build();
    }
}
