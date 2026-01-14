package test;

import java.util.List;
import genetic.*;
import main.App;
import vrp.Client;
import vrp.BenchMarkReader;
import vrp.ProblemInstance;

public class TestSolomon {
    public static void main(String[] args) throws Exception {
        // Load R101
        BenchMarkReader reader = new BenchMarkReader();
        ProblemInstance instance = reader.readInstaces("src/instances/solomon/R101.txt");

        App.numVehicles = instance.getNumVehicles();
        App.vehicleCapacity = instance.getVehicleCapacity();
        App.numClients = instance.getClients().size();
        App.nextIndividualId = 1;

        System.out.println("=== Teste Solomon I1 ===");
        System.out.println("Veículos: " + App.numVehicles);
        System.out.println("Capacidade: " + App.vehicleCapacity);
        System.out.println("Clientes: " + (App.numClients - 1) + " (+ depósito)");
        System.out.println();

        // Create one individual
        Individual ind = SolomonInsertion.createIndividual(
                instance.getClients(),
                App.vehicleCapacity,
                App.numVehicles);

        // Count clients in routes
        int clientCount = 0;
        int usedVehicles = 0;
        for (int v = 0; v < App.numVehicles; v++) {
            int vehicleClients = 0;
            for (int c = 0; c < App.numClients; c++) {
                if (ind.getRoute()[v][c] == -1)
                    break;
                vehicleClients++;
                clientCount++;
            }
            if (vehicleClients > 0)
                usedVehicles++;
        }

        System.out.println("Resultado:");
        System.out.println("  Clientes roteados: " + clientCount + "/100");
        System.out.println("  Veículos usados: " + usedVehicles);

        if (clientCount < 100) {
            System.out.println("  ❌ FALTAM " + (100 - clientCount) + " clientes!");
        } else {
            System.out.println("  ✅ Todos os clientes roteados!");
        }
    }
}
