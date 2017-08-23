import java.util.HashSet;
import java.util.Iterator;
import org.apache.log4j.Logger;


public class AggregatePicker {
    private static Logger LOGGER;
    protected static HashSet<String> keyWordSet;

    public AggregatePicker() {
    }

    public static void initAggregatePicker() {
        keyWordSet.add("growth");
    }

    public static String AggregateExtractor(String sentence) {
        Iterator var1 = keyWordSet.iterator();

        String keyword;
        do {
            if (!var1.hasNext()) {
                return "";
            }

            keyword = (String)var1.next();
        } while(!Utils.checkWordExist(sentence, keyword));

        return keyword;
    }
}
