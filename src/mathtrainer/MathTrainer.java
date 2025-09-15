package mathtrainer;

import MyTools.Make;
import mratools.MTools;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MathTrainer extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private static JFrame frame;
    private static Clip clip;
    private static final ArrayList<HighScorePair> allHighScores = new ArrayList<>();
    private final String sound1 = "sound/Jeopardy.wav";
    private final String sound2 = "sound/Madonna - Frozen.wav";
    //    private final String sound1 = "sound/Jeopardy.wav";
//    private final String sound2 = "sound/Madonna - Frozen.wav";
    private final String soundOnDisplay = sound2;
    private Timer timer;
    private MyCountDown countDown;

    private ArrayList<ColorSheme> allColorSchemes = new ArrayList<>();
    private ArrayList<Team> alleKlassen = new ArrayList<>();
    private Series series = new Series(10);

    private AllMathematicsTasks allMathematicsTasks = new AllMathematicsTasks();
    private AllEnglishTasks allEnglishTasks = new AllEnglishTasks();
    private AllHistoryTasks allHistoryTasks = new AllHistoryTasks();
    private AllLatinTasks allLatinTasks = new AllLatinTasks();

    //private File[][] imagesMatrixURL = new File[10][10];
    private URL[][] imagesMatrixURL = new URL[10][10];
    private BufferedImage[][] bufimagesMatrixURL = new BufferedImage[10][10];

    BufferedImage bgImg = null;

    private final int fontSizeSchueler = 26;
    private int numberTasksProSchueler = 6;
    private int colorSchemeId = 0;
    private int fontSizeNumbers = 220;
    private int taskCounter = 0;
    private int iterationCount = 0;

    private final float factorDrawSchueler = 1.4f;
    private float transparency = 0.5f;

    private int actualKlasse;

    private long timerStart;
    private long deltaT;
    private long finalDeltaT;

    private final String settingsFileName = "MatheTrainer.binary.settings";
    private String pinnedName = "";

    private boolean timeStartIsRested = false;
    public boolean drawSchueler = false;
    private boolean showDuration = false;
    private boolean beginning = true;
    public boolean drawHelp = false;
    public boolean drawSettings = false;
    private boolean limitedToSelectedSeries = false;
    private boolean debugMode = false;

    private int timePerTaskAndStudent;
    private int pinnedHighScore = 10000;
    private int countDownCounter = -1;
    private int penalty = 0;
    private boolean shallWriteHighScore = false;
    private boolean drawAufgabe = true;
    private final int nextTaskCountDownFrom = 5;
    private final int countDownFrom = 9;
    private int nextTaskCountDown = nextTaskCountDownFrom;
    private Timer nextTaskIn;
    private float soundVolume = 1.0f;
    boolean playMusic = true;
    private boolean isWindows;
    private boolean drawHighScore = false;
    private boolean showAndPlayName = true;
    protected int taskType = TaskTypes.HISTORY;

    public MathTrainer() {

        MTools.init("MathTrainerLogDebug.txt", false);

        new TextFileDropTarget(this);

        isWindows = getOperatingSystem().contains("Windows");

        setFocusable(true);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        /// make sure also whe window is closed via X or cmd q all settings are written
        Thread t = new Thread(() -> {
            writeSettings();
        });
        Runtime.getRuntime().addShutdownHook(t);

        readSettings();
        initColors();


        actualKlasse = 1; /// Alvers = 0, Wischnewski = 1

        initBeginning();

        if (playMusic) {
            setAndPlaySound(soundOnDisplay);
            setVolume();
        }
    }

