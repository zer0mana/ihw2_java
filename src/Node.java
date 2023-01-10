import java.util.ArrayList;
import java.io.*;
import java.util.*;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Класс узел, обертка над File.
 */
public class Node {
    /**
     * Содержит все объекты File не директории.
     */
    private static ArrayList<Node> nodeList = new ArrayList<>();
    /**
     * Содержит ссылку на корневую папку.
     */
    private static String root;
    /**
     * Имя узла.
     */
    private String name;
    /**
     * Ссылка на лист строк текста файла узла.
     */
    private ArrayList<String> text = new ArrayList<>();
    /**
     * Ссылка на лист зависимых узлов.
     */
    private ArrayList<String> require = new ArrayList<>();

    /**
     * Кастомный компаратор для сравнения двух узлов.
     */
    private static class CustomComparator implements Comparator<Node> {
        /**
         * Непосрдественно для метод сравнения.
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return
         */
        @Override
        public int compare(Node o1, Node o2) {
            // Проверяем на наличие узла в списке связанных узлов.
            for (String s : o1.require) {
                String s1 = root + s + ".txt";
                if (o2.name.equals(s1)) {
                    return 1;
                } else {
                }
            }
            for (String s : o2.require) {
                String s1 = root + s + ".txt";
                if (o1.name.equals(s1)) {
                    return -1;
                } else {
                }
            }
            return 0;
        }
    }

    /**
     * Метод, выполняющий работу программы.
     */
    public static void doWork() {
        root = "root\\";
        // Создаем узел по корневой папке.
        // В результате рекурсивно создаются все узлы из коренового каталога.
        Node node = new Node(root);
        try {
            // Проверяем на циклическую зависимость.
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
        // Сортируем узлы.
        node.Sort();
        // Записываем итоговый текст в файл.
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
        // Выводим отсортированный список узлов.
        for (Node node1 : nodeList) {
            System.out.println(node1.name);
        }
    }

    private Node() {}

    /**
     * Констуктор узла.
     * @param path принимает на вход путь до файла в проводнике.
     */
    private Node(String path) {
        name = path;
        File file = new File(path);
        // Если директория создает новые узлы.
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                Node node = new Node(item.getPath());
            }
        }
        if(file.exists() && !file.isDirectory()) {
            // Проверям на .txt
            int dotIndex = path.lastIndexOf('.');
            String postFix = (dotIndex == -1) ? "" : path.substring(dotIndex + 1);
            if ("txt".equals(postFix)) {
                try (Scanner s = new Scanner(file).useDelimiter("\\n")) {
                    while(s.hasNext()) {
                        String line = s.next();
                        if (line.contains("require ‘") && line.indexOf('’') > 8 && line.indexOf('’') != -1) {
                            require.add(line.substring(line.indexOf('‘') + 1, line.indexOf('’')));
                        }
                        text.add(line);
                    }
                    nodeList.add(this);
                } catch (FileNotFoundException e) {
                }
            }
        }
    }

    /**
     * Проверяет список узлов на цикличность.
     */
    private static void checkNodeList() {
        for (Node node : nodeList) {
            for (int i = 0; i < node.require.size(); i++) {
                checkNodeRequire(node.require.get(i), node.require.get(i), node.require);
            }
        }
    }

    /**
     * Проверяет узел на цикличность.
     * @param path путь до текущего узла.
     * @param start начальный узел, который проверяем.
     * @param nodeRequire список зависимых начального узла.
     */
    private static void checkNodeRequire(String path, String start, ArrayList<String> nodeRequire) {
        boolean flag = false;
        Node current = new Node();
        for (Node node : nodeList) {
            if (node.name.equals(root + path + ".txt")) {
                flag = true;
                current = node;
            }
        }
        if (flag) {
            for (int i = 0; i < current.require.size(); i++) {
                String pathRequire = current.require.get(i);
                if (pathRequire.equals(start)) {
                    throw new IllegalArgumentException(pathRequire);
                } else {
                    nodeRequire.add(pathRequire);
                    checkNodeRequire(pathRequire, start, nodeRequire);
                }
            }
        } else {
            System.out.println(root + path + ".txt");
            throw new IllegalArgumentException("");
        }
    }

    /**
     * Сортировка списка узлов.
     */
    private static void Sort() {
        Collections.sort(nodeList, new CustomComparator());
    }
}
