package Enviroment;

import java.io.IOException;
import java.util.UUID;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.CubicCurve;

/**
 * Graphical representation of Connection.
 */
public class Connection extends AnchorPane {

    @FXML CubicCurve node_link;

    private final DoubleProperty XOffset = new SimpleDoubleProperty();
    private final DoubleProperty YOffset = new SimpleDoubleProperty();
    private final DoubleProperty X1DirectionModifier = new SimpleDoubleProperty();
    private final DoubleProperty Y1DirectionModifier = new SimpleDoubleProperty();
    private final DoubleProperty X2DirectionModifier = new SimpleDoubleProperty();
    private final DoubleProperty Y2DirectionModifier = new SimpleDoubleProperty();

    /**
     * Loads Connection graphic resources from FXML file. Set Unique ID of connection.
     */
    public Connection() {

        setId(UUID.randomUUID().toString());

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../Resources/Connection.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML private void initialize() {

        XOffset.set(100.0);
        YOffset.set(50.0);

        X1DirectionModifier.bind(new When(node_link.startXProperty().greaterThan(node_link.endXProperty())).then(-1.0).otherwise(1.0));

        X2DirectionModifier.bind(new When (node_link.startXProperty().greaterThan(node_link.endXProperty())).then(1.0).otherwise(-1.0));


        node_link.controlX1Property().bind(Bindings.add(node_link.startXProperty(), XOffset.multiply(X1DirectionModifier)));

        node_link.controlX2Property().bind(Bindings.add(node_link.endXProperty(), XOffset.multiply(X2DirectionModifier)));

        node_link.controlY1Property().bind(Bindings.add(node_link.startYProperty(), YOffset.multiply(Y1DirectionModifier)));

        node_link.controlY2Property().bind(Bindings.add(node_link.endYProperty(), YOffset.multiply(Y2DirectionModifier)));
    }

    /**
     * Set Start position of connection.
     * @param p start point
     */
    public void SetStartPosition(Point2D p) {

        node_link.setStartX(p.getX());
        node_link.setStartY(p.getY());
    }

    /**
     * Set End position of connection.
     * @param p end point
     */
    public void SetEndPosition(Point2D p) {

        node_link.setEndX(p.getX());
        node_link.setEndY(p.getY());
    }

    /**
     * Connect two blocks. Into blocks is saved information about this connection and other block.
     *
     * @param source source block
     * @param target target block
     * @param mouseY Vertical position of cursor
     * @param bindable bool information if is it possible to bind
     */
    public void ConnectBlocks(MovableBlock source, MovableBlock target, double mouseY, boolean bindable) {

        int index = 0; // deaultni hodnota, pokud je jenom jeden vstup ma index 0

        if (target.NumberOfInputPorts == 2){
            if ((target.getLayoutY() + 100 / 2 /*heigh of draggable block*/ + 39 /*height of menu*/ ) < mouseY){ // jedna se o spodni (druhy vstup)
               index = 1;
            }
        }
        if (bindable){
            int ret1 = source.block.BindOutput(target.block,index);
            int ret2 = target.block.BindInput(source.block,index);

            if (ret1 != 0 || ret2 != 0) {
                // probably trying to bind already binded output
                return;
            }
        }

        double targetWidth = target.localToScene(target.getBoundsInLocal()).getWidth();
        double targetHeight = target.localToScene(target.getBoundsInLocal()).getHeight();

        double sourceWidth = source.localToScene(source.getBoundsInLocal()).getWidth() - 9;
        double sourceHeight = source.localToScene(source.getBoundsInLocal()).getHeight();
        
        node_link.startXProperty().bind( Bindings.add(source.layoutXProperty(), sourceWidth));

        node_link.startYProperty().bind( Bindings.add(source.layoutYProperty(), (sourceHeight/ 2.0) + 5));

        if (target.NumberOfInputPorts == 1) {
            node_link.endXProperty().bind( Bindings.add(target.layoutXProperty(), 0));
            node_link.endYProperty().bind( Bindings.add(target.layoutYProperty(), targetHeight / 2.0));

        } else if (target.NumberOfInputPorts == 2){ // two inputs
            if ((target.getLayoutY() + 100 / 2 /*heigh of draggable block*/ + 39 /*height of menu*/ ) < mouseY) {
                node_link.endYProperty().bind( Bindings.add(target.layoutYProperty().add(15), (targetHeight / 2.0) ));
            } else {
                node_link.endYProperty().bind( Bindings.add(target.layoutYProperty().add(-15), (targetHeight / 2.0) ));
            }

            node_link.endXProperty().bind( Bindings.add(target.layoutXProperty(), 0));
        } else {
            throw new Error("Unexpected inputs number: " + target.NumberOfInputPorts);
        }


        source.registerConnection(getId());
        target.registerConnection(getId());

    }
}
