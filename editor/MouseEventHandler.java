package editor;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;

public class MouseEventHandler implements EventHandler<MouseEvent> {

    private RenderEngine renderEngine;
    private FastLinkedList text;
    private Group textRoot;

    private Node startNode;

    public MouseEventHandler(RenderEngine renderEngine, FastLinkedList text, Group textRoot) {
        this.renderEngine = renderEngine;
        this.text = text;
        this.textRoot = textRoot;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        EventType eventType = mouseEvent.getEventType();
//        System.out.println(eventType);
        if(eventType == MouseEvent.MOUSE_CLICKED) {

            // click event takes effects only when there're none selection boxes
            if(renderEngine.boxes.size() == 0) {
                if(!textRoot.getChildren().contains(renderEngine.cursor.cursor))
                    textRoot.getChildren().add(renderEngine.cursor.cursor);
                getNodeNearMouse(x, y);
            }

        } else if(eventType == MouseEvent.MOUSE_PRESSED) {
            renderEngine.removeSelectionBox();
            startNode = getNodeNearMouse(x, y);
        } else if(eventType == MouseEvent.MOUSE_DRAGGED || eventType == MouseEvent.MOUSE_RELEASED) {
            Node startNode = this.startNode;
            Node endNode = getNodeNearMouse(x, y);

            // special case
            if(startNode != text.head && endNode != text.head) {
                // check which is at front
                double startX = startNode.character.getX();
                double startY = startNode.character.getY();
                double endX = endNode.character.getX();
                double endY = endNode.character.getY();
                if ((startY > endY) || (startY == endY && startX > endX)) {
                    // swap
                    Node tmp = startNode;
                    startNode = endNode;
                    endNode = tmp;
                }
            } else if(startNode != text.head && endNode == text.head) {
                //swap
                Node tmp = startNode;
                startNode = endNode;
                endNode = tmp;
            }

            renderEngine.renderSelectionBox(startNode.next, endNode);

        }
    }

    // return the nearest node and render it
    private Node getNodeNearMouse(double x, double y) {
        int row = renderEngine.getRowIndex(y + renderEngine.scrollBar.getValue());
        int rowListLength = renderEngine.getRowListLength();

        // empty text, do nothing
        if (rowListLength == 0)
            return text.head;

        // deal with special case where cursor is out of bound
        if (row < 0)
            row = 0;
        else if (row >= rowListLength) {
            if (text.tail.pre.character.getText().equals("\n")) {
                // special case
                text.setCurrentNode(text.tail.pre);
                renderEngine.renderCursor(false); // true or false does't matter
                return text.tail.pre;
            } else {
                row = rowListLength - 1;
            }
        }

        Node startNode = renderEngine.getFirstNodeOfTheRow(row);
        Node endNode = renderEngine.getFirstNodeOfTheRow(row + 1);
        if (endNode == null)
            endNode = text.tail;

        return renderEngine.searchAndRenderClosestNodeInARow(startNode, endNode, x);
    }

}
