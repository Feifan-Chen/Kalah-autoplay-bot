package MKAgent;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

public class MCTSThread implements Callable<Node> {

    private Side mySide;
    private Board board;
    private long timeAllowed;

    public MCTSThread(Side mySide, Board board, long timeAllowed) {
        this.mySide = mySide;
        this.board = board;
        this.timeAllowed = timeAllowed;
    }

    @Override
    public Node call() throws Exception {
        int generation = 0;
        final int GEN_LIMIT = Integer.MAX_VALUE;

        long endTime = System.currentTimeMillis() + timeAllowed;


        Node root = new Node(0, 0, mySide, null, board, null, new ArrayList<>());

        Node bestChild = null;

        boolean inLimit = true;


        while (inLimit || bestChild == null) {
            inLimit = System.currentTimeMillis() < endTime && generation < GEN_LIMIT;
            generation++;

            // Selection and Expansion.
            Node nodeToExplore = Main.selectionAndExpansion(root);

            // Rollout and BackPropagation.
            Main.rolloutAndBackPropagation(nodeToExplore, timeAllowed);

            if (!inLimit)
                bestChild = Main.getMaxRobustChild(root);
        }

        for(Node child : root.getChildren()){
            System.err.println(child.getNoOfVisits() + "generation: " + generation);
        }

        return root;

    }
}

/*
      Node root = new Node(0, 0, mySide, null, board, null, new ArrayList<>());

        Node temp = expansion(root);

        int nThreads = root.getChildren().size();
        System.err.println("threads number :" + nThreads);
        Long start = System.currentTimeMillis();

        ExecutorService exs = Executors.newFixedThreadPool(nThreads);
        ArrayList<Node> children = new ArrayList<>();
        try{
            List<Future<Node>> futureList = new ArrayList<Future<Node>>();


            for (Node child: root.getChildren()){
                futureList.add(exs.submit(new MCTSThread(child.getWhosTurnNext(), child.getBoard(), timeAllowed)));
            }

//            Long getResultStart = System.currentTimeMillis();
//            System.err.println("START AGG = " + new Date());

            while (futureList.size() > 0)
            {
                Iterator<Future<Node>> iterable = futureList.iterator();

                while (iterable.hasNext())
                {
                    Future<Node> future = iterable.next();

                    if (future.isDone() && !future.isCancelled())
                    {
                        Node i = future.get();
                        children.add(i);
                        System.err.println("TASK i = " + i + "finished" + new Date());
                        iterable.remove();
                    }
                    else {
                        Thread.sleep(100);
                    }
                }
            }
//            System.err.println("total payoff=" + result);
            System.err.println("time spend=" + (System.currentTimeMillis() - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            exs.shutdown();
        }

        root.setChildren(children);
        System.err.println("child size: " + children.size());
        Node bestChild = getMaxRobustChild(root);

        //return root.getBestChild().getMove();
        return bestChild.getMove();
 */