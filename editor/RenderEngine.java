package editor;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.ScrollBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RenderEngine {

    private FastLinkedList list;
    private int windowWidth;
    private int windowHeight;

    final int leftMargin = 5;
    private final int rightMargin = 5;
    private final int topMargin = 0;
    private final int bottomMargin = 0;
    final Cursor cursor;

    private int fontHeight;
    private String fontName = "Verdana";
    private int fontSize = 12;

    private int xPosition;
    private int yPosition;

    private List<Node> startOfEachRow;
    private Group textRoot;
    List<Rectangle> boxes;
    ScrollBar scrollBar;

    Node boxStartNode;
    Node boxEndNode;

    public RenderEngine(FastLinkedList list, int width, int height, Cursor cursor, ScrollBar scrollBar, Group textRoot) {

        this.list = list;
        windowWidth = width;
        windowHeight = height;
        this.cursor = cursor;
        startOfEachRow = new LinkedList<>();
        this.scrollBar = scrollBar;
        this.textRoot = textRoot;
        boxes = new ArrayList<>();
    }

    private void updateFontHeight() {
        Text tmp = new Text(0,0, "a");
        tmp.setFont(Font.font(fontName, fontSize));
        fontHeight = (int)(tmp.getLayoutBounds().getHeight() + 1);
    }

    public void renderCursor(boolean cursorAtSecondLine) {
        cursor.setHeight(fontHeight);

        Node curNode = list.currentNode;
        Text curText = curNode.character;

        if(curNode == list.head)
            // cursor is at the head
            cursor.setPosition(leftMargin, topMargin);
        else {

            Node nextNode = list.currentNode.next;
            Text nextText = nextNode.character;

            if(curText.getText().equals("\n")) {
                cursor.setPosition(leftMargin, (int)curText.getY() + fontHeight);
            }else if(nextNode != list.tail && curText.getY() != nextText.getY() && cursorAtSecondLine)
                cursor.setPosition((int)nextText.getX(), (int)nextText.getY());
            else {
                int x = Math.min((int) (curText.getX() + textWidth(curText)), windowWidth - rightMargin);
                int y = (int) (curText.getY());
                cursor.setPosition(x, y);
            }
        }

        // cursor.debug();
    }

    public void cursorAppear() {

        int currentY = cursor.getY();
        if(currentY < scrollBar.getValue())
            scrollBar.setValue(currentY);
        else if(currentY > scrollBar.getValue() + windowHeight - bottomMargin - fontHeight)
            scrollBar.setValue(currentY + fontHeight + bottomMargin - windowHeight);

    }

    public void render(boolean cursorAtSecondLine) {
        renderText();

        renderCursor(cursorAtSecondLine);

        renderSelectionBox(boxStartNode, boxEndNode);
    }

    // modify each Text object to make it right
    public void renderText() {

        updateFontHeight();
        startOfEachRow.clear();

        xPosition = leftMargin;
        yPosition = topMargin;

        List<Node> currentWord = new LinkedList<>();
        int length = 0;

        boolean start = true;

        for(Node node : list) {

            Text t = node.character;

            t.setTextOrigin(VPos.TOP);
            t.setFont(Font.font(fontName, fontSize));

            if(start) {
                startOfEachRow.add(node);
                start = false;
            }

            String currentChar = t.getText();
            if(!currentChar.equals(" ") && !currentChar.equals("\n")) {
                currentWord.add(node);
                length += textWidth(t);
            } else {
                appendWord(length, currentWord);
                currentWord.clear();
                length = 0;

                if(currentChar.equals(" ")) {
                    // append the white space
                    t.setX(xPosition);
                    t.setY(yPosition);
                    xPosition += textWidth(t);
                } else if(currentChar.equals("\n")){
                    // '\n' have a length 0 so there's no need to update xPosition
                    t.setX(xPosition);
                    t.setY(yPosition);
                    // append the new line
                    xPosition = leftMargin;
                    yPosition += fontHeight;
                    start = true;
                }
            }

        }

        // append the last word
        appendWord(length, currentWord);

        // set the scroll bar max to the height of the text
        int textHeight;
        if(startOfEachRow.size() == 0)
            // special case: empty text
            textHeight = fontHeight;
        else {
            textHeight = fontHeight * startOfEachRow.size();
            if(list.tail.pre.character.getText().equals("\n"))
                textHeight += fontHeight;
        }
        scrollBar.setPrefHeight(windowHeight);
        scrollBar.setMax(Math.max(textHeight - windowHeight, 0));

        // deal with deletion issue
        if(scrollBar.getValue() > scrollBar.getMax()) {
            scrollBar.setValue(scrollBar.getMax());
        }

        // debugStartOfEachRow();

    }

    private void debugStartOfEachRow() {

        System.out.println("");
        for(Node node : startOfEachRow)
            System.out.println(node.character);
        System.out.println("");

    }

    private void appendWord(int wordLength, List<Node> word)  {
        if(wordLength == 0)
            return;

        if((xPosition + wordLength) <= (windowWidth - rightMargin) || xPosition == leftMargin) {
            // append the word. No need to add to startOfEachRow list cuz we have done that in the renderHelper function
            appendWordHelper(word);
        } else {
            xPosition = leftMargin;
            yPosition += fontHeight;
            startOfEachRow.add(word.get(0));
            appendWordHelper(word);
        }
    }

    private void appendWordHelper(List<Node> word) {

        for(Node node : word) {

            Text c = node.character;
            int length = textWidth(c);

            // modify here to set right margin
            if(xPosition + length > (windowWidth - rightMargin)) {
                xPosition = leftMargin;
                yPosition += fontHeight;
                startOfEachRow.add(node);
            }

            c.setX(xPosition);
            c.setY(yPosition);

            xPosition += length;
        }

    }

    public void setWindowWidth(int width) {
        windowWidth = width;
    }

    public void setWindowHeight(int height) {
        windowHeight = height;
    }

    public static int textWidth(Text t) {
        double w = t.getLayoutBounds().getWidth();
        if(w % 1 >= 0.5)
            return (int)(w + 1);
        else
            return (int)w;
    }

    public int getRowIndex(double y) {
        return (int)(y / fontHeight);
    }

    public Node getFirstNodeOfTheRow(int row) {
        if(row >= startOfEachRow.size() || row < 0)
            return null;

        return startOfEachRow.get(row);
    }

    public int getRowListLength() {
        return startOfEachRow.size();
    }

    // search through startNode to endNode.pre
    //
    public Node searchAndRenderClosestNodeInARow(Node startNode, Node endNode, double target) {
        Node iter = startNode;

        Node targetNode = iter.pre;
        int min = Integer.MAX_VALUE;
        while(iter != endNode && !iter.character.equals("\n")) {
            int dif = (int)Math.abs(target - iter.character.getX());
            if(dif < min) {
                min = dif;
                targetNode = iter.pre;
            }
            iter = iter.next;
        }

        // check the last character in this row
        iter = iter.pre;
        if(!iter.character.equals("\n")) {
            int dif = (int)Math.abs(iter.character.getX() + RenderEngine.textWidth(iter.character) - target);
            if(dif < min) {
                targetNode = iter;
                list.setCurrentNode(targetNode);
                renderCursor(false);
                return targetNode;
            }
        }

        list.setCurrentNode(targetNode);
        renderCursor(true);

        return targetNode;
    }

    public void changFontSize(boolean larger) {
        if(larger)
            fontSize += 4;
        else
            fontSize = Math.max(4, fontSize - 4);

        render(false);
    }

    public void moveCursorToStart() {

        int row = getRowIndex(cursor.getY());
        Node start = getFirstNodeOfTheRow(row);
        if(start == null)
            return;

        list.setCurrentNode(start.pre);
        renderCursor(true);

    }

    public void moveCursorToEnd() {

        int row = getRowIndex(cursor.getY());

        if(row >= startOfEachRow.size() || row < 0)
            // special case: "\n" at the end of a line
            return;

        Node end;
        if(row == startOfEachRow.size() - 1)
            // special case: at the last line
            end = list.tail.pre;
        else
            end = getFirstNodeOfTheRow(row + 1).pre;

        if(end.character.getText().equals("\n"))
            end = end.pre;

        list.setCurrentNode(end);
        renderCursor(false);

    }

    public void moveCursorToTop() {

        list.setCurrentNode(list.head);
        renderCursor(false);

    }

    public void moveCursorToBottom() {

        list.setCurrentNode(list.tail.pre);
        renderCursor(false);

    }

    // select box: node parameter should always be valid
    public void removeSelectionBox() {

        if(boxes.size() != 0) {
            textRoot.getChildren().removeAll(boxes);
            boxes.clear();
            textRoot.getChildren().add(cursor.cursor);
            boxStartNode = null;
            boxEndNode = null;
        }

    }

    public void renderSelectionBox(Node start, Node end) {

        textRoot.getChildren().removeAll(boxes);

        boxes.clear();

        if(start == null || end == null)
            return;

        if(start == end.next)
            return;

        boxStartNode = start;
        boxEndNode = end;

        int startX = (int)start.character.getX();
        int startY = (int)start.character.getY();
        int endX = (int)end.character.getX();
        int endY = (int)end.character.getY();

        int startRow = getRowIndex(startY);
        int endRow = getRowIndex(endY);

        // System.out.println("start: " + startRow + " end: " + endRow);
        if(startRow == endRow) {
            Rectangle rec;
            if(!end.character.getText().equals("\n"))
                rec = new Rectangle(endX + end.character.getLayoutBounds().getWidth() - startX, fontHeight);
            else
                rec = new Rectangle(windowWidth - leftMargin - rightMargin, fontHeight);
            rec.setX(startX);
            rec.setY(startY);
            rec.toBack();
            boxes.add(rec);
        }else {
            // draw the box at the first line
            Rectangle startRec = new Rectangle(windowWidth - rightMargin - startX, fontHeight);
            startRec.setX(startX);
            startRec.setY(startY);
            boxes.add(startRec);

            // draw the middle part of boxes
            for (int i = startRow + 1; i <= endRow - 1; i++) {
                Rectangle rec = new Rectangle(windowWidth - leftMargin - rightMargin, fontHeight);
                rec.setY(fontHeight * i);
                rec.setX(leftMargin);
                boxes.add(rec);
            }

            // draw the box at the last line
            Rectangle endRec;
            if(!end.character.getText().equals("\n"))
                endRec = new Rectangle(endX + end.character.getLayoutBounds().getWidth() - leftMargin, fontHeight);
            else
                endRec = new Rectangle(windowWidth - leftMargin - rightMargin, fontHeight);
            endRec.setX(leftMargin);
            endRec.setY(endY);
            boxes.add(endRec);

        }
        // System.out.println(boxes.size());
        for(Rectangle r : boxes) {
            r.setFill(Color.LIGHTBLUE);
            textRoot.getChildren().add(r);
            r.toBack();
        }

        textRoot.getChildren().remove(cursor.cursor);
    }
}