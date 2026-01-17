import com.google.gson.*;
import java.io.FileReader;
import java.util.*;
public class Main {

    static JsonObject loadJson(String filePath) throws Exception {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(filePath), JsonObject.class);
    }


    public static void main(String[] args) throws Exception
    {

        //Round Robin Testing
        for (int i=1;i<=6;i++)
        {
            String fileName = "test_cases_v5\\Other_Schedulers\\test_"+ i+ ".json";
            JsonObject root = loadJson(fileName);
            runAndCompareRR(root,i);
        }

        //Shortest Job First Testing
        for (int i=1; i<=6; i++) {
            String fileName = "test_cases_v5\\Other_Schedulers\\test_"+ i+ ".json";
            JsonObject root = loadJson(fileName);
            runAndCompareSJF(root,i);
        }

        //Priority Scheduling Testing
        for (int i=1; i<=6; i++) {
            String fileName = "test_cases_v5\\Other_Schedulers\\test_"+ i+ ".json";
            JsonObject root = loadJson(fileName);
            runAndComparePriority(root, i);
        }

        //AG Scheduling Testing
        for (int i=1;i<=6;i++) {
            String fileName = "test_cases_v5\\AG\\AG_test"+i+".json";
            JsonObject root = loadJson(fileName);
            runAndCompareAG(root, i);
        }


    }
    static void runAndCompareRR(JsonObject root, int testNum) {

        JsonObject input = root.getAsJsonObject("input");
        JsonObject expectedRR = root.getAsJsonObject("expectedOutput").getAsJsonObject("RR");

        int contextSwitch = input.get("contextSwitch").getAsInt();
        int rrQuantum = input.get("rrQuantum").getAsInt();

        //Build RR process list
        List<RoundRobinProcess> processList = new ArrayList<>();

        JsonArray processes = input.getAsJsonArray("processes");
        for (JsonElement e : processes) {
            JsonObject p = e.getAsJsonObject();
            processList.add(new RoundRobinProcess(
                    p.get("name").getAsString(),
                    p.get("arrival").getAsInt(),
                    p.get("burst").getAsInt(),
                    p.get("priority").getAsInt()
            ));
        }

        //  Run RR
        RoundRobinResult actualRR = RoundRobinSchedule.schedule(processList, rrQuantum, contextSwitch);

        System.out.println("\n========== ROUND ROBIN COMPARISON TEST "+ testNum + " ==========\n");

        //  Execution Order
        List<String> actualOrder = actualRR.getExcutionOrder();
        List<String> expectedOrder = new ArrayList<>();

        for (JsonElement e : expectedRR.getAsJsonArray("executionOrder")) {
            expectedOrder.add(e.getAsString());
        }

        System.out.println("Execution Order:");
        System.out.println("Expected: " + expectedOrder);
        System.out.println("Actual  : " + actualOrder);
        System.out.println("Result  : " + (expectedOrder.equals(actualOrder) ? "PASS" : "FAIL"));
        System.out.println();

        //  Process Results
        System.out.println("Per-Process Results:");

        for (JsonElement e : expectedRR.getAsJsonArray("processResults")) {

            JsonObject exp = e.getAsJsonObject();
            String name = exp.get("name").getAsString();
            int expWT = exp.get("waitingTime").getAsInt();
            int expTAT = exp.get("turnaroundTime").getAsInt();

            RoundRobinProcess actual = null;
            for (RoundRobinProcess p : actualRR.getProcesses()) {
                if (p.getName().equals(name)) {
                    actual = p;
                    break;
                }
            }

            int actWT = actual.getWaitTime();
            int actTAT = actual.getTaTime();

            System.out.println("Process " + name);
            System.out.println("  Waiting Time    Expected=" + expWT +
                    ", Actual=" + actWT +
                    " -> " + (expWT == actWT ? "PASS" : "FAIL"));
            System.out.println("  Turnaround Time Expected=" + expTAT +
                    ", Actual=" + actTAT +
                    " -> " + (expTAT == actTAT ? "PASS" : "FAIL"));
            System.out.println();
        }

        // Averages
        double expAvgWT = expectedRR.get("averageWaitingTime").getAsDouble();
        double expAvgTAT = expectedRR.get("averageTurnaroundTime").getAsDouble();

        double actAvgWT = actualRR.getAvgWaitTime();
        double actAvgTAT = actualRR.getAvgTaTime();

        System.out.println("Averages:");
        System.out.printf(
                "Average Waiting Time    Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgWT, actAvgWT,
                Math.abs(expAvgWT - actAvgWT) < 0.01 ? "PASS" : "FAIL"
        );

        System.out.printf(
                "Average Turnaround Time Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgTAT, actAvgTAT,
                Math.abs(expAvgTAT - actAvgTAT) < 0.01 ? "PASS" : "FAIL"
        );

    }

    static void runAndCompareSJF(JsonObject root, int testNum) {

        JsonObject input = root.getAsJsonObject("input");
        JsonObject expectedSJF = root.getAsJsonObject("expectedOutput").getAsJsonObject("SJF");

        int contextSwitch = input.get("contextSwitch").getAsInt();

        // Build process list
        List<ShortestJobFirstProcess.Process> processList = new ArrayList<>();

        for (JsonElement e : input.getAsJsonArray("processes")) {
            JsonObject p = e.getAsJsonObject();
            processList.add(new ShortestJobFirstProcess.Process(
                    p.get("name").getAsString(),
                    p.get("arrival").getAsInt(),
                    p.get("burst").getAsInt(),
                    p.has("priority") ? p.get("priority").getAsInt() : 0
            ));
        }

        //  Run SJF
        ShortestJobFirstProcess.SJFResult actual = ShortestJobFirstProcess.schedule(processList, contextSwitch);

        System.out.println("\n========== SJF COMPARISON TEST " + testNum + " ==========\n");

        //Execution Order
        List<String> expectedOrder = new ArrayList<>();
        for (JsonElement e : expectedSJF.getAsJsonArray("executionOrder")) {
            expectedOrder.add(e.getAsString());
        }

        System.out.println("Execution Order:");
        System.out.println("Expected: " + expectedOrder);
        System.out.println("Actual  : " + actual.executionOrder);
        System.out.println("Result  : " +
                (expectedOrder.equals(actual.executionOrder) ? "PASS" : "FAIL"));
        System.out.println();

        //  Process Results
        System.out.println("Per-Process Results:");

        Map<String, ShortestJobFirstProcess.Process> actualMap = new HashMap<>();
        for (ShortestJobFirstProcess.Process p : actual.processes) {
            actualMap.put(p.name, p);
        }

        for (JsonElement e : expectedSJF.getAsJsonArray("processResults")) {
            JsonObject exp = e.getAsJsonObject();
            String name = exp.get("name").getAsString();

            int expWT = exp.get("waitingTime").getAsInt();
            int expTAT = exp.get("turnaroundTime").getAsInt();

            ShortestJobFirstProcess.Process act = actualMap.get(name);

            if (act == null) {
                System.out.println("Process " + name + " NOT FOUND -> FAIL");
                continue;
            }

            System.out.println("Process " + name);
            System.out.println("  Waiting Time    Expected=" + expWT +
                    ", Actual=" + act.waitingtime +
                    " -> " + (expWT == act.waitingtime ? "PASS" : "FAIL"));

            System.out.println("  Turnaround Time Expected=" + expTAT +
                    ", Actual=" + act.turnaroundtime +
                    " -> " + (expTAT == act.turnaroundtime ? "PASS" : "FAIL"));
            System.out.println();
        }

        //  Averages
        double expAvgWT = expectedSJF.get("averageWaitingTime").getAsDouble();
        double expAvgTAT = expectedSJF.get("averageTurnaroundTime").getAsDouble();

        System.out.println("Averages:");

        System.out.printf(
                "Average Waiting Time    Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgWT, actual.averageWaitingTime,
                Math.abs(expAvgWT - actual.averageWaitingTime) < 0.01 ? "PASS" : "FAIL"
        );

        System.out.printf(
                "Average Turnaround Time Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgTAT, actual.averageTurnaroundTime,
                Math.abs(expAvgTAT - actual.averageTurnaroundTime) < 0.01 ? "PASS" : "FAIL"
        );
    }


    static void runAndComparePriority(JsonObject root, int testNum) {
        JsonObject input = root.getAsJsonObject("input");
        JsonObject expectedPriority = root.getAsJsonObject("expectedOutput").getAsJsonObject("Priority");

        int contextSwitch = input.get("contextSwitch").getAsInt();
        int rrQuantum = input.get("rrQuantum").getAsInt();
        int agingInterval = input.has("agingInterval") ? input.get("agingInterval").getAsInt() : 0;

        //  Build Priority process list
        ArrayList<Process> processList = new ArrayList<>();
        JsonArray processes = input.getAsJsonArray("processes");

        for (JsonElement e : processes) {
            JsonObject p = e.getAsJsonObject();
            String name = p.get("name").getAsString();
            int arrival = p.get("arrival").getAsInt();
            int burst = p.get("burst").getAsInt();
            int priority = p.get("priority").getAsInt();
            processList.add(new Process(name, arrival, burst, priority));
        }

        // Run Priority Scheduling
        PriorityScheduling scheduler = new PriorityScheduling(contextSwitch, rrQuantum, agingInterval, processList);
        SchedulerOutput actualOutput = scheduler.exec();

        System.out.println("\n========== PRIORITY SCHEDULING COMPARISON TEST " + testNum + " ==========\n");

        // Execution Order
        List<String> actualOrder = actualOutput.ExecutionOrder;
        List<String> expectedOrder = new ArrayList<>();

        for (JsonElement e : expectedPriority.getAsJsonArray("executionOrder")) {
            expectedOrder.add(e.getAsString());
        }

        System.out.println("Execution Order:");
        System.out.println("Expected: " + expectedOrder);
        System.out.println("Actual  : " + actualOrder);
        System.out.println("Result  : " +
                (expectedOrder.equals(actualOrder) ? "PASS" : "FAIL"));
        System.out.println();

        // Process Results
        System.out.println("Per-Process Results:");

        JsonArray expectedProcessResults = expectedPriority.getAsJsonArray("processResults");

        for (int i = 0; i < expectedProcessResults.size(); i++) {
            JsonObject exp = expectedProcessResults.get(i).getAsJsonObject();
            String name = exp.get("name").getAsString();

            int expWT = exp.get("waitingTime").getAsInt();
            int expTAT = exp.get("turnaroundTime").getAsInt();

            // Use the same index for actual results
            int actWT = actualOutput.WaitingTime.get(i);
            int actTAT = actualOutput.TurnaroundTime.get(i);

            System.out.println("Process " + name);
            System.out.println("  Waiting Time    Expected=" + expWT + ", Actual=" + actWT + " -> " + (expWT == actWT ? "PASS" : "FAIL"));
            System.out.println("  Turnaround Time Expected=" + expTAT + ", Actual=" + actTAT + " -> " + (expTAT == actTAT ? "PASS" : "FAIL"));
            System.out.println();
        }

        // Averages
        double expAvgWT = expectedPriority.get("averageWaitingTime").getAsDouble();
        double expAvgTAT = expectedPriority.get("averageTurnaroundTime").getAsDouble();

        double actAvgWT = actualOutput.AverageWaitingTime;
        double actAvgTAT = actualOutput.AverageTurnaroundTime;

        System.out.println("Averages:");
        System.out.printf(
                "Average Waiting Time    Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgWT, actAvgWT,
                Math.abs(expAvgWT - actAvgWT) <= 0.2 ? "PASS" : "FAIL"
        );

        System.out.printf(
                "Average Turnaround Time Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgTAT, actAvgTAT,
                Math.abs(expAvgTAT - actAvgTAT) <= 0.2 ? "PASS" : "FAIL"
        );
    }

    static boolean compareHistory(JsonArray expected, List<Integer> actual) {
        if (expected.size() != actual.size()) return false;
        for (int i = 0; i < expected.size(); i++) {
            if (expected.get(i).getAsInt() != actual.get(i)) return false;
        }
        return true;
    }

    static void runAndCompareAG(JsonObject root, int testNum) {
        JsonObject input = root.getAsJsonObject("input");
        JsonObject expected = root.getAsJsonObject("expectedOutput");

        //  Build AG task list
        List<Task> taskList = new ArrayList<>();
        JsonArray processes = input.getAsJsonArray("processes");

        for (JsonElement e : processes) {
            JsonObject p = e.getAsJsonObject();
            String name = p.get("name").getAsString();
            int arrival = p.get("arrival").getAsInt();
            int burst = p.get("burst").getAsInt();
            int priority = p.get("priority").getAsInt();
            int quantum = p.get("quantum").getAsInt();
            taskList.add(new Task(name, arrival, burst, priority, quantum));
        }

        // Run AG Scheduling
        AGScheduler.AGScheduleResult result = AGScheduler.runSchedule(taskList);

        System.out.println("\n========== AG SCHEDULING TEST " + testNum + " ==========\n");

        // Execution Order
        List<String> expectedOrder = new ArrayList<>();
        for (JsonElement e : expected.getAsJsonArray("executionOrder"))
            expectedOrder.add(e.getAsString());

        System.out.println("Execution Order:");
        System.out.println("Expected: " + expectedOrder);
        System.out.println("Actual  : " + result.executionOrder);
        System.out.println("Result  : " + (expectedOrder.equals(result.executionOrder) ? "PASS" : "FAIL"));
        System.out.println();

        // Process Results
        System.out.println("Per-Process Results:");
        JsonArray expectedProcessResults = expected.getAsJsonArray("processResults");

        for (int i = 0; i < expectedProcessResults.size(); i++) {
            JsonObject exp = expectedProcessResults.get(i).getAsJsonObject();
            String name = exp.get("name").getAsString();

            AGScheduler.AGProcessResult actual = result.processResults.stream()
                    .filter(t -> t.name.equals(name))
                    .findFirst()
                    .orElseThrow();

            int expWT = exp.get("waitingTime").getAsInt();
            int expTAT = exp.get("turnaroundTime").getAsInt();
            JsonArray expHistory = exp.getAsJsonArray("quantumHistory");

            System.out.println("Process " + name);
            System.out.println("  Waiting Time    Expected=" + expWT +
                    ", Actual=" + actual.waitingTime +
                    " -> " + (expWT == actual.waitingTime ? "PASS" : "FAIL"));

            System.out.println("  Turnaround Time Expected=" + expTAT +
                    ", Actual=" + actual.turnaroundTime +
                    " -> " + (expTAT == actual.turnaroundTime ? "PASS" : "FAIL"));

            System.out.println("  Quantum History Expected=" + expHistory +
                    ", Actual=" + actual.quantumHistory +
                    " -> " + (compareHistory(expHistory, actual.quantumHistory) ? "PASS" : "FAIL"));
            System.out.println();
        }

        //Averages
        double expAvgWT = expected.get("averageWaitingTime").getAsDouble();
        double expAvgTAT = expected.get("averageTurnaroundTime").getAsDouble();

        System.out.println("Averages:");
        System.out.printf("Average Waiting Time    Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgWT, result.averageWaitingTime,
                Math.abs(expAvgWT - result.averageWaitingTime) < 0.01 ? "PASS" : "FAIL");
        System.out.printf("Average Turnaround Time Expected=%.2f, Actual=%.2f -> %s%n",
                expAvgTAT, result.averageTurnaroundTime,
                Math.abs(expAvgTAT - result.averageTurnaroundTime) < 0.01 ? "PASS" : "FAIL");
    }



}