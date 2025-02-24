package com.witboost.provisioning.glue.service.provision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.framework.openapi.model.DescriptorKind;
import com.witboost.provisioning.framework.openapi.model.ProvisioningRequest;
import com.witboost.provisioning.glue.aws.GlueJobClientWrapper;
import com.witboost.provisioning.glue.util.ResourceUtils;
import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.Specific;
import com.witboost.provisioning.model.Workload;
import com.witboost.provisioning.model.common.FailedOperation;
import com.witboost.provisioning.model.common.Problem;
import com.witboost.provisioning.model.request.AccessControlOperationRequest;
import com.witboost.provisioning.model.request.ReverseProvisionOperationRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.ExecutionClass;
import software.amazon.awssdk.services.glue.model.GlueException;
import software.amazon.awssdk.services.glue.model.WorkerType;

@SpringBootTest
@AutoConfigureMockMvc
class WorkloadProvisionServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String mockProvisionEndpoint = "http://127.0.0.1:8888/v1/provision";
    private final String mockUnprovisionEndpoint = "http://127.0.0.1:8888/v1/unprovision";

    @InjectMocks
    private WorkloadProvisionService workloadProvisionService;

    @MockBean
    private GlueJobClientWrapper glueJobClientWrapper;

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
    void provisionCorrectDescriptor0() throws Exception {

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);
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
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockProvisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        assertTrue(result.getResponse().getStatus() == 200);
    }

    @Test
    void provisionCorrectDescriptor1() throws Exception {

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);
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
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockProvisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("Some glue exception"));
    }

    @Test
    void unprovisionCorrectDescriptor0() throws Exception {

        Mockito.doNothing().when(glueJobClientWrapper).deleteJob(any(), anyString());
        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");

        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockUnprovisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        var response = result.getResponse();
        assertEquals(200, response.getStatus());
    }

    @Test
    void unprovisionCorrectDescriptor1() throws Exception {

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);
        doThrow(GlueException.builder()
                        .message("Some bad error message")
                        .cause(new RuntimeException(""))
                        .build())
                .when(glueJobClientWrapper)
                .deleteJob(any(), anyString());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockUnprovisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        var response = result.getResponse();

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Some bad error message"));
    }

    @Test
    void validateBadLocation0() throws Exception {

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(false);
        Mockito.doNothing()
                .when(glueJobClientWrapper)
                .createJob(
                        any(GlueClient.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        Mockito.any(Optional.class),
                        Mockito.any(WorkerType.class),
                        Mockito.anyInt(),
                        Mockito.any(ExecutionClass.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        any());

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockProvisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        var response = result.getResponse();

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("The S3 script location does not exist"));
    }

    @Test
    void validateBadLocation1() throws Exception {

        when(glueJobClientWrapper.checkLocation(any(), anyString()))
                .thenThrow(new RuntimeException("Some very bad exception"));

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockProvisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        var response = result.getResponse();

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString()
                .contains(
                        "An unexpected error occurred while validating the script location: Some very bad exception"));
    }

    @Test
    void provisionBadDescriptor1() throws Exception {

        when(glueJobClientWrapper.checkLocation(any(), anyString())).thenReturn(true);

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_bad1.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockProvisionEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        var response = result.getResponse();
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString()
                .contains("The specific.storageAreaId field does not match any component in the descriptor"));
    }
}
