package com.witboost.provisioning.glue.utils;

public class VersionUtils {
    /**
     * Extracts the major version from a Semantic Versioning (semver) string.
     *
     * @param version the semver string (e.g., "1.2.3")
     * @return the major version as an integer
     * @throws IllegalArgumentException if the input is not a valid semver
     */
    public static String getMajorVersion(String version) {

        String[] parts = version.split("\\.");
        if (parts.length < 1 || !parts[0].matches("\\d+")) {
            throw new IllegalArgumentException("Invalid semantic versioning format: " + version);
        }

        return parts[0];
    }
}
