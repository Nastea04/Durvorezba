package durvorezba;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class WoodCuttingApp extends JFrame {

    // Интерфейс
    interface Placeable {
        int getWidth();
        int getHeight();
        String getName();
    }

    // Абстрактен клас
    abstract static class AbstractItem implements Placeable {
        protected int width, height;
        protected String name;

        public AbstractItem(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getName() { return name; }
    }

    // Детайл
    static class Detail extends AbstractItem {
        private int quantity;

        public Detail(String name, int width, int height, int quantity) {
            super(name, width, height);
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getArea() {
            return width * height;
        }

        // Нови методи за размери с ротация
        public int getWidth(boolean rotated) {
            return rotated ? height : width;
        }

        public int getHeight(boolean rotated) {
            return rotated ? width : height;
        }
    }

    // Плоскост
    static class Board extends AbstractItem {
        public Board(String name, int width, int height) {
            super(name, width, height);
        }
    }

    // Собствена динамична структура
    static class CustomList<T> {
        private Object[] data;
        private int size = 0;

        public CustomList() {
            data = new Object[10];
        }

        public void add(T item) {
            if (size == data.length) resize();
            data[size++] = item;
        }

        @SuppressWarnings("unchecked")
        public T get(int index) {
            if (index >= size || index < 0) throw new IndexOutOfBoundsException();
            return (T) data[index];
        }

        public int size() {
            return size;
        }

        private void resize() {
            Object[] newData = new Object[data.length * 2];
            for (int i = 0; i < data.length; i++)
                newData[i] = data[i];
            data = newData;
        }
    }

    // Позициониране
    static class Placement {
        public int x, y;
        public Detail detail;
        public boolean rotated;

        public Placement(Detail detail, int x, int y, boolean rotated) {
            this.detail = detail;
            this.x = x;
            this.y = y;
            this.rotated = rotated;
        }
    }


    // Backtracking
    static class Cutter {
        private Board board;
        private ArrayList<Detail> details;
        private ArrayList<Placement> placements = new ArrayList<>();
        private boolean[][] used;
        private long startTime;
        private final long TIME_LIMIT_MS = 5000; // 5 секунди

        public Cutter(Board board, ArrayList<Detail> details) {
            this.board = board;
            this.details = details;
            this.used = new boolean[board.getWidth()][board.getHeight()];
        }

        public void InsertionSort() {
            for (int i = 1; i < details.size(); ++i) {
                Detail key = details.get(i);
                int j = i - 1;
                while (j >= 0 && details.get(j).getArea() < key.getArea()) {
                    details.set(j + 1, details.get(j));
                    j = j - 1;
                }
                details.set(j + 1, key);
            }
        }

        public boolean solve() {
            InsertionSort();
            startTime = System.currentTimeMillis();
            return place(0);
        }

        private boolean place(int index) {
            if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS)
                return false;
            if (index >= details.size()) return true;

            Detail detail = details.get(index);

            // Опит без ротация
            for (int x = 0; x <= board.getWidth() - detail.getWidth(false); x++) {
                for (int y = 0; y <= board.getHeight() - detail.getHeight(false); y++) {
                    if (canPlace(x, y, detail.getWidth(false), detail.getHeight(false))) {
                        markUsed(x, y, detail.getWidth(false), detail.getHeight(false), true);
                        placements.add(new Placement(detail, x, y, false));

                        if (place(index + 1)) return true;

                        markUsed(x, y, detail.getWidth(false), detail.getHeight(false), false);
                        placements.remove(placements.size() - 1);
                    }
                }
            }

            // Опит с ротация, само ако ширината != височина
            if (detail.getWidth(false) != detail.getHeight(false)) {
                for (int x = 0; x <= board.getWidth() - detail.getWidth(true); x++) {
                    for (int y = 0; y <= board.getHeight() - detail.getHeight(true); y++) {
                        if (canPlace(x, y, detail.getWidth(true), detail.getHeight(true))) {
                            markUsed(x, y, detail.getWidth(true), detail.getHeight(true), true);
                            placements.add(new Placement(detail, x, y, true));

                            if (place(index + 1)) return true;

                            markUsed(x, y, detail.getWidth(true), detail.getHeight(true), false);
                            placements.remove(placements.size() - 1);
                        }
                    }
                }
            }

            return false;
        }

        private boolean canPlace(int x, int y, int w, int h) {
            if (x + w > board.getWidth() || y + h > board.getHeight())
                return false;

            for (int i = x; i < x + w; i++) {
                for (int j = y; j < y + h; j++) {
                    if (used[i][j]) return false;
                }
            }
            return true;
        }

        private void markUsed(int x, int y, int w, int h, boolean state) {
            for (int i = x; i < x + w; i++)
                for (int j = y; j < y + h; j++)
                    used[i][j] = state;
        }

        public ArrayList<Placement> getPlacements() {
            return placements;
        }
    }

    public WoodCuttingApp() {
        setTitle("Разкрой на плоскости");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupUI();
    }

    private void setupUI() {
        JPanel panel = new JPanel();
        JTextField boardWidth = new JTextField(5);
        JTextField boardHeight = new JTextField(5);
        JTextField detailName = new JTextField(5);
        JTextField detailW = new JTextField(5);
        JTextField detailH = new JTextField(5);
        JTextField detailQty = new JTextField(3);

        JButton addDetail = new JButton("Добави детайл");
        JButton start = new JButton("Стартирай");

        JTextArea output = new JTextArea(20, 50);
        JScrollPane scroll = new JScrollPane(output);

        panel.add(new JLabel("Размер на плоскостта (ширина mm):"));
        panel.add(boardWidth);
        panel.add(new JLabel("Размер на плоскостта (височина mm):"));
        panel.add(boardHeight);

        panel.add(new JLabel("Детайл - Име:"));
        panel.add(detailName);
        panel.add(new JLabel("Ширина mm:"));
        panel.add(detailW);
        panel.add(new JLabel("Височина mm:"));
        panel.add(detailH);
        panel.add(new JLabel("Брой:"));
        panel.add(detailQty);

        panel.add(addDetail);
        panel.add(start);
        panel.add(scroll);
        add(panel);

        CustomList<Detail> details = new CustomList<>();

        addDetail.addActionListener(e -> {
            try {
                String name = detailName.getText();
                int w = Integer.parseInt(detailW.getText());
                int h = Integer.parseInt(detailH.getText());
                int qty = Integer.parseInt(detailQty.getText());
                for (int i = 0; i < qty; i++) {
                    details.add(new Detail(name, w, h, 1));
                }
                output.append("Добавен детайл: " + name + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Грешка при въвеждането!");
            }
        });

        start.addActionListener(e -> {
            try {
                int bw = Integer.parseInt(boardWidth.getText());
                int bh = Integer.parseInt(boardHeight.getText());
                Board board = new Board("Плоскост", bw, bh);

                ArrayList<Detail> detailList = new ArrayList<>();
                for (int i = 0; i < details.size(); i++) {
                    detailList.add(details.get(i));
                }

                Cutter cutter = new Cutter(board, detailList);
                int confirm = JOptionPane.showConfirmDialog(this, "Да се направи ли разкроя?", "Потвърждение", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                if (cutter.solve()) {
                    int boardArea = board.getWidth() * board.getHeight();
                    int usedArea = 0;

                    for (Placement p : cutter.getPlacements()) {
                        usedArea += p.detail.getWidth(p.rotated) * p.detail.getHeight(p.rotated);
                    }

                    double percentUsed = (usedArea * 100.0) / boardArea;
                    int remainingWaste = boardArea - usedArea;

                    output.append(String.format("• Използвана площ: %d кв. mm\n", usedArea));
                    output.append(String.format("• Процент използван материал: %.2f%%\n", percentUsed));
                    output.append(String.format("• Отпадък: %d кв. mm\n", remainingWaste));

                    output.append("Решение намерено!\n");
                    for (Placement p : cutter.getPlacements()) {
                        output.append(String.format("-> %s на (%d, %d)%s\n", p.detail.getName(), p.x, p.y, p.rotated ? " (ротация)" : ""));
                    }

                    SwingUtilities.invokeLater(() -> {
                        JPanel drawingPanel = new JPanel() {
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                int scale = 3;

                                g.setColor(Color.LIGHT_GRAY);
                                g.fillRect(0, 0, board.getWidth() * scale, board.getHeight() * scale);

                                Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.CYAN};
                                int colorIndex = 0;

                                for (Placement p : cutter.getPlacements()) {
                                    g.setColor(colors[colorIndex % colors.length]);
                                    g.fillRect(p.x * scale, p.y * scale,
                                            p.detail.getWidth(p.rotated) * scale, p.detail.getHeight(p.rotated) * scale);
                                    g.setColor(Color.BLACK);
                                    g.drawRect(p.x * scale, p.y * scale,
                                            p.detail.getWidth(p.rotated) * scale, p.detail.getHeight(p.rotated) * scale);

                                    String label = p.detail.getName();
                                    if (p.rotated) label += " (R)";
                                    g.drawString(label, p.x * scale + 2, p.y * scale + 12);
                                    colorIndex++;
                                }
                            }
                        };
                        JFrame frame = new JFrame("Визуализация на разкроя");
                        frame.setSize(board.getWidth() * 3 + 50, board.getHeight() * 3 + 50);
                        frame.add(drawingPanel);
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    });

                } else {
                    output.append("Няма намерено решение за разкрой.\n");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Грешка при въвеждане на размерите.");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WoodCuttingApp app = new WoodCuttingApp();
            app.setVisible(true);
        });
    }
}
