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
public class MediatorPatternGenerator extends DesignPatternGenerator {


    private String      mediator;
    private String      concreteMediator;
    private String      colleague;
    private String[]    concreteColleagues;

    private Logger logger = LoggerFactory.getLogger(MediatorPatternGenerator.class);


    /**
     *
     * @param config
     */
    public MediatorPatternGenerator(Config config){
        super();

        this.mediator           = config.getString("mediator");
        this.concreteMediator   = config.getString("concrete-mediator");
        this.colleague          = config.getString("colleague");
        this.concreteColleagues =
                config.getStringList("concrete-colleagues").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param mediator  defines an interface for communicating with colleague objects
     * @param concreteMediator implements cooperative behavior by coordinating colleague objects
     * @param colleague base of the colleagues hierarchy
     * @param concreteColleagues colleagues communicates with its mediator whenever it would have otherwise
     *                   communicated with another colleague
     */
    public MediatorPatternGenerator(String mediator,
                                    String concreteMediator,
                                    String colleague,
                                    String[] concreteColleagues){
        super();

        this.mediator = mediator;
        this.concreteMediator = concreteMediator;
        this.colleague = colleague;
        this.concreteColleagues = concreteColleagues;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR MEDIATOR PATTERN...");

        // Create compilation unit for interface
        CompilationUnit mediatorUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT MEDIATOR CLASS: " + mediator);

        // Create class for mediator
        ClassOrInterfaceDeclaration mediatorClass =
                addClass(mediatorUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                        mediator);

        // Add mediate method to mediator
        addMethod(mediatorClass,
                new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                "mediate", new Param[]{new Param(colleague, "colleague")});

        // Create compilation unit for concrete mediator
        CompilationUnit concreteMediatorUnit = addCompilationUnit();

        logger.info("ADDING CONCRETE MEDIATOR CLASS: " + concreteMediator);

        // Create class for concrete mediator
        ClassOrInterfaceDeclaration concreteMediatorClass =
                addClass(concreteMediatorUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                        concreteMediator, mediator);

        // Create compilation unit for colleague
        CompilationUnit colleagueUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT COLLEAGUE CLASS: " + colleague);

        // Create class for colleague
        ClassOrInterfaceDeclaration colleagueClass =
                addClass(colleagueUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                        colleague);

        // Add mediator field to abstract colleague class
        addField(colleagueClass,
                new Field(mediator, "mediator", Modifier.Keyword.PROTECTED));

        // Add constructor to abstract colleague class
        addConstructor(colleagueClass, new Param[]{new Param(mediator,"mediator")},
                new BlockStmt().addStatement("this.mediator = mediator;"));

        // Add methods used by mediator to check colleague state
        addMethod(colleagueClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                "getState");

        addMethod(colleagueClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                "doAction");

        logger.info("CREATING CONCRETE COLLEAGUE CLASSES...");

        // Create compilation units for each concrete colleague
        for(String concreteColleague: concreteColleagues){
            // Create compilation unit
            CompilationUnit concreteColleagueUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + concreteColleague);

            // Create concrete colleague class
            ClassOrInterfaceDeclaration concreteColleagueClass =
                    addClass(concreteColleagueUnit, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                            concreteColleague, colleague);

            // Add constructor to concrete colleague class
            addConstructor(concreteColleagueClass, new Param[]{new Param(mediator, "mediator")},
                    new BlockStmt().addStatement("super(mediator);"));

            // Create empty body with line comment
            BlockStmt emptyBody = new BlockStmt();
            emptyBody.addOrphanComment(new LineComment("TODO"));

            // Add bodies to inherited abstract methods from colleague
            addOverridenMethod(concreteColleagueClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "getState", emptyBody);

            addOverridenMethod(concreteColleagueClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "doAction", emptyBody);

            // Create body for changed method
            BlockStmt changedBody = new BlockStmt();
            changedBody.addOrphanComment(new LineComment("TODO"));
            MethodCallExpr mediateCall = new MethodCallExpr("mediator.mediate",
                    new NameExpr("this"));
            changedBody.addStatement(mediateCall);

            // Add method used to report changes to mediator
            addMethod(concreteColleagueClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "changed", changedBody);
        }

        int count = 1;
        // Add concrete colleague fields to concrete mediator
        for(String concreteColleague: concreteColleagues){
            addField(concreteMediatorClass,
                    new Field(concreteColleague, "colleague"+ count,
                            Modifier.Keyword.PRIVATE));
            count++;
        }

        // Create parameters and body for concrete mediator constructor
        BlockStmt constructorBody = new BlockStmt();
        Param[] constructorParams = new Param[concreteColleagues.length];
        for(int i = 0; i < constructorParams.length; i++){
            String name = "colleague"+(i+1);
            constructorParams[i] =
                    new Param(concreteColleagues[i], name);
            constructorBody.addStatement("this." + name + " = " + name + ";");
        }

        // Adding constructor to concrete mediator
        addConstructor(concreteMediatorClass, constructorParams, constructorBody);

        // Create body for overridden mediate method
        BlockStmt mediateBody = new BlockStmt();
        mediateBody.addOrphanComment(new LineComment("TODO"));
        // Add colleague do action method call
        MethodCallExpr mediateCall = new MethodCallExpr("colleague.doAction");
        mediateBody.addStatement(mediateCall);

        // Add mediate method to concrete mediator
        addOverridenMethod(concreteMediatorClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                "mediate", new Param[]{new Param(colleague, "colleague")},
                mediateBody);;
    }
}
