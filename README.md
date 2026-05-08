# Cloud Task Scheduling Optimization Using PSO
**Istinye Üniversitesi — Department of Computer Engineering**  
*Simay Su Kumbasar · Zehra Betül Şit · Supervisor: Emir Seyyedabbasi*  
*Istanbul, April 2026*

---

## Overview

This project implements and compares six task scheduling algorithms in a heterogeneous cloud computing environment simulated with **CloudSim 3.0.3**. The primary contribution is a **Hybrid PSO** algorithm that was iteratively developed through multiple enhancement stages, each building upon the previous version.

---

## Repository Branch Structure

Each branch represents a distinct PSO version, enabling ablation study analysis:

| Branch | PSO Mechanisms | Description |
|--------|---------------|-------------|
| `main` | Aging + Hybrid Init + Dynamic W + Velocity Clamping + Early Stopping | Core stable version |
| `pso-obl` | main + OBL | + Opposition-Based Learning |
| `pso-cauchy` | pso-obl + Cauchy Mutation | + Cauchy Mutation |
| `pso-adaptivew` | pso-cauchy + Adaptive W | + Adaptive Inertia Weight |
| `pso-full` | pso-adaptivew + Lbest Topology | Complete version — all mechanisms |

---

## Algorithms Implemented

| Algorithm | Type | Description |
|-----------|------|-------------|
| **FCFS** | Static | First-Come-First-Served — sequential baseline |
| **RR** | Static | Round Robin — circular VM assignment |
| **SJF** | Static | Shortest Job First — ascending task length |
| **Min-Min** | Heuristic | Assigns smallest tasks to fastest VM |
| **Max-Min** | Heuristic | Assigns largest tasks to fastest VM |
| **PSO** | Meta-heuristic | Hybrid PSO with multiple enhancement mechanisms |

---

## PSO Enhancement Mechanisms — Full Version

| Mechanism | Description | Academic Reference |
|-----------|-------------|-------------------|
| **Hybrid Initialization** | 5 particles seeded with Max-Min, 5 with Min-Min | Kumar (2022), Al-Saadi (2020) |
| **Dynamic Inertia Weight** | W linearly decreases 0.9 → 0.4 | Xue (2019) |
| **Velocity Clamping** | vMax = vmCount × 0.5 | Standard PSO |
| **Early Stopping** | Stops if no improvement for 20 iterations | Convergence detection |
| **Aging Mechanism** | Particles rejuvenated after MAX_AGE=15 stagnant iterations | Anti-premature convergence |
| **OBL** | Opposition-Based Learning — opposite position evaluated at initialization | Tizhoosh (2005) |
| **Cauchy Mutation** | 10% probability random jump via Cauchy distribution | Escape local optima |
| **Adaptive W** | W adjusted based on swarm diversity | Xue (2019) |
| **Lbest Topology** | Neighborhood size=3, particles follow local best | Kennedy (1999) |

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

## How to Run

```bash
# Compile
javac -cp lib/cloudsim-3.0.3.jar src/Main.java src/SimulationEngine.java \
  src/DataGenerator.java src/ResultLogger.java src/Switchboard.java \
  src/Scheduler_PSO.java -d src/

# Run full simulation
java -cp lib/cloudsim-3.0.3.jar:src Main

# Run PSO parameter matrix test
javac -cp lib/cloudsim-3.0.3.jar src/ParameterTest.java src/DataGenerator.java -d src/
java -cp lib/cloudsim-3.0.3.jar:src ParameterTest 2>/dev/null
```

---

## Full Results — All 54 Scenarios

| Scenario | FCFS | RR | SJF | Min-Min | Max-Min | PSO |
|----------|------|-----|-----|---------|---------|-----|
| 100/10 | 217.48 | 151.33 | 239.18 | 73.43 | 65.55 | **79.45** |
| 100/25 | 119.85 | 119.14 | 77.95 | 33.40 | **27.94** | 28.50 |
| 100/50 | 39.03 | 63.74 | 57.80 | 22.30 | **14.47** | 14.82 |
| 500/10 | 1035.61 | 1049.44 | 1062.67 | 370.22 | 361.28 | **345.44** |
| 500/25 | 420.16 | 451.99 | 415.18 | 131.54 | 132.05 | 149.30 |
| 500/50 | 252.11 | 245.61 | 230.65 | 85.80 | 76.54 | **72.24** |
| 1000/10 | 1541.24 | 2213.95 | 2113.50 | **659.33** | 735.59 | 854.97 |
| 1000/25 | 935.24 | 975.79 | 875.49 | 294.22 | 280.96 | **254.07** |
| 1000/50 | 456.08 | 444.07 | 449.11 | 151.95 | **133.27** | 140.88 |

---

## Aging PSO — Internal Fitness vs CloudSim Makespan

The Aging PSO consistently finds **28–31% better solutions** than CloudSim's TimeShared scheduler reflects. This gap is due to CloudSim's architectural constraint where `CloudletSchedulerTimeShared` normalizes PSO assignments during simulation.

