import java.util.*;
import java.util.LinkedList;
import java.util.Queue;

public class RoundRobinProcess {

    String name;
    int ArrTime; //arrival
    int BurstTime; //burst
    int waitTime; //waiting time for each process 7
    int TaTime;//turnAround time
    int completionTime;
    int remainingTime; //remaining time

    /*constructor creates a process with its detailss the name
    when it arrives and how long it needss to run and the priority level*/

    public RoundRobinProcess(String name,int ArrTime, int BurstTime, int priority){
        this.name= name;
        this.ArrTime = ArrTime;
        this.BurstTime = BurstTime;
        this.remainingTime = BurstTime;
        this.TaTime = 0;
        this.completionTime = 0;
        this.waitTime = 0;


    }
    //some setters and getters
    public String getName() {
        return name;
    }
    public int getArrTime() {
        return ArrTime;
    }
    public int getBurstTime() {
        return BurstTime;
    }
    public int getWaitTime() {
        return waitTime;
    }
    public int getTaTime() {
        return TaTime;
    }
    public int getCompletionTime() {
        return completionTime;
    }
    public int getRemainingTime() {
        return remainingTime;
    }
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
    public void setTaTime(int taTime) {
        this.TaTime = taTime;
    }
    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
    public boolean isCompleted() {
        return remainingTime == 0;
    }

    public String toString() {
        return "Process{name='" + name + "', arrival=" + ArrTime + ", burst=" + BurstTime +
                ", remaining=" + remainingTime ;
    }


}
//this holds results of rr
class RoundRobinResult {
    //excution order
    private List<String> excutionOrder;//order of p excution
    private List<RoundRobinProcess> processes;//final state
    private double avgWaitTime;// avg waiting
    private double avgTaTime;// avg turnaround

    public RoundRobinResult(List<String> excutionOrder, List<RoundRobinProcess> processes) {
        this.excutionOrder = excutionOrder;
        this.processes = processes;
        this.avgWaitTime = 0.0;
        this.avgTaTime = 0.0;

    }

    public List<String> getExcutionOrder() {
        return excutionOrder;
    }

    public void setExcutionOrder(List<String> excutionOrder) {
        this.excutionOrder = excutionOrder;
    }

    public void addExcutionOrder(String excutionOrder) {
        this.excutionOrder.add(excutionOrder);
    }

    public List<RoundRobinProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<RoundRobinProcess> processes) {
        this.processes = processes;
    }

    public double getAvgWaitTime() {
        return avgWaitTime;
    }

    public void setAvgWaitTime(double avgWaitTime) {
        this.avgWaitTime = avgWaitTime;
    }

    public double getAvgTaTime() {
        return avgTaTime;
    }

    public void setAvgTaTime(double avgTaTime) {
        this.avgTaTime = avgTaTime;
    }

    public void calcAvgWaitTime() {
        if (processes.isEmpty() || processes==null) {
            avgWaitTime = 0.0;
            avgTaTime = 0.0;
            return;
        }
        double totalWaitTime = 0;
        double totalTaTime = 0;

        for (RoundRobinProcess p : processes) {
            totalWaitTime += p.getWaitTime();
            totalTaTime += p.getTaTime();
        }
        avgWaitTime = totalWaitTime / processes.size();
        avgTaTime = totalTaTime / processes.size();

    }
}

//heres all the logic for scheduling algo
class RoundRobinSchedule{
    public static  RoundRobinResult schedule(
            List<RoundRobinProcess>processList,
            int quantum,
            int contextSwitchTime){
        Queue<RoundRobinProcess> queue = new LinkedList<>();
        List<String>excutionOrder = new ArrayList<>();


        int currentTime = 0;
        int completed=0;
        int n= processList.size();
        int i=0;

        processList.sort(Comparator.comparing(RoundRobinProcess::getArrTime));

        String lastProcessName="";

        while (completed<n){
            while(i<n&&processList.get(i).getArrTime()<= currentTime){
                queue.add(processList.get(i));
                i++;
            }
            //if no process is ready , cpu idle
            if(queue.isEmpty()){
                currentTime++;
                continue;
            }
            RoundRobinProcess current=queue.poll();
            if(!lastProcessName.isEmpty()&&!lastProcessName.equals(current.getName())){
                currentTime+=contextSwitchTime;
            }

            int exTime=Math.min(quantum,current.getRemainingTime());

            for(int m=0;m<exTime;m++) {
                if (excutionOrder.isEmpty() || !excutionOrder.get(excutionOrder.size() - 1).equals(current.getName())) {
                    excutionOrder.add(current.getName());
                }

                currentTime++;
                current.setRemainingTime(current.getRemainingTime() - 1);


                while (i < n && processList.get(i).getArrTime() <= currentTime) {
                    queue.add(processList.get(i));
                    i++;
                }
            }
            if (current.isCompleted()){
                completed++;
                current.setCompletionTime(currentTime);
                current.setTaTime(current.getCompletionTime()-current.getArrTime());
                current.setWaitTime(current.getTaTime()-current.getBurstTime());
            }
            else{ queue.add(current); }

            lastProcessName=current.getName();
        }

//creates obj of result and calc average
        RoundRobinResult result=new RoundRobinResult(excutionOrder,processList);
        result.calcAvgWaitTime();
        return result;}


}




















