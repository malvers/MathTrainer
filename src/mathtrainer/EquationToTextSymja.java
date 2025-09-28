package mathtrainer;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class EquationToTextSymja {

    private static ExprEvaluator util = new ExprEvaluator();

    // Convert integer to words (supports up to 999)
    private static String[] units = {
            "zero","one","two","three","four","five","six","seven","eight","nine",
            "ten","eleven","twelve","thirteen","fourteen","fifteen",
            "sixteen","seventeen","eighteen","nineteen"
    };

    private static String[] tens = {
            "","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"
    };

    public static String numberToWords(int n) {
        if(n < 20) return units[n];
        if(n < 100) return tens[n/10] + (n%10==0?"":"-"+units[n%10]);
        if(n < 1000) return units[n/100]+" hundred" + (n%100==0?" ":" "+numberToWords(n%100));
        return String.valueOf(n);
    }

    // Recursively convert Symja expression to words
    public static String exprToWords(IExpr expr) {
        String str = expr.toString();
        if(expr.isInteger()) {
            try {
                int n = Integer.parseInt(str);
                return numberToWords(n);
            } catch(Exception e) {
                return str;
            }
        } else if(expr.isPlus()) {
            return exprToWords(expr.getAt(1)) + " plus " + exprToWords(expr.getAt(2));
        } else if(expr.isTimes()) {
            return exprToWords(expr.getAt(1)) + " times " + exprToWords(expr.getAt(2));
        } else if(expr.isPower()) {
            return exprToWords(expr.getAt(1)) + " to the power of " + exprToWords(expr.getAt(2));
        } else if(expr.isSqrt()) {
            return "square root of " + exprToWords(expr.getAt(1));
        } else if(str.startsWith("Divide")) {
            return exprToWords(expr.getAt(1)) + " divided by " + exprToWords(expr.getAt(2));
        }
        return str;
    }

    // Convert a full equation line like "\( \sqrt{n+4} - \sqrt{n} = 3 \) :: \( n = 1 \)"
    public static String equationToText(String line) {
        line = line.replace("\\(","").replace("\\)","").trim();
        String[] parts = line.split("::");
        if(parts.length!=2) return line;

        String exprStr = parts[0].trim();
        String solStr  = parts[1].trim();

        try {
            IExpr expr = util.evaluate(exprStr);
            String spoken = exprToWords(expr) + ", so " + solStr.replace("=","equals");
            return spoken;
        } catch(Exception e) {
            return "Error parsing: "+exprStr;
        }
    }

    public static void main(String[] args) {
        String[] lines = {
                "\\( \\sqrt{n} + \\sqrt{4n} = 12 \\) :: \\( n = 16 \\)",
                "\\( \\frac{x}{21} = 3 \\) :: \\( x = 63 \\)",
                "\\( x^2 + 3 = 12 \\) :: \\( x = 3 \\)",
                "\\( \\sqrt{n+4} - \\sqrt{n} = 3 \\) :: \\( n = 1 \\)"
        };

        for(String line: lines) {
            System.out.println(equationToText(line));
        }
    }
}
