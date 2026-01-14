package test;

import java.util.List;
import genetic.*;
import main.App;
import vrp.Client;
import vrp.BenchMarkReader;
import vrp.ProblemInstance;

public class TestFitnessCalculation {
    public static void main(String[] args) throws Exception {
        // Load C101 (simpler instance)
        BenchMarkReader reader = new BenchMarkReader();
        ProblemInstance instance = reader.readInstaces("src/instances/solomon/C101.txt");

        App.numVehicles = instance.getNumVehicles();
        App.vehicleCapacity = instance.getVehicleCapacity();
        App.numClients = instance.getClients().size();
        App.nextIndividualId = 1;
        App.VEHICLE_SPEED = 1.0;
        App.WEIGHT_NUM_VIOLATIONS = 10000.0;

        System.out.println("=== Teste Fitness Calculation - C101 ===");
        System.out.println("Veículos: " + App.numVehicles);
        System.out.println("Capacidade: " + App.vehicleCapacity);
        System.out.println("Clientes: " + (App.numClients - 1));
        System.out.println("Penalidade por violação: " + App.WEIGHT_NUM_VIOLATIONS);
        System.out.println();

        // Create one individual
        Individual ind = SolomonInsertion.createIndividual(
                instance.getClients(),
                App.vehicleCapacity,
                App.numVehicles);

        // Calculate fitness
        DistanceFitnessCalculator distCalc = new DistanceFitnessCalculator();
        double distFitness = distCalc.calculateFitness(ind, instance.getClients());

        TimeFitnessCalculator timeCalc = new TimeFitnessCalculator();
        double timeFitness = timeCalc.calculateFitness(ind, instance.getClients());

        FuelFitnessCalculator fuelCalc = new FuelFitnessCalculator();
        double fuelFitness = fuelCalc.calculateFitness(ind, instance.getClients());

        System.out.println("Fitness calculado:");
        System.out.println("  Distance: " + String.format("%.2f", distFitness));
        System.out.println("  Time: " + String.format("%.2f", timeFitness));
        System.out.println("  Fuel: " + String.format("%.2f", fuelFitness));
        System.out.println();

        // Estimate violations
        double distViolations = (distFitness > 5000) ? (distFitness - 1500) / 10000.0 : 0;
        double timeViolations = (timeFitness > 5000) ? (timeFitness - 1500) / 10000.0 : 0;
        double fuelViolations = (fuelFitness > 5000) ? (fuelFitness - 1500) / 10000.0 : 0;

        System.out.println("Violações estimadas:");
        System.out.println("  Distance calc: ~" + String.format("%.1f", distViolations) + " violações");
        System.out.println("  Time calc: ~" + String.format("%.1f", timeViolations) + " violações");
        System.out.println("  Fuel calc: ~" + String.format("%.1f", fuelViolations) + " violações");
    }
}
