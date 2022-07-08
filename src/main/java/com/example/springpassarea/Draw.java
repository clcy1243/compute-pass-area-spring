package com.example.springpassarea;

import java.util.List;

/**
 * Created at 2022/7/8 16:17
 *
 * @author will
 * @version 1.0
 */
public class Draw {

    public static String toSvg(List<Input> inputs, List<List<List<Integer>>> maps) {
        Input firstPoint = inputs.get(0);
        Input lastPoint = inputs.get(inputs.size() - 1);

        int height = (lastPoint.y + 2) * 20;
        int width = (lastPoint.x + 2) * 20;

        StringBuilder svg = new StringBuilder();
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height)
                .append("\">");

        addPoint(inputs, svg);
        addMaps(maps, svg, lastPoint);

        svg.append("</svg>");

        return svg.toString();
    }

    private static void addPoint(List<Input> inputs, StringBuilder svg) {

        Input lastPoint = inputs.get(inputs.size() - 1);
        int height = (lastPoint.y + 2) * 20;
        inputs.forEach(point -> {
            svg.append("<circle cx=\"").append((point.x + 1) * 20)
                    .append("\" cy=\"").append(height - ((point.y + 1) * 20))
                    .append("\" r=\"").append(point.pass ? "5" : "3")
                    .append("\" fill=\"").append(point.pass ? "green" : "gray")
                    .append("\" >")
                    .append(point.x).append(":").append(point.y)
                    .append("</circle>")
            ;
        });
    }

    private static void addMaps(List<List<List<Integer>>> maps, StringBuilder svg,
            Input lastPoint) {
        int height = (lastPoint.y + 2) * 20;
        maps.forEach(lines -> {
            for (int i = 0; i < lines.size(); i++) {
                svg.append("<circle cx=\"").append((lines.get(i).get(0) + 1) * 20)
                        .append("\" cy=\"").append(height - ((lines.get(i).get(1) + 1) * 20))
                        .append("\" r=\"3\" fill=\"red\" ></circle>");
            }

            for (int i = 0; i < lines.size(); i++) {
                int j = i + 1;
                if (i == lines.size() - 1) {
                    j = 0;
                }
                svg.append("<line x1=\"").append((lines.get(i).get(0) + 1) * 20)
                        .append("\" y1=\"").append(height - ((lines.get(i).get(1) + 1) * 20))
                        .append("\" x2=\"").append((lines.get(j).get(0) + 1) * 20)
                        .append("\" y2=\"").append(height - ((lines.get(j).get(1) + 1) * 20))
                        .append("\" style=\"stroke:rgb(255,0,0);stroke-width:2\" />");
            }
        });
    }
}
