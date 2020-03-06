import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class GeneratorToolWindow implements ActionListener {


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


    private static Logger logger = LoggerFactory.getLogger(GeneratorToolWindow.class);

    public GeneratorToolWindow(ToolWindow toolWindow) {
        $$$setupUI$$$();
        // load values from config file

        classTextField1.setText("Factory");
        classTextField1.setForeground(new Color(-9211021));
        classTextField2.setText("ConcreteFactory");
        classTextField2.setForeground(new Color(-9211021));
        classTextField3.setText("ProductB");
        classTextField3.setForeground(new Color(-9211021));
        classTextField4.setText("ProductA");
        classTextField4.setForeground(new Color(-9211021));

        logger.info("CREATING COMPONENT");
        generateButton.addActionListener(e -> {
            if(patternComboBox.getSelectedIndex() != -1){
                String pattern = (String) patternComboBox.getSelectedItem();

            }
        });

        try {
            InputStream inStream = GeneratorToolWindow.class.getResourceAsStream("design-patterns.conf");
            if (inStream != null) {
                Reader reader = new InputStreamReader(inStream);

                // Load the configuration file
                Config config = ConfigFactory.parseReader(reader);



                // Load the destination directory for designs being generated
                String directory = config.getString("conf.path");



                logger.info("DIRECTORY:"+directory);
                inStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void createUIComponents() {

        String[] patterns = {"Abstract Factory", "Factory Method", "Builder", "Chain"};
        patternComboBox = new ComboBox(patterns);
        patternComboBox.addActionListener(this);

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

        classTextField1 = new JTextField();
        classTextField2 = new JTextField();
        classTextField3 = new JTextField();
        classTextField4 = new JTextField();

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (patternComboBox.getSelectedIndex() != -1) {
            String pattern = (String) patternComboBox.getSelectedItem();
            DesignPatternGenerator generator = new FactoryMethodGenerator("Creator","ConcreteCreator", "Product", new String[]{"Product1"});
        }
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
