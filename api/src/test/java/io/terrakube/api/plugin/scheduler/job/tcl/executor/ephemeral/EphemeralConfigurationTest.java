package io.terrakube.api.plugin.scheduler.job.tcl.executor.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/**
 * Exercises the real Spring property-binding path (not the plain setters) to prove that
 * the empty `${EnvVar:}` placeholder defaults used for the optional fields in
 * application.properties bind to null rather than failing conversion at startup.
 */
public class EphemeralConfigurationTest {

    private EphemeralConfiguration bind(Map<String, String> properties) {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source);
        return binder.bind("io.terrakube.executor.ephemeral", Bindable.of(EphemeralConfiguration.class))
                .orElseGet(EphemeralConfiguration::new);
    }

    @Test
    public void emptyStringDefaultsBindToNull() {
        Map<String, String> properties = new LinkedHashMap<>();
        // Mirrors what ${ExecutorEphemeralActiveDeadlineSeconds:} and ${ExecutorEphemeralBackoffLimit:}
        // resolve to in application.properties when the env var is unset: an empty string.
        properties.put("io.terrakube.executor.ephemeral.activeDeadlineSeconds", "");
        properties.put("io.terrakube.executor.ephemeral.backoffLimit", "");
        properties.put("io.terrakube.executor.ephemeral.ttlSecondsAfterFinished", "30");

        EphemeralConfiguration config = bind(properties);

        assertNull(config.getActiveDeadlineSeconds());
        assertNull(config.getBackoffLimit());
        assertEquals(30, config.getTtlSecondsAfterFinished());
    }

    @Test
    public void populatedValuesBindCorrectly() {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("io.terrakube.executor.ephemeral.activeDeadlineSeconds", "3600");
        properties.put("io.terrakube.executor.ephemeral.backoffLimit", "2");
        properties.put("io.terrakube.executor.ephemeral.ttlSecondsAfterFinished", "60");

        EphemeralConfiguration config = bind(properties);

        assertEquals(3600L, config.getActiveDeadlineSeconds());
        assertEquals(2, config.getBackoffLimit());
        assertEquals(60, config.getTtlSecondsAfterFinished());
    }
}
