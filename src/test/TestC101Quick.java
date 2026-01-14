package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import genetic.*;
import main.App;
import vrp.Client;

public class TestC101Quick {
    public static void main(String[] args) {
        try {
            // Load C101 instance
            String filename = "src/instances/solomon/C101.txt";
            List<Client> clients = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine(); // header
            line = reader.readLine(); // empty
            line = reader.readLine(); // empty
            line = reader.readLine(); // empty
            line = reader.readLine(); // vehicle data

            String[] parts = line.trim().split("\\s+");
            int numVehicles = Integer.parseInt(parts[0]);
            int capacity = Integer.parseInt(parts[1]);

            App.numVehicles = numVehicles;
            App.vehicleCapacity = capacity;
            App.VEHICLE_SPEED = 1;
            // App.WEIGHT_NUM_VIOLATIONS is already set in App.java

            line = reader.readLine(); // empty
            line = reader.readLine(); // empty
            line = reader.readLine(); // empty
            line = reader.readLine(); // column headers

            // Read clients
            int clientCount = 0;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                parts = line.trim().split("\\s+");
                int id = Integer.parseInt(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int demand = Integer.parseInt(parts[3]);
                int ready = Integer.parseInt(parts[4]);
                int due = Integer.parseInt(parts[5]);
                int service = Integer.parseInt(parts[6]);

                clients.add(new Client(id, x, y, demand, ready, due, service));
                clientCount++;
            }
            reader.close();

            App.numClients = clientCount - 1;
            System.out.println("=== Test C101 Quick ===");
            System.out.println("Clientes carregados: " + (clientCount - 1));
            System.out.println("Capacidade: " + capacity);
            System.out.println("Veículos: " + numVehicles);
            System.out.println();

            // Create one individual with Solomon I1
            System.out.println("Criando indivíduo com Solomon I1...");
            Individual ind = SolomonInsertion.createIndividual(clients, capacity, numVehicles);

            // Calculate fitness
            System.out.println("Calculando fitness...");
            DistanceFitnessCalculator distCalc = new DistanceFitnessCalculator();
            TimeFitnessCalculator timeCalc = new TimeFitnessCalculator();
            FuelFitnessCalculator fuelCalc = new FuelFitnessCalculator();

            double distFit = distCalc.calculateFitness(ind, clients);
            double timeFit = timeCalc.calculateFitness(ind, clients);
            double fuelFit = fuelCalc.calculateFitness(ind, clients);

            ind.setFitnessDistance(distFit);
            ind.setFitnessTime(timeFit);
            ind.setFitnessFuel(fuelFit);

            System.out.println("\n=== RESULTADOS ===");
            System.out.println("Fitness Distance: " + distFit);
            System.out.println("Fitness Time: " + timeFit);
            System.out.println("Fitness Fuel: " + fuelFit);

            // Count routes and validate
            int routesUsed = 0;
            Set<Integer> visited = new HashSet<>();
            for (int v = 0; v < numVehicles; v++) {
                boolean hasClients = false;
                for (int i = 0; i < App.numClients; i++) {
                    int client = ind.getRoute()[v][i];
                    if (client == -1)
                        break;
                    hasClients = true;
                    visited.add(client);
                }
                if (hasClients)
                    routesUsed++;
            }

            System.out.println("\nVeículos usados: " + routesUsed);
            System.out.println("Clientes visitados: " + visited.size() + "/" + (clientCount - 1));

            if (visited.size() == clientCount - 1) {
                System.out.println("✅ Todos clientes visitados!");
            } else {
                System.out.println("❌ Clientes faltando!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
