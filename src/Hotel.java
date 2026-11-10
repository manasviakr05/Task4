import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Owns the rooms and bookings; implements search, reserve and cancel. */
public class Hotel {
    private final String name;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private int nextId = 1;

    public Hotel(String name) {
        this.name = name;
        for (int n = 101; n <= 104; n++) rooms.add(new Room(n, RoomCategory.STANDARD));
        for (int n = 201; n <= 203; n++) rooms.add(new Room(n, RoomCategory.DELUXE));
        for (int n = 301; n <= 302; n++) rooms.add(new Room(n, RoomCategory.SUITE));
    }

    public String getName() { return name; }
    public List<Room> getRooms() { return rooms; }
    public List<Booking> getBookings() { return bookings; }

    public Room findRoom(int number) {
        for (Room r : rooms) if (r.getNumber() == number) return r;
        return null;
    }

    public Booking findBooking(String id) {
        for (Booking b : bookings) if (b.getId().equalsIgnoreCase(id.trim())) return b;
        return null;
    }

    public static void validateDates(LocalDate in, LocalDate out) throws BookingException {
        if (in.isBefore(LocalDate.now())) throw new BookingException("Check-in date cannot be in the past.");
        if (!out.isAfter(in)) throw new BookingException("Check-out must be after check-in.");
    }

    public boolean isAvailable(Room room, LocalDate in, LocalDate out) {
        for (Booking b : bookings)
            if (b.getRoom().getNumber() == room.getNumber() && b.overlaps(in, out)) return false;
        return true;
    }

    /** Free rooms for the dates; category may be null for "any". */
    public List<Room> search(RoomCategory category, LocalDate in, LocalDate out) {
        List<Room> result = new ArrayList<>();
        for (Room r : rooms)
            if ((category == null || r.getCategory() == category) && isAvailable(r, in, out)) result.add(r);
        return result;
    }

    public double quote(Room room, LocalDate in, LocalDate out) {
        return ChronoUnit.DAYS.between(in, out) * room.getPricePerNight();
    }

    /** Creates a confirmed booking after payment succeeded. Re-checks availability. */
    public Booking reserve(String guest, String phone, Room room, LocalDate in, LocalDate out,
                           Payment payment) throws BookingException {
        validateDates(in, out);
        if (!isAvailable(room, in, out)) throw new BookingException("Room " + room.getNumber() + " is no longer available.");
        Booking b = new Booking(String.format("BK%04d", nextId++), guest, phone, room, in, out,
                payment, Booking.Status.CONFIRMED, 0);
        bookings.add(b);
        return b;
    }

    /** Refund policy: 100% if 2+ days before check-in, otherwise 50%. */
    public double refundFor(Booking b) {
        long daysBefore = ChronoUnit.DAYS.between(LocalDate.now(), b.getCheckIn());
        double paid = b.getPayment().getAmount();
        return daysBefore >= 2 ? paid : paid * 0.5;
    }

    public Booking cancel(String id) throws BookingException {
        Booking b = findBooking(id);
        if (b == null) throw new BookingException("No booking found with id " + id + ".");
        if (b.getStatus() == Booking.Status.CANCELLED) throw new BookingException("Booking is already cancelled.");
        if (!b.getCheckIn().isAfter(LocalDate.now().minusDays(1)) && b.getCheckOut().isBefore(LocalDate.now()))
            throw new BookingException("Past stays cannot be cancelled.");
        b.cancel(refundFor(b));
        return b;
    }

    /** Used by persistence to restore saved bookings. */
    public void restoreBooking(Booking b) {
        bookings.add(b);
        try {
            nextId = Math.max(nextId, Integer.parseInt(b.getId().substring(2)) + 1);
        } catch (NumberFormatException ignored) { }
    }
}
