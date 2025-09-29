package mathtrainer;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class ElevenLabsTTS {

    private static String modelId = "eleven_multilingual_v2";
    private static HttpClient httpClient = null;
    private static boolean debug = true;
    private static final String apiKey = "sk_27db7a41af907eadeaa4aa21a3689d66112efd0cfd0f28f0";

    // Initialize HttpClient when class loads
    static {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public static boolean textToSpeech(String text, String outputPath) {

        String voiceId = "fN1W6XuvUjfFspGiGNOq"; // Michael R. Alvers
        voiceId = "NBqeXKdZHweef6y0B67V"; //sx7WD8TJIOrk5RQOptDH
        voiceId = "sx7WD8TJIOrk5RQOptDH"; //

        try {
            // Fix the path - ensure it's "resources/sound/" not "resourcessound/"
            Path output = Paths.get(outputPath);

            // Create parent directories if they don't exist
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }

            String jsonPayload = createPayload(text, modelId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/" + voiceId))
                    .header("Accept", "audio/wav")
                    .header("Content-Type", "application/json")
                    .header("xi-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            System.out.println("Response Code: " + response.statusCode());

            if (response.statusCode() == 200) {
                Files.write(output, response.body());
                if (debug) {
                    System.out.println("✅ File saved: " + output.toAbsolutePath());
                }
                return true;
            } else {
                handleError(response);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Exception in textToSpeech: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String createPayload(String text, String modelId) {
        return new JSONObject()
                .put("text", text)
                .put("model_id", modelId)
                .put("voice_settings", new JSONObject()
                        .put("stability", 0.3)      // Lower = more flexible pronunciation
                        .put("similarity_boost", 0.80) // Higher = stick closer to your voice
                        .put("style", 0.7)          // Higher = more expressive (helps with accent)
                        .put("use_speaker_boost", true))
                .toString();
    }

    private static void handleError(HttpResponse<byte[]> response) {
        System.err.println("❌ HTTP Error: " + response.statusCode());
        if (response.body() != null) {
            String errorBody = new String(response.body());
            System.err.println("Error details: " + errorBody);

            // Try to parse as JSON for better error message
            try {
                JSONObject errorJson = new JSONObject(errorBody);
                if (errorJson.has("detail")) {
                    JSONObject detail = errorJson.getJSONObject("detail");
                    if (detail.has("message")) {
                        System.err.println("Error message: " + detail.getString("message"));
                    }
                }
            } catch (Exception e) {
                // If not JSON, just print the raw body
                System.err.println("Raw error: " + errorBody);
            }
        }
    }

    // Test method
    public static void main(String[] args) {
        String name = "Michael";
        String text = "Hallo, ich bin Michael R. Alvers! Ich lebe in Dresden, Deutschland." +
                "Ich arbeite als Lehrer und liebe meinen Job. Wenn ich nicht arbeite, fliege, " +
                "segle und programmiere ich. Ich liebe das Leben! \"Vita Somnium Breve\"  ist mein"+
                "Lebensmotto.";

        text = "Lysann, Álvaro, Nic, Erwin, Sarina, Ellen, Frederike, Colin," +
                "Oskar, Frederik, Ernst";

        String outputFile = "resources/sound/" + name + "_MRA.wav"; // Fixed path

        System.out.println("▶ Generating with ElevenLabs...");
        boolean success = textToSpeech(text, outputFile);

        if (success) {
            System.out.println("✅ Successfully generated: " + outputFile);
        } else {
            System.out.println("❌ Generation failed");
        }
    }
}