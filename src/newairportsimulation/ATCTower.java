package newairportsimulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class ATCTower implements Runnable {
    private final String towerName = "ATCTower";
    private final Lock runwayLock = new ReentrantLock();
    private final Gate[] gates = new Gate[3];
    private final RefuelTruck refuelTruck = new RefuelTruck();
    private int planesOnGround = 0;
    private int planeTookOff = 0;
    private boolean emergencyOccurred = false;
    private final Object planeTookOffObj = new Object();
    private final List<Long> waitingTimes = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> passengersBoarded = Collections.synchronizedList(new ArrayList<>());
    
//    private boolean emergencyLandingGranted = false;

    public ATCTower() {
        for (int i = 0; i < gates.length; i++) {
            gates[i] = new Gate(i + 1, refuelTruck);  
        }
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName("ATC");
        System.out.println("ATC Tower is operational.");
        synchronized(planeTookOffObj){
            while(planeTookOff < 6){
                try {
                    planeTookOffObj.wait(); // Wait until all planes have taken off
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }performSanityChecks();
        }
    }
    
    public boolean responseLanding(Plane plane, boolean emergency) throws InterruptedException {
            while (true) {
            boolean runwayAcquired = runwayLock.tryLock();
            if (emergency) {
                if (planesOnGround >= 3 || !runwayAcquired) {
                    if (runwayAcquired) {
                        runwayLock.unlock();
                    }
                    return false;  
                } else {
                    try {
                        planesOnGround++;
                        emergencyOccurred = true;
                        System.out.println(towerName + ": Emergency landing permission granted for Plane " + plane.getId() 
                                + ". Planes on ground: " + planesOnGround);
                        return true;
                    } finally {
                        if (!runwayAcquired) {
                            runwayLock.unlock();
                        }
                    }
                }
            } else {
                if (!runwayAcquired || planesOnGround >= 3) {
                    if (runwayAcquired) {
                        runwayLock.unlock();
                    }if(planesOnGround >= 3){
                        System.out.println(towerName + ": Permission to land denied for Plane " + plane.getId() + ". Maximum planes on ground");
                        Thread.sleep(3000);
                        return false;  // Plane should retry later
                    }else if(!runwayAcquired){
                        System.out.println(towerName + ": Permission to land denied for Plane " + plane.getId() + ". Waiting for runway");
                        Thread.sleep(3000);
                        return false;  // Plane should retry later    
                    }
                } else {
                    try {
                        planesOnGround++;
                        System.out.println(towerName + ": Permission to land granted for Plane " + plane.getId() + ". Planes on ground: " + planesOnGround);
                        return true;
                    } finally {
                        if (!runwayAcquired) {
                            runwayLock.unlock();
                        }
                    }
                }
            }
        }
    }
    
    
    public synchronized boolean responseTakeOff(Plane plane) throws InterruptedException {
        boolean runwayAcquired = runwayLock.tryLock();
        if (!runwayAcquired) {
            System.out.println(towerName + ": Permission to take off is denied for Plane " + plane.getId() + ". Waiting for runway");
            return false;
        }
        try {
            System.out.println(towerName + ": Permission to take off is granted for Plane " + plane.getId());
//            plane.recordTakeOffTime();
            return true;
        } finally {
//            runwayLock.unlock();
        }
    }
    
    public boolean hasEmergencyOccurred(){
        return emergencyOccurred;
    }
    

    public void moveToGate(Plane plane) throws InterruptedException {
        synchronized (this) {
            Gate assignedGate = assignGate();
            if (assignedGate != null) {
                 System.out.println(towerName + ": Plane" + plane.getId() + " is assigned to gate " + assignedGate.getId());
                plane.assignGate(assignedGate);
            }
            notifyAll();
        }
    }
    
    public synchronized Gate assignGate() {
        for (Gate gate : gates) {
            if (!gate.isOccupied()) {
                gate.occupy();
                return gate;
            }
        }
        return null;
    }

    public synchronized boolean releaseAfterLanding() {
    runwayLock.unlock();
    System.out.println(towerName + ": Runway available after landing.");
    return true;
    }

    public synchronized void planeTookOff() {
        
        planesOnGround--;
        System.out.println(towerName + ": Runway available after takeoff. Planes on ground: " + planesOnGround);
        runwayLock.unlock();
        
        synchronized(planeTookOffObj){
        planeTookOff++;
        planeTookOffObj.notifyAll();
        }
    }
    
    public void recordStatistics(Plane plane, Gate gate) {
        waitingTimes.add(plane.getWaitingTime());
        passengersBoarded.add(gate.getPassengersOnboard());
    }
    
    public void performSanityChecks() {
        System.out.println("\n" + Thread.currentThread().getName() +": Performing sanity checks.");
        boolean gateEmpty = true;
        for(Gate emptyGate:gates){
            if(emptyGate.isOccupied() == true){
                gateEmpty = false;
                break;
            }
        }
        System.out.println("All gates empty: " + gateEmpty);

        long maxWaitingTime = Collections.max(waitingTimes);
        long minWaitingTime = Collections.min(waitingTimes);
        long totalWaitingTime = 0;
        for (Long time : waitingTimes) {
            totalWaitingTime += time;
        }
        long avgWaitingTime = totalWaitingTime / waitingTimes.size();

        int totalPassengers = passengersBoarded.stream().mapToInt(Integer::intValue).sum();

        System.out.println("Maximum waiting time: " + maxWaitingTime + " ms");
        System.out.println("Minimum waiting time: " + minWaitingTime + " ms");
        System.out.println("Average waiting time: " + avgWaitingTime + " ms");
        System.out.println("Number of planes served: " + planeTookOff);
        System.out.println("Total passengers boarded: " + totalPassengers);
    }
    
    public String getName(){
        return towerName;
    }
}
