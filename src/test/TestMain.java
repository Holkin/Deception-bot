package test;

import deception.*;

import java.util.Scanner;

/**
 * Created by Arkitekt on 12/18/2016.
 */
public class TestMain {

    private static Settings settings;

    public static void main(String args[]) {
        Game game = new Game(BoardFactory.newBoard());
        settings = new Settings();
        MoveResolver moveResolver = new MoveResolver(settings);
        int turnNumber = 0;
        int n = 0;
        while (true) {
            if (n++ > 200) {
                System.out.println("ping");
                break;
            }
            Point point = moveResolver.getMove(game);
            try{
                int x = point.getPosX();
                int y = point.getPosY();
                game.playMove(x, y);
                System.out.println(String.format("place_move %d %d",x,y));
            } catch (RuntimeException ex) {
                game.passMove();
                System.out.println("pass");
            }
            game.getBoard().print();
            System.out.flush();
            turnNumber++;
        }
    }
}
