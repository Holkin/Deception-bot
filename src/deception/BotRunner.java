package deception;

import java.util.Scanner;

/**
 * Created by Arkitekt on 12/17/2016.
 */
public class BotRunner {

    private static Scanner scanner = new Scanner(System.in);
    private static Settings settings;

    public static void run() {
        GoBoard board = BoardFactory.newBoard();
        settings = new Settings();
        MoveResolver moveResolver = new MoveResolver(settings);
        int turnNumber = 0;
        boolean colorChosen = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
//            String line = "action";
            if (StringUtil.isEmpty(line)) {
                continue;
            }
            String[] arg = line.split(" ");
            switch (arg[0]) {
                case "settings":
                    break;
                case "update":
                    if (arg[2].equals("last_move")) {
                        if (!colorChosen) {
                            colorChosen = true;
                            settings.myColor = Stone.WHITE;
                        }
                        if (!arg[3].equals("pass")) {
                            if (arg[3].equals("Null")) {
                                continue;
                            }
                            board = board.playMove(Integer.parseInt(arg[4]), Integer.parseInt(arg[5]));
                        } else {
                            board = board.passMove();
                        }
                        turnNumber++;
                    }
                    break;
                case "action":
                    colorChosen = true; // play as black by default
                    Point point = moveResolver.getMove(board, turnNumber);
                    if (point == null) {
                        System.out.println("pass");
                        board = board.passMove();
                    } else {
                        int x = point.getPosX();
                        int y = point.getPosY();
                        System.out.println(String.format("place_move %d %d",x,y));
                        board = board.playMove(x, y);
                    }
//                    board.print();
                    System.out.flush();
                    turnNumber++;
                    break;
            }
        }
    }
}
