import com.github.javaparser.utils.ProjectRoot;
import com.google.gson.internal.$Gson$Preconditions;
import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.typesafe.config.*;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class GeneratorToolWindow{


    private JPanel contentPane;
    private JComboBox patternComboBox;

    private JTextField classTextField1;
    private JTextField classTextField4;
    private JTextField classTextField2;
    private JTextField classTextField3;

    private JList list1;
    private JTable table1;
    private JTable table2;
    private JButton addButton1;
    private JButton addButton2;
    private JButton generateButton;
    private JPanel tablesPanel;
    private JScrollPane table1ScrollPane;
    private JScrollPane table2ScrollPane;
    private Map<String, Map<String, Object>> designMaps;
    private JTextField[] fields;

    private int flip = 1;

    private static Logger logger = LoggerFactory.getLogger(GeneratorToolWindow.class);

    public GeneratorToolWindow(ToolWindow toolWindow) {
        $$$setupUI$$$();
        // load values from config file

        table1ScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        table2ScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        table1.getTableHeader().setForeground(Color.LIGHT_GRAY);
        table2.getTableHeader().setForeground(Color.LIGHT_GRAY);

        fields = new JTextField[4];
        fields[0] = classTextField1;
        fields[1] = classTextField2;
        fields[2] = classTextField3;
        fields[3] = classTextField4;





        logger.info("CREATING COMPONENT");

        patternComboBox.addActionListener(e -> {
            if(patternComboBox.getSelectedIndex() != -1){
                String pattern = (String) patternComboBox.getSelectedItem();
                // Update fields in plugin based on design pattern selected
                updateFields(pattern);
            }
        });


        generateButton.addActionListener(e -> {
            if(patternComboBox.getSelectedIndex() != -1){
                String pattern = (String) patternComboBox.getSelectedItem();


                // Read the fields to update the design pattern
                Map<String, Object> designMap = readFields(pattern);
                // Check if design map was built from user input
                if(designMap == null){
                    return;
                }
                // Create config from design map of respective pattern
                Config config = ConfigFactory.parseMap(designMap);
                // Create design pattern generator based on config
                DesignPatternGenerator generator = DesignPatternGenFactory.create(config);
                if(generator != null){
                    // Get the currently open project
                    Project project = getActiveProject();
                    if(project != null){
                        // Get the path to generate file
                        String path = getSourcePath(project);
                        // Generate files at given path
                        generator.generate(path);
                    }
                }
            }
        });

        try {
            InputStream inStream = GeneratorToolWindow.class.getResourceAsStream("design-patterns.conf");
            if (inStream != null) {
                Reader reader = new InputStreamReader(inStream);

                // Load the configuration file
                Config config = ConfigFactory.parseReader(reader);

                // Update values in config object
                config = config.withValue("conf.path", ConfigValueFactory.fromAnyRef("deeze nuts"));

                // Get the list of configs for each design pattern
                List<? extends Config> configs = config.getConfigList("design-patterns");

                // Create tree map to store maps for each design pattern
                designMaps = new TreeMap<>();

                // Convert design pattern configs into maps
                for(Config designConfig: configs){
                    String pattern = designConfig.getString("design-pattern");
                    Map<String, Object> designMap = new TreeMap<>();
                    // Add key-value pairs from config to map
                    designConfig.entrySet()
                            .forEach(e -> {designMap.put(e.getKey(), e.getValue().unwrapped());});
                    designMaps.put(pattern, designMap);
                }
                inStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void updateToolTips(String pattern){

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

    private void updateTables(List<String> headerTexts){

        table1.getTableHeader().setForeground(Color.LIGHT_GRAY);
        table2.getTableHeader().setForeground(Color.LIGHT_GRAY);

        // Set header texts to blank by default
        String header1Text = "";
        String header2Text = "";
        // Set tables disabled by default
        table1.setEnabled(false);
        table2.setEnabled(false);
        // Enable table 1 and set table 1 header text
        if(headerTexts.size() >= 1){
            header1Text = headerTexts.get(0);
            table1.setEnabled(true);
        }
        // Enable table 2 and set table 2 header text
        if(headerTexts.size() >= 2){
            header2Text = headerTexts.get(1);
            table2.setEnabled(true);
        }
        // Set the header for table 1
        JTableHeader header = table1.getTableHeader();
        TableColumnModel model = header.getColumnModel();
        model.getColumn(0).setHeaderValue(header1Text);;
        // Set the header for table 2
        header = table2.getTableHeader();
        model = header.getColumnModel();
        model.getColumn(0).setHeaderValue(header2Text);

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
                List<String> values = (List<String>)entry.getValue();
                // First element in list contains header text
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    private boolean isValidClassName(String name){
        // Check that name is not a keyword and is a valid class name
        return !SourceVersion.isKeyword(name) && SourceVersion.isName(name);
    }

    private boolean validateTable(JTable table){
        // Get all user input from table
        List<String> values = getValues(table);
        // No user input in table (depends on pattern, check that enabled first)
        if(values.size() == 0){
            return false;
        }
        // Check that each name is table is valid
        for(String name: values){
            if(!isValidClassName(name)){
                logger.error("INVALID CLASS NAME IN TABLE: "+name);
                return false;
            }
        }
        return true;
    }

    private boolean validateFields(String pattern){

        // Get the number of fields, -1 to exclude design pattern key
        int numFields = getFieldKeys(pattern).size() - 1;
        // Check that each field is valid
        for(int i = 0; i < numFields; i++){
            String name = fields[i].getText();
            // Check that name is a valid class name
            if(name.equals("") || !isValidClassName(name)){
                logger.error("INVALID CLASS NAME: "+name);
                return false;
            }
        }
        if(table1.isEnabled()){
            // Check that table 1 has valid input
            if(!validateTable(table1)){
                return false;
            }
        }
        if(table2.isEnabled()){
            // Check that table 2 has valid input
            if(!validateTable(table2)){
                return false;
            }
        }
        return true;
    }

    private List<String> getValues(JTable table){
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
            // Remove design pattern key from list
            fieldKeys.remove("design-pattern");
            // Update design pattern map to user input
            for(int i = 0; i < fields.length && i < fieldKeys.size(); i++){
                designMap.put(fieldKeys.get(i), fields[i].getText());
            }
            // Check if table 1 is enabled before reading table input
            if(table1.isEnabled()){
                List<String> values = getValues(table1);
                if(tableKeys.size() > 0){
                    System.err.println(tableKeys);
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
        // Set field texts for a max of 4 fields
        for(;i < 4 && i < fieldTexts.size(); i++){
            fields[i].setVisible(true);
            TextField f = (TextField)fields[i];
            f.setDefaultText(fieldTexts.get(i));

            //fields[i].setText(fieldTexts.get(i));
            fields[i].setForeground(Color.DARK_GRAY);
        }
        // Set remaining blank fields as invisible
        for(; i < 4; i++){
            fields[i].setVisible(false);
            fields[i].setText("");
        }
        // Update table headers
        updateTables(headerTexts);

        contentPane.revalidate();
        contentPane.repaint();
    }

    private void createUIComponents() {

        String[] patterns = {"Abstract Factory", "Factory Method", "Builder", "Chain"};
        patternComboBox = new ComboBox(patterns);


        DefaultListModel<String> listModel1 = new DefaultListModel<>();
        listModel1.addElement("hello");
        listModel1.addElement("how");
        listModel1.addElement("are");

        list1 = new JBList(listModel1);

        DefaultTableModel model1 = new DefaultTableModel(3, 1);
        model1.setColumnIdentifiers(new String[]{"ProductA"});

        table1 = new JBTable(model1);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
        tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table1.getColumn("ProductA").setHeaderRenderer(tableCellRenderer);

        DefaultTableModel model2 = new DefaultTableModel(3, 1);
        model2.setColumnIdentifiers(new String[]{"ProductB"});

        table2 = new JBTable(model2);
        table2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCellRenderer = new DefaultTableCellRenderer();
        tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table2.getColumn("ProductB").setHeaderRenderer(tableCellRenderer);



        //table2.setVisible(false);
        //table2.setEnabled(false);

        classTextField1 = new TextField();
        classTextField2 = new TextField();
        classTextField3 = new TextField();
        classTextField4 = new TextField();

    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.setEnabled(true);
        contentPane.setMinimumSize(new Dimension(318, 150));
        contentPane.setPreferredSize(new Dimension(924, 150));
        contentPane.add(patternComboBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        classTextField1.setForeground(new Color(-9211021));
        classTextField1.setName("");
        classTextField1.setText("");
        classTextField1.setToolTipText("Hello");
        contentPane.add(classTextField1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        classTextField2.setText("");
        contentPane.add(classTextField2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        classTextField3.setText("");
        classTextField3.setVisible(true);
        contentPane.add(classTextField3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        classTextField4.setText("");
        contentPane.add(classTextField4, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        addButton1 = new JButton();
        addButton1.setText("Button");
        panel1.add(addButton1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton2 = new JButton();
        addButton2.setText("Button");
        panel1.add(addButton2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(table1);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add(scrollPane2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setViewportView(table2);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public JPanel getContent() {
        return contentPane;
    }
}
