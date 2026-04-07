# 🌿 GreenScheduler

A carbon-aware job scheduling system that intelligently defers workloads to periods of low-carbon grid intensity — built for the Indian power grid.

## Overview

GreenScheduler is a research prototype that demonstrates **SLA-adaptive, carbon-aware batch job scheduling**. It ingests real-time Indian Grid data (15-minute intervals), evaluates carbon intensity and grid stability, and makes intelligent dispatch decisions to minimize the carbon footprint of compute workloads — without violating job deadlines.

## How It Works

```
Excel Grid Data (.xls)
        │
        ▼
CarbonMonitorService ──→ Grid Status (Intensity + Frequency)
        │
        ▼
JobSchedulerService ──→ SLA-Adaptive Scheduling Algorithm
        │
        ▼
BatchWorkerService ──→ Job Execution + Carbon Metrics
        │
        ▼
EventService (Pub-Sub) ──→ H2 In-Memory DB (Logs + Metrics)
```

### Scheduling Logic

The scheduler runs a polling loop that:

1. **Checks grid stability** — if frequency drops below `49.9 Hz`, all pending jobs are paused (circuit breaker).
2. **Evaluates SLA urgency** — jobs are classified into three priority tiers based on time remaining before deadline:

| Time Remaining | Mode | Carbon Threshold |
|---|---|---|
| < 30 min | `PANIC_2` | Any intensity (runs unconditionally) |
| 30 – 240 min | `PANIC_1` | ≤ 800 gCO₂/kWh |
| > 240 min | `STANDARD` | ≤ 600 gCO₂/kWh |

3. **Dispatches the best candidate** — highest-priority job that passes its threshold check is sent to the worker.

### Carbon Metrics

Once a job completes, two metrics are calculated:

```
CarbonDelta       = IntensityAtQueueTime − IntensityAtExecutionTime
PercentageSaved   = (CarbonDelta / IntensityAtQueueTime) × 100
```

---

## Architecture

GreenScheduler simulates an **event-driven microservices architecture** within a single Spring Boot runtime using domain boundary segregation.

| Service | Responsibility |
|---|---|
| `CarbonMonitorService` | Polls `.xls` grid dataset via Apache POI; evaluates carbon intensity and simulates frequency fluctuations |
| `JobSchedulerService` | Core decision engine — implements priority routing, SLA management, and dispatch logic |
| `BatchWorkerService` | Simulates edge compute execution; generates detailed execution logs |
| `EventService` | Lightweight pub-sub wrapper; persists cross-cutting state changes to the DB |

### Storage (H2 In-Memory)

- **Job DB** — queue state, SLA mode, carbon savings metrics (`intensityAtQueueTime`, `intensityAtExecutionTime`, `carbonDelta`, `percentageSaved`)
- **Execution Logs** — timeline snapshots with carbon metrics at execution time
- **Event Logs** — cluster-level system events (e.g., `GRID_INSTABILITY`)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.x, Spring Web MVC |
| Frontend | Vanilla HTML5, CSS3 (Custom Properties, Flexbox/Grid), ES6+ JavaScript |
| Database | H2 In-Memory, Spring Data JPA |
| Data Processing | Apache POI (`.xls` parsing) |
| Build | Maven (`mvnw` wrapper) |
| Fonts | Google Fonts — Inter, JetBrains Mono |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)

### Run

```bash
git clone https://github.com/yamn1hc043/GreenScheduler.git
cd GreenScheduler
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` by default.

### Dataset

Place the Indian Grid intensity dataset at the expected path:

```
src/main/resources/dataset.xls
```

The `CarbonMonitorService` maps 15-minute interval rows to a simulated system clock.

---

## Key Concepts

- **Carbon Intensity** — measured in gCO₂/kWh; lower = cleaner grid
- **GREEN grid** — intensity < 600 gCO₂/kWh
- **DIRTY grid** — intensity ≥ 600 gCO₂/kWh
- **STABLE grid** — frequency ≥ 49.9 Hz
- **UNSTABLE grid** — frequency < 49.9 Hz (triggers circuit breaker)

---

## Research Context

This project explores how **carbon-aware scheduling** can reduce the emissions of deferred/batch compute workloads by exploiting temporal variability in grid carbon intensity — a technique applicable to data centers, edge compute, and cloud infrastructure operating under renewable-heavy grids.

---

## License

This project is for academic/research purposes.

## Author 
**VIT Vellore** : Aarif Mohammed J K and Chinmay D
