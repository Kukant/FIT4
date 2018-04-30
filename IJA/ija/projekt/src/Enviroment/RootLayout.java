package Enviroment;

import java.io.*;

import Blocks.*;
import Others.Debugger;
import Others.Output;
import Schemes.Scheme;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import javax.xml.transform.Result;


public class RootLayout extends AnchorPane {

    @FXML SplitPane base_pane;
    @FXML AnchorPane right_pane;
    @FXML VBox left_pane;
    @FXML javafx.scene.control.Button CalculateButton;
    @FXML Button DebugButton;
    @FXML MenuItem SaveBtn;
    @FXML MenuItem LoadBtn;

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

                        DraggableNode node = new DraggableNode(DragIconType.valueOf(container.getValue("type")));
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
            }
        });

        DebugButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                Debugger.log("Debug button clicked");
                Debugger.log(scheme.Blocks.size());
                scheme.CalculateOnce();
                UpdateBlockPrintedValues();
            }
        });

        SaveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Debugger.log("Save button clicked");
                SaveScheme();
            }
        });

        LoadBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Debugger.log("Load button clicked");
                LoadScheme();
            }
        });



    }

    private void SaveScheme() {
        for (Node node: right_pane.getChildren()) {
            if (node.getClass() != DraggableNode.class)
                continue;
            DraggableNode dnode = (DraggableNode) node;
            dnode.block.parentPosX = dnode.localToScene(node.getBoundsInLocal()).getMinX();
            dnode.block.parentPosY = dnode.localToScene(node.getBoundsInLocal()).getMinY();
        }
        // TODO get filename
        String filename = "schemeSaveTest";
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this.scheme);
            out.close();
            fileOut.close();
            Debugger.log("Serialized data is saved in " + filename + ", pwd: " + System.getProperty("user.dir"));
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void LoadScheme() {
        // TODO get filename
        String filename = "schemeSaveTest";
        Scheme scheme = null;
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            scheme = (Scheme) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Scheme class not found");
            c.printStackTrace();
        }

        if (scheme == null)
            throw new Error("Scheme was not loaded correctly");

        this.scheme = scheme;

        // clear the right pane
        right_pane.getChildren().clear();

        // generate new nodes
        for (Block block : scheme.Blocks) {
            DraggableNode node = new DraggableNode(BlockToDragIconType(block.getClass()));
            block.parent = node;
            right_pane.getChildren().add(node);
            node.relocateToPoint( new Point2D(block.parentPosX, block.parentPosY));

        }

        for (Block block : scheme.Blocks) {
            // generate new connections
            for (Output out : block.Outputs) {
                NodeLink link = new NodeLink();
                right_pane.getChildren().add(0,link);
                if (out.Index == 0) {
                    link.bindEnds(block.parent, out.block.parent, -50000);
                } else if (out.Index == 1){
                    link.bindEnds(block.parent, out.block.parent, 50000);
                } else {
                    throw new Error("Unexpected Index");
                }
            }

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

    private DragIconType BlockToDragIconType(Class klasa) {
        if (klasa == SubBlock.class )
            return DragIconType.sub;
        else if ( klasa == AddBlock.class)
            return DragIconType.add;
        else if ( klasa == DivBlock.class)
            return DragIconType.div;
        else if ( klasa == PowBlock.class)
            return DragIconType.pow;
        else if ( klasa == MulBlock.class)
            return DragIconType.mul;
        else if ( klasa == RootBlock.class)
            return DragIconType.root;
        else if (klasa == ConstBlock.class)
            return DragIconType._const;
        else if (klasa == ResultBlock.class)
            return DragIconType.result;
        else
            throw new Error("Unknown draggableNodeType");
    }
}