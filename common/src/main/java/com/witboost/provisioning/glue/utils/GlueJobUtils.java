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
        return new StringJoiner("-")
                .add("job")
                .add(dp.getDomain())
                .add(dp.getName())
                .add(dp.getEnvironment())
                .add(VersionUtils.getMajorVersion(dp.getVersion()))
                .add(cp.getName())
                .toString();
    }
}
