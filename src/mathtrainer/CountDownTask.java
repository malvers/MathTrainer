package mathtrainer;

import java.util.TimerTask;

public class CountDownTask extends TimerTask {

    private int counter;
    MatheTrainer matheTrainer;

    public CountDownTask(MatheTrainer mt, int c) {
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
