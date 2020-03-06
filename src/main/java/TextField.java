import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextField extends JTextField {

    private boolean isDefault;
    private String defaultText;

    public TextField(){
        // Create text field with blank default text
        this("");
    }

    public TextField(String defaultText){
        // Text field starts off in default state
        this.isDefault = true;
        this.defaultText = defaultText;

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                String defaultText = getDefaultText();
                // Check if user hasn't had focus and default text still set
                if(isDefault && TextField.super.getText().equals(defaultText)){
                    // User has potentially entered text
                    isDefault = false;
                    // Clear default text
                    setText("");
                    setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Lost focus and no user input
                if(TextField.super.getText().equals("")){
                    // Text field is back in default state
                    isDefault = true;
                    String defaultText = getDefaultText();
                    // Set default text back in place
                    setText(defaultText);
                    setForeground(Color.DARK_GRAY);
                }
            }
        });
    }

    public String getDefaultText(){
        return defaultText;
    }

    public void setDefaultText(String defaultText){
        this.defaultText = defaultText;
        // Set default text
        setText(defaultText);
        setForeground(Color.DARK_GRAY);
        // Setting default text puts field in default state
        isDefault = true;
    }

    @Override
    public String getText() {
        // Default text is just a placeholder
        if(!isDefault){
            // Get user inserted text, might also be ""
            return super.getText();
        }
        return "";
    }
}
