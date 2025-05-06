package mathtrainer;

import mratools.MTools;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem settingsMenu = new JMenuItem("Settings");
    JMenuItem schuelerMenu = new JMenuItem("Schüler");
    JMenuItem highScoreMenu = new JMenuItem("High-score");
    JMenuItem helpMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");
    JMenuItem wischnewski = new JMenuItem("Klasse Fr. Wischnewski");
    JMenuItem alvers = new JMenuItem("Klasse Dr. Alvers");


    public MyPopup(MathTrainer mathTrainer) {

        helpMenu.addActionListener(e -> {
            mathTrainer.drawSchueler = false;
            mathTrainer.drawSettings = false;
            mathTrainer.drawHelp = !mathTrainer.drawHelp;
            mathTrainer.repaint();
        });

        newGameMenu.addActionListener(e -> mathTrainer.initBeginning());

        /// Teacher

        alvers.addActionListener(e -> mathTrainer.setActualKlasse(0));

        wischnewski.addActionListener(e -> mathTrainer.setActualKlasse(1));

        /// end Teacher

        settingsMenu.addActionListener(e -> mathTrainer.showSettingsPage());

        schuelerMenu.addActionListener(e -> mathTrainer.showSchuelerPage());

        highScoreMenu.addActionListener(e -> mathTrainer.showHighScorePage());

        add(newGameMenu);

        add(new Separator());

        add(alvers);
        add(wischnewski);

        add(new Separator());

        JCheckBox cb = new JCheckBox("Sound on/off");
        cb.setSelected(mathTrainer.playMusic);
        MTools.println("playMusic: " + mathTrainer.playMusic);
        add(cb);
        cb.addActionListener(e -> mathTrainer.toggleMusicOnOff());

        add(new Separator());

        add(highScoreMenu);
        add(schuelerMenu);
        add(settingsMenu);
        add(helpMenu);
    }
}
