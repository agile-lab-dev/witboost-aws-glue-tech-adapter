package com.witboost.provisioning.glue.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vavr.control.Either;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GlueSpecificTests {

    @Test
    void checkIsRegionInvalid() {

        GlueSpecific specific = new GlueSpecific();
        specific.setExecutionClass("FLEX");
        specific.setWorkerType("G.1X");
        specific.setRegion("eu-invalid-1");

        Either<List<String>, Void> res = specific.isValid();

        assertTrue(res.isLeft());
        assertEquals(1, res.getLeft().size());
        assertEquals(
                "The specified region is unknown",
                res.getLeft().stream().findFirst().get());

        ;
    }

    @Test
    void checkIsExecutionClassInvalid() {

        GlueSpecific specific = new GlueSpecific();
        specific.setExecutionClass("INVALID");
        specific.setWorkerType("G.1X");
        specific.setRegion("eu-central-1");

        Either<List<String>, Void> res = specific.isValid();

        assertTrue(res.isLeft());
        assertEquals(1, res.getLeft().size());
        assertEquals(
                "The specified executionClass is unknown",
                res.getLeft().stream().findFirst().get());

        ;
    }

    @Test
    void checkIsWorkerTypeInvalid() {

        GlueSpecific specific = new GlueSpecific();
        specific.setExecutionClass("FLEX");
        specific.setWorkerType("G.1.X");
        specific.setRegion("eu-central-1");

        Either<List<String>, Void> res = specific.isValid();

        assertTrue(res.isLeft());
        assertEquals(1, res.getLeft().size());
        assertEquals(
                "The specified workerType is unknown",
                res.getLeft().stream().findFirst().get());

        ;
    }

    @Test
    void checkIsWorkerTypeAndRegionInvalid() {

        GlueSpecific specific = new GlueSpecific();
        specific.setExecutionClass("FLEX");
        specific.setWorkerType("G.1.X");
        specific.setRegion("eu-INVALID-1");

        Either<List<String>, Void> res = specific.isValid();

        assertTrue(res.isLeft());
        assertEquals(2, res.getLeft().size());

        ;
    }
}
