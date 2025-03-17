package ru.nms.diplom.ircrossfeature.optimization;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.DoubleRange;

import java.io.*;
import java.util.*;

import static ru.nms.diplom.ircrossfeature.optimization.Utils.computeMRR;
import static ru.nms.diplom.ircrossfeature.optimization.Utils.loadData;
import static ru.nms.diplom.ircrossfeature.util.Constants.FEATURE_STORE_FILE;

public class JeneticsMRROptimizer {

    // Jenetics function for optimization
    public static double fitnessFunction(Genotype<DoubleGene> genotype, List<Map.Entry<Integer, List<Passage>>> data) {
        double p = genotype.chromosome().get(0).doubleValue();
        double w = genotype.chromosome().get(1).doubleValue();
        return computeMRR(data, p, w);  // MRR is directly used as fitness
    }

    // Run Genetic Algorithm using Jenetics
    public static double[] optimizeMRR(List<Map.Entry<Integer, List<Passage>>> data) {
        Engine<DoubleGene, Double> engine = Engine
                .builder(gt -> fitnessFunction(gt, data),
                        DoubleChromosome.of(DoubleRange.of(0.01, 10.0), 2))  // 2 genes: p and w
                .populationSize(100)
                .optimize(Optimize.MAXIMUM)  // Maximize MRR
                .alterers(new Mutator<>(0.1), new MeanAlterer<>(0.6))  // Mutation + Crossover
                .build();

        // Run optimization
        EvolutionResult<DoubleGene, Double> result = engine.stream()
                .limit(100)  // Run for 100 generations
                .collect(EvolutionResult.toBestEvolutionResult());

        Phenotype<DoubleGene, Double> best = result.bestPhenotype();
        double p = best.genotype().chromosome().get(0).doubleValue();
        double w = best.genotype().chromosome().get(1).doubleValue();
        double maxMRR = best.fitness();

        return new double[]{p, w, maxMRR};
    }

    public static void main(String[] args) throws IOException {
        List<Map.Entry<Integer, List<Passage>>> data = loadData(FEATURE_STORE_FILE);

        double[] optimalParams = optimizeMRR(data);
        System.out.printf("Optimal p: %.4f, Optimal w: %.4f, Max MRR: %.6f%n",
                optimalParams[0], optimalParams[1], optimalParams[2]);
    }
}
