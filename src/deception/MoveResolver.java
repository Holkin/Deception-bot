package deception;


import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MoveResolver {
    private static final double KILL_FACTOR = 11;
    private static final double CREATE_FACTOR = .5;
    private static final double OP_ATARI_FACTOR = 5;
    private static final double MY_ATARI_FACTOR = 9;
    private static final double REDUCED_EYE_FACTOR = 100;
    private static final double RANDOM_FACTOR = 0.2;
    private static final double FISRT_LINE_FACTOR = 0.2;
    private static final double SECOND_LINE_FACTOR = 0.7;

    public static class Branch {
        GoBoard board;
        Point move;
        double myScoreDiff;
        double opScoreDiff;
//        double totalScoreDiff;
        List<Group> myGroupsInAtari;
        List<Group> opGroupsInAtari;
        long createdGroups = 0;
        long killedGroups = 0;
        boolean reducedPotentialEye = false;

        public Branch(GoBoard board, Point move) {
            this.board = board;
            this.move = move;
        }

        public String toString() {
            return String.format("(mScDif=%3.2f, opScDif=%3.2f, mAt=%d, opAt=%d, cr=%d, k=%d, red=%s, x=%d, y=%d)",
                    myScoreDiff, opScoreDiff, myGroupsInAtari.size(), opGroupsInAtari.size(), createdGroups,
                    killedGroups, reducedPotentialEye, move.getPosX(), move.getPosY());
        }
    }

    private Settings settings;
    private Random random = new Random();

    public MoveResolver(Settings settings) {
        this.settings = settings;
    }

    public Point getMove(Game game) {
        List<Branch> branches = initCandidateMoves(game);
        filterSuicidal(branches);
        return choseBest(branches);
    }

    private Point choseBest(List<Branch> branches) {
        double bestScore = Double.NEGATIVE_INFINITY;
        Branch best = null;
        for (Branch branch : branches) {
            double score = random.nextGaussian() * RANDOM_FACTOR;
            if (branch.move.getHeight() == 0) {
                score *= FISRT_LINE_FACTOR;
            } else if (branch.move.getHeight() == 1) {
                score *= SECOND_LINE_FACTOR;
            }
            score += branch.myScoreDiff - branch.opScoreDiff * KILL_FACTOR * branch.killedGroups;
            score -= branch.myGroupsInAtari.size() * MY_ATARI_FACTOR;
            score += branch.opGroupsInAtari.size() * OP_ATARI_FACTOR;
            score -= branch.createdGroups * CREATE_FACTOR;
            score -= (branch.reducedPotentialEye && branch.killedGroups == 0) ? REDUCED_EYE_FACTOR : 0;

            if (score > bestScore) {
                bestScore = score;
                best = branch;
                log(String.format("best=%3.2f, branch=%s", bestScore, best));
            }
        }
        if (best == null || best.reducedPotentialEye) {
            return null;
        }
        return best.move;
    }

    private void filterSuicidal(List<Branch> branches) {
        branches.parallelStream()
                .filter(branch -> branch.myScoreDiff > 0);
    }

    private List<Branch> initCandidateMoves(Game game) {
        // TODO change this
        final Stone myColor = getMyColor(game);
        final Stone opponentColor = getOpponentColor(game);
        final double myScoreBefore = Game.countStones(myColor, game.getBoard());
        final double opScoreBefore = Game.countStones(opponentColor, game.getBoard());
        final long myGroups = game.getBoard().groups().stream().filter(g -> g.getColor() == myColor).count();
        final long opGroups = game.getBoard().groups().stream().filter(g -> g.getColor() == opponentColor).count();
//        final double totalScoreBefore = (game.isBlackTurn() ? 1 : -1) * (Game.countStones(Stone.BLACK, game.getBoard()) - Game.countStones(Stone.WHITE, game.getBoard()));

        return game.getBoard()
                .candidateMoves()
                .parallelStream()
                .map(move -> {
                    try {
                        int x = move.getPosX();
                        int y = move.getPosY();
                        Branch branch = new Branch(game.getBoard().playMove(x, y), move);
                        long myAtariGroups = game.getBoard().getNeighbourGroups(myColor, x, y).stream().filter(g -> g.dame() == 1).count();
                        branch.reducedPotentialEye = game.getBoard().getNotMyColorNeighbours(x, y).isEmpty() && myAtariGroups > 0;
                        return branch;
                    } catch (RuntimeException ex) {
                        log("illegal move");
                        return null;
                    }
                })
                .filter(item -> item != null)
                .filter(item -> game.getPreviousPositions().get(item.board.hash()) == null) // super ko
                .map(item -> {
                    item.myScoreDiff = Game.countStones(myColor, item.board) - myScoreBefore;
                    item.opScoreDiff = Game.countStones(opponentColor, item.board) - opScoreBefore;
                    item.myGroupsInAtari = item.board.groups().stream()
                            .filter(group -> group.getColor() == myColor && group.isInAtari())
                            .collect(Collectors.toList());
                    item.opGroupsInAtari = item.board.groups().stream()
                            .filter(group -> group.getColor() == opponentColor && group.isInAtari())
                            .collect(Collectors.toList());
                    item.createdGroups = myGroups - item.board.groups().stream()
                            .filter(g -> g.getColor() == myColor)
                            .count();
                    item.killedGroups = opGroups - item.board.groups().stream()
                            .filter(g -> g.getColor() == opponentColor)
                            .count();
//                    item.totalScoreDiff =(game.isBlackTurn() ? 1 : -1) * (Game.countStones(Stone.BLACK, item.board) - Game.countStones(Stone.WHITE, item.board)) - totalScoreBefore;
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Stone getOpponentColor(Game game) {
        return game.isBlackTurn() ? Stone.WHITE : Stone.BLACK;
    }

    private Stone getMyColor(Game game) {
        return game.isBlackTurn() ? Stone.BLACK : Stone.WHITE;
    }

    private void log(Object object) {
        if (settings.debug) {
            System.out.println(object);
        }
    }

}
