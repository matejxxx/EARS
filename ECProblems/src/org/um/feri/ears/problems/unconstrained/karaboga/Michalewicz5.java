package org.um.feri.ears.problems.unconstrained.karaboga;

import java.util.Arrays;

import org.um.feri.ears.problems.Problem;

public class Michalewicz5 extends Problem {
	
	public Michalewicz5(int d) {
		dim = d;
		interval = new double[d];
		intervalL = new double[d];
		Arrays.fill(interval, Math.PI);
		Arrays.fill(intervalL, 0);
		name = "Michalewicz5";
		characteristic = "MS";
	}
	
	public double eval(double x[]) {
		double v = 0;
		int m = 10;
		for (int i = 0; i < dim; i++){
			v = v + Math.sin(x[i])*Math.pow(Math.sin((i+1)*x[i]*x[i]/Math.PI), 2*m);
		}
		v = v * (-1);
		return v;
	}

	public double getOptimumEval() {
		return -4.6877;
	}

	@Override
	public boolean isFirstBetter(double[] x, double eval_x, double[] y,
			double eval_y) {
		return (Math.abs(eval_x - getOptimumEval()) < (Math.abs(eval_y - getOptimumEval())));
	}

}
