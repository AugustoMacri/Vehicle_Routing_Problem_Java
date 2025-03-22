package main;

import java.io.IOException;

import vrp.BenchMarkReader;
import vrp.Client;
import vrp.ProblemInstance;
import configuration.*;

public class App {
    public static void main(String[] args) throws Exception {

        BenchMarkReader reader = new BenchMarkReader();

        try {
            ProblemInstance instance = reader.readInstaces("src/instances/solomon/C101.txt");
            System.out.println(("Number of vehicles: " + instance.getNumVehicles()));
            System.out.println(("Vehicle capacity: " + instance.getVehicleCapacity()));
            System.out.println(("Number of clients: " + instance.getClients().size()));

            // Printing the clients
            for (int i = 0; i < Math.min(5, instance.getClients().size()); i++) {
                System.out.println(instance.getClients().get(i));
            }

            System.out.println("Client 0: " + instance.getClients().get(0));

        } catch (IOException e) {
            System.out.println("Error reading the file");
            e.printStackTrace();
        }
    }
}
