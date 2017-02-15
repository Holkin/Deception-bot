package test;


import deception.BoardFactory;
import deception.Game;
import deception.GoBoard;
import deception.MoveResolver;
import deception.Settings;

import java.io.BufferedReader;
import java.io.FileReader;

import static deception.GoBoard.BOARD_SIZE;

public class TestFromFile {
    public static void main(String... args) throws Exception {
        Game game = new Game(BoardFactory.newBoard());
        GoBoard.Hash prevHash = null;
        MoveResolver resolver = new MoveResolver(new Settings());
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

                String str = GoBoardUtil.toString(game.getBoard());
                resolver.getMove(game);
                String str2 = GoBoardUtil.toString(game.getBoard());
                if (!str.equals(str2)) {
                    System.out.println(str);
                    System.out.println(str2);
                    int l = 0, r = Math.max(str.length(), str2.length());
                    for (int i = 0; i < r; i++) {
                        if (str.charAt(i) != str2.charAt(i)) {
                            l = i;
                            break;
                        }
                    }
                    for (int i = r - 1; i > -1; i--) {
                        if (str.charAt(i) != str2.charAt(i)) {
                            r = i+1;
                            break;
                        }
                    }
                    System.out.println("\n\n\n\n\n");
                    System.out.println(str.substring(l, r));
                    System.out.println(str2.substring(l, r));
                    throw new RuntimeException();
                }

                playMove(splits[4], splits[5], game);
                turn++;
                System.out.println(game.getBoard().hash());
                System.out.println(prevHash);
                System.out.println("hashDif=" + !game.getBoard().hash().equals(prevHash) + " turn=" + turn / 2);
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
        int maxOffset = BOARD_SIZE * BOARD_SIZE - 1;
        String mat[][] = new String[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < arr.length; i++) {
            mat[i % BOARD_SIZE][i / BOARD_SIZE] = arr[i];
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
