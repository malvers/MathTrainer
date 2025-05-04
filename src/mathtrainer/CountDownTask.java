package mathtrainer;

import java.util.TimerTask;

public class CountDownTask extends TimerTask {

    private int counter;
    MathTrainer matheTrainer;

    public CountDownTask(MathTrainer mt, int c) {
        matheTrainer = mt;
        counter = c;
    }

    @Override
    public void run() {
        if (counter == 0) matheTrainer.fireDown();
        matheTrainer.setCountDown(counter);
        --counter;
    }
}
