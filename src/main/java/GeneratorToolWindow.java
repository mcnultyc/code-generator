/**
 * @author Carlos Antonio McNulty
 */


import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.projectView.impl.nodes.PackageUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.PackageIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.table.JBTable;
import com.intellij.psi.*;
import com.intellij.util.Query;
import com.intellij.util.indexing.FileBasedIndex;
import com.typesafe.config.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;


public class GeneratorToolWindow{


    private JPanel contentPane;
    private JComboBox patternComboBox;

    private JTextField classTextField1;
    private JTextField classTextField4;
    private JTextField classTextField2;
    private JTextField classTextField3;

    private JTable table1;
    private JTable table2;

    private JButton addRowButton1;
    private JButton addRowButton2;
    private JButton generateButton;

    private JScrollPane table1ScrollPane;
    private JScrollPane table2ScrollPane;
    private JTextField packageField;

    private Map<String, Map<String, Object>> designMaps;
    private Map<String, Map<String, String>> toolTipMaps;

    private JTextField[] fields;

    private static Logger logger = LoggerFactory.getLogger(GeneratorToolWindow.class);


    public GeneratorToolWindow(ToolWindow toolWindow) {

        $$$setupUI$$$();

        // Set the border color and thickness of the table scroll panes
        table1ScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        table2ScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        // Set the color of the table headers
        table1.getTableHeader().setForeground(Color.DARK_GRAY);
        table2.getTableHeader().setForeground(Color.DARK_GRAY);

        // Each field becomes element in array (for convenience)
        fields = new JTextField[4];
        fields[0] = classTextField1;
        fields[1] = classTextField2;
        fields[2] = classTextField3;
        fields[3] = classTextField4;

        // Add tool tip to package field
        String packageToolTip = "Enter package name. If left blank then default package" +
                " is chosen. If package doesn't exit then it will be created for you.";
        packageField.setToolTipText(packageToolTip);

        logger.info("CREATING COMPONENT");

        // Add action listener to table 1 add button
        addRowButton1.addActionListener(e ->{
            // Only add rows to enabled tables
            if(table1.isEnabled()){
                DefaultTableModel model = (DefaultTableModel) table1.getModel();
                // Add row to table 1
                model.addRow(new Object[]{});
            }
        });

        // Add action listener to table 2 add button
        addRowButton2.addActionListener(e ->{

            // Only add rows to enabled tables
            if(table2.isEnabled()){
                DefaultTableModel model = (DefaultTableModel) table2.getModel();

                // Add row to table 2
                model.addRow(new Object[]{});
            }
        });

        patternComboBox.addActionListener(e -> {
            if(patternComboBox.getSelectedIndex() != -1){
                String pattern = (String) patternComboBox.getSelectedItem();

                // Update fields in plugin based on design pattern selected
                updateFields(pattern);
            }
        });

        generateButton.addActionListener(e -> {
            if(patternComboBox.getSelectedIndex() != -1){

                 // Get the currently selected pattern
                String pattern = (String) patternComboBox.getSelectedItem();

                // Read the fields to update the design pattern
                Map<String, Object> designMap = readFields(pattern);

                // Check if design map was built from user input
                if(designMap == null){
                    return;
                }

                // Create config from design map of respective pattern
                Config config = ConfigFactory.parseMap(designMap);

                // Get the currently open project
                Project project = getActiveProject();
                if(project != null){

                    // Get the package name from field
                    String packageName = packageField.getText();

                    // Get directory for package
                    PsiDirectory directory = getDirectory(project, packageName);

                    // Check that virtual file exists
                    if(directory != null){

                        // Create design pattern generator based on config
                        DesignPatternGenerator generator = DesignPatternGenFactory.create(config);
                        if(generator != null){

                            // Generate files inside of package
                            generator.generate(project, directory);
                        }
                    }
                    else{

                        // Add package and get psi directory for package
                        PsiDirectory newDirectory = addPackage(project, packageName);

                        // Check if package directory was created (user could reject)
                        if(newDirectory != null){

                            // Create design pattern generator based on config
                            DesignPatternGenerator generator = DesignPatternGenFactory.create(config);
                            if(generator != null){

                                // Generate files inside of new package
                                generator.generate(project, newDirectory);
                            }
                        }
                        else{
                            logger.info("PACKAGE NOT CREATED");
                        }
                    }
                }
            }
        });

        // Create tree map to store maps for each design pattern
        designMaps = new TreeMap<>();

        // Create tree map to store tool tips for each design pattern
        toolTipMaps = new TreeMap<>();

        // Store the list of design patterns supported
        List<String> patterns = new ArrayList<>();

        try {

            // Get the config file input stream
            InputStream inStream = GeneratorToolWindow.class.getResourceAsStream("design-patterns.conf");
            if (inStream != null) {
                Reader reader = new InputStreamReader(inStream);

                // Load the configuration file
                Config config = ConfigFactory.parseReader(reader);

                // Get the list of configs for each design pattern
                List<? extends Config> configs = config.getConfigList("design-patterns");

                // Convert design pattern configs into maps
                for(Config designConfig: configs){
                    String pattern = designConfig.getString("design-pattern");
                    Map<String, Object> designMap = new TreeMap<>();

                    // Add key-value pairs from config to map
                    designConfig.entrySet()
                            .forEach(e -> {designMap.put(e.getKey(), e.getValue().unwrapped());});
                    designMaps.put(pattern, designMap);
                    patterns.add(pattern);
                }

                // Get the list of configs for each design pattern
                List<? extends Config> toolTipConfigs = config.getConfigList("tool-tips");

                // Convert tool tip config into map
                for(Config toolTipConfig: toolTipConfigs){
                    String pattern = toolTipConfig.getString("design-pattern");
                    Map<String, String> toolTipMap = new TreeMap<>();

                    // Add key-value pairs from config to map
                    toolTipConfig.entrySet()
                            .forEach(e -> {toolTipMap.put(e.getKey(), e.getValue().unwrapped().toString());});

                    // Remove key for design pattern
                    toolTipMap.remove("design-pattern");

                    toolTipMaps.put(pattern, toolTipMap);
                }

                inStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        // Add patterns to combo box
        for(String pattern: patterns){
            patternComboBox.addItem(pattern);
        }

        // Check if design patterns were found
        if(patterns.size() > 0) {
            // Set the default design pattern for combo box
            updateFields(patterns.get(0));
        }
        else{
            logger.error("NO DESIGN PATTERNS IN CONFIG FILE");
        }

    }


    private void updateToolTips(String pattern){

        // Get the tool tip map for given design pattern
        Map<String, String> toolTipMap = toolTipMaps.get(pattern);

        // Get the field keys used by this design pattern
        List<String> fieldKeys = getFieldKeys(pattern);

        // Update tool tips for each field
        for(int i = 0; i < fields.length && i < fieldKeys.size(); i++){
            // Get tool tip text for given key
            String toolTipText = toolTipMap.get(fieldKeys.get(i));
            fields[i].setToolTipText(toolTipText);
        }

        // Get the table keys used by this design pattern
        List<String> tableKeys = getTableKeys(pattern);

        // Update tool tips for each table
        if(tableKeys.size() >= 1){

            // Set the tool tip text for table 1
            String toolTipText = toolTipMap.get(tableKeys.get(0));
            table1.setToolTipText(toolTipText);
        }
        if(tableKeys.size() >= 2){

            // Set the tool tip text for table 2
            String toolTipText = toolTipMap.get(tableKeys.get(1));
            table2.setToolTipText(toolTipText);
        }
    }


    private String getSourcePath(Project project){

        // Get the project manager
        ProjectRootManager manager = ProjectRootManager.getInstance(project);

        // Get all the source roots in the project
        VirtualFile[] sourceRoots = manager.getContentSourceRoots();

        // Check if project has any existing source roots
        if(sourceRoots.length > 0){
            String path = sourceRoots[0].getPath();

            // Return path of first source root found
            return path;
        }
        else{

            // Get all content roots of project
            VirtualFile[] contentRoots = manager.getContentRoots();
            if(contentRoots.length > 0){
                String path = contentRoots[0].getPath();

                // Return path of first content root found
                return path;
            }
        }

        // Return path of working directory
        return System.getProperty("user.dir");
    }


    private Project getActiveProject(){

        // Get list of open projects
        Project[] projects = ProjectManager.getInstance().getOpenProjects();

        // Iterate through open projects looking for active project
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);

            // Check for active window
            if (window != null && window.isActive()) {
                return project;
            }
        }
        return null;
    }


