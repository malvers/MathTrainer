package mathtrainer;

import mratools.MTools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Team extends ArrayList<OneStudent> {

    public String fileName = "no team assigned";
    static String[] teamsString;

    public Team() {
        initTeamId();
    }

    private void readTeams(int classId) {

//        MTools.println("readTeams");

        fileName = "/teams/Team" + teamsString[classId] + ".txt";

        InputStream inputStream = getClass().getResourceAsStream(fileName);

        if (inputStream == null) {
            MTools.println("❌ File not found: " + fileName);
            return;
        }

        // ✅ Read content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String studentName;
            while ((studentName = reader.readLine()) != null) {
                if (studentName.startsWith("//")) {
                    continue;
                }
                add(new OneStudent(studentName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Team(int classId) {

        super(16);

        initTeamId();

        readTeams(classId);
    }

    private void initTeamId() {
        teamsString = new String[3]; // adjust if person is added
        teamsString[0] = "Alvers";
        teamsString[1] = "Wischnewski";
        teamsString[2] = "Cool";
    }

    public int getNumberTasks() {

        int sum = 0;
        for (int i = 0; i < size(); i++) {
            OneStudent oneStudent = getStudent(i);
            if (oneStudent.anwesend) {
                sum += oneStudent.getNumberTasks();
            }
        }
        return sum;
    }

    @Override
    public OneStudent get(int index) {
        return getStudent(index);
    }

    public OneStudent getStudent(int index) {
        return super.get(index);
    }

    public ArrayList<OneStudent> getStudents() {
        return this;
    }
}
