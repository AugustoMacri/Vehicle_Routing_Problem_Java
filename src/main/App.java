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
import genetic.FuelFitnessCalculator;
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
    public static int numGenerations = 1000;

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

            // Calculando o fitness de cada subpopulação
            System.out.println("Calculando fitness para todas as subpopulações...");
            List<Client> clients = instance.getClients();

            for (Individual ind : population.getSubPopDistance()) {
                double fitnessDistance = new DistanceFitnessCalculator().calculateFitness(ind, clients);
                ind.setFitnessDistance(fitnessDistance);
            }

            for (Individual ind : population.getSubPopTime()) {
                double fitnessTime = new TimeFitnessCalculator().calculateFitness(ind, clients);
                ind.setFitnessTime(fitnessTime);
            }

            for (Individual ind : population.getSubPopFuel()) {
                double fitnessFuel = new FuelFitnessCalculator().calculateFitness(ind, clients);
                ind.setFitnessFuel(fitnessFuel);
            }

            for (Individual ind : population.getSubPopPonderation()) {
                double fitnessPond = new DefaultFitnessCalculator().calculateFitness(ind, clients);
                ind.setFitness(fitnessPond);
            }

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

            int elitismSize = Math.max(1, (int) (sub_pop_size * elitismRate));
            int generationsBeforeComparison = 5;
            int selectionType = 2; // Tournament
            int crossingType = 1; // One-point

            for (int generation = 0; generation < numGenerations; generation++) {
                System.out.println("\nGeração: " + generation);

                try {
                    System.out.println("Iniciando evolução...");

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

                    System.out.println("Evolução concluída!");

                    if (generation % 100 == 0 || generation == numGenerations - 1) {
                        System.out.println("--- Estatísticas da geração " + generation + " ---");
                    }
                } catch (Exception e) {
                    System.out.println("ERRO NA GERAÇÃO " + generation + ": " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }

            // Printar IDs de todos os indivíduos em cada subpopulação
            System.out.println("\n--- IDs dos indivíduos em cada subpopulação ---");

            System.out.print("SubPopDistance IDs: ");
            for (Individual ind : population.getSubPopDistance()) {
                System.out.print(ind.getId() + " " +
                        "(Fitness Distance: " + ind.getFitnessDistance() + ") ");
            }
            System.out.println();

            System.out.print("SubPopTime IDs: ");
            for (Individual ind : population.getSubPopTime()) {
                System.out.print(ind.getId() + " " +
                        "(Fitness Time: " + ind.getFitnessTime() + ") ");
            }
            System.out.println();

            System.out.print("SubPopFuel IDs: ");
            for (Individual ind : population.getSubPopFuel()) {
                System.out.print(ind.getId() + " " +
                        "(Fitness Fuel: " + ind.getFitnessFuel() + ") ");
            }
            System.out.println();

            System.out.print("SubPopPonderation IDs: ");
            for (Individual ind : population.getSubPopPonderation()) {
                System.out.print(ind.getId() + " " +
                        "(Fitness Pond: " + ind.getFitness() + ") ");
            }
            System.out.println();

            // Você também pode imprimir os IDs das nextSubPop para verificar se estão
            // vazios
            System.out.println("\n--- IDs dos indivíduos nas próximas subpopulações ---");

            System.out.print("NextSubPopDistance IDs: ");
            for (Individual ind : nextSubPopDistance) {
                System.out.print(ind.getId() + " ");
            }
            System.out.println();

            System.out.print("NextSubPopTime IDs: ");
            for (Individual ind : nextSubPopTime) {
                System.out.print(ind.getId() + " ");
            }
            System.out.println();

            System.out.print("NextSubPopFuel IDs: ");
            for (Individual ind : nextSubPopFuel) {
                System.out.print(ind.getId() + " ");
            }
            System.out.println();

            System.out.print("NextSubPopPonderation IDs: ");
            for (Individual ind : nextSubPopPonderation) {
                System.out.print(ind.getId() + " ");
            }
            System.out.println();

        } catch (IOException e) {
            System.out.println("Error reading the file");
            e.printStackTrace();
        }
    }

}
