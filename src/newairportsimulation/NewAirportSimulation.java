package newairportsimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class NewAirportSimulation {
    private static final int NUM_PLANES = 6;
    
    public static void main(String[] args) {
        ATCTower tower = new ATCTower();
        Thread towerThread = new Thread(tower, "ATCTower");
        towerThread.start();
        
        RefuelTruck refuelTruck = new RefuelTruck();
        
        List<Thread> planes = new ArrayList<>();
        Random rand = new Random();      
        
        boolean emergencyOccurred = false;    
        
        for (int i = 1; i <= NUM_PLANES; i++) {
            boolean emergency;
            if (!emergencyOccurred && i < NUM_PLANES) {
                emergency = rand.nextInt(100) < 15; // 15% chance of emergency if no emergency occurred yet
                if (emergency) {
                    emergencyOccurred = true;
                }
            } else {
                emergency = !emergencyOccurred; // Last plane will have 100% chance if no emergency occurred yet
            }    
        
            Plane plane = new Plane(tower, refuelTruck, emergency);
            Thread planeThread = new Thread(plane);
            planes.add(planeThread);
        }
        
        for (Thread plane : planes) {
            plane.start();
            try {
                Thread.sleep(rand.nextInt(2000)); // Simulate random arrival times (0, 1, or 2 seconds)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        planes.forEach(plane -> {
            try {
                plane.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
