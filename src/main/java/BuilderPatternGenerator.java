/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import com.typesafe.config.Config;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class BuilderPatternGenerator extends DesignPatternGenerator {


    private String      builder;
    private String      concreteBuilder;
    private String      complexObject;
    private String      product;
    private String[]    concreteProducts;

    private static Logger logger =
            LoggerFactory.getLogger(BuilderPatternGenerator.class);


    public BuilderPatternGenerator(Config config){
        super();

        this.builder            = config.getString("builder");
        this.concreteBuilder    = config.getString("concrete-builder");
        this.complexObject      = config.getString("complex-object");
        this.product            = config.getString("product");
        this.concreteProducts   =
                config.getStringList("concrete-products").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param builder
     * @param concreteBuilder
     * @param complexObject
     * @param product
     * @param concreteProducts
     */
    public BuilderPatternGenerator(String builder,
                                   String concreteBuilder,
                                   String complexObject,
                                   String product,
                                   String[] concreteProducts){
        super();

        this.builder = builder;
        this.concreteBuilder = concreteBuilder;
        this.complexObject = complexObject;
        this.product = product;
        this.concreteProducts = concreteProducts;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR BUILDER PATTERN...");

        // Create compilation unit for complex object
        CompilationUnit complexObjectUnit = addCompilationUnit();

        logger.info("ADDING COMPLEX OBJECT CLASS: " + complexObject);

        // Add class for complex object to compilation unit
        ClassOrInterfaceDeclaration complexObjectClass =
                addClass(complexObjectUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        complexObject);

        // Create body of add method for complex object
        BlockStmt addBody = new BlockStmt();
        addBody.addOrphanComment(new LineComment("TODO"));

        // Add 'add' method to complex object
        addMethod(complexObjectClass,
                new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                "add", new Param[]{new Param(product,"child")}, addBody);

        // Create compilation unit for builder
        CompilationUnit builderUnit =  addCompilationUnit();

        logger.info("ADDING BUILDER INTERFACE CLASS: " + builder);

        // Add class for builder to compilation unit
        ClassOrInterfaceDeclaration builderClass =
                addClass(builderUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        builder);
        // Set class as interface
        builderClass.setInterface(true);

        // Add abstract build part methods
        for(String product: concreteProducts){
            addMethod(builderClass,
                    new Modifier.Keyword[]{},
                    "build"+product);
        }

        // Add method used to get result of build process
        addMethod(builderClass, new Modifier.Keyword[]{},
                complexObject, "getResult");

        // Create compilation unit for concrete builder
        CompilationUnit concreteBuilderUnit = addCompilationUnit();

        logger.info("ADDING IMPLEMENTING BUILDER CLASS: " + concreteBuilder);

        // Add concrete builder class to compilation unit
        ClassOrInterfaceDeclaration concreteBuilderClass =
                addClass(concreteBuilderUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteBuilder);
        // Add implements builder
        concreteBuilderClass.addImplementedType(builder);

        // Create name of complex object field
        String coFieldName = Character.toLowerCase(complexObject.charAt(0))+complexObject.substring(1);

        // Add complex object field to concrete builder class
        FieldDeclaration field =
                addField(concreteBuilderClass,
                        new Field(complexObject, coFieldName, Modifier.Keyword.PRIVATE),
                        "new "+complexObject+"()");

        // Create compilation unit for product
        CompilationUnit productUnit = addCompilationUnit();

        logger.info("ADDING PRODUCT INTERFACE CLASS: " + product);

        // Add product class to product compilation unit
        ClassOrInterfaceDeclaration productClass =
                addClass(productUnit, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        product);
        productClass.setInterface(true);

        // Add overridden build part methods to concrete builder
        for(String product: concreteProducts){
            // Create body for build part method
            BlockStmt buildBody = new BlockStmt();
            // Create call to add part to complex object
            MethodCallExpr addCall =
                    new MethodCallExpr(coFieldName+".add", new NameExpr("new "+product+"()"));
            buildBody.addOrphanComment(new LineComment("TODO"));
            // Add call to body of method
            buildBody.addStatement(addCall);
            addOverridenMethod(concreteBuilderClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "build"+product, buildBody);
        }

        // Create body for get result method
        BlockStmt resultBody = new BlockStmt();
        resultBody.addOrphanComment(new LineComment("TODO"));
        // Returns complex object field
        resultBody.addStatement(new ReturnStmt(coFieldName));

        // Add get result method to complex object
        addOverridenMethod(concreteBuilderClass,
                new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                complexObject, "getResult", resultBody);

        logger.info("CREATING IMPLEMENTING CLASSES OF " + product + "...");

        // Create classes for concrete products
        for(String concreteProduct: concreteProducts){
            // Create compilation unit
            CompilationUnit concreteProductUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + concreteProduct);

            // Add class to compilation unit
            ClassOrInterfaceDeclaration concreteProductClass =
                    addClass(concreteProductUnit,
                            new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                            concreteProduct);
            concreteProductClass.addImplementedType(product);
        }
    }
}
