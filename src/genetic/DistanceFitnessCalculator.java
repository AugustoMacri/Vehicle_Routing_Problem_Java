package genetic;

import java.util.ArrayList;
import java.util.List;

import main.App;
import vrp.Client;

public class DistanceFitnessCalculator implements FitnessCalculator {

    @Override
    public double calculateFitness(Individual individual, List<Client> clients) {
        double totalDistance = 0;
        Client depot = clients.get(0); // Depósito sempre no índice 0

        for (int v = 0; v < App.numVehicles; v++) {
            double vehicleDistance = 0;

            // Collect all clients in this vehicle's route
            List<Integer> routeClients = new ArrayList<>();
            for (int c = 0; c < App.numClients - 1; c++) {
                int clientId = individual.getRoute()[v][c];
                if (clientId == -1)
                    break;
                routeClients.add(clientId);
            }

            // If vehicle has no clients, skip
            if (routeClients.isEmpty()) {
                continue;
            }

            // Calculate distance: depot -> first client
            Client firstClient = clients.get(routeClients.get(0));
            vehicleDistance += calculateDistance(depot, firstClient);

            // Calculate distances between consecutive clients
            for (int i = 0; i < routeClients.size() - 1; i++) {
                Client currentClient = clients.get(routeClients.get(i));
                Client nextClient = clients.get(routeClients.get(i + 1));
                vehicleDistance += calculateDistance(currentClient, nextClient);
            }

            // Calculate distance: last client -> depot
            Client lastClient = clients.get(routeClients.get(routeClients.size() - 1));
            vehicleDistance += calculateDistance(lastClient, depot);

            totalDistance += vehicleDistance;
        }

        // Calculating the total cost of the Individual
        double fitnessDistance = (totalDistance * 1.0);

        return fitnessDistance;

    }

    // Function to calculate the distance between two clients
    private double calculateDistance(Client c1, Client c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }

}
