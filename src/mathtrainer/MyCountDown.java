package mathtrainer;

import java.util.Timer;

public class MyCountDown extends Timer {

    public MyCountDown(MathTrainer matheTrainer, int countDownFromIn) {
        schedule(new CountDownTask(matheTrainer, countDownFromIn), 0, 1000);
    }
}
