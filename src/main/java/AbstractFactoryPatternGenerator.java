/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.*;


/**
 *
 */
public class AbstractFactoryPatternGenerator extends DesignPatternGenerator {


    private String      factory;
    private String      concreteFactory;
    private String      productA;
    private String[]    concreteProductsA;
    private String      productB;
    private String[]    concreteProductsB;

    private Logger logger = LoggerFactory.getLogger(AbstractFactoryPatternGenerator.class);


    /**
     *
     * @param config
     */
    public AbstractFactoryPatternGenerator(Config config){

        super();

        this.factory            = config.getString("factory");
        this.concreteFactory    = config.getString("concrete-factory");
        this.productA           = config.getString("product-a");
        this.concreteProductsA  =
                config.getStringList("concrete-products-a").toArray(new String[0]);
        this.productB           = config.getString("product-b");
        this.concreteProductsB  =
                config.getStringList("concrete-products-b").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param factory
     * @param concreteFactory
     * @param productA
     * @param concreteProductsA
     * @param productB
     * @param concreteProductsB
     */
    public AbstractFactoryPatternGenerator(String factory,
                                           String concreteFactory,
                                           String productA,
                                           String[] concreteProductsA,
                                           String productB,
                                           String[] concreteProductsB){
        super();

        this.factory = factory;
        this.concreteFactory = concreteFactory;
        this.productA = productA;
        this.concreteProductsA = concreteProductsA;
        this.productB = productB;
        this.concreteProductsB = concreteProductsB;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR ABSTRACT FACTORY PATTERN...");

        // Create compilation unit for factory
        CompilationUnit factoryUnit = addCompilationUnit();

        logger.info("ADDING FACTORY INTERFACE: " + factory);

        // Create class for factory
        ClassOrInterfaceDeclaration factoryClass =
                addClass(factoryUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        factory);
        factoryClass.setInterface(true);

        // Add abstract creation methods to factory
        addMethod(factoryClass,
                new Modifier.Keyword[]{},
                productA,"create"+productA);

        addMethod(factoryClass,
                new Modifier.Keyword[]{},
                productB,"create"+productB);

        // Create compilation unit for concrete factory
        CompilationUnit concreteFactoryUnit = addCompilationUnit();

        logger.info("ADDING IMPLEMENTING FACTORY CLASS: " + concreteFactory);

        // Create class for concrete factory
        ClassOrInterfaceDeclaration concreteFactoryClass =
                addClass(concreteFactoryUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteFactory);
        concreteFactoryClass.addImplementedType(factory);

        // Create body of create product A method
        BlockStmt body = new BlockStmt();
        body.addOrphanComment(new LineComment("TODO"));
        body.addStatement(new ReturnStmt("new "+concreteProductsA[0]+"()"));
        // Add body of overridden method to concrete factory
        addOverridenMethod(concreteFactoryClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                productA,"create"+productA, body);

        // Create body of create product B method
        body = new BlockStmt();
        body.addOrphanComment(new LineComment("TODO"));
        body.addStatement(new ReturnStmt("new "+concreteProductsB[0]+"()"));
        // Add body of overridden method to concrete factory
        addOverridenMethod(concreteFactoryClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                productB,"create"+productB, body);

        // Create compilation unit for product A
        CompilationUnit productAUnit = addCompilationUnit();

        logger.info("ADDING PRODUCT A INTERFACE: " + productB);

        // Add product A class to compilation unit
        ClassOrInterfaceDeclaration productAClass =
                addClass(productAUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        productA);
        productAClass.setInterface(true);

        logger.info("CREATING IMPLEMENTING CLASSES OF " + productA + "...");

        // Create classes for concrete products A
        for(String concreteProductA: concreteProductsA){
            // Create compilation unit
            CompilationUnit concreteProductUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + concreteProductA);

            // Create class for concrete product A
            ClassOrInterfaceDeclaration concreteProductClass =
                    addClass(concreteProductUnit,
                            new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                            concreteProductA);
            concreteProductClass.addImplementedType(productA);
        }

        logger.info("ADDING PRODUCT B INTERFACE: " + productB);

        // Create compilation unit for product B
        CompilationUnit productBUnit = addCompilationUnit();
        // Add product B class to compilation unit
        ClassOrInterfaceDeclaration productBClass =
                addClass(productBUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        productB);
        productBClass.setInterface(true);

        logger.info("CREATING IMPLEMENTING CLASSES OF " + productB + "...");

        // Create classes for concrete products B
        for(String concreteProductB: concreteProductsB){
            // Create compilation unit
            CompilationUnit concreteProductUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + concreteProductB);

            // Create class for concrete product B
            ClassOrInterfaceDeclaration concreteProductClass =
                    addClass(concreteProductUnit,
                            new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                            concreteProductB);
            concreteProductClass.addImplementedType(productB);
        }
    }
}
