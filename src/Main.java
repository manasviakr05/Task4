import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Console UI for the hotel reservation system. */
public class Main {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Hotel hotel = new Hotel("Grand Palace Hotel");
        try {
            int n = PersistenceManager.load(hotel);
            if (n > 0) System.out.println("Loaded " + n + " saved booking(s).");
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not read saved bookings (" + e.getMessage() + "). Starting empty.");
        }
        System.out.println("=== " + hotel.getName().toUpperCase() + " - RESERVATION SYSTEM ===");

        boolean running = true;
        while (running) {
            printMenu();
            if (!in.hasNextLine()) break;
            switch (in.nextLine().trim()) {
                case "1": listRooms(hotel); break;
                case "2": search(hotel); break;
                case "3": book(hotel); break;
                case "4": viewBooking(hotel); break;
                case "5": cancel(hotel); break;
                case "6": listBookings(hotel); break;
                case "7": running = false; break;
                default: System.out.println("Invalid option.");
            }
        }
        try {
            PersistenceManager.save(hotel);
            System.out.println("Bookings saved. Goodbye!");
        } catch (IOException e) {
            System.out.println("Could not save bookings: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. View all rooms");
        System.out.println("2. Search available rooms");
        System.out.println("3. Book a room");
        System.out.println("4. View booking details");
        System.out.println("5. Cancel a booking");
        System.out.println("6. List all bookings");
        System.out.println("7. Save & exit");
        System.out.print("Choose: ");
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    private static LocalDate askDate(String prompt) {
        try {
            return LocalDate.parse(ask(prompt + " (yyyy-mm-dd): "));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            return null;
        }
    }

    private static void listRooms(Hotel hotel) {
        System.out.println("\n=== ALL ROOMS ===");
        for (Room r : hotel.getRooms()) System.out.println(r);
    }

    private static void search(Hotel hotel) {
        LocalDate checkIn = askDate("Check-in");
        LocalDate checkOut = checkIn == null ? null : askDate("Check-out");
        if (checkIn == null || checkOut == null) return;
        String cat = ask("Category (Standard/Deluxe/Suite, or Enter for any): ");
        RoomCategory category = cat.isEmpty() ? null : RoomCategory.parse(cat);
        if (!cat.isEmpty() && category == null) { System.out.println("Unknown category."); return; }
        try {
            Hotel.validateDates(checkIn, checkOut);
        } catch (BookingException e) { System.out.println(e.getMessage()); return; }

        List<Room> free = hotel.search(category, checkIn, checkOut);
        System.out.println("\n=== AVAILABLE ROOMS ===");
        if (free.isEmpty()) System.out.println("No rooms available for those dates.");
        for (Room r : free)
            System.out.printf("%s  | total Rs.%,.0f%n", r, hotel.quote(r, checkIn, checkOut));
    }

    private static void book(Hotel hotel) {
        String guest = ask("Guest name: ");
        String phone = ask("Phone: ");
        if (guest.isEmpty() || phone.isEmpty()) { System.out.println("Name and phone are required."); return; }
        LocalDate checkIn = askDate("Check-in");
        LocalDate checkOut = checkIn == null ? null : askDate("Check-out");
        if (checkIn == null || checkOut == null) return;
        try {
            Hotel.validateDates(checkIn, checkOut);
            String cat = ask("Category (Standard/Deluxe/Suite): ");
            RoomCategory category = RoomCategory.parse(cat);
            if (category == null) throw new BookingException("Unknown category.");
            List<Room> free = hotel.search(category, checkIn, checkOut);
            if (free.isEmpty()) throw new BookingException("No " + category + " rooms available for those dates.");
            for (Room r : free)
                System.out.printf("%s  | total Rs.%,.0f%n", r, hotel.quote(r, checkIn, checkOut));

            int number;
            try {
                number = Integer.parseInt(ask("Room number to book: "));
            } catch (NumberFormatException e) { throw new BookingException("Enter a valid room number."); }
            Room room = hotel.findRoom(number);
            if (room == null || !free.contains(room)) throw new BookingException("That room is not in the available list.");

            double amount = hotel.quote(room, checkIn, checkOut);
            System.out.printf("%nAmount due: Rs.%,.2f%n", amount);
            Payment payment = pay(amount);
            Booking b = hotel.reserve(guest, phone, room, checkIn, checkOut, payment);
            System.out.println("\nPayment successful! Booking confirmed.");
            b.display();
        } catch (BookingException e) {
            System.out.println("Booking not completed: " + e.getMessage());
        }
    }

    private static Payment pay(double amount) throws BookingException {
        String m = ask("Payment method (1 = Card, 2 = UPI): ");
        if (m.equals("1"))
            return PaymentProcessor.process(Payment.Method.CARD, ask("Card number (16 digits): "), amount);
        if (m.equals("2"))
            return PaymentProcessor.process(Payment.Method.UPI, ask("UPI id: "), amount);
        throw new BookingException("Invalid payment method.");
    }

    private static void viewBooking(Hotel hotel) {
        Booking b = hotel.findBooking(ask("Booking id (e.g. BK0001): "));
        if (b == null) System.out.println("No booking found.");
        else b.display();
    }

    private static void cancel(Hotel hotel) {
        String id = ask("Booking id to cancel: ");
        Booking b = hotel.findBooking(id);
        if (b == null) { System.out.println("No booking found."); return; }
        if (b.getStatus() == Booking.Status.CONFIRMED)
            System.out.printf("Refund if cancelled now: Rs.%,.2f (100%% if 2+ days before check-in, else 50%%)%n",
                    hotel.refundFor(b));
        if (!ask("Confirm cancellation? (y/n): ").equalsIgnoreCase("y")) { System.out.println("Not cancelled."); return; }
        try {
            b = hotel.cancel(id);
            System.out.println("Booking cancelled.");
            b.display();
        } catch (BookingException e) {
            System.out.println("Could not cancel: " + e.getMessage());
        }
    }

    private static void listBookings(Hotel hotel) {
        System.out.println("\n=== ALL BOOKINGS ===");
        if (hotel.getBookings().isEmpty()) { System.out.println("(none)"); return; }
        System.out.printf("%-7s %-16s %-5s %-8s %-10s %-10s %s%n", "ID", "GUEST", "ROOM", "TYPE", "CHECK-IN", "CHECK-OUT", "STATUS");
        for (Booking b : hotel.getBookings())
            System.out.printf("%-7s %-16s %-5d %-8s %-10s %-10s %s%n", b.getId(), b.getGuestName(),
                    b.getRoom().getNumber(), b.getRoom().getCategory(), b.getCheckIn(), b.getCheckOut(), b.getStatus());
    }
}
