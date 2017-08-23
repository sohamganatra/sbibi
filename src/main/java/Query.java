import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import sun.rmi.runtime.Log;

public class Query {
    private String question;
    private Product product;
    private SubProduct subProduct;
    private AggregatePicker aggregatePicker;
    private DateTime dateTime;
    private String mode;
    private static Logger LOGGER = Logger.getLogger(Query.class);

    public Query(String question, Product product, SubProduct subProduct, AggregatePicker aggregatePicker, DateTime dateTime, String mode) {
        this.question = question;
        this.product = product;
        this.subProduct = subProduct;
        this.aggregatePicker = aggregatePicker;
        this.dateTime = dateTime;
        this.mode = mode;
        this.getPeriod();
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public AggregatePicker getAggregatePicker() {
        return this.aggregatePicker;
    }

    public void setAggregatePicker(AggregatePicker aggregatePicker) {
        this.aggregatePicker = aggregatePicker;
    }

    public DateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
        this.getPeriod();
    }

    public void getPeriod() {
        if (DateExtractor.getDifference(this.dateTime) < 20) {
            this.mode = "day";
        } else if (DateExtractor.getDifference(this.dateTime) < 300) {
            this.mode = "month";
        } else {
            this.mode = "year";
        }

    }

    private void copyMap(HashMap<String, HashMap<DateTime, String>> output,HashMap<String,String> attributeResultMap) {
        for (String attribute : attributeResultMap.keySet()) {
            if (output.containsKey(attribute)) {
                (output.get(attribute)).put(this.dateTime, attributeResultMap.get(attribute));
            } else {
                HashMap<DateTime, String> dateResult;
                dateResult = new HashMap<>();
                dateResult.put(this.dateTime, attributeResultMap.get(attribute));
                output.put(attribute, dateResult);
            }
        }
    }

    public void generateAndExecuteSql(HashMap<String, HashMap<DateTime, String>> output) {
        String statement;
        String productView;
        String selectClause;
        String whereClause;
        String fromClause;
        if (this.mode.equalsIgnoreCase("day")) {
            productView = this.product.getProductDayView();
            productView = productView.replace("$location$", LocationExtractor.getWhereClauseLocation(this.question));
            LOGGER.info(productView);

            selectClause = Utils.addAttributesWithSeparator(product.getAttribute().keySet(),",")
                    .replace("$time$",Integer.toString(this.dateTime.getDayOfMonth()));

            whereClause = " ";

            if (this.subProduct != null) {
                whereClause = " where " + Utils.addAttributes(this.subProduct.getSubProductCode(), this.subProduct.getSubProductField());
            }

            fromClause = this.product.getProductTable();
            statement = productView + " select " + selectClause + " from " + fromClause + whereClause;
            HashMap<String,String> attributeResultMap = Database.convertSQLToMAP(statement);

            assert attributeResultMap != null;
            copyMap(output,attributeResultMap);

        } else if (this.mode.equalsIgnoreCase("month")) {
            productView = this.product.getProductMonthView();
            productView = productView.replace("$location$", LocationExtractor.getWhereClauseLocation(this.question));
            LOGGER.info(productView);

            selectClause = Utils.addAttributesWithSeparator(product.getAttribute().keySet(),",")
                    .replace("$time$",Integer.toString(this.dateTime.getMonthOfYear()));

            whereClause = " ";
            if (this.subProduct != null) {
                whereClause = " where " + Utils.addAttributes(this.subProduct.getSubProductCode(), this.subProduct.getSubProductField());
            }
            fromClause = this.product.getProductTable();
            statement = productView + " select " + selectClause + " from " + fromClause + whereClause;
            HashMap<String,String> attributeResultMap = Database.convertSQLToMAP(statement);

            assert attributeResultMap != null;
            copyMap(output,attributeResultMap);

        } else {
            LOGGER.info("Entering Yearly SQL query");
            productView = this.product.getProductYearView();
            productView = productView.replace("$location$", LocationExtractor.getWhereClauseLocation(this.question));
            productView = productView.replace("$year$",Integer.toString(this.dateTime.getYearOfEra()));
            productView = productView.replace("$month$",Integer.toString(this.dateTime.getMonthOfYear()));
            selectClause = Utils.addAttributesWithSeparator(product.getAttribute().keySet(),",")
                    .replace("$time$","");

            whereClause = " ";
            if (this.subProduct != null) {
                whereClause = " where " + Utils.addAttributes(this.subProduct.getSubProductCode(), this.subProduct.getSubProductField());
            }

            fromClause = this.product.getProductTable();
            statement = productView + " select " + selectClause + " from " + fromClause + whereClause;
            HashMap<String,String> attributeResultMap = Database.convertSQLToMAP(statement);

            assert attributeResultMap != null;
            copyMap(output,attributeResultMap);

        }

    }
}
