/**
 * @author Carlos Antonio McNulty
 */


import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class DesignPatternGenFactory {


    private static Logger logger = LoggerFactory.getLogger(DesignPatternGenFactory.class);


    public static DesignPatternGenerator create(Config config){
        String type = config.getString("design-pattern");

        logger.info("CREATING DESIGN PATTERN: " + type);

        switch (type){
            case "Abstract Factory":
                return new AbstractFactoryPatternGenerator(config);
            case "Builder":
                return new BuilderPatternGenerator(config);
            case "Chain":
                return new ChainPatternGenerator(config);
            case "Facade":
                return new FacadePatternGenerator(config);
            case "Factory Method":
                return new FactoryMethodGenerator(config);
            case "Mediator":
                return new MediatorPatternGenerator(config);
            case "Template Method":
                return new TemplatePatternGenerator(config);
            case "Visitor":
                return new VisitorPatternGenerator(config);
        }
        return null;
    }
}