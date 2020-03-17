/**
 * @author Carlos Antonio McNulty
 */


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;


/**
 *
 */
public abstract class DesignPatternGenerator {


    protected ArrayList<CompilationUnit> compilationUnits;

    private Logger logger = LoggerFactory.getLogger(DesignPatternGenerator.class);


    DesignPatternGenerator(){
        this.compilationUnits = new ArrayList<>();
    }


    /**
     *
     * @return
     */
    public ArrayList<CompilationUnit> getCompilationUnits(){
        if(compilationUnits.size() == 0){
            build();
        }
        return compilationUnits;
    }


    protected CompilationUnit addCompilationUnit(){
        // Default text set for auto-generated files
        String commentText = "This file has been auto-generated.";
        // Create compilation unit
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.addOrphanComment(new BlockComment(commentText));
        compilationUnits.add(compilationUnit);
        return compilationUnit;
    }


    protected ClassOrInterfaceDeclaration addClass(CompilationUnit compilationUnit,
                                                   Modifier.Keyword[] modifiers,
                                                   String type,
                                                   String extendedType){
        // Add class to compilation unit and set extended type
        return addClass(compilationUnit, modifiers, type).addExtendedType(extendedType);
    }


    protected ClassOrInterfaceDeclaration addClass(CompilationUnit compilationUnit,
                                                   Modifier.Keyword[] modifiers,
                                                   String type){
        // Add class to compilation unit
        ClassOrInterfaceDeclaration unit = compilationUnit.addClass(type);
        // Add modifiers to class declaration
        for(Modifier.Keyword modifier: modifiers){
            unit.addModifier(modifier);
        }
        return unit;
    }


    protected ConstructorDeclaration addConstructor(ClassOrInterfaceDeclaration unit,
                                                    Param[] params,
                                                    BlockStmt body){
        // Create constructor for class
        ConstructorDeclaration constructor =
                unit.addConstructor(Modifier.Keyword.PUBLIC);
        // Add parameters to constructor
        for(Param param: params){
            constructor.addParameter(param.type, param.name);
        }
        // Set body of constructor
        constructor.setBody(body);
        return constructor;
    }


    protected ArrayList<FieldDeclaration> addFields(ClassOrInterfaceDeclaration unit,
                             Field[] fields){
        ArrayList<FieldDeclaration> fieldDeclarations = new ArrayList<>();
        // Add fields to class
        for(Field field: fields){
            fieldDeclarations.add(addField(unit, field));
        }
        return fieldDeclarations;
    }


    protected FieldDeclaration addField(ClassOrInterfaceDeclaration unit, Field field){
        // Parse type from string
        ParseResult<ClassOrInterfaceType> result =
                new JavaParser().parseClassOrInterfaceType(field.type);
        Optional<ClassOrInterfaceType> opt = result.getResult();
        if(opt.isPresent()) {
            // Create field declaration for field
            FieldDeclaration fieldDeclaration =
                    new FieldDeclaration(new NodeList<>(new Modifier(field.access)),
                            opt.get(), field.name);
            // Add field to class
            unit.addMember(fieldDeclaration);
            return fieldDeclaration;
        }
        return null;
    }



