
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** 
 * Algorithm: Shortest Job First (Non-Preemptive)
 * Owner: Love Margaret Leonado
 * Conventions: 
 * Shortest burst time will go first. 
 * If the BT are the same, earlier AT will go first. 
 * If the BT and AT are the same, lower PID will go first. 
 * If there are no available process, the CPU will remain idle. 
 */


public class SJF {

    static JFrame frame = new JFrame("SJF Simulator");
    static JTextField number = new JTextField("10", 5);

    // Columns ng input at results table.
    static DefaultTableModel input = new DefaultTableModel(
        new String[]{"Process", "AT", "BT"}, 0);

    static DefaultTableModel result = new DefaultTableModel(
        new String[]{"Process", "AT", "BT", "CT", "TAT", "WT"}, 0);

    static JTable inputTable = new JTable(input);
    static JTable resultTable = new JTable(result);

    static JLabel avgWT = new JLabel("Average WT: 0.00");
    static JLabel avgTAT = new JLabel("Average TAT: 0.00");

    static JPanel gantt = new JPanel();

    static Color green = new Color(200, 217, 184);
    static Color pink = new Color(245, 223, 231);

    public static void main(String[] args) {

        // Main window ng simulator.
        frame.setSize(850, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Shortest Job First Algorithm");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);

        // Dito ung number of processes pati buttons.
        JPanel controls = new JPanel();
        controls.setBackground(Color.WHITE);

        JButton generate = new JButton("Generate");
        JButton calculate = new JButton("Calculate SJF");
        JButton reset = new JButton("Reset");

        generate.setBackground(green);
        calculate.setBackground(green);
        reset.setBackground(pink);

        controls.add(new JLabel("Number of Processes:"));
        controls.add(number);
        controls.add(generate);
        controls.add(calculate);
        controls.add(reset);

        panel.add(controls);

        // Input table para sa AT at BT.
        inputTable.setRowHeight(30);
        inputTable.getTableHeader().setBackground(green);

        panel.add(new JLabel("Process Information"));
        panel.add(new JScrollPane(inputTable));

        // Results table, hindi na editable.
        resultTable.setRowHeight(30);
        resultTable.setEnabled(false);
        resultTable.getTableHeader().setBackground(green);

        panel.add(new JLabel("Scheduling Results"));
        panel.add(new JScrollPane(resultTable));

        // Magkatabi ung dalawang averages.
        JPanel averages = new JPanel(new GridLayout(1, 2));

        avgWT.setOpaque(true);
        avgWT.setBackground(green);
        avgWT.setHorizontalAlignment(JLabel.CENTER);

        avgTAT.setOpaque(true);
        avgTAT.setBackground(pink);
        avgTAT.setHorizontalAlignment(JLabel.CENTER);

        averages.add(avgWT);
        averages.add(avgTAT);

        averages.setMaximumSize(new Dimension(1000, 50));

        panel.add(averages);

        // Horizontal layout para sa Gantt blocks.
        gantt.setBackground(Color.WHITE);
        gantt.setLayout(new BoxLayout(gantt, BoxLayout.X_AXIS));

        panel.add(new JLabel("Gantt Chart"));
        panel.add(new JScrollPane(gantt));

        // Function na tatawagin pag pinindot ung button.
        generate.addActionListener(e -> generateTable());
        calculate.addActionListener(e -> calculateSJF());
        reset.addActionListener(e -> reset());

        // May 10 processes na agad pag open.
        generateTable();

        frame.add(panel);
        frame.setVisible(true);
    }

    static void generateTable() {

        try {
            int n = Integer.parseInt(number.getText());

            // Minimum of 10, maximum of 20 processes lang.
            if (n < 10 || n > 20) {
                JOptionPane.showMessageDialog(
                    frame, "Enter 10 to 20 processes.");
                return;
            }

            // Clear muna ung previous rows at results.
            input.setRowCount(0);
            clearResults();

            // Default AT is 0, BT is 1.
            for (int i = 1; i <= n; i++) {
                input.addRow(new Object[]{"P" + i, 0, 1});
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                frame, "Enter a valid number.");
        }
    }

    static void calculateSJF() {

        // Save muna ung cell na currently ine-edit.
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

        int n = input.getRowCount();

        // Separate arrays para sa values ng bawat process.
        int[] at = new int[n];
        int[] bt = new int[n];
        int[] ct = new int[n];
        int[] tat = new int[n];
        int[] wt = new int[n];

        // Para ma-track kung tapos na ung process.
        boolean[] done = new boolean[n];

        try {

            // Get ung AT at BT from the input table.
            for (int i = 0; i < n; i++) {

                at[i] = Integer.parseInt(
                    input.getValueAt(i, 1).toString());

                bt[i] = Integer.parseInt(
                    input.getValueAt(i, 2).toString());

                // AT can be zero pero BT dapat positive.
                if (at[i] < 0 || bt[i] <= 0 ||
                    at[i] > 1000000 || bt[i] > 1000000) {

                    JOptionPane.showMessageDialog(
                        frame, "Enter valid AT and BT (up to 1000000).");
                    return;
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                frame, "Please enter valid numbers.");
            return;
        }

        clearResults();

        int time = 0;
        int completed = 0;

        double totalWT = 0;
        double totalTAT = 0;

        // Loop lang hanggang matapos lahat ng processes.
        while (completed < n) {

            // -1 muna kasi wala pang selected process.
            int shortest = -1;

            for (int i = 0; i < n; i++) {

                // Check lang ung arrived at hindi pa tapos.
                if (!done[i] && at[i] <= time) {

                    if (shortest == -1) {
                        shortest = i;
                    }

                    // Choose ung smaller burst time.
                    else if (bt[i] < bt[shortest]) {
                        shortest = i;
                    }

                    // Same burst time? Earlier arrival wins.
                    else if (bt[i] == bt[shortest]
                            && at[i] < at[shortest]) {
                        shortest = i;
                    }
                }
            }

            // Pag walang ready process, idle ung CPU.
            if (shortest == -1) {

                int next = Integer.MAX_VALUE;

                // Find ung next arrival ng unfinished process.
                for (int i = 0; i < n; i++) {
                    if (!done[i] && at[i] < next) {
                        next = at[i];
                    }
                }

                addGantt("Idle", time, next);

                // Diretso na sa next arrival time.
                time = next;
                continue;
            }

            int start = time;

            // Buong burst time agad kasi non-preemptive.
            time = time + bt[shortest];

            // CT is ung time kung kailan natapos.
            ct[shortest] = time;

            // TAT = CT minus AT.
            tat[shortest] = ct[shortest] - at[shortest];

            // WT = TAT minus BT.
            wt[shortest] = tat[shortest] - bt[shortest];

            // Add sa totals para sa averages later.
            totalWT += wt[shortest];
            totalTAT += tat[shortest];

            // Mark as done para hindi na mapili ulit.
            done[shortest] = true;
            completed++;

            // Add ung process pati start at end time.
            addGantt("P" + (shortest + 1), start, time);
        }

        // Display ung results in process order.
        for (int i = 0; i < n; i++) {

            result.addRow(new Object[]{
                "P" + (i + 1),
                at[i],
                bt[i],
                ct[i],
                tat[i],
                wt[i]
            });
        }

        // Divide ung totals by number of processes.
        // Two decimal places lang ung display.
        avgWT.setText(
            String.format("Average WT: %.2f", totalWT / n));

        avgTAT.setText(
            String.format("Average TAT: %.2f", totalTAT / n));

        // Update ung layout after mag-add ng blocks.
        gantt.revalidate();

        // Redraw ung updated Gantt chart.
        gantt.repaint();
    }

    static void addGantt(String process, int start, int end) {

        // Two rows per block, process name at time.
        JPanel block = new JPanel(new GridLayout(2, 1));

        JLabel name = new JLabel(process, JLabel.CENTER);

        JLabel duration = new JLabel(
            start + " - " + end, JLabel.CENTER);

        // Alternate lang ung green at pink.
        if (gantt.getComponentCount() % 2 == 0) {
            block.setBackground(green);
        } else {
            block.setBackground(pink);
        }

        block.setPreferredSize(new Dimension(100, 60));
        block.setMaximumSize(new Dimension(100, 60));

        block.setBorder(
            BorderFactory.createLineBorder(Color.WHITE));

        block.add(name);
        block.add(duration);

        gantt.add(block);
    }

    static void clearResults() {

        // Clear ung old results bago mag-calculate ulit.
        result.setRowCount(0);
        gantt.removeAll();

        avgWT.setText("Average WT: 0.00");
        avgTAT.setText("Average TAT: 0.00");

        gantt.revalidate();
        gantt.repaint();
    }

    static void reset() {

        // Balik lang sa default na 10 processes.
        number.setText("10");
        generateTable();
    }
}
