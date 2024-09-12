package newairportsimulation;

import java.util.concurrent.Semaphore;

public class RefuelTruck {

    private final Semaphore semaphore = new Semaphore(1);

    void refuelPlane(Plane plane) throws InterruptedException {
        try {
            semaphore.acquire();
            System.out.println("Refuel Truck: moving to Plane " + plane.getId());
            Thread.sleep(2000); // Simulate refueling time
            System.out.println("Refuel Truck: Refueling Plane " + plane.getId());
            Thread.sleep(2000); // Simulate refueling time
            System.out.println("Refuel Truck: Plane " + plane.getId() + " has been refueled.");
        } catch (InterruptedException e) {
            System.out.println("RefuelTruck: Refueling interrupted for Plane " + plane.getId());
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release(); // Release permit back to semaphore
        }
    }
}

//    private BlockingQueue<Plane> refuelQueue;
//    
//    public RefuelTruck(){
//        this.refuelQueue = new LinkedBlockingQueue<>();
//    } 
//    public void addPlaneToRefuelQueue(Plane plane) {
//        try {
//            refuelQueue.put(plane); // Add plane to the refuel queue
//        } catch (InterruptedException e) {
//            System.out.println("Interrupted while adding plane to refuel queue.");
//            Thread.currentThread().interrupt();
//        }
//    }

