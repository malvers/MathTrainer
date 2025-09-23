package mathtrainer;

import java.util.ArrayList;

public class AllDropTasks extends ArrayList<DropTask> {

    public AllDropTasks() {
        super(50);
    }

    public void checkForDoubleNames() {

        for (int i = 0; i < size() - 1; i++) {
            if (get(i).student.contentEquals(get(i + 1).student)) {
                DropTask store = get(i);
                remove(i);
                add(store);
            }
        }
    }

    public void print() {

        for (int i = 0; i < size(); i++) {
            String q = get(i).getQuestion();
            String a = get(i).getAnswer();
            //System.out.println(q + " ::: " + a);
        }
    }
}
