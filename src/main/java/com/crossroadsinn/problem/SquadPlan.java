package com.crossroadsinn.problem;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.settings.Roles;
import com.crossroadsinn.settings.Squads;
import com.crossroadsinn.signups.Player;
import com.crossroadsinn.squadassigning.SelectionAssigner;
import com.crossroadsinn.squadassigning.SquadUtilities;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * An iteration of a valid squad plan in the making
 */
public class SquadPlan {

	static class RoleTestingInput {
		final Role playerRole;
		final String roleType;
		final int requiredAmount;

		RoleTestingInput(Role role, String roleType, int requiredAmount) {
			this.playerRole = role;
			this.roleType = roleType;
			this.requiredAmount = requiredAmount;
		}
	}

	private final int SQUAD_SIZE = 10;
    private int numSquads;
    private final List<Player> trainers;
    private final List<Player> players;
    private Map<Player, Role> assigned = new HashMap<>();
	
	//what squad types are allowed to use up to which amounts, 0 being endless
	private final String squadTypeAllowed;
	private final SearchResultsState searchResultsState;
	
	//save what squads have been used to generate to parse it on for SquadComposition.java
	private ArrayList<String> squadTypes = new ArrayList<>();

	// Linked hashmap as we want a certain order to the roles (for filling purposes you want multi-covering roles first)
    private LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<>();
    private List<String> forbiddenRoles = new ArrayList<>();
	private int reqPlayers;

	Predicate<RoleTestingInput> specialRoleTester = (input) -> !forbiddenRoles.contains(input.playerRole.getRoleHandle()) && input.playerRole.getSpecialRoles().contains(input.roleType);
	Predicate<RoleTestingInput> boonRoleTester = (input) -> {
		if (input.playerRole.getBoons().containsKey(input.roleType)) {
			// Only if we don't provide too much boons we are a valid role!
			return !forbiddenRoles.contains(input.playerRole.getRoleHandle()) && input.playerRole.getBoons().get(input.roleType) <= input.requiredAmount;
		}
		return false;
	};

	/**
	 * Build up the initial state and start of the search, the input will be shuffled on creation in a new list
	 * @param trainees Input trainees
	 * @param trainers Input trainers (make sure they are marked as trainer
	 * @param numSquads Amount of squads you want to build
	 * @param squadTypeAllowed What kind of squads we want to build
	 */
    public SquadPlan(List<Player> trainees, List<Player> trainers, int numSquads, String squadTypeAllowed) {
        this.trainers = new ArrayList<>(trainers);
        this.players = new ArrayList<>(trainees);
		// Shuffle once so we don't have to constantly random pick but we can work through all solutions in orderly fashion
		Collections.shuffle(this.trainers);
		Collections.shuffle(this.players);
		// By adding trainers after the players, the will be always the last pick option for special roles and boons
		this.players.addAll(this.trainers);
		this.numSquads = numSquads;
		this.squadTypeAllowed = squadTypeAllowed;
		this.searchResultsState = new SearchResultsState();
		setupTotalSquadRequirements();
	}

	private void setupTotalSquadRequirements() {
		// max number was set in UI, check if enough players even exist and enough squads are allowed, if not, assume we have no max squads setting and set numSquads based on available players
		// Make sure to not start the search with more squads than possible
		if (numSquads * SQUAD_SIZE > players.size()) {
			numSquads = 0;
		}
		if (numSquads == 0) {
			numSquads = (players.size()/10);
		}

		// set amount of required players, this will used to check if we are done with building squads
		reqPlayers = numSquads * SQUAD_SIZE;

		// Build up the required roles
		for (int i = 0; i < numSquads; i++) {
			SquadUtilities.buildSquadRequirements(squadTypeAllowed, reqBoons, reqSpecialRoles);
			forbiddenRoles = new ArrayList<>(Squads.getSquad(squadTypeAllowed).getForbiddenRoles());;
		}
	}

    /**
     * Copy constructor. Creates a shallow copy.
     * @param other SquadPlan to copy
     */
    public SquadPlan(SquadPlan other) {
        this.numSquads = other.numSquads;
        this.trainers = new ArrayList<>(other.trainers);
        this.players = new ArrayList<>(other.players);
        this.assigned = new HashMap<>(other.assigned);
		this.squadTypeAllowed = other.squadTypeAllowed;
        this.squadTypes = other.squadTypes; //add proper deepcopy here
		this.reqPlayers = other.reqPlayers;
		this.reqBoons = new LinkedHashMap<>(other.reqBoons);
		this.reqSpecialRoles = new LinkedHashMap<>(other.reqSpecialRoles);
		this.searchResultsState = other.searchResultsState;
		this.forbiddenRoles = other.forbiddenRoles;
    }

    public int getNumSquads() {
        return numSquads;
    }

