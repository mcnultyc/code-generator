/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class VisitorPatternGenerator extends DesignPatternGenerator{


    private String      element;
    private String[]    concreteElements;
    private String      visitor;
    private String      concreteVisitor;

    private Logger logger = LoggerFactory.getLogger(VisitorPatternGenerator.class);


    /**
     *
     * @param config
     */
    public VisitorPatternGenerator(Config config){
        super();

        this.element            = config.getString("element");
        this.concreteElements   = config.getStringList("concrete-elements").toArray(new String[0]);
        this.visitor            = config.getString("visitor");
        this.concreteVisitor    = config.getString("concrete-visitor");

        build();
    }


    /**
     *
     * @param element
     * @param concreteElements
     * @param visitor
     * @param concreteVisitor
     */
    public VisitorPatternGenerator(String element,
                                   String[] concreteElements,
                                   String visitor,
                                   String concreteVisitor){
        super();

        this.element = element;
        this.concreteElements = concreteElements;
        this.visitor = visitor;
        this.concreteVisitor = concreteVisitor;

        build();
    }


    protected void build(){

        logger.info("ADDING COMPILATION UNITS TO VISITOR PATTERN...");

        // Create compilation unit for element class
        CompilationUnit elementUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT ELEMENT CLASS: " + element);

        // Add element class to compilation unit
        ClassOrInterfaceDeclaration elementClass = addClass(elementUnit,
                new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                element);

        // Add accept method to abstract element class
        addMethod(elementClass,
                new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                "accept",
                new Param[]{new Param(visitor,"visitor")});

        logger.info("CREATING CONCRETE ELEMENT CLASSES...");

        // Creating concrete element classes
        for(String concreteElement: concreteElements){
            // Creating compilation unit for concrete element
            CompilationUnit concreteElementUnit = addCompilationUnit();

            logger.info("ADDING: " + concreteElement);

            // Add concrete element class to compilation unit
            ClassOrInterfaceDeclaration concreteElementClass =
                    addClass(concreteElementUnit,
                            new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                            concreteElement, element);

            // Add body to visit call
            BlockStmt body = new BlockStmt();
            body.addOrphanComment(new LineComment("TODO"));
            MethodCallExpr visitCall = new MethodCallExpr("visitor.visit"+concreteElement,
                    new NameExpr("this"));
            body.addStatement(visitCall);
            // Add overridden visit method
            addOverridenMethod(concreteElementClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "accept",
                    new Param[]{new Param(visitor,"visitor")},
                    body);
        }

        // Create compilation unit for abstract visitor
        CompilationUnit visitorUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT VISITOR CLASS: " + visitor);

        // Add abstract visitor class to compilation unit
        ClassOrInterfaceDeclaration visitorClass =
                addClass(visitorUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                        visitor);

        // Create abstract visit element methods to abstract visitor
        for(String concreteElement: concreteElements){
            addMethod(visitorClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                    "visit"+concreteElement,
                    new Param[]{new Param(concreteElement,"element")});
        }

        // Create compilation unit for concrete visitor
        CompilationUnit concreteVisitorUnit = addCompilationUnit();

        logger.info("ADDING CONCRETE VISITOR CLASS: " + concreteVisitor);

        // Add concrete visitor class to compilation unit
        ClassOrInterfaceDeclaration concreteVisitorClass =
                addClass(concreteVisitorUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteVisitor,
                        visitor);

        // Add overridden visit methods to concrete visitor class
        for(String concreteElement: concreteElements){
            // Create body for method
            BlockStmt body = new BlockStmt();
            body.addOrphanComment(new LineComment("TODO"));
            // Add overridden method to concrete visitor
            addOverridenMethod(concreteVisitorClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "visit"+concreteElement,
                    new Param[]{new Param(concreteElement,"element")},
                    body);
        }

    }
}
