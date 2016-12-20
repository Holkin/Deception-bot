package deception;

import java.util.*;
import java.util.stream.Collectors;

import static deception.BoardFactory.newGroup;
import static deception.BoardFactory.newPoint;

public class GoBoard {
    public static final int BOARD_SIZE = 19;
    private final Point[][] points;
    private boolean isBlackTurn = true;
    private final Set<Group> groups = new HashSet<>();

    public GoBoard(Point[][] points, Set<Group> groups) {
        this.points = points;
        this.groups.addAll(groups);
    }

    private Point get(int x, int y) {
        x = (x + BOARD_SIZE) % BOARD_SIZE;
        y = (y + BOARD_SIZE) % BOARD_SIZE;
        return points[x][y];
    }

    public List<Point> getNeighbours(int x, int y) {
        int x1 = BOARD_SIZE - x - 1;
        int y1 = BOARD_SIZE - y - 1;
        if (x == 0) {
            // top row
            if (y == 0) {
                // top left corner
                return Arrays.asList(points[x + 1][y], points[x][y + 1]);
            } else if (y1 == 0) {
                // top right corner
                return Arrays.asList(points[x + 1][y], points[x][y - 1]);
            } else {
                return Arrays.asList(points[x + 1][y], points[x][y - 1], points[x][y + 1]);
            }
        } else if (x1 == 0) {
            // bottom row
            if (y == 0) {
                // bottom left corner
                return Arrays.asList(points[x - 1][y], points[x][y + 1]);
            } else if (y1 == 0) {
                // bottom right corner
                return Arrays.asList(points[x - 1][y], points[x][y - 1]);
            } else {
                return Arrays.asList(points[x - 1][y], points[x][y - 1], points[x][y + 1]);
            }
        } else if (y == 0) {
            // left side
            return Arrays.asList(points[x - 1][y], points[x][y + 1], points[x + 1][y]);
        } else if (y1 == 0) {
            // right side
            return Arrays.asList(points[x - 1][y], points[x][y - 1], points[x + 1][y]);
        }
        return Arrays.asList(points[x - 1][y], points[x][y - 1], points[x][y + 1], points[x + 1][y]);
    }

    public List<Point> getFreeNeighbours(int x, int y) {
        return getNeighbours(x, y).stream().filter(p -> p.stone() == Stone.EMPTY).collect(Collectors.toList());
    }

    public GoBoard playMove(int x, int y) {
        if (get(x, y).stone() != Stone.EMPTY) {
            return passMove();
//            throw new IllegalMoveException();
        }
        Point[][] points = copyPoints(this);
        points[x][y] = newPoint(x, y, isBlackTurn ? Stone.BLACK : Stone.WHITE);
        GoBoard newBoard = new GoBoard(points, groups);
        List<Point> neighbours = newBoard.getNeighbours(x, y);
        Set<Group> dead = new HashSet<>();
        Set<Group> outDated = new HashSet<>();
        Set<Group> temp = new HashSet<>();
        Group mergeWith = null;
        Stone myColor = isBlackTurn ? Stone.BLACK : Stone.WHITE;
        Stone opponentColor = isBlackTurn ? Stone.WHITE : Stone.BLACK;
//        System.out.println("my = "+myColor+" op = "+opponentColor);
        boolean createNewGroup = true;
        for (Point nearPoint : neighbours) {
            int nx = nearPoint.getPosX();
            int ny = nearPoint.getPosY();
            if (nearPoint.stone() == opponentColor) {
                // opponent group
                for (Group opponentGroup : groups) {
                    if (opponentGroup.getColor() != opponentColor || !opponentGroup.hasPoint(nx,ny)) {
                        continue;
                    }
                    Group updated = opponentGroup.attach(points[x][y], newBoard);
                    if (updated.dame() == 0) {
                        dead.add(opponentGroup);
                    } else {
                        temp.add(updated);
                        outDated.add(opponentGroup);
                    }
                }
            } else {
                // my group
                for (Group myGroup : groups) {
                    if (myGroup.getColor() != myColor|| !myGroup.hasPoint(nx,ny)) {
                        continue;
                    }
                    if (mergeWith == null) {
                        mergeWith = myGroup.attach(points[x][y], newBoard);
                        outDated.add(myGroup);
                        temp.add(mergeWith);
                    } else {
                        temp.remove(mergeWith);
                        mergeWith = myGroup.merge(mergeWith, newBoard);
                        outDated.add(myGroup);
                        temp.add(mergeWith);
                    }
                    createNewGroup = false;
                }

            }
        }
        if (createNewGroup) {
            newBoard.groups.add(newGroup(points[x][y], newBoard));
        }
        // kill dead
        Set<Point> updateTrigger = new HashSet<>();
        if (!dead.isEmpty()) {
//            System.out.println("killdead");
            // clear board
            for (Group deadGroup : dead) {
                for (Point p : deadGroup.getPoints()) {
                    int x1 = p.getPosX();
                    int y1 = p.getPosY();
                    points[x1][y1] = newPoint(x1, y1, Stone.EMPTY);
                    updateTrigger.add(points[x1][y1]);
                }
            }
            // update groups
            for (Group deadGroup : dead) {
                for (Point p : deadGroup.getPoints()) {
                    int x1 = p.getPosX();
                    int y1 = p.getPosY();
                    for (Group g : newBoard.groups) {
                        g.attach(points[x1][y1], newBoard);
                    }
                }
                newBoard.groups.remove(deadGroup);
            }
        }
//        System.out.println(newBoard.groups.size()+" "+temp.size()+" "+outDated.size()+" ping");
        newBoard.groups.addAll(temp);
        newBoard.groups.removeAll(outDated);

        for (Point triggerPoint : updateTrigger) {
            ArrayList<Group> updated = new ArrayList<>();
            for (Group group : newBoard.groups) {
                updated.add(group.attach(triggerPoint, newBoard));
            }
            newBoard.groups.clear();
            newBoard.groups.addAll(updated);
        }

//
//        boolean createNewGroup = true;
//        Group mergeWith = null;
//        Set<Group> temp = new HashSet<>(groups.size());
//        Set<Group> forbidden = new HashSet<>(groups.size());
//        for (Point n : neighbours) {
//            for (Group g : groups) {
//                if (g.hasPoint(n.getPosX(), n.getPosY())) {
//                    if (g.getColor() == Stone.WHITE ^ isBlackTurn) {
//                        // try to kill
//                        if (g.getEdges().isEmpty()) {
//                            newBoard = killGroup(g, newBoard);
//                        }
//                        continue;
//                    }
//                    createNewGroup = false;
//                    if (mergeWith == null) {
//                        mergeWith = g.attach(points[x][y], newBoard);
//                        forbidden.add(g);
//                    }
//                    else {
//                        temp.remove(mergeWith);
//                        temp.remove(g);
//                        forbidden.add(mergeWith);
//                        forbidden.add(g);
//                        mergeWith = mergeWith.merge(g.attach(points[x][y], newBoard), newBoard);
//                    }
//                    temp.add(mergeWith);
//                } else {
//                    temp.add(g);
//                }
//            }
//        }
//        temp.removeAll(forbidden);
//        list.addAll(temp);
//        if (createNewGroup) {
//            list.add(newGroup(points[x][y], newBoard));
//        }
//        return processBoard(new GoBoard(points, list));
        return processBoard(newBoard);
    }

