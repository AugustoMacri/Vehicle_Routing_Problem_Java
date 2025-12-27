package genetic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import main.App;

public class Crossover {

    public static Individual onePointCrossing(Individual parent1, Individual parent2) {

        // System.out.println("Entrou no onePointCrossing");

        Random random = new Random();

        // Normalize the routes of the parents to have the same size
        int[][] normalizedParent1 = normalizeRoute(parent1.getRoute());
        int[][] normalizedParent2 = normalizeRoute(parent2.getRoute());

        int[][] childRoute = new int[App.numVehicles][App.numClients];

        // Initialize the child route with 0 (not -1, because repairRoute expects 0 for
        // empty positions)
        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = 0; c < App.numClients; c++) {
                childRoute[v][c] = 0;
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

        // Repair child route: ensure all clients (1 to numClients-1) appear exactly
        // once
        childRoute = repairRoute(childRoute, parent1, parent2);

        // Denormalize the child route
        int[][] denormalizedChildRoute = denormalizeRoute(childRoute);
        
        // Final validation: ensure all clients are present after denormalization
        if (!validateRoute(denormalizedChildRoute, App.numClients - 1)) {
            System.err.println("ERRO CRÍTICO: Crossover gerou rota inválida após desnormalização!");
            // Try to repair again using the normalized version
            denormalizedChildRoute = denormalizeRoute(childRoute);
        }

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
            int pos = 0;
            
            // Copy ALL clients (skip zeros and -1), regardless of position
            for (int c = 0; c < App.numClients; c++) {
                if (route[v][c] != 0 && route[v][c] != -1) {
                    denormalizedRoute[v][pos] = route[v][c];
                    pos++;
                }
            }

            // Fill remaining positions with -1
            for (; pos < App.numClients; pos++) {
                denormalizedRoute[v][pos] = -1;
            }
        }

        return denormalizedRoute;
    }

    /**
     * Repairs the child route to ensure all clients appear exactly once.
     * Removes duplicates and adds missing clients.
     */
    private static int[][] repairRoute(int[][] childRoute, Individual parent1, Individual parent2) {
        Random random = new Random();

        // Step 1: Find all clients present in the child route
        boolean[] clientPresent = new boolean[App.numClients + 1]; // +1 to include depot
        clientPresent[0] = true; // depot is always present

        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = 0; c < App.numClients; c++) {
                int client = childRoute[v][c];
                if (client > 0 && client < App.numClients) { // client IDs are 1 to App.numClients-1
                    clientPresent[client] = true;
                }
            }
        }

        // Step 2: Remove duplicates - keep only first occurrence of each client
        boolean[] alreadySeen = new boolean[App.numClients + 1]; // +1 to include depot
        alreadySeen[0] = true; // depot

        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = 0; c < App.numClients; c++) {
                int client = childRoute[v][c];

                if (client > 0 && client < App.numClients) { // client IDs are 1 to App.numClients-1
                    if (alreadySeen[client]) {
                        // Duplicate found - replace with 0
                        childRoute[v][c] = 0;
                    } else {
                        alreadySeen[client] = true;
                    }
                }
            }
        }

        // Step 3: Collect missing clients
        java.util.List<Integer> missingClients = new java.util.ArrayList<>();
        for (int client = 1; client < App.numClients; client++) { // < because clients are 1 to App.numClients-1
            if (!alreadySeen[client]) {
                missingClients.add(client);
            }
        }

        // Step 4: Insert missing clients into the route
        // Try to place them in positions from parents first, then randomly
        for (int missingClient : missingClients) {
            boolean inserted = false;

            // Try to find this client's position in parent1 or parent2
            Individual[] parents = { parent1, parent2 };
            for (Individual parent : parents) {
                if (inserted)
                    break;

                int[][] parentRoute = normalizeRoute(parent.getRoute());

                // Find where this client appears in the parent
                for (int v = 0; v < App.numVehicles && !inserted; v++) {
                    for (int c = 0; c < App.numClients && !inserted; c++) {
                        if (parentRoute[v][c] == missingClient) {
                            // Try to insert at the same position in child
                            if (insertClientAtPosition(childRoute, missingClient, v, c)) {
                                inserted = true;
                            }
                            break;
                        }
                    }
                }
            }

            // If still not inserted, insert at first available position
            if (!inserted) {
                insertClientAnywhere(childRoute, missingClient);
            }
        }

        return childRoute;
    }

    /**
     * Tries to insert a client at a specific position in the route.
     * Returns true if successful, false otherwise.
     */
    private static boolean insertClientAtPosition(int[][] route, int client, int vehicle, int position) {
        // Check if position is available (contains 0 or -1)
        if (route[vehicle][position] == 0 || route[vehicle][position] == -1) {
            route[vehicle][position] = client;
            return true;
        }

        // Try positions around the target position
        for (int offset = 1; offset < App.numClients; offset++) {
            int pos1 = position + offset;
            int pos2 = position - offset;

            if (pos1 < App.numClients && (route[vehicle][pos1] == 0 || route[vehicle][pos1] == -1)) {
                route[vehicle][pos1] = client;
                return true;
            }

            if (pos2 >= 0 && (route[vehicle][pos2] == 0 || route[vehicle][pos2] == -1)) {
                route[vehicle][pos2] = client;
                return true;
            }
        }

        return false;
    }

    /**
     * Inserts a client at the first available position in any vehicle.
     */
    private static void insertClientAnywhere(int[][] route, int client) {
        for (int v = 0; v < App.numVehicles; v++) {
            for (int c = 1; c < App.numClients; c++) { // Start from 1 to preserve depot at position 0
                if (route[v][c] == 0 || route[v][c] == -1) {
                    route[v][c] = client;
                    return;
                }
            }
        }

        // If no space found (shouldn't happen), replace last non-zero/non-minus-one in
        // last vehicle
        for (int c = App.numClients - 1; c >= 1; c--) {
            if (route[App.numVehicles - 1][c] != 0 && route[App.numVehicles - 1][c] != -1) {
                route[App.numVehicles - 1][c] = client;
                return;
            }
        }
    }

    /**
     * Validates that all clients 1 to numClients appear exactly once in the route.
     * Returns true if valid, false otherwise.
     */
    public static boolean validateRoute(int[][] route, int numClients) {
        Set<Integer> foundClients = new HashSet<>();

        for (int v = 0; v < route.length; v++) {
            for (int c = 0; c < route[v].length; c++) {
                int clientId = route[v][c];
                if (clientId > 0) {
                    if (foundClients.contains(clientId)) {
                        System.err.println("ERRO VALIDAÇÃO: Cliente " + clientId + " duplicado!");
                        return false;
                    }
                    foundClients.add(clientId);
                }
            }
        }

        // Check if all clients from 1 to numClients are present
        for (int i = 1; i <= numClients; i++) {
            if (!foundClients.contains(i)) {
                System.err.println("ERRO VALIDAÇÃO: Cliente " + i + " está faltando!");
                return false;
            }
        }

        if (foundClients.size() != numClients) {
            System.err.println(
                    "ERRO VALIDAÇÃO: Esperado " + numClients + " clientes, encontrados " + foundClients.size());
            return false;
        }

        return true;
    }

}
