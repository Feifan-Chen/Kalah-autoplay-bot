package MKAgent;

import java.util.Collections;

public class UCT {

    public static double getUCTValue(int noOfTotalVisits, int totalScore, int noOfVisits) {
        if (noOfVisits == 0)
            return Integer.MAX_VALUE;

        return (totalScore / (double)noOfVisits + 2 * Math.sqrt(Math.log(noOfTotalVisits) / (double)noOfVisits));
    }

    public static Node chooseBestUCTNode(Node parent) {
        // Learnt from https://stackoverflow.com/questions/1669282/find-max-value-in-java-with-a-predefined-comparator
        return Collections.max(parent.getChildren(), (first, second) -> {
            assert first.getParent() == second.getParent();
            Node parent1 = first.getParent();
            int totalVisit = parent1.getNoOfVisits();
            double uct1 = getUCTValue(totalVisit, first.getTotalScore(), first.getNoOfVisits());
            double uct2 = getUCTValue(totalVisit, second.getTotalScore(), second.getNoOfVisits());
            return Double.compare(uct1, uct2);
        });
    }
}
