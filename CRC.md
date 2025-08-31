CRC Cards (Selected)

Class: Student
Responsibilities:
- Hold student profile (name, studentId, passwordHash)
- Track loyalty points and discount wallet
- Provide serialization friendly getters
Collaborators: Order, LoyaltyProgram, StudentRepository

Class: Order
Responsibilities:
- Store order lines and compute total
- Generate unique order ID
- Track OrderStatus
Collaborators: MenuItem, OrderProcessor, OrderRepository

Class: LoyaltyProgram
Responsibilities:
- Calculate points earned from an order
- Redeem points for discounts or free items
Collaborators: PointsCalculator, StudentRepository

Class: OrderProcessor
Responsibilities:
- Place orders, assign ID, persist to repository
- Coordinate payment, awarding points, notifications
Collaborators: MenuProvider, OrderRepository, LoyaltyProgram, NotificationService

