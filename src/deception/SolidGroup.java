package deception;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static deception.BoardFactory.newGroup;
import static deception.BoardFactory.newPoint;

public class SolidGroup implements Group {

    private final List<Point> points = new ArrayList<>();
    private final Set<Point> edges = new HashSet<>();
    private final Stone color;

    private SolidGroup(Stone color) {
        this.color = color;
    }
    public SolidGroup(Point point, GoBoard board) {
        this.color = point.stone();
        points.add(point);
        edges.addAll(board.getFreeNeighbours(point.getPosX(), point.getPosY()));
    }
    public SolidGroup(SolidGroup prototype) {
        this.color = prototype.color;
        points.addAll(prototype.points);
        edges.addAll(prototype.edges);
    }

    @Override
    public int size() {
        return points.size();
    }

    @Override
    public int dame() {
        return edges.size();
    }

    @Override
    public boolean isInAtari() {
        return dame() == 1;
    }

    @Override
    public boolean hasPoint(int x, int y) {
        for (Point point : points) {
            if (point.getPosX() == x && point.getPosY() == y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Group attach(Point move, GoBoard board) {
        SolidGroup group = copyGroup();
        int x = move.getPosX();
        int y = move.getPosY();
        Point spot = newPoint(x, y, Stone.EMPTY);

        if (this.edges.contains(spot) && this.color != move.stone() && (move.stone() != Stone.EMPTY)) {
            // reduce dame
            group.edges.remove(spot);
            return group;
        }

        if (this.edges.contains(spot) && this.color == move.stone()) {
            // merge with same color
            return group.merge(new SolidGroup(move, board), board, spot);
        }

//        group.points.add(move);
//        group.edges.addAll(board.getFreeNeighbours(x,y));


        // some points become free
        if (Stone.EMPTY == move.stone()) {
            for (Point p : board.getNeighbours(x,y)) {
                if (points.contains(p)){
                    edges.add(move);
                }
            }
        }
        return this;
    }

    private SolidGroup copyGroup() {
        SolidGroup group = new SolidGroup(color);
        group.edges.addAll(edges);
        group.points.addAll(points);
        return group;
    }

    @Override
    public Group merge(Group group, GoBoard board, Point mergePoint) {
        int x = mergePoint.getPosX();
        int y = mergePoint.getPosY();
        Point spot = newPoint(x, y, Stone.EMPTY);

        SolidGroup newGroup = new SolidGroup(color);
        newGroup.edges.addAll(group.getEdges());
        newGroup.edges.addAll(edges);
        newGroup.points.addAll(group.getPoints());
        newGroup.points.addAll(points);
        newGroup.edges.removeAll(points);
        newGroup.edges.remove(spot);
        return newGroup;
    }

    @Override
    public List<Point> getPoints() {
        return points;
    }

    @Override
    public Set<Point> getEdges() {
        return edges;
    }

    @Override
    public Stone getColor() {
        return color;
    }

    @Override
    public String toString() {
        if (points.isEmpty()) {
            return super.toString();
        }
        return String.format("Group(points=%d, edges=%d, x=%d, y=%d, color=%s)", points.size(), edges.size(), points.get(0).getPosX(), points.get(0).getPosY(), color.toString().substring(0,1));
    }
}
