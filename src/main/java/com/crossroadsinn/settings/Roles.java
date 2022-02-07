package com.crossroadsinn.settings;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Currently available roles
 * @author moon
 * @version 1.1
 */
public class Roles {
	private static final LinkedHashMap<String, Role> roles = new LinkedHashMap<>();
	// We don't want to overfill roles like healers, but reflect for matthias is fine as it simply a special property for some roles,
	// This is used to check if special roles aren't overflowing the squad, you cannot have too many of these basically
	private static List<String> OVERFLOWABLE_ROLES = new ArrayList<>();

	public static void init() {
		try {
			File csvFile = new File("roles.csv");
			FileInputStream csvFileInputStream = new FileInputStream(csvFile);
			InputStreamReader csvInputStreamReader = new InputStreamReader(csvFileInputStream,StandardCharsets.UTF_8);
			parse(csvInputStreamReader);
		} catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void addRole(String roleHandle, String roleName, String boons, String specialRoles, boolean commRole) {
		roles.put(roleHandle,new Role(roleHandle,roleName,boons,specialRoles, commRole));
	}

	public static Role getRole(String roleHandle) {
		return roles.get(roleHandle);
	}
	
	public static ArrayList<Role> getAllRoles() {
		return new ArrayList<>(roles.values());
	}
	
	public static Set<String> getAllRolesAsStrings() {
		return roles.keySet();
	}

	public static List<String> getOverflowableRoles() {
		return OVERFLOWABLE_ROLES;
	}
	
    /**
     * Parse a given CSV and generate a list of roles it contains.
     * @param inputStream The csv stream to parse.
     */
    public static void parse(InputStreamReader inputStream) {
        CSVReader parser = new CSVReaderBuilder(inputStream).withSkipLines(1).build();
        try {
        	for (String[] line : parser.readAll()) {
				parseRole(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                parser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	 
	
	/**
     * Generate role given the information contained in a line.
     * @param roleLine The line containing the role info.
     */
	private static void parseRole(String[] roleLine) {
		String roleHandle = roleLine[0].trim();
		String roleName = roleLine[1].trim();
		String boons = roleLine[2].trim();
		String specialRoles = roleLine[3].trim();
		boolean commRole = roleLine[4].toLowerCase().contains("true");
		boolean overFlowable = roleLine[5].toLowerCase().contains("true");
		if (overFlowable) {
			OVERFLOWABLE_ROLES.add(roleHandle);
		}
		addRole(roleHandle,roleName,boons,specialRoles,commRole);		
	}
		
}