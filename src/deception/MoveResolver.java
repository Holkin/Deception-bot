package deception;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static deception.PlayParam.NO_SECOND_LINE_MOVE_TILL_TURN;

public class MoveResolver {
    private Settings settings;
    private Random random = new Random();

    public MoveResolver(Settings settings) {
        this.settings = settings;
    }

    public Point getMove(GoBoard board, int turnNumber) {
        Point point = handleImmediateAtari(board);
        if (point != null) {
            return point;
        }
        List<Point> allMoves = board.candidateMoves();
        // filter 2nd line moves
        List<Point> candidateMoves = allMoves.parallelStream()
                .filter(move -> move.getHeight() >= 2 || turnNumber > NO_SECOND_LINE_MOVE_TILL_TURN)
                .collect(Collectors.toList());
        if (candidateMoves.isEmpty()) {
            candidateMoves = allMoves;
        }
        // filter suicide and illegal moves
        candidateMoves = candidateMoves.parallelStream().filter(move -> {
            try{
                board.playMove(move.getPosX(), move.getPosY());
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        }).collect(Collectors.toList());

        final Map<Point, Integer> bestMoves = new HashMap<>();
        if (!candidateMoves.isEmpty()) {
            bestMoves.put(candidateMoves.get(random.nextInt(candidateMoves.size())), 0);
        }
        Map.Entry<Point, Integer> bestMove = getBestMove(bestMoves);
        candidateMoves.parallelStream()
                .forEach(move -> {
                    try{
                        GoBoard nb = board.playMove(move.getPosX(), move.getPosY());
                        for (Group g : nb.groups()) {
                            if (g.isInAtari()) {
                                if (g.getColor() == settings.myColor) {
                                    return;
                                }
                                else {
                                    if (bestMove.getValue() < g.size()) {
                                        bestMoves.put(move, g.size());
                                    }
                                }
                            }
                        }
                    } catch (RuntimeException ex) {}
                });
        return bestMove == null ? null : bestMove.getKey();
//        return candidateMoves.get(random.nextInt(candidateMoves.size()));
    }

    private Point handleImmediateAtari(GoBoard board) {
        // TODO biggest atari
        for (Group g : board.groups()) {
            if (g.isInAtari()) {
                Point p = g.getEdges().iterator().next();
                try{
                    return BoardFactory.newPoint(p.getPosX(), p.getPosY(), settings.myColor);
                } catch (IllegalMoveException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    private Map.Entry<Point, Integer> getBestMove(Map<Point, Integer> map) {
        Map.Entry<Point, Integer> best = null;
        for (Map.Entry<Point, Integer> entry : map.entrySet()) {
            if (best == null) {
                best = entry;
                continue;
            }
            if (best.getValue() < entry.getValue()) {
                best = entry;
            }
        }
        return best;
    }
}
