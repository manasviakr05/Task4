import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

/** Saves/loads bookings to data/bookings.txt. Availability is derived from these bookings. */
public class PersistenceManager {
    private static final Path FILE = Paths.get("data", "bookings.txt");

    private static String clean(String s) { return s.replace("|", " ").trim(); }

    public static void save(Hotel hotel) throws IOException {
        Files.createDirectories(FILE.getParent());
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(FILE))) {
            for (Booking b : hotel.getBookings()) {
                Payment p = b.getPayment();
                out.println(String.join("|",
                        b.getId(), clean(b.getGuestName()), clean(b.getPhone()),
                        String.valueOf(b.getRoom().getNumber()),
                        b.getCheckIn().toString(), b.getCheckOut().toString(),
                        b.getStatus().name(), String.valueOf(b.getRefundAmount()),
                        p.getMethod().name(), clean(p.getMaskedDetail()), p.getReference(),
                        String.valueOf(p.getAmount())));
            }
        }
    }

    public static int load(Hotel hotel) throws IOException {
        if (!Files.exists(FILE)) return 0;
        int count = 0;
        List<String> lines = Files.readAllLines(FILE);
        for (String line : lines) {
            if (line.isBlank()) continue;
            String[] f = line.split("\\|");
            Room room = hotel.findRoom(Integer.parseInt(f[3]));
            if (room == null) continue;
            Payment payment = new Payment(Payment.Method.valueOf(f[8]), f[9], f[10], Double.parseDouble(f[11]));
            hotel.restoreBooking(new Booking(f[0], f[1], f[2], room,
                    LocalDate.parse(f[4]), LocalDate.parse(f[5]),
                    payment, Booking.Status.valueOf(f[6]), Double.parseDouble(f[7])));
            count++;
        }
        return count;
    }
}
