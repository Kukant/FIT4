package Enviroment;

import java.io.IOException;
import java.util.*;

import Blocks.*;
import Others.Output;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

/**
 * Representation of graphical block.
 */
public class MovableBlock extends AnchorPane {

    @FXML AnchorPane root_pane;

    private final List AttachedConnections = new ArrayList (); //TODO :pole inputs a outputs

    public int NumberOfInputPorts;
    Block block;

    private EventHandler DragOver, DragDropped;

    private DragIconType TypeOfBlock;

    private Point2D RelocatedPosition = new Point2D(0.0, 0.0);

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

    private EventHandler <MouseEvent> DragDetected;
    private EventHandler <DragEvent> BlockDragDropped, LinkDragOver, LinkDragDropped;

    private final MovableBlock self;

    /**
     * Creates new block.
     *
     * @param type  one predefined block types
     */
    public MovableBlock(DragIconType type) {

        self = this;
        TypeOfBlock = type;
        setId(UUID.randomUUID().toString());
        String blockResourcePath;
        switch (type) {
            case add:
            case mul:
            case div:
            case root:
            case pow:
            case sub:
                blockResourcePath = "./../Resources/MovableBlock.fxml";
                NumberOfInputPorts = 2;
                break;
            case _const:
                blockResourcePath = "./../Resources/MovableBlockConst.fxml";
                NumberOfInputPorts = 0;
                break;
            case result:
                blockResourcePath = "./../Resources/MovableBlockResult.fxml";
                NumberOfInputPorts = 1;
                break;

                default:
                    throw new Error();

        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(blockResourcePath));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) { throw new RuntimeException(exception); }
    }

    /**
     * Initialize empty unattached block.
     */
    @FXML private void initialize() {

        setType();
        BlockDragHandlers();
        ConnectionDragHandlers();
        RegisterOtherHandlers();

        //left_link_handle.setOnDragDetected(DragDetected);
        if (right_link_handle != null)
            right_link_handle.setOnDragDetected(DragDetected);

        if (left_link_handle != null)
            left_link_handle.setOnDragDropped(BlockDragDropped);

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

    /**
     * Returns type of block.
     * @return type of block
     */
    public DragIconType getType() { return TypeOfBlock;}

    public void setType() {

        switch (TypeOfBlock) {
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

    /**
     * Changes position of block in scene.
     * @param NewPosition new position
     */
    public void ChangePosition(Point2D NewPosition) {

        Point2D OriginalPosition = getParent().sceneToLocal(NewPosition);

        relocate ((int) (OriginalPosition.getX() - RelocatedPosition.getX()), (int) (OriginalPosition.getY() - RelocatedPosition.getY()));
    }

    /**
     * Handles Drag events of Blocks. If specific event is caught, his predefined action occurs.
     */
    public void BlockDragHandlers() {

        //move block
        DragOver = new EventHandler <DragEvent>() {

            @Override
            public void handle(DragEvent event) {

                event.acceptTransferModes(TransferMode.ANY);
                ChangePosition(new Point2D( event.getSceneX(), event.getSceneY()));

                event.consume();
            }
        };

        DragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver(null);

                event.setDropCompleted(true);

                event.consume();
            }
        };

        //block destroyed
        close_button.setOnMouseClicked( new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {
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
                            self.block.Inputs[i].UnbindOutput(self.block, i);
                            self.block.UnbindInput(i);
                    }


                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);
                RootLayout rootLayout = (RootLayout) parent.getParent().getParent().getParent();
                rootLayout.scheme.Blocks.remove(self.block);


                for (ListIterator <String> iterId = AttachedConnections.listIterator();
                     iterId.hasNext();) {

                    String nextBlock = iterId.next();

                    for (ListIterator <Node> iterBlock = parent.getChildren().listIterator();
                         iterBlock.hasNext();) {

                        Node node = iterBlock.next();

                        if (node.getId() == null)
                            continue;

                        if (node.getId().equals(nextBlock))
                            iterBlock.remove();
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

                getParent().setOnDragOver (DragOver);
                getParent().setOnDragDropped (DragDropped);

                //begin drag ops
                RelocatedPosition = new Point2D(event.getX(), event.getY());

                ChangePosition(new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent data = new ClipboardContent();
                DataHolder dataContainer = new DataHolder();

                dataContainer.importData("type", TypeOfBlock.toString());
                data.put(DataHolder.BlockAdded, dataContainer);

                startDragAndDrop (TransferMode.ANY).setContent(data);

                event.consume();
            }

        });
    }

    /**
     * Handles Drag events of Connections. If specific event is caught, his predefined action occurs.
     */
    private void ConnectionDragHandlers() {

        DragDetected = new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {
                
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver(null);
                

                getParent().setOnDragOver(LinkDragOver);
                getParent().setOnDragDropped(LinkDragDropped);

                //Set up user-draggable link
                right_pane.getChildren().add(0,mDragLink);

                mDragLink.setVisible(false);

                Point2D StartPosition = new Point2D(getLayoutX() + getWidth(), getLayoutY() + (getHeight() / 2.0));

                mDragLink.setStart(StartPosition);

                //Drag data code
                ClipboardContent data = new ClipboardContent();
                DataHolder dataContainer = new DataHolder();

                //pass the UUID of the source node for later lookup
                dataContainer.importData("source", getId());

                data.put(DataHolder.ConnectionAdded, dataContainer);

                startDragAndDrop (TransferMode.ANY).setContent(data);

                event.consume();
            }
        };

        BlockDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                //get the drag data.  If it's null, abort.
                //This isn't the drag event we're looking for.
                DataHolder dataContainer = (DataHolder) event.getDragboard().getContent(DataHolder.ConnectionAdded);

                if (dataContainer == null)
                    return;

                mDragLink.setVisible(false);
                right_pane.getChildren().remove(0);

                AnchorPane link_handle = (AnchorPane) event.getSource();

                ClipboardContent data = new ClipboardContent();

                //pass the UUID of the target node for later lookup
                dataContainer.importData("target", getId());
                dataContainer.importData("mouse_y", event.getSceneY());
                data.put(DataHolder.ConnectionAdded, dataContainer);

                event.getDragboard().setContent(data);
                event.setDropCompleted(true);
                event.consume();
            }
        };

        LinkDragOver = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.ANY);

                if (!mDragLink.isVisible())
                    mDragLink.setVisible(true);

                Point2D EndPosition = new Point2D(event.getX(), event.getY());

                mDragLink.setEnd(EndPosition);

                event.consume();

            }
        };

        //drop event for connection creation
        LinkDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {
                System.out.println("context connection drag dropped");

                getParent().setOnDragDropped(null);
                getParent().setOnDragOver(null);


                mDragLink.setVisible(false);
                right_pane.getChildren().remove(0);

                event.setDropCompleted(true);
                event.consume();
            }
        };

    }

    /**
     * Handles different events. If specific event is caught, his predefined action occurs.
     */
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

    /**
     * Attach connection to block.
     * @param linkId attached connection
     */
    public void registerLink(String linkId) {
        AttachedConnections.add(linkId);
    }
}

