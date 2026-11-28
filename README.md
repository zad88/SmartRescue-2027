# SmartRescue 2027

A Java Swing application for coordinating emergency reports, ambulance
dispatch, on-scene assistance and hospital transfers. Data is stored locally
in CSV files; no database or network service is required.

## Features

- Report incidents with a unique ID, location, description and injured count.
- Classify and verify incidents, then assign an available ambulance.
- Record on-scene assistance and register patients.
- Search for a hospital, create a transfer and confirm patient handover.
- Update hospital beds, emergency capacity and medical specialties.
- Resolve and close incidents, and make completed ambulances available again.

## Architecture

The project is organized into three layers:

1. **Presentation:** four independent Swing interfaces, one per actor.
2. **Logic:** emergency management, dispatch, hospital selection and transfer.
3. **Data:** CSV repositories and persistent ID generation.

The main menu opens the Witness Reporting, Operator Dashboard, Ambulance
Console and Hospital Portal interfaces. It uses seeded demo actors rather than
authentication or staff-management screens.

## Project Structure

```text
SmartRescue-2027/
├── src/
│   └── smartrescue/
│       ├── gui/        Swing interfaces
│       ├── service/    Presentation-facing service
│       ├── emergency/  Emergency and witness logic
│       ├── dispatch/   Operator, ambulance and team logic
│       ├── hospital/   Hospital and staff logic
│       ├── transfer/   Patient and transfer logic
│       └── data/       CSV repositories
├── tests/              Regression and integration tests
├── data/               CSV records and demo actors
└── out/                Locally compiled classes
```

## Requirements and build

Install a JDK (not just a Java runtime), version 21 or later. A graphical
desktop is needed to run the Swing application.

From the project root in PowerShell:

```powershell
java -version
javac -version
New-Item -ItemType Directory -Force out | Out-Null
$sourceFiles = Get-ChildItem src -Recurse -Filter *.java -File
javac -d out $sourceFiles.FullName
```

## Run the application

From the project root, after building:

```powershell
java -cp out smartrescue.gui.SmartRescueApp
```

The working directory must be the project root because the application reads
and writes the relative `data/` directory. Use **Back to Main Menu** to switch
interfaces and **Exit** on the main menu to close the application.

## Emergency workflow

The incident lifecycle is:

```text
REPORTED -> VERIFIED -> RESPONDING -> ON_SCENE -> RESOLVED -> CLOSED
```

1. **Witness Reporting:** enter a location, description and a positive number
   of injured people. Submit the report and note the incident ID.
2. **Operator Dashboard:** select the incident, enter its type and severity,
   then classify and verify it. Choose an available ambulance and assign it;
   the incident changes to `RESPONDING`.
3. **Ambulance Console:** select the assigned ambulance when opening the
   console, select the incident and update it to `ON_SCENE`. Record assistance
   details and the number treated.
4. **If hospital treatment is required:** register each reported patient,
   choose a specialty from the dropdown, search by minimum beds, select a
   matching hospital and create the transfer. Specialty options come from the
   saved hospital records. If transfer is not ready, the console explains
   what is missing. In **Hospital Portal**, select the destination hospital and
   confirm the transfer. Confirmation checks the linked patient count and
   hospital capacity, then updates available beds and capacity.
5. **Resolve and close:** in the ambulance console, resolve the incident after
   assistance is recorded and any transfer is confirmed. An incident without
   a transfer can also be resolved. In the operator dashboard, close the
   `RESOLVED` incident.

The console shows what is missing if an incident is not ready to resolve.
Confirming a hospital transfer does not replace the required on-scene
assistance record.

The ambulance console shows active unassigned incidents and incidents assigned
to the selected ambulance. Unassigned incidents are visible but cannot be
worked on until an operator assigns that ambulance. Completed incidents are
not shown in that console. Only one hospital transfer per incident is
currently supported.

Portals periodically reload saved changes while open; their **Refresh** buttons
can also be used manually. Transfer status appears in the ambulance and
operator screens. Resolving an incident releases its ambulance if it has no
other active incidents. The operator's **Release Completed Ambulances** button
can recover eligible ambulances left dispatched by older completed incidents.

## Data and demo records

| Files in `data/` | Contents |
| --- | --- |
| `emergencies.csv`, `assistance_records.csv` | Incidents and on-scene assistance |
| `ambulances.csv`, `ambulance_teams.csv` | Ambulances and their demo teams |
| `hospitals.csv`, `hospital_staff.csv` | Hospitals and their demo staff |
| `patients.csv`, `hospital_transfers.csv` | Patients and hospital transfers |
| `witnesses.csv`, `operators.csv` | Demo witness and operator |
| `id_counters.csv` | Last-used IDs for generated records |

These are working data files: using the app changes them. New records link to
the relevant demo actor IDs. Older records are migrated when loaded; missing
historical actor IDs remain empty. Back up `data/` before running a version
that migrates existing CSV files. Do not reset the ID counters independently
of existing records, as that can cause ID collisions.

**Privacy:** Some CSV files are tracked by Git, and other CSV files may be
added to the repository. Do not put real patient or other sensitive
information in them or commit it. The application is a local demo and does
not provide authentication or encryption.

## Tests

Compile the tests after building the application:

```powershell
$testFiles = Get-ChildItem tests -Recurse -Filter *.java -File
javac -cp out -d out $testFiles.FullName
```

The five tests cover the emergency lifecycle, complete workflow, ambulance
assignment and release, console visibility, and input/CSV persistence.
For VS Code, configure `java.project.sourcePaths` as `["src", "tests"]` in
workspace settings so Java recognizes the test packages. Workspace IDE
settings are ignored by Git.
Run them in separate empty working directories so they cannot modify live data:

```powershell
$classes = (Resolve-Path out).Path
$tests = @(
    'EmergencyLifecycleTest',
    'ProjectWorkflowIntegrationTest',
    'AmbulanceReleaseIntegrationTest',
    'smartrescue.gui.AmbulanceConsoleVisibilityTest',
    'EmergencyInputPersistenceTest'
)
foreach ($test in $tests) {
    $testDir = Join-Path $env:TEMP ("smartrescue-test-" + [guid]::NewGuid())
    New-Item -ItemType Directory $testDir | Out-Null
    Push-Location $testDir
    try {
        java -cp $classes $test
        if ($LASTEXITCODE -ne 0) { throw "$test failed" }
    } finally { Pop-Location }
}
```

## Git and generated files

`.gitignore` excludes compiled classes, `out/`, `bin/`, IDE settings, logs and
new `.bak` or `.backup` files. Git continues to track files that were already
committed even if they now match an ignore rule; in particular, the existing
`data/emergencies.csv.backup` is still tracked. Review tracked data and backups
before sharing or committing the repository.

## Troubleshooting

- **A button is disabled:** check the incident status, ambulance assignment
  and required patient or transfer records.
- **No ambulance is available:** check the dashboard's availability message.
  An out-of-service or dispatched ambulance cannot be assigned.
- **A confirmed transfer is not visible:** wait for the periodic refresh or
  click **Refresh**, then select the correct incident and ambulance.
- **A hospital is not found:** check its specialty, available beds and
  emergency capacity in the Hospital Portal.
- **The app cannot find data:** run it from the project root and check that
  the required CSV files are present in `data/`.

## GitHub

https://github.com/zad88/SmartRescue-2027