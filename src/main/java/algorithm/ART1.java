package algorithm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ART1 {

    private final int N = 63;    //Number of components in an input vector.
    private final int M = 30;    //Max number of clusters to be formed.

    private double VIGILANCE = 0.8;

    private final int FONT_WIDTH = 7;

    private double[][] bw = null;    //Bottom-up weights.
    private double[][] tw = null;    //Top-down weights.

    private int[] f1a = null;        //Input layer.
    private int[] f1b = null;        //Interface layer.
    private double[] f2 = null;

    private Map<String, Integer> membership = new HashMap<>();

    public ART1() {
        initialize();
    }

    public double getVIGILANCE() {
        return VIGILANCE;
    }

    public void setVIGILANCE(double VIGILANCE) {
        this.VIGILANCE = VIGILANCE;
    }

    public Map<String, Integer> getMembership() {
        return membership;
    }

    public void setMembership(Map<String, Integer> membership) {
        this.membership = membership;
    }

    public void initialize() {

        // Initialize bottom-up weight matrix.
        bw = new double[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                bw[i][j] = 1.0 / (1.0 + N);
            }
        }

        // Initialize top-down weight matrix.
        tw = new double[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                tw[i][j] = 1.0;
            }
        }

        f1a = new int[N];
        f1b = new int[N];
        f2 = new double[M];
    }

    public void train(Integer[] data, String name) {
        int inputSum = 0;
        int activationSum = 0;
        int f2Max = 0;
        boolean reset = true;

        // Initialize f2 layer activations to 0.0
        for (int i = 0; i < M; i++) {
            f2[i] = 0.0;
        }

        // Input pattern() to F1 layer.
        for (int i = 0; i < N; i++) {
            f1a[i] = data[i];
        }

        // Compute sum of input pattern.
        inputSum = vectorSum(f1a);

        // Compute activations for each node in the F1 layer.
        // Send input signal from f1a to the f1b layer.
        for (int i = 0; i < N; i++) {
            f1b[i] = f1a[i];
        }

        // Compute net input for each node in the f2 layer.
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                f2[i] += bw[i][j] * (double) f1a[j];
            }
        }

        reset = true;
        while (reset == true) {
            // Determine the largest value of the f2 nodes.
            f2Max = maximum(f2);

            // Recompute the f1a to f1b activations (perform AND function).
            for (int i = 0; i < N; i++) {
                f1b[i] = f1a[i] * (int) Math.floor(tw[f2Max][i]);
            }

            // Compute sum of input pattern.
            activationSum = vectorSum(f1b);

            reset = testForReset(activationSum, inputSum, f2Max);
        }

        // Only use number of TRAINING_PATTERNS for training, the rest are tests.
        updateWeights(activationSum, f2Max);

        // Record which cluster the input vector is assigned to.
        membership.put(name, f2Max);
    }

    public boolean testForReset(int activationSum, int inputSum, int f2Max) {
        if ((double) activationSum / (double) inputSum >= VIGILANCE) {
            return false;     // Candidate is accepted.
        } else {
            f2[f2Max] = -1.0; // Inhibit.
            return true;      // Candidate is rejected.
        }
    }

    public void updateWeights(int activationSum, int f2Max) {
        // Update bw(f2Max)
        for (int i = 0; i < N; i++) {
            bw[f2Max][i] = (2.0 * (double) f1b[i]) / (1.0 + (double) activationSum);
        }

        // Update tw(f2Max)
        for (int i = 0; i < N; i++) {
            tw[f2Max][i] = f1b[i];
        }

    }

    public int vectorSum(int[] nodeArray) {
        int tempSum = 0;

        // Compute sum of input pattern.
        for (int i = 0; i < N; i++) {
            tempSum += nodeArray[i];
        }

        return tempSum;
    }

    public int maximum(double[] nodeArray) {
        int winner = 0;
        boolean foundNewWinner = false;
        boolean done = false;

        while (!done) {
            foundNewWinner = false;
            for (int i = 0; i < M; i++) {
                if (i != winner) {
                    if (nodeArray[i] > nodeArray[winner]) {
                        winner = i;
                        foundNewWinner = true;
                    }
                }
            }

            if (foundNewWinner == false) {
                done = true;
            }
        }

        return winner;
    }

    public double[] getTw(Integer number) {
        return tw[number];
    }

    public Integer test(Integer[] data) {
        int inputSum = 0;
        int activationSum = 0;
        int f2Max = 0;
        boolean reset = true;

        // Initialize f2 layer activations to 0.0
        for (int i = 0; i < M; i++) {
            f2[i] = 0.0;
        }

        // Input pattern() to F1 layer.
        for (int i = 0; i < N; i++) {
            f1a[i] = data[i];
        }

        // Compute sum of input pattern.
        inputSum = vectorSum(f1a);

        // Compute activations for each node in the F1 layer.
        // Send input signal from f1a to the f1b layer.
        for (int i = 0; i < N; i++) {
            f1b[i] = f1a[i];
        }

        // Compute net input for each node in the f2 layer.
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                f2[i] += bw[i][j] * (double) f1a[j];
            }
        }

        reset = true;
        while (reset == true) {
            // Determine the largest value of the f2 nodes.
            f2Max = maximum(f2);

            // Recompute the f1a to f1b activations (perform AND function).
            for (int i = 0; i < N; i++) {
                f1b[i] = f1a[i] * (int) Math.floor(tw[f2Max][i]);
            }

            // Compute sum of input pattern.
            activationSum = vectorSum(f1b);

            reset = testForReset(activationSum, inputSum, f2Max);
        }

        // Record which cluster the input vector is assigned to.
        return f2Max;
    }

    public String getClustersString() {
        StringBuilder sb = new StringBuilder();
        int maxValue = membership.values().stream()
                .max(Comparator.comparingInt(o -> o)).get();

        for (int i = 0; i < maxValue + 1; i++) {
            sb.append("Cluster ").append(i).append(" : ");

            for (Map.Entry<String, Integer> entry : membership.entrySet()) {
                if (entry.getValue() == i) {
                    sb.append(entry.getKey()).append(", ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}