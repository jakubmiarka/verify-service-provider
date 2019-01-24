package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslatedNonMatchingResponseBody extends TranslatedResponseBody {
    @JsonCreator
    public TranslatedNonMatchingResponseBody(
            @JsonProperty("scenario")
            NonMatchingScenario scenario,
            @JsonProperty("pid")
            String pid,
            @JsonProperty("levelOfAssurance")
            LevelOfAssurance levelOfAssurance,
            @JsonProperty("attributes")
            NonMatchingAttributes attributes
    ) {
        super(scenario, pid, levelOfAssurance, attributes);
    }

}
