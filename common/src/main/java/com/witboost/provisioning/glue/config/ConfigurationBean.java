package com.witboost.provisioning.glue.config;

import com.witboost.provisioning.framework.service.ProvisionConfiguration;
import com.witboost.provisioning.framework.service.validation.ValidationConfiguration;
import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.service.provision.WorkloadProvisionService;
import com.witboost.provisioning.glue.service.validation.WorkloadValidationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class ConfigurationBean {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public S3Client createS3Client(Region region) {
        return S3Client.builder().region(region).build();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GlueClient createGlueClient(Region region) {
        return GlueClient.builder().region(region).build();
    }

    @Bean
    public GlueJobClientWrapper glueJobClientWrapper() {
        return new GlueJobClientWrapper();
    }

    @Bean
    public WorkloadProvisionService workloadProvisionService(
            ObjectProvider<GlueClient> glueClientObjectProvider, GlueJobClientWrapper glueJobClientWrapper) {
        return new WorkloadProvisionService(glueClientObjectProvider, glueJobClientWrapper);
    }

    @Bean
    public ProvisionConfiguration provisionConfiguration(WorkloadProvisionService workloadProvisionService) {
        return ProvisionConfiguration.builder()
                .workloadProvisionService(workloadProvisionService)
                .build();
    }

    @Bean
    public WorkloadValidationService workloadValidationService(
            ObjectProvider<S3Client> s3ClientObjectProvider, GlueJobClientWrapper glueJobClientWrapper) {
        return new WorkloadValidationService(s3ClientObjectProvider, glueJobClientWrapper);
    }

    @Bean
    public ValidationConfiguration validationConfiguration(WorkloadValidationService workloadValidationService) {
        return ValidationConfiguration.builder()
                .workloadValidationService(workloadValidationService)
                .build();
    }
}
