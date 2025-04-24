package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vrp.BenchMarkReader;
import vrp.Client;
import vrp.ProblemInstance;
import configuration.*;
import genetic.DefaultFitnessCalculator;
import genetic.DistanceFitnessCalculator;
import genetic.FitnessCalculator;
import genetic.Individual;
import genetic.Population;
import genetic.SelectionUtils;
import genetic.TimeFitnessCalculator;

public class App {
    // VRP Variables
    public static int numVehicles;
    public static int vehicleCapacity;
    public static int numClients;
    public static int VEHICLE_SPEED = 50;
    public static int NUM_FUEL_TYPES = 3;
    public static double G_FUEL_PRICE = 5.48;
    public static double E_FUEL_PRICE = 3.99;
    public static double D_FUEL_PRICE = 8.79;
    public static double G_FUEL_CONSUMPTION = 7.53;
    public static double E_FUEL_CONSUMPTION = 5;
    public static double D_FUEL_CONSUMPTION = 12;
    public static double WEIGHT_NUM_VEHICLES = 0.25;
    public static double WEIGHT_NUM_VIOLATIONS = 0.5;
    public static double WEIGHT_TOTAL_COST = 0.75;

    // EAs Variables
    public static int pop_size = 10;
    public static int sub_pop_size = (int) Math.floor((double) pop_size / 3);
    public static double elitismRate = 0.1;
    public static int QUANTITYSELECTEDTOURNAMENT = 2;
    public static int tournamentSize = 2;

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
            population.distributeSubpopulations();

            // Criando o FitnessCalculator
            FitnessCalculator fitnessCalculator = new TimeFitnessCalculator();

            // System.out.println("\nCalculando fitness para subPopDistance:");
            for (Individual ind : population.getSubPopTime()) {
                double fitness = fitnessCalculator.calculateFitness(ind, instance.getClients());
                ind.setFitness(fitness);
                // System.out.println("Fitness do indivíduo " + ind.getId() + ": " + fitness);
            }

            // Seleciona dois pais de subpopulações aleatórias
            List<Individual> parents = SelectionUtils.subPopSelection(population);

            System.out.println("Pais selecionados:");
            for (Individual parent : parents) {
                System.out.println("Indivíduo ID: " + parent.getId() + ", Fitness: " + parent.getFitness());
                parent.printRoutes();
            }

            // Printando os 5 primeiros indivíduos
            // for (int i = 0; i < Math.min(1, individuals.size()); i++) {
            // Individual ind = individuals.get(i);
            // System.out.println("Individual " + i + ":");
            // ind.printRoutes();
            // }

            // Printando os individuos das subpopulações
            // population.printSubPopulations();

        } catch (IOException e) {
            System.out.println("Error reading the file");
            e.printStackTrace();
        }
    }

}
