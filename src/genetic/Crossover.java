package genetic;

import java.util.Random;

import main.App;

public class Crossover {

    public static Individual onePointCrossing(Individual parent1, Individual parent2) {

        //System.out.println("Entrou no onePointCrossing");

        Random random = new Random();

        // Normalize the routes of the parents to have the same size
        int[][] normalizedParent1 = normalizeRoute(parent1.getRoute());
        int[][] normalizedParent2 = normalizeRoute(parent2.getRoute());

        int[][] childRoute = new int[App.numVehicles][App.numClients];

        // Initialize the child route with -1
        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = 0; c < App.numClients; c++) {
                childRoute[v][c] = -1;
            }
        }

        // Generate a random crossover point
        int cut;
        do {
            cut = random.nextInt(App.numClients) + 1;

        } while (cut == App.vehicleCapacity + 1);

        // Copy the first part of the route from parent1
        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = 0; c < cut; c++) {
                childRoute[v][c] = normalizedParent1[v][c];
            }
        }

        // Copy the second part of the route from parent2
        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = cut; c < App.numClients; c++) {
                childRoute[v][c] = normalizedParent2[v][c];
            }
        }

        // Verify if the child route has a client repeated (except 0)
        int[] vetDadsChosen = new int[App.numVehicles];

        for (int v = 0; v < App.numVehicles; v++) {
            int dadChosen;

            // Random selection of the parent for comparation
            do {
                dadChosen = random.nextInt(2);
            } while (v > 0 && dadChosen == vetDadsChosen[v - 1]);

            vetDadsChosen[v] = dadChosen;

            for (int c = 0; c < App.numClients; c++) {

                int val1 = childRoute[v][c];

                for (int k = c + 1; k < App.numClients; k++) {
                    int val2 = childRoute[v][k];
                    if (val1 == val2 && val1 != 0 && val2 != 0) {
                        int substituto = compareFatherSon(parent1, parent2, childRoute, v, dadChosen);

                        childRoute[v][k] = substituto;
                    }
                }

            }
        }

        // Denormalize the child route
        int[][] denormalizedChildRoute = denormalizeRoute(childRoute);

        // Usar ID único e incrementar o contador
        Individual newSon = new Individual(App.nextIndividualId++, 0, 0, 0, 0);
        newSon.setRoute(denormalizedChildRoute);

        return newSon;
    }

    // Normalize the route to have the same size
    private static int[][] normalizeRoute(int[][] route) {
        int[][] normalizedRoute = new int[App.numVehicles][App.numClients];

        for (int v = 0; v < App.numVehicles; v++) {
            int c;

            for (c = 0; c < App.numClients; c++) {
                if (route[v][c] == 0 && c > 0) {
                    break;
                }
                normalizedRoute[v][c] = route[v][c];
            }

            for (; c < App.numClients; c++) {
                normalizedRoute[v][c] = 0;
            }
        }

        return normalizedRoute;
    }

    // Denormalize the route of the child to have the same size as the others
    private static int[][] denormalizeRoute(int[][] route) {
        int[][] denormalizedRoute = new int[App.numVehicles][App.numClients];

        for (int v = 0; v < App.numVehicles; v++) {
            boolean foundZero = false;

            for (int c = 0; c < App.numClients; c++) {
                if (foundZero) {
                    denormalizedRoute[v][c] = -1;
                } else {
                    denormalizedRoute[v][c] = route[v][c];
                    if (route[v][c] == 0 && c > 0) {
                        foundZero = true;
                    }
                }
            }
        }

        return denormalizedRoute;
    }

    // Verify if there is one individual that isnt on the route of the child
    public static int compareFatherSon(Individual parent1, Individual parent2, int[][] childRoute, int vehicleIndex,
            int dadChosen) {
        Individual chosenParent = (dadChosen == 0) ? parent1 : parent2;

        //System.out.println("Entrou no compareFatherSon");

        for (int c = 1; c < App.numClients; c++) {
            int val1 = chosenParent.getRoute()[vehicleIndex][c];
            boolean found = false;

            for (int k = 1; k < App.numClients; k++) {
                int val2 = childRoute[vehicleIndex][k];
                if (val1 == val2 && val1 != 0 && val2 != 0) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return val1;
            }

        }

        //System.out.println("saiu do compareFatherSon");
        return 0;
    }
}
