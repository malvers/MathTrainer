package mathtrainer;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem settingsMenu = new JMenuItem("Settings");
    JMenuItem matheMenu = new JMenuItem("Mathematics");
    JMenuItem englishMenu = new JMenuItem("English");
    JMenuItem historyMenu = new JMenuItem("History");
    JMenuItem latinMenu = new JMenuItem("Latin");
    JMenuItem schuelerMenu = new JMenuItem("Students");
    JMenuItem highScoreMenu = new JMenuItem("High-Score");
    JMenuItem helpMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");
    JMenuItem wischnewski = new JMenuItem("Klasse Fr. Wischnewski");
    JMenuItem alvers = new JMenuItem("Klasse Dr. Alvers");
    JMenuItem cool = new JMenuItem("Klasse Cool");


    public MyPopup(MathTrainer mathTrainer) {

        helpMenu.addActionListener(e -> {
            mathTrainer.drawSchueler = false;
            mathTrainer.drawSettings = false;
            mathTrainer.drawHelp = !mathTrainer.drawHelp;
            mathTrainer.repaint();
        });

        newGameMenu.addActionListener(e -> mathTrainer.initBeginning());

        /// Teacher

        alvers.addActionListener(e -> mathTrainer.setActualTeam(0));

        wischnewski.addActionListener(e -> mathTrainer.setActualTeam(1));

        cool.addActionListener(e -> mathTrainer.setActualTeam(2));

        /// end Teacher

        matheMenu.addActionListener(e -> mathTrainer.taskType = TaskTypes.MATHEMATICS);
        englishMenu.addActionListener(e -> mathTrainer.taskType = TaskTypes.ENGLISH);
        historyMenu.addActionListener(e -> mathTrainer.taskType = TaskTypes.HISTORY);
        latinMenu.addActionListener(e -> mathTrainer.taskType = TaskTypes.LATIN);

        settingsMenu.addActionListener(e -> mathTrainer.showSettingsPage());

        schuelerMenu.addActionListener(e -> mathTrainer.showSchuelerPage());

        highScoreMenu.addActionListener(e -> mathTrainer.showHighScorePage());

        add(matheMenu);
        add(englishMenu);
        add(historyMenu);
        add(latinMenu);

        add(new Separator());

        add(newGameMenu);

        add(new Separator());

        add(alvers);
        add(wischnewski);
        add(cool);

        add(new Separator());

        JCheckBox cb = new JCheckBox("Sound on/off");
        cb.setSelected(mathTrainer.playMusic);
        System.out.println("playMusic: " + mathTrainer.playMusic);
        add(cb);
        cb.addActionListener(e -> mathTrainer.toggleMusicOnOff());

        add(new Separator());

        add(highScoreMenu);
        add(schuelerMenu);
        add(settingsMenu);
        add(helpMenu);
    }
}
