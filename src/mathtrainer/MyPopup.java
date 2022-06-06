package mathtrainer;

import mathtrainer.MatheTrainer;
import mratools.MTools;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem helpMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");
    JMenuItem alvers = new JMenuItem("Klasse Alvers");
    JMenuItem heidingsfelder = new JMenuItem("Klasse Heigingsfelder");
    JMenuItem mueller = new JMenuItem("Klasse Müller");
    JMenuItem beyer = new JMenuItem("Klasse Beyer");
    JMenuItem schnabel = new JMenuItem("Klasse Schnabel");
    JMenuItem wilhelm = new JMenuItem("Klasse Wilhelm");

    public MyPopup(MatheTrainer mathTrainer) {

        helpMenu.addActionListener(e -> {
            mathTrainer.drawStatistics = false;
            mathTrainer.drawSettings = false;
            mathTrainer.drawHelp = !mathTrainer.drawHelp;
            mathTrainer.repaint();
        });

        newGameMenu.addActionListener(e -> mathTrainer.initBeginning());

        alvers.addActionListener(e -> mathTrainer.setActualKlasse(0));

        heidingsfelder.addActionListener(e -> mathTrainer.setActualKlasse(3));

        mueller.addActionListener(e -> mathTrainer.setActualKlasse(2));

        beyer.addActionListener(e -> mathTrainer.setActualKlasse(4));

        schnabel.addActionListener(e -> mathTrainer.setActualKlasse(1));

        wilhelm.addActionListener(e -> mathTrainer.setActualKlasse(5));

        add(newGameMenu);

        add(new Separator());

        JCheckBox cb = new JCheckBox("Sound on/off");
        cb.setSelected(mathTrainer.playMusic);
        MTools.println("playMusic: " + mathTrainer.playMusic);
        add(cb);

        cb.addActionListener(e -> mathTrainer.toggleSoundOnOff());

        add(new Separator());

        add(alvers);
        add(heidingsfelder);
        add(mueller);
        add(beyer);
        add(schnabel);
        add(wilhelm);

        add(new Separator());

        add(helpMenu);
    }
}
