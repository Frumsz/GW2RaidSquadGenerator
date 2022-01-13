package com.crossroadsinn.signups;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.settings.Roles;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class that can hold information about a player.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class Player {

    private final String gw2Account;
    private final String discordName;
    private final String discordPing;
    private final String tier;
    private final String comments;
    private final String[] bossLvlChoice;
    private Set<Role> roles;
    private final SimpleStringProperty assignedRole = new SimpleStringProperty();
    private ChangeListener<String> assignedRoleListener;
	private Role assignedRoleObj;
    private boolean isTrainer = false;

    public Player(String gw2Account, String discordName, String discordPing, String tier, String comments, Set<Role> roles, String[] bossLvlChoice) {
        this.gw2Account = gw2Account;
        this.discordName = discordName;
        this.discordPing = discordPing;
        this.tier = tier;
        this.comments = comments;
        this.roles = roles;
        this.bossLvlChoice = bossLvlChoice;
    }
	
    public Player(String gw2Account, String discordName, String tier, String comments, Set<Role> roles, String[] bossLvlChoice) {
        this.gw2Account = gw2Account;
        this.discordName = discordName;
        this.discordPing = "@" + discordName;
        this.tier = tier;
        this.comments = comments;
        this.roles = roles;
        this.bossLvlChoice = bossLvlChoice;
    }

    public Player(Player player) {
        this(player.getGw2Account(), player.getDiscordName(), player.getDiscordPing(), player.getTier(), player.getComments(), player.getRoles(), player.getBossLvlChoice());
    }

    public String toString() {
        return assignedRole.get() == null ? getName() : String.format("%s - %s", getName(), assignedRoleObj.getRoleName());
    }

    public String getGw2Account() {
        return gw2Account;
    }

    public String getDiscordName() {
        return discordName;
    }
	
    public String getDiscordPing() {
        return discordPing;
    }

    public String getTier() {
        return tier;
    }

    public String getComments() {
        return comments;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void resetAssignedRole() {
        this.assignedRoleObj = null;
        this.assignedRole.set(null);
    }
	
	public void setAssignedRole(Role role) {
		this.assignedRoleObj = role;
		this.assignedRole.set(role.getRoleHandle());
	}

    public Role getAssignedRoleObj() {
        return assignedRoleObj;
    }

    public String[] getBossLvlChoice() {
        return bossLvlChoice;
    }

    public String getBossLvlChoiceAsString() {
        return String.join(", ",bossLvlChoice);
    }

    public Set<String> getRolenameList() {
        return roles.stream().map(Role::getRoleName).collect(Collectors.toSet());
    }

    public Set<String> getRolehandleList() {
        return roles.stream().map(Role::getRoleHandle).collect(Collectors.toSet());
    }

    public void setRoleListener(ChangeListener<String> changeListener) {
        assignedRoleListener = changeListener;
        assignedRole.addListener(changeListener);
    }

    public void clearRoleListener() {
        assignedRole.removeListener(assignedRoleListener);
        assignedRoleListener = null;
    }

    public String getName() {
        return gw2Account.isBlank() ? discordName : gw2Account;
    }

    public boolean isTrainer() {
        return isTrainer;
    }

    public void setTrainer(boolean trainer) {
        isTrainer = trainer;
    }
}
