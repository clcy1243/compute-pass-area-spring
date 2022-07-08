package com.example.springpassarea;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Created at 2022/7/8 16:17
 *
 * @author will
 * @version 1.0
 */
public class InputReader {

    public static List<Input> readFromFile(String path) {

        try (InputStream is = InputReader.class.getClassLoader().getResourceAsStream(path)) {
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(reader)) {

                String line;
                boolean firstLine = true;
                String pointNum;

                List<Input> list = new ArrayList<>();

                while ((line = bufferedReader.readLine()) != null) {
                    if (firstLine) {
                        pointNum = line;
                        firstLine = false;
                    } else if (StringUtils.isNotBlank(line)) {
                        String[] a = line.split(",");
                        Input i = new Input();
                        i.x = Integer.parseInt(a[0]);
                        i.y = Integer.parseInt(a[1]);
                        i.pass = Boolean.parseBoolean(a[2]);

                        list.add(i);
                    }
                    //System.out.println(line);
                }

                return list;

            } catch (Exception ex) {
                throw new RuntimeException("");
            }

        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }
}
