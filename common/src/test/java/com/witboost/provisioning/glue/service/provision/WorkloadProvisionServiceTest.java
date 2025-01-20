package com.witboost.provisioning.glue.service.provision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.witboost.provisioning.framework.openapi.model.DescriptorKind;
import com.witboost.provisioning.framework.openapi.model.ProvisioningRequest;
import com.witboost.provisioning.framework.service.validation.ValidationServiceImpl;
import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.config.ClassProviderBean;
import com.witboost.provisioning.glue.config.ConfigurationBean;
import com.witboost.provisioning.glue.service.validation.WorkloadValidationService;
import com.witboost.provisioning.glue.util.ResourceUtils;
import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.OperationType;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.AccessControlOperationRequest;
import com.witboost.provisioning.model.request.ReverseProvisionOperationRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.ExecutionClass;
import software.amazon.awssdk.services.glue.model.GlueException;
import software.amazon.awssdk.services.glue.model.WorkerType;

/*
 * TODO Review these tests after you have implemented the tech adapter logic
 */
class WorkloadProvisionServiceTest {

    @InjectMocks
    private WorkloadProvisionService workloadProvisionService;

    @InjectMocks
    private WorkloadValidationService workloadValidationService;

    @Mock
    private GlueJobClientWrapper glueJobClientWrapper;

    @Mock
    private ObjectProvider<GlueClient> glueClientProvider;

    @Mock
    private GlueClient glueClient;

    ClassProviderBean classProviderBean = new ClassProviderBean();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateAclUnimplemented() {

        var expectedError = new FailedOperation(
                "Access control lists update for the operation request is not supported",
                Collections.singletonList(new Problem(
                        "This adapter doesn't support updating access control lists for the received request",
                        Set.of(
                                "Ensure that the adapter is registered correctly for this type of request and that the ProvisionConfiguration is set up to support the requested component",
                                "Please try again. If the problem persists, contact the platform team."))));
        var actual = workloadProvisionService.updateAcl(
                new AccessControlOperationRequest<>(new DataProduct<>(), Optional.of(new Workload<>()), Set.of()));
        assertTrue(actual.isLeft());
        assertEquals(expectedError, actual.getLeft());
    }

    @Test
    void reverseProvisionUnimplemented() {

        var expectedError = new FailedOperation(
                "Reverse provisioning for the operation request is not supported",
                Collections.singletonList(new Problem(
                        "This adapter doesn't support reverse provisioning for the received request",
                        Set.of(
                                "Ensure that the adapter is registered correctly for this type of request and that the ProvisionConfiguration is set up to support the requested component",
                                "Please try again. If the problem persists, contact the platform team."))));
        var actual = workloadProvisionService.reverseProvision(
                new ReverseProvisionOperationRequest<>("useCaseTemplateId", "environment", new Specific(), null));
        assertTrue(actual.isLeft());
        assertEquals(expectedError, actual.getLeft());
    }

    @Test
    void provisionCorrectDescriptor0() throws IOException {

        var bean = new ConfigurationBean().validationConfiguration(workloadValidationService);
        ValidationServiceImpl service = new ValidationServiceImpl(
                bean, classProviderBean.componentClassProvider(), classProviderBean.specificClassProvider());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);

        var req = service.validate(provisioningRequest, OperationType.VALIDATE);
        var opReq = req.get();

        Mockito.doNothing()
                .when(glueJobClientWrapper)
                .createJob(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Optional.class),
                        any(WorkerType.class),
                        Mockito.anyInt(),
                        any(ExecutionClass.class),
                        anyString());

        when(glueClientProvider.getObject(any(Region.class))).thenReturn(glueClient);
        var res = workloadProvisionService.provision(opReq);

        assertTrue(res.isRight());
    }

    @Test
    void provisionCorrectDescriptor1() throws IOException {

        var bean = new ConfigurationBean().validationConfiguration(workloadValidationService);
        ValidationServiceImpl service = new ValidationServiceImpl(
                bean, classProviderBean.componentClassProvider(), classProviderBean.specificClassProvider());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);
        var req = service.validate(provisioningRequest, OperationType.VALIDATE);

        var opReq = req.get();

        Mockito.doThrow(GlueException.builder().message("Some glue exception").build())
                .when(glueJobClientWrapper)
                .createJob(
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Optional.class),
                        any(WorkerType.class),
                        Mockito.anyInt(),
                        any(ExecutionClass.class),
                        anyString());

        when(glueClientProvider.getObject(any(Region.class))).thenReturn(glueClient);
        var res = workloadProvisionService.provision(opReq);

        assertTrue(res.isLeft());
        assertEquals("Some glue exception", res.getLeft().problems().get(0).description());
    }

    @Test
    void unprovisionCorrectDescriptor0() throws IOException {

        var bean = new ConfigurationBean().validationConfiguration(workloadValidationService);
        ValidationServiceImpl service = new ValidationServiceImpl(
                bean, classProviderBean.componentClassProvider(), classProviderBean.specificClassProvider());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);

        var req = service.validate(provisioningRequest, OperationType.VALIDATE);
        var opReq = req.get();

        Mockito.doNothing().when(glueJobClientWrapper).deleteJob(any(), anyString());

        when(glueClientProvider.getObject(any(Region.class))).thenReturn(glueClient);
        var res = workloadProvisionService.unprovision(opReq);

        assertTrue(res.isRight());
    }

    @Test
    void unprovisionCorrectDescriptor1() throws IOException {

        var bean = new ConfigurationBean().validationConfiguration(workloadValidationService);
        ValidationServiceImpl service = new ValidationServiceImpl(
                bean, classProviderBean.componentClassProvider(), classProviderBean.specificClassProvider());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);

        var req = service.validate(provisioningRequest, OperationType.VALIDATE);
        var opReq = req.get();

        doThrow(GlueException.builder()
                        .message("Some bad error message")
                        .cause(new RuntimeException(""))
                        .build())
                .when(glueJobClientWrapper)
                .deleteJob(any(), anyString());

        when(glueClientProvider.getObject(any(Region.class))).thenReturn(glueClient);

        var res = workloadProvisionService.unprovision(opReq);

        assertTrue(res.isLeft());
    }
}
