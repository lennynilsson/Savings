package se.bylenny.savings.models.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsGoalsResponse {

    public List<SavingsGoalResponse> savingsGoals;

    public SavingsGoalsResponse() {
    }
}
