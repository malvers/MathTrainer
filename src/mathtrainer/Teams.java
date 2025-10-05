package mathtrainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Teams {

    protected static final short MICHAEL = 0;
    protected static final short MAGDALENA = 1;
    protected static final short LUISE_LEON = 2;
    protected static final short MARINA_EVGENIJA = 3;
    protected static final short TIBOR = 4;
    protected static final short JULIA_LAURA = 5;
    protected static final short LAURA_MICHEL = 6;
    protected static final short LAURA_MASSARI = 7;

    private static final String[] TEAM_NAMES = loadTeamNames();

    private static String[] loadTeamNames() {
        List<String> names = new ArrayList<>();

        try (InputStream inputStream = Teams.class.getClassLoader().getResourceAsStream("teamNames.txt")) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        names.add(line.trim());
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load team names from file", e);
        }

        if (names.isEmpty()) {
            throw new RuntimeException("No team names found in file!");
        }

        return names.toArray(new String[0]);
    }

    protected static String getTeamName(int id) {
        if (id >= 0 && id < TEAM_NAMES.length) {
            return "Team " + TEAM_NAMES[id];
        }
        return "Unknown";
    }

    // Optional: Method to get all team names
    protected static String[] getAllTeamNames() {
        return TEAM_NAMES.clone();
    }

    // Optional: Method to print all teams (for testing)
    protected static void printAllTeams() {
        System.out.println("Available teams:");
        for (int i = 0; i < TEAM_NAMES.length; i++) {
            System.out.println(i + ": " + TEAM_NAMES[i]);
        }
    }
}