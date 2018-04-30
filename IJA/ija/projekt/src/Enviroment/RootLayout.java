package Enviroment;

import java.io.IOException;

import Blocks.ConstBlock;
import Blocks.ResultBlock;
import Others.Debugger;
import Schemes.Scheme;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class RootLayout extends AnchorPane {

    @FXML SplitPane base_pane;
    @FXML AnchorPane right_pane;
    @FXML VBox left_pane;
    @FXML javafx.scene.control.Button CalculateButton;
    @FXML Button DebugButton;
    @FXML Text WarningLine;

    private EventHandler mIconDragOverRoot=null;
    private EventHandler mIconDragDropped=null;
    private EventHandler mIconDragOverRightPane=null;
    private DragIcon mDragOverIcon = null;

    // main scheme used to store list of all blocks
    public Scheme scheme = new Scheme();

    public RootLayout() {

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("./../Resources/RootLayout.fxml")
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

        mDragOverIcon = new DragIcon(); //specialni neviditelna ikona, je zobrazena jen kdyz "Drag and Dropujeme"

        mDragOverIcon.setVisible(false);//uprava jejich atributu
        mDragOverIcon.setOpacity(0.65);
        getChildren().add(mDragOverIcon);


        for (int i = 0; i < 8; i++) { // zobrazeni osmi ikon z DragIcon.java

            DragIcon icn = new DragIcon();// nova ikona

            addDragDetection(icn); //je dragovatelna

            icn.setType(DragIconType.values()[i]); //nastaven typ (barva)
            left_pane.getChildren().add(icn);// pridani ikony na levou stanu okna (do Vboxu)
        }

        buildDragHandlers();
        buildButtonHandlers();
    }


    private void buildDragHandlers() {

        //drag over transition to move widget form left pane to right pane
        mIconDragOverRoot = new EventHandler <DragEvent>() {

            @Override
            public void handle(DragEvent event) {

                javafx.geometry.Point2D p = right_pane.sceneToLocal(event.getSceneX(), event.getSceneY());

                if (!right_pane.boundsInLocalProperty().get().contains(p)) {
                    mDragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                    return;
                }

                event.consume();
            }
        };

        mIconDragOverRightPane = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                event.acceptTransferModes(TransferMode.ANY);

                mDragOverIcon.relocateToPoint(
                        new Point2D(event.getSceneX(), event.getSceneY())
                );


                event.consume();
            }
        };

        mIconDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                DragContainer container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

                container.addData("scene_coords", new Point2D(event.getSceneX(), event.getSceneY()));
                container.addData("block_type", mDragOverIcon.operatorTextField.getText());

                ClipboardContent content = new ClipboardContent();
                content.put(DragContainer.AddNode, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
            }
        };


        this.setOnDragDone (new EventHandler <DragEvent> (){

            @Override
            public void handle (DragEvent event) {

                right_pane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRightPane);
                right_pane.removeEventHandler(DragEvent.DRAG_DROPPED, mIconDragDropped);
                base_pane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRoot);

                mDragOverIcon.setVisible(false);

                DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

                if (container != null) {
                    if (container.getValue("scene_coords") != null) {

                        DraggableNodeType draggableNodeType;
                        String blockType = container.getValue("block_type");
                        Debugger.log("block type: " + blockType);
                        switch (blockType) {
                            case "-":
                            case "\u00D7":
                            case "/":
                            case "\u221A":
                            case "+":
                            case "^":
                                draggableNodeType = DraggableNodeType.TwoInputs;
                                break;
                            case "N":
                                draggableNodeType = DraggableNodeType.Constant;
                                break;
                            case "=":
                                draggableNodeType = DraggableNodeType.Result;
                                break;
                            default:
                                throw new Error("Unknown draggableNodeType string :" + blockType);
                        }

                        DraggableNode node = new DraggableNode(draggableNodeType);

                        node.setType(DragIconType.valueOf(container.getValue("type")));
                        right_pane.getChildren().add(node);

                        Point2D cursorPoint = container.getValue("scene_coords");

                        node.relocateToPoint(
                                new Point2D(cursorPoint.getX() - 32, cursorPoint.getY() - 32)
                        );

                        scheme.Blocks.add(node.block);

                    }
                }

                container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.DragNode);

                if (container != null) {
                    if (container.getValue("type") != null)
                        System.out.println ("Moved node " + container.getValue("type"));
                }

                //AddLink drag operation
                container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

                if (container != null) {

                    //bind the ends of our link to the nodes whose id's are stored in the drag container
                    String sourceId = container.getValue("source");
                    String targetId = container.getValue("target");

                    if (sourceId != null && targetId != null) {

                        //System.out.println(container.getData());
                        NodeLink link = new NodeLink();

                        //add our link at the top of the rendering order so it's rendered first
                        right_pane.getChildren().add(0,link);

                        DraggableNode source = null;
                        DraggableNode target = null;

                        for (Node n: right_pane.getChildren()) {

                            if (n.getId() == null)
                                continue;

                            if (n.getId().equals(sourceId))
                                source = (DraggableNode) n;

                            if (n.getId().equals(targetId))
                                target = (DraggableNode) n;

                        }

                        if (source != null && target != null)
                            link.bindEnds(source, target, container.getValue("mouse_y"));
                    }

                }

                event.consume();

                //TODO : create block
            }
        });
    }

    private void addDragDetection(DragIcon dragIcon) {

        dragIcon.setOnDragDetected (new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                // set the other drag event handles on their respective objects
                base_pane.setOnDragOver(mIconDragOverRoot);
                right_pane.setOnDragOver(mIconDragOverRightPane);
                right_pane.setOnDragDropped(mIconDragDropped);

                // get a reference to the clicked DragIcon object
                DragIcon icn = (DragIcon) event.getSource();

                //begin drag ops
                mDragOverIcon.setType(icn.getType());
                mDragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", mDragOverIcon.getType().toString());
                content.put(DragContainer.AddNode, container);

                mDragOverIcon.startDragAndDrop (TransferMode.ANY).setContent(content);
                mDragOverIcon.setVisible(true);
                mDragOverIcon.setMouseTransparent(true);
                event.consume();
            }
        });
    }

    public void buildButtonHandlers() {
        CalculateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                Debugger.log("Calculate button clicked");
                WarningLine.setText(""); // smazani varovne zpravy
                int validationResult =  scheme.ValidateScheme();
                if (validationResult == 0){
                   scheme.Calculate();
                   UpdateBlockPrintedValues();
                }
                else {
                    ValidationMessage(validationResult);
                }
            }
        });

        DebugButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                WarningLine.setText(""); // smazani varovne zpravy
                Debugger.log("Debug button clicked");
                int validationResult =  scheme.ValidateScheme();
                if (validationResult == 0){
                    scheme.CalculateOnce();
                    UpdateBlockPrintedValues();
                }
                else {
                    ValidationMessage(validationResult);
                }
            }
        });
    }

    private void ValidationMessage(int validationResult) {

        switch (validationResult){
                case 1: //input error
                    WarningLine.setText("WARNING: UNATTACHED INPUTS FOUND!!!");
                    break;

                case 2: //output error
                    WarningLine.setText("WARNING: UNATTACHED OUTPUTS FOUND!!!");
                    break;

                case 3: // cycle error
                    WarningLine.setText("WARNING: CYCLE DETECTED!!!");
                    break;

                default:
                    break;
            }
    }

    private void UpdateBlockPrintedValues() {

        for(Node node: right_pane.getChildren()){
            if (node instanceof DraggableNode) {
                DraggableNode dn = (DraggableNode) node;
                if (dn.block.MyVal.defined) {
                    if (dn.block instanceof ResultBlock) {
                        dn.Result.setText(String.valueOf(dn.block.MyVal.val));
                    } else if ( dn.block instanceof ConstBlock) {

                    } else {
                        dn.valueDisplay.setText(String.valueOf(dn.block.MyVal.val));
                    }
                }
            }
        }
    }
}