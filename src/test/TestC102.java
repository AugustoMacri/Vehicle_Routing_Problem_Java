package test;

import java.util.List;
import genetic.*;
import main.App;
import vrp.Client;
import vrp.BenchMarkReader;
import vrp.ProblemInstance;

public class TestC102 {
    public static void main(String[] args) throws Exception {
        // Load C102
        BenchMarkReader reader = new BenchMarkReader();
        ProblemInstance instance = reader.readInstaces("src/instances/solomon/C102.txt");

        App.numVehicles = instance.getNumVehicles();
        App.vehicleCapacity = instance.getVehicleCapacity();
        App.numClients = instance.getClients().size();
        App.nextIndividualId = 1;
        App.WEIGHT_NUM_VIOLATIONS = 10000.0;

        System.out.println("=== Teste Solomon I1 - C102 ===");
        System.out.println("Veículos: " + App.numVehicles);
        System.out.println("Capacidade: " + App.vehicleCapacity);
        System.out.println("Clientes: " + (App.numClients - 1));
        System.out.println();

        // Create 5 individuals to test consistency
        for (int i = 0; i < 5; i++) {
            Individual ind = SolomonInsertion.createIndividual(
                    instance.getClients(),
                    App.vehicleCapacity,
                    App.numVehicles);

            // Check capacity violations
            int violationCount = 0;
            for (int v = 0; v < App.numVehicles; v++) {
                int demand = 0;
                int clientCount = 0;
                for (int c = 0; c < App.numClients; c++) {
                    int clientId = ind.getRoute()[v][c];
                    if (clientId == -1)
                        break;
                    demand += instance.getClients().get(clientId).getDemand();
                    clientCount++;
                }

                if (demand > App.vehicleCapacity) {
                    System.out.printf("❌ Indivíduo %d, Veículo %d: %d/%d (EXCEDE!)\n",
                            i + 1, v, demand, App.vehicleCapacity);
                    violationCount++;
                } else if (clientCount > 0) {
                    // System.out.printf(" Veículo %d: %d/%d ✓\n", v, demand, App.vehicleCapacity);
                }
            }

            if (violationCount == 0) {
                System.out.printf("✅ Indivíduo %d: TODAS as rotas respeitam capacidade\n", i + 1);
            }
        }
    }
}
