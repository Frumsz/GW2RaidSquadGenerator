package com.crossroadsinn.problem;

import com.crossroadsinn.settings.Squads;
import com.crossroadsinn.signups.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
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

    List<Player> playersToSort;
    List<List<Player>> squads;
	ArrayList<String> squadTypes;

    /**
     * Constructors.
     * Since Player objects will not be modified throughout this CSP,
     * Shallow copies of lists can be created keeping references to
     * the original Player objects.
     */
    public SquadComposition(List<Player> playersToSort, List<List<Player>> squads, ArrayList<String> squadTypes) {
        // Shallow copy lists.
        this.playersToSort = new ArrayList<>(playersToSort);
        this.squads = squads.stream().map(ArrayList::new).collect(Collectors.toList());
		this.squadTypes = squadTypes;
    }

    public SquadComposition(SquadComposition other) {
        // Shallow copy constructor.
        this.playersToSort = new ArrayList<>(other.getPlayersToSort());
        this.squads = other.getSquads().stream().map(ArrayList::new).collect(Collectors.toList());
		this.squadTypes = other.squadTypes;
    }

    public List<Player> getPlayersToSort() {
        return playersToSort;
    }

    public List<List<Player>> getSquads() {
        return squads;
    }

    /**
     * Place first player of list into squad at given index.
     * @param squadIndex The index of the squad.
     * @return Whether the resulting state of the CSP satisfies all constraints.
     */
    public boolean addPlayersToSquad(int squadIndex) {
        List<Player> squad = squads.get(squadIndex);
        if (squads.get(squadIndex).size() == 10) {
            // Don't have to add anyone to this squad
            return false;
        }

        // Build up the requirements for this squad type, can optimize this to do only once instead of 10 times per squad, but whatever
        LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<>(Squads.getSquad(squadTypes.get(squadIndex)).getReqSpecialRoles());
        LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<>(Squads.getSquad(squadTypes.get(squadIndex)).getReqBoons());

        //remove every special role for each player and check if there is too many of said role
        //same for boons
        for (Player player:squad) {
            reqSpecialRoles.replaceAll((r, v) -> reqSpecialRoles.get(r) - player.getAssignedRoleObj().getIfRole(r));
            reqBoons.replaceAll((k, v) -> reqBoons.get(k) - player.getAssignedRoleObj().getBoonAmount(k));
        }

        for (Map.Entry<String, Integer> specialRole : reqSpecialRoles.entrySet()) {
            if (specialRole.getValue() > 0) {
                // We need a special role player for this role
                List<Player> eligablePlayers = playersToSort.stream()
                        .filter(p -> p.getAssignedRoleObj().getSpecialRoles().contains(specialRole.getKey())
                                && p.getAssignedRoleObj().getSpecialRoles().stream().allMatch(playerSpecialRole -> reqSpecialRoles.get(playerSpecialRole) > 0)).collect(Collectors.toList());
                if (eligablePlayers.size() == 0) {
                    // We have a problem here??
                    System.out.println("Crash in autofill?");
                    throw new IllegalStateException("Not enough players??");
                }
                squad.add(playersToSort.remove(playersToSort.indexOf(eligablePlayers.get(0))));
                // and get stop adding players
                return true;
            }
        }
        for (Map.Entry<String, Integer> boonRole : reqBoons.entrySet()) {
            if (boonRole.getValue() > 0) {
                // We need a special role player for this role
                List<Player> eligablePlayers = playersToSort.stream()
                        .filter(p -> p.getAssignedRoleObj().getBoons().containsKey(boonRole.getKey()) && p.getAssignedRoleObj().getBoons().get(boonRole.getKey()) <= boonRole.getValue())
                        .collect(Collectors.toList());
                if (eligablePlayers.size() == 0) {
                    // We have a problem here??
                    throw new IllegalStateException("Not enough players??");
                }
                squad.add(playersToSort.remove(playersToSort.indexOf(eligablePlayers.get(0))));
                // and get stop adding players
                return true;
            }
        }
        // Boons and special roles filled, check if we have enough commanders and still have leftover to fill up
        if (squad.stream().filter(Player::isTrainer).count() < 2 && playersToSort.stream().anyMatch(Player::isTrainer)) {
            List<Player> eligableDPSTrainers = playersToSort.stream().filter(p -> p.getAssignedRoleObj().getDPS() > 0 && p.isTrainer()).collect(Collectors.toList());
            squad.add(playersToSort.remove(playersToSort.indexOf(eligableDPSTrainers.get(0))));
            return true;
        }

        // Fill rest with dps
        List<Player> eligableDPSTrainees = playersToSort.stream().filter(p -> p.getAssignedRoleObj().getDPS() > 0 && !p.isTrainer()).collect(Collectors.toList());
        squad.add(playersToSort.remove(playersToSort.indexOf(eligableDPSTrainees.get(0))));
        return true;
    }

    /**
     * Apply the transition function and return any valid children.
     * @return The valid children of this state.
     */
    public List<SquadComposition> getChildren() {
        ArrayList<SquadComposition> children = new ArrayList<>();
        for (int i = 0; i < squads.size(); ++i) {
            SquadComposition copy = new SquadComposition(this);
            if (copy.addPlayersToSquad(i)) {
                children.add(copy);
            }
        }
        Collections.shuffle(children);
        return children;
    }

    public int heuristic() {
        // Spots left to fill.
        return (squads.size() * 10) - squads.stream().mapToInt(List::size).sum();
    }

    public boolean isSolution() {
        return (heuristic() == 0);
    }
}
