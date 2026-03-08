# Coffee Shop Ordering System — Stage 1

**Course:** F21AS Advanced Software Engineering  
**University:** Heriot-Watt University  
**Group:** Edinburgh CW 5  
**Members:** Wenbo Geng, Yize Dai, Boyuan Feng, Kefan Pu, Yi Lin

---

## What This Program Does

This is a coffee shop simulation that:

1. Reads a menu and existing customer orders from CSV files
2. Displays a GUI where users can select items and generate a bill (with automatic discount calculation)
3. Generates a summary report when the application exits

---

## Project Structure

```
AS-Assignment/
├── data/
│   ├── menu.csv                    ← Menu items (20 valid + 1 invalid for testing)
│   └── orders.csv                  ← Existing orders (17 valid + 1 invalid)
│
├── src/main/java/coffeeshop/
│   ├── App.java                    ← Main entry point (run this)
│   ├── api/
│   │   ├── Bill.java               ← Bill data object
│   │   ├── Category.java           ← Category enum
│   │   ├── CoffeeShopService.java  ← Service interface
│   │   ├── DiscountCalculator.java ← Discount rules (20% / 15% / 10%)
│   │   ├── InvalidItemIdException.java ← Custom exception
│   │   └── MenuItemView.java       ← Menu item DTO for GUI
│   ├── model/
│   │   ├── MenuItem.java           ← Menu item model (used by loaders)
│   │   └── OrderRecord.java        ← Order record model
│   ├── loader/
│   │   ├── MenuLoader.java         ← Reads menu.csv
│   │   └── OrderLoader.java        ← Reads orders.csv
│   ├── util/
│   │   └── IdValidator.java        ← Validates ID format: <ALPHA>-<3 digits>
│   ├── service/
│   │   └── CoffeeShopServiceImpl.java ← Connects all modules together
│   ├── report/
│   │   └── ReportGenerator.java    ← Generates summary report on exit
│   └── gui/
│       └── MainFrame.java          ← Swing GUI
│
└── src/test/java/coffeeshop/
    └── ReportGeneratorTest.java    ← JUnit 5 tests (19 test cases)
```

---

## How to Run in Eclipse

### Step 1: Import the Project

1. **File → Import → General → Existing Projects into Workspace**
2. Select the root directory of this repository
3. Click **Finish**

### Step 2: Run the Application

1. Navigate to `src/main/java` → `coffeeshop` → **App.java**
2. Right-click **App.java** → **Run As → Java Application**
3. The GUI window will appear and the console will show the loaded data summary

### Step 3: Run JUnit Tests

1. Navigate to `src/test/java` → `coffeeshop` → **ReportGeneratorTest.java**
2. Right-click → **Run As → JUnit Test**
3. All 19 tests should pass (green)

---

## How to Use the Application

### On Startup

The console will display:

```
[MenuLoader] Skipped invalid line: BADID-1,Invalid ID Test,1.00,Other
[OrderLoader] Skipped invalid line: 2026-02-10 10:03,CUST009,
[Service] Loaded 20 menu items, 17 order records.
```

This confirms that the CSV files are loaded correctly and invalid entries are skipped.

### Placing an Order

1. A window titled **"Coffee Shop Ordering System"** appears with a list of menu items
2. Hold **Ctrl** (or **Cmd** on Mac) and click to select multiple items
3. Click **"Generate Bill"**
4. A dialog displays the bill:
   - Each selected item with its price
   - Subtotal before discount
   - Discount amount and which rule was applied
   - Final total after discount

### Discount Rules

| Rule | Condition | Discount |
|------|-----------|----------|
| Rule 1 | 1+ beverage AND 2+ food items | 20% off |
| Rule 2 | 3+ beverages | 15% off |
| Rule 3 | Subtotal exceeds £25 | 10% off |

Rules are evaluated in priority order (Rule 1 > Rule 2 > Rule 3). Only the first matching rule is applied.

### Exiting and Generating the Report

1. Click **"Exit & Generate Report"** (or close the window)
2. A summary report is printed to the console and saved to `reports/report.txt`
3. The report contains:
   - A full list of all menu items
   - How many times each item was ordered
   - Total revenue after discounts

---

## Test Data

### menu.csv (21 lines)

Contains 20 valid menu items across 3 categories, plus 1 intentionally invalid entry:

| Category | Items | ID Examples |
|----------|-------|-------------|
| Beverage | 8 items | BEV-001 to BEV-008 |
| Food | 7 items | FOOD-001 to FOOD-007 |
| Other | 5 items | OTH-001 to OTH-005 |
| Invalid | 1 item | BADID-1 (skipped during loading) |

### orders.csv (18 lines)

Contains 17 valid orders from 8 customers, plus 1 line with an empty item ID (skipped during loading). The data covers various discount scenarios:

- CUST001: 1 beverage + 2 food items → triggers 20% discount
- CUST003: 3 beverages → triggers 15% discount
- CUST005: 1 item only → no discount

---

## Technologies Used

- **Java** (JDK 17+)
- **Java Swing** for the GUI
- **JUnit 5** for unit testing
- **Git/GitHub** for version control

---

## Troubleshooting

If you encounter errors after importing, try the following:

| Problem | Solution |
|---------|----------|
| `Error! Cannot read file: data/menu.csv` | The working directory may not be correct. In Eclipse: **Run → Run Configurations → Arguments tab** → set Working Directory to `${workspace_loc}/[project-name]/AS-Assignment` |
| `coffeeshop.api cannot be resolved` | Go to **Properties → Java Build Path → Source tab**. Remove any extra source folders such as `coffeeshop/api` or `coffeeshop/data_loader/src`. Keep only `AS-Assignment/src/main/java` and `AS-Assignment/src/test/java`. Also check that `coffeeshop/api/` is not listed under Excluded patterns |
| `BeforeEach cannot be resolved` / `Test cannot be resolved` | Go to **Properties → Java Build Path → Libraries tab** → click Classpath → **Add Library → JUnit → JUnit 5** → Finish |
| 100+ errors on import | If two copies of the project are open in Eclipse, right-click the unused one → **Close Project** |
