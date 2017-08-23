import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class SubProductList {
    private static Logger LOGGER = Logger.getLogger(SubProductList.class);
    private static HashSet<SubProduct> subProducts = new HashSet();
    private static String subProductDirectoryName;

    public SubProductList() {
    }

    public static HashSet<SubProduct> getSubProducts() {
        return subProducts;
    }

    public static void setSubProducts(HashSet<SubProduct> subProducts) {
        subProducts = subProducts;
    }

    static void initSubProductList() {
        LOGGER.info("Initializing the sub Product List");
        File subProductDirectory = new File(SubProductList.class.getResource(subProductDirectoryName).getFile());
        if (!subProductDirectory.isDirectory()) {
            LOGGER.error("Error: The subProductDirectory is not present. The program will not work without it.");
        }

        File[] subProductFiles = subProductDirectory.listFiles();
        if (subProductFiles != null) {
            File[] var2 = subProductFiles;
            int var3 = subProductFiles.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                File subProductFile = var2[var4];

                try {
                    List<String> lines = FileUtils.readLines(subProductFile, "UTF-8");
                    if (lines.size() < 4) {
                        LOGGER.error("Error: The file is not having complete content size: " + lines.size() + " name: " + subProductFile.getName());
                    } else if (lines.size() == 4) {
                        SubProduct subProduct = new SubProduct();
                        subProduct.setSubProductName(lines.get(0));
                        subProduct.setSubProductCode(lines.get(1));
                        subProduct.setSubProductField(lines.get(2));
                        HashSet<String> subProductWordList = new HashSet(Arrays.asList(lines.get(3).split(",")));
                        subProduct.setSubProductWords(subProductWordList);
                        subProducts.add(subProduct);
                        LOGGER.info("Addition of subProduct Completed " + subProduct.getSubProductName());
                    } else {
                        LOGGER.error("Error: The file is having extra complete content size: " + lines.size() + " name: " + subProductFile.getName());
                    }
                } catch (IOException e) {
                    LOGGER.error("Error: Exception in SubProduct IO: ", e);
                }
            }

            LOGGER.info("All subProducts added");
        } else {
            LOGGER.error("Error: The subproduct files is null");
        }

    }

    public static SubProduct extractSubProduct(String sentence, Product product) {
        for(SubProduct subProduct : product.getSubProducts()){
            LOGGER.debug("Checking for the subProductName: " + subProduct.getSubProductName());
            for(String subProductKeyWordName: subProduct.getSubProductWords()){
                LOGGER.debug("checking the subProductKeyword name : " + subProductKeyWordName);
                if (Utils.checkWordExist(sentence, subProductKeyWordName)) {
                    LOGGER.debug("The keyword: " + subProductKeyWordName + " matched. Returning product: " + subProduct.getSubProductName());
                    return subProduct;
                }
            }
        }

        LOGGER.error("Error: Unable to determine the subProduct of the sentence: " + sentence);
        if (subProducts.size() == 0) {
            LOGGER.error("The size of the subProducts is zero. ");
            return null;
        } else {
            return null;
        }
    }

    static {
        subProductDirectoryName = Config.subProductDirectory;
    }
}
