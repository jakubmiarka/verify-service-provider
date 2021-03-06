package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslatedNonMatchingResponseBody implements TranslatedResponseBody {

    @JsonProperty("scenario")
    private final NonMatchingScenario scenario;
    @JsonProperty("pid") @JsonInclude(value = JsonInclude.Include.NON_NULL)
    protected final String pid;
    @JsonProperty("levelOfAssurance") @JsonInclude(value = JsonInclude.Include.NON_NULL)
    protected final LevelOfAssurance levelOfAssurance;
    @JsonProperty("attributes") @JsonInclude(value = JsonInclude.Include.NON_NULL)
    protected final NonMatchingAttributes attributes;

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
        this.scenario = scenario;
        this.pid = pid;
        this.levelOfAssurance = levelOfAssurance;
        this.attributes = attributes;
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    public String getPid() {
        return pid;
    }

    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public NonMatchingAttributes getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslatedNonMatchingResponseBody that = (TranslatedNonMatchingResponseBody) o;

        if (scenario != that.scenario) return false;
        if (pid != null ? !pid.equals(that.pid) : that.pid != null) return false;
        if (levelOfAssurance != that.levelOfAssurance) return false;
        return attributes != null ? attributes.equals(that.attributes) : that.attributes == null;
    }

    @Override
    public int hashCode() {
        int result = scenario != null ? scenario.hashCode() : 0;
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        result = 31 * result + (levelOfAssurance != null ? levelOfAssurance.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TranslatedMatchingResponseBody{" +
                "scenario='" + scenario + '\'' +
                ", pid='" + pid + '\'' +
                ", levelOfAssurance=" + levelOfAssurance +
                ", attributes=" + attributes +
                '}';
    }

}
