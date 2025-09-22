package mathtrainer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class WolframAlphaSolver {

    public static void main(String[] args) {
        getSolutions("\\( \\sqrt{n} + \\sqrt{n} + \\sqrt{n} + \\sqrt{n} = n \\)");
    }

    protected static void getSolutions(String task) {
        try {
            String equation = URLEncoder.encode(task, StandardCharsets.UTF_8);
            String appId = "THWHXPV8RL"; // Replace with your actual App ID
            String urlStr = "https://api.wolframalpha.com/v2/query?input=" + equation + "&appid=" + appId + "&output=json";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            //System.out.println("Response Code: " + responseCode);

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
                    return;
                }

                // Loop through all the pods to find the "Solution" or "Results" one
                for (int i = 0; i < pods.size(); i++) {
                    JsonObject pod = pods.get(i).getAsJsonObject();
                    String title = pod.get("title").getAsString();

                    if (title.contains("Solution") || title.contains("Result")) {

                        JsonArray subpods = pod.getAsJsonArray("subpods");

                        if (!subpods.isEmpty()) {

                            //System.out.println("Pod Title: " + title); // Show what pod was found

                            for (int j = 0; j < subpods.size(); j++) {
                                JsonObject subpod = subpods.get(j).getAsJsonObject();
                                String plaintext = subpod.get("plaintext").getAsString();

                                if (title.contains("Result") && !plaintext.isEmpty()) {
                                    System.out.println("Solution for: \\( " + plaintext + " \\)");
                                } else {
                                    System.out.println("Solution " + (j+1) + ":   \\( " + plaintext + " \\)");
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
    }
}