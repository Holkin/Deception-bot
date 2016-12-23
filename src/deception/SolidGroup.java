package deception;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Group attach(Point point, GoBoard board) {
        int x = point.getPosX();
        int y = point.getPosY();
        SolidGroup group = new SolidGroup(color);
        group.edges.addAll(edges);
        group.points.addAll(points);
        Point freePoint = newPoint(x, y, Stone.EMPTY);

        if (this.edges.contains(freePoint) && this.color != point.stone() && (point.stone() != Stone.EMPTY)) {
            // reduce dame
            group.edges.remove(freePoint);
            return group;
        }

        group.points.add(point);
        group.edges.addAll(board.getFreeNeighbours(x,y));
        if (this.edges.contains(freePoint) && this.color == point.stone()) {
            // merge with same color
            return merge(group, board);
        }
        if (Stone.EMPTY == point.stone()) {
            for (Point p : board.getNeighbours(x,y)) {
                if (points.contains(p)){
                    edges.add(point);
                }
            }
        }
        return this;
    }

    @Override
    public Group merge(Group group, GoBoard board) {
        SolidGroup newGroup = new SolidGroup(color);
        newGroup.edges.addAll(group.getEdges());
        newGroup.edges.addAll(edges);
        newGroup.points.addAll(group.getPoints());
        newGroup.points.addAll(points);
        return this;
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
        return String.format("Group(points=%d, edges=%d, x=%d, y=%d, color=%s)", points.size(), edges.size(), points.get(0).getPosX(), points.get(0).getPosY(), color.toString().substring(0,1));
    }
}