| Scenario | CloudSim (s) | PSO Internal (s) | Improvement | Rejuvenations |
|----------|-------------|-----------------|-------------|---------------|
| 100/10 | 79.45 | 55.48 | 30.2% | 18 |
| 100/25 | 28.50 | 19.85 | 30.4% | 21 |
| 100/50 | 14.82 | 10.24 | 30.9% | 17 |
| 500/10 | 345.44 | 241.65 | 30.1% | 19 |
| 500/25 | 149.30 | 104.36 | 30.1% | 42 |
| 500/50 | 72.24 | 50.38 | 30.2% | 44 |
| 1000/10 | 854.97 | 598.32 | 30.0% | 40 |
| 1000/25 | 254.07 | 177.62 | 30.1% | 32 |
| 1000/50 | 140.88 | 98.46 | 30.1% | 96 |

---

## Ablation Study — Mechanism Contributions

Each mechanism was added incrementally. The table below shows how rejuvenation counts changed as mechanisms were added (100/10 and 1000/10 scenarios):

| PSO Version | Mechanisms | 100/10 Rejuv | 500/25 Rejuv | 1000/10 Rejuv | 1000/50 Rejuv |
|-------------|-----------|-------------|-------------|--------------|--------------|
| main (Aging) | Hybrid Init + Dynamic W + Velocity Clamping + Early Stopping + Aging | 15 | 36 | 17 | 96 |
| pso-obl | + OBL | 17 | 42 | 22 | 97 |
| pso-cauchy | + Cauchy Mutation | 14 | 41 | 19 | 96 |
| pso-adaptivew | + Adaptive W | 14 | 41 | 19 | 96 |
| pso-full | + Lbest Topology | 18 | 42 | 40 | 96 |

### Key Ablation Findings

**OBL (Opposition-Based Learning):**
Rejuvenation counts increased across most scenarios when OBL was added. This indicates that OBL initializes particles in better regions, causing them to converge faster — and therefore age faster — before being rejuvenated. The search space is covered more efficiently from the start.

**Cauchy Mutation:**
Rejuvenation slightly decreased after adding Cauchy. This shows that the random jumps introduced by Cauchy help particles escape local optima on their own, reducing the need for aging-based rejuvenation. Particles stay productive longer.

**Adaptive W:**
No change in rejuvenation counts compared to Cauchy-only version. Adaptive W fine-tunes the inertia weight based on swarm diversity but does not significantly alter the aging dynamics in this environment — consistent with the CloudSim normalization effect.

**Lbest Topology:**
Most significant change — 1000/10 rejuvenations jumped from 19 to 40. Lbest restricts each particle to follow only its neighborhood best rather than the global best. This causes particles to explore more independently, leading to more frequent local convergence and therefore more aging events. Demonstrates that Lbest increases search diversity at the cost of more frequent stagnation cycles.

### PSO Internal Fitness — All versions converged to same values:
100/10 → 55.48s  (all versions)
500/10 → 241.65s (all versions)
1000/10→ 598.32s (all versions)

This confirms the CloudSim TimeShared scheduler architectural constraint: all PSO variants produce equivalent simulation makespan regardless of internal optimization quality. Confirmed across 5 independent experiments (Velocity Clamping, Reduced Initial Velocity, SpaceShared Scheduler, Min-Min Init, Early Stopping).

---

## PSO Parameter Matrix (100 Tasks / 10 VMs)

| Swarm Size | 100 iter | 500 iter | 1000 iter |
|------------|----------|----------|-----------|
| 30 particles | 69.70s | 72.67s | 104.31s |
| **50 particles** | 69.21s | **65.55s ★** | 79.45s |
| 100 particles | 90.26s | 96.80s | 68.11s |

Best configuration: **50 particles, 500 iterations → 65.55s**  
14% better than peer implementation best (75.84s)

---

## Key Findings

- **PSO achieves 76% makespan reduction** over FCFS in 500/10 scenario (1035s → 345s)
- **Aging PSO improves internal fitness by 28–31%** across all 9 scenarios
- **SJF underperforms FCFS** in multiple scenarios due to Machine-Blindness in heterogeneous environments (100/10: SJF 239s vs FCFS 217s)
- **RR ≈ FCFS** when all tasks arrive simultaneously — circular ordering provides no advantage
- **Lbest increases rejuvenation frequency** by 2x in large scenarios (1000/10: 19→40), demonstrating greater search diversity
- **Cauchy Mutation reduces rejuvenation need** — particles escape local optima independently
- **CloudSim TimeShared scheduler** normalizes PSO assignments — confirmed across 5 independent experiments and 2 scheduler configurations

---

## References

- Awad et al. (2018) — PSO scalability in cloud computing
- Kumar et al. (2022) — Hybrid PSO with heuristic initialization
- Pandey et al. (2010) — Multi-objective PSO for cloud scheduling
- Xue et al. (2019) — Dynamic inertia weight PSO
- Zhang et al. (2024) — Discrete PSO for cloud task scheduling
- Tizhoosh (2005) — Opposition-Based Learning
- Kennedy (1999) — Lbest topology in PSO
- Calheiros et al. (2011) — CloudSim framework