    public GoBoard passMove() {
        GoBoard newBoard = new GoBoard(points, groups);
        newBoard.isBlackTurn = !isBlackTurn;
        return newBoard;
    }

    public List<Point> candidateMoves() {
        ArrayList<Point> candidateMoves = new ArrayList<>(BOARD_SIZE * BOARD_SIZE);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (points[i][j].stone() == Stone.EMPTY) {
                    candidateMoves.add(points[i][j]);
                }
            }
        }
        return candidateMoves;
    }

    public Set<Group> groups() {
        return groups;
    }

    public byte[] hash() {
        int hashLen = BOARD_SIZE * BOARD_SIZE / 4;
        byte[] hash = new byte[hashLen];
        for (int i = 0; i < hashLen; i++) {
            int k = 0;
            for (int j = 0; j < 4; j++) {
                int offset = i * 4 + j;
                k *= 4;
                k += points[offset / BOARD_SIZE][offset % BOARD_SIZE].stone().ordinal();
            }
            hash[i] = (byte) k;
        }
        return hash;
    }

    private GoBoard processBoard(GoBoard newBoard) {
//        Set<Group> dead = new HashSet<>();
//        for (Group g : newBoard.groups) {
//            if (g.dame() == 0) {
//                if ((newBoard.isBlackTurn && g.getColor() == Stone.BLACK) || (!newBoard.isBlackTurn && g.getColor() == Stone.WHITE)) {
//                    throw new IllegalMoveException();
//                }
//                Point[][] newPoints = copyPoints(newBoard);
//                for (Point deadPoint : g.getPoints()) {
//                    int x = deadPoint.getPosX();
//                    int y = deadPoint.getPosY();
//                    newPoints[x][y] = newPoint(x, y, Stone.EMPTY);
//                    for (Point n : newBoard.getNeighbours(x, y)) {
//                        newBoard.groups.stream().filter(group -> group.hasPoint(n.getPosX(), n.getPosY())).forEach(group -> {
//                            group.getEdges().add(newPoints[x][y]);
//                        });
//                    }
//                }
//                dead.add(g);
//            }
//        }
//        newBoard.groups.removeAll(dead);
        if (!newBoard.isValid()) {
            throw new IllegalMoveException();
        }
        newBoard.isBlackTurn = !isBlackTurn;
        return newBoard;
    }

    private static Point[][] copyPoints(GoBoard src) {
        Point[][] points = new Point[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(src.points[i], 0, points[i], 0, BOARD_SIZE);
        }
        return points;
    }

    private boolean isValid() {
        // TODO
        if (!groups.stream().filter(g -> g.dame() == 0).collect(Collectors.toList()).isEmpty()){
            // suicidal move
            throw new IllegalMoveException();
        }
        return !GlobalState.previousPositions.containsKey(hash()); // super ko
    }

    public Point[][] getPoints() {
        return points;
    }

    public void print() {
        System.out.println("hash=" + Arrays.toString(hash()));
        String headerLine = "+";
        String patternLine = "|";
        for (int i = 0; i < BOARD_SIZE; i++) {
            headerLine += "---+";
            patternLine += " %s |";
        }
        System.out.println(headerLine);
        int i = 0;
        for (Point[] pointsRow : points) {
            System.out.println(String.format(patternLine + " " + i++,
                    pointsRow[0], pointsRow[1], pointsRow[2], pointsRow[3], pointsRow[4],
                    pointsRow[5], pointsRow[6], pointsRow[7], pointsRow[8], pointsRow[9],
                    pointsRow[10], pointsRow[11], pointsRow[12], pointsRow[13], pointsRow[14],
                    pointsRow[15], pointsRow[16], pointsRow[17], pointsRow[18]));
            System.out.println(headerLine);
        }
    }
}
