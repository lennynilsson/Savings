package se.bylenny.savings.models.internal;

import android.net.Uri;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.net.URL;

import se.bylenny.savings.models.DataModel;

@DatabaseTable(tableName = "Users")
public class User implements DataModel {

    @DatabaseField(id = true, columnName = "_id")
    private Integer id;

    @DatabaseField(canBeNull = true)
    private String displayName;

    @DatabaseField(canBeNull = true)
    private String avatarUri;

    @DatabaseField(foreign = true, foreignAutoRefresh=true,
            canBeNull=true, maxForeignAutoRefreshLevel = 1)
    private SavingsGoal savingsGoal;

    public User() {
    }

    public SavingsGoal getSavingsGoal() {
        return savingsGoal;
    }

    public void setSavingsGoal(SavingsGoal savingsGoal) {
        this.savingsGoal = savingsGoal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }
}
