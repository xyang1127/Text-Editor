package editor;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.util.*;

public class KeyEventHandler implements EventHandler<KeyEvent> {

    private Group textRoot;
    private FastLinkedList text;
    private RenderEngine renderEngine;
    private Deque<Info> undoStack;
    private final int stackSize = 100;
    private Deque<Info> redoStack;
    private Clipboard clipboard = Clipboard.getSystemClipboard();

    private class Info {
        Node position;
        Node toAdd;
        boolean add;
        List<Node> nodes;
        boolean notDone = false;

        Info(Node n, boolean add) {
            this.position = n;
            this.add = add;
        }

        Info(Node n, Node toAdd, boolean add) {
            this.position = n;
            this.toAdd = toAdd;
            this.add = add;
        }

        Info(Node position, List<Node> nodes, boolean add) {
            this.position = position;
            this.nodes = nodes;
            this.add = add;
        }

        void addChild() {
            notDone = true;
        }

        @Override
        public String toString() {
            return add + " : " + "add " + toAdd.character.getText() + " after \'" + position.character.getText() + "\'";
        }
    }

    public KeyEventHandler(Group root, FastLinkedList text, RenderEngine renderEngine) {
        this.textRoot = root;
        this.text = text;
        this.renderEngine = renderEngine;

        undoStack = new ArrayDeque<>(stackSize);
        redoStack = new ArrayDeque<>();
    }

