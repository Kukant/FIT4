package Enviroment;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import Blocks.*;
import Others.Debugger;
import Others.Output;
import com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import static Enviroment.DraggableNodeType.Constant;
import static Enviroment.DraggableNodeType.Result;
import static Enviroment.DraggableNodeType.TwoInputs;


public class DraggableNode extends AnchorPane {

    @FXML AnchorPane root_pane;

    private final List  mLinkIds = new ArrayList (); //TODO :pole inputs a outputs

    public int inputsNumber;
    Block block;

    private EventHandler  mContextDragOver;
    private EventHandler  mContextDragDropped;

    private DragIconType mType = null;

    private Point2D mDragOffset = new Point2D(0.0, 0.0);

    @FXML private Label title_bar;
    @FXML private Label close_button;

    @FXML TextField ConstValue; // textove pole v bloku CONST
    @FXML Text operatorTextField; // znak (operator) v matematickych blocich
    @FXML Text Result; // Vysledna hodnota bloku RESULT
    @FXML Text valueDisplay; // Aktualni hodnota bloku

    @FXML AnchorPane left_link_handle;
    @FXML AnchorPane right_link_handle;

    private NodeLink mDragLink = null;
    private AnchorPane right_pane = null;

    private EventHandler <MouseEvent> mLinkHandleDragDetected;
    private EventHandler <DragEvent> mLinkHandleDragDropped;
    private EventHandler <DragEvent> mContextLinkDragOver;
    private EventHandler <DragEvent>  mContextLinkDragDropped;

    private final DraggableNode self;

