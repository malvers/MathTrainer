package mathtrainer;

import mratools.MTools;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TextFileDropTarget {

    public TextFileDropTarget(JPanel panel) {

        // Enable drop target
        panel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);

                    // Check if it's a file list
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> droppedFiles = (List<File>) dtde.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);

                        for (File file : droppedFiles) {
                            if (file.getName().toLowerCase().endsWith(".txt")) {
                                processTextFile(file);
                            } else {
                                MTools.println("❌ Ignored non-txt file: " + file.getName());
                            }
                        }
                    }

                    dtde.dropComplete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.dropComplete(false);
                }
            }
        });
    }

    private void processTextFile(File file) {
//        System.out.println("\n📄 Reading: " + file.getName());
//        System.out.println("================================");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split by whitespace (space, tab, etc.)
                String[] columns = line.trim().split(":", 2); // Limit 2 parts max (in case there are extra colons)

                if (columns.length < 2) {
                    MTools.println("⚠️  Line " + lineNum + ": Not enough columns: \"" + line + "\"");
                    continue;
                }

                // Take first two columns (ignore extra ones)
                String col1 = columns[0];
                String col2 = columns[1];

                MTools.println(col1 + " --- " + col2);

                HistoryTask.tasks.add(new HistoryTask.Vocabulary(col1, col2));
            }

        } catch (IOException e) {
            System.err.println("❌ Error reading file: " + file.getName());
            e.printStackTrace();
        }
    }
}
