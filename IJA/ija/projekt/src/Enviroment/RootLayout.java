package Enviroment;

import java.io.*;

import Blocks.*;
import Others.Debugger;
import Others.Output;
import Schemes.Scheme;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Text;


public class RootLayout extends AnchorPane {

    @FXML SplitPane base_pane;
    @FXML AnchorPane right_pane;
    @FXML VBox left_pane;
    @FXML javafx.scene.control.Button CalculateButton;
    @FXML Button DebugButton;
    @FXML Text WarningLine;
    @FXML MenuItem SaveBtn;
    @FXML MenuItem LoadBtn;

    private EventHandler mIconDragOverRoot=null;
    private EventHandler mIconDragDropped=null;
    private EventHandler mIconDragOverRightPane=null;
    private BlockIcon mDragOverIcon = null;

    Stage stage;

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

        mDragOverIcon = new BlockIcon(); //specialni neviditelna ikona, je zobrazena jen kdyz "Drag and Dropujeme"

        mDragOverIcon.setVisible(false);//uprava jejich atributu
        mDragOverIcon.setOpacity(0.65);
        getChildren().add(mDragOverIcon);


        for (int i = 0; i < 8; i++) { // zobrazeni osmi ikon z BlockIcon.java

            BlockIcon icn = new BlockIcon();// nova ikona

            addDragDetection(icn); //je dragovatelna

            icn.setType(BlockIconType.values()[i]); //nastaven typ (barva)
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
                    mDragOverIcon.ChangePosition(new Point2D(event.getSceneX(), event.getSceneY()));
                    return;
                }

