import java.util.*;

class Process implements Comparable<Process> {
    String Name;
    int ArrivalTime;
    int BurstTime;
    int OriginalBurstTime;
    int Priority;
    int CumulativeWaitTime;

    public Process(String name, int arrivalTime, int burstTime, int priority) {
        Name = name;
        ArrivalTime = arrivalTime;
        BurstTime = burstTime;
        OriginalBurstTime = burstTime;
        Priority = priority;
        CumulativeWaitTime = 0;
    }

    public int getEffectivePriority(int agingInterval) {
        if (agingInterval == 0) return Priority;
        int agingBonus = CumulativeWaitTime / agingInterval;
        int effectivePriority = Priority - agingBonus;
        return Math.max(1, effectivePriority);  // Minimum priority is 1
    }

    @Override
    public int compareTo(Process rhs) {
        return Integer.compare(this.ArrivalTime, rhs.ArrivalTime);
    }
}

class SchedulerOutput {
    List<String> ExecutionOrder = new ArrayList<>();
    List<Integer> WaitingTime = new ArrayList<>();
    List<Integer> TurnaroundTime = new ArrayList<>();
    double AverageWaitingTime;
    double AverageTurnaroundTime;

    public SchedulerOutput() {}
}

class PriorityScheduling {
    int contextSwitch;
    int rrQuantum;
    int agingInterval;
    ArrayList<Process> incomingJobs;
    ArrayList<Process> readyQueue = new ArrayList<>();

    public PriorityScheduling(int contextSwitch, int rrQuantum, int agingInterval, ArrayList<Process> processes) {
        this.contextSwitch = contextSwitch;
        this.rrQuantum = rrQuantum;
        this.agingInterval = agingInterval;
        this.incomingJobs = new ArrayList<>(processes);
        this.incomingJobs.sort(Comparator.comparingInt(p -> p.ArrivalTime));
    }

    private Process getHighestPriority(ArrayList<Process> queue) {
        if (queue.isEmpty()) return null;

        Process selected = null;
        int bestEffectivePriority = Integer.MAX_VALUE;

        for (Process p : queue) {
            int effectivePriority = p.getEffectivePriority(agingInterval);

            if (selected == null) {
                selected = p;
                bestEffectivePriority = effectivePriority;
            } else if (effectivePriority < bestEffectivePriority) {
                selected = p;
                bestEffectivePriority = effectivePriority;
            } else if (effectivePriority == bestEffectivePriority) {
                // Tie in effective priority - compare arrival time
                if (p.ArrivalTime < selected.ArrivalTime) {
                    selected = p;
                } else if (p.ArrivalTime == selected.ArrivalTime) {
                    // Tie in arrival time - compare names
                    if (p.Name.compareTo(selected.Name) < 0) {
                        selected = p;
                    }
                }
            }
        }
        return selected;
    }

    private void addArrivingProcesses(ArrayList<Process> completed, int currentTime) {
        while (!incomingJobs.isEmpty() && incomingJobs.get(0).ArrivalTime <= currentTime) {
            Process p = incomingJobs.remove(0);
            if (!completed.contains(p)) {
                readyQueue.add(p);
            }
        }
    }

    private void contextSwitchTime(ArrayList<Process> completed, int currentTime) {
        // Age all processes in ready queue
        for (Process p : readyQueue) {
            p.CumulativeWaitTime += contextSwitch;
        }
        currentTime += contextSwitch;
        // Check for arrivals during/after context switch
        addArrivingProcesses(completed, currentTime);
        return;
    }

    private void ageReadyQueue(Process executing, int time) {
        for (Process p : readyQueue) {
            if (p != executing) {
                p.CumulativeWaitTime += time;
            }
        }
    }

