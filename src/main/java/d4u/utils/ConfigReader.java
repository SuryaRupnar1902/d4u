package d4u.utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class ConfigReader {
	
	    private static Properties prop = new Properties();

	    static {
	        try {
	            FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
	            prop.load(fis);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public static String getProperty(String key) {
	        return prop.getProperty(key);
	    }
	}
