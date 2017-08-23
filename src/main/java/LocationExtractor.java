import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

@WebListener
public class LocationExtractor implements ServletContextListener {
    private static String CIRCLE = "circle";
    private static String MODULE = "module";
    private static String REGION = "region";
    private static String BRANCH = "branch";
    private static Logger LOGGER = Logger.getLogger(LocationExtractor.class);
    private static HashMap<String, LocationExtractor.Circle> getCircleFromName = new HashMap();
    private static HashMap<String, LocationExtractor.Module> getModuleFromName = new HashMap();
    private static HashMap<String, LocationExtractor.Branch> getBranchFromName = new HashMap();

    public LocationExtractor() {
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        init();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    private static LocationExtractor.QUERY_TYPE getQueryType(String query) {
        if (query.contains(MODULE)) {
            return fetchModuleInfoFromQuery(query).getModuleName().length() > 0 ? LocationExtractor.QUERY_TYPE.QUERY_TYPE_MODULE : LocationExtractor.QUERY_TYPE.QUERY_TYPE_BANK;
        } else if (query.contains(CIRCLE)) {
            return fetchCircleInfoFromQuery(query).getCircleName().length() > 0 ? LocationExtractor.QUERY_TYPE.QUERY_TYPE_CIRCLE : LocationExtractor.QUERY_TYPE.QUERY_TYPE_BANK;
        } else if (query.contains(BRANCH)) {
            return fetchBranchInfoFromQuery(query).getBranchName().length() > 0 ? LocationExtractor.QUERY_TYPE.QUERY_TYPE_BRANCH : LocationExtractor.QUERY_TYPE.QUERY_TYPE_BANK;
        } else if (query.contains(REGION)) {
            return fetchRegionInfoFromQuery(query).getRegionName().length() > 0 ? LocationExtractor.QUERY_TYPE.QUERY_TYPE_REGION : LocationExtractor.QUERY_TYPE.QUERY_TYPE_BANK;
        } else {
            return LocationExtractor.QUERY_TYPE.QUERY_TYPE_BANK;
        }
    }

    static LocationExtractor.Circle fetchCircleInfoFromQuery(String query) {
        System.out.println("Query is: " + query);
        String[] words = query.split(" ");

        for(int i = 0; i < words.length - 1; ++i) {
            String circleName = words[i] + " " + words[i + 1];
            if (getCircleFromName.containsKey(circleName)) {
                System.out.println("Matched Circle pair: " + circleName);
                return (LocationExtractor.Circle)getCircleFromName.get(circleName);
            }
        }

        String[] var6 = words;
        int var7 = words.length;

        for(int var4 = 0; var4 < var7; ++var4) {
            String circle = var6[var4];
            if (getCircleFromName.containsKey(circle)) {
                System.out.println("Matched Circle: " + circle);
                return (LocationExtractor.Circle)getCircleFromName.get(circle);
            }
        }

        return new LocationExtractor.Circle("", "");
    }

    static LocationExtractor.Module fetchModuleInfoFromQuery(String query) {
        String[] words = query.split(" ");

        for(int i = 0; i < words.length - 1; ++i) {
            String moduleName = words[i] + " " + words[i + 1];
            if (getModuleFromName.containsKey(moduleName)) {
                System.out.println("Matched Module: " + moduleName);
                return (LocationExtractor.Module)getModuleFromName.get(moduleName);
            }
        }

        String[] var6 = words;
        int var7 = words.length;

        for(int var4 = 0; var4 < var7; ++var4) {
            String module = var6[var4];
            if (getModuleFromName.containsKey(module)) {
                System.out.println("Matched Module: " + module);
                return (LocationExtractor.Module)getModuleFromName.get(module);
            }
        }

        return new LocationExtractor.Module("", "", "");
    }

    static LocationExtractor.Region fetchRegionInfoFromQuery(String query) {
        LocationExtractor.Module module = fetchModuleInfoFromQuery(query);
        query = query + " ";
        int regionCodeStart = query.indexOf("region") + 7;
        String regionCode = query.substring(regionCodeStart, query.indexOf(" ", regionCodeStart));
        return new LocationExtractor.Region(module.getModuleName(), regionCode, module.getModuleCode(), module.getCircleCode());
    }

    static LocationExtractor.Branch fetchBranchInfoFromQuery(String query) {
        String[] words = query.split(" ");

        for(int i = 0; i < words.length - 1; ++i) {
            String branchName = words[i] + " " + words[i + 1];
            if (getBranchFromName.containsKey(branchName)) {
                System.out.println("Matched Branch: " + branchName);
                return (LocationExtractor.Branch)getBranchFromName.get(branchName);
            }
        }

        String[] var6 = words;
        int var7 = words.length;

        for(int var4 = 0; var4 < var7; ++var4) {
            String branch = var6[var4];
            if (getBranchFromName.containsKey(branch)) {
                System.out.println("Matched Branch: " + branch);
                return (LocationExtractor.Branch)getBranchFromName.get(branch);
            }
        }

        return new LocationExtractor.Branch("", "");
    }

    private static String getCircleCode(String[] data) {
        return data[2].toLowerCase();
    }

    private static String getCircleName(String[] data) {
        return Utils.replaceRedundantCharacters(data[3]).toLowerCase();
    }

    private static String getModuleCode(String[] data) {
        return data[5].toLowerCase();
    }

    private static String getModuleName(String[] data) {
        return Utils.replaceRedundantCharacters(data[6]).toLowerCase();
    }

    private static String getBranchCode(String[] data) {
        return data[0].toLowerCase();
    }

    private static String getBranchName(String[] data) {
        return Utils.replaceRedundantCharacters(data[1]).toLowerCase();
    }

    static String getWhereClauseLocation(String sentence) {
        LocationExtractor.QUERY_TYPE query_type = getQueryType(sentence);
        String sql = "";
        switch(query_type) {
            case QUERY_TYPE_BRANCH:
                LocationExtractor.Branch branch = fetchBranchInfoFromQuery(sentence);
                LOGGER.debug("Identified Branch Name: " + branch.getBranchName());
                LOGGER.debug("Identified Branch Code: " + branch.getBranchCode());
                sql = "and brcd_code = '" + branch.getBranchCode() + "' ";
                return sql;
            case QUERY_TYPE_REGION:
                LocationExtractor.Region region = fetchRegionInfoFromQuery(sentence);
                LOGGER.debug("Identified Region Name: " + region.getRegionName());
                LOGGER.debug("Identified Region Code: " + region.getRegionCode());
                sql = sql + "and crmd_sdf_circlecode = '" + region.getCircleCode() + "' and ";
                sql = sql + " crmd_sdf_modulecode = '" + region.getModuleCode() + "' and ";
                sql = sql + " sdf_regioncode = '" + region.getRegionCode() + "' ";
                return sql;
            case QUERY_TYPE_MODULE:
                LocationExtractor.Module module = fetchModuleInfoFromQuery(sentence);
                LOGGER.debug("Identified Module Name: " + module.getModuleName());
                LOGGER.debug("Identified Module Code: " + module.getModuleCode());
                sql = sql + "and crmd_sdf_circlecode = '" + module.getCircleCode() + "' and ";
                sql = sql + " crmd_sdf_modulecode = '" + module.getModuleCode() + "' ";
                return sql;
            case QUERY_TYPE_CIRCLE:
                LocationExtractor.Circle circle = fetchCircleInfoFromQuery(sentence);
                LOGGER.debug("Identified Circle Name: " + circle.getCircleName());
                LOGGER.debug("Identified Circle Code: " + circle.getCircleCode());
                sql = sql + "and crmd_sdf_circlecode = '" + circle.getCircleCode() + "' ";
                return sql;
            case QUERY_TYPE_BANK:
                LOGGER.debug("query type bank");
                return sql;
            default:
                return sql;
        }
    }

    protected static void init() {
        File bankDataFile = new File(LocationExtractor.class.getResource(Config.BankDataFile).getFile());

        try {
            List<String> dataPoints = FileUtils.readLines(bankDataFile, "UTF-8");
            Iterator var2 = dataPoints.iterator();

            while(var2.hasNext()) {
                String dataPoint = (String)var2.next();
                String[] data = dataPoint.split(",");
                getCircleFromName.put(getCircleName(data), new LocationExtractor.Circle(getCircleName(data), getCircleCode(data)));
                getModuleFromName.put(getModuleName(data), new LocationExtractor.Module(getModuleName(data), getModuleCode(data), getCircleCode(data)));
                getBranchFromName.put(getBranchName(data), new LocationExtractor.Branch(getBranchName(data), getBranchCode(data)));
            }
        } catch (IOException var8) {
            LOGGER.info("Error in reading BankData.txt" + var8.toString());
        } finally {
            LOGGER.info("BankData.txt reading complete");
        }

    }

    static enum QUERY_TYPE {
        QUERY_TYPE_BANK,
        QUERY_TYPE_CIRCLE,
        QUERY_TYPE_MODULE,
        QUERY_TYPE_REGION,
        QUERY_TYPE_BRANCH;

        private QUERY_TYPE() {
        }
    }

    public static class Region {
        private String regionName;
        private String regionCode;
        private String moduleCode;
        private String circleCode;

        Region(String regionName_, String regionCode_, String moduleCode_, String circleCode_) {
            this.regionName = regionName_;
            this.regionCode = regionCode_;
            this.moduleCode = moduleCode_;
            this.circleCode = circleCode_;
        }

        String getRegionName() {
            return this.regionName;
        }

        String getRegionCode() {
            return this.regionCode;
        }

        String getModuleCode() {
            return this.moduleCode;
        }

        String getCircleCode() {
            return this.circleCode;
        }
    }

    static class Module {
        private String moduleName;
        private String moduleCode;
        private String circleCode;

        Module(String moduleName_, String moduleCode_, String circleCode_) {
            this.moduleName = moduleName_;
            this.moduleCode = moduleCode_;
            this.circleCode = circleCode_;
        }

        String getModuleName() {
            return this.moduleName;
        }

        String getModuleCode() {
            return this.moduleCode;
        }

        String getCircleCode() {
            return this.circleCode;
        }
    }

    static class Circle {
        private String circleName;
        private String circleCode;

        Circle(String circleName_, String circleCode_) {
            this.circleName = circleName_;
            this.circleCode = circleCode_;
        }

        String getCircleName() {
            return this.circleName;
        }

        String getCircleCode() {
            return this.circleCode;
        }
    }

    static class Branch {
        private String branchName;
        private String branchCode;

        Branch(String branchName_, String branchCode_) {
            this.branchName = branchName_;
            this.branchCode = branchCode_;
        }

        String getBranchName() {
            return this.branchName;
        }

        String getBranchCode() {
            return this.branchCode;
        }
    }
}
