package com.example.springpassarea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.servlet.tags.form.InputTag;

/**
 * Created at 2022/7/8 16:15
 *
 * @author will
 * @version 1.0
 */
public class Solution {

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
                for (PassMap passMap : passMapList) {
                    if (!passMap.contains(input)) {
                        // new map
                        PassMap m = new PassMap(input, matrix);

                        boolean finish = false;

                        do {
                            finish = m.growth();
                        } while (finish);

                        passMapList.add(m);
                    }
                    break;
                }
            }
        }

        return passMapList.stream().map(x -> x.toList()).collect(Collectors.toList());
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
            list.add(get(x , y + 1)); // ^
            list.add(get(x + 1, y + 1)); // ^>
            list.add(get(x + 1, y)); // >
            list.add(get(x + 1, y - 1)); // v>
            list.add(get(x, y - 1)); // v
            list.add(get(x -1, y - 1)); // <v
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
        private final List<Input> edges = new ArrayList<>();

        public PassMap(Input point, Matrix matrix) {
            this.edges.add(point);
            this.matrix = matrix;
        }

        public boolean contains(Input point) {
            return true; // todo
        }

        /**
         * 包围一步
         * @return 成功返回 true 否则返回 false
         */
        public boolean growth() {
            if (edges.size() == 1) {

                Input p = edges.get(0);
                List<Input> roundPoints = matrix.getRound(p.x, p.y);
                // 从左侧顺时针找到两个方向
                List<Input> vectors = new ArrayList<>(2);
                vectors.add(null);
                vectors.add(null);
                boolean singlePoint = true;
                for (int i = 0; i < 8; i ++) {
                    int j = i+1;
                    if (i==7) {
                        j = 0;
                    }
                    boolean a = Optional.ofNullable(roundPoints.get(i)).map(x -> x.pass).orElse(false);
                    boolean b = Optional.ofNullable(roundPoints.get(j)).map(x -> x.pass).orElse(false);
                    if (a == b) {
                        continue;
                    } else {
                        // 需要依据从fail点开始的顺时针方向插入 edge
                        if (b) {
                            vectors.set(1, roundPoints.get(i));
                        } else {
                            vectors.set(0, roundPoints.get(j));
                        }

                        // 有可能不止两个，只取前两个，最终能实现全覆盖
                        if (vectors.get(0) != null && vectors.get(1) != null) {
                            singlePoint = false;
                            break;
                        }
                    }
                }

                if (singlePoint) {
                    System.out.println("this map just one point:" + p.x + ":" + p.y + "");
                    return true;
                }

                StringBuilder log = new StringBuilder("first point: ");
                log.append(vectors.get(0).x).append(":").append(vectors.get(0).y)
                        .append("<-").append(p.x).append(":").append(p.y).append("->")
                        .append(vectors.get(1).x).append(":").append(vectors.get(1).y);

                System.out.println(log);

                edges.add(0, vectors.get(0));

                // 特殊情况下，多个点会连成一条线，此时首尾相同，应该只保留顺时针方向
                if (!vectors.get(0).equals(vectors.get(1))) {
                    edges.add(vectors.get(1));
                }
            } else {
                List<Input> vectors = new ArrayList<>(2);
                Input p1 = edges.get(0);
                // 从 edges.get(1)， 顺时针找到一个方向
                List<Input> roundPoints = matrix.getRound(p1.x, p1.y);
                boolean found = false;
                for (int i = 0; i < 8; i++ ) {
                    if (found) {
                        int j = i+1;
                        if (i==7) {
                            j = 0;
                        }

                        boolean a = Optional.ofNullable(roundPoints.get(i)).map(x -> x.pass).orElse(false);
                        boolean b = Optional.ofNullable(roundPoints.get(j)).map(x -> x.pass).orElse(false);

                        if (b) {
                            vectors.add(roundPoints.get(j));
                            break;
                        }

                    } else {
                        if (roundPoints.get(i) != null && roundPoints.get(i).x == edges.get(1).x && roundPoints.get(i).y == edges.get(1).y) {
                            // 找到了上一个点，进行下一步
                            found = true;
                        }
                    }
                    if (i == 7) {
                        i = -1;
                    }
                }

                Input p2 = edges.get(edges.size() - 1);
                // 从 edges.get(edges.size() - 2)， 逆时针找到一个方向
                roundPoints = matrix.getRound(p2.x, p2.y);
                found = false;
                for (int i = 0; i < 8; i-- ) {
                    if (found) {
                        int j = i+1;
                        if (i==7) {
                            j = 0;
                        }

                        boolean a = Optional.ofNullable(roundPoints.get(i)).map(x -> x.pass).orElse(false);
                        boolean b = Optional.ofNullable(roundPoints.get(j)).map(x -> x.pass).orElse(false);

                        if (a) {
                            vectors.add(roundPoints.get(i));
                            break;
                        }

                    } else {
                        if (roundPoints.get(i) != null && roundPoints.get(i).x == edges.get(edges.size() - 2).x && roundPoints.get(i).y == edges.get(edges.size() - 2).y) {
                            // 找到了上一个点，进行下一步
                            found = true;
                        }
                    }
                    if (i == 0) {
                        i = 8;
                    }
                }

                StringBuilder log = new StringBuilder("other point: ");
                log.append(vectors.get(0).x).append(":").append(vectors.get(0).y)
                        .append("<-").append(p1.x).append(":").append(p1.y)
                        .append("---").append(p2.x).append(":").append(p2.y).append("->")
                        .append(vectors.get(1).x).append(":").append(vectors.get(1).y);

                System.out.println(log);

                edges.add(0, vectors.get(0));

                // 特殊情况下，多个点会连成一条线，此时首尾相同，应该只保留顺时针方向

                if (!vectors.get(0).equals(vectors.get(1))) {
                    // 先判断一下是否结束,如果是奇数个点，不判断的话可能不会按照预期结束
                    if (!(edges.get(0).x == edges.get(edges.size() - 1).x
                            && edges.get(0).y == edges.get(edges.size() - 1).y)) {
                        edges.add(vectors.get(1));
                    }
                }
            }


            Input first = edges.get(0);
            Input last = edges.get(edges.size() - 1);

            return first.x == last.x && first.y == last.y;
        }

        public List<List<Integer>> toList() {
            List<List<Integer>> list = new ArrayList<>(edges.size() - 1);
            if (edges.size() == 1) {
                list.add(Arrays.asList(edges.get(0).x, edges.get(0).y));
                return list;
            }
            for (int i = 0; i < edges.size() - 1; i++) {
                list.add(Arrays.asList(edges.get(i).x, edges.get(i).y));
            }
            return list;
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

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public BigDecimal getN() {
            if (x1 < x2) {
                return BigDecimal.valueOf(y2 - y1)
                        .divide(BigDecimal.valueOf(x2 - x1), RoundingMode.HALF_UP)
                        .setScale(6, RoundingMode.HALF_UP);
            } else {
                return BigDecimal.valueOf(y1 - y2)
                        .divide(BigDecimal.valueOf(x1 - x2), RoundingMode.HALF_UP)
                        .setScale(6, RoundingMode.HALF_UP);
            }
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
                return ((Line) obj).getC().equals(this.getC()) &&
                        ((Line) obj).getN().equals(this.getN());
            }
            return false;
        }

        @Override
        public String toString() {
            return "Line{" + x1 + ":" + y1 + "->" + x2 + ":" + y2 + '}';
        }
    }
}
