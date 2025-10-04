package mathtrainer;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem settingsMenu = new JMenuItem("Settings");
    JMenuItem studentsMenu = new JMenuItem("Students");
    JMenuItem helpingMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");

    JRadioButton matheMenu = new JRadioButton("Mathematics [1]");
    JRadioButton historyMenu = new JRadioButton("Dropped [2]");

    JRadioButton michael = new JRadioButton("Team Michael");
    JRadioButton tibor = new JRadioButton("Team Tibor");
    JRadioButton magdalena = new JRadioButton("Team Magdalena");
    JRadioButton hannah = new JRadioButton("Team Hannah");
    JRadioButton maria = new JRadioButton("Team Maria");

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
            mathTrainer.setActualTeam(Teachers.MICHAEL);
            //sleep();
            this.setVisible(false); // ← Close popup after selection
        });
        magdalena.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.MAGDALENA);
            this.setVisible(false);
        });
        hannah.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.HANNAH);
            this.setVisible(false);
        });

        maria.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.MARIA);
            this.setVisible(false);
        });

        tibor.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.TIBOR);
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
        teacher.add(michael);
        teacher.add(tibor);
        teacher.add(magdalena);
        teacher.add(hannah);
        teacher.add(maria);

        selectActiveTeam(mathTrainer.actualTeam);

        add(michael);
        add(tibor);
        add(magdalena);
        add(hannah);
        add(maria);

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

        if (actualTeam == Teachers.MICHAEL) {
            michael.setSelected(true);
        } else if (actualTeam == Teachers.MAGDALENA) {
            magdalena.setSelected(true);
        } else if (actualTeam == Teachers.HANNAH) {
            hannah.setSelected(true);
        } else if (actualTeam == Teachers.MARIA) {
            maria.setSelected(true);
        } else if (actualTeam == Teachers.TIBOR) {
            tibor.setSelected(true);
        }
    }
}
