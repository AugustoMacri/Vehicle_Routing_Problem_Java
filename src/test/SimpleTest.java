package test;

import main.App;

public class SimpleTest {
    public static void main(String[] args) {
        App.WEIGHT_NUM_VIOLATIONS = 1000.0;
        int violations = 46;
        double distance = 2001.82;
        double fitness = distance + (violations * App.WEIGHT_NUM_VIOLATIONS);
        System.out.println("Weight: " + App.WEIGHT_NUM_VIOLATIONS);
        System.out.println("Violações: " + violations);
        System.out.println("Distância: " + distance);
        System.out.println("Fitness calculado: " + fitness);
    }
}
