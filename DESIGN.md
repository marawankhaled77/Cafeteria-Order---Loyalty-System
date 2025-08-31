# Design, Abbott’s, SOLID, CRC, UML (ASCII)

## Abbott's Technique
- **Nouns → Classes**: Student, MenuItem, Order, OrderLine, LoyaltyProgram, PointsCalculator, MenuManager, OrderProcessor, StudentManager, ReportService, NotificationService.
- **Verbs → Methods**: register, login, addMenuItem, editMenuItem, removeMenuItem, browse, addToCart, placeOrder, redeemPoints, markPreparing, markReady, notify, summarize.
- **Scenarios**: Student browses menu, builds order, places it → points awarded; Student redeems points → discount; Staff marks orders and reports.

## CRC (examples)
- **Order**: responsibilities: hold lines, total(), id, status. collaborators: MenuItem, LoyaltyProgram, OrderProcessor.
- **LoyaltyProgram**: responsibilities: earnPoints(total), applyRedemption(student, order). collaborators: Student, PointsCalculator.
- **OrderProcessor**: responsibilities: placeOrder, updateStatus, listPending. collaborators: StudentRepository, MenuProvider, LoyaltyProgram, NotificationService.

## SOLID
- **SRP**: managers/services do one job each.
- **OCP**: `PointsCalculator` strategy (basic/tiered), `PaymentProcessor` interface.
- **LSP**: any `PaymentProcessor` fits where `process(total)` is needed.
- **ISP**: thin interfaces (`MenuProvider`, `StudentRepository`).
- **DIP**: services depend on interfaces, injected in `Main`.

## Patterns
- Strategy (`PointsCalculator`), Observer (`NotificationService`), Repository (in‑memory), Factory (simple seeds).

## Simple UML (ASCII)
```
Student --(has many)--> Order
Order --(has many)--> OrderLine --> MenuItem
OrderProcessor --> LoyaltyProgram --> PointsCalculator
OrderProcessor --> NotificationService (notifyReady)
OrderProcessor --> StudentRepository
MenuManager implements MenuProvider
```

## Streams
- Sum totals via `lines.stream().mapToDouble(...).sum()`
- Reports via grouping by date using streams.
