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

    private static RandomNamePicker instance;
    private ArrayList<OneStudent> names;
    private ArrayList<OneStudent> availableNames;
    private final Random random;
    private volatile boolean running = true;
    private static Clip clip;
    private int roundNumber = 1;
    private static boolean isHookRegistered = false;

    // Private constructor to prevent multiple instances
    private RandomNamePicker(ArrayList<OneStudent> names) {
        this.names = new ArrayList<>(names);
        this.availableNames = new ArrayList<>(names);
        this.random = new Random();

        setupNativeHook();
        startKeepAlive();

        System.out.println("Total students: " + names.size());
        System.out.println("Round 1 started - " + availableNames.size() + " students available");
    }

    // Public method to get the singleton instance
    public static synchronized RandomNamePicker getInstance(ArrayList<OneStudent> names) {
        if (instance == null) {
            instance = new RandomNamePicker(names);
        } else {
            instance.updateStudents(names);
        }
        return instance;
    }

    // Method to update students without creating new instance
    public void updateStudents(ArrayList<OneStudent> newStudents) {
        this.names.clear();
        this.names.addAll(newStudents);
        this.availableNames.clear();
        this.availableNames.addAll(newStudents);
        this.roundNumber = 1;
        System.out.println("Updated student list! Total: " + names.size());
        System.out.println("Round 1 restarted - " + availableNames.size() + " students available");
    }

    private void setupNativeHook() {
        try {
            // Check if already registered
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
                isHookRegistered = true;
                System.err.println("Registered native hook ...");
            } else {
                System.err.println("Native hook already registered.");
                // Remove any existing listeners to avoid duplicates
                GlobalScreen.removeNativeKeyListener(this);
            }

            GlobalScreen.addNativeKeyListener(this);

            // Add shutdown hook for cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                cleanup();
            }));

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
        cleanup();
    }

    private void cleanup() {
        try {
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.removeNativeKeyListener(this);
                GlobalScreen.unregisterNativeHook();
                isHookRegistered = false;
                System.out.println("Native hook unregistered.");
            }
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
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
        if (availableNames.isEmpty()) {
            // Reset the pool when everyone has been picked
            availableNames = new ArrayList<>(names);
            roundNumber++;
            System.out.println("\n--- Round " + roundNumber + " started! Everyone available again ---");
            System.out.println("Students available: " + availableNames.size());
        }

        OneStudent randomStudent = availableNames.remove(random.nextInt(availableNames.size()));
        setAndPlaySound(randomStudent.name);

        System.out.println("\nSelected: " + randomStudent.name);
        System.out.println("Round: " + roundNumber);
        System.out.println("Students left this round: " + availableNames.size());

        if (availableNames.isEmpty()) {
            System.out.println("--- Round " + roundNumber + " completed! ---");
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
        String soundName = "sound/" + name + ".wav";
        URL soundUrl = MathTrainer.class.getResource("/" + soundName);

        if (soundUrl == null) {
            MTools.println("setSound: resource not found: " + name);
            return;
        }

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
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
        students.add(new OneStudent("Lea"));
        students.add(new OneStudent("Tim"));
        students.add(new OneStudent("Fritz"));
        students.add(new OneStudent("Frederike"));
        students.add(new OneStudent("Eva"));
        students.add(new OneStudent("Peter"));
        students.add(new OneStudent("Nic"));
        students.add(new OneStudent("Edgar"));
        students.add(new OneStudent("Erwin"));
        students.add(new OneStudent("Frederik"));
        students.add(new OneStudent("Lysann"));
        students.add(new OneStudent("Sarina"));
        students.add(new OneStudent("Ellen"));
        students.add(new OneStudent("Aurora"));
        students.add(new OneStudent("Laura"));

        // Use the singleton pattern
        RandomNamePicker picker = RandomNamePicker.getInstance(students);

        // Main thread can exit immediately - the keep-alive thread keeps JVM running
        System.out.println("Main thread finished, but JVM remains alive...");

        // Example of switching teams later:
        // ArrayList<OneStudent> otherTeam = getOtherTeamStudents();
        // RandomNamePicker.getInstance(otherTeam); // This updates the existing instance
    }
}