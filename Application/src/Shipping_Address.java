public class Shipping_Address {
    public String postal_code;
    public String street;
    public String city;
    public String province;
    public String country;


    public Shipping_Address (String p_code, String street, String city, String prov, String country){
        this.postal_code = p_code;
        this.street = street;
        this.city = city;
        this.province = prov;
        this.country = country;
    }
}
