package com.crossroadsinn.settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;
/**
 * A class that can hold information about a squad.
 * @author moon
 * @version 1.1
 */
public class Squad {
	private String squadHandle;
	private String squadName;
    private LinkedHashMap<String, Integer> reqBoons = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> reqSpecialRoles = new LinkedHashMap<>();
	private boolean enabled;
	private int maxAmount = 0;

    public Squad(String squadHandle, String squadName, String Boons, String SpecialRoles, boolean isDefault) {
        this.squadHandle = squadHandle;
        this.squadName = squadName;
		this.enabled = isDefault;
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
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled() {
		enabled = true;
	}
	
	public void setDisabled() {
		enabled = false;
	}
	
	public int getMaxAmount() {
		return maxAmount;
	}
}