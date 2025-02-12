package com.witboost.provisioning.glue.service.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.witboost.provisioning.framework.openapi.model.DescriptorKind;
import com.witboost.provisioning.framework.openapi.model.ProvisioningRequest;
import com.witboost.provisioning.glue.util.ResourceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class WorkloadValidationServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String mockValidateEndpoint = "http://127.0.0.1:8888/v1/validate";

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateCorrectDescriptor0() throws Exception {

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_ok0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void validateBadDescriptor0() throws Exception {

        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_bad0.yml");
        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        var response = result.getResponse();
        var responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("The specified workerType is unknown"));
        assertTrue(responseContent.contains("The specified region is unknown"));
    }

    @Test
    public void validateBadDescriptor2() throws Exception {
        String ymlDescriptor = ResourceUtils.getContentFromResource("/pr_descriptor_bad2.yml");

        ProvisioningRequest provisioningRequest =
                new ProvisioningRequest(DescriptorKind.COMPONENT_DESCRIPTOR, ymlDescriptor, false);

        MvcResult result = mockMvc.perform(post(mockValidateEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provisioningRequest)))
                .andReturn();

        assertEquals(400, result.getResponse().getStatus());
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("warehouseLocation must not be blank"));
    }
}
