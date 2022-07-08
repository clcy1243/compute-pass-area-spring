package com.example.springpassarea;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Created at 2022/7/8 18:32
 *
 * @author will
 * @version 1.0
 */
class InputReaderTest {

    @Test
    void readFromFile() {
        List<Input> a = InputReader.readFromFile("cases/1.in");

        int lastX = -1;
        for (Input point : a) {
            if (lastX != point.x) {
                System.out.print("\n");
                lastX = point.x;
            }

            System.out.print((point.pass ? "@" : "_") + " ");

        }

        System.out.print("\n");
    }
}