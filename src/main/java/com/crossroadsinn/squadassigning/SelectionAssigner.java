package com.crossroadsinn.squadassigning;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.signups.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectionAssigner {

    /**
     * We filter based on boons, special roles and dps quite some times
     * To safe on computation time, we split the players between specialRoles, boons and dpsPlayers
     * NOTE: a player will only be in 1 of the lists, even though they could fit all 3 roles
     * A player will be assigned in this order -> specialRoles, boons, dps
     */
    static class PlayerPool {
        private final Map<Player, Role> specialRolePlayers;
        private final Map<Player, Role> boonPlayers;
        private final Map<Player, Role> dpsPlayers;

        public PlayerPool(Map<Player, Role> specialRolePlayers, Map<Player, Role> boonPlayers, Map<Player, Role> dpsPlayers) {
            this.boonPlayers = boonPlayers;
            this.specialRolePlayers = specialRolePlayers;
            this.dpsPlayers = dpsPlayers;
        }

        public PlayerPool(Map<Player, Role> players) {
            this.boonPlayers = new HashMap<>();
            this.specialRolePlayers = new HashMap<>();
            this.dpsPlayers = new HashMap<>();
            for(Map.Entry<Player, Role> player: players.entrySet()) {
                if (player.getValue().getSpecialRoles().stream().anyMatch(r -> !r.equalsIgnoreCase("dps"))) {
                    this.specialRolePlayers.put(player.getKey(), player.getValue());
                } else if (player.getValue().getBoons().size() > 0) {
                    this.boonPlayers.put(player.getKey(), player.getValue());
                } else {
                    this.dpsPlayers.put(player.getKey(), player.getValue());
                }
            }
        }

        // TODO these methods are too much alike, deduplicate
        public PlayerPool copyWithoutSpecialRole(Player p) {
            Map<Player, Role> specialRolePlayersCopy = new HashMap<>(specialRolePlayers);
            specialRolePlayersCopy.remove(p);
            return new PlayerPool(specialRolePlayersCopy, boonPlayers, dpsPlayers);
        }

        public PlayerPool copyWithoutBoon(Player p) {
            Map<Player, Role> boonPlayersCopy = new HashMap<>(boonPlayers);
            boonPlayersCopy.remove(p);
            return new PlayerPool(specialRolePlayers, boonPlayersCopy, dpsPlayers);
        }
        public PlayerPool copyWithoutDpsPlayer(Player p) {
            Map<Player, Role> dpsPlayersCopy = new HashMap<>(dpsPlayers);
            dpsPlayersCopy.remove(p);
            return new PlayerPool(specialRolePlayers, boonPlayers, dpsPlayersCopy);
        }

        public boolean isEmpty() {
            return specialRolePlayers.isEmpty() && boonPlayers.isEmpty() && dpsPlayers.isEmpty();
        }
    }

    private final List<PlayerSquad> playerSquads;
    private final PlayerPool playerPool;

    public static List<PlayerSquad> buildInitialState(int squadCount, String squadType) {
        List<PlayerSquad> initialState = new ArrayList<>();
        for(int i = 0; i < squadCount; i++){
            initialState.add(new PlayerSquad(squadType));
        }
        return initialState;
    }

    public SelectionAssigner(Map<Player, Role> players, int squadCount, String squadType) {
        this(new PlayerPool(players), buildInitialState(squadCount, squadType));
    }

    public SelectionAssigner(PlayerPool playerPool, List<PlayerSquad> playerSquads) {
        this.playerPool = playerPool;
        this.playerSquads = playerSquads;
    }

    public List<List<Player>> getPlayersFlattened() {
        ArrayList<List<Player>> squads = new ArrayList<>();
        for(PlayerSquad sq: playerSquads) {
            squads.add(new ArrayList<>(sq.getPlayers()));
        }
        return squads;
    }

    public SelectionAssigner makeCopy(PlayerPool playerPool, int squadModified, PlayerSquad newSquad) {
        List<PlayerSquad> playerSquadsCopy = new ArrayList<>(playerSquads);

        playerSquadsCopy.set(squadModified, newSquad);

        return new SelectionAssigner(playerPool, playerSquadsCopy);
    }

    /**
     * Auto assign all players to a squad, if we don't have enough players it will return partial squads
     * This doesn't check anything, it simply attempts to assign players, the PlayerSquad will check if it possible
     * @return List of filled squads or null if not possible to assign all players
     */
    public SelectionAssigner assignToSquads() {
        if (playerPool.isEmpty()) {
            // no-one left to assign, we are done
            return this;
        }

        // TODO fix this copy paste fest here
        if (playerPool.specialRolePlayers.size() > 0) {
            for(int i = 0; i < playerSquads.size(); i++) {
                PlayerSquad currentSquad = playerSquads.get(i);
                if (currentSquad.needsSpecialRoles()) {
                    for (Map.Entry<Player, Role> p : playerPool.specialRolePlayers.entrySet()) {
                        PlayerSquad squadWithPlayer = currentSquad.assignPlayer(p);
                        if (squadWithPlayer != null) {
                            // we are able to assign the player
                            return makeCopy(playerPool.copyWithoutSpecialRole(p.getKey()), i, squadWithPlayer).assignToSquads();
                        }
                    }
                }
            }
        }

        if (playerPool.boonPlayers.size() > 0) {
            for(int i = 0; i < playerSquads.size(); i++) {
                PlayerSquad currentSquad = playerSquads.get(i);
                if (currentSquad.needsBoonPlayers()) {
                    for (Map.Entry<Player, Role> p : playerPool.boonPlayers.entrySet()) {
                        PlayerSquad squadWithPlayer = currentSquad.assignPlayer(p);
                        if (squadWithPlayer != null) {
                            // we are able to assign the player
                            return makeCopy(playerPool.copyWithoutBoon(p.getKey()), i, squadWithPlayer).assignToSquads();
                        }
                    }
                }
            }
        }

        if (playerPool.dpsPlayers.size() > 0) {
            for(int i = 0; i < playerSquads.size(); i++) {
                PlayerSquad currentSquad = playerSquads.get(i);
                if (!currentSquad.isFilled()) {
                    for (Map.Entry<Player, Role> p : playerPool.dpsPlayers.entrySet()) {
                        PlayerSquad squadWithPlayer = currentSquad.assignPlayer(p);
                        if (squadWithPlayer != null) {
                            // we are able to assign the player
                            return makeCopy(playerPool.copyWithoutDpsPlayer(p.getKey()), i, squadWithPlayer).assignToSquads();
                        }
                    }
                }
            }
        }

        // All combinations failed, so it is not possible to create squads with these players
        return null;
    }
}
