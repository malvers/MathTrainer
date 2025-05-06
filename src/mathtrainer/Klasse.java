package mathtrainer;

import mratools.MTools;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Klasse extends ArrayList<OneSchueler> {

    public String name = "Nonameklasse";
    int id;
    long highScore;
    static String[] klassenString;

    public Klasse() {
        initKlassenId();
    }


    public Klasse(int idIn) {

        super(16);

        initKlassenId();

        highScore = Long.MAX_VALUE;

        id = idIn;

        File file = null;
        File fileHighScore = null;

        if (klassenString[id] == null) {
            return;
        }

        name = "/Users/malvers/IdeaProjects/MathTrainer/klassen/Klasse" + klassenString[id] + ".txt";
        file = new File(name);

        String hStr = "klassen/Klasse" + klassenString[id] + "HighScore.txt";
        fileHighScore = new File(hStr);

        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.startsWith("//")) {
                continue;
            }
            add(new OneSchueler(line));
        }

        /// read high scores
        readHighscores(fileHighScore);
    }

     private void readHighscores(File fileHighScore) {
        Scanner sc;
        try {
            sc = new Scanner(fileHighScore);
        } catch (FileNotFoundException e) {
            MTools.println("HighScore file not found.");
            return;
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.length() == 0) {
                continue;
            }
            highScore = Long.parseLong(line);
        }
    }

    private void initKlassenId() {
        klassenString = new String[10];
        klassenString[0] = "Alvers";
        klassenString[1] = "Wischnewski";
    }

    public int getNumberTasks() {

        int sum = 0;
        for (int i = 0; i < size(); i++) {
            OneSchueler osch = getSchueler(i);
            if (osch.anwesend) {
                sum += osch.getNumberTasks();
            }
        }
        return sum;
    }

    @Override
    public OneSchueler get(int index) {
//        System.out.println( "get -> call getSchueler!" );
        return getSchueler(index);
    }

    public OneSchueler getSchueler(int index) {
        return super.get(index);
    }

    void print() {
        for (int i = 0; i < size(); i++) {
            get(i).print();
        }
    }

    public void writeHighScore() throws IOException {

        File file = null;
        file = new File("Klasse" + klassenString[id] + "_HighScore.txt");

        Writer writer = new OutputStreamWriter(new FileOutputStream(file, true));
        try {
            writer.append("" + highScore + "\n");
        } finally {
            writer.close();
        }
    }
}
