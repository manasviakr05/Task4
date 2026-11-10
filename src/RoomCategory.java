/** Room types with nightly price (INR) and guest capacity. */
public enum RoomCategory {
    STANDARD(2500, 2),
    DELUXE(4500, 3),
    SUITE(8000, 4);

    private final double pricePerNight;
    private final int capacity;

    RoomCategory(double pricePerNight, int capacity) {
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
    }

    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }

    public static RoomCategory parse(String s) {
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
