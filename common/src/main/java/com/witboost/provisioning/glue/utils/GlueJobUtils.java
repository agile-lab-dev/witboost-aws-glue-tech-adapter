package com.witboost.provisioning.glue.utils;

import com.witboost.provisioning.model.Component;
import com.witboost.provisioning.model.DataProduct;
import java.util.StringJoiner;

public class GlueJobUtils {

    /**
     * Computes the job name from the descriptor
     * @param dp is the data product descriptor
     * @param cp is the component descriptor
     * @return the name of the job
     */
    public static String computeName(DataProduct dp, Component cp) {

        // Example component id urn:dmb:cmp:domain:dp:0:component
        String[] idComponents = cp.getId().split(":");

        return new StringJoiner("-")
                .add("job")
                .add(idComponents[3])
                .add(idComponents[4])
                .add(dp.getEnvironment())
                .add(idComponents[5])
                .add(idComponents[6])
                .toString();
    }
}
