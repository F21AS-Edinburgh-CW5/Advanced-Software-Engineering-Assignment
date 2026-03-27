# Coffee Shop Ordering System — Stage 2

**F21AS Advanced Software Engineering**
**Heriot-Watt University**
**Group:** Edinburgh CW 5

This project extends the Stage 1 coffee shop ordering system with a multithreaded Stage 2 simulation.

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
* Log simulation events
* Provide the base structure for later Observer / MVC style extensions

## Project Layout

```text
AS-Assignment/
├── data/
│   ├── menu.csv
│   └── orders.csv
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
│   │   └── MainFrame.java
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
│   │   └── ProcessingTimeService.java
│   │
│   ├── simulation/
│   │   ├── ProducerThread.java
│   │   ├── ServingStaffWorker.java
│   │   ├── SharedOrderQueue.java
│   │   └── SimulationManager.java
│   │
│   └── util/
│       └── IdValidator.java
│
├── src/test/java/coffeeshop/
│   └── ReportGeneratorTest.java
│
├── pom.xml
└── README.md
```

## Entry Points

### GUI

Run:

```java
coffeeshop.App
```

This launches the Stage 1 ordering interface.

### Console Simulation

Run:

```java
coffeeshop.SimulationApp
```

This launches the Stage 2 multithreaded simulation.

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
Stores customer orders waiting to be processed using `synchronized`, `wait()`, and `notifyAll()`.

**ProducerThread**
Loads and groups order records, then adds them to the queue.

**ServingStaffWorker**
Takes orders from the queue and processes them.

**SimulationManager**
Starts the simulation, creates workers, and handles shutdown.

**EventLogger**
Records simulation events. Implemented as a Singleton.

**Supporting Models**

* `CustomerOrder`
* `ServingStaff`
* `SimulationSnapshot`
* `StaffStatus`

### Intended Flow

1. Load menu data
2. Load order records
3. Group records into customer orders
4. Add orders to the shared queue
5. Start multiple workers
6. Process orders concurrently
7. Log events and exit cleanly

## Data Files

The project expects:

* `data/menu.csv`
* `data/orders.csv`

These are used by both the GUI and the simulation.

## Tech Stack

* Java
* Java Swing
* JUnit 5
* Maven
* Git / GitHub

## Known Notes

The project was developed iteratively. Depending on the current branch state, the Stage 2 simulation may still need final interface alignment.

Known integration points include:

* package declaration issue in `SimulationApp.java`
* constructor mismatch between `SimulationManager` and `ServingStaffWorker`
* `Runnable` vs `Thread` inconsistency
* static vs instance use of `OrderLoader.load(...)`

## Troubleshooting

| Problem                                              | What to check                                                                 |
| ---------------------------------------------------- | ----------------------------------------------------------------------------- |
| `data/menu.csv` or `data/orders.csv` cannot be found | Check the run configuration working directory and the relative file paths     |
| `coffeeshop.api cannot be resolved`                  | Make sure only `src/main/java` and `src/test/java` are marked as source roots |
| JUnit annotations cannot be resolved                 | Reimport the Maven project or add JUnit 5 correctly                           |
| `SimulationApp` fails to run                         | Check the simulation classes against the known integration notes              |
| GUI opens but no data loads                          | Verify that the CSV paths match the working directory                         |

## Notes

This repository contains both:

* the original Stage 1 GUI ordering workflow
* the Stage 2 multithreaded simulation extension

The first Stage 2 iteration focused on the minimum console-based threaded architecture, with later expansion toward a fuller application structure.
