import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by Johannes on 22.09.2015.
 */
public class layerSelectedProperty extends BooleanProperty {

    private boolean isSelected;

    public layerSelectedProperty()
    {
        this.isSelected = false;
    }

    @Override
    public boolean get() {
        return isSelected;
    }

    @Override
    public void set(boolean value) {
        isSelected = value;
    }

    @Override
    public void bind(ObservableValue<? extends Boolean> observable) {

    }

    @Override
    public void unbind() {

    }

    @Override
    public boolean isBound() {
        return false;
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void addListener(ChangeListener<? super Boolean> listener) {

    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> listener) {

    }

    @Override
    public void addListener(InvalidationListener listener) {

    }

    @Override
    public void removeListener(InvalidationListener listener) {

    }
}
