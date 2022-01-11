package com.crossroadsinn.signups;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.settings.Roles;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Uses OpenCSV to parse players in a sign-up sheet.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class SignupsParser {

    // private static final String[] columns = {"gw2 account", "discord account", "tier", "comments", "tank", "druid", "offheal", "chrono", "alacrigade", "quickbrand", "banners", "dps"};
    private final int[] columnIndices = {-1, -1, -1, -1, -1, -1};

    /**
     * Parse a given CSV and generate a list of players it contains.
     * @param reader The csv stream to parse.
     * @return The list of generated players.
     */
    public ArrayList<Player> parse(InputStreamReader reader) {
        ArrayList<Player> players = new ArrayList<>();
        CSVReader parser = null;
        try {
            parser = new CSVReader(reader);
            String [] line;
            Player player;
            // Ignore first line
            line = parser.readNext();
            // Invalid file.
            if (!getColumnIndices(line)) return null;
            while ((line = parser.readNext()) != null) {
                if ((player = parsePlayer(line)) != null) players.add(player);
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
        return players;
    }

    /**
     * Generate player given the information contained in a line.
     * @param playerLine The line containing the player info.
     * @return The Player object.
     */
    private Player parsePlayer(String[] playerLine) {
        String gw2Account = playerLine[columnIndices[0]];
        String discordName = playerLine[columnIndices[1]];
        String discordPing = playerLine[columnIndices[2]];
		//String tier = playerLine[columnIndices[6]];
        String tier = "3";
        if (gw2Account.equals("Commander")) {tier = "Commander"; gw2Account = " ";}
        else if (gw2Account.equals("Aide")) {tier = "Aide"; gw2Account = " ";}
        String comments = playerLine[columnIndices[3]];
        String possibleRoles = playerLine[columnIndices[5]];
        Set<Role> roles = new HashSet<>();
		if (!playerLine[columnIndices[5]].isBlank()) {
			for (String role: possibleRoles.split("\\s*,\\s*")) {
                Role roleObj = Roles.getRole(role);
				if (roleObj != null) {
                    roles.add(roleObj);
                }

                //hardcoded additional roles to be added
                if (role.equals("ctank")) { roles.add(Roles.getRole("forcedctank"));}
                if (role.equals("hfb")) { roles.add(Roles.getRole("hfbmattreflect"));}
                if (role.equals("qchrono")) {roles.add(Roles.getRole("qchronomattreflect"));}
                if (role.equals("scam")) { roles.add(Roles.getRole("scammattreflect"));}
			}
		}
        // Player without roles is useless
        if (roles.isEmpty()){
            System.out.println("Failed parsing player " + gw2Account + ", no valid roles found for (" + possibleRoles + "), mismatch between roles.csv and signup sheet?");
            return null;
        }
		if (playerLine[columnIndices[4]].isBlank()) return null;
		String[] bossLvlChoice = playerLine[columnIndices[4]].split("\\s*,\\s*");

        return new Player(gw2Account, discordName, discordPing, tier, comments, roles, bossLvlChoice);
    }

    private boolean getColumnIndices(String[] headerLine) {
        // {"gw2 account", "discord account", "tier", "comments", "tank", "druid", "offheal", "chrono", "alacrigade", "quickbrand", "banners", "dps"}
        for (int i = 0; i < headerLine.length; ++i) {
            String header = headerLine[i].toLowerCase();
            if (header.contains("gw2 account")) columnIndices[0] = i;
            else if (header.contains("discord account")) columnIndices[1] = i;
            else if (header.contains("discord ping")) columnIndices[2] = i;
            else if (header.contains("comment")) columnIndices[3] = i;
            else if (header.contains("training name")) columnIndices[4] = i;
            else if (header.equals("roles")) columnIndices[5] = i;
            //else if (header.equals("tier")) columnIndices[6] = i;
        }
        // Valid file, no column missing.
        return IntStream.of(columnIndices).noneMatch(e -> e == -1);
    }

}
