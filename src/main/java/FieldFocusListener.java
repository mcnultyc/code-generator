import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class FieldFocusListener implements FocusListener {

    private String defaultText;
    private JTextField textField;


    public FieldFocusListener(JTextField textField, String defaultText){
        this.textField = textField;
        this.defaultText = defaultText;

        textField.setText(defaultText);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(textField.getText().equals(defaultText)){
            textField.setText("");
            textField.setForeground(Color.BLACK);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(textField.getText().equals("")){
            textField.setText(defaultText);
            textField.setForeground(Color.DARK_GRAY);
        }
    }
}
