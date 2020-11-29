package MKAgent;

public class UCT {

    public static double getUCTValue(int noOfTotalVisits, int totalScore, int noOfVisits) {
        if (noOfVisits == 0)
            return Integer.MAX_VALUE;

        return (totalScore / (double)noOfVisits + Math.sqrt(2 * Math.log(noOfTotalVisits) / (double)noOfVisits));
    }

    public static Node chooseBestUCTNode(Node node) {
        return null;
    }
}
