package org.um.feri.ears.algorithms.moo.nsga2;

import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.EnumAlgorithmParameters;
import org.um.feri.ears.operators.BinaryTournament2;
import org.um.feri.ears.operators.PolynomialMutation;
import org.um.feri.ears.operators.SBXCrossover;
import org.um.feri.ears.problems.Individual;
import org.um.feri.ears.problems.StopCriteriaException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.moo.MOIndividual;
import org.um.feri.ears.problems.moo.MOParetoIndividual;
import org.um.feri.ears.util.CrowdingComparator;
import org.um.feri.ears.util.Distance;
import org.um.feri.ears.util.Ranking;



public class NSGAII extends Algorithm {

	int populationSize = 100;
	Task task;
	int num_var;
	int num_obj;

	MOParetoIndividual population;
	MOParetoIndividual offspringPopulation;
	MOParetoIndividual union;

	public NSGAII() {
		this(100);
	}

	public NSGAII(int populationSize) {
		this.populationSize = populationSize;

		au = new Author("miha", "miha.ravber at gamil.com");
		ai = new AlgorithmInfo(
				"NSGAII",
				"\\bibitem{Deb2002}\nK.~Deb, S.~Agrawal, A.~Pratap, T.~Meyarivan\n\\newblock A fast and elitist multiobjective genetic algorithm: {NSGA-II}.\n\\newblock \\emph{IEEE Transactions on Evolutionary Computation}, 6(2):182--197, 2002.\n",
				"NSGAII", "Nondominated Sorting Genetic Algorithm II ");
		ai.addParameter(EnumAlgorithmParameters.POP_SIZE, populationSize + "");
	}

	@Override
	public Individual run(Task taskProblem) throws StopCriteriaException {
		task = taskProblem;
		num_var = task.getDimensions();
		num_obj = task.getNumberOfObjectives();

		init();
		start();

		// Return the first non-dominated front
		Ranking ranking = new Ranking(population);
		MOParetoIndividual best = ranking.getSubfront(0);
		best.setFileName(task.getProblemFileName());

		double IGD_value = best.getEval();
		System.out.println("IGD value: " + IGD_value);
		best.printFeasibleFUN("FUN_NSGAII");
		best.printVariablesToFile("VAR");
		best.printObjectivesToFile("FUN");

		return best;
	}

	public void start() throws StopCriteriaException {
		Distance distance = new Distance();
		BinaryTournament2 bt2 = new BinaryTournament2();
		SBXCrossover sbx = new SBXCrossover(0.9, 20.0);
		PolynomialMutation plm = new PolynomialMutation(1.0 / num_var, 20.0);

		// Create the initial population
		MOIndividual newSolution;
		for (int i = 0; i < populationSize; i++) {
			if (task.isStopCriteria())
				return;
			newSolution = new MOIndividual(task.getRandomMOIndividual());
			// problem.evaluateConstraints(newSolution);
			population.add(newSolution);
		}

		// Generations
		while (!task.isStopCriteria()) {
			// Create the offSpring solutionSet
			offspringPopulation = new MOParetoIndividual(populationSize);
			MOIndividual[] parents = new MOIndividual[2];

			for (int i = 0; i < (populationSize / 2); i++) {
				if (!task.isStopCriteria()) {
					// obtain parents
					parents[0] = (MOIndividual) bt2.execute(population);
					parents[1] = (MOIndividual) bt2.execute(population);
					MOIndividual[] offSpring = (MOIndividual[]) sbx.execute(parents, task);
					plm.execute(offSpring[0], task);
					plm.execute(offSpring[1], task);
					if (task.isStopCriteria())
						break;
					task.eval(offSpring[0]);
					offspringPopulation.add(offSpring[0]);
					// problem.evaluateConstraints(offSpring[0]);
					if (task.isStopCriteria())
						break;
					task.eval(offSpring[1]);
					// problem.evaluateConstraints(offSpring[1]);
					offspringPopulation.add(offSpring[1]);
				}
			}

			// Create the solutionSet union of solutionSet and offSpring
			union = population.union(offspringPopulation);

			// Ranking the union
			Ranking ranking = new Ranking(union);

			int remain = populationSize;
			int index = 0;
			MOParetoIndividual front = null;
			population.clear();

			// Obtain the next front
			front = ranking.getSubfront(index);

			while ((remain > 0) && (remain >= front.size())) {
				// Assign crowding distance to individuals
				distance.crowdingDistanceAssignment(front, num_obj);
				// Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					population.add(front.get(k));
				}

				// Decrement remain
				remain = remain - front.size();

				// Obtain the next front
				index++;
				if (remain > 0) {
					front = ranking.getSubfront(index);
				}
			}

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) { // front contains individuals to insert
				distance.crowdingDistanceAssignment(front, num_obj);
				front.sort(new CrowdingComparator());
				for (int k = 0; k < remain; k++) {
					population.add(front.get(k));
				}
				remain = 0;
			}
		}
	}

	private void init() {
		population = new MOParetoIndividual(populationSize);
	}

	@Override
	public void resetDefaultsBeforNewRun() {

	}
}