    private void updateTable(String headerText, JTable table, boolean enabled){

        // Set enabled flag for table
        table.setEnabled(enabled);

        // Set the header for table
        JTableHeader header = table.getTableHeader();
        TableColumnModel columnModel = header.getColumnModel();
        columnModel.getColumn(0).setHeaderValue(headerText);

        // Clear data from table
        for(int i = 0; i < table.getRowCount(); i++){
            for(int j = 0; j < table.getColumnCount(); j++){
                table.setValueAt("", i, j);
            }
        }

        // Set row count back to original 3 rows
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setRowCount(3);
    }


    private void updateTables(List<String> headerTexts){

        // Make table scroll panes invisible by default
        table1ScrollPane.setVisible(false);
        table2ScrollPane.setVisible(false);

        // Make table buttons invisible buttons
        addRowButton1.setVisible(false);
        addRowButton2.setVisible(false);

        GridBagLayout layout = (GridBagLayout) contentPane.getLayout();

        // Get constraints for both tables
        GridBagConstraints table1Constraints = layout.getConstraints(table1ScrollPane);
        GridBagConstraints table2Constraints = layout.getConstraints(table2ScrollPane);

        // Make scroll panes and buttons visible selectively
        if(headerTexts.size() == 0){

            // Remove headers and disable both tables
            updateTable("", table1, false);
            updateTable("", table2, false);
        }
        if(headerTexts.size() == 1){

            // Add header to first table and disable second table
            updateTable(headerTexts.get(0), table1, true);
            updateTable("", table2, false);

            // Make table 1 scroll pane and button visible
            table1ScrollPane.setVisible(true);
            addRowButton1.setVisible(true);

            // Set constraints for button to be centered along entire row
            GridBagConstraints buttonConstraints = layout.getConstraints(addRowButton1);
            buttonConstraints.gridwidth = 2;
            buttonConstraints.anchor = GridBagConstraints.CENTER;

            // Update button constraints with layout manager
            layout.setConstraints(addRowButton1, buttonConstraints);

            // Set table constraints to fill entire row
            table1Constraints.gridwidth = 2;
        }
        else if(headerTexts.size() >= 2){

            // Add headers and enable both tables
            updateTable(headerTexts.get(0), table1, true);
            updateTable(headerTexts.get(1), table2, true);

            // Make both tables scroll panes visible
            table1ScrollPane.setVisible(true);
            table2ScrollPane.setVisible(true);

            // Make both tables buttons visible
            addRowButton1.setVisible(true);
            addRowButton2.setVisible(true);

            // Set constraints for button to be relocated in one column
            GridBagConstraints buttonConstraints = layout.getConstraints(addRowButton1);
            buttonConstraints.gridwidth = 1;
            buttonConstraints.anchor = GridBagConstraints.CENTER;

            // Update button constraints with layout manager
            layout.setConstraints(addRowButton1, buttonConstraints);

            // Set table constraints to share row
            table1Constraints.gridwidth = 1;
            table2Constraints.gridwidth = 1;
        }

        // Update both tables constraints with layout manager
        layout.setConstraints(table1ScrollPane, table1Constraints);
        layout.setConstraints(table2ScrollPane, table2Constraints);

        table1.repaint();
        table2.repaint();
    }


