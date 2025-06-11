package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

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
    public static int pop_size = 1;
    public static int sub_pop_size = (int) Math.floor((double) pop_size / 3);
    public static double elitismRate = 0.1;
    public static int QUANTITYSELECTEDTOURNAMENT = 2;
    public static int tournamentSize = 2;
    public static double mutationRate = 0.1;
    public static int numGenerations = 3000; // 3000 gerações que era o número utilizado na versão em C
    public static int nextIndividualId = pop_size; // Inicializa com pop_size

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // Menu para escolher o algoritmo
        System.out.println("=== MENU DE SELEÇÃO DE ALGORITMO ===");
        System.out.println("1 - Algoritmo Multi-Objetivo");
        System.out.println("2 - Algoritmo Mono-Objetivo");
        System.out.println("3 - Algoritmo Passo a Passo (para depuração)");
        System.out.print("Digite sua escolha (1, 2 ou 3): ");

        int algorithmChoice = 0;
        while (algorithmChoice != 1 && algorithmChoice != 2 && algorithmChoice != 3) {
            try {
                algorithmChoice = Integer.parseInt(scanner.nextLine().trim());
                if (algorithmChoice != 1 && algorithmChoice != 2) {
                    System.out.print("Opção inválida. Digite 1, 2 ou 3: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Digite 1, 2 ou 3: ");
            }
        }

        // Menu para escolher a instância
        System.out.println("\n=== MENU DE SELEÇÃO DE INSTÂNCIA ===");
        String instancePath = "src/instances/solomon/";
        File folder = new File(instancePath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Erro: Diretório de instâncias não encontrado.");
            scanner.close();
            return;
        }

        File[] instanceFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (instanceFiles == null || instanceFiles.length == 0) {
            System.out.println("Erro: Nenhuma instância encontrada.");
            scanner.close();
            return;
        }

        // Ordenar arquivos alfabeticamente
        Arrays.sort(instanceFiles, Comparator.comparing(File::getName));

        // Mostrar as instâncias disponíveis
        for (int i = 0; i < instanceFiles.length; i++) {
            System.out.println((i + 1) + " - " + instanceFiles[i].getName());
        }

        System.out.print("Escolha uma instância (1-" + instanceFiles.length + "): ");
        int instanceChoice = 0;
        while (instanceChoice < 1 || instanceChoice > instanceFiles.length) {
            try {
                instanceChoice = Integer.parseInt(scanner.nextLine().trim());
                if (instanceChoice < 1 || instanceChoice > instanceFiles.length) {
                    System.out.print("Opção inválida. Digite um número entre 1 e " + instanceFiles.length + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Digite um número entre 1 e " + instanceFiles.length + ": ");
            }
        }

        String selectedInstancePath = instanceFiles[instanceChoice - 1].getPath();
        System.out.println("\nInstância selecionada: " + instanceFiles[instanceChoice - 1].getName());

        // Fechar o scanner depois de usar
        scanner.close();

        BenchMarkReader reader = new BenchMarkReader();

        try {
            ProblemInstance instance = reader.readInstaces(selectedInstancePath);

            numVehicles = instance.getNumVehicles();
            vehicleCapacity = instance.getVehicleCapacity();
            numClients = instance.getClients().size();

            System.out.println("Number of vehicles: " + numVehicles);
            System.out.println("Vehicle capacity: " + vehicleCapacity);
            System.out.println("Number of clients: " + numClients);

            if (algorithmChoice == 1) {
                // Executar algoritmo Multi-Objetivo (código existente)
                runMultiObjectiveAlgorithm(instance);
            } else if (algorithmChoice == 2) {
                // Executar algoritmo Mono-Objetivo
                // runMonoObjectiveAlgorithm(instance);
            } else if (algorithmChoice == 3) {
                // Executar versão que roda o algoritmo passo a passo, para deputação
                runDebugAlgorithm(instance);
            } else {
                System.out.println("Opção inválida. Encerrando o programa.");
            }

        } catch (IOException e) {
            System.out.println("Error reading the file");
            e.printStackTrace();
        }
    }

    private static void runMultiObjectiveAlgorithm(ProblemInstance instance) {
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

        // Criação de listas para armazenar os melhores fitness a cada 100 gerações
        List<Double> bestDistanceFitnessList = new ArrayList<>();
        List<Double> bestTimeFitnessList = new ArrayList<>();
        List<Double> bestFuelFitnessList = new ArrayList<>();
        List<Double> bestPonderationFitnessList = new ArrayList<>();
        List<Integer> generationsList = new ArrayList<>();

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

                // A cada 100 gerações ou na última geração, salvamos os melhores fitness
                if (generation % 100 == 0 || generation == numGenerations - 1) {
                    System.out.println("--- Estatísticas da geração " + generation + " ---");

                    // Encontrar o melhor fitness para cada subpopulação
                    double bestDistanceFitness = findBestFitness(population.getSubPopDistance(),
                            individual -> individual.getFitnessDistance());

                    double bestTimeFitness = findBestFitness(population.getSubPopTime(),
                            individual -> individual.getFitnessTime());

                    double bestFuelFitness = findBestFitness(population.getSubPopFuel(),
                            individual -> individual.getFitnessFuel());

                    double bestPonderationFitness = findBestFitness(population.getSubPopPonderation(),
                            individual -> individual.getFitness());

                    // Adicionando às listas
                    bestDistanceFitnessList.add(bestDistanceFitness);
                    bestTimeFitnessList.add(bestTimeFitness);
                    bestFuelFitnessList.add(bestFuelFitness);
                    bestPonderationFitnessList.add(bestPonderationFitness);
                    generationsList.add(generation);

                    System.out.println("Melhor Fitness Distance: " + bestDistanceFitness);
                    System.out.println("Melhor Fitness Time: " + bestTimeFitness);
                    System.out.println("Melhor Fitness Fuel: " + bestFuelFitness);
                    System.out.println("Melhor Fitness Ponderado: " + bestPonderationFitness);
                }
            } catch (Exception e) {
                System.out.println("ERRO NA GERAÇÃO " + generation + ": " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        // Salvar os resultados em um arquivo
        saveResultsToFile(generationsList, bestDistanceFitnessList, bestTimeFitnessList,
                bestFuelFitnessList, bestPonderationFitnessList);

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

        // System.out.println("\n--- IDs dos indivíduos nas próximas subpopulações
        // ---");

        // System.out.print("NextSubPopDistance IDs: ");
        // for (Individual ind : nextSubPopDistance) {
        // System.out.print(ind.getId() + " ");
        // }
        // System.out.println();

        // System.out.print("NextSubPopTime IDs: ");
        // for (Individual ind : nextSubPopTime) {
        // System.out.print(ind.getId() + " ");
        // }
        // System.out.println();

        // System.out.print("NextSubPopFuel IDs: ");
        // for (Individual ind : nextSubPopFuel) {
        // System.out.print(ind.getId() + " ");
        // }
        // System.out.println();

        // System.out.print("NextSubPopPonderation IDs: ");
        // for (Individual ind : nextSubPopPonderation) {
        // System.out.print(ind.getId() + " ");
        // }
        // System.out.println();
    }

    // private static void runMonoObjectiveAlgorithm(ProblemInstance instance) {

    // List<Individual> individuals = new ArrayList<>();

    // // Initializing population
    // Population population = new Population(individuals);
    // population.initializePopulation(instance.getClients());
    // // We will not distribute subpopulations in mono-objective algorithm

    // // Calculating fitness for the initial population
    // System.out.println("Calculating fitness for the initial population...");
    // List<Client> clients = instance.getClients();
    // for (Individual ind : population) {
    // double fitness = new DefaultFitnessCalculator().calculateFitness(ind,
    // clients);
    // ind.setFitness(fitness);
    // }

    // // Initializing auxiliary subpopulations for the next generation
    // List<Individual> nextPopulation = new ArrayList<>();
    // for (int i = 0; i < pop_size; i++) {
    // nextPopulation.add(new Individual(-1, 0, 0, 0, 0));
    // }

    // int elitismSize = Math.max(1, (int) (sub_pop_size * elitismRate));

    // // List to store the best fitness every 100 generations
    // List<Double> bestFitnessList = new ArrayList<>();
    // List<Integer> generationsList = new ArrayList<>();

    // for (int generation = 0; generation < numGenerations; generation++) {
    // System.out.println("\nGeração: " + generation);

    // try {
    // System.out.println("Iniciando evolução...");

    // evolvePopMono(
    // generation,
    // population,
    // nextPopulation,
    // instance.getClients(),
    // elitismSize);

    // System.out.println("Evolução concluída!");

    // // A cada 100 gerações ou na última geração, salvamos os melhores fitness
    // if (generation % 100 == 0 || generation == numGenerations - 1) {
    // System.out.println("--- Estatísticas da geração " + generation + " ---");

    // // Encontrar o melhor fitness da população
    // double bestFitness = findBestFitness(population, individual ->
    // individual.getFitness());

    // // Adicionando às listas
    // bestFitnessList.add(bestFitness);
    // generationsList.add(generation);

    // System.out.println("Melhor Fitness: " + bestFitness);
    // }
    // } catch (Exception e) {
    // System.out.println("ERRO NA GERAÇÃO " + generation + ": " + e.getMessage());
    // e.printStackTrace();
    // break;
    // }
    // }

    // saveResultsToFile(generationsList, bestFitnessList);

    // Individual bestIndividual = findBestIndividual(population);
    // System.out.println("\n--- Melhor indivíduo ---");
    // System.out.println("ID: " + bestIndividual.getId());
    // System.out.println("Fitness: " + bestIndividual.getFitness());
    // bestIndividual.printRoutes();
    // }

    private static void runDebugAlgorithm(ProblemInstance instance) {
        System.out.println("\n=== EXECUTANDO ALGORITMO DE DEBUG ===");

        // Lista original de clientes da instância
        List<Client> clients = instance.getClients();

        // Iniciar debug da população
        System.out.println("\n=== INICIALIZANDO POPULAÇÃO COM DEBUG ===");

        // Criando uma população de debug com apenas 1 indivíduo para facilitar a
        // visualização
        List<Individual> individuals = new ArrayList<>();
        Population population = new Population(individuals);

        // Chamando método de inicialização com debug
        population.initializePopulation(clients);

        System.out.println("\n=== DEBUG DE INICIALIZAÇÃO CONCLUÍDO ===");
    }

    /**
     * Encontra o melhor fitness (menor valor) em uma lista de indivíduos
     * 
     * @param individuals      Lista de indivíduos
     * @param fitnessExtractor Função para extrair o valor de fitness do indivíduo
     * @return O melhor valor de fitness (menor valor)
     */
    private static double findBestFitness(List<Individual> individuals,
            java.util.function.Function<Individual, Double> fitnessExtractor) {
        return individuals.stream()
                .mapToDouble(ind -> fitnessExtractor.apply(ind))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private static void saveResultsToFile(List<Integer> generations,
            List<Double> distanceFitness,
            List<Double> timeFitness,
            List<Double> fuelFitness,
            List<Double> ponderationFitness) {

        try {
            // Criar diretório de resultados se não existir
            File resultsDir = new File("results");
            if (!resultsDir.exists()) {
                resultsDir.mkdir();
            }

            // Obter timestamp atual para nome do arquivo
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String fileName = "results/evolution_results_" + timestamp + ".txt";

            java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(fileName));

            // Escrever cabeçalho com números das gerações
            writer.print("Subpopulação\\Geração\t");
            for (Integer gen : generations) {
                writer.print("g" + gen + "\t");
            }
            writer.println();

            // Escrever resultados de cada subpopulação
            writer.print("subPopDistance\t");
            for (Double fitness : distanceFitness) {
                writer.print(String.format("%.2f\t", fitness));
            }
            writer.println();

            writer.print("subPopTime\t");
            for (Double fitness : timeFitness) {
                writer.print(String.format("%.2f\t", fitness));
            }
            writer.println();

            writer.print("subPopFuel\t");
            for (Double fitness : fuelFitness) {
                writer.print(String.format("%.2f\t", fitness));
            }
            writer.println();

            writer.print("subPopPonderation\t");
            for (Double fitness : ponderationFitness) {
                writer.print(String.format("%.2f\t", fitness));
            }
            writer.println();

            writer.close();
            System.out.println("\nResultados salvos em: " + fileName);

        } catch (IOException e) {
            System.out.println("Erro ao salvar resultados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
