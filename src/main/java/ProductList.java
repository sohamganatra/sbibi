import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

@WebListener
public class ProductList implements ServletContextListener {
    protected static Logger LOGGER = Logger.getLogger(ProductList.class);
    protected static HashSet<Product> Products = new HashSet();
    private static String productDirectoryName;

    public ProductList() {
    }

    private static void initProductList() {
        LOGGER.info("Initializing the sub Product List");
        File productDirectory = new File(ProductList.class.getResource(productDirectoryName).getFile());
        if (!productDirectory.isDirectory()) {
            LOGGER.error("Error: The ProductDirectory is not present. The program will not work without it.");
        }

        File[] productFiles = productDirectory.listFiles();
        if (productFiles != null) {
            File[] var2 = productFiles;
            int var3 = productFiles.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                File productFile = var2[var4];
                LOGGER.info("In the file: " + productFile.getName());

                try {
                    List<String> lines = FileUtils.readLines(productFile, "UTF-8");
                    if (lines.size() < 8) {
                        LOGGER.error("Error: The file is not having complete content size: " + lines.size() + " name: " + productFile.getName());
                    } else if (lines.size() != 8) {
                        LOGGER.error("Error: The file is having extra complete content size: " + lines.size() + " name: " + productFile.getName());
                    } else {
                        Product product = new Product();
                        product.setProductName(lines.get(0));
                        product.setProductTable(lines.get(1));
                        product.setProductDayView(lines.get(2));
                        product.setProductMonthView(lines.get(3));
                        product.setProductYearView(lines.get(4));
                        HashSet keyWords = new HashSet(Arrays.asList(lines.get(5).split(",")));
                        product.setProductKeywords(keyWords);
                        HashSet subProductNames = new HashSet(Arrays.asList(lines.get(6).split(",")));
                        HashSet<SubProduct> subProducts = new HashSet();
                        Iterator var11 = subProductNames.iterator();

                        while(var11.hasNext()) {
                            String subProductName = (String)var11.next();
                            Iterator var13 = SubProductList.getSubProducts().iterator();

                            while(var13.hasNext()) {
                                SubProduct subProduct = (SubProduct)var13.next();
                                if (subProduct.getSubProductName().equalsIgnoreCase(subProductName)) {
                                    subProducts.add(subProduct);
                                }
                            }
                        }

                        product.setSubProducts(subProducts);
                        HashSet<String> attributeSet = new HashSet(Arrays.asList(lines.get(7).split(",")));
                        HashMap<String,String> attributeMap = new HashMap<>();
                        for(String attribute: attributeSet){
                            attributeMap.put(Arrays.asList(attribute.split(":")).get(0),Arrays.asList(attribute.split(":")).get(1));
                        }
                        product.setAttribute(attributeMap);
                        Products.add(product);
                        LOGGER.info("Addition of Product Completed " + product.getProductName());
                    }
                } catch (IOException var15) {
                    LOGGER.error("Error: Exception in product IO: ", var15);
                }
            }

            LOGGER.info("All Products added");
        } else {
            LOGGER.error("Error: The Product files is null");
        }

    }

    public static Product extractProduct(String sentence) {
        Iterator var1 = Products.iterator();

        while(var1.hasNext()) {
            Product product = (Product)var1.next();
            LOGGER.debug("Checking for the productName: " + product.getProductName());
            Iterator var3 = product.getProductKeywords().iterator();

            while(var3.hasNext()) {
                String productKeyWordName = (String)var3.next();
                LOGGER.debug("checking the productKeyword name : " + productKeyWordName);
                if (Utils.checkWordExist(sentence, productKeyWordName)) {
                    LOGGER.debug("The keyword: " + productKeyWordName + " matched. Returning product: " + product.getProductName());
                    return product;
                }
            }
        }

        LOGGER.error("Error: Unable to determine the product of the sentence: " + sentence);
        if (Products.size() == 0) {
            LOGGER.error("The size of the products is zero. ");
            return new Product();
        } else {
            return Products.iterator().next();
        }
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SubProductList.initSubProductList();
        initProductList();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    static {
        productDirectoryName = Config.productDirectory;
    }
}