    private List<String> getFieldKeys(String pattern){

        // Get design map for given pattern
        Map<String, Object> designMap = designMaps.get(pattern);
        List<String> keys = new ArrayList<>();

        // Go through the entries in the design pattern map
        for(Map.Entry<String, Object> entry: designMap.entrySet()){

            // Check if object is of type string
            if(entry.getValue() instanceof String){
                keys.add(entry.getKey());
            }
        }

        // Remove design pattern key (redundant)
        keys.remove("design-pattern");

        return keys;
    }


    private List<String> getTableKeys(String pattern){

        // Get design map for given pattern
        Map<String, Object> designMap = designMaps.get(pattern);
        List<String> keys = new ArrayList<>();

        // Go through the entries in the design pattern map
        for(Map.Entry<String, Object> entry: designMap.entrySet()){

            // Check if object is of type list for subclass lists
            if(entry.getValue() instanceof List){

                // First element in list contains header text
                keys.add(entry.getKey());
            }
        }
        return keys;
    }


    private PsiDirectory getDirectory(Project project, String packageName){

        // Get virtual directory files for package
        Query<VirtualFile> files =
                PackageIndex.getInstance(project).getDirsByPackageName(packageName, true);

        // Get the virtual file for the package
        VirtualFile packageFile = files.findFirst();

        // Check that virtual file exists
        if(packageFile != null){

            // Get directory for package
            PsiDirectory directory =
                    PsiManager.getInstance(project).findDirectory(packageFile);
            return directory;
        }
        return null;
    }

