package MKAgent;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

public class SimulationThread implements Callable<Integer> {

    private Side mySide;
    private Board myBoard;
    private Side currentSide;

    public SimulationThread(Side mySide, Board myBoard, Side currentSide) {
        this.mySide = mySide;
        this.myBoard = myBoard;
        this.currentSide = currentSide;
    }

    @Override
    public Integer call() throws Exception {
        int payoff = 0;

        for (int i = 0; i < 10; i ++)
        {
            Board board = new Board(myBoard);
            while(!Kalah.gameOver(board))
            {
                ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, currentSide);
                Move next_move = legalMoves.get(new Random().nextInt(legalMoves.size()));
                currentSide = Kalah.makeMove(board, next_move);
            }
            int result;
            if(board.payoffs(mySide) > 0) result = 1; else result = 0;
            payoff += result;
        }

        return payoff;
    }

}

/*
        int result = 0;

        Long start = System.currentTimeMillis();
        int nThreads = 5;
        ExecutorService exs = Executors.newFixedThreadPool(nThreads);
        try{
            List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();

            for (int i = 0; i < nThreads; i++)
            {
                futureList.add(exs.submit(new SimulationThread(mySide, board, side)));
            }

//            Long getResultStart = System.currentTimeMillis();
//            System.err.println("START AGG = " + new Date());

            while (futureList.size() > 0)
            {
                Iterator<Future<Integer>> iterable = futureList.iterator();

                while (iterable.hasNext())
                {
                    Future<Integer> future = iterable.next();

                    if (future.isDone() && !future.isCancelled())
                    {
                        Integer i = future.get();
//                        System.err.println("TASK i = " + i + "finished" + new Date());
                        result += i;
                        iterable.remove();
                    }
                    else {
                        Thread.sleep(1);
                    }
                }
            }
//            System.err.println("total payoff=" + result);
            System.err.println("time spend=" + (System.currentTimeMillis() - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        double score = (double)result / 5;

        System.err.println("time spend=" + (System.currentTimeMillis() - start));
 */
