package mathtrainer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryTask {

    String Student;
    String question = "Question";
    String answer = "Answer";
    private static List<Vocabulary> tasks = new ArrayList<>();
    private static int taskNumber = 0;

    public HistoryTask(String nameIn, boolean read) {

        if (read) {
            try {
                //readTasksFromFile(Path.of(MathTrainer.workingDirectory + "history/history.txt"));
                readTasksFromResource();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Student = nameIn;
    }

    public HistoryTask() {

    }

    protected static void nextTask() {
        taskNumber++;
    }

    protected static void clearTasks() {
        System.out.println("cearTasks");
        tasks = new ArrayList<>();
//        tasks.clear();
    }

    protected static void addTask(Vocabulary vocabulary) {
        tasks.add(vocabulary);
        Collections.shuffle(tasks);
    }

    protected static List<Vocabulary> getTasks() {

        return tasks;
    }

    protected static class Vocabulary {

        String question;
        String answer;

        public Vocabulary(String en, String ge) {
            question = en;
            answer = ge;
        }
    }

    private void readTasksFromFile(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s*:\\s*");
            if (parts.length >= 2) {
                try {
                    tasks.add(new Vocabulary(parts[0], parts[1]));
//                    System.out.println(parts[0] + " - " + parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line: " + line);
                }
            }
        }
        Collections.shuffle(tasks);
    }

    private void readTasksFromResource() throws IOException {

        try (InputStream in = MathTrainer.class.getResourceAsStream("/history/history.txt")) {
            if (in == null) {
                throw new IOException("Resource not found: " + "/history/history.txt");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                List<String> lines = reader.lines().toList();

                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split("\\s*:\\s*");
                    if (parts.length >= 2) {
                        try {
                            tasks.add(new HistoryTask.Vocabulary(parts[0], parts[1]));
                            //System.out.println(parts[0] + " - " + parts[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping line: " + line);
                        }
                    }
                }
                Collections.shuffle(tasks);
            }
        }
    }

    String getQuestion() {

        return tasks.get(taskNumber).question;
    }

    public void print(int i) {

        String space = "";
        if (i < 10) {
            space = " ";
        }
        System.out.println(Student + " ->\t" + space + i + " ->\t" + question);
    }

    public String getAnswer() {
        return tasks.get(taskNumber).answer;
    }

    public Color getColor() {
        return Color.WHITE;
    }
}
