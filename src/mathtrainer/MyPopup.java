package mathtrainer;

import mathtrainer.MatheTrainer;
import mratools.MTools;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem helpMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");
    JMenuItem obenaus = new JMenuItem("Klasse Fr. Obenaus");
    JMenuItem tremel = new JMenuItem("Klasse Fr. Tremel");
    JMenuItem alvers = new JMenuItem("Klasse Dr. Alvers");
    JMenuItem heidingsfelder = new JMenuItem("Klasse Hr. Heigingsfelder");
    JMenuItem mueller = new JMenuItem("Klasse Hr. Müller");
    JMenuItem beyer = new JMenuItem("Klasse Fr. Beyer");
    JMenuItem schnabel = new JMenuItem("Klasse Fr. Schnabel");
    JMenuItem wilhelm = new JMenuItem("Klasse Fr. Wilhelm");
    JMenuItem platz = new JMenuItem("Klasse Fr. Platz");
    JMenuItem liebermann = new JMenuItem("Klasse Fr. Liebermann");

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

        obenaus.addActionListener(e -> mathTrainer.setActualKlasse(6));

        tremel.addActionListener(e -> mathTrainer.setActualKlasse(7));

        platz.addActionListener(e -> mathTrainer.setActualKlasse(8));

        liebermann.addActionListener(e -> mathTrainer.setActualKlasse(8));

        add(newGameMenu);

        add(new Separator());

        JCheckBox cb = new JCheckBox("Sound on/off");
        cb.setSelected(mathTrainer.playMusic);
        MTools.println("playMusic: " + mathTrainer.playMusic);
        add(cb);

        cb.addActionListener(e -> mathTrainer.toggleSoundOnOff());

        add(new Separator());

        add(alvers);
        add(beyer);
        add(heidingsfelder);
        add(mueller);
        add(liebermann);
        add(obenaus);
        add(schnabel);
        add(platz);
        add(tremel);
        add(wilhelm);

        add(new Separator());

        add(helpMenu);
    }
}
