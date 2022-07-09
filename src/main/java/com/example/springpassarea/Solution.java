package com.example.springpassarea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created at 2022/7/8 16:15
 *
 * @author will
 * @version 1.0
 */
public class Solution {
    // 精度倍数
    public static int SCALE = 100;

    public static List<List<List<Integer>>> computePassArea(List<Input> inputs) {
        /*
        Compute the maximum pass area
        inputs: list of shmoo test cases, each case includes 2 input values and 1 result, e.g. [[0, 1, True], [0, 2, False]]
        :return: 3d list, e.g. [[[7,17], [8,13], [12,5]], [[21,12], [21,15], [20,17]]]
        */

        /*
         * 方案1 ： 收缩法，由外四边向内收缩成图
         * 方案2 ： 扩张法，由点向四周共计八个方向成图
         * 方案3 ： 包围法，找到点的四周两种点分界的方向，向两个方向成长直到形成环（暂定）；有个简单的，每次只顺时针找个方向
         *           决定改成简单的写法，更明朗
         * 难点 ： 图内图外的判断
         * 难点 ： 扩散或者收缩时如果抛弃无效方向
         */

        // step 1 变换为二维数组
        int lastX = -1;
        Matrix matrix = new Matrix(inputs);

        List<PassMap> passMapList = new ArrayList<>();
        for (Input input : inputs) {
            if (input.pass) {
                if (passMapList.isEmpty()) {
                    // new map
                    PassMap m = new PassMap(input, matrix);

                    boolean finish = false;

                    do {
                        finish = m.growth();
                    } while (!finish);

                    passMapList.add(m);

                    continue; // todo
                }

                if (passMapList.stream()
                        .noneMatch(map -> PassMap.contains(map.edges, input))) {

                //if (passMapList.isEmpty()) {

                    // new map
                    PassMap m = new PassMap(input, matrix);

                    boolean finish = false;

                    do {
                        finish = m.growth();
                    } while (!finish);

                    passMapList.add(m);
                }
            }
        }
        for (PassMap passMap : passMapList) {
            passMap.removeLineMiddlePoint();
        }

        return passMapList.stream().map(x -> x.toList()).collect(Collectors.toList());
    }

    /**
     * todo
     * Performs the even-odd-rule Algorithm to find out whether a point is in a given polygon.
     * This runs in O(n) where n is the number of edges of the polygon.
     *
     * @param polygon an array representation of the polygon where polygon[i][0] is the x Value of the i-th point and polygon[i][1] is the y Value.
     * @param point   an array representation of the point where point[0] is its x Value and point[1] is its y Value
     * @return whether the point is in the polygon (not on the edge, just turn < into <= and > into >= for that)
     */
    public static boolean pointInPolygon(int[][] polygon, int[] point) {
        //A point is in a polygon if a line from the point to infinity crosses the polygon an odd number of times
        boolean odd = false;
        // int totalCrosses = 0; // this is just used for debugging
        //For each edge (In this case for each point of the polygon and the previous one)
        for (int i = 0, j = polygon.length - 1; i < polygon.length; i++) { // Starting with the edge from the last to the first node
            //If a line from the point into infinity crosses this edge
            if (((polygon[i][1] > point[1]) != (polygon[j][1] > point[1])) // One point needs to be above, one below our y coordinate
                    // ...and the edge doesn't cross our Y corrdinate before our x coordinate (but between our x coordinate and infinity)
                    && (point[0] < (polygon[j][0] - polygon[i][0]) * (point[1] - polygon[i][1]) / (polygon[j][1] - polygon[i][1]) + polygon[i][0])) {
                // Invert odd
                // System.out.println("Point crosses edge " + (j + 1));
                // totalCrosses++;
                odd = !odd;
            }
            //else {System.out.println("Point does not cross edge " + (j + 1));}
            j = i;
        }
        // System.out.println("Total number of crossings: " + totalCrosses);
        //If the number of crossings was odd, the point is in the polygon
        return odd;
    }

    public static class Matrix {

        private final List<List<Input>> matrix = new ArrayList<>();
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        public boolean isOut(int x, int y) {
            return x > maxX || x < minX
                    || y > maxY || y < minY;
        }

