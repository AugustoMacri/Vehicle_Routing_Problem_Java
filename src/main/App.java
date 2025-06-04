package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import vrp.BenchMarkReader;
import vrp.Client;
import vrp.ProblemInstance;
import configuration.*;
import genetic.Crossover;
import genetic.DefaultFitnessCalculator;
import genetic.DistanceFitnessCalculator;
import genetic.FitnessCalculator;
import genetic.Individual;
import genetic.Mutation;
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
    public static double mutationRate = 0.1;

    public static void main(String[] args) throws Exception {

        BenchMarkReader reader = new BenchMarkReader();

        try {
            ProblemInstance instance = reader.readInstaces("src/instances/solomon/C101.txt");

            numVehicles = instance.getNumVehicles();
            vehicleCapacity = instance.getVehicleCapacity();
            numClients = instance.getClients().size();

            System.out.println("Number of vehicles: " + numVehicles);
            System.out.println("Vehicle capacity: " + vehicleCapacity);
            System.out.println("Number of clients: " + numClients);

            // Criando lista vazia de indivíduos
            List<Individual> individuals = new ArrayList<>();

            // Inicializando população
            Population population = new Population(individuals);
            population.initializePopulation(instance.getClients());
            population.distributeSubpopulations();

            // Inicializando as subpopulações auxiliares para a próxima geração
            List<Individual> nextSubPopDistance = new ArrayList<>();
            List<Individual> nextSubPopTime = new ArrayList<>();
            List<Individual> nextSubPopFuel = new ArrayList<>();
            List<Individual> nextSubPopPonderation = new ArrayList<>();
            for (int i = 0; i < sub_pop_size; i++) {
                nextSubPopDistance.add(new Individual(-1, 0, 0, 0, 0));
                nextSubPopTime.add(new Individual(-1, 0, 0, 0, 0));
                nextSubPopFuel.add(new Individual(-1, 0, 0, 0, 0));
                nextSubPopPonderation.add(new Individual(-1, 0, 0, 0, 0));
            }

            // Imprimir o primeiro indivíduo da subpopulação de distância
            Individual first = population.getSubPopDistance().get(0);
            System.out.println("\nPrimeiro indivíduo da subpopulação de distância:");
            System.out.println("ID: " + first.getId());
            System.out.println("FitnessDistance: " + first.getFitnessDistance());
            System.out.println("FitnessTime: " + first.getFitnessTime());
            System.out.println("FitnessFuel: " + first.getFitnessFuel());
            System.out.println("Fitness (Ponderado): " + first.getFitness());
            System.out.println("Rotas:");
            int[][] route = first.getRoute();
            for (int v = 0; v < App.numVehicles; v++) {
                System.out.print("Veículo " + v + ": ");
                for (int c = 0; c < App.numClients; c++) {
                    System.out.print(route[v][c] + " ");
                }
                System.out.println();
            }

            int elitismSize = Math.max(1, (int) (sub_pop_size * elitismRate));
            int generationsBeforeComparison = 5;
            int selectionType = 2; // Torneio
            int crossingType = 1; // One-point

            // Rodar apenas uma geração para teste
            int generation = 0;
            System.out.println("\nGeração: " + generation);

            population.evolvePopMulti(
                    generation,
                    population.getSubPopDistance(), nextSubPopDistance,
                    population.getSubPopTime(), nextSubPopTime,
                    population.getSubPopFuel(), nextSubPopFuel,
                    population.getSubPopPonderation(), nextSubPopPonderation,
                    instance.getClients(),
                    elitismSize,
                    generationsBeforeComparison,
                    selectionType,
                    crossingType);

            // Imprimir o primeiro indivíduo da subpopulação de distância
            Individual first1 = population.getSubPopDistance().get(0);
            System.out.println("\nPrimeiro indivíduo da subpopulação de distância:");
            System.out.println("ID: " + first1.getId());
            System.out.println("FitnessDistance: " + first1.getFitnessDistance());
            System.out.println("FitnessTime: " + first1.getFitnessTime());
            System.out.println("FitnessFuel: " + first1.getFitnessFuel());
            System.out.println("Fitness (Ponderado): " + first1.getFitness());
            System.out.println("Rotas:");
            int[][] route1 = first1.getRoute();
            for (int v = 0; v < App.numVehicles; v++) {
                System.out.print("Veículo " + v + ": ");
                for (int c = 0; c < App.numClients; c++) {
                    System.out.print(route1[v][c] + " ");
                }
                System.out.println();
            }

        } catch (IOException e) {
            System.out.println("Error reading the file");
            e.printStackTrace();
        }
    }

}
