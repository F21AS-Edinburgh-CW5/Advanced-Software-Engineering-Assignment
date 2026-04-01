# Coffee Shop Ordering System — Stage 2

**F21AS Advanced Software Engineering**
**Heriot-Watt University**
**Group:** Edinburgh CW 5

This project extends the Stage 1 coffee shop ordering system with a multithreaded Stage 2 simulation.
## Run the Project

> **Important:** Please clone this repository using `git clone`. Do not download and extract the ZIP archive, as the working directory configuration will not resolve correctly.

## Features

### Stage 1

* Load menu items from CSV
* Load historical orders from CSV
* Browse items in a Swing GUI
* Generate bills with automatic discounts
* Generate a summary report on exit

### Stage 2

* Simulate internal order processing with a producer–consumer model
* Push grouped customer orders into a shared queue
* Process orders concurrently with multiple serving staff workers
* Log simulation events to `simulation_log.txt`
* Real-time Swing GUI showing queue state and staff status via Observer / MVC pattern
* Speed control slider (0.25× – 4×) to adjust simulation pace at runtime
* Dynamic staff addition and removal during a live simulation
* Online order priority queue: online orders are processed before walk-in orders
* Staff status colour coding: green (PROCESSING), yellow (WAITING), grey (IDLE)

## Project Layout

```text
AS-Assignment/
├── data/
│   ├── menu.csv
│   ├── orders.csv
│   └── online_orders.csv
│
├── src/main/java/coffeeshop/
│   ├── App.java
│   ├── SimulationApp.java
│   │
│   ├── api/
│   │   ├── Bill.java
│   │   ├── Category.java
│   │   ├── CoffeeShopService.java
│   │   ├── DiscountCalculator.java
│   │   ├── InvalidItemIdException.java
│   │   └── MenuItemView.java
│   │
│   ├── gui/
│   │   ├── ControlPanel.java
│   │   ├── MainFrame.java
│   │   ├── QueuePanel.java
│   │   ├── SimulationController.java
│   │   └── StaffPanel.java
│   │
│   ├── loader/
│   │   ├── MenuLoader.java
│   │   └── OrderLoader.java
│   │
│   ├── logging/
│   │   └── EventLogger.java
│   │
│   ├── model/
│   │   ├── CustomerOrder.java
│   │   ├── MenuItem.java
│   │   ├── OnlineOrder.java
│   │   ├── OrderRecord.java
│   │   ├── ServingStaff.java
│   │   ├── SimulationSnapshot.java
│   │   └── StaffStatus.java
│   │
│   ├── report/
│   │   └── ReportGenerator.java
│   │
│   ├── service/
│   │   ├── CoffeeShopServiceImpl.java
│   │   ├── DemoCoffeeShopService.java
│   │   ├── ProcessingTimeService.java
│   │   ├── QueueObserver.java
│   │   ├── ServerObserver.java
│   │   └── SimulationService.java
│   │
│   ├── simulation/
│   │   ├── OnlineOrderProducerThread.java
│   │   ├── ProducerThread.java
│   │   ├── ServingStaffWorker.java
│   │   ├── SharedOrderQueue.java
│   │   ├── SimulationConfig.java
│   │   └── SimulationManager.java
│   │
│   └── util/
│       └── IdValidator.java
│
├── src/test/java/coffeeshop/
│   └── ReportGeneratorTest.java
│
├── AS-Assignment.jar
├── pom.xml
└── README.md
```


## Entry Points

### GUI Simulation (Stage 2)

Run:

```java
coffeeshop.App
```

This launches the Stage 2 multithreaded simulation with the full Swing GUI.

### Console Simulation;

Run:

```java
coffeeshop.SimulationApp
```

This launches a console-only version of the Stage 2 simulation (no GUI).

### JAR

```bash
java -jar AS-Assignment.jar
```

Requires the `AS-Assignment/data/` folder to be present in the same directory as the JAR.

## Run the Project

### IntelliJ IDEA

1. Open the folder containing `pom.xml`
2. Import as a Maven project
3. Use **JDK 17+**
4. Run either:

   * `coffeeshop.App`
   * `coffeeshop.SimulationApp`

### Eclipse

