package com.example.springpassarea;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Created at 2022/7/8 21:20
 *
 * @author will
 * @version 1.0
 */
class ThymeleafControllerTest {

    @Test
    void home() {

        String path = "cases/1.in";
        List<Input> inputs = InputReader.readFromFile(path);
        List<List<List<Integer>>> lines = Solution.computePassArea(inputs);
        //String svg = Draw.toSvg(inputs, lines);
    }
}