    public Map<Player, Role> getAssigned() {
        return assigned;
    }

    public String getSquadTypeAllowed() {
        return squadTypeAllowed;
    }

    /**
     * Set a role for a player. Return true if arc dependencies remain satisfied.
     * This method is to be used with basic roles only.
     * @return whether or not arc dependencies are satisfied.
     */
    private boolean setPlayer(Player player, Role playerRole) {
		//check if even still need a player
		if (reqPlayers < 1) return false;

		// Do we still need everything this role brings? (example are we trying to fill a HFB tank into a squad that is already full of healers)
		// If that is the case, we should break, but not for some
		for (String key : playerRole.getSpecialRoles()) {
			//workaround for dps in special roles and any overflowable special roles
			if (Roles.getOverflowableRoles().contains(key)) continue;
			if (!reqSpecialRoles.containsKey(key)) return false;
			if (playerRole.getIfRole(key) > reqSpecialRoles.get(key)) return false;
		}
		// Check the boons still required, don't want to overfill on heals or quickness
		for (String key : playerRole.getBoons().keySet()) {
			if (playerRole.getBoonAmount(key) > reqBoons.get(key)) return false;
		}

		// We have a valid player we can add, assign it and remove it from the pool
        assigned.put(player, playerRole);
		players.remove(player);
		
		// remove player from requirements
		reqPlayers -= 1;

		// And clean up still needed special roles or boons
		for(String specialRole : playerRole.getSpecialRoles()) {
			if (reqSpecialRoles.containsKey(specialRole)) {
				if (reqSpecialRoles.get(specialRole) == 1) {
					reqSpecialRoles.remove(specialRole);
				} else {
					reqSpecialRoles.put(specialRole, reqSpecialRoles.get(specialRole) - 1);
				}
			}
		}
		for(String boon: playerRole.getBoons().keySet()) {
			if (reqBoons.containsKey(boon)) {
				int leftOverRequired = reqBoons.get(boon) - playerRole.getBoonAmount(boon);
				if(leftOverRequired == 0) {
					reqBoons.remove(boon);
				} else {
					reqBoons.put(boon, reqBoons.get(boon) - playerRole.getBoonAmount(boon));
				}
			}
		}
		// Attempt to "auto-fill" all selected trainees, this will only give a result if all players can be assigned
		// This is to ensure we don't create a solution which works when pooling all squads, but can't be created
		SelectionAssigner result = new SelectionAssigner(assigned, numSquads, squadTypeAllowed).assignToSquads();
		return result != null;
	}

