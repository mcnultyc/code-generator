/**
 * @author Carlos Antonio McNulty
 */

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.typesafe.config.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DesignPatternGeneratorTests {


    @Test
    public void testDesignPatternGenFactoryCorrectness(){

        // Facade pattern description in conf
        String facadeConf = "    {\n" +
                "        design-pattern  = \"facade\"\n" +
                "        facade          = \"Facade\"\n" +
                "        concrete-facade = \"Facade1\"\n" +
                "        subsystems      = [\n" +
                "            \"Subsystem1\", \"Subsystem2\"\n" +
                "        ]\n" +
                "    }";
        // Get config object from string resource
        Config config = ConfigFactory.parseString(facadeConf);

        // Create design pattern generator from factory method
        DesignPatternGenerator generator =
                DesignPatternGenFactory.create(config);

        // Test that the correct object was created
        assertTrue((generator instanceof FacadePatternGenerator));
    }


    @Test
    public void testDesignPatternFromConfigCorrectness(){

        // Chain pattern description in conf
        String chainConf = "    {\n" +
                "        design-pattern      = \"chain\"\n" +
                "        sender              = \"Sender\"\n" +
                "        handler             = \"Handler\"\n" +
                "        concrete-handlers   = [\n" +
                "            \"Receiver1\", \"Receiver2\"\n" +
                "        ]\n" +
                "    }";
        // Get config object from string resource
        Config config = ConfigFactory.parseString(chainConf);

        // Create design pattern generator from config resource
        ChainPatternGenerator generator = new ChainPatternGenerator(config);

        // Get compilation units from generator
        ArrayList<CompilationUnit> compilationUnits = generator.getCompilationUnits();

        // Store class names expected to be found in compilation units
        String[] namesExpected = {"Sender", "Handler", "Receiver1", "Receiver2"};
        ArrayList<String> namesFound = new ArrayList<>();

        // Check the correctness of the compilation units
        for(CompilationUnit compilationUnit: compilationUnits){
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            // Get name of class
            String name = types.get(0).getNameAsString();
            namesFound.add(name);
        }

        // Check that all expected classes were found
        for(String nameExpected: namesExpected){
            // Check that expected class was already seen
            assertTrue(namesFound.contains(nameExpected));
        }
    }


    @Test
    public void testCompilationUnitCorrectness(){

        // Create abstract factory generator
        AbstractFactoryPatternGenerator factory =
                new AbstractFactoryPatternGenerator("Factory", "Factory1", "ProductA",
                        new String[]{"ProductA1"}, "ProductB", new String[]{"ProductB1"});

        // Get compilation units for the class names provided
        ArrayList<CompilationUnit> compilationUnits =
                factory.getCompilationUnits();

        // Check that there is one compilation unit for each class
        assertEquals(6, compilationUnits.size());

        // Store the list of class names expected
        ArrayList<String> namesExpected = new ArrayList<String>(){
            {
                add("Factory");
                add("Factory1");
                add("ProductA");
                add("ProductB");
                add("ProductA1");
                add("ProductB1");
            }
        };

        ArrayList<String> namesFound = new ArrayList<>();

        // Check the correctness of the compilation units
        for(CompilationUnit compilationUnit: compilationUnits){
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            // Check that each compilation unit has exactly one class
            assertEquals(1, types.size());
            // Get name of class
            String name = types.get(0).getNameAsString();
            namesFound.add(name);
        }

        // Check that the correct number of classes were found
        assertTrue(namesExpected.size() == namesFound.size());

        // Check that all expected classes were found
        for(String nameExpected: namesExpected){
            // Check that expected class was already seen
            assertTrue(namesFound.contains(nameExpected));
        }
    }


    @Test
    public void testFieldDeclarationCorrectness(){

        FacadePatternGenerator facade =
                new FacadePatternGenerator("Facade", "Facade1",
                        new String[]{"Subsystem1", "Subsystem2"});

        ArrayList<CompilationUnit> compilationUnits = facade.getCompilationUnits();

        ClassOrInterfaceDeclaration concreteFacade = null;

        for(CompilationUnit compilationUnit: compilationUnits){
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            // Get name of class
            String name = types.get(0).getNameAsString();
            if(name == "Facade1"){
                // Get class declaration from compilation unit
                Optional<ClassOrInterfaceDeclaration> classOpt =
                        compilationUnit.getClassByName(name);
                if(classOpt.isPresent()){
                    // Set concrete facade class
                    concreteFacade = classOpt.get();
                }
            }
        }
        // Check that concrete facade class was found
        assertNotEquals(null, concreteFacade);
        // Store list of field names for concrete facade
        String[] namesExpected = {"subsystem1", "subsystem2"};
        // Get fields from concrete facade
        List<FieldDeclaration> fields = concreteFacade.getFields();

        ArrayList<String> namesFound = new ArrayList<>();

        // Store fields found in concrete facade
        for(FieldDeclaration field: fields){
            // Get field name
            String name = field.getVariable(0).getNameAsString();
            namesFound.add(name);
        }

        // Check that correct number of fields are present
        assertEquals(2, namesFound.size());

        // Check that correct fields are present
        for(String nameExpected: namesExpected){
            assertTrue(namesFound.contains(nameExpected));
        }
    }


    @Test
    public void testMethodDeclarationCorrectness(){

        MediatorPatternGenerator mediator =
                new MediatorPatternGenerator("Mediator", "Mediator1", "Colleague",
                        new String[]{"Colleague1", "Colleague2"});

        ArrayList<CompilationUnit> compilationUnits = mediator.getCompilationUnits();

        ArrayList<ClassOrInterfaceDeclaration> concreteColleagues =
                new ArrayList<>();

        for(CompilationUnit compilationUnit: compilationUnits){
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            // get name of class
            String name = types.get(0).getNameAsString();
            // check that class is one of the two colleagues
            if(name == "Colleague1" || name == "Colleague2"){
                // get class declaration from compilation unit
                Optional<ClassOrInterfaceDeclaration> classOpt =
                        compilationUnit.getClassByName(name);
                if(classOpt.isPresent()){
                    // Add declaration to collection
                    concreteColleagues.add(classOpt.get());
                }
            }
        }

        // Check that two of the concrete colleagues exist in compilation units
        assertEquals(2, concreteColleagues.size());

        String[] overridenMethods = {"getState", "doAction"};

        // Check that each concrete colleague is overriding correct methods
        for(ClassOrInterfaceDeclaration concreteColleague: concreteColleagues){
            // Get methods from concrete colleague
            List<MethodDeclaration> methods = concreteColleague.getMethods();
            // Create list to store method names
            ArrayList<String> names = new ArrayList<>();
            for(MethodDeclaration method: methods){
                // Check that method has body
                if(method.getBody().isPresent()){
                    // Add method name to collection
                    names.add(method.getNameAsString());
                }
            }
            // Check that colleague has defined certain methods
            for(String overriddenMethod: overridenMethods){
                // Check that method was defined
                assertTrue(names.contains(overriddenMethod));
            }
        }
    }

    @Test
    public void testFieldFocusListener(){

    }
}
