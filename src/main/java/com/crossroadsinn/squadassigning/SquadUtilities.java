package com.crossroadsinn.squadassigning;

import com.crossroadsinn.settings.Squad;
import com.crossroadsinn.settings.Squads;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SquadUtilities {

    /**
     * A lovely side-effects method, this is usually a really bad idea, well it still is but I just felt like moving a whole blob of code over for now
     * @param squadTypeAllowed
     * @param reqBoons
     * @param reqSpecialRoles
     */
    public static void buildSquadRequirements(String squadTypeAllowed, LinkedHashMap<String, Integer> reqBoons, LinkedHashMap<String, Integer> reqSpecialRoles) {
        Squad squadToBuild = Squads.getSquad(squadTypeAllowed);
        //add requirements - boons
        for (String key : squadToBuild.getReqBoons().keySet()) {
            int value = squadToBuild.getReqBoons().get(key);
            if (reqBoons.containsKey(key)) {
                value += reqBoons.get(key);
            }
            reqBoons.put(key,value);
        }

        //add requirements - roles
        for (String key : squadToBuild.getReqSpecialRoles().keySet()) {
            int value = squadToBuild.getReqSpecialRoles().get(key);
            if (reqSpecialRoles.containsKey(key)) {
                value += reqSpecialRoles.get(key);
            }
            reqSpecialRoles.put(key,value);
        }
    }
}
