package mathtrainer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Klasse extends ArrayList<OneSchueler> {

    public String fileName = "Nonameklasse";
    int classId;
    static String[] klassenString;

    public Klasse() {
        initKlassenId();
    }

    private void readKlasse() {

        fileName = "klassen/Klasse" + klassenString[classId] + ".txt";

        InputStream inputStream = getClass().getResourceAsStream(fileName);

        if (inputStream == null) {
            System.err.println("❌ File not found: " + fileName);
            return;
        }

        // ✅ Read content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            System.out.println("📄 Contents of " + fileName + ":");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Klasse(int idIn) {

        super(16);

        initKlassenId();

        classId = idIn;

        readKlasse();

        File file = null;

        if (klassenString[classId] == null) {
            return;
        }

        fileName = "klassen/Klasse" + klassenString[classId] + ".txt";
        file = new File(fileName);

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
    }

    private void initKlassenId() {
        klassenString = new String[10];
        klassenString[0] = "Alvers";
        klassenString[1] = "Wischnewski";
        klassenString[2] = "Cool";
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
}
