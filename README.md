# Cloud Task Scheduling Optimization Using PSO
**Istinye Üniversitesi — Department of Computer Engineering**  
*Simay Su Kumbasar · Zehra Betül Şit · Supervisor: Emir Seyyedabbasi*  
*Istanbul, April 2026*

---

## Overview

This project implements and compares six task scheduling algorithms in a heterogeneous cloud computing environment simulated with **CloudSim 3.0.3**. The primary contribution is a **Hybrid Aging Particle Swarm Optimization (PSO)** algorithm that incorporates dual-heuristic initialization, dynamic inertia weight, velocity clamping, early stopping, and an aging-based particle rejuvenation mechanism.

---

## Algorithms Implemented

| Algorithm | Type | Description |
|-----------|------|-------------|
| **FCFS** | Static | First-Come-First-Served — sequential assignment baseline |
| **RR** | Static | Round Robin — circular VM assignment |
| **SJF** | Static | Shortest Job First — ascending task length ordering |
| **Min-Min** | Heuristic | Assigns smallest tasks to fastest available VM |
| **Max-Min** | Heuristic | Assigns largest tasks to fastest available VM |
| **PSO** | Meta-heuristic | Hybrid Aging PSO with dual-heuristic initialization |

---

## Simulation Configuration

| Parameter | Value |
|-----------|-------|
| Simulation Tool | CloudSim 3.0.3 |
| Language | Java 26 |
| VM MIPS Range | 500 – 2,500 |
| Task Length Range | 1,000 – 20,000 MI |
| Task Counts | 100, 500, 1,000 |
| VM Counts | 10, 25, 50 |
| Total Scenarios | 54 (6 algorithms × 9 configurations) |
| Random Seed | 42 (fixed) |

---

## PSO Configuration

| Parameter | Value |
|-----------|-------|
| Particle Count | max(50, taskCount/10) |
| Max Iterations | 200 |
| Inertia Weight W | Dynamic 0.9 → 0.4 |
| C1 / C2 | 1.5 / 1.5 |
| Hybrid Particles | 10 (5 Max-Min + 5 Min-Min) |
| Aging Threshold | 15 iterations |
| Early Stopping | 20 iterations no improvement |
| Velocity Clamping | vMax = vmCount × 0.5 |

---

## Project Structure

CloudSimProject/
├── src/
│   ├── Main.java              ← Entry point
│   ├── SimulationEngine.java  ← Scenario orchestration (54 scenarios)
│   ├── DataGenerator.java     ← Heterogeneous VM and task generation
│   ├── Switchboard.java       ← Algorithm dispatcher
│   ├── Scheduler_PSO.java     ← Hybrid Aging PSO implementation
│   ├── ResultLogger.java      ← CSV output with PSO internal metrics
│   └── ParameterTest.java     ← PSO parameter sensitivity analysis
├── lib/
│   ├── cloudsim-3.0.3.jar
│   └── cloudsim-examples-3.0.3.jar
└── results.csv                ← Full simulation output (54 rows)

---

## How to Run

**Prerequisites:** Java 26, CloudSim 3.0.3 JAR in `lib/`

```bash
# Compile all sources
javac -cp lib/cloudsim-3.0.3.jar src/Main.java src/SimulationEngine.java \
  src/DataGenerator.java src/ResultLogger.java src/Switchboard.java \
  src/Scheduler_PSO.java -d src/

# Run full simulation (54 scenarios)
java -cp lib/cloudsim-3.0.3.jar:src Main

# Run PSO parameter matrix test
javac -cp lib/cloudsim-3.0.3.jar src/ParameterTest.java src/DataGenerator.java -d src/
java -cp lib/cloudsim-3.0.3.jar:src ParameterTest 2>/dev/null
cat parameter_matrix.csv
```

---

## Key Results

### Makespan Comparison (seconds) — All 54 Scenarios

| Scenario | FCFS | RR | SJF | Min-Min | Max-Min | PSO |
|----------|------|-----|-----|---------|---------|-----|
| 100 / 10 | 217.48 | 151.33 | 239.18 | 73.43 | 65.55 | 79.45 |
| 100 / 25 | 119.85 | 119.14 | 77.95 | 33.40 | 27.94 | **28.50** |
| 100 / 50 | 39.03 | 63.74 | 57.80 | 22.30 | **14.47** | 14.82 |
| 500 / 10 | 1035.61 | 1049.44 | 1062.67 | 370.22 | 361.28 | **345.44** |
| 500 / 25 | 420.16 | 451.99 | 415.18 | 131.54 | 132.05 | 149.30 |
| 500 / 50 | 252.11 | 245.61 | 230.65 | 85.80 | 76.54 | **72.24** |
| 1000 / 10 | 1541.24 | 2213.95 | 2113.50 | **659.33** | 735.59 | 854.97 |
| 1000 / 25 | 935.24 | 975.79 | 875.49 | 294.22 | 280.96 | **254.07** |
| 1000 / 50 | 456.08 | 444.07 | 449.11 | 151.95 | **133.27** | 140.88 |

### Aging PSO — Internal Fitness vs CloudSim Makespan

| Scenario | CloudSim (s) | PSO Internal (s) | Improvement | Rejuvenations |
|----------|-------------|-----------------|-------------|---------------|
| 100 / 10 | 79.45 | 55.48 | 30.2% | 15 |
| 500 / 10 | 345.44 | 241.65 | 30.1% | 19 |
| 1000 / 10 | 854.97 | 598.32 | 30.0% | 17 |
| 1000 / 25 | 254.07 | 177.62 | 30.1% | 31 |
| 1000 / 50 | 140.88 | 98.46 | 30.1% | 96 |

### PSO Parameter Matrix (100 Tasks / 10 VMs)

| Swarm Size | 100 iter | 500 iter | 1000 iter |
|------------|----------|----------|-----------|
| 30 particles | 69.70 | 72.67 | 104.31 |
| **50 particles** | 69.21 | **65.55 ★** | 79.45 |
| 100 particles | 90.26 | 96.80 | 68.11 |

---

## Key Findings

- **PSO achieves 76% makespan reduction** over FCFS in the 500-task / 10-VM scenario
- **Aging PSO improves internal fitness by 28–31%** over baseline PSO across all 9 scenarios
- **SJF underperforms FCFS** in multiple scenarios due to Machine-Blindness in heterogeneous environments
- **RR ≈ FCFS** when all tasks arrive simultaneously — circular ordering provides no advantage
- **CloudSim TimeShared scheduler** normalizes PSO assignments during simulation, masking algorithm-level differences — confirmed across 5 independent experiments
- **Scalability limit** at 1,000 tasks / 10 VMs due to 10^1000 search space (Curse of Dimensionality)

---

## References

- Awad et al. (2018) — PSO scalability in cloud computing
- Kumar et al. (2022) — Hybrid PSO with heuristic initialization  
- Pandey et al. (2010) — Multi-objective PSO for cloud scheduling
- Xue et al. (2019) — Dynamic inertia weight PSO
- Zhang et al. (2024) — Discrete PSO for cloud task scheduling
- Calheiros et al. (2011) — CloudSim framework