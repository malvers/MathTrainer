package mathtrainer;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioSystem;
import java.util.Locale;

public class MaryTTSDemo {

    public static void main(String[] args) throws Exception {

        MaryInterface mary = new LocalMaryInterface();
        mary.setLocale(Locale.GERMAN);
        AudioInputStream audio = mary.generateAudio("My name is Michael and I live in Dresden.");

        // Play the audio immediately
        playAudio(audio);

        // Optional: Keep the program running until audio finishes
        Thread.sleep(3000); // Adjust based on audio length
    }

    private static void playAudio(AudioInputStream audioStream) throws Exception {
        // Get audio format information
        DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());

        // Create and open the clip
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(audioStream);

        // Start playing
        clip.start();

        // Optional: Add a listener to know when playback finishes
        clip.addLineListener(event -> {
            if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                clip.close();
                System.out.println("Playback finished");
            }
        });
    }
}