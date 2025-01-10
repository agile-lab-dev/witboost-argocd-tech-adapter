package it.agilelab.witboost.provisioning.argocd.configuration;

import static io.vavr.API.Some;
import static org.junit.jupiter.api.Assertions.*;

import com.witboost.provisioning.framework.service.ComponentClassProvider;
import com.witboost.provisioning.framework.service.SpecificClassProvider;
import com.witboost.provisioning.model.Workload;
import it.agilelab.witboost.provisioning.argocd.model.application.ArgoCDApplicationSpecific;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClassProviderConfigurationTest {

    @Autowired
    private ComponentClassProvider componentClassProvider;

    @Autowired
    private SpecificClassProvider specificClassProvider;

    @Test
    void testComponentClassProvider() {
        // Verify that the `componentClassProvider` bean is correctly created
        assertNotNull(componentClassProvider, "ComponentClassProvider should not be null");

        // Check that the default class is set to Workload.class
        assertEquals(
                Some(Workload.class),
                componentClassProvider.get("any"),
                "The default class for ComponentClassProvider should be Workload.class");
    }

    @Test
    void testSpecificClassProvider() {
        // Verify that the `specificClassProvider` bean is correctly created
        assertNotNull(specificClassProvider, "SpecificClassProvider should not be null");

        // Check that the default specific class is set to ArgoCDApplicationSpecific.class
        assertEquals(
                Some(ArgoCDApplicationSpecific.class),
                specificClassProvider.get("any"),
                "The default specific class for SpecificClassProvider should be ArgoCDApplicationSpecific.class");
    }
}
