package test;


import deception.GoBoard;

public class GoBoardUtil {
    public static String toString(GoBoard board) {
        return board.groups().stream().map(item -> item.toString()+item.hashCode()).reduce("", (a, b) -> a + b);
    }
}
