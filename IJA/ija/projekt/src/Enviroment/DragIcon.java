package Enviroment;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;


public class DragIcon extends AnchorPane {

    private BlockIconType mType;
    @FXML public Text operatorTextField;

    public DragIcon() {

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("./../Resources/DragIcon.fxml")
        );

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void initialize() {}

    public BlockIconType getType() { return mType;}

    public void setType(BlockIconType type) {

        mType = type;

        getStyleClass().clear();
        getStyleClass().add("dragicon");
        switch (mType) {

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

    public void relocateToPoint (Point2D p) {

        Point2D localCoords = getParent().sceneToLocal(p);

        relocate (
                (int) (localCoords.getX() -
                        (getBoundsInLocal().getWidth() / 2)),
                (int) (localCoords.getY() -
                        (getBoundsInLocal().getHeight() / 2))
        );
    }
}
