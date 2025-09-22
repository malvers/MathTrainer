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

        List<String> solutions = getSolutions("\\( \\sqrt{n} + \\sqrt{n} + \\sqrt{n} + \\sqrt{n} = n \\)");
        solutions = getSolutions("\\( 2x = 10 \\)");

        if (solutions == null) {
            System.err.println("No solutions found");
            System.exit(-1);
        }
        System.out.println("Found " + (solutions.size() - 1) + " solutions for: " + solutions.getFirst());

        for (int i = 1; i < solutions.size(); i++) {
            System.out.println("Solution " + i + ": " + solutions.get(i));
        }
    }

    protected static List<String> getSolutions(String task) {
        List<String> solutions = new ArrayList<>();

        try {
            String equation = URLEncoder.encode(task, StandardCharsets.UTF_8);
            String appId = "THWHXPV8RL"; // my personal App ID
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

                // --- Start of JSON Parsing ---
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

                // Get the main queryresult object
                JsonObject queryResult = jsonObject.getAsJsonObject("queryresult");
                JsonArray pods = queryResult.getAsJsonArray("pods");

                if (pods == null) {
                    System.err.println("pods == null");
                    return null;
                }

                // Loop through all the pods to find the "Solution" or "Results" one
                for (int i = 0; i < pods.size(); i++) {
                    JsonObject pod = pods.get(i).getAsJsonObject();
                    String title = pod.get("title").getAsString();

                    if (title.contains("Solution") || title.contains("Result")) {
                        JsonArray subpods = pod.getAsJsonArray("subpods");

                        if (!subpods.isEmpty()) {
                            for (int j = 0; j < subpods.size(); j++) {
                                JsonObject subpod = subpods.get(j).getAsJsonObject();
                                String plaintext = subpod.get("plaintext").getAsString();

                                if (!plaintext.isEmpty()) {
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
            e.printStackTrace();
        }

        return solutions;
    }
}