import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** A reservation of one room for a date range (check-out day is not occupied). */
public class Booking {
    public enum Status { CONFIRMED, CANCELLED }

    private final String id;
    private final String guestName;
    private final String phone;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final Payment payment;
    private Status status;
    private double refundAmount;

    public Booking(String id, String guestName, String phone, Room room,
                   LocalDate checkIn, LocalDate checkOut, Payment payment,
                   Status status, double refundAmount) {
        this.id = id;
        this.guestName = guestName;
        this.phone = phone;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.payment = payment;
        this.status = status;
        this.refundAmount = refundAmount;
    }

    public long getNights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }

    /** True if this booking blocks the room for any night in [in, out). */
    public boolean overlaps(LocalDate in, LocalDate out) {
        return status == Status.CONFIRMED && in.isBefore(checkOut) && out.isAfter(checkIn);
    }

    public void cancel(double refund) {
        this.status = Status.CANCELLED;
        this.refundAmount = refund;
    }

    public void display() {
        System.out.println("\n--------- BOOKING " + id + " ---------");
        System.out.println("Status:    " + status);
        System.out.println("Guest:     " + guestName + " (" + phone + ")");
        System.out.printf("Room:      %d (%s)%n", room.getNumber(), room.getCategory());
        System.out.printf("Stay:      %s to %s (%d night%s)%n", checkIn, checkOut, getNights(), getNights() == 1 ? "" : "s");
        System.out.printf("Paid:      Rs.%,.2f via %s %s (ref %s)%n", payment.getAmount(),
                payment.getMethod(), payment.getMaskedDetail(), payment.getReference());
        if (status == Status.CANCELLED) System.out.printf("Refunded:  Rs.%,.2f%n", refundAmount);
        System.out.println("-----------------------------------");
    }

    public String getId() { return id; }
    public String getGuestName() { return guestName; }
    public String getPhone() { return phone; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public Payment getPayment() { return payment; }
    public Status getStatus() { return status; }
    public double getRefundAmount() { return refundAmount; }
}
