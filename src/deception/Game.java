package deception;


import java.util.HashMap;

public class Game {
    private GoBoard board;
    private double blackScore = 0;
    private double whiteScore = 0;
    private int turnNumber = 0;
    private HashMap<GoBoard.Hash, Integer> previousPositions;
    private boolean isBlackTurn = true;

    public Game(GoBoard board) {
        this.board = board;
        whiteScore = 7.5;
        previousPositions = new HashMap<>();
        previousPositions.put(board.hash(), turnNumber++);
    }

    public void passMove() {
        board = board.passMove();
        passTurn();
    }

    public void playMove(int x, int y) {
        GoBoard updatedBoard = board.playMove(x, y);
        blackScore += countStones(Stone.BLACK, updatedBoard) - countStones(Stone.BLACK, this.board);
        whiteScore += countStones(Stone.WHITE, updatedBoard) - countStones(Stone.WHITE, this.board);
        koCheck(updatedBoard);
        this.board = updatedBoard;
        previousPositions.put(updatedBoard.hash(), turnNumber);
        passTurn();
    }

    public void koCheck(GoBoard newBoard) {
        GoBoard.Hash hash = newBoard.hash();
        Integer turnWithSuchPosition = previousPositions.get(hash);
        if (turnWithSuchPosition != null/* && turnWithSuchPosition == turnNumber - 2*/) {
            throw new IllegalMoveException();
        }
    }

    private int countArea(Stone color) {
        return countStones(color, this.board);
    }

    public static int countStones(Stone color, GoBoard board) {
        int k = 0;
        for (Point[] row : board.getPoints()) {
            for (Point p : row) {
                if (p.stone() == color) {
                    k++;
                }
            }
        }
        return k;
    }

    private void passTurn() {
        isBlackTurn = !isBlackTurn;
        turnNumber++;
    }

    public GoBoard getBoard() {
        return board;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public HashMap<GoBoard.Hash, Integer> getPreviousPositions() {
        return previousPositions;
    }

    public boolean isBlackTurn() {
        return isBlackTurn;
    }

    private boolean isWhiteTurn() {
        return !isBlackTurn;
    }

    public double getBlackScore() {
        return blackScore + countArea(Stone.BLACK);
    }

    public double getWhiteScore() {
        return whiteScore + countArea(Stone.WHITE);
    }
}
