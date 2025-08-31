# University Cafeteria Order & Loyalty System (Console, Java 11+)

## Build & Run
```bash
# compile
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
# run
java -cp out cafeteria.Main
```

- Admin login: `admin` / `admin123`
- Data is in-memory; reports export as CSV to `reports/`.

## Features
- FR1–FR6 implemented: registration/login, menu CRUD, cart & orders, loyalty earn/redeem, staff dashboard, notifications, reporting.
- OOP + SOLID, Strategy / Observer / Repository, Streams & Optionals.

## JavaFX GUI (bonus)
To run the JavaFX demo (`MainFX`), you need a JavaFX SDK and to add JavaFX modules on the module path.

Example (PowerShell) assuming `PATH_TO_FX` points to the JavaFX lib folder:
```powershell
javac --module-path $env:PATH_TO_FX -d out @sources.txt
java --module-path $env:PATH_TO_FX --add-modules javafx.controls -cp out cafeteria.MainFX
```

