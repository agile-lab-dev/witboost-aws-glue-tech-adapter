package com.witboost.provisioning.glue.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.witboost.provisioning.glue.utils.GlueJobUtils;
import com.witboost.provisioning.model.Component;
import com.witboost.provisioning.model.DataProduct;
import com.witboost.provisioning.model.Workload;
import org.junit.jupiter.api.Test;

public class GlueJobUtilsTests {

    @Test
    void jobNameCreation() {

        DataProduct dp = new DataProduct();
        Component cp = new Workload();
        cp.setId("urn:dmb:cmp:mydomain:awesomedp:1:ingestion");
        dp.setEnvironment("dev");

        String jobName = GlueJobUtils.computeName(dp, cp);
        assertEquals("job-mydomain-awesomedp-dev-1-ingestion", jobName);
    }
}
