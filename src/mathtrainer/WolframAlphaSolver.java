package mathtrainer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class WolframAlphaSolver {

    public static void main(String[] args) {

        checkFile("/tasks/Roots2.0.txt");

//        List<String> solutions1 = getSolutions("\\( \\sqrt{n} + \\sqrt{n} + \\sqrt{n} + \\sqrt{n} = n \\)");
//        List<String> solutions1 = getSolutions("\\( \\sqrt{4n} + \\sqrt{9n} = 5\\sqrt{n} \\)");
//        solutions1.forEach(System.out::println);
    }

    private static void checkFile(String fileName) {
        try {
            ComplexMathTask.readTasksFromResource(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayList<ComplexMathTask.Vocabulary> tasks = ComplexMathTask.getTasks();

        System.out.println("Number tasks: " + tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            ComplexMathTask.Vocabulary task = tasks.get(i);
            System.out.println();
            System.out.println(i + ". task: " + task.question);
            List<String> solutions = getSolutions(task.question);

            for (String sol : solutions) {
                String cleanSolution = task.answer.replace("( ", "(");
                cleanSolution = cleanSolution.replace(" \\)", "\\)");
                System.out.println("s: " + sol.trim());
                System.out.println("a: " + cleanSolution.trim());
            }

        }
    }

    protected static List<String> getSolutions(String task) {
        List<String> solutions = new ArrayList<>();

        try {
            String cleanedTask = task.replace("\\(", "").replace("\\)", "").trim();
            String equation = URLEncoder.encode(cleanedTask, StandardCharsets.UTF_8);
            String appId = "THWHXPV8RL";
            String urlStr = "https://api.wolframalpha.com/v2/query?input=" + equation + "&appid=" + appId + "&output=json";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                JsonObject queryResult = jsonObject.getAsJsonObject("queryresult");

                boolean success = queryResult.get("success").getAsBoolean();
                if (!success) {
                    System.err.println("WolframAlpha could not understand the query: " + cleanedTask);
                    return solutions;
                }

                JsonArray pods = queryResult.getAsJsonArray("pods");
                if (pods == null) {
                    System.err.println("No pods found for: " + cleanedTask);
                    return solutions;
                }

                for (int i = 0; i < pods.size(); i++) {
                    JsonObject pod = pods.get(i).getAsJsonObject();
                    String title = pod.get("title").getAsString().toLowerCase();

                    if (title.contains("solution") || title.contains("result") ||
                            title.contains("root") || title.contains("zero") ||
                            title.contains("solve") || title.contains("answer")) {

                        JsonArray subpods = pod.getAsJsonArray("subpods");
                        if (subpods != null && !subpods.isEmpty()) {
                            for (int j = 0; j < subpods.size(); j++) {
                                JsonObject subpod = subpods.get(j).getAsJsonObject();
                                String plaintext = subpod.get("plaintext").getAsString();

                                if (!plaintext.isEmpty()) {
                                    if (plaintext.equalsIgnoreCase("true")) {
                                        solutions.add("\\( \\forall n \\geq 0 \\)");//
                                    } else {
                                        solutions.add("\\(" + plaintext + "\\)");
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                System.out.println("API request failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error processing task: " + task);
            e.printStackTrace();
        }

        return solutions;
    }
}