package mathtrainer;

import java.util.ArrayList;

public class AllDropTasks extends ArrayList<DropTask> {

    public AllDropTasks() {
        super(50);
    }

    public void checkForDoubleNames() {

        for (int i = 0; i < size() - 1; i++) {
            if (get(i).Student.contentEquals(get(i + 1).Student)) {
                DropTask store = get(i);
                remove(i);
                add(store);
            }
        }
    }
}