                event.consume();
            }
        };

        mIconDragOverRightPane = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                event.acceptTransferModes(TransferMode.ANY);

                mDragOverIcon.ChangePosition(
                        new Point2D(event.getSceneX(), event.getSceneY())
                );


                event.consume();
            }
        };

        mIconDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                DataHolder container =
                        (DataHolder) event.getDragboard().getContent(DataHolder.BlockAdded);

                container.importData("scene_coords", new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                content.put(DataHolder.BlockAdded, container);

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

                DataHolder container = (DataHolder) event.getDragboard().getContent(DataHolder.BlockAdded);

                if (container != null) {
                    if (container.fetchData("scene_coords") != null) {

                        MovableBlock node = new MovableBlock(BlockIconType.valueOf(container.fetchData("type")));
                        right_pane.getChildren().add(node);

                        Point2D cursorPoint = container.fetchData("scene_coords");

                        node.ChangePosition(
                                new Point2D(cursorPoint.getX() - 32, cursorPoint.getY() - 32)
                        );

                        scheme.Blocks.add(node.block);

                    }
                }

                container =
                        (DataHolder) event.getDragboard().getContent(DataHolder.BlockDragged);

                if (container != null) {
                    if (container.fetchData("type") != null)
                        System.out.println ("Moved node " + container.fetchData("type"));
                }

                //ConnectionAdded drag operation
                container = (DataHolder) event.getDragboard().getContent(DataHolder.ConnectionAdded);

                if (container != null) {

                    //bind the ends of our link to the nodes whose id's are stored in the drag container
                    String sourceId = container.fetchData("source");
                    String targetId = container.fetchData("target");

                    if (sourceId != null && targetId != null) {

                        //System.out.println(container.getData());
                        Connection link = new Connection();

                        //add our link at the top of the rendering order so it's rendered first
                        right_pane.getChildren().add(0,link);

                        MovableBlock source = null;
                        MovableBlock target = null;

                        for (Node n: right_pane.getChildren()) {

                            if (n.getId() == null)
                                continue;

                            if (n.getId().equals(sourceId))
                                source = (MovableBlock) n;

                            if (n.getId().equals(targetId))
                                target = (MovableBlock) n;

                        }

                        if (source != null && target != null)
                            link.ConnectBlocks(source, target, container.fetchData("mouse_y"), true);
                    }

                }

                event.consume();

                //TODO : create block
            }
        });
    }

    private void addDragDetection(BlockIcon blockIcon) {

        blockIcon.setOnDragDetected (new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                // set the other drag event handles on their respective objects
                base_pane.setOnDragOver(mIconDragOverRoot);
                right_pane.setOnDragOver(mIconDragOverRightPane);
                right_pane.setOnDragDropped(mIconDragDropped);

                // get a reference to the clicked BlockIcon object
                BlockIcon icn = (BlockIcon) event.getSource();

                //begin drag ops
                mDragOverIcon.setType(icn.getType());
                mDragOverIcon.ChangePosition(new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                DataHolder container = new DataHolder();

                container.importData("type", mDragOverIcon.getType().toString());
                content.put(DataHolder.BlockAdded, container);

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
                    for (Block res : scheme.Blocks){ // pro kazdy blok
                        if (res instanceof ResultBlock){ // ktery je vysledkovym blokem

                            while (!res.MyVal.defined) {
                                scheme.CalculateOnce();  // pocitame dokud neni definovan pocitame
                                UpdateBlockPrintedValues();
                            }
                        }
                    }
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
                Debugger.log(scheme.Blocks.size());
                Debugger.log(scheme.Blocks.get(0).MyVal.defined + " " + scheme.Blocks.get(0).MyVal.val);
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

    private void SaveScheme() {
        for (Node node: right_pane.getChildren()) {
            if (node.getClass() != MovableBlock.class)
                continue;
            MovableBlock dnode = (MovableBlock) node;
            dnode.block.parentPosX = dnode.localToScene(node.getBoundsInLocal()).getMinX();
            dnode.block.parentPosY = dnode.localToScene(node.getBoundsInLocal()).getMinY();
        }

        stage = (Stage) base_pane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Set your scheme name");
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null)
            return;

        String filename = selectedFile.getAbsolutePath();

        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this.scheme);
            out.close();
            fileOut.close();
            Debugger.log("Serialized data is saved in " + filename);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void LoadScheme() {
        stage = (Stage) base_pane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open scheme file");
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null)
            return;
        String filename = selectedFile.getAbsolutePath();

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
            MovableBlock node = new MovableBlock(BlockToDragIconType(block.getClass()));
            if (block.getClass() == ConstBlock.class) {
                int n = (int) block.InputValues[0].val;
                node.ConstValue.setText(String.valueOf(n));
            }

            node.block = block;
            block.parent = node;
            right_pane.getChildren().add(node);
            node.ChangePosition( new Point2D(block.parentPosX, block.parentPosY));

        }

        for (Block block : scheme.Blocks) {
            // generate new connections
            for (Output out : block.Outputs) {
                Connection link = new Connection();
                right_pane.getChildren().add(0,link);
                if (out.Index == 0) {
                    link.ConnectBlocks(block.parent, out.block.parent, -50000, false);
                } else if (out.Index == 1){
                    link.ConnectBlocks(block.parent, out.block.parent, 50000, false);
                } else {
                    throw new Error("Unexpected Index");
                }
            }

        }

        UpdateBlockPrintedValues();




    }

    private void UpdateBlockPrintedValues() {

        for(Node node: right_pane.getChildren()){

            if (node instanceof MovableBlock) {
                MovableBlock dn = (MovableBlock) node;
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

    private BlockIconType BlockToDragIconType(Class klasa) {
        if (klasa == SubBlock.class )
            return BlockIconType.sub;
        else if ( klasa == AddBlock.class)
            return BlockIconType.add;
        else if ( klasa == DivBlock.class)
            return BlockIconType.div;
        else if ( klasa == PowBlock.class)
            return BlockIconType.pow;
        else if ( klasa == MulBlock.class)
            return BlockIconType.mul;
        else if ( klasa == RootBlock.class)
            return BlockIconType.root;
        else if (klasa == ConstBlock.class)
            return BlockIconType._const;
        else if (klasa == ResultBlock.class)
            return BlockIconType.result;
        else
            throw new Error("Unknown draggableNodeType");
    }
}