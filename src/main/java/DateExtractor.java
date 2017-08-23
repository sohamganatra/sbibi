import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

@WebListener
public class DateExtractor implements ServletContextListener {
    private static Logger LOGGER = Logger.getLogger(DateExtractor.class);
    protected static DateTime latestDate;
    private static DateTime latestDateTime = DateTime.now();
    private static Parser parser = new Parser();

    public DateExtractor() {
    }

    private static void init() {
        LOGGER.info("Initializing the latest date");
        String latestDateFromSql = Database.runSQLWithoutTable("select max(repdate) from sunrise_dates");
        LOGGER.info("latestdateFromSql: " + latestDateFromSql);
        Date latestDateParsed = DateTime.now().toDate();
        if (!getDateHelper(latestDateFromSql).isEmpty()) {
            latestDateParsed = (Date)getDateHelper(latestDateFromSql).get(0);
            LOGGER.debug("Parsing the database for latest datetime successful: " + latestDateParsed.toString());
        } else {
            LOGGER.error("Error: Not able to fetch the latestdate. ");
        }

        latestDate = LocalDate.fromDateFields(latestDateParsed).toDateTimeAtStartOfDay();
    }

    public static List<DateTime> getDates(String sentence, DateTime latestDateTime) {
        Date startDate = latestDateTime.toDate();
        Date endDate = latestDateTime.toDate();
        List<Date> dates = getDateHelper(sentence);
        if (dates.size() > 0) {
            LOGGER.info("setting start date");
            startDate = (Date)dates.get(0);
        }

        if (dates.size() > 1) {
            endDate = (Date)dates.get(1);
        }

        DateTime startDateTime = LocalDate.fromDateFields(startDate).toDateTimeAtStartOfDay();
        LOGGER.info("startdatetime " + startDateTime.toString());
        DateTime endDateTime = LocalDate.fromDateFields(endDate).toDateTimeAtStartOfDay();
        DateTime temp;
        if (endDateTime.isBefore(startDateTime)) {
            temp = endDateTime;
            endDateTime = startDateTime;
            startDateTime = temp;
        }

        if (endDateTime.isAfter(latestDateTime)) {
            if (Days.daysBetween(endDateTime, latestDateTime).getDays() > 30) {
                endDateTime = endDateTime.minusYears(1);
            } else {
                startDateTime = startDateTime.minus(endDateTime.getMillis() - latestDateTime.getMillis());
                endDateTime = latestDateTime;
            }
        }

        if (endDateTime.isBefore(startDateTime)) {
            temp = endDateTime;
            endDateTime = startDateTime;
            startDateTime = temp;
        }

        ArrayList<DateTime> result = new ArrayList();
        result.add(startDateTime);
        result.add(endDateTime);
        return result;
    }

    public static List<Date> getDateHelper(String string) {
        LOGGER.info("Date parser : " + string);
        List<Date> dummy = new ArrayList();
        List<DateGroup> dateGroups = parser.parse(string);
        if (!dateGroups.isEmpty()) {
            LOGGER.info("dategroup size not 0");
            LOGGER.info(((Date)((DateGroup)dateGroups.get(0)).getDates().get(0)).toString());
            return ((DateGroup)dateGroups.get(0)).getDates();
        } else {
            return dummy;
        }
    }

    static int getNumberDate(String string) {
        LOGGER.info("getNumberDate is : " + getDateHelper(string).size());
        return getDateHelper(string).size();
    }

    static int getDifference(DateTime dateTime, DateTime dateTime1) {
        return Math.abs(Days.daysBetween(dateTime, dateTime1).getDays());
    }

    static int getDifference(DateTime dateTime) {
        return getDifference(dateTime, latestDateTime);
    }

    static int getDifference(String string) {
        List<Date> dates = getDateHelper(string);
        LocalDate date1;
        LocalDate date2;
        if (dates.size() >= 2) {
            date1 = LocalDate.fromDateFields(dates.get(0));
            date2 = LocalDate.fromDateFields(dates.get(1));
            return Math.abs(Days.daysBetween(date1, date2).getDays());
        } else if (dates.size() >= 1) {
            date1 = LocalDate.fromDateFields(dates.get(0));
            date2 = LocalDate.now();
            return Math.abs(Days.daysBetween(date1, date2).getDays());
        } else {
            return 0;
        }
    }

    public static Boolean isGrowth(String sentence) {
        if (sentence.toLowerCase().contains("growth")) {
            return true;
        } else {
            return getNumberDate(sentence) > 1;
        }
    }

    public static String isPeriod(String sentence) {
        if (sentence.toLowerCase().contains("day")) {
            return "day";
        } else if (sentence.toLowerCase().contains("month")) {
            return "month";
        } else if (sentence.toLowerCase().contains("year")) {
            return "year";
        } else if (getDifference(sentence) < 20) {
            return "day";
        } else {
            return getDifference(sentence) < 300 ? "month" : "year";
        }
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        init();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
