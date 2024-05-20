import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GUI extends JFrame {
    private JTextArea textArea;  // 用于显示结果的文本区域
    private JTextField word1Field;  // 输入第一个单词的文本框
    private JTextField word2Field;  // 输入第二个单词的文本框
    private JTextField inputTextField;  // 输入新文本的文本框
    private JLabel resultLabel;  // 显示结果的标签
    private Main graphApp;  // 主应用程序类的实例

    // 构造方法，初始化GUI组件
    public GUI() {
        setTitle("Directed Graph Application");  // 设置窗口标题
        setSize(800, 600);  // 设置窗口大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 设置关闭操作
        setLocationRelativeTo(null);  // 将窗口居中

        graphApp = new Main();  // 初始化主应用程序类的实例

        // 布局设置
        setLayout(new BorderLayout());

        // 初始化用于显示结果的文本区域，并添加到窗口中心
        textArea = new JTextArea();
        textArea.setEditable(false);  // 设置文本区域为不可编辑
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // 控制面板，包含文件选择、功能按钮和输入字段
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 1));

        // 文件选择面板
        JPanel filePanel = new JPanel();
        JButton fileButton = new JButton("Select File");  // 创建文件选择按钮
        fileButton.addActionListener(new ActionListener() {  // 添加按钮点击事件监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();  // 创建文件选择器
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();  // 获取选中的文件
                    graphApp.loadFile(selectedFile.getAbsolutePath());  // 加载文件内容
                    textArea.append("File loaded: " + selectedFile.getName() + "\n");  // 显示文件加载信息
                }
            }
        });
        filePanel.add(fileButton);  // 将文件选择按钮添加到文件面板
        controlPanel.add(filePanel);  // 将文件面板添加到控制面板

        // 功能按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5));

        // 显示图按钮
        JButton showGraphButton = new JButton("Show Graph");
        showGraphButton.addActionListener(new ActionListener() {  // 添加按钮点击事件监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                showDirectedGraph();  // 调用显示图方法
            }
        });
        buttonPanel.add(showGraphButton);

        // 查询桥接词按钮
        JButton queryBridgeButton = new JButton("Query Bridge Words");
        queryBridgeButton.addActionListener(new ActionListener() {  // 添加按钮点击事件监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                queryBridgeWords();  // 调用查询桥接词方法
            }
        });
        buttonPanel.add(queryBridgeButton);

        // 生成新文本按钮
        JButton generateTextButton = new JButton("Generate New Text");
        generateTextButton.addActionListener(new ActionListener() {  // 添加按钮点击事件监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                generateNewText();  // 调用生成新文本方法
            }
        });
        buttonPanel.add(generateTextButton);

        // 最短路径按钮
        JButton shortestPathButton = new JButton("Shortest Path");
        shortestPathButton.addActionListener(new ActionListener() {  // 添加按钮点击事件监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                calcShortestPath();  // 调用计算最短路径方法
            }
        });
        buttonPanel.add(shortestPathButton);

        // 随机游走按钮
        JButton randomWalkButton = new JButton("Random Walk");
        randomWalkButton.addActionListener(new ActionListener() {  // 添加按钮点击事件监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                randomWalk();  // 调用随机游走按钮
            }
        });
        buttonPanel.add(randomWalkButton);

        controlPanel.add(buttonPanel);  // 将功能按钮面板添加到控制面板

        // 输入字段面板，用于输入 word1, word2 和新文本
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        // 第一个单词输入
        inputPanel.add(new JLabel("Word 1:"));  // 添加标签
        word1Field = new JTextField();  // 初始化文本框
        inputPanel.add(word1Field);  // 将文本框添加到面板

        // 第二个单词输入
        inputPanel.add(new JLabel("Word 2:"));  // 添加标签
        word2Field = new JTextField();  // 初始化文本框
        inputPanel.add(word2Field);  // 将文本框添加到面板

        // 新文本输入
        inputPanel.add(new JLabel("New Text:"));  // 添加标签
        inputTextField = new JTextField();  // 初始化文本框
        inputPanel.add(inputTextField);  // 将文本框添加到面板

        controlPanel.add(inputPanel);  // 将输入字段面板添加到控制面板

        add(controlPanel, BorderLayout.NORTH);  // 将控制面板添加到窗口北部

        // 初始化结果标签，并添加到窗口
        resultLabel = new JLabel();
        add(resultLabel, BorderLayout.SOUTH);
    }

    // 显示图方法
    private void showDirectedGraph() {
        try {
            String dotContent = graphApp.generateDotFileContent();  // 生成Dot文件内容
            String outputFilePath = "./graph.png";  // 输出图像文件路径
            createGraph(dotContent, outputFilePath);  // 创建图像文件
            ImageIcon graphImage = new ImageIcon(outputFilePath);  // 创建图像图标
            JLabel graphLabel = new JLabel(graphImage);  // 创建标签并设置图像图标
            JOptionPane.showMessageDialog(null, graphLabel, "Directed Graph", JOptionPane.PLAIN_MESSAGE);  // 显示图像对话框
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 创建图像文件方法
    private void createGraph(String dotContent, String outputFilePath) throws IOException, InterruptedException {
        File dotFile = File.createTempFile("graph", ".dot");  // 创建临时Dot文件
        FileWriter writer = new FileWriter(dotFile);  // 创建文件写入器
        writer.write(dotContent);  // 写入Dot内容
        writer.close();  // 关闭写入器

        // 使用Graphviz创建图像文件
        ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", dotFile.getAbsolutePath(), "-o", outputFilePath);
        Process process = processBuilder.start();  // 启动进程
        process.waitFor();  // 等待进程完成

        dotFile.delete();  // 删除临时Dot文件
    }

    // 查询桥接词方法
    private void queryBridgeWords() {
        String word1 = word1Field.getText();  // 获取第一个单词
        String word2 = word2Field.getText();  // 获取第二个单词
        String result = graphApp.queryBridgeWords(word1, word2);  // 查询桥接词
        textArea.append(result + "\n");  // 显示结果到文本区域
    }

    // 生成新文本方法
    private void generateNewText() {
        String inputText = inputTextField.getText();  // 获取输入的新文本
        String result = graphApp.generateNewText(inputText);  // 生成新文本
        textArea.append(result + "\n");  // 显示结果到文本区域
    }

    // 计算最短路径方法
    private void calcShortestPath() {
        String word1 = word1Field.getText();  // 获取第一个单词
        String word2 = word2Field.getText();  // 获取第二个单词
//        List<String> path = graphApp.calcShortestPath(word1, word2);  // 获取最短路径
        Object[] result =  graphApp.calcShortestPath(word1, word2);
        List<String> path = (List<String>) result[0];
        int shortestLength = (int) result[1];

        if (path == null) {
            textArea.append("No path from " + word1 + " to " + word2 + "!" + "\n");  // 显示无路径信息
            return;
        }

        try {
            String dotContent = graphApp.generateDotFileContentWithHighlightedPath(path, shortestLength);  // 生成带高亮路径的Dot文件内容
            String outputFilePath = "highlighted_graph.png";  // 输出图像文件路径
            createGraph(dotContent, outputFilePath);  // 创建图像文件

            ImageIcon graphImage = new ImageIcon(outputFilePath);  // 创建图像图标
            JLabel graphLabel = new JLabel(graphImage);  // 创建标签并设置图像图标
            JOptionPane.showMessageDialog(null, graphLabel, "Shortest Path", JOptionPane.PLAIN_MESSAGE);  // 显示图像对话框

            textArea.append("The shortest path from " + word1 + " to " + word2 + " is: " + String.join(" -> ", path) + ", and the total length is " + shortestLength + ".");  // 显示路径信息
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 随机漫步方法
    private void randomWalk() {
        String result = graphApp.randomWalk();  // 执行随机漫步(B1)
        textArea.append(result + "\n");  // 显示结果到文本区域
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}
