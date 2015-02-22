package se.bylenny.savings.models.internal;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SavingsGoals")
public class SavingsGoal {

    @DatabaseField(id = true, columnName = "_id")
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String goalImageUri;

    @DatabaseField(canBeNull = true)
    private Float targetAmount;

    @DatabaseField(canBeNull = false)
    private Float currentBalance;

    @DatabaseField(canBeNull = false)
    private Status status;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(foreign = true, foreignAutoRefresh = true,
            canBeNull = true, maxForeignAutoRefreshLevel = 1)
    private User user;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<User> connectedUsers;

    public SavingsGoal() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGoalImageUri() {
        return goalImageUri;
    }

    public void setGoalImageUri(String goalImageUri) {
        this.goalImageUri = goalImageUri;
    }

    public Float getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Float targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Float getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Float currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ForeignCollection<User> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(ForeignCollection<User> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }
}
