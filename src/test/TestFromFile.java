package test;


import deception.BoardFactory;
import deception.Game;
import deception.GoBoard;

import java.io.BufferedReader;
import java.io.FileReader;

import static deception.GoBoard.BOARD_SIZE;

public class TestFromFile {
    public static void main(String... args) throws Exception {
        Game game = new Game(BoardFactory.newBoard());
        GoBoard.Hash prevHash = null;
        int turn = -1;
        BufferedReader reader = new BufferedReader(new FileReader("test1"));
        String line = reader.readLine();
        while (line != null) {
            if (turn == 300) {
                System.out.println(); // break point
            }
            String splits[] = line.replaceAll("\"", "").split(" ");
            if (line.contains("last_move") && !line.contains("Null")) {
                if (line.contains("pass")) {
                    game.passMove();
                    turn++;
                    continue;
                }
                System.out.println(String.format("%s %s %s", splits[3], splits[4], splits[5]));
                playMove(splits[4], splits[5], game);
                turn++;
                System.out.println(game.getBoard().hash());
                System.out.println(prevHash);
                System.out.println("hashDif="+!game.getBoard().hash().equals(prevHash)+" turn="+turn/2);
            }
            if (line.contains("Output from your bot") && !line.contains("null")) {
                if (line.contains("pass")) {
                    game.passMove();
                    turn++;
                    continue;
                }
                System.out.println(String.format("%s %s %s", splits[4], splits[5], splits[6]));
                playMove(splits[5], splits[6], game);
                turn++;
            }

            if (line.contains("update game field")) {
                String arr[] = splits[3].split(",");
                prevHash = hash(arr);
            }
            line = reader.readLine();
        }
    }

    private static void playMove(String xStr, String yStr, Game game) {
        int x = Integer.valueOf(xStr);
        int y = Integer.valueOf(yStr);
        game.playMove(x, y);
        game.getBoard().print();
    }

    public static GoBoard.Hash hash(String arr[]) {
        int hashLen = (BOARD_SIZE * BOARD_SIZE + 3) / 4;
        int maxOffset = BOARD_SIZE*BOARD_SIZE -1;
        String mat[][] = new String[BOARD_SIZE][BOARD_SIZE];
        for (int i=0; i<arr.length; i++) {
            mat[i%BOARD_SIZE][i/BOARD_SIZE] = arr[i];
        }
        byte[] hash = new byte[hashLen];
        for (int i = 0; i < hashLen; i++) {
            int k = 0;
            for (int j = 0; j < 4; j++) {
                int offset = Math.min(maxOffset, i * 4 + j);
                k *= 4;
                k += Math.max(0, Integer.valueOf(mat[offset / BOARD_SIZE][offset % BOARD_SIZE]));
            }
            hash[i] = (byte) k;
        }
        return new GoBoard.Hash(hash);
    }
}
