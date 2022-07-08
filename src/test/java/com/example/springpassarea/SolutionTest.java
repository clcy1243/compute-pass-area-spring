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
    }
}