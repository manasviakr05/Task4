/** A physical hotel room. Availability is derived from bookings, not stored here. */
public class Room {
    private final int number;
    private final RoomCategory category;

    public Room(int number, RoomCategory category) {
        this.number = number;
        this.category = category;
    }

    public int getNumber() { return number; }
    public RoomCategory getCategory() { return category; }
    public double getPricePerNight() { return category.getPricePerNight(); }

    @Override
    public String toString() {
        return String.format("Room %d  %-8s  Rs.%,.0f/night  (up to %d guests)",
                number, category, getPricePerNight(), category.getCapacity());
    }
}
