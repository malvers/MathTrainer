package mathtrainer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class WolframAlphaSolver {

    public static void main(String[] args) {
        // Test both formats - both should work!
        List<String> solutions1 = getSolutions("\\( \\sqrt{n} + \\sqrt{n} + \\sqrt{n} + \\sqrt{n} = n \\)");
        List<String> solutions2 = getSolutions("2x = 10");
        List<String> solutions3 = getSolutions("x^2 - 4 = 0");

        System.out.println("Solutions for \\( \\sqrt{n} + \\sqrt{n} + \\sqrt{n} + \\sqrt{n} = n \\):");
        solutions1.forEach(System.out::println);

        System.out.println("\nSolutions for 2x = 10:");
        solutions2.forEach(System.out::println);

        System.out.println("\nSolutions for x^2 - 4 = 0:");
        solutions3.forEach(System.out::println);
    }

    protected static List<String> getSolutions(String task) {
        List<String> solutions = new ArrayList<>();

        try {
            // Just encode the task as-is - WolframAlpha can handle LaTeX notation!
            String equation = URLEncoder.encode(task, StandardCharsets.UTF_8);
            String appId = "THWHXPV8RL"; // Replace with your actual App ID
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

                // Check if the query was successful
                boolean success = queryResult.get("success").getAsBoolean();
                if (!success) {
                    System.err.println("WolframAlpha could not understand the query: " + task);
                    return solutions;
                }

                JsonArray pods = queryResult.getAsJsonArray("pods");

                if (pods == null) {
                    System.err.println("No pods found for: " + task);
                    return solutions;
                }

                // Look for solution pods with more comprehensive titles
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
                                    // Format as LaTeX for consistency
                                    String latexSolution = "\\(" + plaintext + "\\)";
                                    solutions.add(latexSolution);
                                }
                            }
                        }
                    }
                }

            } else {
                System.out.println("API request failed with response code: " + responseCode);
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println("Error: " + errorLine);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing task: " + task);
            e.printStackTrace();
        }

        return solutions;
    }
}