package com.crossroadsinn.settings;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * Currently available squads
 * @author moon
 * @version 1.1
 */
public class Squads {
	private static final Hashtable<String, Squad> squads = new Hashtable<>();
	public static void init() {
		try {
			File csvFile = new File("squads.csv");
			FileInputStream csvFileInputStream = new FileInputStream(csvFile);
			InputStreamReader csvInputStreamReader = new InputStreamReader(csvFileInputStream,StandardCharsets.UTF_8);
			parse(csvInputStreamReader);
		} catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static void addSquad(String squadHandle, String squadName, String reqBoons, String reqSpecialRoles, boolean isDefault) {
		squads.put(squadHandle,new Squad(squadHandle, squadName, reqBoons, reqSpecialRoles, isDefault));
    }
	
	public static Squad getSquad(String squadHandle) {
		return squads.get(squadHandle);
	}
	
	public static ArrayList<Squad> getSquads() {
		return new ArrayList<>(squads.values());
	}

	/**
     * Parse a given CSV and generate a list of squads it contains.
     * @param inputStreamReader The csv stream to parse.
     */
    public static void parse(InputStreamReader inputStreamReader) {
        CSVReader parser = new CSVReaderBuilder(inputStreamReader).withSkipLines(1).build();
        try {
           	for(String[] line : parser.readAll()) {
				parseSquad(line);
			}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (parser != null) parser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	 
	
	/**
     * Generate role given the information contained in a line.
     * @param SquadLine The line containing the role info.
     */
	private static void parseSquad(String[] SquadLine) {
		String squadHandle = SquadLine[0].trim();
		String squadName = SquadLine[1].trim();
		String reqBoons = SquadLine[2].trim();
		String reqSpecialRoles = SquadLine[3].trim();
		boolean isDefault = SquadLine[4].toLowerCase().contains("true");
		addSquad(squadHandle,squadName,reqBoons,reqSpecialRoles,isDefault);		
	}
}