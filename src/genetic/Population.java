package genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // boolean debugMode = true;

        // Condition to see if the list of clients isn't null
        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("Client list cannot be empty");
        } else {
            // System.out.println("Passed\n");
        }

        // Copying the clients array
        List<Client> clientsCopy = new ArrayList<>(clients);

        // Usar K-means clustering para 70% da população inicial
        int clusteringPopSize = (int) (App.pop_size * 0.7);

        System.out.println("Inicializando população com K-means clustering...");
        System.out.println("Indivíduos com clustering: " + clusteringPopSize);
        System.out.println("Indivíduos com Gillet-Miller: " + (App.pop_size - clusteringPopSize));

        KMeansClusteringInitializer kmeansInitializer = new KMeansClusteringInitializer();

        // Inicializar com K-means clustering
        for (int h = 0; h < clusteringPopSize; h++) {
            // Variar o número de clusters para diversidade (entre 8 e 15 clusters)
            int numClusters = 8 + (h % 8);
            Individual individual = kmeansInitializer.initializeWithClustering(h, clientsCopy, numClusters);
            individuals.add(individual);
        }

        // Inicializar o resto com Gillet-Miller (método original)
        for (int h = clusteringPopSize; h < App.pop_size; h++) {
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

                    // ---------------------------------------------------------------------------------------------------------
                    // Esse trecho é porque estava tendo um problema de passar o next client como
                    // posição, nao como ID, sendo que eu passava o ID como parâmetro
                    // Isso é absudo, porque se o próximo cliente era o com ID 5, ele pegava o
                    // cliente na posição 5 da lista.
                    Client client = null;
                    for (Client c : clientsCopy) {
                        if (c.getId() == nextClient) {
                            client = c;
                            break;
                        }
                    }
                    if (client == null) {
                        throw new RuntimeException("Cliente com ID " + nextClient + " não encontrado na lista");
                    }

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

        System.out.println("População inicializada com sucesso!");

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

    }

    // Function to update the current population to the next population
    public void updatePopMono(List<Individual> population, List<Individual> nextPopulation) {
        int size = Math.min(population.size(), nextPopulation.size());

        for (int i = 0; i < size; i++) {
            Individual src = nextPopulation.get(i);
            Individual dest = population.get(i);

            dest.setId(src.getId());
            dest.setFitness(src.getFitness());

            int[][] srcRoute = src.getRoute();
            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    dest.setClientInRoute(j, k, srcRoute[j][k]);
                }
            }
        }
    }

    // Function to compare the son with the subpopulation and replace only the worst
    // individual
    public static void compareSonSubPop(
            Individual newSon,
            List<Individual> subPop,
            List<Individual> nextPop,
            int fitnessType,
            int startIndex) {

        // Primeiro, encontra o pior indivíduo na subpopulação (excluindo os elites)
        // Inicializa a busca a partir de startIndex (não da posição 0)
        int worstIndex = startIndex;
        double worstFitness;

        // Define o fitness do primeiro indivíduo não-elite como pior inicialmente
        switch (fitnessType) {
            case 0:
                worstFitness = subPop.get(worstIndex).getFitnessDistance();
                // Procura pelo pior fitness
                for (int i = startIndex; i < subPop.size(); i++) {
                    double fitness = subPop.get(i).getFitnessDistance();
                    if (fitness > worstFitness) {
                        worstFitness = fitness;
                        worstIndex = i;
                    }
                }
                break;
            case 1:
                worstFitness = subPop.get(worstIndex).getFitnessTime();
                for (int i = startIndex; i < subPop.size(); i++) {
                    double fitness = subPop.get(i).getFitnessTime();
                    if (fitness > worstFitness) {
                        worstFitness = fitness;
                        worstIndex = i;
                    }
                }
                break;
            case 2:
                worstFitness = subPop.get(worstIndex).getFitnessFuel();
                for (int i = startIndex; i < subPop.size(); i++) {
                    double fitness = subPop.get(i).getFitnessFuel();
                    if (fitness > worstFitness) {
                        worstFitness = fitness;
                        worstIndex = i;
                    }
                }
                break;
            case 3:
                worstFitness = subPop.get(worstIndex).getFitness();
                for (int i = startIndex; i < subPop.size(); i++) {
                    double fitness = subPop.get(i).getFitness();
                    if (fitness > worstFitness) {
                        worstFitness = fitness;
                        worstIndex = i;
                    }
                }
                break;
            default:
                // Valor de fallback, não deve acontecer com fitnessType válido
                worstFitness = Double.NEGATIVE_INFINITY;
                break;
        }

        // Verifica se o novo filho é melhor que o pior indivíduo
        boolean isBetter = false;

        switch (fitnessType) {
            case 0:
                isBetter = newSon.getFitnessDistance() < worstFitness;
                break;
            case 1:
                isBetter = newSon.getFitnessTime() < worstFitness;
                break;
            case 2:
                isBetter = newSon.getFitnessFuel() < worstFitness;
                break;
            case 3:
                isBetter = newSon.getFitness() < worstFitness;
                break;
            default:
                break;
        }

        // Se o filho é melhor, substitui o pior indivíduo
        if (isBetter) {
            Individual next = nextPop.get(worstIndex);

            // Copia o ID (já é único conforme Crossover.java)
            next.setId(newSon.getId());

            // Copia a rota
            int[][] sonRoute = newSon.getRoute();
            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    next.setClientInRoute(j, k, sonRoute[j][k]);
                }
            }

            // Copia os valores de fitness
            next.setFitnessDistance(newSon.getFitnessDistance());
            next.setFitnessTime(newSon.getFitnessTime());
            next.setFitnessFuel(newSon.getFitnessFuel());
            next.setFitness(newSon.getFitness());

        }
    }

    // Function to compare the son with the population and replace only the worst
    // individual
    public static void compareSonPopMono(
            Individual newSon,
            List<Individual> population,
            List<Individual> nextPopulation,
            int startIndex) {

        // Encontra o pior indivíduo na população (excluindo os elites)
        int worstIndex = 0;
        double worstFitness = population.get(worstIndex).getFitness();

        for (int i = startIndex; i < population.size(); i++) {
            double fitness = population.get(i).getFitness();
            if (fitness > worstFitness) {
                worstFitness = fitness;
                worstIndex = i;
            }
        }

        // Verifica se o novo filho é melhor que o pior indivíduo
        boolean isBetter = newSon.getFitness() < worstFitness;

        // Se o filho é melhor, substitui o pior indivíduo
        if (isBetter) {
            Individual next = nextPopulation.get(worstIndex);

            // Copia o ID
            next.setId(newSon.getId());

            // Copia a rota
            int[][] sonRoute = newSon.getRoute();
            for (int j = 0; j < App.numVehicles; j++) {
                for (int k = 0; k < App.numClients; k++) {
                    next.setClientInRoute(j, k, sonRoute[j][k]);
                }
            }

            // Copia o fitness
            next.setFitness(newSon.getFitness());
        }
    }

    /**
     * Validates and repairs an individual if necessary.
     * Ensures all clients 1 to (numClients-1) are present exactly once.
     */
    private static void validateAndRepairIndividual(Individual individual, int generation, String context) {
        if (!Crossover.validateRoute(individual.getRoute(), App.numClients - 1)) {
            System.err.println("ERRO CRÍTICO: Indivíduo inválido detectado!");
            System.err.println("Contexto: " + context + " na geração " + generation);
            System.err.println("Tentando reparar...");

            // Count how many clients are missing
            Set<Integer> foundClients = new HashSet<>();
            for (int v = 0; v < App.numVehicles; v++) {
                for (int c = 0; c < App.numClients; c++) {
                    int clientId = individual.getRoute()[v][c];
                    if (clientId > 0) {
                        foundClients.add(clientId);
                    }
                }
            }

            System.err.println("Clientes encontrados: " + foundClients.size() + " de " + (App.numClients - 1));

            // Find missing clients
            List<Integer> missingClients = new ArrayList<>();
            for (int i = 1; i < App.numClients; i++) { // Real clients are 1 to App.numClients-1
                if (!foundClients.contains(i)) {
                    missingClients.add(i);
                }
            }

            System.err.println("Clientes faltando: " + missingClients);
            System.err.println("AVISO: Indivíduo será descartado - não pode ser reparado aqui!");
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
        // System.out.println("Início do evolvePopMulti()");

        // System.out.println("Selecionando elite da subPopDistance...");
        SelectionUtils.selectElite(subPopDistance, nextSubPopDistance, 0, elitismSize);

        // System.out.println("Selecionando elite da subPopTime...");
        SelectionUtils.selectElite(subPopTime, nextSubPopTime, 1, elitismSize);

        // System.out.println("Selecionando elite da subPopFuel...");
        SelectionUtils.selectElite(subPopFuel, nextSubPopFuel, 2, elitismSize);

        // System.out.println("Selecionando elite da subPopPonderation...");
        SelectionUtils.selectElite(subPopPonderation, nextSubPopPonderation, 3, elitismSize);

        // System.out.println("Elites selecionados com sucesso!");

        // Pré-preenche a próxima geração com cópias da geração atual para a faixa
        // não-elite
        for (int idx = elitismSize; idx < App.sub_pop_size; idx++) {
            nextSubPopDistance.set(idx, subPopDistance.get(idx).deepCopy());
            nextSubPopTime.set(idx, subPopTime.get(idx).deepCopy());
            nextSubPopFuel.set(idx, subPopFuel.get(idx).deepCopy());
            nextSubPopPonderation.set(idx, subPopPonderation.get(idx).deepCopy());
        }

        // Evolving the population
        if (generation < generationsBeforeComparison) {

            // Ensures diversity: inserts children directly into the next generation
            for (int i = elitismSize; i < App.sub_pop_size; i++) {
                // Gera filhos distintos para cada subpopulação (reduz correlação)
                // Distance
                List<Individual> parentsD = SelectionUtils.subPopSelection(this);
                Individual newSonD = null;
                switch (crossingType) {
                    case 1:
                        newSonD = Crossover.onePointCrossing(parentsD.get(0), parentsD.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonD, App.mutationRate);
                newSonD.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonD, clients));
                newSonD.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonD, clients));
                newSonD.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonD, clients));
                newSonD.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonD, clients));

                // Time
                List<Individual> parentsT = SelectionUtils.subPopSelection(this);
                Individual newSonT = null;
                switch (crossingType) {
                    case 1:
                        newSonT = Crossover.onePointCrossing(parentsT.get(0), parentsT.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonT, App.mutationRate);
                newSonT.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonT, clients));
                newSonT.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonT, clients));
                newSonT.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonT, clients));
                newSonT.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonT, clients));

                // Fuel
                List<Individual> parentsF = SelectionUtils.subPopSelection(this);
                Individual newSonF = null;
                switch (crossingType) {
                    case 1:
                        newSonF = Crossover.onePointCrossing(parentsF.get(0), parentsF.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonF, App.mutationRate);
                newSonF.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonF, clients));
                newSonF.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonF, clients));
                newSonF.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonF, clients));
                newSonF.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonF, clients));

                // Ponderation
                List<Individual> parentsP = SelectionUtils.subPopSelection(this);
                Individual newSonP = null;
                switch (crossingType) {
                    case 1:
                        newSonP = Crossover.onePointCrossing(parentsP.get(0), parentsP.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonP, App.mutationRate);
                newSonP.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonP, clients));
                newSonP.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonP, clients));
                newSonP.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonP, clients));
                newSonP.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonP, clients));

                // Inserção direta (diversidade inicial)
                nextSubPopDistance.set(i, newSonD.deepCopy());
                nextSubPopTime.set(i, newSonT.deepCopy());
                nextSubPopFuel.set(i, newSonF.deepCopy());
                nextSubPopPonderation.set(i, newSonP.deepCopy());
            }
        } else {

            // System.out.println("Compara o filho com os indivíduos da subpopulação...");

            // After the first generations, compare and only replace if the child is better
            for (int i = elitismSize; i < App.sub_pop_size; i++) {
                // Gera filhos distintos para cada subpopulação
                // Distance
                List<Individual> parentsD = SelectionUtils.subPopSelection(this);
                Individual newSonD = null;
                switch (crossingType) {
                    case 1:
                        newSonD = Crossover.onePointCrossing(parentsD.get(0), parentsD.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonD, App.mutationRate);
                newSonD.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonD, clients));
                newSonD.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonD, clients));
                newSonD.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonD, clients));
                newSonD.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonD, clients));

                // Time
                List<Individual> parentsT = SelectionUtils.subPopSelection(this);
                Individual newSonT = null;
                switch (crossingType) {
                    case 1:
                        newSonT = Crossover.onePointCrossing(parentsT.get(0), parentsT.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonT, App.mutationRate);
                newSonT.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonT, clients));
                newSonT.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonT, clients));
                newSonT.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonT, clients));
                newSonT.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonT, clients));

                // Fuel
                List<Individual> parentsF = SelectionUtils.subPopSelection(this);
                Individual newSonF = null;
                switch (crossingType) {
                    case 1:
                        newSonF = Crossover.onePointCrossing(parentsF.get(0), parentsF.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonF, App.mutationRate);
                newSonF.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonF, clients));
                newSonF.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonF, clients));
                newSonF.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonF, clients));
                newSonF.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonF, clients));

                // Ponderation
                List<Individual> parentsP = SelectionUtils.subPopSelection(this);
                Individual newSonP = null;
                switch (crossingType) {
                    case 1:
                        newSonP = Crossover.onePointCrossing(parentsP.get(0), parentsP.get(1), clients);
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de cruzamento não implementado");
                }
                Mutation.mutate(newSonP, App.mutationRate);
                newSonP.setFitnessDistance(new DistanceFitnessCalculator().calculateFitness(newSonP, clients));
                newSonP.setFitnessTime(new TimeFitnessCalculator().calculateFitness(newSonP, clients));
                newSonP.setFitnessFuel(new FuelFitnessCalculator().calculateFitness(newSonP, clients));
                newSonP.setFitness(new DefaultFitnessCalculator().calculateFitness(newSonP, clients));

                // Compara contra o pior na faixa não-elite inteira (a partir de elitismSize)
                compareSonSubPop(newSonD, subPopDistance, nextSubPopDistance, 0, elitismSize);
                compareSonSubPop(newSonT, subPopTime, nextSubPopTime, 1, elitismSize);
                compareSonSubPop(newSonF, subPopFuel, nextSubPopFuel, 2, elitismSize);
                compareSonSubPop(newSonP, subPopPonderation, nextSubPopPonderation, 3, elitismSize);
            }
        }

        // Updates subpopulations with individuals from the next generation
        updateSubPop(subPopDistance, nextSubPopDistance);
        updateSubPop(subPopTime, nextSubPopTime);
        updateSubPop(subPopFuel, nextSubPopFuel);
        updateSubPop(subPopPonderation, nextSubPopPonderation);
    }

    // Function to Evolve the population in mono-objective mode
    public void evolvePopMono(
            int generation,
            List<Individual> population,
            List<Individual> nextPopulation,
            List<Client> clients,
            int elitismSize,
            int generationsBeforeComparison) {

        // Aplicando elitismo - copiando os melhores indivíduos para a próxima geração
        SelectionUtils.elitism(population, nextPopulation, elitismSize);

        // Evolução da população
        if (generation < generationsBeforeComparison) {
            // Nas primeiras gerações, garantimos diversidade: inserimos filhos diretamente
            // na próxima geração

            for (int i = elitismSize; i < population.size(); i++) {
                // Seleção de pais por torneio
                Set<Integer> previousWinners = new HashSet<>();
                Individual parent1 = SelectionUtils.tournamentSelectionMono(population, App.tournamentSize,
                        previousWinners);
                previousWinners.add(parent1.getId());
                Individual parent2 = SelectionUtils.tournamentSelectionMono(population, App.tournamentSize,
                        previousWinners);

                // Cruzamento para gerar um novo filho
                Individual newSon = Crossover.onePointCrossing(parent1, parent2, clients);

                // Mutação
                Mutation.mutate(newSon, App.mutationRate);

                // Cálculo do fitness usando apenas o DefaultFitnessCalculator
                double fitness = new DefaultFitnessCalculator().calculateFitness(newSon, clients);
                newSon.setFitness(fitness);

                // Adicionando o filho diretamente à próxima geração
                nextPopulation.set(i, newSon);
            }
        } else {
            // Após as primeiras gerações, comparamos e só substituímos se o filho for
            // melhor

            for (int i = elitismSize; i < population.size(); i++) {

                // Seleção de pais por torneio
                Set<Integer> previousWinners = new HashSet<>();
                Individual parent1 = SelectionUtils.tournamentSelectionMono(population, App.tournamentSize,
                        previousWinners);
                previousWinners.add(parent1.getId());
                Individual parent2 = SelectionUtils.tournamentSelectionMono(population, App.tournamentSize,
                        previousWinners);

                // Cruzamento para gerar um novo filho
                Individual newSon = Crossover.onePointCrossing(parent1, parent2, clients);

                // Mutação
                Mutation.mutate(newSon, App.mutationRate);

                // Cálculo do fitness usando apenas o DefaultFitnessCalculator
                double fitness = new DefaultFitnessCalculator().calculateFitness(newSon, clients);
                newSon.setFitness(fitness);

                // Comparando o filho com a população atual e substituindo o pior se for melhor
                compareSonPopMono(newSon, population, nextPopulation, elitismSize);
            }
        }

        // Atualiza a população com os indivíduos da próxima geração
        updatePopMono(population, nextPopulation);

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

    public List<Individual> getIndividuals() {
        return this.individuals;
    }
}
