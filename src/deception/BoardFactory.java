package deception;

import java.util.HashSet;

import static deception.GoBoard.BOARD_SIZE;

public class BoardFactory {

    private static final BasicPoint points[][][] = new BasicPoint[BOARD_SIZE][BOARD_SIZE][Stone.values().length];
    private static final BasicPoint clear[][] = new BasicPoint[BOARD_SIZE][BOARD_SIZE];

    static {
        for (int i=0; i<BOARD_SIZE; i++) {
            for (int j = 0; j<BOARD_SIZE; j++) {
                for (Stone stone : Stone.values()) {
                    points[i][j][stoneToIndex(stone)] = new BasicPoint(i, j, stone);
                }
                clear[i][j] = points[i][j][0];
            }
        }
    }

    public static Point newPoint(int x, int y, Stone stone) {
        return points[x][y][stoneToIndex(stone)];
    }
    public static GoBoard newBoard() {
        return new GoBoard(clear, new HashSet<>());
    }
    public static Group newGroup(Point point, GoBoard board) {
        return new SolidGroup(point, board);
    }

    private static int stoneToIndex(Stone stone) {
        return stone.ordinal();
    }
}
