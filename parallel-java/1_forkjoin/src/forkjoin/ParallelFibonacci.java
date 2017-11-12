package forkjoin;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Intensive {
	private static final int LIMIT = 1000000;

	static public double doCompute(int n) {
        double acc = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < LIMIT; j++) {
                acc += Math.sqrt(j);
            }
        }

        return acc;
    }
}

class SequentialComputer {
    public static int doCompute(List<Integer> range, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            double temp = Intensive.doCompute(range.get(i));
            result += range.get(i);
        }
        return result;
    }
}

class ParallelComputer extends RecursiveAction {
    private List<Integer> range;
    private int start;
    private int end;
    private int value;

    private static final int SEQUENTIAL_THRESHOLD = 10;

    ParallelComputer(List<Integer> range, int start, int end) {
        assert start < end;
        this.range = range;
        this.start = start;
        this.end = end;
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void compute() {
        if (end - start <= SEQUENTIAL_THRESHOLD) {
            value = SequentialComputer.doCompute(range, start, end);
        } else {
            int middle = start + (end - start) / 2;
            ParallelComputer left = new ParallelComputer(range, start, middle);
            ParallelComputer right = new ParallelComputer(range, middle, end);
            left.fork();
            right.compute();
            left.join();
            value = left.getValue() + right.getValue();
        }
    }
}


public class ParallelFibonacci {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("START");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

        double programStart = System.currentTimeMillis();
		double start;

		List<Integer> range = IntStream.range(1, 100).boxed().collect(Collectors.toList());
        System.out.println("The range: " + range);

        start = System.currentTimeMillis();
		System.out.println("sequential: " + SequentialComputer.doCompute(range, 0, range.size()));
        double sequentialTime = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("sequential time: " + sequentialTime);

        start = System.currentTimeMillis();
        ParallelComputer parComputer = new ParallelComputer(range, 0, range.size());
        ForkJoinPool.commonPool().invoke(parComputer);
        double parallelTime = (System.currentTimeMillis() - start) / 1000.;
        System.out.println("parallel time: " + parallelTime);

        System.out.println("parallel: " + parComputer.getValue());

        System.out.println("parallel/sequential ratio: " + (sequentialTime / parallelTime));

        double diff = (System.currentTimeMillis() - programStart) / 1000.;
		System.out.println("DONE in " + diff);
	}

}
