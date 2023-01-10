import java.util.ArrayList;
import java.io.*;
import java.util.*;
import java.util.Comparator;
import java.util.Scanner;

public class Node {
    private static ArrayList<Node> nodeList = new ArrayList<>();
    private static String root;
    private String name;
    private ArrayList<String> text = new ArrayList<>();
    private ArrayList<String> require = new ArrayList<>();

    private static class CustomComparator implements Comparator<Node> {
        @Override
        public int compare(Node o1, Node o2) {
            if (o1.require.contains(o2.name)) {
                return 1;
            }
            if (o2.require.contains(o1.name)) {
                return -1;
            }
            return 0;
        }
    }

    public static void doWork() {
        root = "root\\";
        Node node = new Node(root);
        try {
            Node.checkNodeList();
        }
        catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("")) {
                System.out.println("Проблема с require (указана ссылка на несуществующий в корневом каталоге файл.)" + ex);
            } else {
                System.out.println("Проблема с require (цилкличиская зависимость в " + ex.getMessage() + ").");
            }
            return;
        }
        node.Sort();
        try(FileWriter writer = new FileWriter("out.txt", false))
        {
            for (Node node1 : Node.nodeList) {
                for (String s : node1.text) {
                    writer.write(s + '\n');
                }
            }
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    private Node() {}
    private Node(String path) {
        name = path;
        File file = new File(path);
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                Node node = new Node(item.getPath());
            }
        }
        if(file.exists() && !file.isDirectory()) {
            int dotIndex = path.lastIndexOf('.');
            String postFix = (dotIndex == -1) ? "" : path.substring(dotIndex + 1);
            if ("txt".equals(postFix)) {
                try (Scanner s = new Scanner(file).useDelimiter("\\n")) {
                    while(s.hasNext()) {
                        String line = s.next();
                        if (line.contains("require ‘") && line.indexOf('’') > 8 && line.indexOf('’') != -1) {
                            require.add(line.substring(line.indexOf('‘') + 1, line.indexOf('’')));
                        } else {
                            text.add(line);
                        }
                    }
                    nodeList.add(this);
                } catch (FileNotFoundException e) {
                }
            }
        }
    }
    private static void checkNodeList() {
        for (Node node : nodeList) {
            for (int i = 0; i < node.require.size(); i++) {
                checkNodeRequire(node.require.get(i), node.require);
            }
        }
    }

    private static void checkNodeRequire(String path, ArrayList<String> nodeRequire) {
        boolean flag = false;
        Node current = new Node();
        for (Node node : nodeList) {
            if (node.name.equals(root + path + ".txt")) {
                flag = true;
                current = node;
            }
        }
        if (flag) {
            for (String pathRequire : current.require) {
                if (nodeRequire.contains(pathRequire)) {
                    throw new IllegalArgumentException(pathRequire);
                } else {
                    nodeRequire.add(pathRequire);
                    checkNodeRequire(pathRequire, nodeRequire);
                }
            }
        } else {
            System.out.println(root + path + ".txt");
            throw new IllegalArgumentException("");
        }
    }

    private static void Sort() {
        Collections.sort(nodeList, new CustomComparator());
    }
}
