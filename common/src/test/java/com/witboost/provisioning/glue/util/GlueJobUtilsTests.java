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
        dp.setName("allcustomers");
        dp.setDomain("sales");
        dp.setVersion("1.0.0-SNAPSHOT");
        dp.setEnvironment("dev");

        Component cp = new Workload();
        cp.setName("ingestion");

        String jobName = GlueJobUtils.computeName(dp, cp);
        assertEquals("job-sales-allcustomers-dev-1-ingestion", jobName);
    }
}
