package test;

import java.util.*;
import genetic.*;
import main.App;
import vrp.Client;

public class TestFitnessCalc {
    public static void main(String[] args) {
        System.out.println("App.WEIGHT_NUM_VIOLATIONS = " + App.WEIGHT_NUM_VIOLATIONS);

        // Create dummy client list
        List<Client> clients = new ArrayList<>();
        clients.add(new Client(0, 40, 50, 0, 0, 1000, 0)); // depot
        clients.add(new Client(1, 50, 50, 10, 0, 500, 10));
        clients.add(new Client(2, 60, 50, 10, 0, 400, 10)); // Will violate

        // Create individual with one route: depot -> client2 -> client1 -> depot
        // client2 will arrive at time ~20, but due=400 (no violation)
        // But we'll make client2 arrive late by design
        Individual ind = new Individual(1, 0, 0, 0, 0);
        int[][] routes = new int[5][3];
        for (int i = 0; i < 5; i++) {
            Arrays.fill(routes[i], -1);
        }
        routes[0][0] = 2; // This will be late
        routes[0][1] = 1;
        ind.setRoute(routes);

        // Calculate fitness
        DistanceFitnessCalculator calc = new DistanceFitnessCalculator();
        double fitness = calc.calculateFitness(ind, clients);

        System.out.println("Fitness calculated: " + fitness);
    }
}
