package mathtrainer;

/*
 TODO:
 MTools -> windows

 sk_27db7a41af907eadeaa4aa21a3689d66112efd0cfd0f28f0

 */

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
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MathTrainer extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private static JFrame frame;
    private static Clip clip;
    private final String sound2 = "Madonna - Frozen";
    private final String soundOnDisplay = sound2;
    public String lastFileProcessed = null;
    private Timer timer;
    private MyCountDown countDown;

    private final ArrayList<ColorSheme> allColorSchemes = new ArrayList<>();
    private final ArrayList<Team> allTeams = new ArrayList<>();
    private final Series series = new Series(10);

    private final AllMathematicsTasks allMathematicsTasks = new AllMathematicsTasks();
    private final AllDropTasks allDropTasks = new AllDropTasks();

    private final URL[][] imagesMatrixURL = new URL[10][10];

    private final ArrayList<URL> photosURL = new ArrayList<>();

    BufferedImage backgroundImage = null;

    private final int fontSizeStudent = 26;
    private int numberTasksPerStudent = 3;
    private int colorSchemeId = 0;
    private int fontSizeNumbers = 180;
    private int taskCounter = 0;
    private int iterationCount = 0;

    private final float factorDrawStudent = 1.4f;
    private float transparency = 0.5f;

    protected int actualTeam;

    private long timerStart;
    private long deltaT;
    private long finalDeltaT;

    private final String settingsFileName = "MatheTrainer.binary.settings";
    private String pinnedName = "";

    private boolean timeStartIsRested = false;
    protected boolean drawStudents = false;
    private boolean showDuration = false;
    private boolean beginning = true;
    public boolean drawHelp = false;
    public boolean drawSettings = false;
    private boolean limitedToSelectedSeries = false;
    private boolean debugMode = false;

    private int countDownCounter = -1;
    private int penalty = 0;
    private boolean drawTask = true;
    private final int nextTaskCountDownFrom = 5;
    private final int countDownFrom = 10;
    private int nextTaskCountDown = nextTaskCountDownFrom;
    private Timer nextTaskIn;
    private float soundVolume = 1.0f;
    boolean playMusic = true;
    private final boolean isWindows;
    private boolean drawAndPlayStudentName = true;
    protected int taskType = TaskTypes.MATHEMATICS;
    private BufferedImage laTeXLabel;
    private final Point laTeXPos = new Point();
    private final Color transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    private final String copyright = "SchoolTrainer by Dr. Michael R. Alvers - ©2020-2025 - all rights reserved";
    private BufferedImage solutionLabel;
    private boolean wolframMode = true;
    private boolean countDownMode = true;
    private static String droppedFileName = "nothing dropped";
    private boolean questionPlayed = false;
    private boolean playQuestion = false;

    public MathTrainer() {

        MTools.init("SchoolTrainerDebugLog.txt", false);

        new TextFileDropTarget(this);

        isWindows = getOperatingSystem().contains("Windows");

        setFocusable(true);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        /// make sure also whe window is closed via X or cmd q all settings are written
        Thread t = new Thread(this::writeSettings);

        Runtime.getRuntime().addShutdownHook(t);

        actualTeam = Teachers.ALVERS;

        readSettings();

        initColors();

        initBeginning();

        if (playMusic) {
            setAndPlaySound(soundOnDisplay);
            setVolume();
        }

        requestFocus();

        repaintEverySecondTimer();
    }

    private void repaintEverySecondTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //System.out.println("tick");
                repaint();
            }
        }, 0, 1000);
    }

    public void initBeginning() {

        System.out.println("\ninitBeginning:");

        timeStartIsRested = false;
        beginning = true;
        taskCounter = 0;
        iterationCount = 0;
        countDownCounter = -1;
        penalty = 0;

        showDuration = false;
        drawStudents = false;
        drawHelp = false;
        drawSettings = false;

        if (countDown != null) {
            countDown.cancel();
        }

        readImagesFromResources();
        readPhotosFromResources();

        System.out.println("readImages done ...");

        initNames(true);

//        new RandomNamePicker(allTeams.get(actualTeam).getStudents());
        RandomNamePicker.getInstance(allTeams.get(actualTeam).getStudents());

        System.out.println("initNames done ...");

        initAllTasks();

        System.out.println("initAllTasks done ...");

        setImageForTask();

        System.out.println("setImageForTask done ...");

        timer = new Timer();
        resetTimerStart();

        laTeXLabel = null;
        System.out.println("ready to go ...");
    }

    private void initNames(boolean shuffle) {

        allTeams.clear();

        new Team();
        for (int teamId = 0; teamId < Team.teamsString.length; teamId++) {
            allTeams.add(new Team(teamId));
        }
        if (shuffle) {
            Collections.shuffle(allTeams.get(actualTeam));
        }
    }

    protected void initAllTasks() {

        System.err.println("initAllTasks");
        allMathematicsTasks.clear();
        allDropTasks.clear();

        for (int i = 0; i < numberTasksPerStudent; i++) {

            Team team = allTeams.get(actualTeam);

            for (int j = 0; j < team.size(); j++) {

                OneStudent oneStudent = team.getStudent(j);

                //System.out.println("oneStudent: " + oneStudent.name);

                if (!oneStudent.anwesend) {
                    System.out.println("nicht anwesend ...");
                    continue;
                }

                //System.out.println("Get operation ...");

                int operation = Operations.getRandomOperation();

                allMathematicsTasks.add(new MathTask(oneStudent.name, series, limitedToSelectedSeries, operation));
                allDropTasks.add(new DropTask(oneStudent.name));
            }
        }

        for (Team oneStudent : allTeams) {
            Collections.shuffle(oneStudent);
        }

        /// do we need it for all subjects? /////////////////
        Team team = allTeams.get(actualTeam);

        for (OneStudent oneStudent : team) {

            for (MathTask allTask : allMathematicsTasks) {

                if (allTask.name.contentEquals(oneStudent.name)) {
                    oneStudent.setNumberTasks(oneStudent.getNumberTasks() + 1);
                    if (oneStudent.getNumberTasks() > numberTasksPerStudent) {
                        oneStudent.setNumberTasks(numberTasksPerStudent);
                    }
                }
            }
        }
        /////////////////////////////////////////////////////
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
            os.writeInt(numberTasksPerStudent);
            os.writeFloat(transparency);
            os.writeBoolean(debugMode);
            os.writeInt(fontSizeNumbers);
            os.writeFloat(soundVolume);
            os.writeBoolean(playMusic);

            Operations.write(os);

            os.writeInt(taskType);
            os.writeInt(actualTeam);
            os.writeBoolean(countDownMode);
            os.writeBoolean(drawStudents);

            os.writeObject(lastFileProcessed);

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
            frame.setTitle(getStringTaskType() + " - " + copyright);
            colorSchemeId = in.readInt();
            numberTasksPerStudent = in.readInt();
            if (numberTasksPerStudent < 2) {
                numberTasksPerStudent = 2;
            }
            transparency = in.readFloat();
            debugMode = in.readBoolean();
            fontSizeNumbers = in.readInt();
            soundVolume = in.readFloat();
            playMusic = in.readBoolean();

            Operations.read(in);

            taskType = in.readInt();

            setTaskType(taskType);

            actualTeam = in.readInt();
            countDownMode = in.readBoolean();
            drawStudents = in.readBoolean();

            lastFileProcessed = (String) in.readObject();

            System.err.println("last file processed: " + lastFileProcessed);

            in.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStringTaskType() {

        return switch (taskType) {
            case TaskTypes.MATHEMATICS -> "Mathematics";
            case TaskTypes.DROPPED -> getGreeting();
            default -> "Unknown Task";
        };
    }

    public static String getGreeting() {
        LocalTime now = LocalTime.now();
        if (now.getHour() < 12) {
            return "Good morning!";
        } else {
            return "Good afternoon!";
        }
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

        Graphics2D g2d = (Graphics2D) g;

        ColorSheme cs = allColorSchemes.get(colorSchemeId);

        if (backgroundImage != null) {
            float scWidth = (float) this.getWidth() / (float) backgroundImage.getWidth();
            float scHeight = (float) this.getHeight() / (float) backgroundImage.getHeight();
            if (scWidth < scHeight) {
                g2d.drawImage(backgroundImage, 0, 0, (int) (backgroundImage.getWidth() * scHeight), (int) (backgroundImage.getHeight() * scHeight), this);
            } else {
                g2d.drawImage(backgroundImage, 0, 0, (int) (backgroundImage.getWidth() * scWidth), (int) (backgroundImage.getHeight() * scWidth), this);
            }

            if (beginning) {
                g2d.setColor(ColorSheme.darkBlue);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                drawTransparentCover(g2d);
            }
        }

        g2d.setColor(cs.fgLight);
        g2d.setColor(cs.fgDark);

        drawTeamAndNumberTasks(g2d);

        if (drawHelp) {
            drawHelp(g2d, cs);
            return;
        }

        if (drawSettings) {
            drawSettings(g2d, cs);
            return;
        }

        if (drawStudents) {
            drawStudents(g2d, cs);
            drawTeamAndNumberTasks(g2d);
            return;
        }

        if (showDuration) {
            drawDurationAtTheEnd(g2d, getWidth() / 2, cs);
            return;
        }

        if (taskType == TaskTypes.MATHEMATICS) {
            drawMathOperations(g2d, cs);
        }

        if (drawTask) {
            drawTasks(g2d, cs);
        }

        if (!beginning) {
            drawRunningTime(g2d, getWidth() / 2, cs);
        }

        drawLaTexLabel(g2d);
    }

    private void drawLaTexLabel(Graphics2D g2d) {
        if (laTeXLabel == null) {
            return;
        }
        g2d.drawImage(laTeXLabel, laTeXPos.x, laTeXPos.y, transparent, this);
        if (solutionLabel != null) {

            int xPos = (getWidth() - solutionLabel.getWidth()) / 2;
            int yPos = (getHeight() - solutionLabel.getHeight()) / 2;

            g2d.drawImage(solutionLabel, xPos, yPos + 160, transparent, this);
        }
    }

    private void drawTransparentCover(Graphics2D g2d) {
        g2d.setColor(new Color(0.0f, 0.0f, 0.0f, transparency));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawTeamAndNumberTasks(Graphics2D g2d) {

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);

        /// draw number students
        String str = allTeams.get(actualTeam).size() + " Students";

        if (!pinnedName.isEmpty()) {
            g2d.setColor(Color.RED);
            str = pinnedName;
        }

        FontMetrics metrics = g2d.getFontMetrics();
        int width;
        //g2d.drawString(str, (float) (getWidth() / 2.0 - width / 2.0), 26);

        g2d.setColor(Color.WHITE);

        if (limitedToSelectedSeries && taskType == TaskTypes.MATHEMATICS) {

            str = "limit to selected series";
            width = (int) metrics.getStringBounds(str, g2d).getWidth();
            Color sc = g2d.getColor();
            g2d.setColor(Color.ORANGE);
            g2d.drawString(str, (float) (getWidth() / 2.0 - width / 2.0), 66);
            g2d.setColor(sc);
        }

        if (!droppedFileName.contains("nothing")) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            String toRender = droppedFileName.substring(0, droppedFileName.indexOf('.'));
            myDrawString(g2d, toRender, 80);
        }

        g2d.drawString(str, 10, 26);

        //g2d.setColor(Color.yellow);
        str = allTeams.get(actualTeam).getNumberTasks() + " Tasks";
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
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStudent));
        g2d.setColor(cs.fgDark);

        int yShift = 40;
        int xShift = 360;
        int i = 1;
        float yPos = factorDrawStudent * fontSizeStudent;
        g2d.drawString("H   ", 50, ((yShift + yPos * i)));
        g2d.drawString("Help", xShift, yShift + (yPos * i++));

        g2d.drawString("0 ", 50, yShift + (yPos * i));
        g2d.drawString("Loop color scheme", xShift, yShift + (yPos * i++));

        g2d.drawString("1 ", 50, yShift + (yPos * i));
        g2d.drawString("Dropped files are take", xShift, yShift + (yPos * i++));

        g2d.drawString("2 ", 50, yShift + (yPos * i));
        g2d.drawString("Mathematics", xShift, yShift + (yPos * i++));

        g2d.drawString("↓", 50, yShift + (yPos * i));
        g2d.drawString("Next task", xShift, yShift + (yPos * i++));

        g2d.drawString("Cmd Q", 50, yShift + (yPos * i));
        g2d.drawString("Quit the program", xShift, yShift + (yPos * i++));

        g2d.drawString("ESC", 50, yShift + (yPos * i));
        g2d.drawString("Back to main page", xShift, yShift + (yPos * i++));

        g2d.drawString("+ | -", 50, yShift + (yPos * i));
        g2d.drawString("Increase | decrease tasks per student", xShift, yShift + (yPos * i++));

        g2d.drawString("B", 50, yShift + (yPos * i));
        g2d.drawString("Zurück auf Beginn", xShift, yShift + (yPos * i++));

        g2d.drawString("C", 50, yShift + (yPos * i));
        g2d.drawString("Count down on/off", xShift, yShift + (yPos * i++));

        g2d.drawString("E", 50, yShift + (yPos * i));
        g2d.drawString("Show selected series (1 x 1 only)", xShift, yShift + (yPos * i++));

        g2d.drawString("L", 50, yShift + (yPos * i));
        g2d.drawString("Limit mode no/off", xShift, yShift + (yPos * i++));

        g2d.drawString("M", 50, yShift + (yPos * i));
        g2d.drawString("Hintergrundmusik on/off", xShift, yShift + (yPos * i++));

        g2d.drawString("N", 50, yShift + (yPos * i));
        g2d.drawString("Name no/off", xShift, yShift + (yPos * i++));

        g2d.drawString("S", 50, yShift + (yPos * i));
        g2d.drawString("Show statistics students", xShift, yShift + (yPos * i++));

        g2d.drawString("Q", 50, yShift + (yPos * i));
        g2d.drawString("Toggle play question", xShift, yShift + (yPos * i++));

        g2d.drawString("T | Shift T", 50, yShift + (yPos * i));
        g2d.drawString("Change transparency image (+|-)", xShift, yShift + (yPos * i++));

        g2d.drawString("V | Shift V", 50, yShift + (yPos * i));
        g2d.drawString("Change volume background music (+|-)", xShift, yShift + (yPos * i++));

        g2d.drawString("W", 50, yShift + (yPos * i));
        g2d.drawString("Toggle thinking/wolfram mode", xShift, yShift + (yPos * i++));

        g2d.drawString("Z", 50, yShift + (yPos * i));
        g2d.drawString("Check if all names have sounds - developer only)", xShift, yShift + (yPos * i++));

        g2d.drawString("D", 50, yShift + (yPos * i));
        g2d.drawString("Toggle debug mode (developer only)", xShift, yShift + (yPos));


        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics metrics = g2d.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(copyright, g2d);
        g2d.setColor(Color.GRAY);
        g2d.drawString(copyright, (float) ((double) getWidth() / 2 - bounds.getWidth() / 2), getHeight() - 30);

        drawTeamAndNumberTasks(g2d);
    }

    private void drawSettings(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStudent));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        int yShift = 80;
        for (int i = 2; i < series.size(); i++) {

            g2d.setColor(colorStore);

            var y = yShift + factorDrawStudent * fontSizeStudent * (i - 2);
            if (series.get(i)) {
                g2d.drawString("✓  ", 50, y);
            } else {
                g2d.setColor(cs.fgLight);
            }

            String str = "er series";
            g2d.drawString((i) + str, xIndent, y);
        }

        drawTeamAndNumberTasks(g2d);
    }

    private void drawStudents(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStudent));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        Team team = allTeams.get(actualTeam);

        int yShift = 80;
        for (int i = 0; i < team.size(); i++) {

            g2d.setColor(colorStore);
            if (team.get(i).anwesend) {
                g2d.drawString("✅  ", 50, yShift + factorDrawStudent * fontSizeStudent * i);
            } else {
                g2d.drawString("❌  ", 50, yShift + factorDrawStudent * fontSizeStudent * i);
                g2d.setColor(cs.fgLight);
            }

            String name = team.get(i).name;
            if (name.contentEquals(pinnedName)) {
                g2d.setColor(Color.RED);
            }
            g2d.drawString(name, xIndent, yShift + factorDrawStudent * fontSizeStudent * i);
            int num = team.get(i).getNumberTasks();
            String str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }

            if (!team.get(i).anwesend) {
                continue;
            }

            g2d.drawString(str, 500, yShift + factorDrawStudent * fontSizeStudent * i);
            num = team.get(i).numberRightSolutions;
            str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }
            g2d.drawString(str, 800, yShift + factorDrawStudent * fontSizeStudent * i);
        }
    }

    private void drawTasks(Graphics2D g2d, ColorSheme cs) {

        if (beginning) {
            drawSchoolIsCool(g2d, cs, getWidth() / 2);
            return;
        }

        String toRender;

        if (taskType == TaskTypes.DROPPED) {
            if (droppedFileName.contains("nothing dropped")) {
                return;
            }
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            toRender = droppedFileName.substring(0, droppedFileName.indexOf('.'));
            myDrawString(g2d, toRender, 80);
        }

        drawStudentNameAndCountDown(g2d, getWidth() / 2);

        /// question and answer

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));

        MathTask mathTask = allMathematicsTasks.get(taskCounter);
        DropTask dropTask = allDropTasks.get(taskCounter);

        if (debugMode) {
            g2d.setColor(Color.DARK_GRAY);
        } else {
            g2d.setColor(mathTask.getColor());
        }

        String onDisplay = "";

        if (iterationCount % 2 > 0 || iterationCount == 0) {

            ///  question

            if (iterationCount == 0) {
                iterationCount++;
            }

            if (taskType == TaskTypes.MATHEMATICS) {
                onDisplay = mathTask.getQuestion();
            } else if (taskType == TaskTypes.DROPPED) {
                onDisplay = dropTask.getQuestion();
            }

            if (!questionPlayed && playQuestion) {
                sleep(1111);
                playSoundDirect(onDisplay);
                questionPlayed = true;
            }

            if (onDisplay.startsWith("\\(")) {
                if (taskType == TaskTypes.DROPPED) {
                    onDisplay = dropTask.getQuestion();
                }
                prepareLaTeXLabel(onDisplay);
            } else {
                laTeXLabel = null;
                myDrawString(g2d, onDisplay, getHeight() / 2);
            }

        } else {

            /// answer

            questionPlayed = false;

            if (taskType == TaskTypes.MATHEMATICS) {
                onDisplay = mathTask.getQuestion() + " = " + mathTask.getResult();
            } else if (taskType == TaskTypes.DROPPED) {
                onDisplay = getProperLatex(dropTask);
            }

            if (onDisplay.startsWith("\\(")) {
                prepareLaTeXLabel(onDisplay);

            } else {
                laTeXLabel = null;
                myDrawString(g2d, onDisplay, getHeight() / 2);
            }
        }
    }

    public void playSoundDirect(String text) {
        try {
            byte[] audioBytes = GoogleTTS.textToSpeechBytes(text, "en-US", "LINEAR16");
            if (audioBytes == null) {
                return;
            }

            // Skip WAV header (usually 44 bytes for PCM WAV)
            int headerSize = 44;
            if (audioBytes.length > headerSize) {
                byte[] pureAudio = new byte[audioBytes.length - headerSize];
                System.arraycopy(audioBytes, headerSize, pureAudio, 0, pureAudio.length);
                audioBytes = pureAudio;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
            AudioInputStream audioInputStream = new AudioInputStream(bais, format,
                    audioBytes.length / format.getFrameSize());

            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Small delay before start can help
            Thread.sleep(10);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getProperLatex(DropTask dropTask) {

        String q = dropTask.getQuestion();

        String space;
        q = q.replace("= \\)", "=\\)");
        ///  no LaTeX math
        if (!q.startsWith("\\(")) {
            //System.err.println("no LaTeX");
            return q + " " + dropTask.getAnswer();
        }

        //System.out.println("q1: " + q);
        if (q.endsWith("=\\)")) {
            //System.out.println("q2: " + q);
            space = "\\;";
        } else {
            space = "\\quad";
        }

        return q + space + dropTask.getAnswer();
    }

    private void prepareLaTeXLabel(String toLatex) {

        laTeXLabel = Latexer.renderLatexToImage(toLatex, 160, getWidth() - 100, Color.ORANGE);

        if (laTeXLabel == null) {
            return;
        }

        int w = laTeXLabel.getWidth();
        int h = laTeXLabel.getHeight();

        laTeXPos.x = (getWidth() - w) / 2;
        laTeXPos.y = (getHeight() - h) / 2;
    }

    private void myDrawString(Graphics2D g2d, String result, int yPos) {

        int yShift = -60;
//        int yPos = getHeight() / 2;
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

    private void drawMathOperations(Graphics2D g2d, ColorSheme cs) {

        g2d.setFont(new Font("Times", Font.PLAIN, 30));
        FontMetrics metrics = g2d.getFontMetrics();

        g2d.setColor(cs.fgLight);

        String sOperations;
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

    private void drawStudentNameAndCountDown(Graphics2D g2d, int xPos) {

        FontMetrics metrics;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 90));

        if (taskCounter >= 0) {
            metrics = g2d.getFontMetrics();
            String str = "Task " + (taskCounter + 1);
            int sWidth = metrics.stringWidth(str);
            g2d.drawString(str, xPos - sWidth / 2, 160);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 90));

        /// draw student name

        String str;
        int sw;
        int yPos;

        if (drawAndPlayStudentName) {

            metrics = g2d.getFontMetrics();
            g2d.setColor(Color.LIGHT_GRAY);
            if (!pinnedName.isEmpty()) {
                str = pinnedName;
            } else {
                str = allMathematicsTasks.get(taskCounter).name;
            }

            sw = metrics.stringWidth(str);
            yPos = 220;

            if (isWindows) {
                yPos += 50;
            }

            ///  TODO: make it perfect ///////
            //drawStudenPhoto(g2d, str, yPos);

            g2d.drawString(str, xPos - (sw / 2), getHeight() - yPos);

            /// draw count down

            str = "" + countDownCounter;
            g2d.setFont(new Font("Arial", Font.PLAIN, 50));
            metrics = g2d.getFontMetrics();
            sw = metrics.stringWidth(str);
            if (countDownCounter > -1) {
                int rw = 68;
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

                g2d.drawRect(getWidth() / 2 - rw / 2 + 1, getHeight() - yPos - rw / 2 - 19, rw, rw);
                g2d.drawString(str, (float) getWidth() / 2.0f - (float) sw / 2.0f, getHeight() - yPos);
            }
        }
    }

    private void drawStudenPhoto(Graphics2D g2d, String str, int yPos) {

        var localURL = getUrl(str);

        if (localURL != null) {
            try {
                BufferedImage img = ImageIO.read(localURL);
                int s = 120;
                int x = (getWidth() - s) / 2;
                int y = getHeight() - yPos + 26;
                g2d.drawImage(img, x, y, s, s, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private URL getUrl(String str) {
        URL localURL = null;
        boolean found = false;
        for (URL url : photosURL) {
            localURL = url;
            String lStr = str.replace(" ", "");
            lStr = lStr.replace("Á", "A");


            if (localURL.getPath().contains(lStr + ".png")) {
                found = true;
                break;
            }
        }
        if (!found) {
            localURL = null;//smileyURL;
        }
        return localURL;
    }

    private void drawSchoolIsCool(Graphics2D g2d, ColorSheme cs, int xPos) {

        FontMetrics metrics;
        g2d.setFont(new Font("Raleway", Font.PLAIN, 90));
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
        g2d.setFont(new Font("Raleway", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        sw = metrics.stringWidth(gForGo);
        g2d.drawString(gForGo, xPos - (sw / 2), getHeight() - 100);

        if (debugMode) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(cs.fgDark);
        }

        g2d.setFont(new Font("Raleway", Font.PLAIN, fontSizeNumbers));

        myDrawString(g2d, getStringTaskType(), getHeight() / 2);

    }

    private void drawRunningTime(Graphics2D g2d, int xPos, ColorSheme cs) {

        FontMetrics metrics;
        long now = System.currentTimeMillis();

        deltaT = now - timerStart;

        String duration = Util.getTimeStringDuration(deltaT);

        duration = duration.substring(3);
        duration = duration.substring(0, duration.length() - 4);

        if (penalty > 0 && countDownMode) {
            duration += " + " + penalty + " s penalty";
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        metrics = g2d.getFontMetrics();
        int sWidth = metrics.stringWidth(duration);

        g2d.setColor(cs.fgLight);
        int yPos = 40;
        if (isWindows) {
            yPos += 30;
        }
        ///  draw time center bottom
        g2d.drawString(duration, xPos - (sWidth / 2), getHeight() - yPos);
    }

    private void drawDurationAtTheEnd(Graphics2D g2d, int xPos, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawTeamAndNumberTasks(g2d);

        int yShift = -40;

        drawGesamtzeitTeam(g2d, cs, xPos, yShift);

        drawTimePerTaskAndStudent(g2d, cs, xPos, yShift);
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

    private void drawTimePerTaskAndStudent(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        FontMetrics metrics;

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        String ttt = "Time per task per student";
        int sWidth = metrics.stringWidth(ttt);
        g2d.setColor(cs.fgLight);
        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2));

        int timePerTaskAndStudent = (int) ((double) finalDeltaT / (double) allTeams.get(actualTeam).getNumberTasks());
        String ts = (double) timePerTaskAndStudent / 1000.0 + " s";
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(ts);
        g2d.setColor(Color.ORANGE);
        g2d.drawString(ts, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 100));
    }

    private void display() {

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {

            mathtrainer.MyPopup pop = new mathtrainer.MyPopup(this);
            pop.show(this, e.getX(), e.getY());
            return;
        }

        if (handleOperationToggling(e)) {
            return;
        }

        double yPos = e.getY();

        int id = (int) ((yPos - (fontSizeStudent)) / (factorDrawStudent * fontSizeStudent));

        if (id >= allTeams.get(actualTeam).size()) {
            return;
        }

        if (drawStudents) {

            if (SwingUtilities.isRightMouseButton(e)) {

                if (pinnedName.isEmpty()) {

                    pinnedName = allTeams.get(actualTeam).get(id).name;

                } else {
                    pinnedName = "";
                }

            } else {
                allTeams.get(actualTeam).get(id).anwesend = !allTeams.get(actualTeam).get(id).anwesend;
            }

        } else if (drawSettings) {
            if (id + 2 < series.size()) {
                series.set(id + 2, !series.get(id + 2));
            }
        }

        repaint();
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
            initAllTasks();
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            System.out.println(" - ");
            Operations.toggleOperationOnOff(Operations.minus);
            initAllTasks();
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            System.out.println(" X ");
            Operations.toggleOperationOnOff(Operations.multiply);
            initAllTasks();
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            Operations.toggleOperationOnOff(Operations.divide);
            System.out.println(" : ");
            initAllTasks();
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

        requestFocus();
        repaint();
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

            case KeyEvent.VK_F1 -> System.out.println("F1 F1 F1 F1");

            ///  34 = arrow down
            case KeyEvent.VK_DOWN, 34, KeyEvent.VK_SPACE -> {
                if (handleNextTask()) {
                    return;
                }
            }
            // 93 = PLUS
            case 93 -> {
                if (e.isShiftDown()) {
                    fontSizeNumbers += 5;
                } else {
                    numberTasksPerStudent++;
                    initAllTasks();
                }
            }
            // 47 = bMINUS
            case 47 -> {
                if (e.isShiftDown()) {
                    fontSizeNumbers -= 5;
                } else {
                    numberTasksPerStudent--;
                    if (numberTasksPerStudent < 3) {
                        numberTasksPerStudent = 3;
                    }
                    initAllTasks();
                }
            }

            case KeyEvent.VK_0 -> loopColorScheme();

            case KeyEvent.VK_1 -> setTaskType(TaskTypes.MATHEMATICS);
            case KeyEvent.VK_2 -> setTaskType(TaskTypes.DROPPED);

            case KeyEvent.VK_A -> drawTask = !drawTask;
            case KeyEvent.VK_B -> initBeginning();
            case KeyEvent.VK_C -> countDownMode = !countDownMode;
            case KeyEvent.VK_D -> {
                if (e.isMetaDown()) {
                    debugMode = !debugMode;
                }
            }
            case KeyEvent.VK_E -> showSettingsPage();
            case KeyEvent.VK_H -> {
                drawStudents = false;
                drawSettings = false;
                drawHelp = !drawHelp;
            }
            case KeyEvent.VK_L -> {
                limitedToSelectedSeries = !limitedToSelectedSeries;
                initAllTasks();
            }
            case KeyEvent.VK_M -> toggleMusicOnOff();
            case KeyEvent.VK_N -> drawAndPlayStudentName = !drawAndPlayStudentName;
            case KeyEvent.VK_O -> loadFile();
            case KeyEvent.VK_Q -> playQuestion = !playQuestion;
            case KeyEvent.VK_S -> showStudentsPage();
            case KeyEvent.VK_X -> handleExperimental();
            case KeyEvent.VK_T -> {
                if (e.isShiftDown()) {
                    transparency -= 0.1F;
                } else {
                    transparency += 0.1F;
                }
                System.out.println("transparency: " + transparency);
            }
            case KeyEvent.VK_W -> wolframMode = !wolframMode;
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
            case KeyEvent.VK_Z -> soundCheck();

            default -> {
            }
        }
        display();
    }

    private static void loadFile() {

        FileDialog dialog = new FileDialog(frame, "Select a file", FileDialog.LOAD); // LOAD or SAVE
        dialog.setVisible(true);
        String directory = dialog.getDirectory();
        String file = dialog.getFile();
        try {
            droppedFileName = file;
            DropTask.readTasksFromFile(Path.of(directory + file));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loopColorScheme() {
        colorSchemeId++;
        if (colorSchemeId >= allColorSchemes.size()) {
            colorSchemeId = 0;
        }
    }

    private void handleExperimental() {

        System.out.println("Experimental ...");

        Make.jarAndApp(this.getClass());
    }

    void setTaskType(int newType) {
        taskType = newType;
        frame.setTitle(copyright);
    }

    void showSettingsPage() {
        drawHelp = false;
        drawStudents = false;
        drawSettings = !drawSettings;
        repaint();
    }

    void showStudentsPage() {
        drawSettings = false;
        drawHelp = false;
        drawStudents = !drawStudents;
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

        drawStudents = false;
        drawSettings = false;
        beginning = false;
//        initAllTasks(true);
    }

    private boolean handleNextTask() {

        drawSettings = false;
        drawStudents = false;
        drawHelp = false;

        if (droppedFileName.contains("nothing dropped")) {
            return false;
        }

        if (beginning) {

            System.out.println("\nhandleDown() - beginning");
            beginning = false;

            timer = new Timer();
            resetTimerStart();

            if (taskCounter == 0 && !timeStartIsRested) {

                if (countDownMode) {
                    countDown = new MyCountDown(this, countDownFrom);
                    timeStartIsRested = true;
                    resetTimerStart();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            repaint();
                        }
                    }, 0, 200);
                }
            }
            setAndPlaySound(allMathematicsTasks.get(taskCounter).name);
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

        System.out.println("iterationCount: " + iterationCount + ".....................................");

        if (iterationCount % 2 > 0) {

            //System.out.println("In question ...");

            solutionLabel = null;

            if (taskType == TaskTypes.DROPPED) {
                DropTask.nextTask();
            }

            taskCounter++;
            if (taskCounter >= allTeams.get(actualTeam).getNumberTasks()) {
                handleFinished();
            }
            if (countDownMode) {
                countDown = new MyCountDown(this, countDownFrom);
            }
            setImageForTask();
            if (drawAndPlayStudentName) {
                setAndPlaySound(allMathematicsTasks.get(taskCounter).name);
            }

        } else {

            //System.out.println("In answer ...");

            if (taskCounter >= allTeams.get(actualTeam).getNumberTasks()) {
                taskCounter = numberTasksPerStudent;
            }
            Team team = allTeams.get(actualTeam);
            for (int i = 0; i < team.size(); i++) {
                if (team.getStudent(i).name.contentEquals(allMathematicsTasks.get(taskCounter).name)) {
                    team.getStudent(i).numberRightSolutions++;
                }
            }

            if (countDown != null) {
//                System.out.println("automated new task");
                countDown.cancel();
                nextTaskIn = new Timer();
                nextTaskIn.scheduleAtFixedRate(
                        new TimerTask() {
                            @Override
                            public void run() {
                                nextTaskCountDown--;
                                if (nextTaskCountDown == 0) {
                                    this.cancel();
//                                    handleDown();
                                    nextTaskCountDown = nextTaskCountDownFrom;
                                }
                            }
                        }, 0, 1000
                );
            }
            countDownCounter = -1;

            if (taskType == TaskTypes.DROPPED && wolframMode) {
                solutionLabel = Latexer.renderLatexToImage("Thinking \\,...", 20, 200, Color.LIGHT_GRAY);
                new Thread(this::wolframCalling).start();
            }

        }

        return false;
    }

    private void wolframCalling() {

        String question = allDropTasks.get(taskCounter).getQuestion();

        if (checkIfLatex(question)) {
            return;
        }

        List<String> solutions = WolframAlphaSolver.getSolutions(question);

        System.out.println("Found " + solutions.size() + " solutions for:" + question);

        for (int i = 0; i < solutions.size(); i++) {
            System.out.println("Solution    " + i + ": " + solutions.get(i));
            String properLatex = solutions.get(i).replaceAll("(\\d+)/(\\d+)", "\\\\frac{$1}{$2}");
            solutionLabel = Latexer.renderLatexToImage(properLatex, 30, 200, Color.LIGHT_GRAY);
        }
        repaint();
    }

    private boolean checkIfLatex(String question) {
        return !question.startsWith("\\(");
    }

    private void handleFinished() {

        showDuration = true;
        taskCounter = numberTasksPerStudent;
        finalDeltaT = deltaT;

        timer.cancel();
    }

    private void handleWrong() {

        if (drawStudents && drawSettings) {
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
                    backgroundImage = ImageIO.read(imagesMatrixURL[task.number2][task.number1]);
                } else {
                    backgroundImage = ImageIO.read(imagesMatrixURL[task.number1][task.number2]);
                }
            } else if (task.getOperation() == Operations.divide) {

                int ind1 = task.number1 / task.number2;
                int ind2 = task.number2;

                if (ind1 > ind2) {
                    backgroundImage = ImageIO.read(imagesMatrixURL[ind2][ind1]);
                } else {
                    backgroundImage = ImageIO.read(imagesMatrixURL[ind1][ind2]);
                }
            }
        } catch (Exception e) {
            System.out.println("some image could not be loaded: setImageForTask()");
        }
    }

    public void setActualTeam(int i) {
        actualTeam = i;
        initNames(false);
        RandomNamePicker.getInstance(allTeams.get(actualTeam).getStudents());
        initAllTasks();
        repaint();
    }

    private void readImagesFromResources() {

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
                URL imageUrl = getClass().getResource("/images" + "/" + imageName);

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
                backgroundImage = ImageIO.read(imagesMatrixURL[0][0]);
                //System.out.println("Background image loaded: " + imagesMatrixURL[0][0].getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readPhotosFromResources() {

        java.util.List<String> photNames = getResourceListing("/photos/TeamAlvers");

        // Filter only image files (optional)
        photNames.removeIf(name -> !name.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$"));

        System.out.println("Number of photos: " + photNames.size());

        int counter = 0;
        for (int i = 0; i < photNames.size(); i++) {

            String photoName = photNames.get(counter);

            //System.out.println("photo: " + photoName);

            URL imageUrl = getClass().getResource("/photos/TeamAlvers" + "/" + photoName);
            photosURL.add(imageUrl);

            if (imageUrl == null) {
                System.err.println("Image not found: " + photoName);
                counter++;
                continue;
            }

            counter++;
        }
    }

    // 👇 This is the magic method — lists all resources in a folder, works in IDE and JAR
    private java.util.List<String> getResourceListing(String path) {

        java.util.List<String> result = new ArrayList<>();

        path = path.substring(1);

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

    public static String getOperatingSystem() {
        return System.getProperty("os.name");
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

    }

    private static void setAndPlaySound(String name) {

        try {
            setSound(name);
            clip.start();
            if (name.contains("Frozen")) {
                System.out.println("Looooooping ...");
                clip.loop(10);
            }
        } catch (Exception ex) {
            MTools.println("Error with playing sound: " + name);
        }
    }

    private static void setSound(String student) {

        // Try loading via URL first (works great in JARs!)
        String resource = "/sound/" + student + ".wav";
        URL soundUrl = MathTrainer.class.getResource(resource);

        if (soundUrl == null) {
            System.err.println("setSound - resource not found: " + resource);
            String out = "resources" + resource;
            GoogleTTS.ttsWAV(student, "de-DE", out);
            return;
        }

        try {
            // 🚀 Use URL directly — this works perfectly in JARs!
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            //clip.loop(10);

        } catch (UnsupportedAudioFileException e) {
            MTools.println("setSound: Unsupported audio format for: " + student);
            e.printStackTrace();
        } catch (IOException e) {
            MTools.println("setSound: IO error loading: " + student);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            MTools.println("setSound: Audio line unavailable for: " + student);
            e.printStackTrace();
        }
    }

    private void soundCheck() {
        int count = 0;
        for (Team team : allTeams) {
            System.out.println("Team: " + team.fileName);

            for (OneStudent student : team) {
                System.out.println((++count) + " name " + student.name);

                // Check for Google TTS file
                String resource = "/sound/" + student.name + ".wav";
                URL soundUrl = MathTrainer.class.getResource(resource);

                if (soundUrl == null) {
                    System.out.println("❌ GoogleTTS file not found: " + resource);
                    String out = "resources" + resource; // This becomes "resources/sound/name.wav"
                    boolean b = GoogleTTS.ttsWAV(student.name, "de-DE", out);
                    if (b) {
                        System.out.println("✅ GoogleTTS sound created successfully 🎵");
                    }
                }

//                // Check for ElevenLabs file
//                String resourceMRA = "/sound/" + student.name + "_ELEVEN.wav";
//                URL soundUrlMRA = MathTrainer.class.getResource(resourceMRA);
//
//                if (soundUrlMRA == null) {
//                    System.out.println("❌ ElevenLabs file not found: " + resourceMRA);
//                    String out = "resources" + resourceMRA; // This becomes "resources/sound/name_MRA.wav"
//                    System.out.println("Output path: " + out);
//
//                    // Use ElevenLabs - just the name as text
//                    boolean b = ElevenLabsTTS.textToSpeech(student.name, out);
//
//                    if (b) {
//                        System.out.println("✅ ElevenLabs sound created successfully 🎵");
//                    } else {
//                        System.err.println("❌ ElevenLabs generation failed for: " + student.name);
//                    }
//                }
            }
        }
    }

    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public int getTaskType() {
        return taskType;
    }

    public void toggleCountDownOn() {
        countDownMode = !countDownMode;
        System.err.println("countDownOn: " + countDownMode);
    }

    public boolean getCountDownMode() {
        return countDownMode;
    }

    public boolean getDrawStudenNames() {
        return countDownMode;
    }

    public void toggleDrawStudentName() {
        drawAndPlayStudentName = !drawAndPlayStudentName;
    }


    /// main for testing ///////////////////////////////////////////////

    public static void main(String[] args) {

        frame = new JFrame();
        frame.setFocusable(true);
        frame.setFocusableWindowState(true);
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

    public void setDroppedFileName(String name) {
        droppedFileName = name;
    }
}
