Grading Mapping (FR -> Files)

FR1.1 Register student: `Services.java` -> `StudentManager.register` and `Student.java`
FR1.2 Secure login: `Services.java` -> `StudentManager.login` (uses `HashUtil`)
FR1.3 Display points: `Student.getPoints`, `LoyaltyProgram.pointsOf`

FR2.1 Menu CRUD: `MenuManager` in `Services.java`, `InMemoryMenuProvider.java`
FR2.2 MenuItem fields: `MenuItem.java`
FR2.3 Menu display: `MenuManager.getMenu` + `Main.java`

FR3.1 Browse & select: `Main.java` and `OrderLine.java`
FR3.2 Total cost: `Order.getTotal` (streams)
FR3.3 Place order: `OrderProcessor.placeOrder` (in `Services.java`)
FR3.4 Order ID: `Order` uses UUID

FR4.1 Earn points: `BasicPointsCalculator` + `LoyaltyProgram.awardPoints`
FR4.2 Redeem: `LoyaltyProgram.redeemDiscount` and `redeemFreeItem`
FR4.3 Deduct: `Student.deductPoints`

FR5.1 Staff pending orders: `OrderRepository.byStatus` and `OrderProcessor.listByStatus`
FR5.2 Mark statuses: `OrderProcessor.updateStatus`
FR5.3 Notify ready: `NotificationService.notifyReady`

FR6.1 Reporting: `ReportService.dailySummary`, `weeklySummary`, `exportCsv`

Design notes:
- Strategy pattern: `PointsCalculator`, `PaymentProcessor` implementations.
- Repository pattern: interfaces `MenuProvider`, `StudentRepository`, `OrderRepository` with in-memory implementations.
- Streams used for totals and reporting.

