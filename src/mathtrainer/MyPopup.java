package mathtrainer;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem settingsMenu = new JMenuItem("Settings");
    JMenuItem studentsMenu = new JMenuItem("Students");
    JMenuItem helpingMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");

    JRadioButton matheMenu = new JRadioButton("Mathematics [1]");
    JRadioButton historyMenu = new JRadioButton("Dropped [2]");

    JRadioButton julialaura = new JRadioButton(Teams.getTeamName(Teams.JULIA_LAURA));
    JRadioButton laramichel = new JRadioButton(Teams.getTeamName(Teams.LAURA_MICHEL));
    JRadioButton lauramassari = new JRadioButton(Teams.getTeamName(Teams.LAURA_MASSARI));
    JRadioButton michael = new JRadioButton(Teams.getTeamName(Teams.MICHAEL));
    JRadioButton tibor = new JRadioButton(Teams.getTeamName(Teams.TIBOR));
    JRadioButton magdalena = new JRadioButton(Teams.getTeamName(Teams.MAGDALENA));
    JRadioButton luiseleon = new JRadioButton(Teams.getTeamName(Teams.LUISE_LEON));
    JRadioButton marinaevgenija = new JRadioButton(Teams.getTeamName(Teams.MARINA_EVGENIJA));

    public MyPopup(MathTrainer mathTrainer) {

        helpingMenu.addActionListener(e -> {
            mathTrainer.drawStudentsList = false;
            mathTrainer.drawSettings = false;
            mathTrainer.drawHelp = !mathTrainer.drawHelp;
            mathTrainer.repaint();
        });

        newGameMenu.addActionListener(e -> mathTrainer.initBeginning());
        newGameMenu.addActionListener(e -> mathTrainer.toggleCountDownOn());

        /// TEAM
        michael.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.MICHAEL);
            //sleep();
            this.setVisible(false); // ← Close popup after selection
        });
        magdalena.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.MAGDALENA);
            this.setVisible(false);
        });
        luiseleon.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.LUISE_LEON);
            this.setVisible(false);
        });

        marinaevgenija.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.MARINA_EVGENIJA);
            this.setVisible(false);
        });

        tibor.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.TIBOR);
            this.setVisible(false);
        });

        lauramassari.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.LAURA_MASSARI);
            this.setVisible(false);
        });

        julialaura.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.JULIA_LAURA);
            this.setVisible(false);
        });

        laramichel.addActionListener(e -> {
            mathTrainer.setActualTeam(Teams.LAURA_MICHEL);
            this.setVisible(false);
        });

        /// end TEAM

        ButtonGroup subjects = new ButtonGroup();
        subjects.add(matheMenu);
        subjects.add(historyMenu);

        selectActiveTaskType(mathTrainer.getTaskType());

        add(newGameMenu);

        add(new Separator());

        add(matheMenu);
        add(historyMenu);

        matheMenu.addActionListener(e -> mathTrainer.setTaskType(TaskTypes.MATHEMATICS));
        historyMenu.addActionListener(e -> mathTrainer.setTaskType(TaskTypes.DROPPED));

        settingsMenu.addActionListener(e -> mathTrainer.showSettingsPage());
        studentsMenu.addActionListener(e -> mathTrainer.showStudentsPage());

        add(new Separator());

        ButtonGroup teacher = new ButtonGroup();
        teacher.add(julialaura);
        teacher.add(laramichel);
        teacher.add(magdalena);
        teacher.add(lauramassari);
        teacher.add(michael);
        teacher.add(tibor);
        teacher.add(marinaevgenija);
        teacher.add(luiseleon);

        selectActiveTeam(mathTrainer.actualTeam);

        add(julialaura);
        add(laramichel);
        add(magdalena);
        add(lauramassari);
        add(michael);
        add(tibor);
        add(marinaevgenija);
        add(luiseleon);

        add(new Separator());

        add(studentsMenu);
        add(settingsMenu);
        add(helpingMenu);

        add(new Separator());

        JCheckBox scb = new JCheckBox("Sound on/off");
        scb.setSelected(mathTrainer.playMusic);
        add(scb);
        scb.addActionListener(e -> mathTrainer.toggleMusicOnOff());

        JCheckBox ccb = new JCheckBox("Countdown on/off");
        ccb.setSelected(mathTrainer.getCountDownMode());
        ccb.addActionListener(e -> mathTrainer.toggleCountDownOn());
        add(ccb);

        JCheckBox ncb = new JCheckBox("Student name on/off");
        ncb.setSelected(mathTrainer.getDrawStudenNames());
        ncb.addActionListener(e -> mathTrainer.toggleDrawStudentName());
        add(ncb);
    }

    private void selectActiveTaskType(int activeSubject) {
        if (activeSubject == TaskTypes.MATHEMATICS) {
            matheMenu.setSelected(true);
        } else if (activeSubject == TaskTypes.DROPPED) {
            historyMenu.setSelected(true);
        }
    }

    private void selectActiveTeam(int actualTeam) {

        if (actualTeam == Teams.MICHAEL) {
            michael.setSelected(true);
        } else if (actualTeam == Teams.MAGDALENA) {
            magdalena.setSelected(true);
        } else if (actualTeam == Teams.LUISE_LEON) {
            luiseleon.setSelected(true);
        } else if (actualTeam == Teams.MARINA_EVGENIJA) {
            marinaevgenija.setSelected(true);
        } else if (actualTeam == Teams.TIBOR) {
            tibor.setSelected(true);
        } else if (actualTeam == Teams.LAURA_MASSARI) {
            lauramassari.setSelected(true);
        } else if (actualTeam == Teams.JULIA_LAURA) {
            julialaura.setSelected(true);
        } else if (actualTeam == Teams.LAURA_MICHEL) {
            laramichel.setSelected(true);
        }
    }
}
