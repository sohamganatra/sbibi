import java.util.HashSet;

public class SubProduct {
    private String subProductName;
    private HashSet<String> subProductWords;
    private String subProductField;
    private String subProductCode;

    public SubProduct() {
    }

    public String getSubProductCode() {
        return this.subProductCode;
    }

    public void setSubProductCode(String subProductCode) {
        this.subProductCode = subProductCode;
    }

    public String getSubProductName() {
        return this.subProductName;
    }

    public void setSubProductName(String subProductName) {
        this.subProductName = subProductName;
    }

    public HashSet<String> getSubProductWords() {
        return this.subProductWords;
    }

    public void setSubProductWords(HashSet<String> subProductWords) {
        this.subProductWords = subProductWords;
    }

    public String getSubProductField() {
        return this.subProductField;
    }

    public void setSubProductField(String subProductField) {
        this.subProductField = subProductField;
    }
}
