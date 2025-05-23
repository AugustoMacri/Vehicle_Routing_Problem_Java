package genetic;

import java.util.List;

import main.App;
import vrp.Client;

public class DefaultFitnessCalculator implements FitnessCalculator {

    @Override
    public double calculateFitness(Individual individual, List<Client> clients) {
        double totalDistance = 0;
        double totalTime = 0;
        double totalFuel = 0;
        int numViolations = 0;

        for (int v = 0; v < App.numVehicles; v++) {
            double currentTime = 0;
            double vehicleDistance = 0;
            int numViolationsVehicle = 0;

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

                // Calculating the time
                currentTime += (distance / App.VEHICLE_SPEED) * 60; // Convert to minutes

                // Check if the vehicle arrives between the ready time and due time and if the
                // service time plus current time is less than the due time (respect the due
                // time)
                if (currentTime < currentClient.getReadyTime() || currentTime > currentClient.getDueTime()) {

                    System.out.println("Vehicle " + v + " | Client Id " + currentClientId + " | Current time: " + currentTime
                            + " | Ready time: " + currentClient.getReadyTime() + " | Due time: "
                            + currentClient.getDueTime());

                    numViolations++;
                    numViolationsVehicle++;
                }

                currentTime += currentClient.getServiceTime(); // Add service time
            }

            // Calculating the fuel cost
            double fuelCost = fuelCost(vehicleDistance);

            // Adding the total distance, time and fuel
            totalDistance += vehicleDistance;
            totalTime += currentTime;
            totalFuel += fuelCost;

            // Debugging
            System.out.printf(
                    "Vehicle %d | Distance: %.2f | Time: %.2f | Fuel: %.2f | ViolationsVehicle: %d | Violations: %d%n",
                    v, vehicleDistance, currentTime, fuelCost(vehicleDistance), numViolationsVehicle, numViolations);

        }

        // Calculating the total cost of the Individual
        double totalCost = (totalDistance * 1.0) + (totalTime * 0.5) + (totalFuel * 0.75);

        // Calculating the final fitness
        double fitness = (App.numVehicles * App.WEIGHT_NUM_VEHICLES) + (numViolations * App.WEIGHT_NUM_VIOLATIONS)
                + totalCost;

        return fitness;
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
