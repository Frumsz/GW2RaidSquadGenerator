package com.crossroadsinn.signups;

import com.crossroadsinn.Main;
import com.crossroadsinn.problem.SquadSolution;
import com.opencsv.CSVWriter;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements methods that help save squad compositions as CSVs.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class SquadSaver {
    public static boolean exportToCSV(List<SquadSolution> squadComps, List<Player> leftOvers, String day) {
        CSVWriter writer = null;
        String chosenDir = chooseDir();
        if (chosenDir == null) return false;
        try {
            File csv = new File(chosenDir);
            csv.createNewFile();
            writer = new CSVWriter(new FileWriter(csv));

            writer.writeNext(new String[]{"Player name", "Discord name", "Discord Ping", "Day", "Squad", "Squad Type", "Assigned Role", "Tier", "Signups", "Roles"});
			
			int squadCounter = 0;

            for (SquadSolution squadComp : squadComps) {
                squadCounter = writeSquad(squadComp.getName(), squadComp.getSquads(), day, writer, squadCounter);
            }

            for (Player player : leftOvers) {
                writer.writeNext(playerLine(player, day, "", ""));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write all provided squads to the CSV.
     * @param compName The name of the composition.
     * @param squadList The squads in the composition.
     * @param writer The CSV writer object.
     * @param day The day of the training.
	 * @param currentSquadCounter the current counter of the squad number
     */
    private static int writeSquad(String compName, List<List<Player>> squadList, String day, CSVWriter writer, int currentSquadCounter) {
        int i = 0;
		for (; i < squadList.size(); ++i) {
            for (Player player : squadList.get(i)) {
                writer.writeNext(playerLine(player, day, currentSquadCounter+i+1+"", compName));
            }
        }
		return currentSquadCounter+i;
    }

    private static String[] playerLine(Player player, String day, String squad, String squadType) {
        boolean isComm = player.getTier().equalsIgnoreCase("commander") || player.getTier().equalsIgnoreCase("aide");
        ArrayList<String> line = new ArrayList<>();
        line.add(isComm ? player.getTier() : player.getGw2Account());
        line.add(player.getDiscordName().isEmpty() ? player.getGw2Account() : player.getDiscordName());
        line.add(player.getDiscordPing().isEmpty() ? player.getGw2Account() : player.getDiscordPing());
        line.add(day);
        line.add(squad);
        line.add(squadType);
        line.add(player.getAssignedRoleObj() != null ? player.getAssignedRoleObj().getRoleName() : "");
        line.add(isComm ? "-" : player.getTier());
        line.add(player.getBossLvlChoiceAsString());
        line.addAll(player.getRolenameList());
        return line.toArray(new String[0]);
    }

    /**
     * Allow user to select preferred location for saving squads.
     * @return User chosen location
     */
    private static String chooseDir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("squad-compositions.csv");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Where should I save squad compositions?");
        File userChosenFile = fileChooser.showSaveDialog(Main.getPrimaryStage());
        if (userChosenFile == null) return null;
        else {
            String filePath = userChosenFile.getAbsolutePath();
            if (!filePath.endsWith(".csv")) filePath += ".csv";
            return filePath;
        }
    }
}