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

public class EnglishTask {

    String name;
    String question = "Question";
    String answer = "Answer";
    private final List<Vocabulary> tasks = new ArrayList<>();
    private static int taskNumber = 0;

    public EnglishTask(String nameIn) {

        try {
            //readTasksFromFile(Path.of(MathTrainer.workingDirectory + "english/english.txt"));
            readTasksFromResource("/english/english.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        name = nameIn;
    }

    public static void nextTask() {
        taskNumber++;
    }

    private static class Vocabulary {

        String english;
        String german;

        public Vocabulary(String en, String ge) {
            english = en;
            german = ge;
        }
    }

    private void readTasksFromFile(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("[,\\s]+");
            if (parts.length >= 2) {
                try {
                    tasks.add(new Vocabulary(parts[0], parts[1]));
                    //System.out.println(parts[0] + " - " + parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line: " + line);
                }
            }
        }
        Collections.shuffle(tasks);
    }

    private void readTasksFromResource(String resourcePath) throws IOException {

        try (InputStream in = MathTrainer.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                List<String> lines = reader.lines().toList();

                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split("[,\\s]+");
                    if (parts.length >= 2) {
                        try {
                            tasks.add(new EnglishTask.Vocabulary(parts[0], parts[1]));
//                            System.out.println(parts[0] + " - " + parts[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping line: " + line);
                        }
                    }
                }
                Collections.shuffle(tasks);
            }
        }
    }


    String getTaskString() {

        return tasks.get(taskNumber).english;
    }

    public void print(int i) {

        String space = "";
        if (i < 10) {
            space = " ";
        }
        System.out.println(name + " ->\t" + space + i + " ->\t" + question);
    }

    public String getResult() {
        return tasks.get(taskNumber).german;
    }

    public Color getColor() {
        return Color.WHITE;
    }
}
