package com.witboost.provisioning.glue.service.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.witboost.provisioning.framework.openapi.model.DescriptorKind;
import com.witboost.provisioning.framework.openapi.model.ProvisioningRequest;
import com.witboost.provisioning.framework.service.validation.ValidationServiceImpl;
import com.witboost.provisioning.glue.config.ClassProviderBean;
import com.witboost.provisioning.glue.config.ConfigurationBean;
import com.witboost.provisioning.glue.util.ResourceUtils;
import com.witboost.provisioning.model.OperationType;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/*
 * TODO Review these tests after you have implemented the tech adapter logic
 */
class WorkloadValidationServiceTest {

    @Mock
    private ObjectProvider<S3Client> s3ClientProvider;

    @Mock
    private S3Client s3Client;

    ClassProviderBean classProviderBean = new ClassProviderBean();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @InjectMocks
    private WorkloadValidationService workloadValidationService;

    @Test
    void validateCorrectDescriptor0() throws IOException {

        when(s3ClientProvider.getObject(any(Region.class))).thenReturn(s3Client);

        var bean = new ConfigurationBean().validationConfiguration(workloadValidationService);
        ValidationServiceImpl service = new ValidationServiceImpl(
                bean, classProviderBean.componentClassProvider(), classProviderBean.specificClassProvider());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        var req = service.validate(provisioningRequest, OperationType.VALIDATE);

        assertTrue(req.isRight());
    }

    @Test
    void validateBadDescriptor0() throws IOException {

        when(s3ClientProvider.getObject(any(Region.class))).thenReturn(s3Client);

        var bean = new ConfigurationBean().validationConfiguration(workloadValidationService);
        ValidationServiceImpl service = new ValidationServiceImpl(
                bean, classProviderBean.componentClassProvider(), classProviderBean.specificClassProvider());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_bad0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);
        var req = service.validate(provisioningRequest, OperationType.VALIDATE);

        assertTrue(req.isLeft());
        var fail = req.getLeft();

        assertEquals(3, fail.problems().size());
        assertEquals(
                "The specified executionClass is unknown",
                fail.problems().get(0).getMessage());
        assertEquals(
                "The specified workerType is unknown", fail.problems().get(1).getMessage());
        assertEquals("The specified region is unknown", fail.problems().get(2).getMessage());
    }
}
