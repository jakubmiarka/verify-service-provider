package uk.gov.ida.verifyserviceprovider.compliance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import org.joda.time.DateTime;

public class MatchingAttribute {
    @NotNull
    @JsonProperty
    private String value;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private DateTime from;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private DateTime to;
    @NotNull
    @JsonProperty
    private boolean verified;

    public MatchingAttribute() {};

    public String getValue() {
        return value;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }

    public MatchingAttribute(
            final String value,
            final boolean verified,
            final DateTime from,
            final DateTime to) {

        this.value = value;
        this.verified = verified;
        this.from = from;
        this.to = to;
    }
}