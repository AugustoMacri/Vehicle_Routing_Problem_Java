package genetic;

import java.util.List;

import main.App;
import vrp.Client;

public class TimeFitnessCalculator implements FitnessCalculator {

    @Override
    public double calculateFitness(Individual individual, List<Client> clients) {
        double totalTime = 0;
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
                currentTime += ((distance / App.VEHICLE_SPEED) * 60); // Convert to minutes

                // Check if the vehicle arrives between the ready time and due time and if the
                // service time plus current time is less than the due time (respect the due
                // time)
                if (currentTime < currentClient.getReadyTime() || currentTime > currentClient.getDueTime()
                        || (currentTime + currentClient.getServiceTime()) > currentClient.getDueTime()) {
                    numViolations++;
                    numViolationsVehicle++;
                }

                currentTime += currentClient.getServiceTime(); // Add service time
            }

            // Adding the time
            totalTime = currentTime;

            // Debugging
            System.out.printf(
                    "Vehicle %d | Time: %.2f | ViolationsVehicle: %d | Violations: %d | SPEED: %d | Distance: %.2f%n",
                    v, currentTime, numViolationsVehicle, numViolations, App.VEHICLE_SPEED, vehicleDistance);


        }

        // Calculating the total cost of the Individual
        double fitnessTime = (numViolations * App.WEIGHT_NUM_VIOLATIONS) + (totalTime * 0.5);

        return fitnessTime;

    }

    // Function to calculate the distance between two clients
    private double calculateDistance(Client c1, Client c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }
    
}
