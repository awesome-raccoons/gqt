import javafx.scene.control.TextField;

/**
 * Created by Johannes on 15.10.2015.
 */
public class NumberTextField extends TextField {

    public NumberTextField(final int defaultValue) {
        this.setText(Integer.toString(defaultValue));
    }

    @Override
    public final void replaceText(final int start,
                                  final int end,
                                  final String text) {
        if (validate(text))  {
            super.replaceText(start, end, text);
        }

    }

    @Override
    public final void replaceSelection(final String text) {
        if (validate(text)) {
            super.replaceSelection(text);
        }
    }

    private boolean validate(final String text) {
        return ("".equals(text) || text.matches("[0-9]"));
    }

    public final int getValue() {
        String val = this.getText();
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }

    }
}
