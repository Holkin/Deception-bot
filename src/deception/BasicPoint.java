package deception;

import static deception.GoBoard.*;

public class BasicPoint implements Point {

    private final Stone stone;
    private final int x, y;

    public BasicPoint(int x, int y, Stone stone) {
        this.x = x;
        this.y = y;
        this.stone = stone;
    }

    @Override
    public Stone stone() {
        return stone;
    }

    @Override
    public int getPosX() {
        return x;
    }

    @Override
    public int getPosY() {
        return y;
    }

    @Override
    public int getHeight() {
        return Math.min(Math.min(x, y), Math.min(BOARD_SIZE - x - 1, BOARD_SIZE - y - 1));
    }

    public String toString() {
        return stone == Stone.WHITE ? "W" : (stone == Stone.BLACK ? "B" : "_");
    }
}