//    private static void readWorkingDirectory() {
//
//        File wd;
//        List<String> lines = null;
//        try {
//            lines = Files.readAllLines(Path.of(MathTrainer.workingDirectory + "workingDirectory.txt"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        if (lines != null) {
//            workingDirectory = lines.getFirst();
//        }
//
//        MTools.init("mathDebug.txt", true);
//        MTools.println("wd: " + workingDirectory);
//    }

    public void initBeginning() {

        System.out.println("\ninitBeginning:");

        timeStartIsRested = false;
        beginning = true;
        taskCounter = 0;
        iterationCount = 0;
        countDownCounter = -1;
        penalty = 0;

        showDuration = false;
        drawSchueler = false;
        drawHighScore = false;
        shallWriteHighScore = false;
        drawHelp = false;
        drawSettings = false;

        if (countDown != null) {
            countDown.cancel();
        }

//        readImagesOld();
        readImages();

        //System.out.setLogFileName("mathtrainer.log");

        System.out.println("readImages done ...");

        initNames(true);

        System.out.println("initNames done ...");

        initAllTasks(true);

        System.out.println("initAllTasks done ...");

        setImageForTask();

        System.out.println("setImageForTask done ...");

        timer = new Timer();
        resetTimerStart();

        System.out.println("ready to go ...\n\n");
    }

    private void initNames(boolean shuffle) {

        alleKlassen.clear();

        new Team();
        for (int klassenId = 0; klassenId < Team.teamsString.length; klassenId++) {
            alleKlassen.add(new Team(klassenId));
        }
        if (shuffle) {
            Collections.shuffle(alleKlassen.get(actualKlasse));
        }
    }

    private void initAllTasks(boolean shuffle) {

        allMathematicsTasks.clear();
        for (int i = 0; i < numberTasksProSchueler; i++) {

            Team klasse = alleKlassen.get(actualKlasse);

            for (int j = 0; j < klasse.size(); j++) {

                OneSchueler oneSchueler = klasse.getSchueler(j);

                //System.out.println("oneSchueler: " + oneSchueler.name);

                if (!oneSchueler.anwesend) {
                    System.out.println("nicht anwesend ...");
                    continue;
                }

                //System.out.println("Get operation ...");

                int operation = Operations.getRandomOperation();

                //System.out.println("operation: " + operation);

                allMathematicsTasks.add(new MathTask(oneSchueler.name, series, limitedToSelectedSeries, operation));
                allEnglishTasks.add(new EnglishTask(oneSchueler.name));
                allHistoryTasks.add(new HistoryTask(oneSchueler.name));
                allLatinTasks.add(new LatinTask(oneSchueler.name));

                //System.out.println("After: allTasks.add()");
            }
        }

        if (shuffle) {

            for (Team oneSchuelers : alleKlassen) {
                Collections.shuffle(oneSchuelers);
            }
        }

        Team klasse = alleKlassen.get(actualKlasse);

        for (OneSchueler osch : klasse) {

            for (MathTask allTask : allMathematicsTasks) {

                if (allTask.name.contentEquals(osch.name)) {
                    osch.setNumberTasks(osch.getNumberTasks() + 1);
                    if (osch.getNumberTasks() > numberTasksProSchueler) {
                        osch.setNumberTasks(numberTasksProSchueler);
                    }
                }
            }
        }
        Collections.shuffle(allMathematicsTasks);
        allMathematicsTasks.checkForDoubleNames();

        setImageForTask();
    }

    private void initColors() {

        allColorSchemes.add(new ColorSheme(Color.LIGHT_GRAY, Color.DARK_GRAY, Color.LIGHT_GRAY));
        allColorSchemes.add(new ColorSheme(Color.DARK_GRAY, Color.WHITE, Color.LIGHT_GRAY));
        allColorSchemes.add(new ColorSheme(ColorSheme.darkBlue, ColorSheme.orange, Color.lightGray));
        allColorSchemes.add(new ColorSheme(ColorSheme.darkBlue, ColorSheme.niceGreen, Color.WHITE));
    }

    private void writeSettings() {

        FileOutputStream f;
        try {
            System.out.println("writeSettings: " + settingsFileName);
            f = new FileOutputStream(settingsFileName);
            ObjectOutputStream os = new ObjectOutputStream(f);
            os.writeInt(frame.getX());
            os.writeInt(frame.getY());
            os.writeInt(frame.getWidth());
            os.writeInt(frame.getHeight());
            os.writeInt(colorSchemeId);
            os.writeInt(numberTasksProSchueler);
            os.writeFloat(transparency);
            os.writeBoolean(debugMode);
            os.writeInt(fontSizeNumbers);
            os.writeFloat(soundVolume);
            os.writeBoolean(playMusic);

            Operations.write(os);

            os.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSettings() {

        FileInputStream f;
        try {
            f = new FileInputStream(settingsFileName);
            ObjectInputStream in = new ObjectInputStream(f);
            int x = in.readInt();
            int y = in.readInt();
            int w = in.readInt();
            int h = in.readInt();
            frame.setBounds(x, y, w, h);
            frame.setTitle(getSubject());
            colorSchemeId = in.readInt();
            numberTasksProSchueler = in.readInt();
            if (numberTasksProSchueler < 2) {
                numberTasksProSchueler = 2;
            }
            transparency = in.readFloat();
            debugMode = in.readBoolean();
            fontSizeNumbers = in.readInt();
            soundVolume = in.readFloat();
            playMusic = in.readBoolean();

            System.out.println("playMusic: " + playMusic);

            Operations.read(in);

            in.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSubject() {

        String typeName = switch (taskType) {
            case TaskTypes.ENGLISH -> "English";
            case TaskTypes.MATHEMATICS -> "Mathematics";
            case TaskTypes.LATIN -> "Latin";
            case TaskTypes.HISTORY -> "History";

            default -> "Unknown";
        };
        return typeName;
    }

    @Override
    public int getHeight() {
        return frame.getHeight();
    }

    @Override
    public int getWidth() {
        return frame.getWidth();
    }

    @Override
    public void paint(Graphics g) {

        //super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        ColorSheme cs = allColorSchemes.get(colorSchemeId);

        if (bgImg != null) {
            float scWidth = (float) this.getWidth() / (float) bgImg.getWidth();
            float scHeight = (float) this.getHeight() / (float) bgImg.getHeight();
            if (scWidth < scHeight) {
                g2d.drawImage(bgImg, 0, 0, (int) (bgImg.getWidth() * scHeight), (int) (bgImg.getHeight() * scHeight), this);
            } else {
                g2d.drawImage(bgImg, 0, 0, (int) (bgImg.getWidth() * scWidth), (int) (bgImg.getHeight() * scWidth), this);
            }

            if (beginning) {
                g2d.setColor(ColorSheme.darkBlue);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                drawTransparentCover(g2d);
            }
        }

        g2d.setColor(cs.fgLight);

        drawKlasseAndNumberTasks(g2d);

        if (drawHelp) {
            drawHelp(g2d, cs);
            return;
        }

        if (drawSettings) {
            drawSettings(g2d, cs);
            return;
        }

        if (drawSchueler) {
            drawStudents(g2d, cs);
            drawKlasseAndNumberTasks(g2d);
            return;
        }

        if (drawHighScore) {
            drawHighScore(g2d, cs);
            return;
        }

        if (showDuration) {
            drawDurationAtTheEnd(g2d, getWidth() / 2, cs);
            return;
        }

        drawOperations(g2d, cs);

        if (drawAufgabe) {
            drawTasks(g2d, cs);
        }

        if (!beginning) {
            drawRunningTime(g2d, getWidth() / 2, cs);
        }
    }

    private void drawTransparentCover(Graphics2D g2d) {
        g2d.setColor(new Color(0.0f, 0.0f, 0.0f, transparency));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawKlasseAndNumberTasks(Graphics2D g2d) {

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);

        /// draw number Schueler
        String str = "" + alleKlassen.get(actualKlasse).size() + " Schüler";

        if (pinnedName.length() > 0) {
            g2d.setColor(Color.RED);
            str = pinnedName;
        }

        FontMetrics metrics = g2d.getFontMetrics();
        int width = (int) metrics.getStringBounds(str, g2d).getWidth();
        g2d.drawString(str, (float) (getWidth() / 2.0 - width / 2.0), 26);

        g2d.setColor(Color.WHITE);

        if (limitedToSelectedSeries) {

            str = "limit to selected series";
            width = (int) metrics.getStringBounds(str, g2d).getWidth();
            Color sc = g2d.getColor();
            g2d.setColor(Color.ORANGE);
            g2d.drawString(str, (float) (getWidth() / 2.0 - width / 2.0), 66);
            g2d.setColor(sc);
        }

        g2d.drawString(str, 10, 26);


        g2d.setColor(Color.yellow);
        str = alleKlassen.get(actualKlasse).getNumberTasks() + " Aufgaben";
        width = (int) metrics.getStringBounds(str, g2d).getWidth();
        int xShift = 10;
        if (isWindows) {
            xShift += 20;
        }
        g2d.drawString(str, getWidth() - width - xShift, 26);
    }

    private void drawHelp(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeSchueler));
        g2d.setColor(cs.fgDark);

        int yShift = 40;
        int xShift = 360;
        int i = 1;
        float yPos = factorDrawSchueler * fontSizeSchueler;
        g2d.drawString("H   ", 50, ((yShift + yPos * i)));
        g2d.drawString("Help", xShift, yShift + (yPos * i++));

        g2d.drawString("↓", 50, yShift + (yPos * i));
        g2d.drawString("Start des Trainings", xShift, yShift + (yPos * i++));

        g2d.drawString("Cmd Q", 50, yShift + (yPos * i));
        g2d.drawString("Quit beendet das Programm", xShift, yShift + (yPos * i++));

        g2d.drawString("ESC", 50, yShift + (yPos * i));
        g2d.drawString("Zurück zur Hauptseite", xShift, yShift + (yPos * i++));

        g2d.drawString("+ | -", 50, yShift + (yPos * i));
        g2d.drawString("Erhöht | erniedrigt Aufgaben pro Schüler", xShift, yShift + (yPos * i++));

        g2d.drawString("B", 50, yShift + (yPos * i));
        g2d.drawString("Zurück auf Beginn", xShift, yShift + (yPos * i++));

        g2d.drawString("E", 50, yShift + (yPos * i));
        g2d.drawString("Einstellungen - welche Reihen sollen benutzt werden", xShift, yShift + (yPos * i++));

        g2d.drawString("L", 50, yShift + (yPos * i));
        g2d.drawString("Limit Modus ein/aus", xShift, yShift + (yPos * i++));

        g2d.drawString("M", 50, yShift + (yPos * i));
        g2d.drawString("Hintergrundmusik ein/aus", xShift, yShift + (yPos * i++));

        g2d.drawString("N", 50, yShift + (yPos * i));
        g2d.drawString("Name ein/aus", xShift, yShift + (yPos * i++));

        g2d.drawString("S", 50, yShift + (yPos * i));
        g2d.drawString("Zeigt die Schüler Statistik", xShift, yShift + (yPos * i++));

        g2d.drawString("T | Shift T", 50, yShift + (yPos * i));
        g2d.drawString("Ändere die Tranzparenz des Bildes (+|-)", xShift, yShift + (yPos * i++));

        g2d.drawString("V | Shift V", 50, yShift + (yPos * i));
        g2d.drawString("Ändere die Lautstärke (volume) der Hintergrundmusik (+|-)", xShift, yShift + (yPos * i++));

        g2d.drawString("5 ", 50, yShift + (yPos * i));
        g2d.drawString("Mathematics", xShift, yShift + (yPos * i++));

        g2d.drawString("6 ", 50, yShift + (yPos * i));
        g2d.drawString("English", xShift, yShift + (yPos * i++));

        g2d.drawString("7", 50, yShift + (yPos * i));
        g2d.drawString("History", xShift, yShift + (yPos * i++));

        g2d.drawString("8", 50, yShift + (yPos * i));
        g2d.drawString("Latin", xShift, yShift + (yPos * i++));


        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics metrics = g2d.getFontMetrics();
        String str = "MathTrainer by Dr. Michael R. Alvers - ©2020-2022 - all rights reserved";
        Rectangle2D bounds = metrics.getStringBounds(str, g2d);
        g2d.setColor(Color.GRAY);
        g2d.drawString(str, (float) (getWidth() / 2 - bounds.getWidth() / 2), getHeight() - 30);

        drawKlasseAndNumberTasks(g2d);
    }

    private void drawSettings(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeSchueler));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        int yShift = 80;
        for (int i = 2; i < series.size(); i++) {

            g2d.setColor(colorStore);

            if (series.get(i)) {
                g2d.drawString("✓  ", 50, yShift + factorDrawSchueler * fontSizeSchueler * (i - 2));
            } else {
                g2d.setColor(cs.fgLight);
            }

            String str = "er Reihe";
            g2d.drawString("" + (i) + str, xIndent, yShift + factorDrawSchueler * fontSizeSchueler * (i - 2));
        }

        drawKlasseAndNumberTasks(g2d);
    }

    private void drawHighScore(Graphics2D g2d, ColorSheme cs) {

        readKlassenHighScores();

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeSchueler));

        int xIndent = 160;
        int yShift = 80;
        Color colorStore = g2d.getColor();

        for (int i = 0; i < allHighScores.size(); i++) {
            HighScorePair hsp = allHighScores.get(i);
            String kl = " Klasse Fr. ";
            if (hsp.name.contains("Mueller") || hsp.name.contains("Heidingsfelder")) {
                kl = " Klasse Hr. ";
            } else if (hsp.name.contains("Alvers")) {
                kl = " Klasse Dr. ";
            }
            double val = hsp.value / 1000.0;

            String sVal = "";// (" + System.out.fixedWidthDoubletoString(val, 4, 2) + " Sekunden pro Aufgabe)";
            g2d.setColor(cs.fgDark);
            g2d.drawString((i + 1) + ". Platz: " + kl + hsp.name, 50, yShift + factorDrawSchueler * fontSizeSchueler * i);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString(sVal, 560, yShift + factorDrawSchueler * fontSizeSchueler * i);

        }
    }

    private void drawStudents(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeSchueler));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        Team kl = alleKlassen.get(actualKlasse);

        int yShift = 80;
        for (int i = 0; i < kl.size(); i++) {

            g2d.setColor(colorStore);
            if (kl.get(i).anwesend) {
                g2d.drawString("✅  ", 50, yShift + factorDrawSchueler * fontSizeSchueler * i);
            } else {
                g2d.drawString("❌  ", 50, yShift + factorDrawSchueler * fontSizeSchueler * i);
                g2d.setColor(cs.fgLight);
            }

            String name = kl.get(i).name;
            if (name.contentEquals(pinnedName)) {
                g2d.setColor(Color.RED);
            }
            g2d.drawString(name, xIndent, yShift + factorDrawSchueler * fontSizeSchueler * i);
            int num = kl.get(i).getNumberTasks();
            String str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }

            if (!kl.get(i).anwesend) {
                continue;
            }

            g2d.drawString(str, 500, yShift + factorDrawSchueler * fontSizeSchueler * i);
            num = kl.get(i).numberRightSolutions;
            str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }
            g2d.drawString(str, 800, yShift + factorDrawSchueler * fontSizeSchueler * i);
        }
    }

    private void drawTasks(Graphics2D g2d, ColorSheme cs) {

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));


        if (beginning) {
            drawMathIsImportant(g2d, cs, getWidth() / 2);
            return;
        }

        drawNameAndCountDown(g2d, cs, getWidth() / 2);

        /// draw numbers 1  &  number 2 & result

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));

        Rectangle2D bounds;

        MathTask mathTask = allMathematicsTasks.get(taskCounter);
        EnglishTask englishTask = allEnglishTasks.get(taskCounter);
        HistoryTask historyTask = allHistoryTasks.get(taskCounter);
        LatinTask latinTask = allLatinTasks.get(taskCounter);

        if (debugMode) {
            g2d.setColor(Color.DARK_GRAY);
        } else {
            g2d.setColor(mathTask.getColor());
        }

        if (iterationCount % 2 > 0 || iterationCount == 0) {

            if (iterationCount == 0) {
                iterationCount++;
            }

            String aufgabe = "";

            if (taskType == TaskTypes.ENGLISH) {
                aufgabe = englishTask.getTaskString();
            } else if (taskType == TaskTypes.MATHEMATICS) {
                aufgabe = mathTask.getTaskString();
            } else if (taskType == TaskTypes.HISTORY) {
                aufgabe = historyTask.getTaskString();
            } else if (taskType == TaskTypes.LATIN) {
                aufgabe = latinTask.getTaskString();
            }

            myDrawString(g2d, aufgabe);

        } else {

            String result = "";

            if (taskType == TaskTypes.ENGLISH) {
                result = englishTask.getTaskString() + " - " + englishTask.getResult();
            } else if (taskType == TaskTypes.MATHEMATICS) {
                result = mathTask.getTaskString() + " = " + mathTask.getResult();
            } else if (taskType == TaskTypes.HISTORY) {
                result = historyTask.getTaskString() + ": " + historyTask.getResult();
            } else if (taskType == TaskTypes.LATIN) {
                result = latinTask.getTaskString() + " - " + latinTask.getResult();
            }
            myDrawString(g2d, result);

        }
    }

    private void myDrawString(Graphics2D g2d, String result) {

        int yShift = -60;
        int yPos = getHeight() / 2;
        int xPos = getWidth() / 2;

        Font font = g2d.getFont();

        // Measure string width
        FontMetrics metrics = g2d.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(result, g2d);
        double textWidth = bounds.getWidth();

        // Window width (or drawing area)
        int windowWidth = getWidth(); // e.g. if you are in a JPanel

        // If too wide, scale down font
        if (textWidth > windowWidth * 0.95) {  // keep a small margin
            double scale = (windowWidth * 0.95) / textWidth;
            float newSize = (float) (font.getSize2D() * scale);
            font = font.deriveFont(newSize);
            g2d.setFont(font);

            // Recalculate metrics and bounds with new font
            metrics = g2d.getFontMetrics(font);
            bounds = metrics.getStringBounds(result, g2d);
        }

        // Now center it
        float x = (float) (xPos - bounds.getWidth() / 2.0);
        float y = (float) (yShift + yPos - bounds.getY() / 2.0);

        g2d.drawString(result, x, y);
    }

    private void drawOperations(Graphics2D g2d, ColorSheme cs) {

        g2d.setFont(new Font("Times", Font.PLAIN, 30));
        FontMetrics metrics = g2d.getFontMetrics();

        g2d.setColor(cs.fgLight);

        String sOperations = "";
        int sw = -1;
        int xPos = 10;
        int yPos = getHeight() - 40;

        if (isWindows) {
            yPos -= 30;
        }

        /// PLUS
        g2d.setColor(Color.WHITE);
        sOperations = " + ";
        if (Operations.isOn(Operations.plus) == Operations.ON) {
            g2d.setColor(Color.CYAN);
        }
        g2d.drawString(sOperations, xPos, yPos);
        xPos += metrics.stringWidth(sOperations);

        /// MINUS
        g2d.setColor(Color.WHITE);
        sOperations = " − ";
        if (Operations.isOn(Operations.minus) == Operations.ON) {
            g2d.setColor(Color.RED.darker());
        }
        g2d.drawString(sOperations, xPos, yPos);
        xPos += metrics.stringWidth(sOperations);

        /// TIMES
        g2d.setColor(Color.WHITE);
        sOperations = " × ";
        if (Operations.isOn(Operations.multiply) == Operations.ON) {
            g2d.setColor(Color.GREEN);
        }
        g2d.drawString(sOperations, xPos, yPos);
        xPos += metrics.stringWidth(sOperations);

        /// DIVIDE
        g2d.setColor(Color.WHITE);
        sOperations = " ÷ ";
        if (Operations.isOn(Operations.divide) == Operations.ON) {
            g2d.setColor(Color.ORANGE);
        }
        g2d.drawString(sOperations, xPos, yPos);
    }

    private void drawNameAndCountDown(Graphics2D g2d, ColorSheme cs, int xPos) {

        FontMetrics metrics;
        // TODO: check why cs.fgLight is not taken
//        g2d.setColor(cs.fgLight);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 90));

        if (taskCounter >= 0) {
            metrics = g2d.getFontMetrics();
            String str = "Aufgabe " + (taskCounter + 1);
            int sWidth = metrics.stringWidth(str);
            g2d.drawString(str, xPos - sWidth / 2, 160);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 90));

        /// draw Schüler name

        String sName;
        int sw;
        int yPos;

        if (showAndPlayName) {
            metrics = g2d.getFontMetrics();
            // TODO: check why cs.fgLight is not taken
            //g2d.setColor(cs.fgLight);
            g2d.setColor(Color.WHITE);
            if (!pinnedName.isEmpty()) {
                sName = pinnedName;
            } else {
                sName = allMathematicsTasks.get(taskCounter).name;
            }

            sw = metrics.stringWidth(sName);
            yPos = 200;

            if (isWindows) {
                yPos += 50;
            }
            g2d.drawString(sName, xPos - (sw / 2), getHeight() - yPos);
        }

        /// draw count down

        sName = "" + countDownCounter;
        g2d.setFont(new Font("Arial", Font.PLAIN, 50));
        metrics = g2d.getFontMetrics();
        sw = metrics.stringWidth(sName);
        if (countDownCounter > -1) {
            int rw = 60;
            g2d.setColor(Color.CYAN.darker());
            if (countDownCounter < 4) {
                g2d.setColor(Color.ORANGE);
            }
            if (countDownCounter < 2) {
                g2d.setColor(Color.RED);
            }
            yPos = 110;

            if (isWindows) {
                yPos += 50;
            }

            g2d.drawOval(getWidth() / 2 - rw / 2, getHeight() - yPos - 48, rw, rw);
            g2d.drawString(sName, (float) getWidth() / 2.0f - (float) sw / 2.0f, getHeight() - yPos);
        }
    }

    private void drawMathIsImportant(Graphics2D g2d, ColorSheme cs, int xPos) {

        FontMetrics metrics;
        g2d.setFont(new Font("Arial", Font.PLAIN, 90));
        metrics = g2d.getFontMetrics();

        String upper = "School";
        int sw = metrics.stringWidth(upper);
        g2d.setColor(cs.fgLight);
        g2d.drawString(upper, xPos - (sw / 2), 190);

        g2d.setColor(cs.fgLight);
        String lower = "is cool!";
        sw = metrics.stringWidth(lower);
        g2d.drawString(lower, xPos - (sw / 2), getHeight() - 160);

        /// arrow down for go
        g2d.setColor(Color.GRAY);
        String gForGo = "Press ↓ to start training! Press h for help.";
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        sw = metrics.stringWidth(gForGo);
        g2d.drawString(gForGo, xPos - (sw / 2), getHeight() - 100);

        if (debugMode) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(cs.fgDark);
        }
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));
        metrics = g2d.getFontMetrics();

        String str = " \u2219 ";
        Rectangle2D bounds = metrics.getStringBounds("1" + str + "1", g2d);
        g2d.drawString("1" + str + "1", (float) (xPos - (bounds.getWidth() / 2.0)), (float) ((getHeight() / 2.0) + (bounds.getHeight() / 4.0f)));

    }

    private void drawRunningTime(Graphics2D g2d, int xPos, ColorSheme cs) {

        FontMetrics metrics;
        long now = System.currentTimeMillis();

        deltaT = now - timerStart;

        String duration = Util.getTimeStringDuration(deltaT);

        duration = duration.substring(3);
        duration = duration.substring(0, duration.length() - 4);

        if (penalty > 0) {
            duration += " + " + penalty + " s Strafe";
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        metrics = g2d.getFontMetrics();
        int sWidth = metrics.stringWidth(duration);

        g2d.setColor(cs.fgLight);
        int yPos = 40;
        if (isWindows) {
            yPos += 30;
        }
        g2d.drawString(duration, xPos - (sWidth / 2), getHeight() - yPos);
    }

    private void drawDurationAtTheEnd(Graphics2D g2d, int xPos, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawKlasseAndNumberTasks(g2d);

        int yShift = -40;

        drawGesamtzeitTeam(g2d, cs, xPos, yShift);

        drawZeitProSchueler(g2d, cs, xPos, yShift);

        drawHighScore(g2d, cs, xPos, yShift);
    }

    private void drawGesamtzeitTeam(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        FontMetrics metrics;
        int sWidth;

        String ttt = "Gesamtzeit für das Team";
        g2d.setColor(cs.fgLight);

        if (penalty > 0) {
            g2d.setColor(Color.RED);
            ttt = "Gesamtzeit für das Team mit " + penalty + " s Strafe";
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(ttt);
        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2 - 220));

        String duration = Util.getTimeStringDuration(finalDeltaT + penalty * 1000L);

        duration = duration.substring(3);
        duration = duration.substring(0, duration.length() - 4);
        String min = duration.substring(0, duration.indexOf(":"));
        String sec = duration.substring(duration.indexOf(":") + 1);

        duration = min + " min " + sec + " s";

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        g2d.setColor(ColorSheme.niceGreen);
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(duration);

        g2d.drawString(duration, xPos - (sWidth / 2), yShift + (getHeight() / 2 - 120));
    }

    private void drawZeitProSchueler(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        FontMetrics metrics;

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        String ttt = "Zeit pro Aufgabe pro Schüler";
        int sWidth = metrics.stringWidth(ttt);
        g2d.setColor(cs.fgLight);
        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2));

        timePerTaskAndStudent = (int) ((double) finalDeltaT / (double) alleKlassen.get(actualKlasse).getNumberTasks());
        String ts = "" + (double) timePerTaskAndStudent / 1000.0 + " s";
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(ts);
        g2d.setColor(Color.ORANGE);
        g2d.drawString(ts, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 100));
    }

    private void drawHighScore(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        String ttt;
        FontMetrics metrics;
        int sWidth;
        String ts;

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        ttt = "High Score Team";
        sWidth = metrics.stringWidth(ttt);
        g2d.setColor(cs.fgLight);
//        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 200));

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        metrics = g2d.getFontMetrics();

        ts = "Toll gemacht!"; //getHighScoreString() + " s";
        sWidth = metrics.stringWidth(ts);
        g2d.setColor(Color.CYAN);
        g2d.drawString(ts, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 300));
    }

    private void display() {

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        System.out.println("mousy");

        if (SwingUtilities.isRightMouseButton(e)) {

            System.out.println("mousy:right");

            mathtrainer.MyPopup pop = new mathtrainer.MyPopup(this);
            pop.show(this, e.getX(), e.getY());
            return;
        }

        if (handleOperationToggling(e)) {
            return;
        }

        double yPos = e.getY();

        int id = (int) ((yPos - (fontSizeSchueler)) / (factorDrawSchueler * fontSizeSchueler));

        if (id >= alleKlassen.get(actualKlasse).size()) {
            return;
        }

        if (drawSchueler) {

            if (SwingUtilities.isRightMouseButton(e)) {

                if (pinnedName.length() == 0) {

                    pinnedName = alleKlassen.get(actualKlasse).get(id).name;

                    /// read pinned high score
                    String fileName = pinnedName + "HighScore.txt";
                    Scanner sc = null;
                    try {
                        sc = new Scanner(new File(fileName));
                    } catch (FileNotFoundException fileNotFoundException) {

                        /// do nothing

                    } finally {

                        while (true) {
                            assert sc != null;
                            if (!sc.hasNextLine()) {
                                break;
                            }
                            String str = sc.nextLine();
                            pinnedHighScore = Integer.parseInt(str);
                        }
                    }

                } else {
                    pinnedName = "";
                }

            } else {
                alleKlassen.get(actualKlasse).get(id).anwesend = !alleKlassen.get(actualKlasse).get(id).anwesend;
            }

        } else if (drawSettings) {
            if (id + 2 < series.size()) {
                series.set(id + 2, !series.get(id + 2));
            }
        }

        initAllTasks(false);

        repaint();
    }

    public static void readKlassenHighScores() {

        File folder = new File("teams/");
        File[] listOfFiles = folder.listFiles();

        allHighScores.clear();

        for (int i = 0; i < listOfFiles.length; i++) {

            File file = listOfFiles[i];

            if (!file.getName().contains("HighScore")) {
                continue;
            }

//            System.out.println("Klassen File: " + file.getName());

            String klassenleiter = file.getName().substring(6, file.getName().indexOf(".") - 9);

            int val = -1;
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String highScore = br.readLine();

                val = Integer.parseInt(highScore);

            } catch (IOException e1) {
                System.out.println("An I/O Error Occurred");
            }
            allHighScores.add(new HighScorePair(klassenleiter, val));
        }

        Collections.sort(allHighScores, new Comparator<HighScorePair>() {
            @Override
            public int compare(HighScorePair hsp1, HighScorePair hsp2) {
                if (hsp2.value > hsp1.value) {
                    return -1;
                }
                if (hsp2.value < hsp1.value) {
                    return +1;
                }
                return 0;
            }
        });
    }

    private boolean handleOperationToggling(MouseEvent e) {

        int xPos = e.getX();
        int yPos = e.getY();
        if (yPos < getHeight() - 100) {
            return false;
        }

        int xInc = 38;
        int xStart = 0;

        if (xPos < xInc) {
            System.out.println(" + ");
            Operations.toggleOperationOnOff(Operations.plus);
            initAllTasks(true);
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            System.out.println(" - ");
            Operations.toggleOperationOnOff(Operations.minus);
            initAllTasks(true);
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            System.out.println(" X ");
            Operations.toggleOperationOnOff(Operations.multiply);
            initAllTasks(true);
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            Operations.toggleOperationOnOff(Operations.divide);
            System.out.println(" : ");
            initAllTasks(true);
            repaint();
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

//        System.out.println("key: " + e.getKeyCode() + " shift: " + e.isShiftDown());

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> handleEscape();

            case KeyEvent.VK_RIGHT -> handleWrong();

            case KeyEvent.VK_DOWN, 34 -> {
                if (handleNextTask()) {
                    return;
                }
            }

            case 93 -> { // PLUS
                if (e.isShiftDown()) {
                    fontSizeNumbers += 5;
                } else {
                    numberTasksProSchueler++;
                    initAllTasks(true);
                }
            }

            case 47 -> { // MINUS
                if (e.isShiftDown()) {
                    fontSizeNumbers -= 5;
                } else {
                    numberTasksProSchueler--;
                    if (numberTasksProSchueler < 3) {
                        numberTasksProSchueler = 3;
                    }
                    initAllTasks(true);
                }
            }

            case KeyEvent.VK_SPACE -> {
                if (clip.isRunning()) {
                    clip.stop();
                } else {
                    clip.start();
                }
            }

            case KeyEvent.VK_1 -> colorSchemeId = 0;
            case KeyEvent.VK_2 -> colorSchemeId = 1;
            case KeyEvent.VK_3 -> colorSchemeId = 2;
            case KeyEvent.VK_4 -> colorSchemeId = 3;

            case KeyEvent.VK_5 -> setTaskType(TaskTypes.MATHEMATICS);
            case KeyEvent.VK_6 -> setTaskType(TaskTypes.ENGLISH);
            case KeyEvent.VK_7 -> setTaskType(TaskTypes.HISTORY);
            case KeyEvent.VK_8 -> setTaskType(TaskTypes.LATIN);


            case KeyEvent.VK_A -> drawAufgabe = !drawAufgabe;
            case KeyEvent.VK_B -> initBeginning();
            case KeyEvent.VK_D -> debugMode = !debugMode;
            case KeyEvent.VK_E -> showSettingsPage();
            case KeyEvent.VK_H -> {
                drawHighScore = false;
                drawSchueler = false;
                drawSettings = false;
                drawHelp = !drawHelp;
            }
            case KeyEvent.VK_L -> {
                limitedToSelectedSeries = !limitedToSelectedSeries;
                initAllTasks(true);
            }
            case KeyEvent.VK_M -> toggleMusicOnOff();
            case KeyEvent.VK_N -> showAndPlayName = !showAndPlayName;
            case KeyEvent.VK_P -> allMathematicsTasks.print();
            case KeyEvent.VK_S -> showSchuelerPage();
            case KeyEvent.VK_X -> handleExperimental();
            case KeyEvent.VK_T -> {
                if (e.isShiftDown()) {
                    transparency -= 0.1;
                } else {
                    transparency += 0.1;
                }
                System.out.println("transparency: " + transparency);
            }
            case KeyEvent.VK_W -> {
                if (clip.isOpen() || clip.isRunning()) {
                    clip.stop();
                }
                setAndPlaySound("/sound/Jeopardy.wav");
            }
            case KeyEvent.VK_V -> {
                if (e.isShiftDown()) {
                    soundVolume += 0.1f;
                } else {
                    soundVolume -= 0.1f;
                }
                soundVolume = Math.max(0.1f, Math.min(1.0f, soundVolume));
                System.out.println("soundVolume: " + soundVolume);
                setVolume();
            }
            case KeyEvent.VK_Z -> drawHighScore = !drawHighScore;

            // Handle special cases with modifiers
            default -> {
                // Handle shift+48 or enter
                if ((e.isShiftDown() && e.getKeyCode() == 48) || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Do nothing (empty case)
                }
                // Handle ctrl+shift+C
                else if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown() && e.isShiftDown()) {
                    // copyToGoogleDrive();
                }
                // Handle R with modifiers
                else if (e.getKeyCode() == KeyEvent.VK_R) {
                    if (e.isMetaDown()) {
                        // Make.jarAndApp(getClass());
                    } else if (e.isShiftDown()) {
                        readImages();
                    } else {
                        initNames(false);
                    }
                }
            }
        }
        display();
    }
    private void setTaskType(int newType) {
        taskType = newType;
        frame.setTitle(getSubject());
    }
    void showSettingsPage() {
        drawHelp = false;
        drawHighScore = false;
        drawSchueler = false;
        drawSettings = !drawSettings;
        repaint();
    }

    void showSchuelerPage() {
        drawSettings = false;
        drawHelp = false;
        drawSchueler = !drawSchueler;
        repaint();
    }

    void showHighScorePage() {
        drawSchueler = false;
        drawSettings = false;
        drawHelp = false;
        drawHighScore = !drawHighScore;
        repaint();
    }

    void toggleMusicOnOff() {

        playMusic = !playMusic;

        if (playMusic) {
            setAndPlaySound(soundOnDisplay);
            setVolume();
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (playMusic) {
            gainControl.setValue(20f * (float) Math.log10(soundVolume));
        } else {
            gainControl.setValue(-80);
        }
    }

    private void handleExperimental() {

        System.out.println("Experimental ...");

        Make.jarAndApp(this.getClass());
    }

    private void setVolume() {

        if (clip == null) {
            return;
        }

        System.out.println("soundVolume: " + soundVolume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(soundVolume));
        System.out.println("setVolume() - volume: " + gainControl.getValue());
    }

    private void handleEscape() {

        drawHighScore = false;
        drawSchueler = false;
        drawSettings = false;
        beginning = false;
//        initAllTasks(true);
    }

    private boolean handleNextTask() {

        drawHighScore = false;
        drawSettings = false;
        drawSchueler = false;
        drawHelp = false;

        if (beginning) {

            System.out.println("\n\nhandleDown() - beginning");
            beginning = false;
            shallWriteHighScore = false;

            if (taskCounter == 0 && !timeStartIsRested) {

                countDown = new MyCountDown(this, countDownFrom);
                timeStartIsRested = true;
                resetTimerStart();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
//                        System.out.println( "running:" );
                        repaint();
                    }
                }, 0, 200);
            }
            playStudentName(allMathematicsTasks.get(taskCounter).name);
            return true;
        }

        if (showDuration) {
            System.out.println("handleDown() - show duration true");
            return true;
        }

        if (nextTaskIn != null) {
            System.out.println("handleDown() - nextTask != null");
            nextTaskIn.cancel();
        }

        iterationCount++;

        System.out.println("\niterationCount: " + iterationCount + ".....................................");

        if (iterationCount % 2 > 0) {

            System.out.println("In Aufgabe ...");

            EnglishTask.nextTask();
            HistoryTask.nextTask();
            LatinTask.nextTask();

            taskCounter++;
            if (taskCounter >= alleKlassen.get(actualKlasse).getNumberTasks()) {
                handleFinished();
            }
            countDown = new MyCountDown(this, countDownFrom);
            setImageForTask();
            if (showAndPlayName) {
                playStudentName(allMathematicsTasks.get(taskCounter).name);
            }


        } else {

            System.out.println("In Ergebnis ...");

            if (taskCounter >= alleKlassen.get(actualKlasse).getNumberTasks()) {
                taskCounter = numberTasksProSchueler;
            }
            Team klasse = alleKlassen.get(actualKlasse);
            for (int i = 0; i < klasse.size(); i++) {
                if (klasse.getSchueler(i).name.contentEquals(allMathematicsTasks.get(taskCounter).name)) {
                    klasse.getSchueler(i).numberRightSolutions++;
                }
            }

            if (countDown != null) {
                System.out.println("automated new Aufgabe");
                countDown.cancel();
                nextTaskIn = new Timer();
                nextTaskIn.scheduleAtFixedRate(
                        new TimerTask() {
                            @Override
                            public void run() {
                                nextTaskCountDown--;
                                if (nextTaskCountDown == 0) {
                                    this.cancel();
                                    System.out.println("should start automatically");
                                    /// TODO: not thread safe
//                                    handleDown();
                                    nextTaskCountDown = nextTaskCountDownFrom;
                                }
                            }
                        }, 0, 1000
                );
            }
            countDownCounter = -1;
        }

        return false;
    }

    private void handleFinished() {

        showDuration = true;
        taskCounter = numberTasksProSchueler;
        finalDeltaT = deltaT;

        int numSchueler = alleKlassen.get(actualKlasse).size();
        int numTasks = numberTasksProSchueler * numSchueler;

        timer.cancel();
    }

    private void handleWrong() {

        if (drawSchueler && drawSettings) {
            return;
        }
        penalty += countDownFrom;
        handleNextTask();

    }


    private void setImageForTask() {

        try {
            MathTask task = allMathematicsTasks.get(taskCounter);
            if (task.getOperation() == Operations.multiply) {
                if (task.number1 > task.number2) {
                    bgImg = ImageIO.read(imagesMatrixURL[task.number2][task.number1]);
                } else {
                    bgImg = ImageIO.read(imagesMatrixURL[task.number1][task.number2]);
                }
            } else if (task.getOperation() == Operations.divide) {

                int ind1 = task.number1 / task.number2;
                int ind2 = task.number2;

                if (ind1 > ind2) {
                    bgImg = ImageIO.read(imagesMatrixURL[ind2][ind1]);
                } else {
                    bgImg = ImageIO.read(imagesMatrixURL[ind1][ind2]);
                }
            }
        } catch (IOException e) {
            System.out.println("some image could not be loaded: setImageForTask()");
        }
    }

    public void setActualKlasse(int i) {
        actualKlasse = i;
        initNames(false);
        initAllTasks(true);
        repaint();
    }

    private void readImages() {

        java.util.List<String> imageNames = getResourceListing("/images");

        // Filter only image files (optional)
        imageNames.removeIf(name -> !name.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$"));

        //System.out.println("Found " + imageNames.size() + " images: " + imageNames);

        int counter = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (counter >= imageNames.size()) {
                    //System.out.println("No more images at position [" + i + "][" + j + "]");
                    break;
                }

                String imageName = imageNames.get(counter);
                URL imageUrl = getClass().getResource("/images/" + imageName);

                if (imageUrl == null) {
                    System.err.println("Image not found: " + imageName);
                    counter++;
                    continue;
                }

                // Store URL (change imagesMatrixURL to URL[][])
                imagesMatrixURL[i][j] = imageUrl;

                //System.out.println(i + " - " + j + " loaded: " + imageName);

                counter++;
            }
        }

        // Load background image
        try {
            if (imagesMatrixURL[0][0] != null) {
                bgImg = ImageIO.read(imagesMatrixURL[0][0]);
                //System.out.println("Background image loaded: " + imagesMatrixURL[0][0].getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 👇 This is the magic method — lists all resources in a folder, works in IDE and JAR
    private java.util.List<String> getResourceListing(String path) {

        java.util.List<String> result = new ArrayList<>();

        // Remove leading "/" if present
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    // Running from IDE / file system
                    File folder = new File(url.toURI());
                    if (folder.isDirectory()) {
                        File[] files = folder.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile()) {
                                    result.add(file.getName());
                                }
                            }
                        }
                    }
                } else if ("jar".equals(protocol)) {
                    // Running from JAR
                    JarURLConnection conn = (JarURLConnection) url.openConnection();
                    JarFile jarFile = conn.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        // Match entries under the given path
                        if (name.startsWith(path + "/") && !entry.isDirectory()) {
                            // Extract just the filename
                            String fileName = name.substring(name.lastIndexOf('/') + 1);
                            result.add(fileName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getOperatingSystem() {
        String os = System.getProperty("os.name");
        return os;
    }

    public void fireDown() {
        handleNextTask();
    }

    public void setCountDown(int counter) {
        countDownCounter = counter;
        if (countDownCounter == 0) {
            countDownCounter = -1;
        }

        repaint();
    }

    private void resetTimerStart() {
        timer = new Timer();
        timerStart = System.currentTimeMillis();
    }

    @Override
    public void keyReleased(KeyEvent e) {

//        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//            resetTimerStart();
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    repaint();
//                }
//            }, 0, 1000);
//            if (countDown != null) {
//                countDown.cancel();
//            }
//            countDown = new MyCountDown(this, countDownFrom);
//        }
    }

    private static void playStudentName(String schueler) {

        String name = "sound/" + schueler + ".wav";
        setAndPlaySound(name);
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
        URL soundUrl = MathTrainer.class.getResource("/" + name);

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

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    /// main for testing

    public static void main(String[] args) throws InterruptedException {

        frame = new JFrame();
        frame.setLayout(new GridLayout());
        frame.setBounds(0, 0, 1280, 900);
        frame.add(new MathTrainer());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            File f = new File("images/Abacus.jpg");
            BufferedImage img = ImageIO.read(f);
            frame.setIconImage(img);
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
        frame.setVisible(true);
    }
}
