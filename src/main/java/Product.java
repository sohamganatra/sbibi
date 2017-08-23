
import java.util.HashMap;
import java.util.HashSet;

public class Product {
    private String productName;
    private HashSet<String> productKeywords;
    private String productTable;
    private HashSet<SubProduct> subProducts;
    private HashMap<String,String> attribute;
    private String productDayView;
    private String productMonthView;
    private String productYearView;

    public Product() {
    }

    public String getProductDayView() {
        return this.productDayView;
    }

    public void setProductDayView(String productDayView) {
        this.productDayView = productDayView;
    }

    public String getProductMonthView() {
        return this.productMonthView;
    }

    public void setProductMonthView(String productMonthView) {
        this.productMonthView = productMonthView;
    }

    public String getProductYearView() {
        return this.productYearView;
    }

    public void setProductYearView(String productYearView) {
        this.productYearView = productYearView;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public HashSet<String> getProductKeywords() {
        return this.productKeywords;
    }

    public void setProductKeywords(HashSet<String> productKeywords) {
        this.productKeywords = productKeywords;
    }

    public String getProductTable() {
        return this.productTable;
    }

    public void setProductTable(String productTable) {
        this.productTable = productTable;
    }

    public HashSet<SubProduct> getSubProducts() {
        return this.subProducts;
    }

    public void setSubProducts(HashSet<SubProduct> subProducts) {
        this.subProducts = subProducts;
    }

    public HashMap<String,String> getAttribute() {
        return this.attribute;
    }

    public void setAttribute(HashMap<String,String> attribute) {
        this.attribute = attribute;
    }
}
