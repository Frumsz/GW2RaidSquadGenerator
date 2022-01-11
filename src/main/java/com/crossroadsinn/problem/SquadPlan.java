package com.crossroadsinn.problem;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.settings.Squads;
import com.crossroadsinn.signups.Player;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Defines the CSP structure for Squad Generation.
 * Includes arc dependency checks and forward chaining.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class SquadPlan implements CSP {
    private int numSquads;
    private final List<Player> trainers;
    private final List<Player> players;
    private Map<Player, Role> assigned = new HashMap<>();
	
	//what squad types are allowed to use up to which amounts, 0 being endless
	private final LinkedHashMap<String, Integer> squadTypeAllowed;
	private final SearchResultsState searchResultsState;
	
	//save what squads have been used to generate to parse it on for SquadComposition.java
	private ArrayList<String> squadTypes = new ArrayList<>();

	BiPredicate<Role, String> specialRoleTester = (playerRole, roleType) -> playerRole.getSpecialRoles().contains(roleType);
	BiPredicate<Role, String> boonRoleTester = (playerRole, roleType) -> playerRole.getBoons().containsKey(roleType);

	// Linked hashmap as we want a certain order to the roles (for filling purposes you want multi-covering roles first)
    private LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<String, Integer>();
	private int reqPlayers = 10;

    public SquadPlan(List<Player> trainees, List<Player> trainers, int numSquads, LinkedHashMap<String, Integer> squadTypeAllowed) {
        this.trainers = new ArrayList<>(trainers);
        this.players = new ArrayList<>(trainees);
		// Shuffle once so we don't have to constantly random pick but we can work through all solutions in orderly fashion
		Collections.shuffle(this.trainers);
		Collections.shuffle(this.players);
		// By adding trainers after the players, the will be always the last pick option for special roles and boons
		this.players.addAll(this.trainers);
		this.numSquads = numSquads;
		this.squadTypeAllowed = new LinkedHashMap<>(squadTypeAllowed);
		this.searchResultsState = new SearchResultsState();
		calculateRequirements();
	}
		
	private void calculateRequirements() {
		//temp part, will be replaced with proper passing of settings from UI later
		//get all allowed SquadTypes and check if there's a max amount of squads from there. if yes, check if bigger than numSquads and apply
		int maxSquadsSelected = 0;
		for (int i:squadTypeAllowed.values()) {
			if (i == 0) {
				maxSquadsSelected = 0;
				break;
			}
			maxSquadsSelected += i;
		}
		if (maxSquadsSelected > 0 && maxSquadsSelected < numSquads) numSquads = maxSquadsSelected;
		
		//max number of squads possible guessing
		//max number was set in UI, check if enough players even exist and enough squads are allowed, if not, assume we have no max squads setting and set numSquads based on available players
		if (numSquads != 0) {
			if (numSquads*10>players.size()) {
				numSquads = 0;
			}
		};
		if (numSquads == 0) {
			numSquads = (players.size()/10);
			if (maxSquadsSelected > 0 && maxSquadsSelected < numSquads) numSquads = maxSquadsSelected;
		}
		
		//set amount of required players
		reqPlayers = numSquads*10;

		//build requirements from a random composition of available squadtypes, max 1k tries, then go lower numSquads amount
		boolean foundSquads = false;
		while(numSquads>squadTypes.size()) {
			int i = 0;
			while (i<1000) {
				if (buildSquadRequirements()) {
					foundSquads = true;
					break;
				} else {
					//reset variables and try again
					this.squadTypes = new ArrayList<>();
					this.reqBoons = new LinkedHashMap<>();
					this.reqSpecialRoles = new LinkedHashMap<>();
				}
				i++;
			}
			if (!foundSquads) {
				numSquads-= 1;
				//set amount of required players
				reqPlayers = this.numSquads*10;
			}			
		}
	}

    /**
     * Copy constructor. Creates a deepcopy.
     * @param other SquadPlan to copy
     */
    public SquadPlan(SquadPlan other) {
        this.numSquads = other.numSquads;
        this.trainers = new ArrayList<>(other.trainers);
        this.players = new ArrayList<>(other.players);
        this.assigned = new HashMap<>(other.assigned);
		this.squadTypeAllowed = new LinkedHashMap<>(other.squadTypeAllowed);
        this.squadTypes = other.squadTypes; //add proper deepcopy here
		this.reqPlayers = other.reqPlayers;
		this.reqBoons = new LinkedHashMap<>(other.reqBoons);
		this.reqSpecialRoles = new LinkedHashMap<>(other.reqSpecialRoles);
		this.searchResultsState = other.searchResultsState;
    }

    public int getNumSquads() {
        return numSquads;
    }

    public Map<Player, Role> getAssigned() {
        return assigned;
    }

    public ArrayList<String> getSquadTypes() {
        return squadTypes;
    }
	
	//function to build squad requirements based on allowed squads
	private boolean buildSquadRequirements() {
		//get a random squad
		List<String> valuesList = new ArrayList<String>(squadTypeAllowed.keySet());
		int randomIndex = new Random().nextInt(valuesList.size());
		String randomValue = valuesList.get(randomIndex);

		//add requirements - boons
		for (String key:Squads.getSquad(randomValue).getReqBoons().keySet()) {
			int value = Squads.getSquad(randomValue).getReqBoons().get(key);
			if (reqBoons.containsKey(key)) {
				value += reqBoons.get(key);
			}
			reqBoons.put(key,value);
		}

		//add requirements - roles
		for (String key:Squads.getSquad(randomValue).getReqSpecialRoles().keySet()) {
			int value = Squads.getSquad(randomValue).getReqSpecialRoles().get(key);
			if (reqSpecialRoles.containsKey(key)) {
				value += reqSpecialRoles.get(key);
			}
			reqSpecialRoles.put(key,value);
		}

		//add to squad amounts
		squadTypes.add(randomValue);

		//check dependencies, return false if can't satisfy the squad combination requirements
		// TODO FRUMS are we sure we still need this?
		return true;
	}

    /**
     * Set a role for a player. Return true if arc dependencies remain satisfied.
     * This method is to be used with basic roles only.
     * @return whether or not arc dependencies are satisfied.
     */
    private boolean setPlayer(Player player, Role playerRole) {
		//check if even still need a player
		if (reqPlayers < 1) return false;

		//special role still needed
		for (String key : playerRole.getSpecialRoles()) {
			//workaround for dps in special roles
			if (key.equals("dps")) continue;
			if (!reqSpecialRoles.containsKey(key)) return false;
			if (playerRole.getIfRole(key) > reqSpecialRoles.get(key)) return false;
		}
		// enough boons still available
		for (String key : playerRole.getBoons().keySet()) {
			if (playerRole.getBoonAmount(key) > reqBoons.get(key)) return false;
		}

        assigned.put(player, playerRole);
		players.remove(player);
		
		// remove player from requirements
		reqPlayers -= 1;
		for(String specialRole : playerRole.getSpecialRoles()) {
			if (reqSpecialRoles.containsKey(specialRole)) {
				reqSpecialRoles.put(specialRole, reqSpecialRoles.get(specialRole) - 1);
			}
		}
		for(String boon: playerRole.getBoons().keySet()) {
			if (reqBoons.containsKey(boon)) {
				reqBoons.put(boon, reqBoons.get(boon) - playerRole.getBoonAmount(boon));
			}
		}
        // Return validity of state
        return true;
    }

	/**
	 *
	 * @param roleType Name of the type a role has to fulfill (e.g. "tank" or "quickness"
//	 * @param useTrainers Use trainers instead of trainees in the search
	 * @param roleToTypeMapper To get the role type, this allows the method to be used for both boons and special roles
	 * @return
	 */
	private Optional<CSP> getNextCSPForRoleType(String roleType, BiPredicate<Role, String> roleToTypeMapper) throws Exception {
		// Get a filtered list of player indexes that can play druid, an
		List<Player> boonPlayers = players.stream()
				.filter(es -> es.getRoles().stream().anyMatch(s -> roleToTypeMapper.test(s, roleType)))
				.collect(Collectors.toList());

		for (Player player : boonPlayers) {
			SquadPlan copy = new SquadPlan(this);
			// .get() is fine as we already filtered the players
			if (copy.setPlayer(player, player.getRoles().stream().filter(rl -> roleToTypeMapper.test(rl, roleType)).findFirst().get())) {
				CSP possibleResult = copy.getChildren();
				if (possibleResult != null) {
					return Optional.of(possibleResult);
				}
			}
		}
		return Optional.empty();
	}

	private Optional<CSP> addPlayersForRoleType(LinkedHashMap<String, Integer> requiredRolesMap, BiPredicate<Role, String> roleTypeFetch) throws Exception {
		for (String requiredRoleType : requiredRolesMap.keySet()) {
			if (requiredRolesMap.get(requiredRoleType) > 0) {
				Optional<CSP> possibleResult = getNextCSPForRoleType(requiredRoleType, roleTypeFetch);
				if (possibleResult.isPresent()) {
					return possibleResult;
				}
			}
		}
		return Optional.empty();
	}

	private Optional<CSP> addDpsPlayer(List<Player> eligiblePlayers) throws Exception {
		for (Player player : eligiblePlayers) {
			for (Role role : player.getRoles().stream().filter(r -> r.getDPS() > 0).collect(Collectors.toList())) {
				SquadPlan copy = new SquadPlan(this);
				if (copy.setPlayer(player, role)) {
					CSP possibleResult = copy.getChildren();
					if (possibleResult != null) {
						return Optional.of(possibleResult);
					}
				}
			}
		}
		return Optional.empty();
	}

    /**
     * Pick a random player and for each of it's availabilities whether a valid plan can be generated.
     * If not, remove player from list (soz broski, maybe next week will be your week)
     * @return valid SquadPlans generated.
     */
    public CSP getChildren() throws Exception {
		if (searchResultsState.getFailures() > 10000) {
			return null;
		}
		// TODO frums add a check if we take too long (10 seconds), just quit

		// Don't go on if we already are a solution!
		if (isSolution()) {
			return this;
		}
		// First add the special roles, if not required we will continue
		Optional<CSP> optAddedPlayer =  addPlayersForRoleType(reqSpecialRoles, specialRoleTester);
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
		optAddedPlayer = addDpsPlayer(players.stream().filter(Player::isTrainer).collect(Collectors.toList()));
		if (optAddedPlayer.isPresent()) {
			return optAddedPlayer.get();
		}
		// And fill up with leftover dps trainees
		optAddedPlayer = addDpsPlayer(players);
		if(optAddedPlayer.isPresent()){
			return optAddedPlayer.get();
		}
		// Can't find the last DPS player, but we already checked for that so must be programmer error
		throw new Exception("Classic this should not happen exception");
	}

	/**
	 * Note, this should only be used if you already have the special roles/boons filled
	 */
	public boolean checkIfStillEnoughDPS() {
		return players.stream().filter(p -> p.getRoles().stream().anyMatch(r -> r.getDPS() > 0)).count() > reqPlayers;
	}

    /**
     * Calculates the heuristic for the problem.SquadPlan based on remaining spots to fill.
     * @return the heuristic value for this plan.
     */
    public int heuristic() {
		// Having 4 instead of 5 dps isn't the worst in a squad
		int notDpsPlayers = (int) assigned.values().stream().filter(r -> r.getDPS() == 0).count();

		// Avoid if possible
		int commNotAsDps = (int) assigned.entrySet().stream().filter(e -> e.getKey().isTrainer()).filter(r -> r.getValue().getDPS() == 0).count() * 50;

		return notDpsPlayers + commNotAsDps;
    }

    /**
     * @return whether or not this plan is a solution.
     */
    public boolean isSolution() {
		//must not need any more players
		if (reqPlayers != 0) return false;
		//must not need any more special roles
		for (int amount:reqSpecialRoles.values()) {
			if (amount != 0) return false;
		}
		//must not need any more boons
		for (int amount:reqBoons.values()) {
			if (amount != 0) return false;
		}
		return true;
    }
}
