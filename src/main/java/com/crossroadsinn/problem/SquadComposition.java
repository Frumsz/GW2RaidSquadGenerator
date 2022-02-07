package com.crossroadsinn.problem;

import com.crossroadsinn.settings.Roles;
import com.crossroadsinn.settings.Squads;
import com.crossroadsinn.signups.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Given an initial state and a list of players to sort,
 * Fill squads with players to sort until all squads contain
 * 10 players and constraints are satisfied.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class SquadComposition {

    List<Player> playersToAssign;
    List<List<Player>> squads;
	String squadType;

    /**
     * Constructors.
     * Since Player objects will not be modified throughout this CSP,
     * Shallow copies of lists can be created keeping references to
     * the original Player objects.
     */
    public SquadComposition(List<Player> playersToSort, List<List<Player>> squads, String squadType) {
        // Shallow copy lists.
        this.playersToAssign = new ArrayList<>(playersToSort);
        this.squads = squads.stream().map(ArrayList::new).collect(Collectors.toList());
		this.squadType = squadType;
    }

    public SquadComposition(SquadComposition other) {
        // Shallow copy constructor.
        this.playersToAssign = new ArrayList<>(other.getPlayersToAssign());
        this.squads = other.getSquads().stream().map(ArrayList::new).collect(Collectors.toList());
		this.squadType = other.squadType;
    }

    public List<Player> getPlayersToAssign() {
        return playersToAssign;
    }

    public List<List<Player>> getSquads() {
        return squads;
    }

    /**
     * Place first player of list into squad at given index.
     * @return Whether the resulting state of the CSP satisfies all constraints.
     * TODO this currently assumes trainers on DPS to fill them up 2 each in squads, which means it doesn't always do that correctly
     */
    public boolean addPlayersToNextSquad(int squadIndex) {
        List<Player> squad = squads.get(squadIndex);
        if (squads.get(squadIndex).size() == 10) {
            // Don't have to add anyone to this squad
            return false;
        }

        // Build up the requirements for this squad type, can optimize this to do only once instead of 10 times per squad, but whatever
        LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<>(Squads.getSquad(squadType).getReqSpecialRoles());
        LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<>(Squads.getSquad(squadType).getReqBoons());

        //remove every special role for each player and check if there is too many of said role
        //same for boons
        for (Player player:squad) {
            reqSpecialRoles.replaceAll((r, v) -> reqSpecialRoles.get(r) - player.getAssignedRoleObj().getIfRole(r));
            reqBoons.replaceAll((k, v) -> reqBoons.get(k) - player.getAssignedRoleObj().getBoonAmount(k));
        }

        for (Map.Entry<String, Integer> specialRole : reqSpecialRoles.entrySet()) {
            if (specialRole.getValue() > 0) {
                // We need a special role player for this role
                List<Player> eligablePlayers = playersToAssign.stream()
                        .filter(p -> p.getAssignedRoleObj().getSpecialRoles().contains(specialRole.getKey())
                                && p.getAssignedRoleObj().getSpecialRoles().stream().allMatch(playerSpecialRole -> {
                                    // We still need to fill this role OR it is overflowable
                                    return reqSpecialRoles.getOrDefault(playerSpecialRole, 0) > 0 || Roles.getOverflowableRoles().contains(playerSpecialRole);
                        })).collect(Collectors.toList());
                if (eligablePlayers.size() == 0) {
                    // TODO we can probably prevent this
                    throw new InvalidSolutionException("Filled up wrong, cannot fill up all squads");
                }
                squad.add(playersToAssign.remove(playersToAssign.indexOf(eligablePlayers.get(0))));
                // and get stop adding players
                return true;
            }
        }
        for (Map.Entry<String, Integer> boonRole : reqBoons.entrySet()) {
            if (boonRole.getValue() > 0) {
                // We need a boon player that fills this boon
                List<Player> eligablePlayers = playersToAssign.stream()
                        .filter(p -> {
                            // First check if the assigned boons match what we still need, don't want an 10 alac player when you only need 5 for example
                            return p.getAssignedRoleObj().getBoons().containsKey(boonRole.getKey()) && p.getAssignedRoleObj().getBoons().get(boonRole.getKey()) <= boonRole.getValue() &&
                            // But also check if this boon role doesn't fill a special role, prevent a quickness heal scrapper from overfilling the heal role for example
                                    (p.getAssignedRoleObj().getSpecialRoles().isEmpty() || Roles.getOverflowableRoles().containsAll(p.getAssignedRoleObj().getSpecialRoles()));
                        })
                        .collect(Collectors.toList());
                if (eligablePlayers.size() == 0) {
                    // TODO we can probably prevent this
                    throw new InvalidSolutionException("Filled up wrong, cannot fill up all squads");
                }
                squad.add(playersToAssign.remove(playersToAssign.indexOf(eligablePlayers.get(0))));
                // and get stop adding players
                return true;
            }
        }
        // Boons and special roles filled, check if we have enough commanders and still have leftover to fill up
        if (squad.stream().filter(Player::isTrainer).count() < 2 && playersToAssign.stream().anyMatch(Player::isTrainer)) {
            List<Player> eligableDPSTrainers = playersToAssign.stream().filter(p -> p.getAssignedRoleObj().getDPS() > 0 && p.isTrainer()).collect(Collectors.toList());
            if (eligableDPSTrainers.size() > 0) {
                squad.add(playersToAssign.remove(playersToAssign.indexOf(eligableDPSTrainers.get(0))));
                return true;
            }
        }

        // Fill rest with dps
        List<Player> eligableDPSTrainees = playersToAssign.stream().filter(p -> p.getAssignedRoleObj().getDPS() > 0 && !p.isTrainer()).collect(Collectors.toList());
        if (eligableDPSTrainees.size() > 0) {
            squad.add(playersToAssign.remove(playersToAssign.indexOf(eligableDPSTrainees.get(0))));
            return true;
        }
        return false;
    }

    /**
     * Apply the transition function and return any valid children.
     * @return The valid children of this state.
     */
    public List<SquadComposition> getChildren() {
        ArrayList<SquadComposition> children = new ArrayList<>();
        for (int i = 0; i < squads.size(); i++) {
            SquadComposition copy = new SquadComposition(this);
            if (copy.addPlayersToNextSquad(i)) {
                children.add(copy);
            }
        }
        return children;
    }

    public int heuristic() {
        // Spots left to fill.
        return playersToAssign.size();
    }

    public boolean isSolution() {
        return playersToAssign.isEmpty();
    }
}
