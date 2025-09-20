package mathtrainer;

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

public class ComplexMathTask {

    String name;
    protected static final ArrayList<Vocabulary> tasks = new ArrayList<>();
    private static int taskNumber = 0;

    public ComplexMathTask(String nameIn, boolean read) {

        if (read) {
            try {
                //readTasksFromFile(Path.of(MathTrainer.workingDirectory + "latin/latin.txt"));

                readTasksFromResource("/complexmath/complexmath.txt");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        name = nameIn;
    }

    protected static void addTask(ComplexMathTask.Vocabulary vocabulary) {
        tasks.add(vocabulary);
        Collections.shuffle(tasks);
    }
    public static void nextTask() {
        taskNumber++;
    }

    protected static class Vocabulary {

        String question;
        String answer;

        public Vocabulary(String en, String ge) {
            question = en;
            answer = ge;
        }
    }

    protected void readTasksFromFile(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s*-\\s*");
            if (parts.length >= 2) {
                try {
                    tasks.add(new Vocabulary(parts[0], parts[1]));
                    System.out.println(parts[0] + " - " + parts[1]);
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
                    String[] parts = line.split("\\s*:\\s*");
                    if (parts.length >= 2) {
                        try {
                            tasks.add(new Vocabulary(parts[0], parts[1]));
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


    String getQuestion() {
        return tasks.get(taskNumber).question;
    }

    public String getAnswer() {
        return tasks.get(taskNumber).answer;
    }
}
