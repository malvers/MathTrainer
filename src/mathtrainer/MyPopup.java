package mathtrainer;

import javax.swing.*;

public class MyPopup extends JPopupMenu {

    JMenuItem settingsMenu = new JMenuItem("Settings");
    JMenuItem studentsMenu = new JMenuItem("Students");
    JMenuItem highScoreMenu = new JMenuItem("High-Score");
    JMenuItem helpMenu = new JMenuItem("Help");
    JMenuItem newGameMenu = new JMenuItem("New game");

    JRadioButton complexMathMenu = new JRadioButton("Complex math [1]");
    JRadioButton matheMenu = new JRadioButton("Mathematics [2]");
    JRadioButton englishMenu = new JRadioButton("English [3]");
    JRadioButton historyMenu = new JRadioButton("Dropped [4]");

    JRadioButton alvers = new JRadioButton("Team Dr. Alvers");
    JRadioButton wischnewski = new JRadioButton("Team Ms. Wischnewski");
    JRadioButton michel = new JRadioButton("Team Mr. Michel");

    public MyPopup(MathTrainer mathTrainer) {

        helpMenu.addActionListener(e -> {
            mathTrainer.drawStudents = false;
            mathTrainer.drawSettings = false;
            mathTrainer.drawHelp = !mathTrainer.drawHelp;
            mathTrainer.repaint();
        });

        newGameMenu.addActionListener(e -> mathTrainer.initBeginning());

        /// Teacher
        alvers.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.ALVERS);
            //sleep();
            this.setVisible(false); // ← Close popup after selection
        });
        wischnewski.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.WISCHNEWSKI);
            //sleep();
            this.setVisible(false);
        });
        michel.addActionListener(e -> {
            mathTrainer.setActualTeam(Teachers.MICHEL);
            //sleep();
            this.setVisible(false);
        });
        /// end Teacher

        ButtonGroup subjects = new ButtonGroup();
        subjects.add(matheMenu);
        subjects.add(englishMenu);
        subjects.add(historyMenu);
        subjects.add(complexMathMenu);

        selectActiveTaskType(mathTrainer.getTaskType());

        add(complexMathMenu);
        add(matheMenu);
        add(englishMenu);
        add(historyMenu);

        matheMenu.addActionListener(e -> mathTrainer.setTaskType(TaskTypes.MATHEMATICS));
        englishMenu.addActionListener(e -> mathTrainer.setTaskType(TaskTypes.ENGLISH));
        historyMenu.addActionListener(e -> mathTrainer.setTaskType(TaskTypes.DROPPED));
        complexMathMenu.addActionListener(e -> mathTrainer.setTaskType(TaskTypes.COMPLEXMATH));

        settingsMenu.addActionListener(e -> mathTrainer.showSettingsPage());
        studentsMenu.addActionListener(e -> mathTrainer.showStudentsPage());
        highScoreMenu.addActionListener(e -> mathTrainer.showHighScorePage());

        add(new Separator());

        add(newGameMenu);

        add(new Separator());

        ButtonGroup teacher = new ButtonGroup();
        teacher.add(alvers);
        teacher.add(wischnewski);
        teacher.add(michel);

        selectActiveTeam(mathTrainer.actualTeam);

        add(alvers);
        add(wischnewski);
        add(michel);

        add(new Separator());

        JCheckBox cb = new JCheckBox("Sound on/off");
        cb.setSelected(mathTrainer.playMusic);
        add(cb);
        cb.addActionListener(e -> mathTrainer.toggleMusicOnOff());

        add(new Separator());

        add(highScoreMenu);
        add(studentsMenu);
        add(settingsMenu);
        add(helpMenu);
    }

    private void selectActiveTaskType(int activeSubject) {
        if (activeSubject == TaskTypes.COMPLEXMATH) {
            complexMathMenu.setSelected(true);
        } else if (activeSubject == TaskTypes.MATHEMATICS) {
            matheMenu.setSelected(true);
        } else if (activeSubject == TaskTypes.ENGLISH) {
            englishMenu.setSelected(true);
        }else if (activeSubject == TaskTypes.DROPPED) {
            historyMenu.setSelected(true);
        }
    }

    private void selectActiveTeam(int actualTeam) {

        if (actualTeam == Teachers.ALVERS) {
            alvers.setSelected(true);
        } else if (actualTeam == Teachers.WISCHNEWSKI) {
            wischnewski.setSelected(true);
        } else if (actualTeam == Teachers.MICHEL) {
            michel.setSelected(true);
        }
    }
}
