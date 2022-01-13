package com.crossroadsinn.signups;

import com.crossroadsinn.settings.Role;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper class for a player with an Integer property that
 * determines chosen roles to allow to chose roles for
 * a commander.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class Commander extends Player {

    Set<Role> chosenRoles;
    StringProperty chosenRolesJoined;

    public Commander(String gw2Account, String discordName, String tier, String comments, Set<Role> roles, String[] ComBossLevelChoice) {
        super(gw2Account, discordName, tier, comments, roles, ComBossLevelChoice);
        chosenRoles = new HashSet<>();
        chosenRolesJoined = new SimpleStringProperty("");
    }

    public Commander(Player player) {
        this(player.getGw2Account(), player.getDiscordName(), player.getTier(), player.getComments(), player.getRoles(), player.getBossLvlChoice());
    }

    public Set<Role> getChosenRoles() {
        return chosenRoles;
    }

    public StringProperty getChosenRolesJoined() {
        return chosenRolesJoined;
    }

    public void setAllChosenRoles(List<Role> roles) {
        this.chosenRoles.clear();
        this.chosenRoles.addAll(roles);
        chosenRolesJoined.set(chosenRoles.stream().map(Role::getRoleHandle).collect(Collectors.joining(", ")));
    }

    public void addChosenRole(Role role) {
        this.chosenRoles.add(role);
        chosenRolesJoined.set(chosenRoles.stream().map(Role::getRoleHandle).collect(Collectors.joining(", ")));
    }

    public void removeChosenRole(Role role) {
        this.chosenRoles.remove(role);
        chosenRolesJoined.set(chosenRoles.stream().map(Role::getRoleHandle).collect(Collectors.joining(", ")));

    }
}
