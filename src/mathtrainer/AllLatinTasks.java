package mathtrainer;

import java.util.ArrayList;

public class AllLatinTasks extends ArrayList<LatinTask> {

    public AllLatinTasks() {
        super(50);
    }

    public void print() {

        for (int i = 0; i < size(); i++) {
            get(i).print(i);
        }
    }

    public void checkForDoubleNames() {

        for (int i = 0; i < size() - 1; i++) {
            if (get(i).name.contentEquals(get(i + 1).name)) {
                LatinTask store = get(i);
                remove(i);
                add(store);
            }
        }
    }
}
