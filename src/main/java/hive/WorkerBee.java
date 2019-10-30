package hive;

import java.util.Random;
import java.util.logging.Logger;

public class WorkerBee implements Runnable {
    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    private Random rand = new Random();
    private String name;
    private boolean looking = true;
    private FlowerIntelligenceSink flowerIntelligenceSink;

    WorkerBee(String name, FlowerIntelligenceSink flowerIntelligenceSink) {
        this.name = name;
        this.flowerIntelligenceSink = flowerIntelligenceSink;
    }

    Boolean isLooking() {
        return looking;
    }

    @Override
    public void run() {
        logger.info(String.format("[%s] %s: Looking for flowers...", Thread.currentThread().getName(), name));
        while (looking) {
            try {
                Thread.sleep((rand.nextInt(5) + 1) * 1000);
                if (rand.nextDouble() <= 0.25) {
                    looking = false;
                    logger.info(String.format("[%s] %s: Found flowers, yay!", Thread.currentThread().getName(), name));
                    flowerIntelligenceSink.foundFlowers(name);
                } else {
                    logger.info(String.format("[%s] %s: Still looking...", Thread.currentThread().getName(), name));
                }
            } catch (InterruptedException e) {
                looking = false;
            }
        }
    }

    String getName() {
        return name;
    }
}
