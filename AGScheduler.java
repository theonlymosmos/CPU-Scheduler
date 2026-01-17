import java.util.*;

public class AGScheduler {



    // ===== Per-process result =====
    public static class AGProcessResult {
        public String name;
        public int waitingTime;
        public int turnaroundTime;
        public List<Integer> quantumHistory;

        public AGProcessResult(Task t) {
            this.name = t.name;
            this.waitingTime = t.waitingTime;
            this.turnaroundTime = t.finishTime - t.arrival;
            this.quantumHistory = new ArrayList<>(t.history);
        }
    }

    // ===== Overall schedule result =====
    public static class AGScheduleResult {
        public List<String> executionOrder;
        public List<AGProcessResult> processResults;
        public double averageWaitingTime;
        public double averageTurnaroundTime;
    }

    // ===== Scheduler =====
    public static AGScheduleResult runSchedule(List<Task> tasks) {
        int timer = 0;
        List<Task> waitingList = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();

        Task currentJob = null;
        int currentUsage = 0;

        tasks.sort(Comparator.comparingInt(t -> t.arrival));
        int totalTasks = tasks.size();

        // Load tasks arriving at t=0
        for (Task t : tasks)
            if (t.arrival == 0) waitingList.add(t);

        while (completedTasks.size() < totalTasks) {

            // CPU idle case
            if (currentJob == null) {
                if (!waitingList.isEmpty()) {
                    currentJob = waitingList.remove(0);
                    executionOrder.add(currentJob.name);
                    currentUsage = 0;
                } else {
                    timer++;
                    checkNewArrivals(tasks, waitingList, timer);
                    continue;
                }
            }

            // Execute one tick
            currentJob.remainingTime--;
            currentUsage++;
            timer++;

            checkNewArrivals(tasks, waitingList, timer);

            // Task finished
            if (currentJob.remainingTime == 0) {
                currentJob.finishTime = timer;
                currentJob.waitingTime = currentJob.finishTime - currentJob.arrival - currentJob.burst;
                currentJob.quantum = 0;
                currentJob.history.add(0);

                completedTasks.add(currentJob);
                currentJob = null;
                currentUsage = 0;
                continue;
            }

            int q = currentJob.quantum;
            int limit25 = (int) Math.ceil(q * 0.25);
            int limit50 = limit25 * 2;

            // Quantum end
            if (currentUsage == q) {
                currentJob.quantum += 2;
                currentJob.history.add(currentJob.quantum);
                waitingList.add(currentJob);
                currentJob = null;
                continue;
            }

            // 25% Quantum: Priority preemption
            if (currentUsage == limit25) {
                Task bestPriority = findBestPriority(waitingList);
                if (bestPriority != null && bestPriority.priority < currentJob.priority) {
                    int unused = q - currentUsage;
                    currentJob.quantum += (int) Math.ceil(unused / 2.0);
                    currentJob.history.add(currentJob.quantum);

                    waitingList.add(currentJob);
                    waitingList.remove(bestPriority);
                    currentJob = bestPriority;
                    executionOrder.add(currentJob.name);
                    currentUsage = 0;
                }
            }

            // 50% Quantum: Shortest Job preemption
            if (currentUsage == limit50) {
                Task bestSJF = findShortestJob(waitingList);
                if (bestSJF != null && bestSJF.remainingTime < currentJob.remainingTime) {
                    int unused = q - currentUsage;
                    currentJob.quantum += unused;
                    currentJob.history.add(currentJob.quantum);

                    waitingList.add(currentJob);
                    waitingList.remove(bestSJF);
                    currentJob = bestSJF;
                    executionOrder.add(currentJob.name);
                    currentUsage = 0;
                }
            }
        }

        // Build result
        AGScheduleResult result = new AGScheduleResult();
        result.executionOrder = executionOrder;
        result.processResults = new ArrayList<>();

        double totalWait = 0;
        double totalTurn = 0;

        completedTasks.sort(Comparator.comparing(t -> t.name));

        for (Task t : completedTasks) {
            AGProcessResult pr = new AGProcessResult(t);
            result.processResults.add(pr);
            totalWait += pr.waitingTime;
            totalTurn += pr.turnaroundTime;
        }

        result.averageWaitingTime = totalWait / totalTasks;
        result.averageTurnaroundTime = totalTurn / totalTasks;

        return result;
    }

    // ===== Helpers =====
    private static void checkNewArrivals(List<Task> all, List<Task> queue, int time) {
        for (Task t : all)
            if (t.arrival == time) queue.add(t);
    }

    private static Task findBestPriority(List<Task> queue) {
        return queue.stream().min(Comparator.comparingInt(t -> t.priority)).orElse(null);
    }

    private static Task findShortestJob(List<Task> queue) {
        return queue.stream().min(Comparator.comparingInt(t -> t.remainingTime)).orElse(null);
    }


}
 class Task {
    public String name;
    public int arrival;
    public int burst;
    public int remainingTime;
    public int priority;
    public int quantum;
    public int finishTime;
    public int waitingTime;
    public List<Integer> history = new ArrayList<>();

    public Task(String name, int arrival, int burst, int priority, int quantum) {
        this.name = name;
        this.arrival = arrival;
        this.burst = burst;
        this.remainingTime = burst;
        this.priority = priority;
        this.quantum = quantum;
        this.history.add(quantum);
    }
}