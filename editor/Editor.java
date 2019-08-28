package editor;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;


public class Editor extends Application {

    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;

    private static FastLinkedList text;
    private static Group root;
    private static Group textRoot;
    private static Scene scene;
    private static RenderEngine renderEngine;
    private static Cursor cursor;
    private static ScrollBar scrollBar;
    private static String inputFilename;

    // initialize basic data structure
    private static void initialize() {
        root = new Group();
        textRoot = new Group();
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        root.getChildren().add(textRoot);
        text = new FastLinkedList();
        cursor = new Cursor(textRoot);
        scrollBar = new ScrollBar();
        renderEngine = new RenderEngine(text, WINDOW_WIDTH, WINDOW_HEIGHT, cursor,scrollBar, textRoot);
    }

    @Override
    public void start(Stage primaryStage) {

        EventHandler<KeyEvent> keyEventHandler = new KeyEventHandler(textRoot, text, renderEngine);
        EventHandler<MouseEvent> mouseEventEventHandler = new MouseEventHandler(renderEngine, text, textRoot);

        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(mouseEventEventHandler);
        scene.setOnMousePressed(mouseEventEventHandler);
        scene.setOnMouseDragged(mouseEventEventHandler);
        scene.setOnMouseReleased(mouseEventEventHandler);


        // listen to the scene window change event
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            double usableScreenWidth = newSceneWidth.intValue() - scrollBar.getLayoutBounds().getWidth();
            scrollBar.setLayoutX(usableScreenWidth);
            renderEngine.setWindowWidth((int)usableScreenWidth);
            renderEngine.render(false);
        });
        scene.heightProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            renderEngine.setWindowHeight(newSceneWidth.intValue());
            renderEngine.render(false);
        });

        scrollBar.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            textRoot.setLayoutY(-(int)newValue.doubleValue());
        });

        primaryStage.setTitle("Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // read file and storing the contents of the document into the FastLinkedList text
    private static void readFile(String inputFilename) {
        try {
            File inputFile = new File(inputFilename);
            // Check to make sure that the input file exists!
            if (!inputFile.exists()) {
                System.out.println("Unable to copy because file with name " + inputFilename
                        + " does not exist");
                return;
            }
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);

            int intRead = -1;
            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                text.addChar(charRead);
            }

            System.out.println("Successfully construct information for rendering " + inputFilename);

            // Close the reader and writer.
            bufferedReader.close();

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
            System.out.println("Now, Imma create a new file");
        } catch (IOException ioException) {
            System.out.println("Error when constructing information about rendering; exception was: " + ioException);
        }
    }

    private static void constructGroup() {
        for(Node node : text) {
            Text t = node.character;
            textRoot.getChildren().add(t);
        }
        text.setCurrentNode(text.head);
    }

    // write the content in the content list to the file
    public static void writeFile() {

        try {
            FileWriter writer = new FileWriter(inputFilename);

            for(Node n : text) {
                writer.write(n.character.getText());
            }

            System.out.println("file has been successfully saved");
            writer.close();

        } catch (IOException e) {
            System.out.println("write file fails");
        }

    }

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            System.out.println("Expected usage: CopyFile <filename> (optional)debug");
            System.exit(1);
        }
        inputFilename = args[0];

        initialize();
        readFile(inputFilename);

        constructGroup();

        cursor.setPosition(250,250);
        // cursor starts to blink
        cursor.makeRectangleColorChange();

        // integrate scroll bar
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        root.getChildren().add(scrollBar);
        double usableScreenWidth = WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth();
        scrollBar.setLayoutX(usableScreenWidth);
        renderEngine.setWindowWidth((int)usableScreenWidth);
        scrollBar.setUnitIncrement(10); // scroll speed
        scrollBar.setMin(0);
        scrollBar.setMax(0);


        renderEngine.render(false);

        // text.debug();

        if (args.length == 2)
            System.out.println("debug");

        launch(args);
    }
}