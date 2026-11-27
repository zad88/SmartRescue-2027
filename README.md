# SmartRescue 2027

A Java-based Smart City Emergency Response System built with Java 21 and object-oriented programming.

## Architecture

The system follows a three-tier architecture:

1. Presentation Layer
2. Logic Layer
3. Data Layer

## Implemented Features

- Emergency reporting and incident ID generation.
- Emergency classification and verification.
- Emergency lifecycle and status management.
- Ambulance availability and assignment.
- Ambulance team operations and on-scene assistance.
- Patient registration and management.
- Hospital search by specialty and minimum available beds.
- Hospital selection and patient transfer.
- Hospital availability updates and transfer confirmation.
- Emergency resolution and closure.
- CSV-based persistence and repository classes.
- Persistent ID generation.
- Regression and integration tests.

## Technology

- Java 21
- Object-Oriented Programming
- Java Collections
- CSV file persistence
- File I/O
- Git and GitHub
- Java Swing (GUI development pending)

## Project Structure

```text
SmartRescue-2027/
├── src/
│   └── smartrescue/
│       ├── data/
│       ├── dispatch/
│       ├── emergency/
│       ├── hospital/
│       └── transfer/
├── tests/
├── data/
├── out/
├── .gitignore
└── README.md
```

- `src/`: Java source code.
- `tests/`: Regression and integration tests.
- `data/`: CSV persistence files and initial data.
- `out/`: Compiled classes generated locally.

## Build

Requirements: JDK 21 or later.

Run these commands from the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sourceFiles = Get-ChildItem src -Recurse -Filter *.java -File
javac -d out $sourceFiles.FullName
```

## Compile Tests

```powershell
$testFiles = Get-ChildItem tests -Filter *.java -File
javac -cp out -d out $testFiles.FullName
```

Run an individual test using:

```powershell
java -cp out AmbulanceTest
```

The backend regression suite has passed 16 tests.

## Development Progress

- [x] Initial project structure
- [x] UML-based domain classes and inheritance
- [x] Emergency management
- [x] Ambulance and dispatch operations
- [x] Hospital search and selection
- [x] Patient registration and transfer
- [x] Hospital availability and transfer confirmation
- [x] CSV persistence
- [x] Persistent ID generation
- [x] Backend regression testing
- [ ] Swing user interfaces
- [ ] GUI and backend integration

## Planned User Interfaces

- Emergency Reporting
- Operator Dashboard
- Ambulance Console
- Hospital Portal

## GitHub

https://github.com/zad88/SmartRescue-2027