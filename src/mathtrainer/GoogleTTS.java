package mathtrainer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleTTS {


    private static final String API_KEY = System.getenv("GOOGLE_TTS_API_KEY");

    // Pick first available Standard voice for the language
    private static JSONObject pickStandardVoice(String languageCode) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ GOOGLE_TTS_API_KEY not set.");
            return null;
        }

        try {
            var voices = getObjects();

            for (int i = 0; i < voices.length(); i++) {
                JSONObject voice = voices.getJSONObject(i);
                String name = voice.getString("name");

                // Prefer voices that don't require special models
                JSONArray langs = voice.getJSONArray("languageCodes");
                for (int j = 0; j < langs.length(); j++) {
                    if (langs.getString(j).equals(languageCode)) {
                        // Return voice info including name
                        return new JSONObject()
                                .put("name", name)
                                .put("languageCode", languageCode);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.err.println("❌ No voice found for " + languageCode);
        return null;
    }

    private static JSONArray getObjects() throws IOException {
        URL url = new URL("https://texttospeech.googleapis.com/v1/voices?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        JSONObject json = new JSONObject(response.toString());
        JSONArray voices = json.getJSONArray("voices");
        return voices;
    }

    // Build TTS JSON with proper voice configuration
    private static JSONObject buildTTSRequest(String text, JSONObject voiceInfo, String audioEncoding) {

        return new JSONObject()
                .put("input", new JSONObject().put("text", text))
                .put("voice", new JSONObject()
                        .put("languageCode", voiceInfo.getString("languageCode"))
                        .put("name", voiceInfo.getString("name")))
                .put("audioConfig", new JSONObject()
                        .put("audioEncoding", audioEncoding)
                        .put("speakingRate", 1.0)
                        .put("pitch", 0.0)
                        .put("volumeGainDb", 0.0));
    }

    // Alternative: Use specific known-working voices
    private static JSONObject getKnownWorkingVoice(String languageCode) {
        return switch (languageCode) {
            case "en-GB" -> new JSONObject()
                    .put("name", "en-GB-Standard-A")
                    .put("languageCode", "en-GB");
            case "de-DE" -> new JSONObject()
                    .put("name", "de-DE-Standard-A")
                    .put("languageCode", "de-DE");
            default -> new JSONObject()
                    .put("name", "en-US-Standard-A")
                    .put("languageCode", "en-US");
        };
    }

    public static byte[] textToSpeechBytes(String text, String languageCode, String audioEncoding) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ GOOGLE_TTS_API_KEY not set.");
            return null;
        }

        try {
            JSONObject voiceInfo = getKnownWorkingVoice(languageCode);
            JSONObject jsonRequest = buildTTSRequest(text, voiceInfo, audioEncoding);
            String jsonString = jsonRequest.toString();

            HttpURLConnection connection = (HttpURLConnection)
                    new URL("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY)
                            .openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }

            if (connection.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }

                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                return Base64.getDecoder().decode(jsonResponse.getString("audioContent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Generate MP3 (original method)
    public static boolean ttsMP3(String text, String languageCode, String outputFile) {
        return textToSpeechWithFormat(text, languageCode, outputFile, "MP3");
    }

    // Generate WAV format
    public static boolean ttsWAV(String text, String languageCode, String outputFile) {
        return textToSpeechWithFormat(text, languageCode, outputFile, "LINEAR16");
    }

    // Main method supporting both MP3 and WAV
    private static boolean textToSpeechWithFormat(String text, String languageCode, String outputFile, String audioEncoding) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("❌ GOOGLE_TTS_API_KEY not set.");
            return false;
        }

        try {
            // Use known working voices instead of auto-detection
            JSONObject voiceInfo = getKnownWorkingVoice(languageCode);
            System.out.println("Using voice: " + voiceInfo.getString("name") + " | Format: " + audioEncoding);

            JSONObject jsonRequest = buildTTSRequest(text, voiceInfo, audioEncoding);
            String jsonString = jsonRequest.toString(2);

            HttpURLConnection connection = (HttpURLConnection)
                    new URL("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY)
                            .openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            InputStream responseStream = (responseCode == 200) ?
                    connection.getInputStream() : connection.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }

            String responseString = responseBuilder.toString();
            System.out.println("Response Code: " + responseCode);

            if (responseCode != 200) {
                // If model is required, try with model specification
                if (responseString.contains("model name")) {
                    System.out.println("Trying with model specification...");
                    return textToSpeechWithModel(text, languageCode, outputFile, audioEncoding);
                }
                return false;
            }

            JSONObject jsonResponse = new JSONObject(responseString);
            if (!jsonResponse.has("audioContent")) {
                System.err.println("❌ No audioContent in response");
                return false;
            }

            byte[] audioBytes = Base64.getDecoder().decode(jsonResponse.getString("audioContent"));
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                out.write(audioBytes);
            }

            String formatName = audioEncoding.equals("LINEAR16") ? "WAV" : "MP3";
            System.out.println("✅ " + formatName + " saved as: " + outputFile + " (" + audioBytes.length + " bytes)");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Alternative method with model specification
    private static boolean textToSpeechWithModel(String text, String languageCode, String outputFile, String audioEncoding) {
        try {
            JSONObject voiceInfo = getKnownWorkingVoice(languageCode);

            JSONObject jsonRequest = new JSONObject()
                    .put("input", new JSONObject().put("text", text))
                    .put("voice", new JSONObject()
                            .put("languageCode", voiceInfo.getString("languageCode"))
                            .put("name", voiceInfo.getString("name")))
                    .put("audioConfig", new JSONObject()
                            .put("audioEncoding", audioEncoding))
                    .put("model", "latest-long"); // Add model specification

            String jsonString = jsonRequest.toString(2);

            HttpURLConnection connection = (HttpURLConnection)
                    new URL("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY)
                            .openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }

                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                byte[] audioBytes = Base64.getDecoder().decode(jsonResponse.getString("audioContent"));
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    out.write(audioBytes);
                }
                String formatName = audioEncoding.equals("LINEAR16") ? "WAV" : "MP3";
                System.out.println("✅ " + formatName + " saved with model specification: " + outputFile);
                return true;
            }
        } catch (Exception e) {
            System.err.println("❌ Error with model: " + e.getMessage());
        }
        return false;
    }

    // Method to convert existing MP3 to WAV using Java Sound API
    public static boolean convertMp3ToWav(String mp3File, String wavFile) {
        try {
            // This is a simplified conversion - you might want to use a proper library like JLayer
            // For now, this just copies and renames, but proper conversion requires decoding
            File mp3 = new File(mp3File);
            File wav = new File(wavFile);

            if (!mp3.exists()) {
                System.err.println("❌ MP3 file not found: " + mp3File);
                return false;
            }

            // Simple file copy (this won't actually convert audio format)
            try (FileInputStream in = new FileInputStream(mp3);
                 FileOutputStream out = new FileOutputStream(wav)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            System.out.println("⚠️  Simple file copy performed. For proper MP3 to WAV conversion, use a dedicated audio library.");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error converting MP3 to WAV: " + e.getMessage());
            return false;
        }
    }

    // Ultra-simple fallback method with format support
    private static void simpleTTS(String text, String outputFile, String audioEncoding) throws Exception {
        String url = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY;

        String json = "{\"input\":{\"text\":\"" + text.replace("\"", "\\\"") +
                "\"},\"voice\":{\"languageCode\":\"de-DE\",\"name\":\"de-DE-Standard-A\"}" +
                ",\"audioConfig\":{\"audioEncoding\":\"" + audioEncoding + "\"}}";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        if (conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            String audioContent = response.split("\"audioContent\":\"")[1].split("\"")[0];

            byte[] audioBytes = Base64.getDecoder().decode(audioContent);
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                out.write(audioBytes);
            }
            String formatName = audioEncoding.equals("LINEAR16") ? "WAV" : "MP3";
            System.out.println("✅ Simple TTS worked! Format: " + formatName);
        }
    }

    public static void main(String[] args) {
        // Test with both MP3 and WAV
        String text = "Mein Name ist Hugo und ich wohne in Meißen. Ich lerne in der Schule in der 5. Klasse.";
        //String text = VoiceTextDE.theText;

        // Test WAV (new functionality)
//        boolean successWav = ttsWAV(text, "en-US", "google-tts-output.wav");
        boolean successWav = ttsWAV(text, "de-DE", "voiceTextDE.wav");

        if (successWav) {
            System.out.println("🎉 WAV conversions complete!");
        } else {
            System.out.println("❌ Conversion failed.");

        }
    }
}