package se.bylenny.savings.models.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsGoalResponse {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_DELETED = "deleted";

    public String goalImageURL;    //URL   where the image can be downloaded from
    public Integer userId;      //Id of the user that owns this goal
    public Float targetAmount;  //Target amount of the goal. Might be null
    public Float currentBalance; //	Current balance on the goal. How much has been saved
    public String status;       //If this goal is active or deleted. A string that is either "active" or "deleted"
    public String name;         //Name of the goal
    public Integer id;          //Unique id of the goal
    public List<Integer> connectedUsers; //A list of user ids that this goal is shared with.

    public SavingsGoalResponse() {
    }
}
