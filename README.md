# Hotel Reservation System (Java, OOP + File I/O)

Console-based system to search, book and manage hotel rooms.

## Run
```
mkdir out
javac -d out src/*.java
java -cp out Main
```
Requires JDK 11+ (tested on 21).

## Features
- 9 rooms in 3 categories: Standard (Rs.2,500), Deluxe (Rs.4,500), Suite (Rs.8,000) per night
- Search available rooms by date range and category
- Book a room: date validation, double-booking prevention, total cost quote
- Payment simulation: Card (16 digits + Luhn check) or UPI id; only last 4 card digits are stored
- Booking details view and list of all bookings
- Cancellation with refund policy: 100% if 2+ days before check-in, else 50%
- File I/O: bookings saved to data/bookings.txt; availability is derived from saved bookings

## Test card number
4111111111111111 (passes the checksum). UPI example: name@bank

## OOP design
| Class | Responsibility |
|---|---|
| RoomCategory (enum) | Category, nightly price, capacity |
| Room | Room number + category |
| Booking | One reservation, status, date-overlap check |
| Payment / PaymentProcessor | Simulated gateway and its result |
| Hotel | Rooms, bookings, search / reserve / cancel / refund |
| BookingException | Custom checked exception |
| PersistenceManager | Save/load bookings |
| Main | Console menu |
