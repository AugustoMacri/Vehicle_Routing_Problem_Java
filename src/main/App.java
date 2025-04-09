package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vrp.BenchMarkReader;
import vrp.Client;
import vrp.ProblemInstance;
import configuration.*;
import genetic.Individual;
import genetic.Population;

public class App {
    // VRP Variables
    public static int numVehicles;
    public static int vehicleCapacity;
    public static int numClients;

    // EAs Variables
    public static int pop_size = 10;

    public static void main(String[] args) throws Exception {

        BenchMarkReader reader = new BenchMarkReader();

        try {
            ProblemInstance instance = reader.readInstaces("src/instances/solomon/C101.txt");
            
            // Getting the number of vehicles, vehicle capacity and clients
            numVehicles = instance.getNumVehicles();
            vehicleCapacity = instance.getVehicleCapacity();
            numClients = instance.getClients().size();  

            System.out.println(("Number of vehicles: " + instance.getNumVehicles()));
            System.out.println(("Vehicle capacity: " + instance.getVehicleCapacity()));
            System.out.println(("Number of clients: " + instance.getClients().size()));


            // Criando lista vazia de indivíduos
            List<Individual> individuals = new ArrayList<>();


            // Initializing Population
            Population population = new Population(individuals);
            population.initializePopulation(instance.getClients());

            // Printando os 5 primeiros indivíduos
            for (int i = 0; i < Math.min(1, individuals.size()); i++) {
                Individual ind = individuals.get(i);
                System.out.println("Individual " + i + ":");
                ind.printRoutes();
            }

        } catch (IOException e) {
            System.out.println("Error reading the file");
            e.printStackTrace();
        }
    }

}
