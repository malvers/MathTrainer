package mathtrainer;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import mratools.MTools;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class RandomNamePicker implements NativeKeyListener {

    private final ArrayList<OneStudent> names;
    private final Random random;
    private volatile boolean running = true;
    private static Clip clip;

    public RandomNamePicker(ArrayList<OneStudent> names) {

        System.out.println("RandomNamePicker");

        this.names = names;
        this.random = new Random();

        setupNativeHook();
        startKeepAlive(); // Start the thread that keeps JVM alive
    }

    private void setupNativeHook() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);

            // Add shutdown hook for cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    GlobalScreen.unregisterNativeHook();
                    System.out.println("Native hook unregistered.");
                } catch (NativeHookException e) {
                    e.printStackTrace();
                }
            }));

            System.err.println("Registered native hook ...");

        } catch (NativeHookException e) {
            System.err.println("Failed to register native hook: " + e.getMessage());
            System.exit(1);
        }
    }

    private void startKeepAlive() {
        // This thread will keep the JVM alive
        Thread keepAliveThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000); // Sleep in 1-second chunks
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("Keep-alive thread stopped.");
        });
        keepAliveThread.setDaemon(false); // Important: non-daemon thread keeps JVM alive
        keepAliveThread.start();
        System.out.println("Keep-alive thread started.");
    }

    public void stop() {
        running = false;
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {

        //System.out.println("nativeKeyPressed: " + e.getKeyCode());

        if (e.getKeyCode() == NativeKeyEvent.VC_9) {
            pickRandomName();
        }

        // Escape key to exit
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            System.out.println("Escape pressed - shutting down...");
            stop();
            System.exit(0);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        // Not used but required by interface
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        // Not used but required by interface
    }

    protected void pickRandomName() {

        //System.out.println("pickRandomName: ");
        OneStudent randomStudent = new OneStudent("no name");
        if (!names.isEmpty()) {
            randomStudent = names.get(random.nextInt(names.size()));
            setAndPlaySound(randomStudent.name);
            System.out.println("Selected: " + randomStudent.name);
        }
    }

    private static void setAndPlaySound(String name) {

        try {
            setSound(name);
            clip.start();
        } catch (Exception ex) {
            MTools.println("Error with playing sound: " + name);
        }
    }

    private static void setSound(String name) {
        // Try loading via URL first (works great in JARs!)

        String soundName = "sound/" + name + ".wav";
        URL soundUrl = MathTrainer.class.getResource("/" + soundName);

        if (soundUrl == null) {
            MTools.println("setSound: resource not found: " + name);
            return;
        }

        try {
            // 🚀 Use URL directly — this works perfectly in JARs!
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            // clip.loop(10); // Uncomment if needed

        } catch (UnsupportedAudioFileException e) {
            MTools.println("setSound: Unsupported audio format for: " + name);
            e.printStackTrace();
        } catch (IOException e) {
            MTools.println("setSound: IO error loading: " + name);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            MTools.println("setSound: Audio line unavailable for: " + name);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        ArrayList<OneStudent> students = new ArrayList<>();
        students.add(new OneStudent("Alice"));
        students.add(new OneStudent("Bob"));
        students.add(new OneStudent("Charlie"));
        students.add(new OneStudent("Diana"));
        students.add(new OneStudent("Eve"));

        // This will now keep running automatically!
        new RandomNamePicker(students);

        // Main thread can exit immediately - the keep-alive thread keeps JVM running
        System.out.println("Main thread finished, but JVM remains alive...");
    }
}