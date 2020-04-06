public class OrderDetails {
    public Long order_no;
    public Long serial_no;
    public String book_name;
    public String author_name;
    public String genre;
    public int no_pages;
    public float sales_price;

    public OrderDetails(Long o_no, Long s_no, String b_name, String a_name, String g, int num, float price) {
        order_no = o_no;
        serial_no = s_no;
        book_name = b_name;
        author_name = a_name;
        genre = g;
        no_pages = num;
        sales_price = price;
    }

    @Override
    public String toString() {
        return  order_no + " " +
                serial_no + " " +
                book_name + " " +
                author_name + " " +
                genre + " " +
                no_pages + " " +
                sales_price;
    }
}
