package genetic;

import java.util.Random;
import main.App;

public class Mutation {

    public static void mutate(Individual individual, double mutationRate) {
        Random random = new Random();

        // Decide if the individual will be mutated
        int mutateIndividual = random.nextInt((int) (1.0 / mutationRate));

        //int mutateIndividual = 0;

        if (mutateIndividual != 0) {
            return;
        }

        System.out.println("Mutating individual: " + individual.getId());

        int[][] route = individual.getRoute();

        for (int v = 0; v < App.numVehicles; v++) {

            java.util.List<Integer> validClients = new java.util.ArrayList<>();
            for (int c = 0; c < App.numClients; c++) {
                if (route[v][c] != 0 && route[v][c] != -1) {
                    validClients.add(c);
                }
            }

            // Only mutate if there are at least 4 clients in the route
            if (validClients.size() < 4)
                continue;

            int point1, point2;

            //System.err.println("Real clients: " + validClients.size());

            do {
                point1 = validClients.get(random.nextInt(validClients.size()));
                point2 = validClients.get(random.nextInt(validClients.size()));
            } while (point1 == point2);

            // Swap the two points in the route
            //System.out.println("Mutating vehicle " + v + ": swapping " + route[v][point1] + " and " + route[v][point2]);

            int temp = route[v][point1];
            route[v][point1] = route[v][point2];
            route[v][point2] = temp;
        }

        individual.setRoute(route);
    }
}