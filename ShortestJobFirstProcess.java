import java.util.*;

public class ShortestJobFirstProcess {

    public static class Process {
        public String name;
        public int arrivaltime;
        public int bursttime;
        public int priority;
        public int remainingtime;
        public int finishtime;
        public int waitingtime;
        public int turnaroundtime;

        public Process(String name, int arrival, int burst, int priority) {
            this.name = name;
            this.arrivaltime = arrival;
            this.bursttime = burst;
            this.priority = priority;
            this.remainingtime = burst;
        }
    }

    public static class SJFResult {
        public List<String> executionOrder;
        public List<Process> processes;
        public double averageWaitingTime;
        public double averageTurnaroundTime;

        public SJFResult(List<String> order, List<Process> processes,
                         double avgWT, double avgTAT) {
            this.executionOrder = order;
            this.processes = processes;
            this.averageWaitingTime = avgWT;
            this.averageTurnaroundTime = avgTAT;
        }
    }

    public static SJFResult schedule(List<Process> processes, int contextSwitch) {

        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        Process currentProcess = null;

        List<String> executionOrder = new ArrayList<>();

        // Sort by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivaltime));

        while (completed < n) {

            List<Process> readyQueue = new ArrayList<>();
            for (Process p : processes) {
                if (p.arrivaltime <= currentTime && p.remainingtime > 0) {
                    readyQueue.add(p);
                }
            }

            // CPU idle
            if (readyQueue.isEmpty()) {
                currentTime++;
                continue;
            }

            // Shortest remaining time
            readyQueue.sort((p1, p2) -> {
                if (p1.remainingtime != p2.remainingtime)
                    return p1.remainingtime - p2.remainingtime;
                return p1.arrivaltime - p2.arrivaltime;
            });

            Process bestProcess = readyQueue.get(0);

            // Context switch
            if (bestProcess != currentProcess) {
                if (currentProcess != null)
                    currentTime += contextSwitch;

                currentProcess = bestProcess;
                executionOrder.add(currentProcess.name);
            }

            // Execute 1 time unit
            currentProcess.remainingtime--;
            currentTime++;

            // Finish
            if (currentProcess.remainingtime == 0) {
                currentProcess.finishtime = currentTime;
                completed++;
            }
        }

        double totalWait = 0;
        double totalTurnaround = 0;

        for (Process p : processes) {
            p.turnaroundtime = p.finishtime - p.arrivaltime;
            p.waitingtime = p.turnaroundtime - p.bursttime;
            totalWait += p.waitingtime;
            totalTurnaround += p.turnaroundtime;
        }

        return new SJFResult(
                executionOrder,
                processes,
                totalWait / n,
                totalTurnaround / n
        );
    }
}