    protected FieldDeclaration addField(ClassOrInterfaceDeclaration unit, Field field, String initializer){
        // Create field with initializer
        return unit.addFieldWithInitializer(field.type, field.name,
                new NameExpr(new SimpleName(initializer)), Modifier.Keyword.PRIVATE);
    }


    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String name){
        // Create method declaration and set body
        MethodDeclaration methodDeclaration =
                unit.addMethod(name);
        // Add modifiers to method
        for(Modifier.Keyword modifier: modifiers){
            methodDeclaration.addModifier(modifier);
        }
        // No body provided
        methodDeclaration.removeBody();
        return methodDeclaration;
    }


    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String type,
                                          String name){
        // Create and add method without body
        MethodDeclaration method = addMethod(unit, modifiers, name);
        // Set return type of method and return method
        return method.setType(type);
    }


    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String type,
                                          String name,
                                          BlockStmt body){

        // Create and add method with body
        MethodDeclaration method = addMethod(unit, modifiers, type, name);
        // Set body of method and return method
        return method.setBody(body);
    }


    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String name,
                                          BlockStmt body){
        // Create and add method body
        return addMethod(unit, modifiers, name).setBody(body);
    }


    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String name,
                                          Param[] params,
                                          BlockStmt body){
        // Create method and add body
        return addMethod(unit, modifiers, name, params).setBody(body);
    }

    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String name,
                                          Param[] params){
        // Create method
        MethodDeclaration method = addMethod(unit, modifiers, name);
        // Add parameters to method
        for(Param param: params){
            method.addParameter(param.type, param.name);
        }
        return method.removeBody();
    }


    protected MethodDeclaration addMethod(ClassOrInterfaceDeclaration unit,
                                          Modifier.Keyword[] modifiers,
                                          String type,
                                          String name,
                                          Param[] params){
        // Create method with type and params
        return addMethod(unit, modifiers, name, params).setType(type);
    }


    protected MethodDeclaration addOverridenMethod(ClassOrInterfaceDeclaration unit,
                                                   Modifier.Keyword[] modifiers,
                                                   String name,
                                                   BlockStmt body){
        // Create method without parameters and add override annotation
        return addMethod(unit, modifiers, name, body).addMarkerAnnotation("Override");
    }


    protected MethodDeclaration addOverridenMethod(ClassOrInterfaceDeclaration unit,
                                                   Modifier.Keyword[] modifiers,
                                                   String name,
                                                   Param[] params,
                                                   BlockStmt body){
        // Create method with parameters and add override annotation
        return addMethod(unit, modifiers, name, params, body).addMarkerAnnotation("Override");
    }


    protected MethodDeclaration addOverridenMethod(ClassOrInterfaceDeclaration unit,
                                                   Modifier.Keyword[] modifiers,
                                                   String type,
                                                   String name,
                                                   BlockStmt body){
        // Create method and add override annotation
        return addMethod(unit, modifiers, type, name, body).addMarkerAnnotation("Override");
    }

    protected abstract void build();


    private void addPsiFiles(PsiDirectory directory){

    }


    public ArrayList<CompilationUnit> generate(Project project, PsiDirectory directory){

        // Check if the compilation units have been created yet
        if(compilationUnits.size() == 0){
            // Build the compilation units
            build();
        }

        logger.info("STORING COMPILATION UNITS TO PACKAGE: " + directory.getName());

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        // Create runnable to be executed later by the command processor
        Runnable runnable = () -> {
            for (CompilationUnit compilationUnit : compilationUnits) {

                // Get name of compilation unit type
                String type = compilationUnit.getType(0).getNameAsString();
                String filename = type + ".java";

                // Intellij only \n for line separators and will throw exception
                String text = compilationUnit.toString().replaceAll("\r\n", "\n");

                // Create psi file for compilation unit
                PsiFile psiFile =
                        fileFactory.createFileFromText(filename, JavaFileType.INSTANCE, text);

                // Add psi file to directory
                directory.add(psiFile);
            }
        };

        // Execute write command
        WriteCommandAction.runWriteCommandAction(project, runnable);

        return compilationUnits;
    }


    /**
     * Stores the compilation units created by this design pattern in
     * the path provided.
     * @param path path to directory where compilation units will be saved.
     * @return list of compilation units
     */
    public ArrayList<CompilationUnit> generate(String path){

        // Check if the compilation units have been created yet
        if(compilationUnits.size() == 0){
            // Build the compilation units
            build();
        }

        logger.info("STORING COMPILATION UNITS TO PATH: " + path);

        for(CompilationUnit compilationUnit: compilationUnits){
            // Get name of compilation unit type
            String type = compilationUnit.getType(0).getNameAsString();

            // Create absolute path to store compilation unit
            Path unitPath = Paths.get(path, type+".java");
            File file = new File(unitPath.toString());

            // Check that file doesn't already exist
            if(!file.exists()){
                // Check that file isn't a directory
                if(!file.isDirectory()){
                    try {
                        // Store compilation unit in file
                        Files.write(unitPath, compilationUnit.toString().getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    logger.error("FILE <"+unitPath.toString()+"> IS A DIRECTORY");
                }
            }
            else{
                logger.error("FILE <"+unitPath.toString()+"> ALREADY EXISTS");
            }
        }
        return compilationUnits;
    }


    /**
     * Stores the compilation units created by this design pattern in
     * the path provided.
     * @param path path to directory where compilation units will be saved.
     * @return list of compilation units
     */
    public ArrayList<CompilationUnit> generate(Path path){

        // Check if the compilation units have been created yet
        if(compilationUnits.size() == 0){
            // Build the compilation units
            build();
        }

        logger.info("STORING COMPILATION UNITS TO PATH: " + path.toString());

        for(CompilationUnit compilationUnit: compilationUnits){
            // Get name of compilation unit type
            String type = compilationUnit.getType(0).getNameAsString();

            // Create absolute path to store compilation unit
            Path unitPath = Paths.get(path.toString(), type+".java");
            File file = new File(unitPath.toString());

            // Check that file doesn't already exist
            if(!file.exists()){
                // Check that file isn't a directory
                if(!file.isDirectory()){
                    try {
                        // Store compilation unit in file
                        Files.write(unitPath, compilationUnit.toString().getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    logger.error("FILE <"+unitPath.toString()+"> IS A DIRECTORY");
                }
            }
            else{
                logger.error("FILE <"+unitPath.toString()+"> ALREADY EXISTS");
            }
        }
        return compilationUnits;
    }


}
