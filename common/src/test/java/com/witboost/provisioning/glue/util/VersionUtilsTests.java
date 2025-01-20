package com.witboost.provisioning.glue.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.witboost.provisioning.glue.utils.VersionUtils;
import org.junit.jupiter.api.Test;

public class VersionUtilsTests {
    @Test
    void extractMajorVersion() {
        String version = "999.0.0-SNAPSHOT";
        String maj = VersionUtils.getMajorVersion(version);
        assertEquals("999", maj);
    }

    @Test
    void extractMajorVersion_fail0() {
        assertThrows(IllegalArgumentException.class, () -> {
            VersionUtils.getMajorVersion("99900@SNAPSHOT");
        });
    }
}