    private PsiDirectory addPackage(Project project, String packageName){

        logger.info("ATTEMPTING TO CREATE PACKAGE: " + packageName);

        // Get the project manager
        ProjectRootManager manager = ProjectRootManager.getInstance(project);

        // Get all the source roots in the project
        VirtualFile[] sourceRoots = manager.getContentSourceRoots();

        // Get the module for the first source root found
        Module module =
                ModuleUtil.findModuleForFile(sourceRoots[0], project);

        // Get the psi manager
        PsiManager psiManager = PsiManager.getInstance(project);

        // Find the psi directory for selected source root
        PsiDirectory directory = psiManager.findDirectory(sourceRoots[0]);

        // Ask user to add package and resolve conflicts
        return com.intellij.ide.util.PackageUtil
                .findOrCreateDirectoryForPackage(module, packageName, directory, true);
    }


    private boolean isNameClash(String packageName, String name, Project project){

        // Get the psi package for this project
        PsiPackage psiPackage =
                JavaPsiFacade.getInstance(project).findPackage(packageName);

        // Check if package exists
        if(psiPackage == null){
            return false;
        }

        // Get all the of the classes inside of chosen package
        PsiClass[] psiClasses = psiPackage.getClasses();

        // Check for name clash against all classes in package
        for(PsiClass psiClass: psiClasses){

            // Check if names are the same
            if(name.equals(psiClass.getName())){
                return true;
            }
        }
        return false;
    }

    private boolean isValidClassName(String packageName, String name){

        // Check that name is not a keyword and is a valid class name
        return !SourceVersion.isKeyword(name)
                    && SourceVersion.isName(name)
                        && !isNameClash(packageName, name, getActiveProject());
    }


    private boolean validateTable(String packageName, JTable table, Set<String> classNames){

        // Get all user input from table
        List<String> values = getValues(table);

        // No user input in table (depends on pattern, check that enabled first)
        if(values.size() == 0){
            return false;
        }

        // Check that each name is table is valid
        for(String name: values){
            if(!isValidClassName(packageName, name)){

                // Log error to console
                logger.error("INVALID CLASS NAME IN TABLE: "+name);

                // Send error notification to user
                GeneratorErrorsNotifier notifier = new GeneratorErrorsNotifier();
                notifier.notify(getActiveProject(), "Invalid class name in table: "+name);

                return false;
            }

            // Check if class name already exists
            if(classNames.contains(name)){

                // Log error to console
                logger.error("DUPLICATE CLASS NAME: " + name);

                // Send error notification to user
                GeneratorErrorsNotifier notifier = new GeneratorErrorsNotifier();
                notifier.notify(getActiveProject(), "Duplicate class name: "+name);
                return false;
            }

            // Add class name to collection
            classNames.add(name);
        }
        return true;
    }


    private boolean validateFields(String pattern){

        // Set to store all class names
        Set<String> classNames = new HashSet<>();

        // Get the number of fields
        int numFields = getFieldKeys(pattern).size();

        String packageName = packageField.getText();

        // Check that each field is valid
        for(int i = 0; i < numFields; i++){
            String name = fields[i].getText();

            // Check that name is a valid class name
            if(name.equals("") || !isValidClassName(packageName, name)){

                // Log error to console
                logger.error("INVALID CLASS NAME: "+name);

                // Send error notification to user
                GeneratorErrorsNotifier notifier = new GeneratorErrorsNotifier();
                notifier.notify(getActiveProject(), "Invalid class name: "+name);
                return false;
            }

            // Check if class name already exists
            if(classNames.contains(name)){

                // Log error to console
                logger.error("DUPLICATE CLASS NAME: " + name);

                // Send error notification to user
                GeneratorErrorsNotifier notifier = new GeneratorErrorsNotifier();
                notifier.notify(getActiveProject(), "Duplicate class name: "+name);
                return false;
            }

            // Add class name to set
            classNames.add(name);
        }
        if(table1.isEnabled()){

            // Check that table 1 has valid input
            if(!validateTable(packageName, table1, classNames)){
                return false;
            }
        }
        if(table2.isEnabled()){

            // Check that table 2 has valid input
            if(!validateTable(packageName, table2, classNames)){
                return false;
            }
        }
        return true;
    }


