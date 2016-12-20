package test;

import deception.*;

import java.util.Scanner;

/**
 * Created by Arkitekt on 12/18/2016.
 */
public class TestMain {

    private static Settings settings;

    public static void main(String args[]) {
        GoBoard board = BoardFactory.newBoard();
        settings = new Settings();
        MoveResolver moveResolver = new MoveResolver(settings);
        int turnNumber = 0;
        int n = 0;
        while (true) {
            if (n++ > 200) {
                System.out.println("ping");
            }
            Point point = moveResolver.getMove(board, turnNumber);
            if (point == null) {
                System.out.println("pass");
                board = board.passMove();
            } else {
                int x = point.getPosX();
                int y = point.getPosY();
                System.out.println(String.format("place_move %d %d", x, y));
                board = board.playMove(x, y);
            }
            board.print();
            boolean hasDeadGroup = false;
            for (Group g: board.groups()) {
                if (g.getEdges().isEmpty()) {
                    hasDeadGroup = true;
                    break;
                }
            }
            System.out.flush();
            turnNumber++;
        }
    }
}
