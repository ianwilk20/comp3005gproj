import java.math.BigInteger;

public class BookDetails {
    Long serial_no;
    Long ISBN;
    String book_name;
    String author_name;
    String genre;
    String publisher;
    int no_pages;
    float sales_price;

    public BookDetails(Long s_no, Long isbn, String name, String g, String pub, int num, float price) {
        serial_no = s_no;
        ISBN = isbn;
        book_name = name;
        genre = g;
        publisher = pub;
        no_pages = num;
        sales_price = price;
    }


}
