package mathtrainer;

import java.util.ArrayList;

public class AllComplexMathTasks extends ArrayList<ComplexMathTask> {

    public AllComplexMathTasks() {
        super(50);
    }

    public void print() {

        for (int i = 0; i < size(); i++) {
            String q = get(i).getQuestion();
            String a = get(i).getAnswer();
            System.out.println(q + "   :   " + a);
        }
    }
}
