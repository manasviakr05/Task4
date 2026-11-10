import java.util.Random;

/** Simulates a payment gateway: validates details and returns a fake transaction. */
public class PaymentProcessor {
    private static final Random RNG = new Random();

    public static Payment process(Payment.Method method, String detail, double amount) throws BookingException {
        if (amount <= 0) throw new BookingException("Invalid payment amount.");
        String masked;
        if (method == Payment.Method.CARD) {
            String digits = detail.replaceAll("[\\s-]", "");
            if (!digits.matches("\\d{16}"))
                throw new BookingException("Payment failed: card number must be 16 digits.");
            if (!luhnValid(digits))
                throw new BookingException("Payment failed: card number is invalid.");
            masked = "xxxx-" + digits.substring(12);
        } else {
            if (!detail.matches("[\\w.\\-]{2,}@[\\w]{2,}"))
                throw new BookingException("Payment failed: UPI id looks invalid (example: name@bank).");
            masked = detail;
        }
        String ref = "TXN" + (100000 + RNG.nextInt(900000));
        return new Payment(method, masked, ref, amount);
    }

    /** Standard card checksum so random digits are rejected. */
    private static boolean luhnValid(String digits) {
        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }
}
