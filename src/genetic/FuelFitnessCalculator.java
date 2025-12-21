package genetic;

import java.util.List;

import main.App;
import vrp.Client;

public class FuelFitnessCalculator implements FitnessCalculator {

    @Override
    public double calculateFitness(Individual individual, List<Client> clients) {
        double totalFuel = 0;
        Client depot = clients.get(0); // Depósito sempre no índice 0

        for (int v = 0; v < App.numVehicles; v++) {
            double vehicleDistance = 0;
            Client firstClient = null;
            Client lastClient = null;

            for (int c = 0; c < App.numClients - 1; c++) {
                int currentClientId = individual.getRoute()[v][c];
                int nextClientId = individual.getRoute()[v][c + 1];

                if (currentClientId == -1 || nextClientId == -1)
                    break;

                // Get the current client and next client
                Client currentClient = clients.get(currentClientId);
                Client nextClient = clients.get(nextClientId);

                // Armazena primeiro e último cliente
                if (firstClient == null) {
                    firstClient = currentClient;
                }
                lastClient = nextClient;

                // Calculating the distance between the current client and the next client
                double distance = calculateDistance(currentClient, nextClient);
                vehicleDistance += distance;

            }

            // Adiciona distâncias do depósito: depósito → primeiro cliente e último cliente
            // → depósito
            if (firstClient != null) {
                vehicleDistance += calculateDistance(depot, firstClient);
                vehicleDistance += calculateDistance(lastClient, depot);
            }

            // Calculating the fuel cost
            double fuelCost = fuelCost(vehicleDistance);

            // Adding the total distance, time and fuel
            totalFuel += fuelCost;

            // Debugging
            // System.out.printf("Vehicle %d | Distance: %.2f | Fuel: %.2f%n",v,
            // vehicleDistance, fuelCost(vehicleDistance));

        }

        // Calculating the total cost of the Individual
        double fitnessFuel = totalFuel * 0.75;

        return fitnessFuel;
    }

    // Function to calculate the distance between two clients
    private double calculateDistance(Client c1, Client c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }

    // Function to calculate the fuel cost
    private double fuelCost(double distance) {
        double gasolineCost = Math.round(distance / App.G_FUEL_CONSUMPTION) * App.G_FUEL_PRICE;
        double ethanolCost = Math.round(distance / App.E_FUEL_CONSUMPTION) * App.E_FUEL_PRICE;
        double dieselCost = Math.round(distance / App.D_FUEL_CONSUMPTION) * App.D_FUEL_PRICE;

        return Math.min(gasolineCost, Math.min(ethanolCost, dieselCost));
    }

}
