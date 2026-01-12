package genetic;

import java.util.*;
import main.App;
import vrp.Client;

/**
 * Solomon I1 Insertion Heuristic for VRPTW
 * Creates feasible initial solutions that respect time windows
 */
public class SolomonInsertion {

    private static final double ALPHA = 1.0; // Weight for distance
    private static final double BETA = 1.0; // Weight for time
    private static final double GAMMA = 1.0; // Weight for time window urgency

    /**
     * Creates a single individual using Solomon I1 insertion heuristic
     * 
     * @param clients         List of all clients (including depot at index 0)
     * @param vehicleCapacity Maximum vehicle capacity
     * @param numVehicles     Maximum number of vehicles available
     * @return Individual with feasible routes
     */
    public static Individual createIndividual(List<Client> clients, int vehicleCapacity, int numVehicles) {
        Individual individual = new Individual(App.nextIndividualId++, 0, 0, 0, 0);
        int[][] routes = new int[numVehicles][App.numClients];

        // Initialize all positions with -1
        for (int v = 0; v < numVehicles; v++) {
            Arrays.fill(routes[v], -1);
        }

        Set<Integer> unrouted = new HashSet<>();
        for (int i = 1; i < clients.size(); i++) {
            unrouted.add(i);
        }

        Client depot = clients.get(0);
        int currentVehicle = 0;
        int[] routeSize = new int[numVehicles];
        double[] routeLoad = new double[numVehicles];
        double[] routeTime = new double[numVehicles];

        // Build routes until all clients are routed or vehicles exhausted
        while (!unrouted.isEmpty() && currentVehicle < numVehicles) {

            // Find seed customer (farthest from depot with earliest due time)
            int seedClient = findSeedCustomer(unrouted, clients, depot);

            if (seedClient == -1)
                break;

            // Start new route with seed
            routes[currentVehicle][0] = seedClient;
            routeSize[currentVehicle] = 1;
            routeLoad[currentVehicle] = clients.get(seedClient).getDemand();

            // Calculate time to reach seed
            double distToSeed = distance(depot, clients.get(seedClient));
            double arrivalTime = (distToSeed / App.VEHICLE_SPEED) * 60;
            routeTime[currentVehicle] = Math.max(arrivalTime, clients.get(seedClient).getReadyTime())
                    + clients.get(seedClient).getServiceTime();

            unrouted.remove(seedClient);

            // Keep inserting customers into this route
            boolean inserted = true;
            while (inserted && !unrouted.isEmpty()) {
                inserted = false;

                BestInsertion best = findBestInsertion(
                        routes[currentVehicle],
                        routeSize[currentVehicle],
                        routeLoad[currentVehicle],
                        routeTime[currentVehicle],
                        unrouted,
                        clients,
                        depot,
                        vehicleCapacity);

                if (best != null) {
                    // Insert customer at best position
                    insertCustomer(routes[currentVehicle], routeSize[currentVehicle], best.position, best.customer);
                    routeSize[currentVehicle]++;
                    routeLoad[currentVehicle] += clients.get(best.customer).getDemand();
                    routeTime[currentVehicle] = best.newTime;
                    unrouted.remove(best.customer);
                    inserted = true;
                }
            }

            currentVehicle++;
        }

        // Handle remaining unrouted customers (shouldn't happen if enough vehicles)
        if (!unrouted.isEmpty() && currentVehicle < numVehicles) {
            // Force insert remaining customers
            for (int client : unrouted) {
                if (currentVehicle >= numVehicles)
                    break;
                routes[currentVehicle][0] = client;
                routeSize[currentVehicle] = 1;
                currentVehicle++;
            }
        }

        individual.setRoute(routes);
        return individual;
    }

    /**
     * Find seed customer: farthest from depot with earliest due time
     */
    private static int findSeedCustomer(Set<Integer> unrouted, List<Client> clients, Client depot) {
        int bestClient = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int clientId : unrouted) {
            Client client = clients.get(clientId);
            double dist = distance(depot, client);
            double urgency = 1.0 / (client.getDueTime() + 1); // Earlier due time = higher urgency
            double score = dist * urgency;

            if (score > bestScore) {
                bestScore = score;
                bestClient = clientId;
            }
        }

