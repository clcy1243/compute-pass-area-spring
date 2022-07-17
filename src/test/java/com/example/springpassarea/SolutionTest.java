package com.example.springpassarea;

import static org.junit.jupiter.api.Assertions.*;

import com.example.springpassarea.Solution.Line;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * Created at 2022/7/8 19:36
 *
 * @author will
 * @version 1.0
 */
class SolutionTest {

    Logger log = Logger.getLogger(SolutionTest.class.getName());

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
    void lineVector() {
        Line l1 = new Line(0, 0, 1, 1); // 0 - 90˚
        Line l2 = new Line(0, 0, -1, 1); // 90 - 180˚
        Line l3 = new Line(0, 0, 1, -1); // -0 - -90˚
        Line l4 = new Line(0, 0, -1, -1); // -0 - -180˚
        log.info("l1 N:" + l1.getN() + " C:" + l1.getC() + " V:" + l1.vector); // n > 0 v = false
        log.info("l2 N:" + l2.getN() + " C:" + l2.getC() + " V:" + l2.vector); // n < 0 v = true
        log.info("l3 N:" + l3.getN() + " C:" + l3.getC() + " V:" + l3.vector); // n < 0 v = false
        log.info("l4 N:" + l4.getN() + " C:" + l4.getC() + " V:" + l4.vector); // n > 0 v = true
    }

    @Test
    void lineHasCross() {
        Line l1 = new Line(67, 103, 85, 135);
        Line l2 = new Line(85, 135, 81, 127);
        Line l3 = new Line(81, 127, 79, 128);
        Line l4 = new Line(79, 128, 67, 103);

        //boolean a = Line.hasCross(Arrays.asList(l1, l2, l3, l4));
        //assertTrue(a);

        l1 = new Line(109, 34, 109, 84);
        l2 = new Line(109, 84, 108, 81);
        l3 = new Line(108, 81, 128, 45);
        l4 = new Line(128, 45, 109, 34);

        boolean b = Line.hasCross(Arrays.asList(l1, l2, l3, l4));
        assertTrue(b);
    }

    @Test
    void score() {
        Input p1 = new Input();
        Input p3 = p1;
        int inNewFail = 0;
        int inOldFail = 0;
        int inNewPass = 1;
        int inOldPass = 5;
        int addScore = (p1 == p3 ? 6 : 3 // 一条边的分数, 假设 p1 p3 相同，实际上会少两条边
                + ((inNewFail - inOldFail) * -10) // 多的 fail 点的分数
                + ((inNewPass - inOldPass) * 5));
        assertEquals(-14, addScore);
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