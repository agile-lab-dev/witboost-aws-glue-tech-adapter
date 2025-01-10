package com.witboost.provisioning.glue.config;

import com.witboost.provisioning.framework.service.ComponentClassProvider;
import com.witboost.provisioning.framework.service.SpecificClassProvider;
import com.witboost.provisioning.framework.service.impl.ComponentClassProviderImpl;
import com.witboost.provisioning.framework.service.impl.SpecificClassProviderImpl;
import com.witboost.provisioning.glue.model.GlueSpecific;
import com.witboost.provisioning.model.Workload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassProviderBean {

    @Bean
    public SpecificClassProvider specificClassProvider() {
        return SpecificClassProviderImpl.builder()
                .withDefaultSpecificClass(GlueSpecific.class)
                .withDefaultReverseProvisionSpecificClass(GlueSpecific.class)
                .build();
    }

    @Bean
    public ComponentClassProvider componentClassProvider() {
        return ComponentClassProviderImpl.builder()
                .withDefaultClass(Workload.class)
                .build();
    }
}
