package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import genetic.*;
import main.App;
import vrp.Client;

public class TestC101Debug {
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
            App.WEIGHT_NUM_VIOLATIONS = 10000.0;

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

            // Create one individual with Solomon I1
            Individual ind = SolomonInsertion.createIndividual(clients, capacity, numVehicles);

            // Manual validation
            Client depot = clients.get(0);
            int totalViolations = 0;
            double totalDistance = 0;

            for (int v = 0; v < numVehicles; v++) {
                List<Integer> route = new ArrayList<>();
                for (int i = 0; i < App.numClients; i++) {
                    int client = ind.getRoute()[v][i];
                    if (client == -1)
                        break;
                    route.add(client);
                }

                if (route.isEmpty())
                    continue;

                System.out.println("\n=== Veículo " + (v + 1) + " ===");
                System.out.println("Clientes: " + route.size());

                double currentTime = 0;
                double vehicleDist = 0;
                int violations = 0;

                // Depot to first
                Client first = clients.get(route.get(0));
                double dist = Math
                        .sqrt(Math.pow(first.getX() - depot.getX(), 2) + Math.pow(first.getY() - depot.getY(), 2));
                currentTime += dist;
                vehicleDist += dist;

                if (currentTime < first.getReadyTime()) {
                    currentTime = first.getReadyTime();
                }
                if (currentTime > first.getDueTime()) {
                    violations++;
                    System.out.println(
                            "❌ Cliente " + first.getId() + ": chegada=" + currentTime + " > due=" + first.getDueTime());
                } else {
                    System.out.println("✅ Cliente " + first.getId() + ": chegada=" + currentTime + " ∈ ["
                            + first.getReadyTime() + ", " + first.getDueTime() + "]");
                }
                currentTime += first.getServiceTime();

                // Between clients
                for (int i = 0; i < route.size() - 1; i++) {
                    Client curr = clients.get(route.get(i));
                    Client next = clients.get(route.get(i + 1));
                    dist = Math.sqrt(Math.pow(next.getX() - curr.getX(), 2) + Math.pow(next.getY() - curr.getY(), 2));
                    currentTime += dist;
                    vehicleDist += dist;

                    if (currentTime < next.getReadyTime()) {
                        currentTime = next.getReadyTime();
                    }
                    if (currentTime > next.getDueTime()) {
                        violations++;
                        System.out.println("❌ Cliente " + next.getId() + ": chegada=" + currentTime + " > due="
                                + next.getDueTime());
                    } else {
                        System.out.println("✅ Cliente " + next.getId() + ": chegada=" + currentTime + " ∈ ["
                                + next.getReadyTime() + ", " + next.getDueTime() + "]");
                    }
                    currentTime += next.getServiceTime();
                }

                // Last to depot
                Client last = clients.get(route.get(route.size() - 1));
                dist = Math.sqrt(Math.pow(depot.getX() - last.getX(), 2) + Math.pow(depot.getY() - last.getY(), 2));
                currentTime += dist;
                vehicleDist += dist;

                System.out.println("Distância: " + vehicleDist);
                System.out.println("Tempo final: " + currentTime);
                System.out.println("Violações: " + violations);

                totalDistance += vehicleDist;
                totalViolations += violations;
            }

            System.out.println("\n=== TOTAIS ===");
            System.out.println("Distância total: " + totalDistance);
            System.out.println("Violações totais: " + totalViolations);
            System.out.println("Fitness esperado: " + (totalDistance + totalViolations * 10000));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
