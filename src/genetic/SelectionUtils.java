package genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import main.App;

public class SelectionUtils {

    private static final Random random = new Random();

    public static void elitism(List<Individual> population, List<Individual> nextPopulation, double elitismRate) {
        int numElites = (int) Math.ceil(population.size() * App.elitismRate);

        // Sorts the population by fitness (lower fitness = better)
        List<Individual> sortedPopulation = new ArrayList<>(population);
        sortedPopulation.sort(Comparator.comparingDouble(Individual::getFitness));

        for (int i = 0; i < numElites; i++) {
            nextPopulation.add(sortedPopulation.get(i).deepCopy());
        }
    }

    public static List<Individual> tournamentSelection(List<Individual> subPop, int fitnessType) {
        List<Individual> parents = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Individual parent = null;

            while (parent == null) {

                List<Individual> tournament = new ArrayList<>();

                // Select two random individual from the subpopulation to participate in the
                // tournament
                for (int j = 0; j < App.QUANTITYSELECTEDTOURNAMENT; j++) {
                    int randomIndex = random.nextInt(subPop.size());
                    tournament.add(subPop.get(randomIndex));
                }

                Individual winner = Collections.min(tournament, (ind1, ind2) -> {
                    switch (fitnessType) {
                        case 0:
                            return Double.compare(ind1.getFitnessDistance(), ind2.getFitnessDistance());
                        case 1:
                            return Double.compare(ind1.getFitnessTime(), ind2.getFitnessTime());
                        case 2:
                            return Double.compare(ind1.getFitnessFuel(), ind2.getFitnessFuel());
                        default:
                            throw new IllegalArgumentException("Invalid fitness type");
                    }
                });

                // Verify if the individual is already in the parents list
                if (!parents.contains(winner)) {
                    parent = winner;
                }
            }

            parents.add(parent);
        }

        return parents;
    }

    // Isso aqui serve para alguma coisa do gênero tipo, seleciona qualquer dois
    // indivíduos para cruzamento
    // mas se escolher a mesma subpopulação duas vezes, o mesmo pai não podera ser,
    // ou algo assim

    /*
     * Se não me engano é alguma cosia do tipo, pode cruzar dois indivíduos de duas
     * subpopulações diferentes
     * Mas ai quando seleciona as duas populações iguais, ele salva o id para evitar
     * que o mesmo indivíduo seja escolhido duas vezes
     */
    public static List<Individual> subPopSelection(Population population, int numSelections) {
        Random rand = new Random();
        int index1 = rand.nextInt(App.sub_pop_size); //Analizar essa parte do sub_pop_size
        int index2 = rand.nextInt(App.sub_pop_size);

        int fitnessType1 = index1;
        int fitnessType2 = index2;

        Set<Integer> previousWinners = new HashSet<>();
        List<Individual> selectedParents = new ArrayList<>();

        Individual parent1 = tournamentSelection(subpopulations.get(index1), App.tournamentSize, previousWinners,
                fitnessType1);
        if (parent1 != null)
            previousWinners.add(parent1.getId());
        selectedParents.add(parent1);

        Individual parent2 = tournamentSelection(subpopulations.get(index2), App.tournamentSize, previousWinners,
                fitnessType2);
        if (parent2 != null)
            previousWinners.add(parent2.getId());
        selectedParents.add(parent2);

        return selectedParents;
    }
}