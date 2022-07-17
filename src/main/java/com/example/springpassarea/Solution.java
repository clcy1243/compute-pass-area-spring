package com.example.springpassarea;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created at 2022/7/8 16:15
 *
 * @author will
 * @version 1.0
 */
public class Solution {
    // 精度倍数
    public static int SCALE = 100;
    public static boolean isEnableSinglePointMapMerge = true;
    public static Map<String, Boolean> pointCheckCache = new HashMap<>();

    public static List<List<List<Integer>>> computePassArea(List<Input> inputs) {
        pointCheckCache.clear();
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
            if (input.pass && matrix.getRound(input.x, input.y).stream().filter(x -> x != null).anyMatch(x -> !x.pass)) {
                if (passMapList.isEmpty()) {
                    // new map
                    PassMap m = new PassMap(input, matrix);

                    boolean finish = false;

                    do {
                        finish = m.growth();
                    } while (!finish);

                    m.removePointAtLine();
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
                    m.removePointAtLine();
                    passMapList.add(m);
                }
            }
        }
        for (PassMap passMap : passMapList) {
            // 分为两部，第一步去掉边上的点，第二步减少顶点
            passMap.removePointAtLine();
            passMap.removePointCompute(passMapList);
        }
        List<PassMap> newPassMapList = new ArrayList<>();
        for (PassMap passMap : passMapList) {
            // 分为两部，第一步去掉边上的点，第二步减少顶点
            // 减少顶点前需要判断多边形是否有完全重合的线，且连接两部分图形的，如果有则删除，此时会生成新的图

            // 第一找到一个重复存在的点，且 index 差值大于2，
            // 第二找到这个点的下一个点是否也是重复存在切index 差值大于2 的点
            for (int i = 0; i < passMap.edges.size(); i++) {
                int lastIndex = passMap.edges.lastIndexOf(passMap.edges.get(i));
                if (lastIndex - i > 2 && passMap.edges.lastIndexOf(passMap.edges.get(i+1)) - i-1 > 2) {
                    // 此线需要附加条件，
                    //  1. 线上不能有pass点
                    //  2. 线的两侧也应该是重合线
                    //Input p1 = passMap.edges.get(i);
                    //Input p2 = passMap.edges.get(i+1);
                    //Line line = Line.createFromInput(p1,p2);
                    //if (!line.getMiddlePoint().isEmpty()) {
                    //    continue;
                    //}
                    //Input fp1 = passMap.edges.get(i==0 ? passMap.edges.size() -1 : i-1);
                    //Input fp2 = passMap.edges.get(lastIndex+1 >= passMap.edges.size() ? 0: lastIndex+1);
                    //Line fl1 = Line.createFromInput(p1,fp1);
                    //Line fl2 = Line.createFromInput(p1,fp2);
                    //
                    //Input bp1 = passMap.edges.get(i+2);
                    //Input bp2 = passMap.edges.get(lastIndex-2);
                    //Line bl1 = Line.createFromInput(p2,bp1);
                    //Line bl2 = Line.createFromInput(p2,bp2);
                    //
                    //
                    //if (!fl1.equals(fl2) || !bl1.equals(bl2)) {
                    //    continue;
                    //}

                    // 此时 要把edge 分成两个list，第一个 0-i + lastIndex+1 - size -1
                    // 第二个 i+2 - lastIndex -1
                    List<Input> map1 = new ArrayList<>();
                    List<Input> map2 = new ArrayList<>();

                    for (int j = 0; j < passMap.edges.size(); j++) {
                        if (j <= i || j > lastIndex) {
                            map1.add(passMap.edges.get(j));
                        } else if (j >= i+1 && j < lastIndex - 1) {
                            map2.add(passMap.edges.get(j));
                        }
                    }

                    passMap.edges = map1;

                    PassMap newPassMap = new PassMap(map2.get(0), matrix);
                    newPassMap.edges = map2;

                    newPassMapList.add(newPassMap);
                    break; // todo 目前只做一次分割
                }
            }
        }
        passMapList.addAll(newPassMapList);

        for (int i = 0; i < newPassMapList.size(); i++) {
            PassMap passMap = newPassMapList.get(i);
            for (int j = 0; j < passMapList.size(); j++) {
                PassMap map = passMapList.get(j);
                if (map.equals(passMap)) {
                    break;
                }
                int count = 0;
                for (Input p : passMap.edges) {
                    if (PassMap.contains(map.edges, p)) {
                        count++;
                    }
                }
                if (count > passMap.edges.size() / 2) {
                    passMapList.remove(passMap);
                }
            }
        }

        return passMapList.stream()
                .filter(x -> x.edges.size() > 2)
                .map(x -> x.toList())
                .collect(java.util.stream.Collectors.toList());

        // 合并单点图

