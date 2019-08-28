package editor;

public class TestList {

    public static void main(String[] args) {
        FastLinkedList list = new FastLinkedList();

        list.addChar('a');
        list.addChar('b');
        list.addChar('c');
        list.addChar('d');

        list.deleteChar();

        list.debug();

    }

}
