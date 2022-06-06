package mathtrainer;//import mratools.System.out;

import java.awt.*;

public class CalculationTask {

    private final short operation;
    String name;
    int number1;
    int number2;
    private String sOperator;

    public CalculationTask(String nameIn, Reihen reihen, boolean limitToSelected, short op) {

        operation = op;
        if (operation == 0) {
            sOperator = " \u2219 ";
        } else if (operation == 1) {
            sOperator = " ÷ ";
        } else if (operation == 2) {
            sOperator = " + ";
        } else if (operation == 3) {
            sOperator = " − ";
        }

        /// TODO: consider reihen.size() as limit beyond 9er Reihe

        while (!reihen.isOn(number1 = (int) (Math.random() * 8 + 2)))
            ;

        if (limitToSelected) {
            while (!reihen.isOn(number2 = (int) (Math.random() * 8 + 2)))
                ;
        } else {
            number2 = (int) (Math.random() * 8 + 2);
        }

        // division !
        if (operation == Operations.divide || operation == Operations.minus) {
            number1 = number2 * number1;
        }

        name = nameIn;
    }

    String getTaskString() {

        return number1 + sOperator + number2;
    }

    public void print(int i) {

        String space = "";
        if (i < 10) {
            space = " ";
        }
        System.out.println(name + " ->\t" + space + i + " ->\t" + number1 + sOperator + number2 + " = ");
    }

    public short getOperation() {
        return operation;
    }

    public int getResult() {

        if (operation == Operations.multiply) {
            return number2 * number1;
        }
        if (operation == Operations.divide) {
            return number1 / number2;
        }
        if (operation == Operations.plus) {
            return number2 + number1;
        }
        if (operation == Operations.minus) {
            return number1 - number2;
        }
        return Integer.MAX_VALUE;
    }

    public Color getColor() {
        if( operation == Operations.plus ) return Color.CYAN;
        if( operation == Operations.minus ) return Color.RED.darker();
        if( operation == Operations.multiply ) return Color.GREEN;
        if( operation == Operations.divide ) return Color.ORANGE;
        return Color.MAGENTA;
    }
}
