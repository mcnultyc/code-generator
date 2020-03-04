/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
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
public class FacadePatternGenerator extends DesignPatternGenerator {


    private String      facade;
    private String      concreteFacade;
    private String[]    subsystems;

    private Logger logger = LoggerFactory.getLogger(FacadePatternGenerator.class);


    /**
     *
     * @param config
     */
    public FacadePatternGenerator(Config config){
        super();

        this.facade         = config.getString("facade");
        this.concreteFacade = config.getString("concrete-facade");
        this.subsystems     =
                config.getStringList("subsystems").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param facade
     * @param concreteFacade
     * @param subsystems
     */
    public FacadePatternGenerator(String facade,
                                  String concreteFacade,
                                  String[] subsystems){
        super();

        this.facade = facade;
        this.concreteFacade = concreteFacade;
        this.subsystems = subsystems;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR FACADE PATTERN...");

        // Create compilation unit for facade
        CompilationUnit facadeUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT FACADE CLASS: " + facade);

        // Add facade class to compilation unit
        ClassOrInterfaceDeclaration facadeClass =
                addClass(facadeUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                        facade);

        // Add abstract operations to facade class
        for(String subsystem: subsystems){
            // Create name for abstract method
            String name =
                    Character.toLowerCase(subsystem.charAt(0))+subsystem.substring(1);
            // Add abstract method to facade class
            addMethod(facadeClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                    name+"Operation");
        }

        // Create compilation unit for concrete facade
        CompilationUnit concreteFacadeUnit = addCompilationUnit();

        logger.info("ADDING CONCRETE FACADE CLASS: " + concreteFacade);

        // Add concrete facade class to compilation unit
        ClassOrInterfaceDeclaration concreteFacadeClass =
                addClass(concreteFacadeUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteFacade,
                        facade);

        String[] names = new String[subsystems.length];
        // Create names for concrete facade fields
        for(int i = 0; i < names.length; i++){
            // Create name for subsystem field
            names[i] = Character.toLowerCase(subsystems[i].charAt(0))
                    + subsystems[i].substring(1);
        }

        // Create list of subsystem fields
        Field[] fields = new Field[subsystems.length];
        // Create fields for concrete facade class
        for(int i = 0; i < fields.length; i++){
            // Create field pair for field
            Field field = new Field(subsystems[i],
                    names[i], Modifier.Keyword.PRIVATE);
            fields[i] = field;
        }

        // Add fields to concrete facade class
        addFields(concreteFacadeClass, fields);

        // Add constructor to concrete facade
        ConstructorDeclaration concreteFacadeConstructor =
                concreteFacadeClass.addConstructor(Modifier.Keyword.PUBLIC);

        BlockStmt constructorBody = new BlockStmt();

        // Create list of parameters for concrete facade constructor
        for(Field field: fields){
            concreteFacadeConstructor.addParameter(field.type, field.name);
            constructorBody.addStatement("this." + field.name + " = " + field.name+";");
        }

        // Set body of concrete facade constructor
        concreteFacadeConstructor.setBody(constructorBody);

        // Add overridden methods to concrete facade class
        for(int i = 0; i < fields.length; i++){
            // Create body of overridden method
            BlockStmt body = new BlockStmt();
            body.addOrphanComment(new LineComment("TODO"));
            // Create expression for facade delegation call to subsystem
            MethodCallExpr opCall =
                    new MethodCallExpr(fields[i].name+".operation");
            opCall.addOrphanComment(new LineComment("TODO"));
            // Add call to the subsystem
            body.addStatement(opCall);
            // Add method with body to concretes facade class
            addMethod(concreteFacadeClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    names[i]+"Operation", body);
        }

        logger.info("ADDING SUBSYSTEM CLASSES...");

        // Create compilation units for subsystems
        for(String subsystem: subsystems) {
            // Create compilation unit for subsystem
            CompilationUnit subsystemUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + subsystem);

            // Add class to compilation unit
            ClassOrInterfaceDeclaration subsystemClass =
                    addClass(subsystemUnit,
                            new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                            subsystem);
            // Create body of method
            BlockStmt body = new BlockStmt();
            body.addOrphanComment(new LineComment("TODO"));
            // Add method that facade will delegate too
            addMethod(subsystemClass,
                    new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "operation", body);
        }
    }



}