        return bestClient;
    }

    /**
     * Find best insertion position for any unrouted customer
     */
    private static BestInsertion findBestInsertion(
            int[] route,
            int routeSize,
            double currentLoad,
            double currentTime,
            Set<Integer> unrouted,
            List<Client> clients,
            Client depot,
            int vehicleCapacity) {

        BestInsertion best = null;
        double bestCost = Double.POSITIVE_INFINITY;

        for (int customer : unrouted) {
            Client client = clients.get(customer);

            // Check capacity
            if (currentLoad + client.getDemand() > vehicleCapacity) {
                continue;
            }

            // Try inserting at each position in the route
            for (int pos = 0; pos <= routeSize; pos++) {
                InsertionCost cost = calculateInsertionCost(
                        route, routeSize, pos, customer, clients, depot);

                if (cost != null && cost.totalCost < bestCost) {
                    bestCost = cost.totalCost;
                    best = new BestInsertion(customer, pos, cost.newTime);
                }
            }
        }

        return best;
    }

    /**
     * Calculate cost of inserting customer at specific position
     */
    private static InsertionCost calculateInsertionCost(
            int[] route,
            int routeSize,
            int position,
            int customer,
            List<Client> clients,
            Client depot) {

        Client newClient = clients.get(customer);
        double currentTime = 0;
        double distanceIncrease = 0;
        double timeViolation = 0;

        // Calculate time up to insertion point
        Client prevClient = depot;
        for (int i = 0; i < position; i++) {
            Client curr = clients.get(route[i]);
            double dist = distance(prevClient, curr);
            currentTime += (dist / App.VEHICLE_SPEED) * 60;

            if (currentTime < curr.getReadyTime()) {
                currentTime = curr.getReadyTime();
            }
            if (currentTime > curr.getDueTime()) {
                timeViolation += (currentTime - curr.getDueTime());
            }

            currentTime += curr.getServiceTime();
            prevClient = curr;
        }

        // Calculate insertion cost
        Client nextClient = (position < routeSize) ? clients.get(route[position]) : depot;

        double distPrevToNew = distance(prevClient, newClient);
        double distNewToNext = distance(newClient, nextClient);
        double distPrevToNext = distance(prevClient, nextClient);

        distanceIncrease = distPrevToNew + distNewToNext - distPrevToNext;

        // Time at new customer
        double arrivalAtNew = currentTime + (distPrevToNew / App.VEHICLE_SPEED) * 60;
        double startServiceAtNew = Math.max(arrivalAtNew, newClient.getReadyTime());

        // Check if new customer violates time window
        if (arrivalAtNew > newClient.getDueTime()) {
            timeViolation += (arrivalAtNew - newClient.getDueTime()) * 10000; // Heavy penalty
        }

        double timeAfterNew = startServiceAtNew + newClient.getServiceTime();

        // Check impact on remaining customers
        double timeShift = timeAfterNew - currentTime;
        for (int i = position; i < routeSize; i++) {
            Client curr = clients.get(route[i]);
            double dist = (i == position) ? distNewToNext : distance(clients.get(route[i - 1]), curr);
            timeAfterNew += (dist / App.VEHICLE_SPEED) * 60;

            if (timeAfterNew < curr.getReadyTime()) {
                timeAfterNew = curr.getReadyTime();
            }
            if (timeAfterNew > curr.getDueTime()) {
                timeViolation += (timeAfterNew - curr.getDueTime()) * 10000; // Heavy penalty
            }

            timeAfterNew += curr.getServiceTime();
        }

        // Return to depot
        Client lastClient = (routeSize > 0) ? clients.get(route[routeSize - 1]) : newClient;
        double returnTime = timeAfterNew + (distance(lastClient, depot) / App.VEHICLE_SPEED) * 60;

        if (returnTime > depot.getDueTime()) {
            timeViolation += (returnTime - depot.getDueTime()) * 100; // Penalty for late return
        }

        // Total cost considers distance increase and time violations
        double totalCost = ALPHA * distanceIncrease + BETA * timeViolation;

        if (timeViolation > 0) {
            return null; // Reject insertions that violate time windows
        }

        return new InsertionCost(totalCost, timeAfterNew);
    }

    /**
     * Insert customer at specified position in route
     */
    private static void insertCustomer(int[] route, int routeSize, int position, int customer) {
        // Shift customers to the right
        for (int i = routeSize; i > position; i--) {
            route[i] = route[i - 1];
        }
        route[position] = customer;
    }

    /**
     * Calculate Euclidean distance between two clients
     */
    private static double distance(Client c1, Client c2) {
        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Helper classes
    static class BestInsertion {
        int customer;
        int position;
        double newTime;

        BestInsertion(int customer, int position, double newTime) {
            this.customer = customer;
            this.position = position;
            this.newTime = newTime;
        }
    }

    static class InsertionCost {
        double totalCost;
        double newTime;

        InsertionCost(double totalCost, double newTime) {
            this.totalCost = totalCost;
            this.newTime = newTime;
        }
    }
}
