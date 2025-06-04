package genetic;

import java.util.List;

import main.App;
import vrp.Client;

public class DistanceFitnessCalculator implements FitnessCalculator {

    @Override
    public double calculateFitness(Individual individual, List<Client> clients) {
        double totalDistance = 0;

        for (int v = 0; v < App.numVehicles; v++) {
            double vehicleDistance = 0;

            for (int c = 0; c < App.numClients - 1; c++) {
                int currentClientId = individual.getRoute()[v][c];
                int nextClientId = individual.getRoute()[v][c + 1];

                if (currentClientId == -1 || nextClientId == -1)
                    break;

                // Get the current client and next client
                Client currentClient = clients.get(currentClientId);
                Client nextClient = clients.get(nextClientId);

                // Calculating the distance between the current client and the next client
                double distance = calculateDistance(currentClient, nextClient);
                vehicleDistance += distance;

            }

            // Adding the total distance, time and fuel
            totalDistance += vehicleDistance;

            // Debugging
            //System.out.printf("Vehicle %d | Distance: %.2f%n",v, vehicleDistance);

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