    private List<String> getValues(JTable table){

        // Get model from table
        TableModel model = table.getModel();
        List<String> values = new ArrayList<>();

        // Get values from table 1
        for(int i = 0; i < model.getRowCount(); i++){
            Object object = model.getValueAt(i, 0);

            // Check if table entry has input
            if (object != null){
                String value = (String)object;

                // Check for valid user input
                if(!value.equals("")){
                    values.add(value);
                }
            }
        }
        return values;
    }


    private Map<String, Object> readFields(String pattern){

        if(validateFields(pattern)) {

            // Copy original design pattern map to update fields
            Map<String, Object> designMap = new TreeMap<>();
            designMap.putAll(designMaps.get(pattern));

            // Get the keys used for fields for given design pattern
            List<String> tableKeys = getTableKeys(pattern);

            // Get the keys used for table headers for given design pattern
            List<String> fieldKeys = getFieldKeys(pattern);

            // Update design pattern map to user input
            for(int i = 0; i < fields.length && i < fieldKeys.size(); i++){
                designMap.put(fieldKeys.get(i), fields[i].getText());
            }

            // Check if table 1 is enabled before reading table input
            if(table1.isEnabled()){
                List<String> values = getValues(table1);
                if(tableKeys.size() > 0){

                    // Set values from given design map (usually subclasses)
                    designMap.put(tableKeys.get(0), values);
                }
            }

            // Check if table 2 is enabled before reading input
            if(table2.isEnabled()){
                List<String> values = getValues(table2);
                if(tableKeys.size() > 1){
                    designMap.put(tableKeys.get(1), values);
                }
            }
            return designMap;
        }
        return null;
    }


    private void updateFields(String pattern){

        // Clear user input from package field
        ((TextField)packageField).setDefaultText("Package:");

        // Get design map for given pattern
        Map<String, Object> designMap = designMaps.get(pattern);

        // Create variables for field and header texts
        List<String> fieldTexts = new ArrayList<>();
        List<String> headerTexts = new ArrayList<>();

        // Go through the entries in the design pattern map
        for(Map.Entry<String, Object> entry: designMap.entrySet()){

            // Check if object is of type list for subclass lists
            if(entry.getValue() instanceof List){
                List<String> values = (List<String>)entry.getValue();

                // First element in list contains header text
                headerTexts.add(values.get(0));
            }
            // Exclude design pattern value from being added to fields
            else if(!entry.getKey().equals("design-pattern")){
                fieldTexts.add((String)entry.getValue());
            }
        }

        int i = 0;
        for(;i < fields.length && i < fieldTexts.size(); i++){

            // Set default text for field and update visibility
            fields[i].setVisible(true);
            TextField f = (TextField)fields[i];
            f.setDefaultText(fieldTexts.get(i));
        }

        // Set remaining blank fields as invisible
        for(; i < 4; i++){
            fields[i].setVisible(false);
            fields[i].setText("");
        }

        // Update tool tips for pattern
        updateToolTips(pattern);

        // Update table headers
        updateTables(headerTexts);

        contentPane.revalidate();
        contentPane.repaint();
    }


    private void createUIComponents() {

        patternComboBox = new ComboBox();

        // Set the dimensions of table 1
        DefaultTableModel model1 = new DefaultTableModel(3, 1);
        table1 = new JBTable(model1);

        // Set the dimensions of table 2
        DefaultTableModel model2 = new DefaultTableModel(3, 1);
        table2 = new JBTable(model2);

        // Create the text fields for user input
        classTextField1 = new TextField();
        classTextField2 = new TextField();
        classTextField3 = new TextField();
        classTextField4 = new TextField();

        // Create text field for packages
        packageField = new TextField("Package: ");
    }


    public JPanel getContent() {
        return contentPane;
    }


    private void $$$setupUI$$$() {
        createUIComponents();
    }
}
