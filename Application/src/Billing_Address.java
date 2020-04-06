public class Billing_Address extends Shipping_Address {
    public Long credit_card;

    public Billing_Address(String p_code, String street, String city, String prov, String country, Long card) {
        super(p_code, street, city, prov, country);
        this.credit_card = card;
    }
}