	/**
	 *
	 * @param roleType Name of the type a role has to fulfill (e.g. "tank" or "quickness"
	 * @param roleToTypeMapper To get the role type, this allows the method to be used for both boons and special roles
	 * @param requiredAmount Amount we still require, for special roles this will be usually 1 or 2, but for boons it might be 5 or 10
	 *                       This is required to not grab an alacrigade player (that provides 10 alac) when you only need 5
	 * @return a possible solution or empty if this branch was not fruitful or we can't add more of the roleType
	 */
	private Optional<SquadPlan> getNextSquadPlanForRoleType(String roleType, Predicate<RoleTestingInput> roleToTypeMapper, int requiredAmount) throws Exception {
		// Get a filtered list of player indexes that can play the roleType we want
		List<Player> rolePlayers = players.stream()
				.filter(es -> es.getRoles().stream().anyMatch(s -> roleToTypeMapper.test(new RoleTestingInput(s, roleType, requiredAmount))))
				.collect(Collectors.toList());

		for (Player player : rolePlayers) {
			List<Role> validPlayerRoles = player.getRoles().stream().filter(rl -> roleToTypeMapper.test(new RoleTestingInput(rl, roleType, requiredAmount))).collect(Collectors.toList());
			// Shuffle and grab, randomizes the roles we pick on the player, this is to help prevent having a role bias
			if (validPlayerRoles.size() > 1) {
				Collections.shuffle(validPlayerRoles);
			}
			for(Role validRole: validPlayerRoles) {
				SquadPlan copy = new SquadPlan(this);
				if (copy.setPlayer(player, validRole)) {
					SquadPlan possibleResult = copy.expandOrReturnSolution();
					if (possibleResult != null) {
						return Optional.of(possibleResult);
					}
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Helper method to walk through specialRoles / boons
	 * @param requiredRolesMap Map of roleType -> amount required
	 * @param roleTypeFetch Predicate to get the right roleType from the Role
	 * @return a possible solution or empty if this branch was not fruitful or we can't add more of the roleType
	 */
	private Optional<SquadPlan> addPlayersForRoleType(LinkedHashMap<String, Integer> requiredRolesMap, Predicate<RoleTestingInput> roleTypeFetch) throws Exception {
		for (String requiredRoleType : requiredRolesMap.keySet()) {
			int requiredAmount = requiredRolesMap.get(requiredRoleType);
			if (requiredAmount > 0) {
				Optional<SquadPlan> possibleResult = getNextSquadPlanForRoleType(requiredRoleType, roleTypeFetch, requiredAmount);
				if (possibleResult.isPresent()) {
					return possibleResult;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Helper method to walk through a list of players and check if they can be added as a DPS player
	 * @param players list of players that are leftover
	 * @return a possible solution or empty if this branch was not fruitful or we can't add more dps players
	 */
	private Optional<SquadPlan> addDpsPlayer(List<Player> players) throws Exception {
		for (Player player : players) {
			for (Role role : player.getRoles().stream().filter(r -> r.getDPS() > 0).collect(Collectors.toList())) {
				SquadPlan copy = new SquadPlan(this);
				if (copy.setPlayer(player, role)) {
					SquadPlan possibleResult = copy.expandOrReturnSolution();
					if (possibleResult != null) {
						return Optional.of(possibleResult);
					}
				}
			}
		}
		return Optional.empty();
	}

    /**
	 * Advances the solution by picking the next player in logical order
	 * - It starts with special roles
	 * - The boons
	 * - Fill it up with DPS
	 * If at any point we arrived a "isSolution" state we can return it
	 * Or if we expanded too deep resulting in too many failed states we simply return null as walking through everything takes too long
	 * If you get a null return, simply try again as the initial creation will shuffle the input resulting in a (probably) new starting point
     */
    public SquadPlan expandOrReturnSolution() throws Exception {
		if (searchResultsState.getFailures() > 10000) {
			return null;
		}
		// TODO frums add a check if we take too long (10 seconds), just quit

		// Don't go on if we already are a solution!
		if (isSolution()) {
			return this;
		}
		// First add the special roles, if not required we will continue
		Optional<SquadPlan> optAddedPlayer = addPlayersForRoleType(reqSpecialRoles, specialRoleTester);
		if (optAddedPlayer.isPresent()) {
			return optAddedPlayer.get();
		}
		// we should have filled up all special roles , if not, we failed as we have no-one to fill the missing role(s), maybe a different setup will work
		if (reqSpecialRoles.values().stream().anyMatch(a -> a > 0)) {
			searchResultsState.incrementFailures();
			return null;
		}

		// Next up, fill up the boons, special roles are filled up
		optAddedPlayer =  addPlayersForRoleType(reqBoons, boonRoleTester);
		if (optAddedPlayer.isPresent()) {
			return optAddedPlayer.get();
		}

		// we should have filled up all boons, if not, we failed as we have no-one to fill the missing role(s), maybe a different setup will work
		if (reqBoons.values().stream().anyMatch(a -> a > 0)) {
			searchResultsState.incrementFailures();
			return null;
		}
		// Sometimes we end up with not enough DPS players because many special roles signed up
		if (!checkIfStillEnoughDPS()) {
			searchResultsState.incrementFailures();
			return null;
		}
		// Should only require DPS now
		// First use up all leftover commanders to fill it up and then the whatever is left, this makes sure we have assigned every commander/trainee
		// TODO we it should be fairly easy to add a total trainer counter so we only fill numSquads * 2 trainers, then fill with trainees and then we can use whatever leftover trainers
		optAddedPlayer = addDpsPlayer(players.stream().filter(Player::isTrainer).collect(Collectors.toList()));
		if (optAddedPlayer.isPresent()) {
			return optAddedPlayer.get();
		}
		// And fill up with leftover dps trainees
		optAddedPlayer = addDpsPlayer(players);
		if (optAddedPlayer.isPresent()) {
			return optAddedPlayer.get();
		}
		// Can't find the last DPS player, but we already checked for that so must be programmer error
		throw new Exception("Classic this should not happen exception");
	}

	/**
	 * Note, this should only be used if you already have the special roles/boons filled
	 */
	public boolean checkIfStillEnoughDPS() {
		return players.stream().filter(p -> p.getRoles().stream().anyMatch(r -> r.getDPS() > 0)).count() >= reqPlayers;
	}

    /**
     * Calculates the heuristic, the lower the better we deem the solution
     * @return the heuristic value for this plan.
     */
	public int heuristic() {
		// Prefer role compression, most new people learn bosses on dps, so try to fit the most dps
		int notDpsPlayers = (int) assigned.values().stream().filter(r -> r.getDPS() == 0).count() * 10;

		// However we want to try to get commanders on DPS if possible, so weigh it more heavily
		int commNotAsDps = (int) assigned.entrySet().stream().filter(e -> e.getKey().isTrainer()).filter(r -> r.getValue().getDPS() == 0).count() * 100;

		return notDpsPlayers + commNotAsDps;
	}

    /**
     * Checks if we still require players
     */
    public boolean isSolution() {
    	return reqPlayers == 0;
    }
}
