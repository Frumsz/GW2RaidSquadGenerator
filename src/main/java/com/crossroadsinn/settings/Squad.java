package com.crossroadsinn.settings;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;

import java.util.*;

/**
 * A class that can hold information about a squad.
 * @author moon
 * @version 1.1
 */
public class Squad {
	private final String squadHandle;
	private final String squadName;
    private final LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<>();
	private final List<String> forbiddenRoles = new ArrayList<>();
	private final SimpleBooleanProperty enabled = new SimpleBooleanProperty();

    public Squad(String squadHandle, String squadName, String Boons, String SpecialRoles, boolean isDefault, String forbiddenRolesInput) {
        this.squadHandle = squadHandle;
        this.squadName = squadName;
		this.enabled.set(isDefault);
        for (String part:Boons.split("\\s*,\\s*")) {
			String[] BoonsValuePair = part.split("\\s*:\\s*");
			if (reqBoons.containsKey(BoonsValuePair[0])) {
				reqBoons.put(BoonsValuePair[0],reqBoons.get(BoonsValuePair[0])+Integer.parseInt(BoonsValuePair[1]));
			} else {
				reqBoons.put(BoonsValuePair[0],Integer.parseInt(BoonsValuePair[1]));
			}
		}
        for (String part:SpecialRoles.split("\\s*,\\s*")) {
			String[] roleValuePair = part.split("\\s*:\\s*");
			if (reqSpecialRoles.containsKey(roleValuePair[0])) {
				reqSpecialRoles.put(roleValuePair[0],reqSpecialRoles.get(roleValuePair[0])+Integer.parseInt(roleValuePair[1]));
			} else {
				reqSpecialRoles.put(roleValuePair[0],Integer.parseInt(roleValuePair[1]));
			}
		}
        String[] forbiddenRolesSplit = forbiddenRolesInput.split("\\s*,\\s*");
		forbiddenRoles.addAll(Arrays.asList(forbiddenRolesSplit));
    }

    public String getSquadHandle() {
        return squadHandle;
	}
	
    public String getSquadName() {
        return squadName;
	}
	
	public String getName() {
		return squadName;
	}
		
    public LinkedHashMap<String, Integer> getReqBoons() {
        return reqBoons;
    }
    public LinkedHashMap<String, Integer> getReqSpecialRoles() {
        return reqSpecialRoles;
    }
	
	public SimpleBooleanProperty getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
    	this.enabled.set(enabled);
	}

	public List<String> getForbiddenRoles() {
		return forbiddenRoles;
	}
}