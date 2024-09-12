package newairportsimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Gate implements Runnable{
    private int id;
    private boolean occupied = false;
    private int passengers;
    private RefuelTruck refuelTruck;
    private List<Plane> planeQueue = new ArrayList<>();
    private static final Random rand = new Random();

    public Gate(int id, RefuelTruck refuelTruck) {
        this.id = id;
        this.refuelTruck = refuelTruck;
        
    }
    
    @Override
    public void run() {
        while (true) {
            if (!planeQueue.isEmpty()) {
                Plane plane = planeQueue.remove(0);
                try {
                    handleOperations(plane);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized boolean isOccupied() {
        return occupied;
    }

    public synchronized void occupy() {
        occupied = true;
    }
    
    boolean dockPlane(Plane plane) throws InterruptedException {
        
        System.out.println("Gate " + id + ": Plane" + plane.getId() + " is docking at gate " + id);
        Thread.sleep(2000); // Simulate docking time
        System.out.println("Gate " + id + ": Plane" + plane.getId() + " has docked at gate " + id);
        occupy();
        return true;
    }
    
    public void handleOperations(Plane plane) throws InterruptedException {
        Thread disembarkThread = new Thread(() -> {
            try {
                disembarkPassengers(plane);
                refillSuppliesandCleaning(plane);
                embarkPassengers(plane);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread refuelThread = new Thread(() -> {
            try {
                refuelTruck.refuelPlane(plane);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        disembarkThread.start();
        refuelThread.start();
        // Wait for both operations to complete
        disembarkThread.join();
        refuelThread.join();
    }
    
    private void disembarkPassengers(Plane plane) throws InterruptedException {
        System.out.println("Gate " + id + ": Disembarking passengers from Plane " + plane.getId());
        Thread.sleep(2000);
        System.out.println("Gate " + id + ": Finished disembarking passengers from Plane " + plane.getId());
    }
    
    private void refillSuppliesandCleaning(Plane plane) throws InterruptedException {
        System.out.println("Gate " + id + ": Refilling supplies and cleaning for Plane " + plane.getId());
        Thread.sleep(2000);
        System.out.println("Gate " + id + ": Finished refilling supplies and cleaning for Plane " + plane.getId());
    }
    
    private void embarkPassengers(Plane plane) throws InterruptedException {
        passengers = generateRandomPassengerCount();
        System.out.println("Gate " + id + ": Passengers embarking to Plane " + plane.getId());
        Thread.sleep(2000);
        System.out.println("Gate " + id + ": Finished embarking passengers to Plane " + plane.getId() + ". Passengers boarded: " + passengers);
    }
    
    public void undockPlane(Plane plane){
        try{
            System.out.println("Gate " + id + ": Undocking plane " + plane.getId());
            Thread.sleep(1000);
            System.out.println("Gate " + id + ": Undocked plane " + plane.getId());
            release();
        } catch (InterruptedException e) {
            System.out.println("Gate " + id + ": " + "Undocked interrupted for " + plane.getId() + " at gate " + id);
            Thread.currentThread().interrupt();
        }
    }
    
    public synchronized void release() {
        this.occupied = false;
        System.out.println("Gate " + id + " is now free.");
    }

    public int getId() {
        return id;
    }
    
    private int generateRandomPassengerCount() {
        return rand.nextInt(41) + 10; // Generates a random number between 10 and 50
    }
    
    public int getPassengersOnboard() {
        return passengers;
    }
    
    public void addPlaneToQueue(Plane plane) {
        planeQueue.add(plane);
    }
}
