package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class PropertiesManager {
    public static Properties getProperties(String name) {
        //String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String rootPath = new File("").getAbsolutePath() + "/src/main/resources/";
        String appConfigPath = rootPath + "properties/" + name + ".properties";
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}