    @Override
    public void handle(KeyEvent keyEvent) {

        if(keyEvent.isShortcutDown()) {
            // handle the shortcut key first
            KeyCode code = keyEvent.getCode();

            switch(code) {
                case S:
                    // save file
                    Editor.writeFile();
                    break;
                case Z:
                    // undo
                    redoUndoHandler(undoStack, redoStack);
                    break;
                case Y:
                    // redo
                    redoUndoHandler(redoStack, undoStack);
                    break;
                case PLUS:
                case EQUALS:
                    // make font size larger
                    renderEngine.changFontSize(true);
                    break;
                case MINUS:
                    // make font size smaller
                    renderEngine.changFontSize(false);
                    break;
                case LEFT:
                    // move the cursor to the end of the line
                    renderEngine.moveCursorToStart();
                    break;
                case RIGHT:
                    // move the cursor to the begining of the line
                    renderEngine.moveCursorToEnd();
                    break;
                case UP:
                    // move cursor to the top
                    renderEngine.moveCursorToTop();
                    break;
                case DOWN:
                    // move cursor to the bottom
                    renderEngine.moveCursorToBottom();
                    break;
                case C:
                    // copy
                    copy();
                    break;
                case V:
                    // paste
                    paste();
                    break;
                case P:
                    // print the position of the cursor
                    System.out.println(renderEngine.cursor.getX() + ", " + renderEngine.cursor.getY());
                    break;
            }

            keyEvent.consume();


        } else {

            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String characterTyped = keyEvent.getCharacter();
                // ignore some characters
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {

                    if(renderEngine.boxes.size() != 0) {
                        List<Node> nodes = deleteSelectedRegion();
                        Info addBackNodes = new Info(text.currentNode, nodes, true);
                        addToUndoStack(addBackNodes);

                        addCharAfterCurrent(characterTyped);
                        Info deleteChar = new Info(text.currentNode, false);
                        deleteChar.addChild();
                        addToUndoStack(deleteChar);

                    } else {

                        addCharAfterCurrent(characterTyped);
                        // push to undoStack
                        addToUndoStack(new Info(text.currentNode, false));

                    }

                    redoStack.clear();

                    keyEvent.consume();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {

                KeyCode code = keyEvent.getCode();

                switch(code){
                    case BACK_SPACE:
                        if(renderEngine.boxes.size() != 0) {
                            List<Node> toDelete = deleteSelectedRegion();
                            renderEngine.render(false);
                            renderEngine.cursorAppear();

                            addToUndoStack(new Info(text.currentNode, toDelete, true));

                        } else {
                            Node toDelete = text.currentNode;
                            deleteCurrent();

                            // push to undo stack
                            addToUndoStack(new Info(text.currentNode, toDelete, true));

                        }
                        redoStack.clear();
                        keyEvent.consume();
                        break;
                    case LEFT:
                        if(renderEngine.boxes.size() == 0) {
                            text.moveLeft();
                            renderEngine.render(true);
                        } else {
                            text.setCurrentNode(renderEngine.boxStartNode.pre);
                            renderEngine.removeSelectionBox();
                            renderEngine.renderCursor(true);
                        }
                        renderEngine.cursorAppear();
                        break;
                    case RIGHT:
                        if(renderEngine.boxes.size() == 0) {
                            text.moveRight();
                            renderEngine.render(true);
                        } else {
                            text.setCurrentNode(renderEngine.boxEndNode);
                            renderEngine.removeSelectionBox();
                            renderEngine.renderCursor(false);
                        }
                        renderEngine.cursorAppear();
                        break;
                    case UP:
                        handleUpArrow();
                        break;
                    case DOWN:
                        handleDownArrow();
                        break;
                }
            }

        }
    }

    private List<Node> deleteSelectedRegion() {

        List<Node> ans = new LinkedList<>();

        if(renderEngine.boxes.size() == 0)
            return ans;

        Node beforeStart = renderEngine.boxStartNode.pre;

        Node iter = renderEngine.boxEndNode;
        text.setCurrentNode(iter);

        while(text.currentNode != beforeStart) {
            Node cur = text.currentNode;
            textRoot.getChildren().remove(cur.character);
            ans.add(0, cur);
            text.deleteChar();
        }

        renderEngine.removeSelectionBox();

        return ans;

    }

    private void deleteCurrent() {
        Node toDelete = text.currentNode;
        Text currentText = toDelete.character;
        if (currentText == null)
            return;
        else {
            text.deleteChar();
            textRoot.getChildren().remove(currentText);
            if (!currentText.getText().equals("\n"))
                renderEngine.render(true);
            else
                renderEngine.render(false);
            renderEngine.cursorAppear();
        }
    }

    private void addCharAfterCurrent(String characterTyped) {
        if (characterTyped.equals("\r"))
            text.addChar('\n');
        else
            text.addChar(characterTyped.charAt(0));
        textRoot.getChildren().add(text.currentNode.character);
        renderEngine.render(false);
        renderEngine.cursorAppear();
    }

    private void addNodeAfterCurrent(Node n) {
        text.addNode(n);
        textRoot.getChildren().add(text.currentNode.character);
        renderEngine.render(false);
        renderEngine.cursorAppear();
    }

    private void handleUpArrow() {

        if(renderEngine.boxes.size() != 0) {
            text.setCurrentNode(renderEngine.boxStartNode.pre);
            renderEngine.removeSelectionBox();
            renderEngine.render(true);
        }

        Cursor cursor = renderEngine.cursor;
        int row = renderEngine.getRowIndex(cursor.getY()) - 1;

        if(row == -1) {
            // at the first line
            text.setCurrentNode(text.head);
            renderEngine.render(false);
        } else {

            Node startNode = renderEngine.getFirstNodeOfTheRow(row);
            Node endNode = renderEngine.getFirstNodeOfTheRow(row + 1);
            if(endNode == null)
                endNode = text.tail;

            renderEngine.searchAndRenderClosestNodeInARow(startNode, endNode, renderEngine.cursor.getX());
        }

        renderEngine.cursorAppear();

    }

    private void handleDownArrow() {

        if(renderEngine.boxes.size() != 0) {
            text.setCurrentNode(renderEngine.boxEndNode);
            renderEngine.removeSelectionBox();
            renderEngine.render(true);
        }

        Cursor cursor = renderEngine.cursor;
        int row = renderEngine.getRowIndex(cursor.getY()) + 1;

        if(row >= renderEngine.getRowListLength()) {
            // at the last line
            text.setCurrentNode(text.tail.pre);
            renderEngine.render(false); // true or false doesn't matter here
        } else {

            Node startNode = renderEngine.getFirstNodeOfTheRow(row);
            Node endNode = renderEngine.getFirstNodeOfTheRow(row + 1);
            if(endNode == null)
                endNode = text.tail;

            renderEngine.searchAndRenderClosestNodeInARow(startNode, endNode, renderEngine.cursor.getX());
        }

        renderEngine.cursorAppear();
    }

    public void redoUndoHandler(Deque<Info> get, Deque<Info> to) {

        redoUndoHandlerHelper(get, to, false);

    }

    private void copy() {
        if(renderEngine.boxes.size() == 0)
            return;

        // build string:
        Node iter = renderEngine.boxStartNode;
        Node end = renderEngine.boxEndNode.next; // loop end condition

        StringBuilder sb = new StringBuilder();
        while (iter != end) {
            sb.append(iter.character.getText());
            iter = iter.next;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        clipboard.setContent(content);

    }

    private void paste() {

        String toPaste = clipboard.getString();

        List<Node> nodes = deleteSelectedRegion();
        Info addBackNodes = new Info(text.currentNode, nodes, true);
        addToUndoStack(addBackNodes);

        for(char c : toPaste.toCharArray()) {

            if (c == '\r')
                text.addChar('\n');
            else
                text.addChar(c);
            textRoot.getChildren().add(text.currentNode.character);

            Info deleteChar = new Info(text.currentNode, false);
            deleteChar.addChild();
            addToUndoStack(deleteChar);
        }

        renderEngine.render(false);
        renderEngine.cursorAppear();

    }

    private void redoUndoHandlerHelper(Deque<Info> get, Deque<Info> to, boolean moveOn) {

        if(get.peek() == null)
            return;

        Info cur = get.pop();

        Info newInfo;

        if(cur.add) {
            if(cur.nodes == null) {
                text.setCurrentNode(cur.position);
                addNodeAfterCurrent(cur.toAdd);
                newInfo = new Info(text.currentNode, false);
            } else {
                text.setCurrentNode(cur.position);
                for(Node n : cur.nodes) {
                    text.addNode(n);
                    textRoot.getChildren().add(text.currentNode.character);
                }
                renderEngine.render(false);
                renderEngine.cursorAppear();

                newInfo = new Info(text.currentNode, cur.nodes, false);
            }
        } else {
            if(cur.nodes == null) {
                text.setCurrentNode(cur.position);
                deleteCurrent();

                newInfo = new Info(text.currentNode, cur.position, true);
            } else {
                text.setCurrentNode(cur.position);
                int l = cur.nodes.size();
                for(int i=0; i<l; i++) {
                    Node tmp = text.currentNode;
                    textRoot.getChildren().remove(tmp.character);
                    text.deleteChar();
                }
                if (text.currentNode != text.head && !text.currentNode.character.getText().equals("\n"))
                    renderEngine.render(true);
                else
                    renderEngine.render(false);
                renderEngine.cursorAppear();

                newInfo = new Info(text.currentNode, cur.nodes, true);
            }
        }

        if(moveOn)
            newInfo.addChild();

        to.push(newInfo);

        if(cur.notDone) {
            redoUndoHandlerHelper(get, to, true);
        }

    }

    // maintain a capacity of stackSize
    private void addToUndoStack(Info i) {

        int size = 0;

        for(Info info : undoStack) {
            if(!info.notDone)
                size++;
        }

        if(size >= 100)
            undoStack.removeLast();

        undoStack.push(i);
    }

}
