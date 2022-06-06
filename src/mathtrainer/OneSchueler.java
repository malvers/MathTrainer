package mathtrainer;

public class OneSchueler {

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
    boolean anwesend = true;

    public OneSchueler(String nameIn) {
        name = nameIn;
        if (nameIn.contentEquals("Michael")) {
            anwesend = false;
        }
        numberTasks = 0;
        numberRightSolutions = 0;
        numberFalseSolutions = 0;
    }

    public void print() {
        System.out.println("name: " + name + " tasks: " + numberTasks);
    }
}
