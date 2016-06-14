package org.talend.dataquality.datamasking.shuffling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This class offers a shuffling service to manipulates the {@link ShuffleColumn} action and the
 * {@link ShufflingHandler} action together.
 */
public class ShufflingService {

    protected ShuffleColumn shuffleColumn;

    protected ConcurrentLinkedQueue<Future<List<List<Object>>>> concurrentQueue = new ConcurrentLinkedQueue<Future<List<List<Object>>>>();

    protected ShufflingHandler shufflingHandler;

    protected ExecutorService executor;

    private List<List<Object>> rows = new ArrayList<>();

    private int seperationSize = Integer.MAX_VALUE;

    private boolean hasLaunched = false;

    private boolean hasFinished = false;

    private boolean hasSubmitted = false;

    /**
     * Constructor without the partition choice
     * 
     * @param shuffledColumns the 2D list of shuffled columns
     * @param allInputColumns the list of all input columns name
     * @throws IllegalArgumentException when the some columns in the shuffledColumns do not exist in the allInputColumns
     */
    public ShufflingService(List<List<String>> shuffledColumns, List<String> allInputColumns) throws IllegalArgumentException {
        this.shuffleColumn = new ShuffleColumn(shuffledColumns, allInputColumns);
    }

    public ShufflingService(List<List<String>> shuffledColumns, List<String> allInputColumns, List<String> partitionColumns)
            throws IllegalArgumentException {
        this.shuffleColumn = new ShuffleColumn(shuffledColumns, allInputColumns, partitionColumns);
    }

    public void setShufflingHandler(ShufflingHandler shufflingHandler) {
        this.shufflingHandler = shufflingHandler;
    }

    /**
     * Executes a row list value.<br>
     * 
     * The row is not executed immediately but is submitted to the {@link java.util.concurrent.ExecutorService}.<br>
     * The results will be retrieved from the {@link java.util.concurrent.Future} objects which are appended to a
     * {@link java.util.concurrent.ConcurrentLinkedQueue}<br>
     * 
     * If the variable hasFinished equals true, it means this service has been closed. Tests whether the rows is empty
     * or not. If the rows have still the values, submits those values to a callable process.<br>
     * 
     * If the variable hasFinished equals false, adds the new value into the rows. Tests whether the rows' size equals
     * the partition demand. When the size equals the partition size, submits those values to a callable process.<br>
     * 
     * @param row
     */
    protected synchronized void execute(List<Object> row) {
        launcheHandler();

        if (hasSubmitted) {
            if (!rows.isEmpty()) {
                executeFutureCall();
            }
        } else {
            if (!row.isEmpty()) {
                rows.add(row);
                if (rows.size() == seperationSize) {
                    executeFutureCall();
                }
            }
        }
    }

    /**
     * Deep copies the rows value to another 2D list. Submits the rows' value to a callable process. Then submits the
     * process to the executor.
     */
    private void executeFutureCall() {
        List<List<Object>> copyRow = deepCopyListTo(rows);
        Future<List<List<Object>>> future = executor.submit(new RowDataCallable<List<List<Object>>>(shuffleColumn, copyRow));
        concurrentQueue.add(future);
    }

    private void launcheHandler() {
        if (!hasLaunched) {
            shufflingHandler.start();
            hasLaunched = true;
        }

        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        } else {
            if (executor.isShutdown()) {
                throw new IllegalArgumentException("executor shutdown");
            }
        }
    }

    private synchronized List<List<Object>> deepCopyListTo(List<List<Object>> rows) {
        List<List<Object>> copyRows = new ArrayList<>();
        for (List<Object> row : rows) {
            List<Object> copyRow = new ArrayList<>();
            for (Object o : row) {
                copyRow.add(o);
            }
            copyRows.add(copyRow);
        }
        rows.clear();
        return copyRows;

    }

    public void setSeperationSize(int seperationSize2) {
        this.seperationSize = seperationSize2;
    }

    /**
     * Adds a new row in the waiting list and check the size of waiting list. When the waiting list fulfills the
     * partition size then launches the shuffle algorithm.
     * 
     * @param row a list of row data
     */
    public void addOneRow(List<Object> row) {
        execute(row);
    }

    public ConcurrentLinkedQueue<Future<List<List<Object>>>> getConcurrentQueue() {
        return concurrentQueue;
    }

    public boolean hasFinished() {
        return hasFinished;
    }

    /**
     * <ul>
     * <li>First sets the hasSubmitted variable to be true and launches the execute() method with the global variable
     * hasSubmitted equals true. This allows the resting rows to be submitted to a callable process.
     * <li>To avoid the handler stopping scanning the result, lets the thread sleep 100 miliseconds. This allows the
     * last callable job to stand by</li>
     * <li>Sets the hasFinished variable true to announce the handler to finish the scan</li>
     * </ul>
     * 
     * @param hasFinished
     */
    public void setHasFinished(boolean hasFinished) {
        this.hasSubmitted = hasFinished;
        execute(null);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.hasFinished = hasFinished;
    }

    /**
     * Shuffles a table's value.
     * 
     * @param rows list of list of object
     */
    public void shuffle(List<List<Object>> rows) {
        shuffleColumn.shuffle(rows);
    }

    /**
     * Sets the a table value directly by giving a 2D list.
     * 
     * @param rows list of list of object
     */
    public void setRows(List<List<Object>> rows) {
        for (List<Object> row : rows) {
            execute(row);
        }
    }

    /**
     * Shuts down the shuffling execution
     */
    public void shutDown() {
        System.out.println("-------- Shuffling service shutdown");
        if (executor != null) {
            try {
                executor.shutdown();
                // wait until all the address object in the linked queue are handled.
                while (concurrentQueue.size() > 0) {
                    Thread.sleep(2000);
                }
                // Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or
                // the
                // current thread is interrupted, whichever happens first.
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void setRandomSeed(long seed) {
        this.shuffleColumn.setRandomSeed(seed);
    }

}
