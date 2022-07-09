package com.example.springpassarea;

import static org.junit.jupiter.api.Assertions.*;

import com.example.springpassarea.Solution.Line;
import org.junit.jupiter.api.Test;

/**
 * Created at 2022/7/8 19:36
 *
 * @author will
 * @version 1.0
 */
class SolutionTest {

    @Test
    void lineEquals() {
        Line line1 = new Line(1, 2, 2, 4);
        Line line2 = new Line(2, 4, 4, 8);

        assertTrue(line1.equals(line2));

        line1 = new Line(1, 2, 2, 4);
        line2 = new Line(2, 4, 4, 9);

        assertFalse(line1.equals(line2));

        line1 = new Line(1, 3, 3, 4);
        line2 = new Line(3, 4, 5, 5);

        assertTrue(line1.equals(line2));

        line1 = new Line(0, 1, 0, 2);
        line2 = new Line(0, 4, 0, 5);

        assertTrue(line1.equals(line2));
    }

    @Test
    void pointInPolygon() {
        int[][] map = new int[][]{
                {0, 0},
                {0, 3},
                {3, 3},
                {3, 0}
        };

        assertTrue(Solution.pointInPolygon(map, new int[]{0, 0}));
        assertTrue(Solution.pointInPolygon(map, new int[]{0, 1}));
        assertTrue(Solution.pointInPolygon(map, new int[]{0, 2}));
        assertTrue(Solution.pointInPolygon(map, new int[]{2, 2}));
        assertFalse(Solution.pointInPolygon(map, new int[]{2, 4}));
    }
}