    public DraggableNode(DragIconType type) {

        self = this;
        mType = type;
        setId(UUID.randomUUID().toString());
        String blockResourcePath;
        switch (type) {
            case add:
            case mul:
            case div:
            case root:
            case pow:
            case sub:
                blockResourcePath = "./../Resources/DraggableNode.fxml";
                inputsNumber = 2;
                break;
            case _const:
                blockResourcePath = "./../Resources/DraggableNodeConstant.fxml";
                inputsNumber = 0;
                break;
            case result:
                blockResourcePath = "./../Resources/DraggableNodeResult.fxml";
                inputsNumber = 1;
                break;

                default:
                    throw new Error();

        }

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource(blockResourcePath)
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

        setType();
        buildNodeDragHandlers();
        buildLinkDragHandlers();
        RegisterOtherHandlers();

        //left_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        if (right_link_handle != null)
            right_link_handle.setOnDragDetected(mLinkHandleDragDetected);

        if (left_link_handle != null)
            left_link_handle.setOnDragDropped(mLinkHandleDragDropped);
        //right_link_handle.setOnDragDropped(mLinkHandleDragDropped);

        mDragLink = new NodeLink();
        mDragLink.setVisible(false);

        parentProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable,
                                Object oldValue, Object newValue) {
                right_pane = (AnchorPane) getParent();
            }
        });


    }

    public DragIconType getType() { return mType;}

    public void setType() {

        switch (mType) {
            case add:
                operatorTextField.setText("+");
                block = new AddBlock();
                break;
            case sub:
                operatorTextField.setText("-");
                block = new SubBlock();
                break;
            case mul:
                operatorTextField.setText("\u00D7");
                block = new MulBlock();
                break;
            case div:
                operatorTextField.setText("/");
                block = new DivBlock();
                break;
            case root:
                operatorTextField.setText("\u221A");
                block = new RootBlock();
                break;
            case pow:
                operatorTextField.setText("^");
                block = new PowBlock();
                break;
            case _const:
                block = new ConstBlock();
                break;
            case result:
                block = new ResultBlock();
                break;
            default:
                break;
        }
    }

    public void relocateToPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);

        relocate (
                (int) (localCoords.getX() - mDragOffset.getX()),
                (int) (localCoords.getY() - mDragOffset.getY())
        );
    }

    public void buildNodeDragHandlers() {

        mContextDragOver = new EventHandler <DragEvent>() {

            //dragover to handle node dragging in the right pane view
            @Override
            public void handle(DragEvent event) {

                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2D( event.getSceneX(), event.getSceneY()));

                event.consume();
            }
        };

        //dragdrop for node dragging
        mContextDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                event.setDropCompleted(true);

                event.consume();
            }
        };

        //close button click
        close_button.setOnMouseClicked( new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

//                Debugger.log("Outputs before delete: " + self.block.Outputs);
//                Debugger.log("Inputs before delete:");
//                for (Block i : self.block.Inputs) {
//                    if (i != null)
//                        Debugger.log(" " +i);
//                }
                List<Output> toRemove = new ArrayList<>(); // pole outputs ktere budeme mazat

                if (! (self.block instanceof ResultBlock))

                    for (Output o : self.block.Outputs) {
                        if (o != null){
                            toRemove.add(o); //pridame do pole mazanych
                        }
                    }

                    for(Output o: toRemove){ // mazeme vystupy
                        o.block.UnbindInput(o.Index);
                        self.block.UnbindOutput(o.block, o.Index);
                    }



                if (! (self.block instanceof ConstBlock))
                    for (int i = 0; i < self.block.Inputs.length ; i++) {
                        if (self.block.Inputs[i] != null)
                            self.block.Inputs[i].UnbindInput(i);
                            self.block.UnbindOutput(self.block,i);
                    }


//                Debugger.log("Outputs after delete: " + self.block.Outputs);
//                Debugger.log("Inputs before delete:");
//                for (Block i : self.block.Inputs) {
//                    if (i != null)
//                        Debugger.log(" " +i);
//                }

                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);
                RootLayout rootLayout = (RootLayout) parent.getParent().getParent().getParent();
                rootLayout.scheme.Blocks.remove(self.block);


                //iterate each link id connected to this node
                //find it's corresponding component in the right-hand
                //AnchorPane and delete it.

                //Note:  other nodes connected to these links are not being
                //notified that the link has been removed.
                for (ListIterator <String> iterId = mLinkIds.listIterator();
                     iterId.hasNext();) {

                    String id = iterId.next();

                    for (ListIterator <Node> iterNode = parent.getChildren().listIterator();
                         iterNode.hasNext();) {

                        Node node = iterNode.next();

                        if (node.getId() == null)
                            continue;

                        if (node.getId().equals(id))
                            iterNode.remove();
                    }

                    iterId.remove();
                }
            }

        });

        //drag detection for node dragging
        title_bar.setOnDragDetected ( new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                //begin drag ops
                mDragOffset = new Point2D(event.getX(), event.getY());

                relocateToPoint(
                        new Point2D(event.getSceneX(), event.getSceneY())
                );

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", mType.toString());
                content.put(DragContainer.AddNode, container);

                startDragAndDrop (TransferMode.ANY).setContent(content);

                event.consume();
            }

        });
    }

    private void buildLinkDragHandlers() {

        mLinkHandleDragDetected = new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver(mContextLinkDragOver);
                getParent().setOnDragDropped(mContextLinkDragDropped);

                //Set up user-draggable link
                right_pane.getChildren().add(0,mDragLink);

                mDragLink.setVisible(false);

                Point2D p = new Point2D(
                        getLayoutX() + getWidth(),
                        getLayoutY() + (getHeight() / 2.0)
                );

                mDragLink.setStart(p);

                //Drag content code
                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer ();

                //pass the UUID of the source node for later lookup
                container.addData("source", getId());

                content.put(DragContainer.AddLink, container);

                startDragAndDrop (TransferMode.ANY).setContent(content);

                event.consume();
            }
        };

        mLinkHandleDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                //get the drag data.  If it's null, abort.
                //This isn't the drag event we're looking for.
                DragContainer container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

                if (container == null)
                    return;

                //hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
                mDragLink.setVisible(false);
                right_pane.getChildren().remove(0);

                AnchorPane link_handle = (AnchorPane) event.getSource();

                ClipboardContent content = new ClipboardContent();

                //pass the UUID of the target node for later lookup
                container.addData("target", getId());
                container.addData("mouse_y", event.getSceneY());
                content.put(DragContainer.AddLink, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
                event.consume();
            }
        };

        mContextLinkDragOver = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.ANY);

                //Relocate end of user-draggable link
                if (!mDragLink.isVisible())
                    mDragLink.setVisible(true);

                mDragLink.setEnd(new Point2D(event.getX(), event.getY()));

                event.consume();

            }
        };

        //drop event for link creation
        mContextLinkDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {
                System.out.println("context link drag dropped");

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                //hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
                mDragLink.setVisible(false);
                right_pane.getChildren().remove(0);

                event.setDropCompleted(true);
                event.consume();
            }
        };

    }

    public void RegisterOtherHandlers() {
        if (ConstValue != null)
        ConstValue.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    ConstValue.setText(newValue.replaceAll("[^\\d]", ""));
                }

                ConstBlock thisBlock = (ConstBlock) block;
                String textVal = ConstValue.getText();
                if (!textVal.isEmpty()) {
                    thisBlock.setConstVal(Double.parseDouble(textVal));
                }
            }
        });
    }


    public void registerLink(String linkId) {
        mLinkIds.add(linkId);
    }
}

