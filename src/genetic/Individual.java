package genetic;

import java.util.List;

import main.App;
import main.App.numClients;
import vrp.Client;

public class Individual {
    private int id;
    private int[][] route;
    private double fitness;
    private double fitnessDistance;
    private double fitnessTime;
    private double fitnessFuel;

    public Individual(int id, double fitness, double fitnessDistance, double fitnessTime,
            double fitnessFuel) {
        this.id = id;
        this.route = new int[App.numVehicles][App.numClients];
        this.fitness = fitness;
        this.fitnessDistance = fitnessDistance;
        this.fitnessTime = fitnessTime;
        this.fitnessFuel = fitnessFuel;
    }

    public void printRoutes() {
        for (int v = 0; v < App.numVehicles; v++) {
            System.out.print("Vehicle " + v + ": 0 → ");
            for (int c = 0; c < App.numClients; c++) {
                int clientId = this.route[v][c];
                if (clientId == -1) break;
                System.out.print(clientId + "");
            }
            System.out.println("0");
        }
        System.out.println();
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[][] getRoute() {
        return route;
    }

    public void setClientInRoute(int vehicle, int position, int clientId) {
        this.route[vehicle][position] = clientId;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitnessDistance() {
        return fitnessDistance;
    }

    public void setFitnessDistance(double fitnessDistance) {
        this.fitnessDistance = fitnessDistance;
    }

    public double getFitnessTime() {
        return fitnessTime;
    }

    public void setFitnessTime(double fitnessTime) {
        this.fitnessTime = fitnessTime;
    }

    public double getFitnessFuel() {
        return fitnessFuel;
    }

    public void setFitnessFuel(double fitnessFuel) {
        this.fitnessFuel = fitnessFuel;
    }

}
