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
            case "abstract-factory":
                return new AbstractFactoryPatternGenerator(config);
            case "builder":
                return new BuilderPatternGenerator(config);
            case "chain":
                return new ChainPatternGenerator(config);
            case "facade":
                return new FacadePatternGenerator(config);
            case "factory-method":
                return new FactoryMethodGenerator(config);
            case "mediator":
                return new MediatorPatternGenerator(config);
            case "template-method":
                return new TemplatePatternGenerator(config);
            case "visitor":
                return new VisitorPatternGenerator(config);
        }
        return null;
    }
}