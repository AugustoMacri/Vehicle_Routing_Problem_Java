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

        subPopDistance = new ArrayList<>(App.sub_pop_size);
        subPopTime = new ArrayList<>(App.sub_pop_size);
        subPopFuel = new ArrayList<>(App.sub_pop_size);
        subPopPonderation = new ArrayList<>(App.sub_pop_size);

        // Fill the subpopulation lists with empty individuals to avoid null pointer
        // exceptions
        for (int i = 0; i < App.sub_pop_size; i++) {
            subPopDistance.add(new Individual(-1, 0, 0, 0, 0));
            subPopTime.add(new Individual(-1, 0, 0, 0, 0));
            subPopFuel.add(new Individual(-1, 0, 0, 0, 0));
            subPopPonderation.add(new Individual(-1, 0, 0, 0, 0));
        }

        // Distribute the population initialized in subpopulations
        for (int i = 0; i < App.pop_size; i++) {
            int index = i / App.sub_pop_size; // Determines the subpopulation (0, 1 or 2)
            int index2 = i % App.sub_pop_size; // Determines the position in the subpopulation

            Individual source = individuals.get(i); // Take the individual from position i in the list and copy it

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
                }
            }

            // Will copy just the necessary individuals to the ponderation (before was
            // overwriting every time)
            if (i < App.sub_pop_size) {
                for (int j = 0; j < App.numVehicles; j++) {
                    for (int k = 0; k < App.numClients; k++) {
                        subPopPonderation.get(index2).setClientInRoute(j, k, source.getRoute()[j][k]);
                    }
                }
                subPopPonderation.get(index2).setId(source.getId());
            }
        }
    }

    // Function to calculate the distance beetwen two points
    private double calculateDistance(Client c1, Client c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }

    // Function to update the current subpopulation to the next subpopulation
    public void updateSubPop(List<Individual> subPop, List<Individual> nextSubPop) {
        int size = Math.min(subPop.size(), nextSubPop.size());

        for (int i = 0; i < size; i++) {
            Individual src = nextSubPop.get(i);
            Individual dest = subPop.get(i);

            dest.setId(src.getId());
            dest.setFitnessDistance(src.getFitnessDistance());
            dest.setFitnessTime(src.getFitnessTime());
            dest.setFitnessFuel(src.getFitnessFuel());
            dest.setFitness(src.getFitness());

            int[][] srcRoute = src.getRoute();
            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    dest.setClientInRoute(j, k, srcRoute[j][k]);
                }
            }

        }

        // Reset all individuals from nextSubPop
        for (int i = 0; i < nextSubPop.size(); i++) {
            Individual ind = nextSubPop.get(i);
            ind.setId(-1);
            ind.setFitnessDistance(0);
            ind.setFitnessTime(0);
            ind.setFitnessFuel(0);
            ind.setFitness(0);

            int[][] route = ind.getRoute();
            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    ind.setClientInRoute(j, k, 0);
                }
            }
        }
    }

    // Function to compare the son that is generated in the crossing with every
    // subpopulation
    public static void compareSonSubPop(
            Individual newSon,
            List<Individual> subPop,
            List<Individual> nextPop,
            int fitnessType,
            int individualIndex) {
        Individual sub = subPop.get(individualIndex);
        Individual next = nextPop.get(individualIndex);

        boolean replaced = false;

        switch (fitnessType) {
            case 0:
                if (newSon.getFitnessDistance() < sub.getFitnessDistance()) {
                    replaced = true;
                }
                break;
            case 1:
                if (newSon.getFitnessTime() < sub.getFitnessTime()) {
                    replaced = true;
                }
                break;
            case 2:
                if (newSon.getFitnessFuel() < sub.getFitnessFuel()) {
                    replaced = true;
                }
                break;
            case 3:
                if (newSon.getFitness() < sub.getFitness()) {
                    replaced = true;
                }
                break;
            default:
                break;
        }

        if (replaced) {
            next.setId(newSon.getId());
            int[][] sonRoute = newSon.getRoute();
            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    next.setClientInRoute(j, k, sonRoute[j][k]);
                }
            }

            next.setFitnessDistance(newSon.getFitnessDistance());
            next.setFitnessTime(newSon.getFitnessTime());
            next.setFitnessFuel(newSon.getFitnessFuel());
            next.setFitness(newSon.getFitness());

            System.out.println("Substituiu indivíduo de ID: " + sub.getId());
        } else {
            //System.out.println("Não substituiu");
        }
    }

    // Function to Evolve the population
    public void evolvePopMulti(
            int generation,
            List<Individual> subPopDistance, List<Individual> nextSubPopDistance,
            List<Individual> subPopTime, List<Individual> nextSubPopTime,
            List<Individual> subPopFuel, List<Individual> nextSubPopFuel,
            List<Individual> subPopPonderation, List<Individual> nextSubPopPonderation,
            List<Client> clients,
            int elitismSize,
            int generationsBeforeComparison,
            int selectionType, // 1: roulette (futuro), 2: tournament
            int crossingType // 1: one-point, 2: two-point (futuro)
    ) {

        SelectionUtils.selectElite(subPopDistance, nextSubPopDistance, 0, elitismSize);
        SelectionUtils.selectElite(subPopTime, nextSubPopTime, 1, elitismSize);
        SelectionUtils.selectElite(subPopFuel, nextSubPopFuel, 2, elitismSize);
        SelectionUtils.selectElite(subPopPonderation, nextSubPopPonderation, 3, elitismSize);

        // Evolving the population
        if (generation < generationsBeforeComparison) {

            // Ensures diversity: inserts children directly into the next generation
            for (int i = elitismSize; i < App.sub_pop_size; i++) {
                List<Individual> parents = SelectionUtils.subPopSelection(this);
                Individual newSon = null;

                switch (crossingType) {
                    case 1:
                        newSon = Crossover.onePointCrossing(parents.get(0), parents.get(1), i);
                        break;

                    // case 2: // Two-point (implementar depois)
                    // newSon = Crossover.twoPointCrossing(parents.get(0), parents.get(1), i);
                    // break;

                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }

                Mutation.mutate(newSon, App.mutationRate);

                newSon.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSon, clients));
                newSon.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSon, clients));
                newSon.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSon, clients));
                newSon.setFitness(new DefaultFitnessCalculator().calculateFitness(newSon, clients));

                nextSubPopDistance.set(i, newSon.deepCopy());
                nextSubPopTime.set(i, newSon.deepCopy());
                nextSubPopFuel.set(i, newSon.deepCopy());
                nextSubPopPonderation.set(i, newSon.deepCopy());
            }
        } else {

            // After the first generations, compare and only replace if the child is better
            for (int i = elitismSize; i < App.sub_pop_size; i++) {
                List<Individual> parents = SelectionUtils.subPopSelection(this);
                Individual newSon = null;

                switch (crossingType) {
                    case 1:
                        newSon = Crossover.onePointCrossing(parents.get(0), parents.get(1), i);
                        break;

                    // case 2: // Two-point (implementar depois)
                    // newSon = Crossover.twoPointCrossing(parents.get(0), parents.get(1), i);
                    // break;

                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }

                Mutation.mutate(newSon, App.mutationRate);

                newSon.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSon, clients));
                newSon.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSon, clients));
                newSon.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSon, clients));
                newSon.setFitness(new DefaultFitnessCalculator().calculateFitness(newSon, clients));

                compareSonSubPop(newSon, subPopDistance, nextSubPopDistance, 0, i);
                compareSonSubPop(newSon, subPopTime, nextSubPopTime, 1, i);
                compareSonSubPop(newSon, subPopFuel, nextSubPopFuel, 2, i);
                compareSonSubPop(newSon, subPopPonderation, nextSubPopPonderation, 3, i);
            }
        }

        /*
         System.out.println("ROTA COMPLETA nextSubPopDistance[0]:");
         Individual ind0 = nextSubPopDistance.get(0);
         for (int v = 0; v < App.numVehicles; v++) {
            System.out.print("Veículo " + v + ": ");
            for (int c = 0; c < App.numClients; c++) {
                System.out.print(ind0.getRoute()[v][c] + " ");
            }
            System.out.println();
        }
        
        System.out.println("ROTA COMPLETA nextSubPopTime[0]:");
        ind0 = nextSubPopTime.get(0);
        for (int v = 0; v < App.numVehicles; v++) {
            System.out.print("Veículo " + v + ": ");
            for (int c = 0; c < App.numClients; c++) {
                System.out.print(ind0.getRoute()[v][c] + " ");
            }
            System.out.println();
        }
        
        System.out.println("ROTA COMPLETA nextSubPopFuel[0]:");
        ind0 = nextSubPopFuel.get(0);
        for (int v = 0; v < App.numVehicles; v++) {
            System.out.print("Veículo " + v + ": ");
            for (int c = 0; c < App.numClients; c++) {
                System.out.print(ind0.getRoute()[v][c] + " ");
            }
            System.out.println();
        }
        
        System.out.println("ROTA COMPLETA nextSubPopPonderation[0]:");
        ind0 = nextSubPopPonderation.get(0);
        for (int v = 0; v < App.numVehicles; v++) {
            System.out.print("Veículo " + v + ": ");
            for (int c = 0; c < App.numClients; c++) {
                System.out.print(ind0.getRoute()[v][c] + " ");
            }
            System.out.println();
        }
        */
        
        // Updates subpopulations with individuals from the next generation
        updateSubPop(subPopDistance, nextSubPopDistance);
        updateSubPop(subPopTime, nextSubPopTime);
        updateSubPop(subPopFuel, nextSubPopFuel);
        updateSubPop(subPopPonderation, nextSubPopPonderation);
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

    // Getters and Setters of the subpopulations
    public List<Individual> getSubPopDistance() {
        return subPopDistance;
    }

    public List<Individual> getSubPopTime() {
        return subPopTime;
    }

    public List<Individual> getSubPopFuel() {
        return subPopFuel;
    }

    public List<Individual> getSubPopPonderation() {
        return subPopPonderation;
    }
}
