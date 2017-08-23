import com.google.gson.Gson;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@WebServlet(
        name = "RequestHome",
        urlPatterns = {"/home"}
)
public class RequestHandler extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(RequestHandler.class);

    public RequestHandler() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("In the get request");
        String sentence = Utils.getSanitizedParam(request, "question");
        String mode = Utils.getSanitizedParam(request, "mode");
        LOGGER.info("Sentence: " + sentence);
        String period = DateExtractor.isPeriod(sentence);
        List<DateTime> dateInQuery = new ArrayList();
        if (period.equals("day")) {
            dateInQuery = DateExtractor.getDates(sentence, DateExtractor.latestDate);
            LOGGER.info("DATE-DAY: " + (dateInQuery).get(0).toString() + "  , " + ((dateInQuery).get(1)).toString());
        } else if (period.equals("month")) {
            dateInQuery = DateExtractor.getDates(sentence, DateExtractor.latestDate.minusDays(DateExtractor.latestDate.getDayOfMonth()));
            LOGGER.info("DATE-MONTH: " + (dateInQuery).get(0).toString() + "  , " + ((dateInQuery).get(1)).toString());
        } else if (period.equals("year")) {
            dateInQuery = DateExtractor.getDates(sentence, DateExtractor.latestDate.minusDays(DateExtractor.latestDate.getDayOfMonth()));
            LOGGER.info("DATE-YEAR: " + (dateInQuery).get(0).toString() + "  , " + (dateInQuery).get(1).toString());
        }

        if (!((DateTime) ((List) dateInQuery).get(1)).toDate().equals(DateExtractor.latestDate.toDate())) {
            dateInQuery.add(DateExtractor.latestDate);
        }

        LOGGER.info("startDateTimeAfter " + ((List) dateInQuery).get(0).toString());
        LOGGER.info("endDateTimeAfter " + ((List) dateInQuery).get(1).toString());
        Product product = ProductList.extractProduct(sentence);
        SubProduct subProduct = SubProductList.extractSubProduct(sentence, product);
        Query query = new Query(sentence, product, subProduct, new AggregatePicker(), DateExtractor.latestDate, DateExtractor.isPeriod(sentence));
        HashMap<String, HashMap<DateTime, String>> resultMap = new HashMap();

        for(DateTime dateTime: dateInQuery){
            query.setDateTime(dateTime);
            query.generateAndExecuteSql(resultMap);
            LOGGER.info(resultMap.toString());
        }


        String table = " <tr> <th> </th> ";

        Format formatter = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);

        ArrayList<DateTime> arrayList = new ArrayList<>(resultMap.entrySet().iterator().next().getValue().keySet());
        Collections.sort(arrayList);

        for(DateTime dateTime: arrayList){
            table = table + "<th> " + formatter.format(dateTime.toDate()) + " </th> ";
        }


        LOGGER.info("Added dates to table: " + table);
        table = table + "</tr> ";


        for(String attribute: resultMap.keySet()){
            table = table + " <tr> <th> " + product.getAttribute().get(attribute.toLowerCase() + "__$time$)") + " </th> ";
            LOGGER.info("actual Attributes: " + product.getAttribute());
            LOGGER.info("found attributes: " + attribute);
            for(DateTime dateTime1 : arrayList ){
                LOGGER.info(" S: for date " + dateTime1);
                String s = (resultMap.get(attribute)).get(dateTime1);
                if(s == "" || s == null){
                    s = "0";
                }
                LOGGER.info("S: " + s);
                table = table + " <td> " + Utils.convertToLakhs(Double.parseDouble(s)) + " </td> ";
            }

            table = table + " </tr> ";
        }


        LOGGER.info("Added dates to table: " + table);
        LOGGER.info("Answer: " + table);
        Response myResponse = new Response(table);
        response.setContentType("application/json");
        String attribute = (new Gson()).toJson(myResponse);
        LOGGER.info("doPost Response: " + attribute);
        response.setCharacterEncoding("UTF8");
        response.getWriter().println(attribute);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("In the post request");
    }
}
