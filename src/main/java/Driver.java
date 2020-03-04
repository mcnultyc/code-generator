/**
 * @author Carlos Antonio McNulty
 */


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;


public class Driver {


    private static Logger logger = LoggerFactory.getLogger(Driver.class);


    public static void main(String[] args) {

        // Load the configuration file
        Config config = ConfigFactory.parseResources("design-patterns.conf");

        // Load the destination directory for designs being generated
        String directory = config.getString("conf.path");

        logger.info("DESTINATION DIRECTORY: " + directory);

        // Get the absolute path from the file system
        Path path = FileSystems.getDefault().getPath(directory).toAbsolutePath();

        // Get the list of configs for each design pattern
        List<? extends Config> configs = config.getConfigList("design-patterns");

        logger.info("CREATING " + configs.size() + " DESIGN PATTERNS...");

        for(Config designConfig: configs){
            // Create the design pattern generator
            DesignPatternGenerator generator
                    = DesignPatternGenFactory.create(designConfig);
            // Store the created compilation units in the destination directory
            generator.generate(path);
        }
    }
}
