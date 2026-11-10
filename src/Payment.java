/** Result of a simulated payment. Never holds a full card number. */
public class Payment {
    public enum Method { CARD, UPI }

    private final Method method;
    private final String maskedDetail; // "xxxx-1234" or the UPI id
    private final String reference;    // simulated transaction id
    private final double amount;

    public Payment(Method method, String maskedDetail, String reference, double amount) {
        this.method = method;
        this.maskedDetail = maskedDetail;
        this.reference = reference;
        this.amount = amount;
    }

    public Method getMethod() { return method; }
    public String getMaskedDetail() { return maskedDetail; }
    public String getReference() { return reference; }
    public double getAmount() { return amount; }
}
