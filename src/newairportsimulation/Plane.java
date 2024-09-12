package newairportsimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Plane implements Runnable {
    private static final int MAX_PASSENGERS = 50;
    private static final List<Integer> availableIds = new ArrayList<>();
    static {
        for (int i = 1; i <= 6; i++) {
            availableIds.add(i);
        }
    }
    private int id;
    private Gate[] gates;
    private RefuelTruck refuelTruck;
    private ATCTower tower;
    private boolean emergency;
    private static Random rand = new Random();
    private Gate assignedGate;
    private int emergencyRequest = 0;
    private long requestLandingTime;
    private long startTime;
    private long waitingTime;
//    private boolean landed = false; // Flag to track if the plane has landed


    public Plane(ATCTower tower, RefuelTruck refuelTruck, boolean emergency) {
        int index = rand.nextInt(availableIds.size());
        this.id = availableIds.get(index);
        availableIds.remove(index);
        this.tower = tower;
//        this.gates = gates;
        this.refuelTruck = refuelTruck;
        this.emergency = emergency;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Plane " + id);
        try {
            while(true){
                while (!requestLanding()) {
                    Thread.sleep(rand.nextInt(1000) + 3000); // Wait before retrying
                }
                boolean permission = tower.responseLanding(this, emergency);
                if (permission) {
                    planeLanding();
                    tower.moveToGate(this);
                    movingGate();
                    assignedGate.addPlaneToQueue(this);                 
                    assignedGate.handleOperations(this);
                    while (!requestTakeOff()) {
                        Thread.sleep(rand.nextInt(1000) + 3000); // Wait before retrying take-off
                    }
                    planeTakeOff();
                    waitingTime = System.currentTimeMillis() - startTime;
                    tower.recordStatistics(this, assignedGate);
                    tower.planeTookOff();
                    break;
                }
            }
        } catch (InterruptedException e) {
            System.out.println(id + " encountered an issue.");
        }
    }
    
    public boolean requestLanding() throws InterruptedException {
        if (emergency == true) {
            return requestEmergencyLanding();
        } else {
            System.out.println(Thread.currentThread().getName() + ": Requesting permission to land.");
            Thread.sleep(rand.nextInt(1000) + 1500);
            return true;
        }
    }
    
    private boolean requestEmergencyLanding() throws InterruptedException {
        if(emergencyRequest < 1){
            System.out.println(Thread.currentThread().getName() + ": Requesting emergency landing due to fuel shortage.");
            emergencyRequest++;
        }else{
        }
        return true;
    }
    
    private boolean movingGate() throws InterruptedException{
        if (assignedGate != null) {
            System.out.println("Plane "+ id + ": Moving to gate " + assignedGate.getId());
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName() + ": Docking at gate " + assignedGate.getId());
            tower.releaseAfterLanding();
            assignedGate.dockPlane(this);
        }return true;
    }

    private void planeLanding() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + " is landing.");
        Thread.sleep(rand.nextInt(2000) + 1000); // Landing time
        System.out.println(Thread.currentThread().getName() + " has landed.");
        Thread.sleep(rand.nextInt(2000) + 1000);
    }
    
    private boolean requestTakeOff() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + ": Requesting permission to take off.");
        boolean permission = tower.responseTakeOff(this);
        if (permission) {
            assignedGate.undockPlane(this);
            System.out.println(Thread.currentThread().getName() + " is moving from gate " + assignedGate.getId() + " to runway.");
            return true;
        } 
        return false;
    }
    private void planeTakeOff() throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ": Taking off.");
        Thread.sleep(rand.nextInt(2000) + 1000);
        System.out.println(Thread.currentThread().getName() + ": Taken off.");
    }
    
    public long getWaitingTime() {
        return waitingTime;
    }
    
    public int getNumberOfPassengers() {
        return MAX_PASSENGERS;
    }
    
    public void assignGate(Gate gate) {
        this.assignedGate = gate;
    }
    
    public ATCTower getATCTower() {
        return tower;
    }
    
    public int getId() {
        return id;
    }
    
}
