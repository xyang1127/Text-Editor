package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Cursor {

    final Rectangle cursor;
    private final Timeline timeline = new Timeline();

    public Cursor(Group root) {

        cursor = new Rectangle(1,40);
        root.getChildren().add(cursor);
    }

    public void setPosition(int x, int y) {
        cursor.setX(x);
        cursor.setY(y);
    }

    public void setHeight(int h) {
        cursor.setHeight(h);
    }

    public int getY() {
        return (int)cursor.getY();
    }

    public int getX() {
        return (int)cursor.getX();
    }

    public void debug() {
        System.out.println(" @x: " + cursor.getX() + " @y: " + cursor.getY());
    }

    public void makeRectangleColorChange() {
        // Create a Timeline that will call the "handle" function of RectangleBlinkEventHandler
        // every 1 second.
        // The rectangle should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        Cursor.RectangleBlinkEventHandler cursorChange = new Cursor.RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void pause() {
        timeline.pause();
    }

    public void resume() {
        timeline.play();
    }

    /** An EventHandler to handle changing the color of the rectangle. */
    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {

        private boolean visible = true;

        private void changeColor() {
            if(visible)
                cursor.setFill(Color.BLACK);
            else
                cursor.setFill(Color.WHITE);

            visible = !visible;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

}
