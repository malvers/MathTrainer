package mathtrainer;

import mratools.MTools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Team extends ArrayList<OneSchueler> {

    public String fileName = "no team assigned";
    static String[] teamsString;

    public Team() {
        initKlassenId();
    }

    private void readTeams(int classId) {

        MTools.println("readTeams");

        fileName = "/teams/Team" + teamsString[classId] + ".txt";

        InputStream inputStream = getClass().getResourceAsStream(fileName);

        if (inputStream == null) {
            MTools.println("❌ File not found: " + fileName);
            return;
        }

        // ✅ Read content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            //System.out.println("📄 Contents of " + fileName + ":");
            while ((line = reader.readLine()) != null) {
                //System.out.println("line: " + line);
                add(new OneSchueler(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Team(int classId) {

        super(16);

        initKlassenId();

        readTeams(classId);
    }

    private void initKlassenId() {
        teamsString = new String[3]; // adjust if person is added
        teamsString[0] = "Alvers";
        teamsString[1] = "Wischnewski";
        teamsString[2] = "Cool";
    }

    public int getNumberTasks() {

        int sum = 0;
        for (int i = 0; i < size(); i++) {
            OneSchueler osch = getSchueler(i);
            if (osch.anwesend) {
                sum += osch.getNumberTasks();
            }
        }
        return sum;
    }

    @Override
    public OneSchueler get(int index) {
        return getSchueler(index);
    }

    public OneSchueler getSchueler(int index) {
        return super.get(index);
    }

    void print() {
        for (int i = 0; i < size(); i++) {
            get(i).print();
        }
    }
}
