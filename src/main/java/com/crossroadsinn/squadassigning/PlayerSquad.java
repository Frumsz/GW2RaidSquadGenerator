package com.crossroadsinn.squadassigning;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.settings.Roles;
import com.crossroadsinn.signups.Player;

import java.util.*;

public class PlayerSquad {

    private LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<>();
    private List<String> forbiddenRoles = new ArrayList<>();

    private final HashMap<Player, Role> players;

    public PlayerSquad(String squadType) {
        SquadUtilities.buildSquadRequirements(squadType, reqBoons, reqSpecialRoles);
        players = new HashMap<>();
    }

    private PlayerSquad(LinkedHashMap<String, Integer> reqBoonsCopy, LinkedHashMap<String, Integer> reqSpecialRolesCopy, List<String> forbiddenRoles, HashMap<Player, Role> playerListCopy) {
        this.reqBoons = reqBoonsCopy;
        this.reqSpecialRoles = reqSpecialRolesCopy;
        this.forbiddenRoles = forbiddenRoles;
        this.players = playerListCopy;
    }

    public boolean needsSpecialRoles() {
        return reqSpecialRoles.size() > 0;
    }

    public boolean needsBoonPlayers() {
        return reqBoons.size() > 0;
    }

    public boolean isFilled() {
        return players.size() == 10;
    }

    public Set<Player> getPlayers(){
        return players.keySet();
    }

    private PlayerSquad copyWithPlayerAssigned(Player player, Role role) {
        LinkedHashMap<String, Integer> reqBoonsCopy = new LinkedHashMap<>(reqBoons);
        LinkedHashMap<String, Integer> reqSpecialRolesCopy = new LinkedHashMap<>(reqSpecialRoles);

        for (Map.Entry<String, Integer> boonsProvided: role.getBoons().entrySet()) {
            String boonName = boonsProvided.getKey();
            int boonAmount = boonsProvided.getValue();
            if (reqBoonsCopy.containsKey(boonName)) {
                int newValue = reqBoonsCopy.get(boonName) - boonAmount;
                if (newValue > 0) {
                    reqBoonsCopy.put(boonName, newValue);
                } else {
                    reqBoonsCopy.remove(boonName);
                }
            }
        }
        for (String specialRole: role.getSpecialRoles()) {
            if (reqSpecialRolesCopy.containsKey(specialRole)) {
                int newValue = reqSpecialRolesCopy.get(specialRole) - 1;
                if (newValue > 0) {
                    reqSpecialRolesCopy.put(specialRole, newValue);
                } else {
                    reqSpecialRolesCopy.remove(specialRole);
                }
            }
        }
        HashMap<Player, Role> playerListCopy = new HashMap<>(players);

        playerListCopy.put(player, role);

        return new PlayerSquad(reqBoonsCopy, reqSpecialRolesCopy, forbiddenRoles, playerListCopy);
    }

    private boolean stillNeedsOtherThanDPS() {
        return
            reqBoons.entrySet().stream().anyMatch(item -> item.getValue() > 0) ||
            reqSpecialRoles.entrySet().stream().anyMatch(item -> item.getValue() > 0);
    }

    private boolean isBoonOrSpecialRoleProvider(Role role) {
        return role.getSpecialRoles().size() > 0 || role.getBoons().entrySet().size() > 0;
    }

    /**
     * Attempt to assign a player, simply check if role still fits
     * @param playerWithRole
     * @return a copy of this squad with the player, or null if not possible
     */
    public PlayerSquad assignPlayer(Map.Entry<Player,Role> playerWithRole) {
        if (players.size() == 10) {
            // Its full...
            return null;
        }
        Role role = playerWithRole.getValue();
        for (Map.Entry<String, Integer> boonsProvided: role.getBoons().entrySet()) {
            if (!reqBoons.containsKey(boonsProvided.getKey()) || reqBoons.get(boonsProvided.getKey()) < boonsProvided.getValue()) {
                // We don't need this boon, or we need less of the boon than you provide
                return null;
            }
        }
        for (String specialRole: role.getSpecialRoles()) {
            if (!Roles.getOverflowableRoles().contains(specialRole) && (!reqSpecialRoles.containsKey(specialRole) || reqSpecialRoles.get(specialRole) == 0)) {
                // We don't need this special role, and it is not overflowable
                return null;
            }
        }

        if (stillNeedsOtherThanDPS() && isBoonOrSpecialRoleProvider(role)) {
            return copyWithPlayerAssigned(playerWithRole.getKey(), role);
        } else if (stillNeedsOtherThanDPS()) {
            // trying to assign a player that doesn't provider any required boons/roles for this squad, need to fill those up before we fill up the rest!
            return null;
        }

        // we don't need boons, lets check if we have a player that doesn't do anything?
        if (role.getDPS() == 0) {
            System.out.println("Cannot place player at all, no boons, no special roles and no dps?");
            return null;
        }
        return copyWithPlayerAssigned(playerWithRole.getKey(), role);
    }
}
