# SchedulX: Advanced CPU Scheduling Engine

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JSON](https://img.shields.io/badge/Data-JSON_Parsing-black?style=for-the-badge&logo=json)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

**SchedulX** is a high-precision CPU scheduling simulator designed to analyze and compare process management strategies. It implements classic preemptive algorithms alongside a custom, hybrid "AG Scheduler" designed to optimize turnaround time and minimize starvation.

Unlike simple console applications, this project features a robust **JSON-based Testing Framework** that automates verification against complex datasets, ensuring algorithmic accuracy down to the millisecond.

## 🚀 Overview

The simulator models the behavior of a single-core CPU handling multiple processes with varying arrival times, burst times, and priorities. It accounts for real-world system overhead by simulating **Context Switching** delays.

### Supported Algorithms
1.  **Shortest Job First (SJF/SRTF):** Preemptive scheduling based on remaining burst time.
2.  **Round Robin (RR):** Time-slice based scheduling with context switching overhead.
3.  **Preemptive Priority:** Priority-based execution with **Aging** to solve the starvation problem.
4.  **AG Scheduling:** A complex, adaptive hybrid algorithm (FCFS → Priority → SJF).

## 🧠 The AG Scheduler (Advanced Logic)

The core innovation of this project is the **AG Scheduler**, a three-stage adaptive algorithm that dynamically adjusts time quantums based on process history.

**The Quantum Logic:**
* **Stage 1 (FCFS):** Runs for the first 25% of the quantum.
* **Stage 2 (Priority):** Runs until 50% of the quantum. If a higher priority job is waiting, it preempts.
* **Stage 3 (SJF):** Runs for the remainder. If a shorter job is waiting, it preempts.

**Dynamic Quantum Updates:**
* *If fully used:* Quantum increases by 2.
* *If unused (finished early):* Quantum reset to 0.
* *If preempted:* Quantum increases based on the remaining unused time.

## 🛠 Technical Architecture

The system is designed with a modular architecture to separate data parsing, scheduling logic, and result verification.

### 1. Data Layer (JSON)
The application parses standardized JSON input files containing:
* Process Metadata (Arrival, Burst, Priority).
* System Config (Context Switch Time, Quantum).
* Expected Outputs (for automated testing).

### 2. Scheduling Engines
Each algorithm is encapsulated in its own class (e.g., `AGScheduler`, `RoundRobinSchedule`), implementing a strategy pattern to handle the `readyQueue` and execution history.

### 3. Verification Module (`Main.java`)
The `Main` class acts as a test runner. It:
1.  Loads test cases from `test_cases_v5`.
2.  Executes the selected scheduler.
3.  Compares `Actual Output` vs `Expected Output` (JSON).
4.  Calculates and verifies **Average Waiting Time** and **Average Turnaround Time**.

## 👥 Contributors

This engine was architected, implemented, and optimized by our engineering team:

* **Mousa Mohamed Mousa**
* **Omar Mohamed Farag**
* **Mohab Amr**
* **Mariel Robert John**
* **Malak Amr**

---

## 🎓 Academic Context

This project was developed as part of the **Operating Systems** subject coursework. It demonstrates a practical implementation of core OS concepts, including:

* **Process Control Blocks (PCB):** Managing process state, metadata, and execution history.
* **CPU Scheduling Algorithms:** Implementing logic to maximize CPU utilization and throughput (SJF, Round Robin, Priority).
* **Context Switching:** Simulating the computational overhead incurred when saving and loading process states.
* **Starvation & Aging:** addressing indefinite blocking of low-priority processes by dynamically increasing their priority over time.
* **Performance Metrics:** Calculating and analyzing Average Waiting Time and Turnaround Time to evaluate algorithmic efficiency.

## 💻 Installation & Usage

### Prerequisites
* Java Development Kit (JDK) 8 or higher.
* **Google Gson** library (for JSON parsing).

### Running the Simulator
1.  **Clone the repository**
3.  **Add Gson Dependency**
    Ensure `gson-2.8.x.jar` is in your classpath.
4.  **Compile and Run**
    ```bash
    javac -cp .:gson.jar Main.java
    java -cp .:gson.jar Main
    ```

## 📊 Sample Verification Output

The system outputs a strict pass/fail report for every metric, ensuring high reliability:

```text
========== AG SCHEDULING TEST 1 ==========

Execution Order:
Expected: [P1, P2, P3, P1]
Actual  : [P1, P2, P3, P1]
Result  : PASS

Per-Process Results:
Process P1
  Waiting Time    Expected=12, Actual=12 -> PASS
  Turnaround Time Expected=29, Actual=29 -> PASS
  Quantum History Expected=[4, 6, 8], Actual=[4, 6, 8] -> PASS

Averages:
Average Waiting Time    Expected=8.50, Actual=8.50 -> PASS
Average Turnaround Time Expected=18.00, Actual=18.00 -> PASS
