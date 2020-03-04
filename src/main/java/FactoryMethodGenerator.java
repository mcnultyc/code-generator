/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class generates the compilation units used for the factory
 * method design pattern based on the class names provided by the user.
 * These can be provided directly or through a json file.
 */
public class FactoryMethodGenerator extends DesignPatternGenerator {


    private String      creator;
    private String      concreteCreator;
    private String      product;
    private String[]    concreteProducts;

    private Logger logger = LoggerFactory.getLogger(FactoryMethodGenerator.class);


    /**
     *
     * @param config
     */
    public FactoryMethodGenerator(Config config){
        super();

        this.creator            = config.getString("creator");
        this.concreteCreator    = config.getString("concrete-creator");
        this.product            = config.getString("product");
        this.concreteProducts   =
                config.getStringList("concrete-products").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param creator  declares the factory method, which returns an object of type product
     * @param concreteCreator  overrides the factory method to return an instance of a concrete product
     * @param product defines the interface of objects the factory method creates
     * @param concreteProducts classes that implement the product interface
     */
    public FactoryMethodGenerator(String creator,
                                  String concreteCreator,
                                  String product,
                                  String[] concreteProducts){

        super();

        this.creator = creator;
        this.concreteCreator = concreteCreator;
        this.product = product;
        this.concreteProducts = concreteProducts;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR FACTORY METHOD PATTERN...");

        // Create compilation unit for interface
        CompilationUnit creatorUnit = addCompilationUnit();

        logger.info("ADDING CREATOR INTERFACE: " + creator);

        // Create interface that declares factory method
        ClassOrInterfaceDeclaration creatorInterface = creatorUnit.addInterface(creator);

        // Create enum declaration used by factory method
        EnumDeclaration productsEnum = new EnumDeclaration();
        productsEnum.setName("Type");
        for (String concreteProduct: concreteProducts){
            productsEnum.addEnumConstant(concreteProduct.toUpperCase());
        }
        // Add enum to concrete class
        creatorInterface.addMember(productsEnum);

        // Add declaration for factory method
        addMethod(creatorInterface, new Modifier.Keyword[]{}, product, "create",
                new Param[]{new Param("Type", "type")});

        // Create compilation unit for concrete creator
        CompilationUnit concreteCreatorUnit = addCompilationUnit();

        logger.info("ADDING IMPLEMENTING CREATOR CLASS: " + concreteCreator);

        // Create concrete class that defines the factory method
        ClassOrInterfaceDeclaration concreteCreatorClass =
                addClass(concreteCreatorUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteCreator);
        concreteCreatorClass.addImplementedType(creator);

        // Add factory method and body to concrete class
        MethodDeclaration concreteCreatorMethod =
                addMethod(concreteCreatorClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        product, "create");

        SwitchStmt switchStmt = new SwitchStmt();
        NodeList<SwitchEntry> entries = new NodeList<>();
        // Create one entry in factory method switch expression per concrete product
        for(String concreteProduct: concreteProducts){
            SwitchEntry entry = new SwitchEntry();
            entry.setLabels(new NodeList<>(
                    new NameExpr(concreteProduct.toUpperCase())));
            entry.addStatement("return new " + concreteProduct + "();");
            entries.add(entry);
        }

        // Add switch expression default
        entries.add(new SwitchEntry().addStatement("return null;"));
        // Set the switch expression selector as type of product being created
        switchStmt.setSelector(new NameExpr("type"));
        switchStmt.setEntries(new NodeList<>(entries));

        // Create body of concrete creator method
        BlockStmt creatorBody = new BlockStmt();
        creatorBody.addOrphanComment(new LineComment("TODO"));
        creatorBody.addStatement(switchStmt);

        // Add switch expression to body of factory method and set parameters and annotation
        concreteCreatorMethod.setBody(creatorBody);
        concreteCreatorMethod.addParameter("Type", "type");
        concreteCreatorMethod.addMarkerAnnotation("Override");

        // Create compilation unit for product interface
        CompilationUnit productUnit = addCompilationUnit();

        logger.info("ADDING PRODUCT INTERFACE: " + product);

        // Create product interface
        ClassOrInterfaceDeclaration productInterface = productUnit.addInterface(product);

        logger.info("CREATING IMPLEMENTING PRODUCT CLASSES...");

        // Create compilation units for each concrete product
        for(String concreteProduct: concreteProducts){
            CompilationUnit concreteProductUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + concreteProduct);

            ClassOrInterfaceDeclaration concreteProductClass =
                    concreteProductUnit.addClass(concreteProduct).addImplementedType(product);
        }
    }
}
