package ru.nms.diplom.ircrossfeature.optimization;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapter;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;

import java.io.*;
import java.util.*;

import static ru.nms.diplom.ircrossfeature.optimization.Utils.computeMRR;
import static ru.nms.diplom.ircrossfeature.optimization.Utils.loadData;
import static ru.nms.diplom.ircrossfeature.util.Constants.FEATURE_STORE_FILE;

public class NelderMeadMRROptimizer {

    // Optimize MRR using Nelder-Mead algorithm
    public static double[] optimizeMRR(List<Map.Entry<Integer, List<Passage>>> data) {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        NelderMeadSimplex simplex = new NelderMeadSimplex(2); // 2 variables: p, w

        // Define objective function (negative MRR to maximize it)
        org.apache.commons.math3.analysis.MultivariateFunction function = point -> -computeMRR(data, point[0], point[1]);

        // Bound parameters between 0.01 and 10.0
        MultivariateFunctionMappingAdapter boundedFunction = new MultivariateFunctionMappingAdapter(
                function, new double[]{0.01, 0.01}, new double[]{10.0, 10.0}
        );

        PointValuePair result = optimizer.optimize(
                MaxEval.unlimited(),
                new org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction(boundedFunction),
                GoalType.MINIMIZE,
                simplex,
                new org.apache.commons.math3.optim.InitialGuess(new double[]{1.0, 1.0})
        );

        double[] optimalParams = boundedFunction.unboundedToBounded(result.getPoint());
        return new double[]{optimalParams[0], optimalParams[1], -result.getValue()}; // Return p, w, and max MRR
    }

    public static void main(String[] args) throws IOException {
        List<Map.Entry<Integer, List<Passage>>> data = loadData(FEATURE_STORE_FILE);

        double[] optimalParams = optimizeMRR(data);
        System.out.printf("Optimal p: %.4f, Optimal w: %.4f, Max MRR: %.6f%n",
                optimalParams[0], optimalParams[1], optimalParams[2]);
    }
}
