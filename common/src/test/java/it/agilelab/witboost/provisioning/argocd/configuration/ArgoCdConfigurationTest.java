package it.agilelab.witboost.provisioning.argocd.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArgoCdConfigurationTest {

    @Autowired
    private ArgoCdConfiguration argoCdConfiguration;

    @Test
    void testConfigurationPropertiesLoaded() {
        assertNotNull(argoCdConfiguration);

        assertEquals("fake-token", argoCdConfiguration.getToken());
        assertEquals("https://fake-url.com", argoCdConfiguration.getBasePath());
    }
}
