package test;


import deception.*;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class TestBoard {
    @Test
    public void testUpperLeftCorner() {
        GoBoard board = BoardFactory.newBoard();
        board = board.playMove(0,0);

        assertEquals(1, board.groups().size());
        Group group = board.groups().iterator().next();
        assertEquals(Stone.BLACK, group.getColor());
        assertEquals(1, group.size());
        assertEquals(2, group.dame());

        board = board.passMove().playMove(0,1);

        assertEquals(1, board.groups().size());
        group = board.groups().iterator().next();
        assertEquals(Stone.BLACK, group.getColor());
        assertEquals(2, group.size());
        assertEquals(3, group.dame());

        board = board.passMove().playMove(1,0);

        assertEquals(1, board.groups().size());
        group = board.groups().iterator().next();
        assertEquals(Stone.BLACK, group.getColor());
        assertEquals(3, group.size());
        assertEquals(3, group.dame());

        board = board.passMove().playMove(1,1);

        assertEquals(1, board.groups().size());
        group = board.groups().iterator().next();
        assertEquals(Stone.BLACK, group.getColor());
        assertEquals(4, group.size());
        assertEquals(4, group.dame());

        board = board.playMove(0,2);
        assertEquals(2, board.groups().size());
        Iterator<Group> it = board.groups().iterator();
        group = it.next();
        Group opGroup = it.next();
        if (group.getColor() == Stone.WHITE) {
            // swap
            Group tmp = group;
            group = opGroup;
            opGroup = tmp;
        }
        assertEquals(3, group.dame());
        assertEquals(2, opGroup.dame());

        board = board.passMove().playMove(1,2);
        assertEquals(2, board.groups().size());
        it = board.groups().iterator();
        group = it.next();
        opGroup = it.next();
        if (group.getColor() == Stone.WHITE) {
            // swap
            Group tmp = group;
            group = opGroup;
            opGroup = tmp;
        }
        assertEquals(2, group.dame());
        assertEquals(3, opGroup.dame());

        board = board.passMove().playMove(2,2).passMove().playMove(2,1);
        assertEquals(2, board.groups().size());
        it = board.groups().iterator();
        group = it.next();
        opGroup = it.next();
        if (group.getColor() == Stone.WHITE) {
            // swap
            Group tmp = group;
            group = opGroup;
            opGroup = tmp;
        }
        assertEquals(1, group.dame());
        assertEquals(6, opGroup.dame());

        board = board.passMove().playMove(3,1).playMove(2,0).playMove(3,0);
        assertEquals(1, board.groups().size());
        group = board.groups().iterator().next();
        assertEquals(9, group.dame());

        board.print();
    }

    @Test
    public void testUpperSide() {
        GoBoard board = BoardFactory.newBoard();
        board = board.playMove(0,0).playMove(0,1).playMove(0,2).passMove();
        long whiteGroupCount = board.groups().stream().filter(g -> g.getColor() == Stone.WHITE).count();
        assertEquals(1L, whiteGroupCount);
        board = board.playMove(1,1);
        whiteGroupCount = board.groups().stream().filter(g -> g.getColor() == Stone.WHITE).count();
        assertEquals(0L, whiteGroupCount);

        board.print();
    }

    @Test
    public void testUpperSideWithMerge() {
        GoBoard board = BoardFactory.newBoard();
        board = board.playMove(0,0).playMove(1,0).playMove(0,2).playMove(1,2).passMove().playMove(0,3).playMove(0,1);
        long blackGroups = board.groups().stream().filter(g -> g.getColor() == Stone.BLACK).count();
        assertEquals(1L, blackGroups);
        board = board.playMove(1,1);
        blackGroups = board.groups().stream().filter(g -> g.getColor() == Stone.BLACK).count();
        assertEquals(0L, blackGroups);

        board.print();
    }

    @Test (expected = IllegalMoveException.class)
    public void testSuicidal() {
        GoBoard board = BoardFactory.newBoard();
        board = board.playMove(0,0).playMove(1,0).playMove(0,2).playMove(1,2).passMove().playMove(0,3).passMove();
        long blackGroups = board.groups().stream().filter(g -> g.getColor() == Stone.BLACK).count();
        assertEquals(2L, blackGroups);
        board = board.playMove(1,1);
        blackGroups = board.groups().stream().filter(g -> g.getColor() == Stone.BLACK).count();
        assertEquals(2L, blackGroups);

        board.playMove(0,1); // suicidal
    }

    @Test
    public void testPreKo() {
        GoBoard board = BoardFactory.newBoard();
        board = board.playMove(2,1).playMove(2,4);
        board = board.playMove(1,2).playMove(1,3);
        board = board.playMove(3,2).playMove(3,3);
        board = board.playMove(2,3);
        board = board.playMove(2,2);
        board = board.playMove(2,3);
        board.print();
    }
}