        public Input get(int x, int y) {
            if (isOut(x, y)) {
                return null;
            }
            return matrix.get(x - minX).get(y - minY);
        }

        // 获取一个点周围的八个点，总是以 左边的点 开始顺时针排序, 数组长度一定是 8
        public List<Input> getRound(int x, int y) {
            List<Input> list = new ArrayList<>(8);
            list.add(get(x - 1, y)); // <
            list.add(get(x - 1, y + 1)); // <^
            list.add(get(x, y + 1)); // ^
            list.add(get(x + 1, y + 1)); // ^>
            list.add(get(x + 1, y)); // >
            list.add(get(x + 1, y - 1)); // v>
            list.add(get(x, y - 1)); // v
            list.add(get(x - 1, y - 1)); // <v
            return list;
        }

        public Matrix(List<Input> inputs) {
            int lastX = -1;
            //List<List<Input>> matrix = new ArrayList<>();
            for (Input input : inputs) {
                if (input.x != lastX) {
                    matrix.add(new ArrayList<>());
                    lastX = input.x;
                }
                matrix.get(matrix.size() - 1).add(input);
            }

            Input fistPoint = inputs.get(0);
            minX = fistPoint.x;
            minY = fistPoint.y;
            Input lastPoint = inputs.get(inputs.size() - 1);
            maxX = lastPoint.x;
            maxY = lastPoint.y;
        }

    }

    public static class PassMap {

        private List<Input> inputs;
        private final Matrix matrix;
        private List<Input> edges = new ArrayList<>();

        public PassMap(Input point, Matrix matrix) {
            this.edges.add(point);
            this.matrix = matrix;
        }

        public static boolean contains(List<Input> inputs, Input point) {

            if (inputs.contains(point)) {
                return true;
            }
            for (int i = 0; i < inputs.size(); i++) {
                int j = i+1;
                if (j == inputs.size()) {
                    j = 0;
                }
                Input p1 = inputs.get(i);
                Input p2 = inputs.get(j);
                Line line = new Line(p1.x, p1.y, p2.x, p2.y);
                if (line.contains(point.x, point.y)) {
                    return true;
                }
            }
            int[] p = new int[]{point.x*SCALE, point.y*SCALE};
            int[][] list = inputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            return pointInPolygon(list, p); // todo
        }

        public static boolean removePointCompute(Matrix matrix, List<Input> inputs, int i1, int i2, int i3) {

            Input p1 = inputs.get(i1);
            Input p2 = inputs.get(i2);
            Input p3 = inputs.get(i3);

            //List<Input> newEdges = new ArrayList<>(edges);
            //newEdges.remove(i2);
            // 1. 以 p1 p2 p3 三点所占用的空间，得到一个矩形，
            int minX = Math.min(Math.min(p1.x, p2.x), p3.x);
            int minY = Math.min(Math.min(p1.y, p2.y), p3.y);
            int maxX = Math.max(Math.max(p1.x, p2.x), p3.x);
            int maxY = Math.max(Math.max(p1.y, p2.y), p3.y);
            // 2.1 拿到矩形内所有的点
            List<int[]> points = new ArrayList<>();
            for (int i = minX; i <= maxX; i++) {
                for (int j = minY; j <= maxY; j++) {
                    points.add(new int[]{i,j});
                }
            }

            // 2.2 构建新旧图，由于精度问题，在边上的点和在图内的点需要分别计算
            int[][] oldMap = inputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            List<Line> oldLines = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                int j = i+1;
                if (j == inputs.size()) {
                    j = 0;
                }
                Input pa = inputs.get(i);
                Input pb = inputs.get(j);
                Line line = new Line(pa.x, pa.y, pb.x, pb.y);
                oldLines.add(line);
            }

            List<Input> newInputs = new ArrayList<>(inputs);

            newInputs.remove(i2);
            int[][] newMap = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLines = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = new Line(pa.x, pa.y, pb.x, pb.y);
                newLines.add(line);
            }

