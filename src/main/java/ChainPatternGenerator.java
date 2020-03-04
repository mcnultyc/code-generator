/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class ChainPatternGenerator extends DesignPatternGenerator {


    private String      sender;
    private String      handler;
    private String[]    concreteHandlers;

    private Logger logger = LoggerFactory.getLogger(ChainPatternGenerator.class);


    /**
     *
     * @param config
     */
    public ChainPatternGenerator(Config config){
        super();

        this.sender             = config.getString("sender");
        this.handler            = config.getString("handler");
        this.concreteHandlers   =
                config.getStringList("concrete-handlers").toArray(new String[0]);

        build();
    }


    /**
     *
     * @param sender  initiates the request to a concrete handler object on the chain
     * @param handler  defines an interface for handling requests
     * @param concreteHandlers handles requests it is responsible for
     */
    public ChainPatternGenerator(String sender,
                                   String handler,
                                   String[] concreteHandlers){
        super();

        this.sender = sender;
        this.handler = handler;
        this.concreteHandlers = concreteHandlers;

        build();
    }


    protected void build(){

        logger.info("CREATING COMPILATION UNITS FOR CHAIN PATTERN...");

        // Create compilation unit for sender
        CompilationUnit senderUnit = addCompilationUnit();

        logger.info("ADDING SENDER CLASS: " + sender);

        // Add sender class to compilation unit
        ClassOrInterfaceDeclaration senderClass =
                addClass(senderUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC} , sender);

        // Add handler field to sender class
        addField(senderClass, new Field("Handler", "handler", Modifier.Keyword.PRIVATE));

        // Add constructor to sender class
        addConstructor(senderClass, new Param[]{new Param("Handler", "handler")},
                new BlockStmt().addStatement("this.handler = handler;"));

        // Create compilation unit for handler
        CompilationUnit handlerUnit = addCompilationUnit();

        logger.info("ADDING ABSTRACT HANDLER CLASS: " + handler);

        // Create class for handler
        ClassOrInterfaceDeclaration handlerClass =
                addClass(handlerUnit,
                        new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT} ,
                        handler);

        // Add successor field to handler class
        addField(handlerClass, new Field("Handler", "successor", Modifier.Keyword.PRIVATE));

        // Add constructor to handler class
        addConstructor(handlerClass, new Param[]{new Param("Handler", "successor")},
                new BlockStmt().addStatement("this.successor = successor;"));

        // Add abstract handler request method to handler class
        addMethod(handlerClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT},
                "handleRequest");

        logger.info("ADDING CONCRETE HANDLER CLASSES...");

        // Create compilation units for concrete handlers
        for(String concreteHandler: concreteHandlers){
            // Create compilation unit
            CompilationUnit concreteHandlerUnit = addCompilationUnit();

            logger.info("ADDING CLASS: " + concreteHandler);

            // Create concrete handler class
            ClassOrInterfaceDeclaration concreteHandlerClass =
                    addClass(concreteHandlerUnit,
                            new Modifier.Keyword[]{Modifier.Keyword.PUBLIC} ,
                            concreteHandler, handler);

            // Add constructor to concrete handler
            addConstructor(concreteHandlerClass,
                    new Param[]{new Param("Handler", "successor")},
                    new BlockStmt().addStatement("super(successor);"));

            // Create body of overridden method
            BlockStmt body = new BlockStmt();
            body.addOrphanComment(new LineComment("TODO"));
            // Add body of overridden method to concrete handler
            addOverridenMethod(concreteHandlerClass, new Modifier.Keyword[]{Modifier.Keyword.PUBLIC},
                    "handleRequest", body);
        }
    }
}
