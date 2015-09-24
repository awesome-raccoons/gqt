import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by Johannes on 22.09.2015.
 */
public final class LayerSelectedProperty extends BooleanProperty {

    private boolean isSelected;

    public LayerSelectedProperty() {
        this.isSelected = false;
    }

    @Override
    public boolean get() {
        return isSelected;
    }

    @Override
    public void set(final boolean value) {
        isSelected = value;
    }

    @Override
    public void bind(final ObservableValue<? extends Boolean> observable) {

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
    public void addListener(final ChangeListener<? super Boolean> listener) {

    }

    @Override
    public void removeListener(final ChangeListener<? super Boolean> listener) {

    }

    @Override
    public void addListener(final InvalidationListener listener) {

    }

    @Override
    public void removeListener(final InvalidationListener listener) {

    }
}
