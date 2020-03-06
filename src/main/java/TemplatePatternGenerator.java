/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import com.typesafe.config.Config;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class TemplatePatternGenerator extends DesignPatternGenerator {


    private String      template;
    private String      concreteTemplate;
    private String      templateMethod;
    private String[]    primitives;

    private Logger logger = LoggerFactory.getLogger(TemplatePatternGenerator.class);


    /**
     *
     * @param config
     */
    public TemplatePatternGenerator(Config config){
        super();

        this.template           = config.getString("template");
        this.concreteTemplate   = config.getString("concrete-template");
        this.templateMethod     = config.getString("template-method");
        this.primitives         =
                config.getStringList("primitives").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param template
     * @param concreteTemplate
     * @param templateMethod
     * @param primitives
     */
    public TemplatePatternGenerator(String template,
                                    String concreteTemplate,
                                    String templateMethod,
                                    String[] primitives){
        super();

        this.template = template;
        this.concreteTemplate = concreteTemplate;
        this.templateMethod = templateMethod;
        this.primitives = primitives;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR TEMPLATE PATTERN...");

        // Create compilation unit for template class
        CompilationUnit templateUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT TEMPLATE CLASS: " + template);

        // Create class for template
        ClassOrInterfaceDeclaration templateClass =
                addClass(templateUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                        template);
        // Add constructor to template
        addConstructor(templateClass, new Param[]{}, new BlockStmt());

        logger.info("ADDING ABSTRACT PRIMITIVES TO ABSTRACT TEMPLATE CLASS...");

        // Add abstract method declarations for primitives
        for(String primitive: primitives){
            addMethod(templateClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                    primitive);
        }

        // Create method body and add comment
        BlockStmt templateMethodBody = new BlockStmt();
        templateMethodBody.addOrphanComment(new LineComment("TODO"));
        // Add primitive method calls in template method
        for(String primitive: primitives){
            templateMethodBody.addStatement(new MethodCallExpr(primitive));
        }

        logger.info("ADDING TEMPLATE METHOD: " + templateMethod);

        // Add template method to template class
        addMethod(templateClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                templateMethod, templateMethodBody);

        // Create compilation unit for concrete subclass of template
        CompilationUnit concreteTemplateUnit = addCompilationUnit();

        logger.info("ADDING CONCRETE TEMPLATE CLASS: " + concreteTemplate);

        // Add concrete template class to compilation unit
        ClassOrInterfaceDeclaration concreteTemplateClass =
                addClass(concreteTemplateUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteTemplate);

        logger.info("ADDING CONCRETE PRIMITIVES TO TEMPLATE CLASS...");

        // Add primitive methods with bodies to concrete subclass
        for(String primitive: primitives){
            BlockStmt body = new BlockStmt();
            body.addOrphanComment(new LineComment("TODO"));
            addMethod(concreteTemplateClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    primitive, body);
        }

    }
}
