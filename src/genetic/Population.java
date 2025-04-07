package genetic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import main.App;
import main.App.numClients;
import vrp.Client;

public class Population {
    private List<Individual> individuals;

    public Population(List<Individual> individuals) {
        this.individuals = individuals;
    }

    public void initializePopulation(List<Client> clients) {

        //Condition to see if the list of clients isn't null
        if (clients == null || clients.isEmpty()){
            throw new IllegalArgumentException("Client list cannot be empty");
        }else{
            System.out.println("Passed\n");
        }

        //Copying the clients array
        List<Client> clientsCopy = new ArrayList<>(clients);


        for(int h=0; h<App.pop_size; h++){
            Individual individual = new Individual(h, 0, 0, 0, 0);
            boolean[] visited = new boolean[App.numClients];

            visited[0] = true; //Distribution center is the starting point
            
            //"Cleaning" the array
            Arrays.fill(visited, false);
            

            //Initializing the population empty (-1)
            Client distributionCenter = clientsCopy.get(0);


            //Calculating the distance beetween the distributioncenter and the clients
            for (Client client: clientsCopy){
                double distance = calculateDistance(client, distributionCenter);
                client.setDistanceFromDepot(distance);
            }

            //Sorting the clients list
            clientsCopy.sort(Comparator.comparingDouble(Client::getDistanceFromDepot));

            int clientIndex = 1;

            for(int v=0; v<App.numVehicles; v++){
                int capacity = 0;
                int pos = 0;
                int currentClient = 0;

                while (clientIndex < App.numClients){
                    int nextClient = findClosestClient(currentClient, clientsCopy, visited);

                    if (nextClient == -1) break;

                    Client client = clients.get(nextClient);
                    int demand = client.getDemand();

                    if(capacity + demand > App.vehicleCapacity) break;

                    individual.setClientInRoute(v, pos, nextClient);
                    visited[nextClient] = true;
                    capacity += demand;
                    currentClient = nextClient;
                    pos++;
                }
            }

            individuals.add(individual);
        }

    }

    //Function to calculate the distance beetwen two points
    private double calculateDistance(Client c1, Client c2){
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
    }

    //Function to locate the closest client from the current client
    private int findClosestClient(int currentClient, List<Client> clients, boolean[] visited){
        double minDistance = Double.MAX_VALUE;
        int closestClient = -1;

        for(Client client : clients){
            if (!visited[client.getId()]){
                double distance = calculateDistance(clients.get(currentClient), client);
                if(distance < minDistance){
                    minDistance = distance;
                    closestClient = client.getId();
                }
            }
        }

        return closestClient;
    }

}
