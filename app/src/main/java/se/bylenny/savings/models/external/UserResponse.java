package se.bylenny.savings.models.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {

    public Integer userId;      //Unique user id
    public String displayName;  //Display name of user
    public String avatarUrl;       //URL where the users avatar can be downloaded from

    public UserResponse() {

    }
}
