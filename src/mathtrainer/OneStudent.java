package mathtrainer;

public class OneStudent {

    public int numberFalseSolutions;
    String name;

    public int getNumberTasks() {
        return numberTasks;
    }

    public void setNumberTasks(int numberTasks) {
        this.numberTasks = numberTasks;
    }

    private int numberTasks;
    int numberRightSolutions;
    boolean present = true;

    public OneStudent(String nameIn) {
        name = nameIn;
        if (nameIn.contentEquals("Michael")) {
            present = false;
        }
        numberTasks = 0;
        numberRightSolutions = 0;
        numberFalseSolutions = 0;
    }

    public void print() {
        System.out.println("name: " + name + " tasks: " + numberTasks);
    }
}
