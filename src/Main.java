import java.io.*;
import java.util.*;

// 主应用程序类
public class Main {
    private Map<String, Map<String, Integer>> graph; // 图的表示，邻接表

    // 构造方法，初始化图
    public Main() {
        graph = new HashMap<>();
    }

    // 从文件加载图的方法
    public void loadFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String prevWord = null; // 前一个单词

            // 逐行读取文件内容
            while ((line = reader.readLine()) != null) {
                // 去除非字母字符并转为小写，按空格分割单词
                String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.isEmpty()) continue; // 跳过空字符串

                    graph.putIfAbsent(word, new HashMap<>()); // 如果图中没有这个单词，添加

                    if (prevWord != null) {
                        // 更新前一个单词到当前单词的边权重
                        graph.get(prevWord).put(word, graph.get(prevWord).getOrDefault(word, 0) + 1);
                    }
                    prevWord = word; // 更新前一个单词为当前单词
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 生成Dot文件内容的方法
    public String generateDotFileContent() {
        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph G {\n");
        for (String src : graph.keySet()) {
            for (String dest : graph.get(src).keySet()) {
                // 添加图的边信息
                dotContent.append(String.format("\"%s\" -> \"%s\" [label=\"%d\"];\n", src, dest, graph.get(src).get(dest)));
            }
        }
        dotContent.append("}\n");
        return dotContent.toString();
    }

    // 查询桥接词的方法
    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        Set<String> bridges = new HashSet<>();
        // 查找从word1出发并且到达word2的中间单词
        for (String middle : graph.get(word1).keySet()) {
            if (graph.get(middle).containsKey(word2)) {
                bridges.add(middle);
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }
        return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridges) + ".";
    }

    // 生成新文本的方法
    public String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().split("\\s+");
        StringBuilder newText = new StringBuilder();
        Random rand = new Random();

        // 逐词处理，查找桥接词并添加到新文本中
        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]).append(" ");
            Set<String> bridges = new HashSet<>();
            if (graph.containsKey(words[i])) {
                for (String middle : graph.get(words[i]).keySet()) {
                    if (graph.get(middle).containsKey(words[i + 1])) {
                        bridges.add(middle);
                    }
                }
            }
            if (!bridges.isEmpty()) {
                String bridge = (String) bridges.toArray()[rand.nextInt(bridges.size())];
                newText.append(bridge).append(" ");
            }
        }
        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    // 获取最短路径列表的方法
    public Object[] calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return null;
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        pq.add(new Node(word1, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentNode = current.name;
            int currentDist = current.cost;

            if (currentDist > distances.get(currentNode)) continue;

            for (Map.Entry<String, Integer> neighbor : graph.get(currentNode).entrySet()) {
                int newDist = currentDist + neighbor.getValue();
                if (newDist < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDist);
                    previous.put(neighbor.getKey(), currentNode);
                    pq.add(new Node(neighbor.getKey(), newDist));
                }
            }
        }

        if (distances.get(word2) == Integer.MAX_VALUE) {
            return null;
        }

//        List<String> path = new ArrayList<>();
//        for (String at = word2; at != null; at = previous.get(at)) {
//            path.add(at);
//        }
//        Collections.reverse(path);
        List<String> path = new ArrayList<>(); // 存储最短路径
        int shortestLength = 0;  // 初始化最短路径长度
        for (String at = word2; at != null; at = previous.get(at)) {
            path.add(at);  // 从终点到起点逆序添加节点，即得到最短路径
        }
        Collections.reverse(path);  // 反转路径，变成起点到终点的顺序

        // 计算最短路径的长度
        for (int i = 0; i < path.size() - 1; i++) {
            String startNode = path.get(i);
            String endNode = path.get(i + 1);
            shortestLength += graph.get(startNode).get(endNode);
        }

        System.out.println(shortestLength);
//        return path;
        return new Object[]{path, shortestLength};
    }

    // 生成带高亮路径的Dot文件内容的方法
    public String generateDotFileContentWithHighlightedPath(List<String> path, int shortestLength) {
        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph G {\n");
        for (String src : graph.keySet()) {
            for (String dest : graph.get(src).keySet()) {
                // 判断边是否在路径中并高亮显示
                if (path != null && path.contains(src) && path.contains(dest) && path.indexOf(dest) == path.indexOf(src) + 1) {
                    dotContent.append(String.format("\"%s\" -> \"%s\" [label=\"%d\", color=\"red\", penwidth=2.0];\n", src, dest, graph.get(src).get(dest)));
                } else {
                    dotContent.append(String.format("\"%s\" -> \"%s\" [label=\"%d\"];\n", src, dest, graph.get(src).get(dest)));
                }
            }
        }
//        dotContent.append("}\n");
        dotContent.append("label=\"Shortest Path Length: " + shortestLength + "\";}\n");

        return dotContent.toString();
    }

    // 随机游走的方法
    public String randomWalk() {
        if (graph.isEmpty()) return "Graph is empty!";
        List<String> nodes = new ArrayList<>(graph.keySet());
        Random rand = new Random();
        String start = nodes.get(rand.nextInt(nodes.size()));
        StringBuilder walk = new StringBuilder(start);

        Set<String> visitedEdges = new HashSet<>();
        String current = start;

        // 随机选择边进行游走
        while (true) {
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors.isEmpty()) break;
            List<String> neighborList = new ArrayList<>(neighbors.keySet());
            String next = neighborList.get(rand.nextInt(neighborList.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) break;
            visitedEdges.add(edge);

            walk.append(" -> ").append(next);
            current = next;
        }
        return walk.toString();
    }

    // 节点类，用于优先队列
    private static class Node {
        String name;
        int cost;

        Node(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }
    }
}
