package hive;

import scala.Int;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class QueenBee implements Runnable, FlowerIntelligenceSink {

    private int numFlowersNeeded;
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    private ExecutorService executor;
    private Random rand = new Random();
    private int numFlowersFound;

    QueenBee(ExecutorService executor, int numFlowersNeeded) {
        this.executor = executor;
        this.numFlowersNeeded = numFlowersNeeded;
    }

    private void searchForPollen() {
        WorkerBee bee = new WorkerBee(UUID.randomUUID().toString(), this);
        executor.execute(bee);
    }

    public void run() {
        logger.info(String.format("QueenBee: Need to find %d flower(s) today...", numFlowersNeeded));

        for (int i = 0; i < numFlowersNeeded; i++) {
            searchForPollen();
        }

        while (numFlowersFound < numFlowersNeeded) {
            logger.info("QueenBee: Waiting for flowers...");
            try { Thread.sleep(3 * 1000); }
            catch (InterruptedException ignored) {}
        }

        shutdownAndAwaitTermination();
        logger.info(String.format("QueenBee: Found %d flowers.  All done for today", numFlowersNeeded));
        System.exit(0);
    }

    private void shutdownAndAwaitTermination() {
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    logger.warning("QueenBee: Pool did not terminate!");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void foundFlowers(String workerBeeName) {
        numFlowersFound++;
        logger.info(String.format("QueenBee: %s found flowers! %d so far today", workerBeeName, numFlowersFound));

    }

    public static void main(String[] args) {

        QueenBee queenBee = new QueenBee(Executors.newSingleThreadExecutor(), 3);

        queenBee.run();

        try { Thread.sleep(Int.MaxValue()); }
        catch (InterruptedException e) {
            System.exit(0);
        }

    }

}
