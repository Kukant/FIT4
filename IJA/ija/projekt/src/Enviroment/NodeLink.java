package Enviroment;

import java.awt.*;
import java.io.IOException;
import java.util.UUID;

import Others.Debugger;
import Others.Output;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.CubicCurve;

public class NodeLink extends AnchorPane {

    @FXML CubicCurve node_link;

    private final DoubleProperty mControlOffsetX = new SimpleDoubleProperty();
    private final DoubleProperty mControlOffsetY = new SimpleDoubleProperty();
    private final DoubleProperty mControlDirectionX1 = new SimpleDoubleProperty();
    private final DoubleProperty mControlDirectionY1 = new SimpleDoubleProperty();
    private final DoubleProperty mControlDirectionX2 = new SimpleDoubleProperty();
    private final DoubleProperty mControlDirectionY2 = new SimpleDoubleProperty();

    public NodeLink() {

        setId(UUID.randomUUID().toString());

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("./../Resources/NodeLink.fxml")
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
    private void initialize() {

        mControlOffsetX.set(100.0);
        mControlOffsetY.set(50.0);

        mControlDirectionX1.bind(new When(
                node_link.startXProperty().greaterThan(node_link.endXProperty()))
                .then(-1.0).otherwise(1.0));

        mControlDirectionX2.bind(new When (
                node_link.startXProperty().greaterThan(node_link.endXProperty()))
                .then(1.0).otherwise(-1.0));


        node_link.controlX1Property().bind(
                Bindings.add(
                        node_link.startXProperty(),
                        mControlOffsetX.multiply(mControlDirectionX1)
                )
        );

        node_link.controlX2Property().bind(
                Bindings.add(
                        node_link.endXProperty(),
                        mControlOffsetX.multiply(mControlDirectionX2)
                )
        );

        node_link.controlY1Property().bind(
                Bindings.add(
                        node_link.startYProperty(),
                        mControlOffsetY.multiply(mControlDirectionY1)
                )
        );

        node_link.controlY2Property().bind(
                Bindings.add(
                        node_link.endYProperty(),
                        mControlOffsetY.multiply(mControlDirectionY2)
                )
        );
    }

    public void setStart(Point2D startPoint) {

        node_link.setStartX(startPoint.getX());
        node_link.setStartY(startPoint.getY());
    }

    public void setEnd(Point2D endPoint) {

        node_link.setEndX(endPoint.getX());
        node_link.setEndY(endPoint.getY());
    }

    public void bindEnds (DraggableNode source, DraggableNode target, double mousePosY) {

        int index = 0; // deaultni hodnota, pokud je jenom jeden vstup ma index 0

        if (target.inputsNumber == 2){
            if ((target.getLayoutY() + 100 / 2 /*heigh of draggable block*/ + 39 /*height of menu*/ ) < mousePosY){ // jedna se o spodni (druhy vstup)
               index = 1;
            }
        }

        int ret1 = source.block.BindOutput(target.block,index);
        int ret2 = target.block.BindInput(source.block,index);

        if (ret1 != 0 || ret2 != 0) {
            // probably trying to bind already binded output
            return;
        }

        double targetWidth = target.localToScene(target.getBoundsInLocal()).getWidth();
        double targetHeight = target.localToScene(target.getBoundsInLocal()).getHeight();

        double sourceWidth = source.localToScene(source.getBoundsInLocal()).getWidth() - 9;
        double sourceHeight = source.localToScene(source.getBoundsInLocal()).getHeight();
        
        node_link.startXProperty().bind( Bindings.add(source.layoutXProperty(), sourceWidth));

        node_link.startYProperty().bind( Bindings.add(source.layoutYProperty(), (sourceHeight/ 2.0) + 5));

        if (target.inputsNumber == 1) {
            node_link.endXProperty().bind( Bindings.add(target.layoutXProperty(), 0));
            node_link.endYProperty().bind( Bindings.add(target.layoutYProperty(), targetHeight / 2.0));

        } else if (target.inputsNumber == 2){ // two inputs
            if ((target.getLayoutY() + 100 / 2 /*heigh of draggable block*/ + 39 /*height of menu*/ ) < mousePosY) {
                node_link.endYProperty().bind( Bindings.add(target.layoutYProperty().add(15), (targetHeight / 2.0) ));
            } else {
                node_link.endYProperty().bind( Bindings.add(target.layoutYProperty().add(-15), (targetHeight / 2.0) ));
            }

            node_link.endXProperty().bind( Bindings.add(target.layoutXProperty(), 0));
        } else {
            throw new Error("Unexpected inputs number: " + target.inputsNumber);
        }


        source.registerLink (getId());
        target.registerLink (getId());

    }
}