1. Import as an **Existing Maven Project**
2. Run either:

   * `App.java` as a Java application
   * `SimulationApp.java` as a Java application

### Tests

Run:

```text
src/test/java/coffeeshop/ReportGeneratorTest.java
```

as a JUnit 5 test.

## GUI Workflow

The GUI lets the user:

* browse menu items
* select multiple items
* generate a bill
* apply discounts automatically
* exit and generate a report

### Discount Rules

| Rule   | Condition                     | Discount |
| ------ | ----------------------------- | -------- |
| Rule 1 | 1+ beverage and 2+ food items | 20% off  |
| Rule 2 | 3+ beverages                  | 15% off  |
| Rule 3 | Subtotal over £25             | 10% off  |

Rules are checked in priority order. Only the first matching rule is applied.

### Report on Exit

The report includes:

* menu items
* order counts
* total revenue after discounts

## Simulation Workflow

The Stage 2 simulation uses a producer–consumer design.

### Main Components

**SharedOrderQueue**
Stores customer orders waiting to be processed using `synchronized`, `wait()`, and `notifyAll()`. Also holds a separate `PriorityQueue` for online orders.

**ProducerThread**
Loads and groups order records, then adds them to the queue at timed intervals.

**OnlineOrderProducerThread**
Reads online orders from `data/online_orders.csv` and submits them to the priority queue inside `SharedOrderQueue`.

**ServingStaffWorker**
Takes orders from the queue and processes them. Checks the online queue first. Supports clean shutdown via a `volatile boolean shouldStop` flag.

**SimulationManager**
Starts the simulation, creates workers, supports dynamic staff addition and removal, and handles shutdown.

**SimulationService**
Maintains observer lists (`QueueObserver`, `ServerObserver`) and distributes `SimulationSnapshot` updates to the GUI.

**SimulationController**
MVC Controller: connects GUI Start/Stop actions to `SimulationManager` and registers GUI panels as observers.

**SimulationConfig**
Holds `volatile double speedMultiplier` (range 0.25–4.0). All `Thread.sleep()` durations are divided by this value at runtime.

**EventLogger**
Records simulation events. Implemented as a Singleton using the Bill Pugh static inner holder pattern. Writes to `simulation_log.txt` on exit.

**Supporting Models**

* `CustomerOrder`
* `OnlineOrder`
* `ServingStaff`
* `SimulationSnapshot`
* `StaffStatus`

### Intended Flow

1. Load menu data
2. Load order records
3. Group records into customer orders
4. Add orders to the shared queue at timed intervals
5. Start multiple serving staff workers
6. Process orders concurrently; online orders take priority
7. GUI updates in real time via Observer pattern on the EDT
8. Log events and write report on exit

## Data Files

The project expects:

* `data/menu.csv`
* `data/orders.csv`
* `data/online_orders.csv`

These must be present relative to the working directory when running from IntelliJ, or relative to the JAR location when running the JAR.

## Tech Stack

* Java
* Java Swing
* JUnit 5
* Maven
* Git / GitHub

## Known Notes

The remove staff function (Ext-2) does not always take effect immediately. A stop request is only honoured when the targeted worker is idle; a worker currently processing an order will not respond until it finishes. This timing issue has not been fully resolved.

## Troubleshooting

| Problem                                              | What to check                                                                 |
| ---------------------------------------------------- | ----------------------------------------------------------------------------- |
| `data/menu.csv` or `data/orders.csv` cannot be found | Check the run configuration working directory and the relative file paths     |
| `coffeeshop.api cannot be resolved`                  | Make sure only `src/main/java` and `src/test/java` are marked as source roots |
| JUnit annotations cannot be resolved                 | Reimport the Maven project or add JUnit 5 correctly                           |
| GUI opens but no data loads                          | Verify that the CSV paths match the working directory                         |
| JAR cannot find data files                           | Ensure `AS-Assignment/data/` folder is in the same directory as the JAR       |

## Notes

This repository contains both:

* the original Stage 1 GUI ordering workflow
* the Stage 2 multithreaded simulation extension

The Stage 2 development followed three agile iterations: Iteration 1 established the console-based producer–consumer skeleton; Iteration 2 added the Swing GUI, Observer pattern, and MVC structure; Iteration 3 delivered the four runtime extensions and final code polish.