    public SchedulerOutput exec() {
        SchedulerOutput output = new SchedulerOutput();
        int currentTime = 0;
        ArrayList<Process> completed = new ArrayList<>();
        Process currentProcess = null;
        Process nextSelected = null;

        Map<String, Integer> completionTimes = new HashMap<>();
        Map<String, Integer> arrivalTimes = new HashMap<>();
        Map<String, Integer> burstTimes = new HashMap<>();

        // Store original data
        for (Process p : incomingJobs) {
            arrivalTimes.put(p.Name, p.ArrivalTime);
            burstTimes.put(p.Name, p.OriginalBurstTime);
        }
        for (Process p : readyQueue) {
            arrivalTimes.put(p.Name, p.ArrivalTime);
            burstTimes.put(p.Name, p.OriginalBurstTime);
        }

        while (completed.size() < arrivalTimes.size()) {
            // Add arriving processes at current time
            addArrivingProcesses(completed, currentTime);

            // Select highest priority process
            if (!readyQueue.isEmpty()) {
                Process selected = getHighestPriority(readyQueue);

                // If a process just finished, nextSelected was pre-determined
                if (nextSelected != null) {
                    // Check if predetermined process is still highest priority after context switch
                    while (nextSelected != selected) {
                        output.ExecutionOrder.add(nextSelected.Name);

                        // Context switch again and pick next process
                        for (Process p : readyQueue) {
                            p.CumulativeWaitTime += contextSwitch;
                        }
                        currentTime += contextSwitch;
                        addArrivingProcesses(completed, currentTime);

                        nextSelected = selected;
                        selected = getHighestPriority(readyQueue);
                    }
                }
                nextSelected = null;

                // Context switching between different processes
                if (currentProcess != null && currentProcess != selected) {
                    // Special case: process just arrived
                    if (selected.ArrivalTime == currentTime) {
                        // Don't count context switch time for the just-arrived process
                        for (Process p : readyQueue) {
                            if (p != selected) {
                                p.CumulativeWaitTime += contextSwitch;
                            }
                        }
                        currentTime += contextSwitch;
                        addArrivingProcesses(completed, currentTime);
                    } else {
                        // Regular context switch
                        for (Process p : readyQueue) {
                            p.CumulativeWaitTime += contextSwitch;
                        }
                        currentTime += contextSwitch;
                        addArrivingProcesses(completed, currentTime);
                    }

                    // Recheck after context switching
                    Process oldSelected = selected;
                    selected = getHighestPriority(readyQueue);
                    if (selected != oldSelected) {
                        output.ExecutionOrder.add(oldSelected.Name);

                        for (Process p : readyQueue) {
                            if (p != oldSelected) {
                                p.CumulativeWaitTime += contextSwitch;
                            }
                        }
                        currentTime += contextSwitch;
                        addArrivingProcesses(completed, currentTime);
                    }
                }

                // Execute for 1 time unit
                currentProcess = selected;

                // Only add to execution order if it's a different process than last
                if (output.ExecutionOrder.isEmpty() ||
                        !output.ExecutionOrder.get(output.ExecutionOrder.size() - 1).equals(selected.Name)) {
                    output.ExecutionOrder.add(selected.Name);
                }

                // Increment waiting time for all except executing process
                ageReadyQueue(selected, 1);

                selected.BurstTime--;
                currentTime++;

                // Check completion
                if (selected.BurstTime == 0) {
                    completionTimes.put(selected.Name, currentTime);
                    completed.add(selected);
                    readyQueue.remove(selected);
                    addArrivingProcesses(completed, currentTime);

                    // Determine next process before context switch
                    nextSelected = getHighestPriority(readyQueue);

                    // Context switch after completion
                    for (Process p : readyQueue) {
                        p.CumulativeWaitTime += contextSwitch;
                    }
                    currentTime += contextSwitch;
                    addArrivingProcesses(completed, currentTime);

                    currentProcess = null;
                }
            } else {
                // Idle time
                currentTime++;
            }
        }

        // Calculate results
        for (String name : completionTimes.keySet()) {
            int finish = completionTimes.get(name);
            int arrival = arrivalTimes.get(name);
            int burst = burstTimes.get(name);
            output.TurnaroundTime.add(finish - arrival);
            output.WaitingTime.add((finish - arrival) - burst);
        }
        output.AverageTurnaroundTime = output.TurnaroundTime.stream().mapToDouble(a -> a).average().orElse(0);
        output.AverageWaitingTime = output.WaitingTime.stream().mapToDouble(a -> a).average().orElse(0);

        return output;
    }
}