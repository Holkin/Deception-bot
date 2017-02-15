package deception;

import java.util.*;
import java.util.stream.Collectors;

import static deception.BoardFactory.newGroup;
import static deception.BoardFactory.newPoint;

public class GoBoard {
    public static final int BOARD_SIZE = 19;
    private final Point[][] points;
    private boolean isBlackTurn = true;
    private final Set<Group> groups;

    public GoBoard(Point[][] points, Set<Group> groups) {
        this.points = points;
        this.groups = groups.stream().map(g -> new SolidGroup((SolidGroup) g)).collect(Collectors.toSet());
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

    public List<Point> getNotMyColorNeighbours(int x, int y) {
        return getNeighbours(x, y).stream().filter(p -> p.stone() != getMyColor()).collect(Collectors.toList());
    }

    public List<Group> getNeighbourGroups(Stone color, int x, int y) {
        List<Point> neighbours = getFreeNeighbours(x, y);
        return groups.stream().filter(g -> {
            for (Point p: neighbours) {
                if (g.getEdges().contains(p)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    public GoBoard playMove(int x, int y) {
        Point spot = get(x, y);
        if (spot.stone() != Stone.EMPTY) {
            return passMove();
//            throw new IllegalMoveException();
        }
        Point[][] points = copyPoints(this);
        Stone myColor = getMyColor();
        points[x][y] = newPoint(x, y, myColor);
        GoBoard newBoard = new GoBoard(points, groups);
        Set<Group> dead = new HashSet<>();
        Set<Group> outDated = new HashSet<>();
        Set<Group> temp = new HashSet<>();
        Group mergeWith = null;
        Stone opponentColor = isBlackTurn ? Stone.WHITE : Stone.BLACK;
        boolean createNewGroup = true;

        // ==== update groups ====
        for (Group group : newBoard.groups) {
            if (group.getEdges().contains(spot)) {
                if (opponentColor == group.getColor()) {
                    Group updated = group.attach(points[x][y], newBoard);
                    if (updated.dame() == 0) {
                        dead.add(group);
                    } else {
                        temp.add(updated);
                        outDated.add(group);
                    }
                }
            }
        }
        // kill dead opponent group
        handleDeadGroups(points, newBoard, dead);

        for (Group group : newBoard.groups) {
            if (group.getEdges().contains(spot)) {
                if (myColor == group.getColor()) {
                    if (mergeWith == null) {
                        mergeWith = group.attach(points[x][y], newBoard);
                        outDated.add(group);
                        temp.add(mergeWith);
                    } else {
                        temp.remove(mergeWith);
                        mergeWith = group.merge(mergeWith, newBoard, spot);
                        outDated.add(group);
                        temp.add(mergeWith);
                    }
                    createNewGroup = false;
                }
            }
        }
        // ==== update groups end ====

        if (createNewGroup) {
            newBoard.groups.add(newGroup(points[x][y], newBoard));
        }
//        System.out.println(newBoard.groups.size()+" "+temp.size()+" "+outDated.size()+" ping");
        newBoard.groups.addAll(temp);
        newBoard.groups.removeAll(outDated);

        return processBoard(newBoard);
    }

    private void handleDeadGroups(Point[][] points, GoBoard newBoard, Set<Group> dead) {
        Set<Point> updateTrigger = new HashSet<>();
        Set<Point> clearPoints = new HashSet<>();
        if (!dead.isEmpty()) {
            // calc update points
            for (Group deadGroup : dead) {
                for (Point p : deadGroup.getPoints()) {
                    int x1 = p.getPosX();
                    int y1 = p.getPosY();
                    updateTrigger.addAll(newBoard.getNeighbours(x1, y1));
                    clearPoints.add(points[x1][y1]);
                }
            }
            // clear board
            for (Point p : clearPoints) {
                int x1 = p.getPosX();
                int y1 = p.getPosY();
                newBoard.points[x1][y1] = newPoint(x1,y1,Stone.EMPTY);
            }
            // update groups
//            for (Group deadGroup : dead) {
//                for (Point p : deadGroup.getPoints()) {
//                    int x1 = p.getPosX();
//                    int y1 = p.getPosY();
//                    for (Group g : newBoard.groups) {
//                        g.attach(points[x1][y1], newBoard);
//                    }
//                }
//                newBoard.groups.remove(deadGroup);
//            }
            newBoard.groups.removeAll(dead);
        }

        ArrayList<Group> newGroups = new ArrayList<>();
        for (Group group : newBoard.groups) {
            Group updated = group;
            for (Point p : updateTrigger) {
                if (updated.getPoints().contains(p)){
                    for (Point freePoint : newBoard.getFreeNeighbours(p.getPosX(), p.getPosY())) {
                        updated = updated.attach(freePoint, newBoard);
                    }
                }
            }
            newGroups.add(updated);
        }
        newBoard.groups.clear();
        newBoard.groups.addAll(newGroups);

//        for (Point triggerPoint : updateTrigger) {
//            ArrayList<Group> updated = new ArrayList<>();
//            for (Group group : newBoard.groups) {
//                if (group.getPoints().contains(triggerPoint)) {
//                    updated.add(group.attach(triggerPoint, newBoard));
//                }
//                else {
//                    updated.add(group);
//                }
//            }
//            newBoard.groups.clear();
//            newBoard.groups.addAll(updated);
//        }
    }

    private Stone getMyColor() {
        return isBlackTurn ? Stone.BLACK : Stone.WHITE;
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

    public Hash hash() {
        int pointsCount = BOARD_SIZE * BOARD_SIZE;
        int hashLen = (pointsCount +3) / 4;
        byte[] hash = new byte[hashLen];
        for (int i = 0; i < hashLen; i++) {
            int k = 0;
            for (int j = 0; j < 4; j++) {
                int offset = Math.min(pointsCount-1, i * 4 + j);
                k *= 4;
                k += points[offset / BOARD_SIZE][offset % BOARD_SIZE].stone().ordinal();
            }
            hash[i] = (byte) k;
        }
        return new Hash(hash);
    }

    private GoBoard processBoard(GoBoard newBoard) {
        if (!newBoard.isValid()) {
            throw new IllegalMoveException(newBoard.groups.stream().filter(g -> g.dame() == 0).collect(Collectors.toList()).get(0).toString());
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
            return false;
        }
        return true; // super ko
    }

    public Point[][] getPoints() {
        return points;
    }

    public void print() {
//        System.out.println("hash=" + hash());
//        String headerLine = "+";
//        String patternLine = "|";
//        for (int i = 0; i < BOARD_SIZE; i++) {
//            headerLine += "---+";
//            patternLine += " %s |";
//        }
        String patternLine = "";
        for (int i = 0; i < BOARD_SIZE; i++) {
            patternLine += " %s";
        }
        int i = 0;
        for (Point[] pointsRow : points) {
            System.out.println(String.format(patternLine + " " + i++,
                    pointsRow[0], pointsRow[1], pointsRow[2], pointsRow[3], pointsRow[4],
                    pointsRow[5], pointsRow[6], pointsRow[7], pointsRow[8], pointsRow[9],
                    pointsRow[10], pointsRow[11], pointsRow[12], pointsRow[13], pointsRow[14],
                    pointsRow[15], pointsRow[16], pointsRow[17], pointsRow[18]));
//            System.out.println(headerLine);
        }
        System.out.println(" 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8");
    }

    public static class Hash {
        private byte[] hash;

        public Hash(byte[] hash) {
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Hash)) {
                return false;
            }
            return Arrays.equals(hash, ((Hash) obj).hash);
        }

        @Override
        public String toString() {
            return Arrays.toString(hash);
        }
    }
}
