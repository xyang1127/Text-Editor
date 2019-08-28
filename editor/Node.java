package editor;

import javafx.scene.text.Text;

class Node {

    Text character;
    Node pre;
    Node next;

    public Node(Text t) {
        character = t;
    }

}