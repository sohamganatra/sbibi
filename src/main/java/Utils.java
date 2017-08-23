import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public class Utils {
    public Utils() {
    }

    public static Boolean checkWordExist(String sentence, String word) {
        return sentence.toLowerCase().contains(" " + word.toLowerCase() + " ") ? Boolean.TRUE : Boolean.FALSE;
    }

    static String sanitizeString(String string) {
        string = replaceRedundantCharacters(string);
        string = removeRedundantSpaces(string);
        return string;
    }

    static String replaceRedundantCharacters(String string) {
        return string.replaceAll("[^A-Za-z0-9 ]", " ");
    }

    static String removeRedundantSpaces(String string) {
        return string.trim().replaceAll(" +", " ");
    }

    static String convertToLakhs(Double value) {
        String result;
        if (value < 100000.0D) {
            result = String.format("%.0f", value);
            return result;
        } else if (value >= 100000.0D & value < 1.0E7D) {
            result = String.format("%.2f", value / 100000.0D) + " L.";
            return result;
        } else {
            result = String.format("%.2f", value / 1.0E7D) + " Cr.";
            return result;
        }
    }

    static String getSanitizedParam(HttpServletRequest request, String param) {
        String paramValue = request.getParameter(param);
        return sanitizeString(paramValue);
    }

    static String addAttributesWithSeparator(Set<String> Attributes, String combiner) {
        String result = " ";
        for(String attribute: Attributes){
            result = result + attribute + " " + combiner + " ";
        }
        result = result.replaceAll(combiner + " " + "$", " ");
        return result;
    }


    static String addAttributes(HashSet<String> Attributes, String field, String combiner) {
        String result = " ";
        Iterator var4;
        String Attribute;
        if (field.equals("")) {
            for(var4 = Attributes.iterator(); var4.hasNext(); result = result + Attribute + " " + combiner + " ") {
                Attribute = (String)var4.next();
            }
        } else {
            for(var4 = Attributes.iterator(); var4.hasNext(); result = result + field + "='" + Attribute + "' " + combiner + " ") {
                Attribute = (String)var4.next();
            }
        }

        result = result.replaceAll(combiner + "$", " ");
        return result;
    }

    static String addAttributes(String Attribute, String field) {
        String result = " ";
        if (field.equals("")) {
            result = result + Attribute;
        } else {
            result = result + field + "='" + Attribute + "' ";
        }

        return result;
    }
}
