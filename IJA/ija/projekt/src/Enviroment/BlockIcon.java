package Enviroment;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

/**
 * Representation of Graphical icon in side panel of application.
 */
public class BlockIcon extends AnchorPane {

    private BlockIconType TypeOfIcon;
    @FXML public Text operatorTextField;

    /**
     * Initialize new Icon from FXML file.
     */
    public BlockIcon() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("Resources/BlockIcon.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) { throw new RuntimeException(exception);}
    }

    @FXML private void initialize() {}

    /**
     * @return type of icon
     */
    public BlockIconType getType() { return TypeOfIcon;}

    /**
     * Set type of Icon.
     * @param type one of predefined types
     */
    public void setType(BlockIconType type) {

        TypeOfIcon = type;

        getStyleClass().clear();
        getStyleClass().add("dragicon");
        switch (TypeOfIcon) {

            case add:
                operatorTextField.setText("+");
                break;
            case sub:
                operatorTextField.setText("-");
                break;
            case mul:
                operatorTextField.setText("\u00D7");
                break;
            case div:
                operatorTextField.setText("/");
                break;
            case root:
                operatorTextField.setText("\u221A");
                break;
            case pow:
                operatorTextField.setText("^");
                break;
            case _const:
                operatorTextField.setText("N");
                break;
            case result:
                operatorTextField.setText("=");
                break;
            default:
                break;
        }
    }

    /**
     * Relocate Position of Icon.
     * @param p new position
     */
    public void ChangePosition(Point2D p) {

        Point2D FetchedCoords = getParent().sceneToLocal(p);

        relocate (
                (int) (FetchedCoords.getX() -
                        (getBoundsInLocal().getWidth() / 2)),
                (int) (FetchedCoords.getY() -
                        (getBoundsInLocal().getHeight() / 2))
        );
    }
}
