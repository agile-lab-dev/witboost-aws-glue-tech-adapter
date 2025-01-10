package com.witboost.provisioning.glue.config;

import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.glue.model.GlueSpecific;
import com.witboost.provisioning.model.Workload;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

/*
 * TODO Review these tests after you have implemented the tech adapter logic
 */
class ClassProviderBeanTest {

    ClassProviderBean classProviderBean = new ClassProviderBean();

    @Test
    void defaultSpecificProvider() {
        var specificProvider = classProviderBean.specificClassProvider();

        assertEquals(Option.of(GlueSpecific.class), specificProvider.get("a-urn"));
        assertEquals(Option.of(GlueSpecific.class), specificProvider.getReverseProvisioningParams("a-urn"));
    }

    @Test
    void defaultComponentProvider() {
        var componentProvider = classProviderBean.componentClassProvider();

        assertEquals(Option.some(Workload.class), componentProvider.get("whatever"));
    }
}
