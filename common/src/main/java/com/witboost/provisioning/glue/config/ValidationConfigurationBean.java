package com.witboost.provisioning.glue.config;

import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import com.witboost.provisioning.glue.service.validation.WorkloadValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfigurationBean {
    @Bean
    ValidationConfiguration validationConfiguration(WorkloadValidationService workloadValidationService) {
        return ValidationConfiguration.builder()
                .workloadValidationService(workloadValidationService)
                .build();
    }
}
