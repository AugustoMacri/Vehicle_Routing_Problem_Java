package genetic;

import java.util.List;

import vrp.Client;

public class Individual {
    private int id;
    private List<Client> route;
    private double fitness;
    private double fitnessDistance;
    private double fitnessTime;
    private double fitnessFuel;

    public Individual(int id, List<Client> route, double fitness, double fitnessDistance, double fitnessTime,
            double fitnessFuel) {
        this.id = id;
        this.route = route;
        this.fitness = fitness;
        this.fitnessDistance = fitnessDistance;
        this.fitnessTime = fitnessTime;
        this.fitnessFuel = fitnessFuel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Client> getRoute() {
        return route;
    }

    public void setRoute(List<Client> route) {
        this.route = route;
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