            // 3. 分别在保留 p2 和 去掉 p2 的情况下进行点是否在图内的遍历
            int inOldPass = 0;
            int inOldFail = 0;
            int inNewPass = 0;
            int inNewFail = 0;
            for (int[] point : points) {
                // 3.1 如果点在矩阵中不存在，则不计算
                Input mPoint = matrix.get(point[0], point[1]);
                int[] cPoint = new int[]{point[0]*SCALE, point[1]*SCALE};
                if (mPoint == null) {
                    continue;
                }
                // 3.2
                boolean isPass = mPoint.pass;
                boolean inOld = linesContainsPoint(oldLines, point[0], point[1]) || Solution.pointInPolygon(oldMap, cPoint);
                boolean inNew = linesContainsPoint(newLines, point[0], point[1]) || Solution.pointInPolygon(newMap, cPoint);
                if (isPass && inOld) { // 是pass 点且在图内
                    inOldPass++;
                } else if (isPass && !inOld) { // 是pass 点且不在图内
                    //oldScore -= 100000;
                } else if (!isPass && inOld) { // 不是pass 点但在图内
                    inOldFail ++;
                } else { // 不是pass 点且不在图内
                    //oldScore += 1;
                }

                if (isPass && inNew) { // 是pass 点且在图内
                    inNewPass++;
                } else if (isPass && !inNew) { // 是pass 点且不在图内
                    //newScore -= 100000;
                } else if (!isPass && inNew) { // 不是pass 点但在图内
                    inNewFail ++;
                } else { // 不是pass 点且不在图内
                    //newScore += 1;
                }
            }
            // 4. 新旧包含点位对比
            if (inNewPass < inOldPass) {
                return false;
            }
            if (inNewFail > inOldFail +3) {
                return false;
            }
            return true;
        }

        private static boolean linesContainsPoint(List<Line> lines, int x, int y) {
            for (Line line : lines) {
                if (line.contains(x, y)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 包围一步
         *
         * @return 成功返回 true 否则返回 false
         */
        public boolean growth() {
            Input firstPoint = edges.get(0);
            Input currentPoint = edges.get(edges.size() - 1);
            Input lastPoint = firstPoint;
            if (edges.size() > 1) {
                lastPoint = edges.get(edges.size() - 2);
            }

            // findNextPoint

            List<Input> roundPoints = matrix.getRound(currentPoint.x, currentPoint.y);

            boolean found = firstPoint.equals(currentPoint);

            Input nextPoint = null;
            int counter = 0;
            for (int i = 0; i < 8; i++) {
                if (found) {
                    int j = i + 1;
                    if (i == 7) {
                        j = 0;
                    }

                    //boolean a = Optional.ofNullable(roundPoints.get(i)).map(x -> x.pass).orElse(false);
                    boolean b = Optional.ofNullable(roundPoints.get(j)).map(x -> x.pass)
                            .orElse(false);

                    if (b) {
                        nextPoint = roundPoints.get(j);
                        break;
                    }

                    counter++;
                    if (counter > 8) {
                        System.out.println("single point:" + inputToString(firstPoint));
                        return true;
                    }

                } else {
                    if (roundPoints.get(i) != null && roundPoints.get(i).equals(lastPoint)) {
                        // 找到了上一个点，进行下一步
                        found = true;
                    }
                }
                if (i == 7) {
                    i = -1;
                }
            }
            System.out.println(
                    inputToString(firstPoint) + "------" + inputToString(lastPoint) + "--"
                            + inputToString(currentPoint) + "->" + inputToString(nextPoint));

            if (firstPoint.equals(nextPoint)) {
                return true;
            } else {
                edges.add(nextPoint);
                return false;
            }
        }

        public void removeLineMiddlePoint() {

            int storePoint = 0;
            int i1 = 0;
            while (edges.size() > 2) {
                if (i1 >= edges.size()) {
                    i1 = 0;
                }
                // 取临近三个点
                int i2 = i1+1;
                if (i2 >= edges.size()) {
                    i2 = 0;
                }
                int i3 = i2+1;
                if (i3 >= edges.size()) {
                    i3 = 0;
                }

                Input p1 = edges.get(i1);
                Input p2 = edges.get(i2);
                Input p3 = edges.get(i3);

                // p1 和 p3 连线，p2 依然在图内时，可以删除 p2
                if (PassMap.removePointCompute(matrix, edges, i1, i2, i3)) { // todo
                    // 能删除则更新存档点为p1，以 p1 为起始点再次执行
                    List<Input> newEdges = new ArrayList<>(edges);
                    newEdges.remove(i2);

                    System.out.println("remove point:" + inputToString(p2));
                    edges = newEdges;
                    storePoint = i1;
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    if (i1 == storePoint) {
                        break;
                    }
                }
                // 如果再次遇到存档点，则结束处理
            }

            //if (edges.size() > 2) {
            //
            //
            //    List<Integer> waitRemove = new ArrayList<>();
            //    for (int i = 0; i < edges.size() - 2; i++) {
            //        // p1 和 p3 连线，p2 依然在图内时，可以删除 p2
            //
            //        Input p1 = edges.get(i);
            //        Input p2 = edges.get(i + 1);
            //        Input p3 = edges.get(i + 2);
            //
            //        Line line1 = new Line(p1.x, p1.y, p2.x, p2.y);
            //        Line line2 = new Line(p2.x, p2.y, p3.x, p3.y);
            //
            //        if (line1.equals(line2)) {
            //            waitRemove.add(i + 1);
            //        }
            //    }
            //    if (waitRemove.isEmpty()) {
            //        return false;
            //    } else {
            //        waitRemove.sort(Comparator.reverseOrder());
            //        waitRemove.forEach(i -> {
            //            System.out.println("remove point:" + inputToString(edges.get(i)));
            //            edges.remove((int) i);
            //        });
            //        return true;
            //    }
            //}
            //return false;
        }

        private String inputToString(Input input) {
            if (input == null) {
                return "";
            }
            return input.x + ":" + input.y;
        }

        public List<List<Integer>> toList() {
            return edges.stream()
                    .map(p -> Arrays.asList(p.x, p.y))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 线可以描述为 n * x + c = y
     */
    public static class Line {

        private final int x1;
        private final int y1;
        private final int x2;
        private final int y2;

        private final boolean vector;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;

            if (x1 == x2) {
                vector = y1 > y2;
            } else {
                vector = x1 > x2;
            }
        }

        public boolean contains(int x, int y) {
            Line newLine = new Line(x1, y1, x, y);
            if (!newLine.equals(this))  {
                return false;
            }
            if (
                    ((x1 <= x && x <= x2) || (x1 >= x && x >= x2)) // x 在x1 x2 之间
                && ((y1 <= y && y <= y2) || (y1 >= y && y >= y2)) // y 在 y1 y2 之间
            ) {
                return true;
            }
            return false;
        }

        public BigDecimal getN() {
            if (x1 == x2) {
                return BigDecimal.valueOf(0);
            }

            return BigDecimal.valueOf(y2 - y1)
                    .divide(BigDecimal.valueOf(x2 - x1), 6, RoundingMode.HALF_UP)
                    .setScale(6, RoundingMode.HALF_UP);
            //
            //if (x1 < x2) {
            //    return BigDecimal.valueOf(y2 - y1)
            //            .divide(BigDecimal.valueOf(x2 - x1), 6, RoundingMode.HALF_UP)
            //            .setScale(6, RoundingMode.HALF_UP);
            //} else {
            //    return BigDecimal.valueOf(y1 - y2)
            //            .divide(BigDecimal.valueOf(x1 - x2), 6, RoundingMode.HALF_UP)
            //            .setScale(6, RoundingMode.HALF_UP);
            //}
        }

        public BigDecimal getC() {
            // y1 - n * x1
            return BigDecimal.valueOf(y1).subtract(getN().multiply(BigDecimal.valueOf(x1)))
                    .setScale(6, RoundingMode.HALF_UP);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj instanceof Line) {
                if (((Line) obj).getN().equals(this.getN()) && ((Line) obj).vector == this.vector) {
                    return this.getN().equals(BigDecimal.ZERO) || ((Line) obj).getC()
                            .equals(this.getC());
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Line{" + x1 + ":" + y1 + "->" + x2 + ":" + y2 + ","
                    + " N:" + getN().toString() + " C:" + getC().toString() +
                    '}';
        }
    }
}
