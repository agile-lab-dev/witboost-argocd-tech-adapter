package it.agilelab.witboost.provisioning.argocd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.witboost.provisioning.model.Specific;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.client.model.V1ObjectMeta;
import org.openapitools.client.model.V1alpha1ApplicationSpec;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationSpecific extends Specific {
    V1ObjectMeta metadata;
    V1alpha1ApplicationSpec spec;
}