        //List<PassMap> multiPointMap = new ArrayList<>();
        //List<PassMap> singlePointMap = new ArrayList<>();
        //
        //for (PassMap passMap : passMapList) {
        //    if (passMap.edges.size() > 1) {
        //        multiPointMap.add(passMap);
        //    } else {
        //        singlePointMap.add(passMap);
        //    }
        //}
        //
        //if (isEnableSinglePointMapMerge && singlePointMap.size() > 1) {
        //    for (int i = 0; i < singlePointMap.size() - 1; i++) {
        //        for (int j = i + 1; j < singlePointMap.size(); j++) {
        //            Input p1 = singlePointMap.get(i).edges.get(0);
        //            Input p2 = singlePointMap.get(j).edges.get(0);
        //            Line line = new Line(p1.x, p1.y, p2.x, p2.y);
        //
        //            if (line.getMiddlePoint().isEmpty()) {
        //                singlePointMap.remove(j);
        //                singlePointMap.remove(i);
        //                i--;
        //
        //                PassMap newMap = new PassMap(p1, matrix);
        //                newMap.edges.add(p2);
        //
        //                multiPointMap.add(newMap);
        //                break;
        //            }
        //        }
        //
        //        if (singlePointMap.size() == 1) {
        //            break;
        //        }
        //    }
        //}
        //
        //return Stream.of(multiPointMap, singlePointMap).flatMap(Collection::stream)
        //        .map(x -> x.toList()).collect(Collectors.toList());
    }

    /**
     * todo 目前这个算法因为精度问题，如果点在边上会存在判读不准确的情况，所以在下面用到的时候，补充了点在边上的判断
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

        //private List<Input> inputs;
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
                Line line = Line.createFromInput(p1,p2);
                if (line.contains(point.x, point.y)) {
                    return true;
                }
            }
            int[] p = new int[]{point.x*SCALE, point.y*SCALE};
            int[][] list = inputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            return pointInPolygon(list, p); // todo
        }

        // 以连续三点为基准计算是否可以去掉中间一点
        // 同理可以计算中间两点或中间三点（针对一个 pass 凸起需要多三条边的情）
        public static boolean removePointCompute3(Matrix matrix, List<Input> inputs, int i1, int i2, int i3) {

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
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            List<Line> oldLines = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                int j = i+1;
                if (j == inputs.size()) {
                    j = 0;
                }
                Input pa = inputs.get(i);
                Input pb = inputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                oldLines.add(line);
            }

            List<Input> newInputs = new ArrayList<>(inputs);

            newInputs.remove(i2);
            int[][] newMap = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLines = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                newLines.add(line);
            }

            // 防止线交叉，则需要进行凹凸变化判断，暂时不能取代上面的全部边的点位变化判断
            if (p1 != p3 && inputs.indexOf(p2) != inputs.lastIndexOf(p2) && maxY - minY > 0 && maxX - minX > 0) { // 可以成为矩形
                // 取得方向
                Input pp1 = null;
                //pp1.pass = true;
                Input pp2 = null;
                //pp2.pass = true;
                if (p3.y > p1.y || (p3.y==p3.x && p3.x >= p1.x)) {
                    // 从下至上，从左至右的线，取 p1 -> p3 -> 右上 -> 右下 成图
                    //pp1.x = maxX;
                    //pp1.y = maxY;
                    pp1 = matrix.get(maxX, maxY);
                    pp2 = matrix.get(maxX, minY);

                    //pp2.x = maxX;
                    //pp2.y = minY;
                } else {
                    // 从上至下，从右至左的线，取 p1 -> p3 -> 左下 -> 左上 成图
                    //pp1.x = minX;
                    //pp1.y = minY;
                    pp1 = matrix.get(minX, minY);
                    pp2 = matrix.get(minX, maxY);

                    //pp2.x = minX;
                    //pp2.y = maxY;
                }
                List<Line> partLines = Arrays.asList(
                        Line.createFromInput(p1, p3),
                        Line.createFromInput(p3, pp1),
                        Line.createFromInput(pp1, pp2),
                        Line.createFromInput(pp2, p1)
                );
                int[][] partMap = new int[][]{
                        {p1.x*SCALE, p1.y*SCALE},
                        {p3.x*SCALE, p3.y*SCALE},
                        {pp1.x*SCALE, pp1.y*SCALE},
                        {pp2.x*SCALE, pp2.y*SCALE},
                };
                boolean inNewMap = linesContainsPoint(newLines, p2.x, p2.y) || Solution.pointInPolygon(newMap, new int[]{p2.x*SCALE, p2.y*SCALE});
                boolean inNewPart = linesContainsPoint(partLines, p2.x, p2.y) || Solution.pointInPolygon(partMap, new int[]{p2.x*SCALE, p2.y*SCALE});
                // 如 inNewPart 为 false， 说明是凹进去的
                // 此时 inNewMap 为 true 的可能会造成交叉，应阻止此次变化
                if (!inNewPart && inNewMap) {
                    return false;
                }
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
            int addScore = computeAddScore(p1==p3 ?  -2 : -1, inNewFail - inOldFail, inNewPass - inOldPass);
                    //(p1==p3 ?  6 : 3) // 一条边的分数, 假设 p1 p3 相同，实际上会少两条边
                    //+ (inNewFail - inOldFail) * -10 // 多的 fail 点的分数
                    //+ (inNewPass - inOldPass) * 5; // 多的pass点的分数
            return addScore>=0;
            //if (inNewPass < inOldPass) {
            //    return false;
            //}
            //if (inNewFail > inOldFail + 3) {
            //    return false;
            //}
            //return true;
        }

        // 顺时针增点
        public static boolean extendLineCompute(Matrix matrix, List<Input> inputs, int currentIndex,
                List<PassMap> passMapList) {
            Input p1 = inputs.get(currentIndex);
            Input p2 = inputs.get((currentIndex+1)%inputs.size());
            Input p3 = inputs.get((currentIndex+2)%inputs.size());
            Line currentLine = Line.createFromInput(p1, p2);
            int[] nextPoint = currentLine.getNextPoint(matrix.maxX);
            if (nextPoint[0] < 0) {
                return false;
            }

            Input po = matrix.get(nextPoint[0], nextPoint[1]);
            if (po == null || !po.pass) {
                return false;
            }

            for (PassMap passMap : passMapList) {
                if (passMap.edges != inputs) {
                    if (PassMap.contains(passMap.edges, po)) {
                        return false;
                    }
                }
            }

            // 1. 以 p1 p2 p3 三点所占用的空间，得到一个矩形，
            int minX = po.x;
            int minY = po.y;
            int maxX = po.x;
            int maxY = po.y;

            for (Input p : Arrays.asList(p1,p2,p3)) {
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }

            // 2.1 拿到矩形内所有的点
            List<int[]> points = new ArrayList<>();
            for (int m = minX; m <= maxX; m++) {
                for (int n = minY; n <= maxY; n++) {
                    points.add(new int[]{m,n});
                }
            }

            // po 是仅替换掉 p2 还是同时替换掉 p3，需要同时计算三个图形
            // case 1 p1 -> p2 -> p3
            // case 2 p1 -> po -> p3
            // case 3 p1 -> po

            // 2.2 构建新旧图，由于精度问题，在边上的点和在图内的点需要分别计算
            // case 1 p1 -> p2 -> p3
            int[][] oldMap = inputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            List<Line> oldLines = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                int j = i+1;
                if (j == inputs.size()) {
                    j = 0;
                }
                Input pa = inputs.get(i);
                Input pb = inputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                oldLines.add(line);
            }

            List<Input> newInputs = new ArrayList<>(inputs);

            // case 2 p1 -> po -> p3
            newInputs.set((currentIndex+1)%inputs.size(), po);

            int[][] newMap = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLines = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                newLines.add(line);
            }

            // case 2 p1 -> po
            newInputs.remove((currentIndex+2)%inputs.size());
            int[][] newMapWithoutP3 = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLinesWithoutP3 = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                newLinesWithoutP3.add(line);
            }

            boolean isNewHasCross = Line.hasCross(newLines);
            boolean isNewWithoutP3HasCross = Line.hasCross(newLinesWithoutP3);

            // 3. 分别在保留 p2 和 去掉 p2 的情况下进行点是否在图内的遍历
            int inOldPass = 0;
            int inOldFail = 0;
            int inNewPass = 0;
            int inNewFail = 0;
            int inNewPassWithoutP3 = 0;
            int inNewFailWithoutP3 = 0;
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
                boolean inNew = !isNewHasCross && (linesContainsPoint(newLines, point[0], point[1]) || Solution.pointInPolygon(newMap, cPoint));
                boolean inNewWithoutP3 = !isNewWithoutP3HasCross && (linesContainsPoint(newLinesWithoutP3, point[0], point[1]) || Solution.pointInPolygon(newMapWithoutP3, cPoint));

                if (isPass) {
                    if (inOld) {
                        inOldPass++;
                    }
                    if (inNew) {
                        inNewPass++;
                    }
                    if (inNewWithoutP3) {
                        inNewPassWithoutP3++;
                    }
                } else {
                    if (inOld) {
                        inOldFail++;
                    }
                    if (inNew) {
                        inNewFail++;
                    }
                    if (inNewWithoutP3) {
                        inNewFailWithoutP3++;
                    }
                }
            }

            int addScore = computeAddScore(0, inNewFail - inOldFail, inNewPass - inOldPass);
            int addScoreWithoutP3 = computeAddScore(-1, inNewFailWithoutP3 - inOldFail, inNewPassWithoutP3 - inOldPass);

            if (addScoreWithoutP3 >= 0 && addScoreWithoutP3 >= addScore) {
                // case 3 p1 -> po
                inputs.set((currentIndex+1)%inputs.size(), po);
                inputs.remove((currentIndex+2)%inputs.size());
                return true;
            } else if (addScore >= 0) {
                // case 2 p1 -> po -> p3
                inputs.set((currentIndex+1)%inputs.size(), po);
                return true;
            } else {
                return false;
            }
        }

        // 逆时针增点
        public static boolean extendLineComputeRes(Matrix matrix, List<Input> inputs, int currentIndex,
                List<PassMap> passMapList) {
            Input p1 = inputs.get(currentIndex);
            int p2Index = (currentIndex-1 + inputs.size())%inputs.size();
            int p3Index = (currentIndex-2 + inputs.size())%inputs.size();
            Input p2 = inputs.get(p2Index);
            Input p3 = inputs.get(p3Index);
            Line currentLine = Line.createFromInput(p1, p2);
            int[] nextPoint = currentLine.getNextPoint(matrix.maxX);
            if (nextPoint[0] < 0) {
                return false;
            }

            Input po = matrix.get(nextPoint[0], nextPoint[1]);
            if (po == null || !po.pass) {
                return false;
            }

            for (PassMap passMap : passMapList) {
                if (passMap.edges != inputs) {
                    if (PassMap.contains(passMap.edges, po)) {
                        return false;
                    }
                }
            }

            // 1. 以 p1 p2 p3 三点所占用的空间，得到一个矩形，
            int minX = po.x;
            int minY = po.y;
            int maxX = po.x;
            int maxY = po.y;

            for (Input p : Arrays.asList(p1,p2,p3)) {
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }

            // 2.1 拿到矩形内所有的点
            List<int[]> points = new ArrayList<>();
            for (int m = minX; m <= maxX; m++) {
                for (int n = minY; n <= maxY; n++) {
                    points.add(new int[]{m,n});
                }
            }

            // po 是仅替换掉 p2 还是同时替换掉 p3，需要同时计算三个图形
            // case 1 p1 -> p2 -> p3
            // case 2 p1 -> po -> p3
            // case 3 p1 -> po

            // 2.2 构建新旧图，由于精度问题，在边上的点和在图内的点需要分别计算
            // case 1 p1 -> p2 -> p3
            int[][] oldMap = inputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            List<Line> oldLines = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                int j = i+1;
                if (j == inputs.size()) {
                    j = 0;
                }
                Input pa = inputs.get(i);
                Input pb = inputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                oldLines.add(line);
            }

            List<Input> newInputs = new ArrayList<>(inputs);

            // case 2 p1 -> po -> p3
            newInputs.set(p2Index, po);

            int[][] newMap = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLines = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                newLines.add(line);
            }

            // case 2 p1 -> po
            newInputs.remove(p3Index);
            int[][] newMapWithoutP3 = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLinesWithoutP3 = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                newLinesWithoutP3.add(line);
            }

            boolean isNewHasCross = Line.hasCross(newLines);
            boolean isNewWithoutP3HasCross = Line.hasCross(newLinesWithoutP3);

            // 3. 分别在保留 p2 和 去掉 p2 的情况下进行点是否在图内的遍历
            int inOldPass = 0;
            int inOldFail = 0;
            int inNewPass = 0;
            int inNewFail = 0;
            int inNewPassWithoutP3 = 0;
            int inNewFailWithoutP3 = 0;
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
                boolean inNew = !isNewHasCross && (linesContainsPoint(newLines, point[0], point[1]) || Solution.pointInPolygon(newMap, cPoint));
                boolean inNewWithoutP3 = !isNewWithoutP3HasCross && (linesContainsPoint(newLinesWithoutP3, point[0], point[1]) || Solution.pointInPolygon(newMapWithoutP3, cPoint));

                if (isPass) {
                    if (inOld) {
                        inOldPass++;
                    }
                    if (inNew) {
                        inNewPass++;
                    }
                    if (inNewWithoutP3) {
                        inNewPassWithoutP3++;
                    }
                } else {
                    if (inOld) {
                        inOldFail++;
                    }
                    if (inNew) {
                        inNewFail++;
                    }
                    if (inNewWithoutP3) {
                        inNewFailWithoutP3++;
                    }
                }
            }

            int addScore = computeAddScore(0, inNewFail - inOldFail, inNewPass - inOldPass);
            int addScoreWithoutP3 = computeAddScore(-1, inNewFailWithoutP3 - inOldFail, inNewPassWithoutP3 - inOldPass);

            if (addScoreWithoutP3 >= 0 && addScoreWithoutP3 >= addScore) {
                // case 3 p1 -> po
                inputs.set(p2Index, po);
                inputs.remove(p3Index);
                return true;
            } else if (addScore >= 0) {
                // case 2 p1 -> po -> p3
                inputs.set(p2Index, po);
                return true;
            } else {
                return false;
            }
        }

        public static int computeAddScore(int addEdgeNum, int addFailNum, int addPassNum) {
            return addEdgeNum * -3 + addFailNum * -10 + addPassNum * 5;
        }

        public static boolean removePointComputeWithPointNum(Matrix matrix, List<Input> inputs, int currentIndex, int pointNum) {
            if (pointNum < 3 || inputs.size() <= pointNum) {
                return false;
            }

            Input p1 = inputs.get(currentIndex);

            //List<Input> newEdges = new ArrayList<>(edges);
            //newEdges.remove(i2);
            // 1. 以 p1 p2 p3 三点所占用的空间，得到一个矩形，
            int minX = p1.x;
            int minY = p1.y;
            int maxX = p1.x;
            int maxY = p1.y;

            for (int j = currentIndex+1; j <currentIndex+pointNum; j++) {
                Input p = inputs.get(j%inputs.size());
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }

            // 2.1 拿到矩形内所有的点
            List<int[]> points = new ArrayList<>();
            for (int m = minX; m <= maxX; m++) {
                for (int n = minY; n <= maxY; n++) {
                    points.add(new int[]{m,n});
                }
            }

            // 2.2 构建新旧图，由于精度问题，在边上的点和在图内的点需要分别计算
            int[][] oldMap = inputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[inputs.size()][]);
            List<Line> oldLines = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) {
                int j = i+1;
                if (j == inputs.size()) {
                    j = 0;
                }
                Input pa = inputs.get(i);
                Input pb = inputs.get(j);
                Line line = Line.createFromInput(pa, pb);
                oldLines.add(line);
            }

            List<Input> newInputs = new ArrayList<>(inputs);

            java.util.stream.IntStream.rangeClosed(currentIndex + 1, currentIndex + pointNum - 2)
                    .map(x -> x % inputs.size())
                    .boxed()
                    .sorted(Comparator.reverseOrder())
                    .forEach(x -> newInputs.remove((int) x));

            int[][] newMap = newInputs.stream()
                    .map(i -> new int[]{i.x*SCALE, i.y*SCALE})
                    .collect(java.util.stream.Collectors.toList())
                    .toArray(new int[newInputs.size()][]);

            List<Line> newLines = new ArrayList<>();
            for (int i = 0; i < newInputs.size(); i++) {
                int j = i+1;
                if (j == newInputs.size()) {
                    j = 0;
                }
                Input pa = newInputs.get(i);
                Input pb = newInputs.get(j);
                Line line = Line.createFromInput(pa, pb);
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
            int addScore = computeAddScore((p1.equals(inputs.get((currentIndex+pointNum -1) % inputs.size())) ? 1 - pointNum : 2 - pointNum), inNewFail - inOldFail, inNewPass - inOldPass);
                    //(p1.equals(inputs.get((currentIndex+pointNum -1) % inputs.size())) ? (pointNum - 1) * 3 : (pointNum - 2) * 3) // 一条边的分数, 假设 p1 p2 相同，实际上会少两条边
                    //+ (inNewFail - inOldFail) * -10 // 多的 fail 点的分数
                    //+ (inNewPass - inOldPass) * 5; // 多的pass点的分数
            return addScore>=0;
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
                        //System.out.println("single point:" + inputToString(firstPoint));
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
            //System.out.println(
            //        inputToString(firstPoint) + "------" + inputToString(lastPoint) + "--"
            //                + inputToString(currentPoint) + "->" + inputToString(nextPoint));

            if (firstPoint.equals(nextPoint)) {
                return true;
            } else {
                edges.add(nextPoint);
                return false;
            }
        }

        public void removePointAtLine() {
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
                boolean couldRemoveP2 = false;
                Line line = Line.createFromInput(p1, p3);
                couldRemoveP2 = line.contains(p2.x, p2.y);
                if (couldRemoveP2) {
                    // 能删除则更新存档点为p1，以 p1 为起始点再次执行
                    List<Input> newEdges = new ArrayList<>(edges);
                    newEdges.remove(i2);

                    //System.out.println("remove point:" + inputToString(p2));
                    edges = newEdges;
                    storePoint = i1;
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    // 如果再次遇到存档点，则结束处理
                    if (i1 == storePoint) {
                        break;
                    }
                }
            }

        }

        public void removePointCompute(List<PassMap> passMapList) {
            // 图形优化时，应先对边上的点进行清楚，再进行下一步的图形简化，
            // 有 case 说明简化时有可能因为边上的顶点移除和图形简化在一步内做，
            // 导致逐渐包含fail点最终导致fail点过多的问题
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
                String key = String.format("3-%d:%d->%d:%d->%d:%d", p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);

                if (!pointCheckCache.containsKey(key)) {
                    pointCheckCache.put(key, PassMap.removePointCompute3(matrix, edges, i1, i2, i3));
                }

                // p1 和 p3 连线，p2 依然在图内时，可以删除 p2
                boolean couldRemoveP2 = pointCheckCache.get(key);
                if (couldRemoveP2) {
                    // 能删除则更新存档点为p1，以 p1 为起始点再次执行
                    List<Input> newEdges = new ArrayList<>(edges);
                    newEdges.remove(i2);

                    //System.out.println("remove point:" + inputToString(p2));
                    edges = newEdges;
                    storePoint = Math.min(i1, edges.size() - 1 );;
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    // 如果再次遇到存档点，则结束处理
                    if (i1 == storePoint) {
                        // 环形遍历控制，第一遍只移除边上的点，第二遍简化图形
                        break;
                    }
                }
            }

            // todo p1-p2 延长到下一个点后，是否能够使得分更高
            storePoint = 0;
            i1 = 0;
            while (edges.size() > 4) {
                if (i1 >= edges.size()) {
                    i1 = 0;
                }

                Input p1 = edges.get(i1);
                Input p2 = edges.get((i1+1)%edges.size());
                Input p3 = edges.get((i1+2)%edges.size());
                String key = String.format("a-3-%d:%d->%d:%d->%d:%d", p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);

                if (!pointCheckCache.containsKey(key)) {
                    pointCheckCache.put(key, PassMap.extendLineCompute(matrix, edges, i1, passMapList));
                }

                boolean couldRemove = pointCheckCache.get(key);
                if (couldRemove) {
                    storePoint = Math.min(i1, edges.size() - 1 );
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    // 如果再次遇到存档点，则结束处理
                    if (i1 == storePoint) {
                        // 环形遍历控制，第一遍只移除边上的点，第二遍简化图形
                        break;
                    }
                }
            }
            // todo p1-p2 延长到下一个点后，是否能够使得分更高
            storePoint = 0;
            i1 = 0;
            while (edges.size() > 4) {
                if (i1 >= edges.size()) {
                    i1 = 0;
                }

                Input p1 = edges.get(i1);
                Input p2 = edges.get((i1-1 + edges.size())%edges.size());
                Input p3 = edges.get((i1-2 + edges.size())%edges.size());
                String key = String.format("a-3-%d:%d->%d:%d->%d:%d", p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);

                if (!pointCheckCache.containsKey(key)) {
                    pointCheckCache.put(key, PassMap.extendLineComputeRes(matrix, edges, i1, passMapList));
                }

                boolean couldRemove = pointCheckCache.get(key);
                if (couldRemove) {
                    storePoint = Math.min(i1, edges.size() - 1 );
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    // 如果再次遇到存档点，则结束处理
                    if (i1 == storePoint) {
                        // 环形遍历控制，第一遍只移除边上的点，第二遍简化图形
                        break;
                    }
                }
            }


            // 做延长线之后，要再次进行三点优化
            storePoint = 0;
            i1 = 0;
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
                String key = String.format("3-%d:%d->%d:%d->%d:%d", p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);

                if (!pointCheckCache.containsKey(key)) {
                    pointCheckCache.put(key, PassMap.removePointCompute3(matrix, edges, i1, i2, i3));
                }

                // p1 和 p3 连线，p2 依然在图内时，可以删除 p2
                boolean couldRemoveP2 = pointCheckCache.get(key);
                if (couldRemoveP2) {
                    // 能删除则更新存档点为p1，以 p1 为起始点再次执行
                    List<Input> newEdges = new ArrayList<>(edges);
                    newEdges.remove(i2);

                    //System.out.println("remove point:" + inputToString(p2));
                    edges = newEdges;
                    storePoint = Math.min(i1, edges.size() - 1 );;
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    // 如果再次遇到存档点，则结束处理
                    if (i1 == storePoint) {
                        // 环形遍历控制，第一遍只移除边上的点，第二遍简化图形
                        break;
                    }
                }
            }


            storePoint = 0;
            i1 = 0;
            int pointNum = 5;
            while (edges.size() > 2 && pointNum >= 3 && edges.size() >= pointNum) {
                if (i1 >= edges.size()) {
                    i1 = 0;
                }

                StringBuilder keyBuilder = new StringBuilder("5-");

                for (int j = i1; j <i1+pointNum; j++) {
                    Input p = edges.get(j%edges.size());
                    keyBuilder.append(p.x).append(":").append(p.y).append("->");
                }

                String key = keyBuilder.toString();

                if (!pointCheckCache.containsKey(key)) {
                    pointCheckCache.put(key, PassMap.removePointComputeWithPointNum(matrix, edges, i1, pointNum));
                }

                // p1 和 p3 连线，p2 依然在图内时，可以删除 p2
                boolean couldRemove = pointCheckCache.get(key);
                if (couldRemove) {
                    // 能删除则更新存档点为p1，以 p1 为起始点再次执行
                    List<Input> newEdges = new ArrayList<>(edges);

                    java.util.stream.IntStream.rangeClosed(i1 + 1, i1 + pointNum - 2)
                            .map(x -> x % edges.size())
                            .boxed()
                            .sorted(Comparator.reverseOrder())
                            //.peek(x -> System.out.println("remove point:" + inputToString(newEdges.get(x))))
                            .forEach(x -> newEdges.remove((int) x));

                    //newEdges.remove(i2);

                    //System.out.println("remove point:" + inputToString(p2));
                    edges = newEdges;
                    storePoint = Math.min(i1, edges.size() - 1 );
                    // 此处 p1 保持不变，所以 i1 不变
                } else {
                    // 不能则以 p2 为起始点继续执行
                    i1++;
                    if (i1 >= edges.size()) {
                        i1 = 0;
                    }
                    // 如果再次遇到存档点，则结束处理
                    if (i1 == storePoint) {
                        // 环形遍历控制，第一遍只移除边上的点，第二遍简化图形
                        break;
                    }
                }
            }

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
                    .collect(java.util.stream.Collectors.toList());
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

        public final boolean vector;
        private BigDecimal n; // 斜率
        private BigDecimal c; // 偏移量

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

        public static Line createFromInput(Input p1, Input p2) {
            return new Line(p1.x, p1.y, p2.x, p2.y);
        }

        public static boolean hasCross(List<Line> lines) {
            // 相邻的不用看
            for (int i = 0; i < lines.size(); i++) {
                for (int j = 0; j < lines.size(); j++) {
                    if (i==j) {
                        continue;
                    }
                    Line l1 = lines.get(i);
                    Line l2 = lines.get(j);
                    if (l1.x2 == l2.x1 && l1.y2 == l2.y1) {
                        continue;
                    }
                    if (l1.x1 == l2.x2 && l1.y1 == l2.y2) {
                        continue;
                    }
                    BigDecimal n1 = l1.getN().setScale(6, RoundingMode.HALF_UP);
                    BigDecimal n2 = l2.getN().setScale(6, RoundingMode.HALF_UP);
                    BigDecimal c1 = l1.getC().setScale(6, RoundingMode.HALF_UP);
                    BigDecimal c2 = l2.getC().setScale(6, RoundingMode.HALF_UP);

                    // case 1 如果有一方的 n 为 0
                    if (BigDecimal.valueOf(0).compareTo(n1) == 0 || BigDecimal.valueOf(0).compareTo(n2) == 0) {
                        Line la = l1;
                        Line lb = l2;
                        if (BigDecimal.valueOf(0).compareTo(n1) != 0) {
                            lb = l1;
                            la = l2;
                        }
                        // 此处 la 的 n 为 0， lb 未知

                        if (lb.getN().compareTo(BigDecimal.ZERO) == 0) {
                            if (la.x1 == la.x2) { // 竖线
                                if (lb.x1 == lb.x2) {
                                    // 同时为竖线
                                    if (la.x1 == lb.x1) {
                                        if (la.contains(lb.x1, lb.y1) || la.contains(lb.x2, lb.y2) ) {
                                            return true;
                                        }
                                    }
                                } else {
                                    // la 竖线 lb 横线
                                    // 不相交条件 竖线的x不在横线x范围内
                                    //       或  横线的y不在竖线的y范围之内
                                    if (la.x1 > Math.max(lb.x1, lb.x2) || la.x1 < Math.min(lb.x1, lb.x2)) {
                                        continue;
                                    }

                                    if (lb.y1 > Math.max(la.y1, la.y2) || lb.y1 < Math.min(la.y1, la.y2)) {
                                        continue;
                                    }
                                }
                            } else { // 横线
                                if (lb.x1 == lb.x2) {
                                    // la 横线 lb 竖线
                                    // 不相交条件 竖线的x不在横线x范围内
                                    //       或  横线的y不在竖线的y范围之内
                                    if (lb.x1 > Math.max(la.x1, la.x2) || lb.x1 < Math.min(la.x1, la.x2)) {
                                        continue;
                                    }

                                    if (la.y1 > Math.max(lb.y1, lb.y2) || la.y1 < Math.min(lb.y1, lb.y2)) {
                                        continue;
                                    }
                                } else {
                                    // 同时为横线
                                    if (la.x1 != lb.x1) {
                                        if (la.contains(lb.x1, lb.y1) || la.contains(lb.x2, lb.y2) ) {
                                            return true;
                                        }
                                    }
                                }
                            }
                            continue;
                        }

                        if (la.x1 == la.x2) { // 竖线
                            if (la.x1 > Math.max(lb.x1, lb.x2) || la.x1 < Math.min(lb.x1, lb.x2)) {
                                continue;
                            }
                            BigDecimal y = n2.multiply(BigDecimal.valueOf(la.x1)).add(c2).setScale(6, RoundingMode.HALF_UP);
                            if (y.compareTo(BigDecimal.valueOf(la.y1)) != 0 && y.compareTo(BigDecimal.valueOf(la.y1)) + y.compareTo(BigDecimal.valueOf(la.y2)) == 0) {
                                return true;
                            }
                        } else { // 横线

                            if (la.y1 > Math.max(lb.y1, lb.y2) || la.y1 < Math.min(lb.y1, lb.y2)) {
                                continue;
                            }


                            BigDecimal x = BigDecimal.valueOf(la.y1).subtract(c2).divide(lb.getN(), 6, RoundingMode.HALF_UP).setScale(6, RoundingMode.HALF_UP);

                            if (x.compareTo(BigDecimal.valueOf(la.x1)) != 0 && x.compareTo(BigDecimal.valueOf(la.x1)) + x.compareTo(BigDecimal.valueOf(la.x2)) == 0) {
                                return true;
                            }
                        }
                        continue;
                    }
                    // case 2 n2 = n1
                    if (n1.equals(n2)) {
                        if (!c1.equals(c2)) {
                            continue;
                        } else if (l1.contains(l2.x1, l2.y1)
                                || l1.contains(l2.x2, l2.y2)
                                || l2.contains(l1.x1, l1.y1)
                                || l2.contains(l1.x2, l1.y2)) {
                            return true;
                        }
                        continue;
                    }
                    // case 3
                    BigDecimal x = c2.subtract(c1).divide(n1.subtract(n2), 6, RoundingMode.HALF_UP).setScale(6, RoundingMode.HALF_UP);
                    BigDecimal y = n1.multiply(x).add(c1);
                    // n1*x+c1 = n2*x+c2
                    // x = (c2-c1) / (n1-n2)
                    if (x.compareTo(BigDecimal.valueOf(l1.x1)) + x.compareTo(BigDecimal.valueOf(l1.x2)) == 0
                    && x.compareTo(BigDecimal.valueOf(l2.x1)) + x.compareTo(BigDecimal.valueOf(l2.x2)) == 0
                    && y.compareTo(BigDecimal.valueOf(l1.y1)) + y.compareTo(BigDecimal.valueOf(l1.y2)) == 0
                    && y.compareTo(BigDecimal.valueOf(l2.y1)) + y.compareTo(BigDecimal.valueOf(l2.y2)) == 0) {
                        return true;
                    }
                }
            }
            return false;
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

        private boolean isIntegerValue(BigDecimal bd) {
            return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
        }
        public List<int[]> getMiddlePoint() {
            List<int[]> list = new ArrayList<>();
            if (x1 == x2) {
                int minY = Math.min(y1, y2);
                int maxY = Math.max(y1, y2);
                if (minY + 1 >= maxY) {
                    return list;
                }

                for (int i = minY+1;i < maxY;i++) {
                    list.add(new int[]{x1, i});
                }

            } else {
                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                if (minX + 1 >= maxX) {
                    return list;
                }
                for (int i = minX+1;i < maxX;i++) {
                    BigDecimal y = getN().multiply(BigDecimal.valueOf(i)).add(getC()).setScale(6, RoundingMode.HALF_UP);
                    if (isIntegerValue(y)) {
                        list.add(new int[]{i, y.intValue()});
                    }
                }
            }
            return list;
        }

        public int[] getNextPoint(int maxSize) {
            if (x1 == x2) {
                if (y1 > y2) {
                    return new int[]{x1, y1 + 1};
                } else if (y1 < y2) {
                    return new int[]{x1, y2 + 1};
                } else {
                    return new int[]{-1, -1};
                }
            } else if (x1 > x2) {
                for (int i = x2-1;i > -1; i--) {
                    BigDecimal y = getN().multiply(BigDecimal.valueOf(i)).add(getC()).setScale(6, RoundingMode.HALF_UP);
                    if (isIntegerValue(y)) {
                        return new int[]{i, y.intValue()};
                    }
                }
            } else {
                for (int i = x2+1;i < maxSize + 2; i++) {
                    BigDecimal y = getN().multiply(BigDecimal.valueOf(i)).add(getC()).setScale(6, RoundingMode.HALF_UP);
                    if (isIntegerValue(y)) {
                        return new int[]{i, y.intValue()};
                    }
                }
            }
            return new int[]{-1, -1};
        }

        public BigDecimal getN() {
            // n*x1 + c = y1
            // n*x2 + c = y2
            // n*(x2-x1) = y2 - y1
            // n = (y2-y1)/(x2-x1)
            if (n != null) {
                return n;
            }
            if (x1 == x2) {
                n = BigDecimal.valueOf(0);
                return n;
            }

            n = BigDecimal.valueOf(y2 - y1)
                    .divide(BigDecimal.valueOf(x2 - x1), 6, RoundingMode.HALF_UP)
                    .setScale(6, RoundingMode.HALF_UP);
            return n;
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
            if (c != null) {
                return c;
            }
            // y1 - n * x1
            c = BigDecimal.valueOf(y1).subtract(getN().multiply(BigDecimal.valueOf(x1)))
                    .setScale(6, RoundingMode.HALF_UP);
            return c;
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
