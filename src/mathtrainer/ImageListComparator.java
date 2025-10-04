package mathtrainer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageListComparator {
    private String list1Path;
    private String list2Path;

    public ImageListComparator(String list1Path, String list2Path) {
        this.list1Path = list1Path;
        this.list2Path = list2Path;
    }

    /**
     * Compare images by actual pixel content and find matches
     */
    public Map<String, String> findContentMatches() throws IOException {
        File[] list1Files = getImageFiles(list1Path);
        File[] list2Files = getImageFiles(list2Path);

        Map<String, String> matches = new HashMap<>();
        Set<String> matchedFiles = new HashSet<>();

        System.out.println("Searching for content-based matches...");

        for (File file1 : list1Files) {
            boolean foundMatch = false;

            for (File file2 : list2Files) {
                if (matchedFiles.contains(file2.getName())) {
                    continue; // Skip already matched files
                }

                if (imagesAreEqual(file1, file2)) {
                    matches.put(file1.getName(), file2.getName());
                    matchedFiles.add(file2.getName());
                    foundMatch = true;
                    System.out.println("MATCH: " + file1.getName() + " ↔ " + file2.getName());
                    break;
                }
            }

            if (!foundMatch) {
                matches.put(file1.getName(), "NO_MATCH");
                System.out.println("NO MATCH: " + file1.getName());
            }
        }

        // Find files in list2 that weren't matched
        for (File file2 : list2Files) {
            if (!matchedFiles.contains(file2.getName())) {
                System.out.println("UNMATCHED in second directory: " + file2.getName());
            }
        }

        return matches;
    }

    /**
     * Compare ALL images in list1 with ALL images in list2 by content
     */
    public void compareAllByContent() throws IOException {
        File[] list1Files = getImageFiles(list1Path);
        File[] list2Files = getImageFiles(list2Path);

        System.out.println("\n=== CONTENT-BASED COMPARISON ===");
        System.out.println("Directory 1: " + list1Path + " (" + list1Files.length + " images)");
        System.out.println("Directory 2: " + list2Path + " (" + list2Files.length + " images)");

        Map<String, List<String>> contentMatches = new HashMap<>();

        // Compare every file in list1 with every file in list2
        for (File file1 : list1Files) {
            List<String> matches = new ArrayList<>();

            for (File file2 : list2Files) {
                if (imagesAreEqual(file1, file2)) {
                    matches.add(file2.getName());
                }
            }

            contentMatches.put(file1.getName(), matches);
        }

        // Print results
        System.out.println("\n=== MATCHING RESULTS ===");
        for (Map.Entry<String, List<String>> entry : contentMatches.entrySet()) {
            String file1 = entry.getKey();
            List<String> matches = entry.getValue();

            if (matches.isEmpty()) {
                System.out.println("✗ " + file1 + " → NO MATCHES FOUND");
            } else {
                System.out.println("✓ " + file1 + " → " + matches);
            }
        }
    }

    private File[] getImageFiles(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Warning: Directory does not exist or is not a directory: " + directoryPath);
            return new File[0];
        }

        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".bmp") ||
                    lower.endsWith(".gif") || lower.endsWith(".tiff") ||
                    lower.endsWith(".webp");
        });

        if (files != null) {
            Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        } else {
            files = new File[0];
        }

        return files;
    }

    private boolean imagesAreEqual(File img1, File img2) throws IOException {
        // Quick file size check (fast rejection)
        if (img1.length() != img2.length()) {
            return false;
        }

        BufferedImage image1 = ImageIO.read(img1);
        BufferedImage image2 = ImageIO.read(img2);

        if (image1 == null || image2 == null) {
            return false;
        }

        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            return false;
        }

        // Pixel-by-pixel comparison
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String fixGermanEncoding(String filename) {
        // Use a regular HashMap to avoid duplicate key issues
        Map<String, String> encodingMap = new HashMap<>();

        // German umlauts and special characters
        encodingMap.put("√∂", "ö");
        encodingMap.put("ˆ", "ö");
        encodingMap.put("Ã¶", "ö");
        encodingMap.put("‰", "ä");  // This was "ö" before, but based on your data it should be "ä"

        encodingMap.put("√º", "ü");
        encodingMap.put("˜", "ü");
        encodingMap.put("Ã¼", "ü");

        encodingMap.put("√§", "ä");
        encodingMap.put("`", "ä");
        encodingMap.put("Ã¤", "ä");
        encodingMap.put("¸", "ü");  // This was "ä" before, but based on your data it should be "ü"

        encodingMap.put("√ü", "ß");
        encodingMap.put("·", "ß");
        encodingMap.put("ÃŸ", "ß");
        encodingMap.put("ﬂ", "ß");

        // Capital German umlauts
        encodingMap.put("√Ñ", "Ä");
        encodingMap.put("Ã„", "Ä");
        encodingMap.put("√ñ", "Ö");
        encodingMap.put("Ã–", "Ö");
        encodingMap.put("√ú", "Ü");
        encodingMap.put("Ãœ", "Ü");

        String result = filename;
        for (Map.Entry<String, String> entry : encodingMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Create a CSV file with the matches (no external libraries needed)
     */
    private static void createCSVFile(Map<String, String> matches, String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(outputPath), "UTF-8"))) {

            writer.write('\uFEFF'); // UTF-8 BOM
            writer.println("Name,Number"); // Comma delimiter

            for (Map.Entry<String, String> entry : matches.entrySet()) {
                if (!entry.getValue().equals("NO_MATCH")) {
                    String name = entry.getKey().replaceAll("\\.(jpg|jpeg|png|gif|bmp|webp)$", "");
                    String number = entry.getValue().replaceAll("\\.(jpg|jpeg|png|gif|bmp|webp)$", "").replace("avatar_", "");

                    // Fix German character encoding
                    name = fixGermanEncoding(name);
                    number = fixGermanEncoding(number);

                    writer.println("\"" + name.replace("\"", "\"\"") + "\"," + number);
                }
            }
        }
        System.out.println("\n✅ CSV file created: " + outputPath);
    }

    public static void main(String[] args) {
        try {
            String path2 = "/Users/malvers/Desktop/avatars/";
            String path1 = "/Users/malvers/Desktop/SuS Rahn/";
            ImageListComparator comparator = new ImageListComparator(path1, path2);

            // ... [your existing code] ...

            // Print final match table
            System.out.println("\n=== FINAL MATCH TABLE ===");
            System.out.println("Name                                      Number");
            System.out.println("----------------------------------------  ------");

            Map<String, String> matches = comparator.findContentMatches();
            for (Map.Entry<String, String> entry : matches.entrySet()) {
                if (!entry.getValue().equals("NO_MATCH")) {
                    String name = entry.getKey().replaceAll("\\.(jpg|jpeg|png|gif|bmp|webp)$", "");
                    String number = entry.getValue().replaceAll("\\.(jpg|jpeg|png|gif|bmp|webp)$", "").replace("avatar_", "");

                    // Fix German character encoding
                    name = fixGermanEncoding(name);
                    number = fixGermanEncoding(number);

                    System.out.printf("%-42s %s%n", name, number);
                }
            }

            // Create Excel/CSV file
            String desktopPath = System.getProperty("user.home") + "/Desktop/";
            createCSVFile(matches, desktopPath + "name_number_matches.csv");
            // OR if you have Apache POI:
            // createExcelFile(matches, desktopPath + "name_number_matches.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}