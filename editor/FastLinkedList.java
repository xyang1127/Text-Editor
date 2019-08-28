package editor;

import javafx.scene.text.Text;

import java.util.Iterator;

public class FastLinkedList implements Iterable<Node> {

    Node head;
    Node tail;
    Node currentNode;
    private int size;

    public FastLinkedList() {
        head = new Node(null);
        tail = new Node(null);
        head.next = tail;
        tail.pre = head;

        currentNode = head;
        size = 0;
    }

    // add the character to the linkedlist after the current Node
    public void addChar(char c) {
        Node newNode = new Node(new Text(0,0, ""+c));

        addNode(newNode);
    }

    public void addNode(Node toAdd) {
        Node nextNode = currentNode.next;

        currentNode.next = toAdd;
        toAdd.pre = currentNode;

        nextNode.pre = toAdd;
        toAdd.next = nextNode;

        size++;

        currentNode = toAdd;
    }

    // delete the character at the current position
    public void deleteChar() {
        if(size == 0 || currentNode == head || currentNode == tail)
            return;

        Node preNode = currentNode.pre;
        Node nextNode = currentNode.next;

        preNode.next = nextNode;
        nextNode.pre = preNode;

        size--;

        currentNode = preNode;
    }

    // move the position of current cursor
    // false indicates this function does nothing
    public boolean moveLeft() {
        if(size == 0 || currentNode == head)
            return false;

        currentNode = currentNode.pre;
        return true;
    }

    // move the position of current cursor
    // false indicates this function does nothing
    public boolean moveRight() {
        if(size == 0 || currentNode.next == tail)
            return false;

        currentNode = currentNode.next;
        return true;
    }

    public int size() {
        return size;
    }

    public void setCurrentNode(Node n) {
        currentNode = n;
    }

    public void debug() {
        Node cur = head.next;
        while(cur != tail) {
            System.out.println(cur.character.getLayoutBounds().getWidth());
            cur = cur.next;
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return new FastLinkedListIterator();
    }

    class FastLinkedListIterator implements Iterator<Node> {

        private Node cur = head.next;

        @Override
        public boolean hasNext() {
            return cur != tail;
        }

        @Override
        public Node next() {
            Node ans = cur;
            cur = cur.next;
            return ans;
        }

    }

}
