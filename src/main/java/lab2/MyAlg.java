package lab2;

import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MyAlg {

    public static void main(String[] args) throws IOException {
        int dimension = 2; // dimension of problem
        int populationSize = 90; // size of population
        final int generations = 100; // number of generations
        int crossoverPoints = 1;

        final int[] bestGenerations = {0};
        final double[] bestFit_w = {0};

        double meanFit = 0;
        double meanGenerations = 0;
        long times = 0;
        final PrintWriter writer = new PrintWriter("results.txt", "UTF-8");
        for (int i = 0; i < 1000; i++) {
            long startTime = System.nanoTime();
            Random random = new Random(); // random

            CandidateFactory<double[]> factory = new MyFactory(dimension); // generation of solutions

            ArrayList<EvolutionaryOperator<double[]>> operators = new ArrayList<>();
            operators.add(new MyCrossover(new ChooseCrossover(), crossoverPoints)); // Crossover
            operators.add(new MyCrossover(new MixCrossover(), crossoverPoints)); // Crossover
            operators.add(new MyMutation()); // Mutation
            EvolutionPipeline<double[]> pipeline = new EvolutionPipeline<>(operators);

            SelectionStrategy<Object> selection = new RouletteWheelSelection(); // Selection operator

            FitnessEvaluator<double[]> evaluator = new FitnessFunction(dimension); // Fitness function

            EvolutionEngine<double[]> algorithm = new SteadyStateEvolutionEngine<>(
                    factory, pipeline, evaluator, selection, populationSize, false, random);

            algorithm.addEvolutionObserver(new EvolutionObserver() {
                public void populationUpdate(PopulationData populationData) {
                    double bestFit = populationData.getBestCandidateFitness();
                    System.out.println("Generation " + populationData.getGenerationNumber() + ": " + bestFit);
                    System.out.println("\tBest solution = " + Arrays.toString((double[]) populationData.getBestCandidate()));
                    System.out.println("\tPop size = " + populationData.getPopulationSize());
                    if (bestFit > bestFit_w[0]) {
                        bestFit_w[0] = bestFit;
                        bestGenerations[0] = populationData.getGenerationNumber();
                    }
                }
            });

            TerminationCondition terminate = new GenerationCount(generations);
            algorithm.evolve(populationSize, 1, terminate);
            long endTime = System.nanoTime();
            times += endTime - startTime;
            writer.println(bestGenerations[0] + " " + bestFit_w[0]);
        }
        writer.close();

        try (BufferedReader br = new BufferedReader(new FileReader("results.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int count = 0;
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                try {
                    line = br.readLine();
                    String[] words = line.split(" ");
                    meanFit = (meanFit * count + Double.parseDouble(words[1])) / (count + 1);
                    meanGenerations = (meanGenerations * count + Integer.parseInt(words[0])) / (count + 1);
                    count += 1;
                } catch (NullPointerException ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Gen: " + meanGenerations + " Fit: " + meanFit + " time: " + times / (1000 * 1000000));
    }
}
