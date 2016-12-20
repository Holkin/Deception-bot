package deception;


import java.util.List;
import java.util.Set;

public interface Group {
    int size();
    int dame();
    boolean isInAtari();
    boolean hasPoint(int x, int y);
    Group attach(Point point, GoBoard board);
    Group merge(Group group, GoBoard board);
    List<Point> getPoints();
    Set<Point> getEdges();
    Stone getColor();
}
