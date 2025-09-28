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

public class DropTask {

    protected static String student;
    private static final List<Vocabulary> tasks = new ArrayList<>();
    private static int taskNumber = 0;

    public DropTask(String nameIn) {
        student = nameIn;
    }

    protected static void nextTask() {
        taskNumber++;
    }

    protected static void clearTasks() {
        taskNumber = 0;
        tasks.clear();
    }

    protected static void addTask(Vocabulary vocabulary) {
        //System.out.println("DropTask.addTasks: " + vocabulary.question + " :: " + vocabulary.answer);
        tasks.add(vocabulary);
        Collections.shuffle(tasks);
    }

    protected static List<Vocabulary> getTasks() {
        return tasks;
    }

    public static void print(String hint) {
        for (Vocabulary task : tasks) {
            System.err.println(hint + task.question + " :: " + task.answer);
        }
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
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            String[] parts = line.split("\\s*::\\s*");
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
                    if (line.isEmpty() || line.startsWith("//")) {
                        continue;
                    }
                    String[] parts = line.split("\\s*::\\s*");
                    if (parts.length >= 2) {
                        try {
                            tasks.add(new DropTask.Vocabulary(parts[0], parts[1]));
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
        if (taskNumber >= tasks.size()) {
            taskNumber = 0;
        }
        return tasks.get(taskNumber).question;
    }

    public String getAnswer() {
        return tasks.get(taskNumber).answer;
    }

    public Color getColor() {
        return Color.WHITE;
    }
}
