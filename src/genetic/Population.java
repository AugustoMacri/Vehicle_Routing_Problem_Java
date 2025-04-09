package genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import main.App;
import vrp.Client;

public class Population {
    private List<Individual> individuals;
    private List<Individual> subPopDistance;
    private List<Individual> subPopTime;
    private List<Individual> subPopFuel;
    private List<Individual> subPopPonderation;

    public Population(List<Individual> individuals) {
        this.individuals = individuals;
    }

    public void initializePopulation(List<Client> clients) {

        // Condition to see if the list of clients isn't null
        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("Client list cannot be empty");
        } else {
            System.out.println("Passed\n");
        }

        // Copying the clients array
        List<Client> clientsCopy = new ArrayList<>(clients);

        for (int h = 0; h < App.pop_size; h++) {
            Individual individual = new Individual(h, 0, 0, 0, 0);
            boolean[] visited = new boolean[App.numClients];

            // "Cleaning" the array
            Arrays.fill(visited, false);

            visited[0] = true; // Distribution center is the starting point

            // Initializing the population empty (-1)
            Client distributionCenter = clientsCopy.get(0);

            // Calculating the distance beetween the distributioncenter and the clients
            for (Client client : clientsCopy) {

                if (client.getId() == 0)
                    continue; // Skip the distribution center

                double distance = calculateDistance(client, distributionCenter);
                client.setDistanceFromDepot(distance);
            }

            // Sorting the clients list
            clientsCopy.sort(Comparator.comparingDouble(Client::getDistanceFromDepot));

            int clientIndex = 1;

            for (int v = 0; v < App.numVehicles; v++) {
                int capacity = 0;
                int pos = 0;
                int currentClient = 0;

                // Adding the distribution center as the first client in the route
                individual.setClientInRoute(v, pos, 0);
                pos++;

                while (clientIndex < App.numClients) {
                    int nextClient = findClosestClient(currentClient, clientsCopy, visited);

                    if (nextClient == -1)
                        break; // To this work, we need to have all routes -1

                    Client client = clients.get(nextClient);
                    int demand = client.getDemand();

                    if (capacity + demand > App.vehicleCapacity)
                        break;

                    individual.setClientInRoute(v, pos, nextClient);
                    visited[nextClient] = true;
                    capacity += demand;
                    currentClient = nextClient;
                    pos++;
                }

                individual.setClientInRoute(v, pos, 0); // Return to depot
            }

            individuals.add(individual);
        }

    }

    public void distributeSubpopulations() {

        subPopDistance = new ArrayList<>(App.SUBPOP_SIZE);
        subPopTime = new ArrayList<>(App.SUBPOP_SIZE);
        subPopFuel = new ArrayList<>(App.SUBPOP_SIZE);
        subPopPonderation = new ArrayList<>(App.SUBPOP_SIZE);

        // Fill the subpopulation lists with empty individuals to avoid null pointer exceptions
        for (int i = 0; i < App.SUBPOP_SIZE; i++) {
            subPopDistance.add(new Individual(-1, 0, 0, 0, 0));
            subPopTime.add(new Individual(-1, 0, 0, 0, 0));
            subPopFuel.add(new Individual(-1, 0, 0, 0, 0));
            subPopPonderation.add(new Individual(-1, 0, 0, 0, 0));
        }

        // Distribute the population initialized in subpopulations
        for (int i = 0; i < App.pop_size; i++) {
            int index = i / App.SUBPOP_SIZE; // Determines the subpopulation (0, 1 or 2)
            int index2 = i % App.SUBPOP_SIZE; // Determines the position in the subpopulation

            Individual source = individuals.get(i); // Indivíduo da população principal

            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    switch (index) {
                        case 0:
                            subPopDistance.get(index2).setClientInRoute(j, k, source.getRoute()[j][k]);
                            subPopDistance.get(index2).setId(source.getId());
                            break;

                        case 1:
                            subPopTime.get(index2).setClientInRoute(j, k, source.getRoute()[j][k]);
                            subPopTime.get(index2).setId(source.getId());
                            break;

                        case 2:
                            subPopFuel.get(index2).setClientInRoute(j, k, source.getRoute()[j][k]);
                            subPopFuel.get(index2).setId(source.getId());
                            break;

                        default:
                            break;
                    }

                    // Putting the individual in the ponderation subpopulation
                    subPopPonderation.get(index2).setClientInRoute(j, k, source.getRoute()[j][k]);
                    subPopPonderation.get(index2).setId(source.getId());
                }
            }
        }
    }

    // Function to calculate the distance beetwen two points
    private double calculateDistance(Client c1, Client c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }

    // Function to locate the closest client from the current client
    private int findClosestClient(int currentClient, List<Client> clients, boolean[] visited) {
        double minDistance = Double.MAX_VALUE;
        int closestClient = -1;

        for (Client client : clients) {
            if (!visited[client.getId()]) {
                double distance = calculateDistance(clients.get(currentClient), client);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestClient = client.getId();
                }
            }
        }

        return closestClient;
    }

    // Function for printing the individual of the subpopulation (just testing)
    public void printSubPopulations() {
        System.out.println("SubPop Distance:");
        printSubPop(subPopDistance);

        System.out.println("SubPop Time:");
        printSubPop(subPopTime);

        System.out.println("SubPop Fuel:");
        printSubPop(subPopFuel);

        System.out.println("SubPop Ponderation:");
        printSubPop(subPopPonderation);
    }

    private void printSubPop(List<Individual> subPop) {
        for (Individual ind : subPop) {
            System.out.print("Individual " + ind.getId() + ": \n");
            ind.printRoutes();
        }
    }
}
