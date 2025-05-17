import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;


public class PageReplacementGUI extends JFrame {

    // GUI components
    private JTextField frameInput;
    private JTextArea outputArea;
    private JButton runButton;

    // Constructor: sets up the GUI
    public PageReplacementGUI() {
        setTitle("Page Replacement Simulator");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TOP PANEL: Input and Button
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(204, 204, 255));
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Number of Page Frames:");
        label.setForeground(new Color(60, 60, 60));
        label.setFont(new Font("Helvetica", Font.PLAIN, 16));
        inputPanel.add(label);

        frameInput = new JTextField(10);
        frameInput.setBackground(new Color(255, 245, 235));
        frameInput.setForeground(new Color(60, 60, 60));
        inputPanel.add(frameInput);

        runButton = new JButton("Run Simulation");
        runButton.setBackground(new Color(220, 220, 255));
        runButton.setForeground(new Color(60, 60, 60));
        inputPanel.add(runButton);

        add(inputPanel, BorderLayout.NORTH);

        // CENTER: Output Display Area
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setBackground(new Color(240, 250, 240));
        outputArea.setForeground(new Color(60, 60, 60)); //
        outputArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);



        // ACTION LISTENER
        runButton.addActionListener(e -> runSimulation());
    }

    // Called when the user clicks "Run Simulation"
    private void runSimulation() {
        outputArea.setText("");  // Clear previous output

        int frames;
        try {
            frames = Integer.parseInt(frameInput.getText());
        } catch (NumberFormatException ex) {
            outputArea.setText("❌ Please enter a valid number for frames.");
            return;
        }

        // Generate random reference string
        List<Integer> reference = generateReferenceString(20, 10); // 20 pages, range 0–9
        outputArea.append("📄 Page Reference String:\n" + reference + "\n\n");

        // FIFO Simulation
        outputArea.append("=== 📦 FIFO Simulation ===\n");
        int fifoFaults = fifo(reference, frames);
        outputArea.append("Total Page Faults: " + fifoFaults + "\n\n");

        // LRU Simulation
        outputArea.append("=== 📦 LRU Simulation ===\n");
        int lruFaults = lru(reference, frames);
        outputArea.append("Total Page Faults: " + lruFaults + "\n\n");

        // Optimal Simulation
        outputArea.append("=== 📦 Optimal Simulation ===\n");
        int optimalFaults = optimal(reference, frames);
        outputArea.append("Total Page Faults: " + optimalFaults + "\n\n");
    }

    // Randomly generates a page reference string of given length and value range
    private List<Integer> generateReferenceString(int length, int range) {
        Random rand = new Random();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(rand.nextInt(range));
        }
        return list;
    }

    // FIFO algorithm
    private int fifo(List<Integer> ref, int frameCount) {
        List<Integer> frames = new ArrayList<>(frameCount);
        List<List<String>> frameHistory = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            frameHistory.add(new ArrayList<>());
            frames.add(null); // initialize empty frames
        }
    
        int faults = 0;
        int nextToReplace = 0; // Points to the next frame to replace (circular)
    
        for (int page : ref) {
            boolean fault = false;
    
            if (!frames.contains(page)) {
                fault = true;
                frames.set(nextToReplace, page); // Replace at nextToReplace position
                nextToReplace = (nextToReplace + 1) % frameCount; // Move pointer circularly
                faults++;
            }
    
            // Snapshot of current frame state
            for (int j = 0; j < frameCount; j++) {
                String value = fault ? (frames.get(j) == null ? " " : frames.get(j).toString()) : " ";
                frameHistory.get(j).add(value);
            }
        }
    
        // Output horizontally
        outputArea.append("Frames ↓ |");
        for (int step = 0; step < ref.size(); step++) {
            outputArea.append(String.format(" %5d |", ref.get(step)));
        }
        outputArea.append("\n");
    
        // Output separator line
        outputArea.append("---------");
        for (int i = 0; i < ref.size(); i++) {
            outputArea.append("--------");
        }
        outputArea.append("\n");
    
        // Output the frame contents for each step
        for (int i = 0; i < frameCount; i++) {
            outputArea.append(String.format("Frame %-2d |", i + 1));
            for (String value : frameHistory.get(i)) {
                outputArea.append(String.format(" %5s |", value));
            }
            outputArea.append("\n");
        }
    
        return faults;
    }
    

    // LRU algorithm
    private int lru(List<Integer> ref, int frameCount) {
        List<Integer> frames = new ArrayList<>();
        Map<Integer, Integer> lastUsed = new HashMap<>();
        List<List<String>> frameHistory = new ArrayList<>();
        
        for (int i = 0; i < frameCount; i++) {
            frameHistory.add(new ArrayList<>());
        }
    
        int faults = 0;
    
        for (int i = 0; i < ref.size(); i++) {
            int page = ref.get(i);
            boolean fault = false;
    
            if (!frames.contains(page)) {
                fault = true;
                if (frames.size() < frameCount) {
                    frames.add(page);
                } else {
                    // Find the LRU page
                    int lruPage = frames.get(0);
                    for (int f : frames) {
                        if (lastUsed.getOrDefault(f, -1) < lastUsed.getOrDefault(lruPage, -1)) {
                            lruPage = f;
                        }
                    }
                    int index = frames.indexOf(lruPage);
                    frames.set(index, page); // replace in same position
                }
                faults++;
            }
    
            lastUsed.put(page, i); // update last used time
    
            List<Integer> current = new ArrayList<>(frames);
            while (current.size() < frameCount) {
                current.add(null);
            }
    
            for (int j = 0; j < frameCount; j++) {
                String value = fault ? (current.get(j) == null ? " " : current.get(j).toString()) : " ";
                frameHistory.get(j).add(value);
            }
        }
    
        // Output the results horizontally (same as before)
        outputArea.append("Frames ↓ |");
        for (int step = 0; step < ref.size(); step++) {
            outputArea.append(String.format(" %5d |", ref.get(step)));
        }
        outputArea.append("\n");
    
        outputArea.append("---------");
        for (int i = 0; i < ref.size(); i++) {
            outputArea.append("--------");
        }
        outputArea.append("\n");
    
        for (int i = 0; i < frameCount; i++) {
            outputArea.append(String.format("Frame %-2d |", i + 1));
            for (String value : frameHistory.get(i)) {
                outputArea.append(String.format(" %5s |", value));
            }
            outputArea.append("\n");
        }
    
        return faults;
    }
    
    

    // Optimal (OPT) algorithm
    private int optimal(List<Integer> ref, int frameCount) {
        List<Integer> frames = new ArrayList<>(Collections.nCopies(frameCount, null));
        List<List<String>> frameHistory = new ArrayList<>();
    
        for (int i = 0; i < frameCount; i++) {
            frameHistory.add(new ArrayList<>());
        }
    
        int faults = 0;
    
        for (int i = 0; i < ref.size(); i++) {
            int page = ref.get(i);
            boolean fault = false;
    
            if (!frames.contains(page)) {
                fault = true;
                faults++;
    
                if (frames.contains(null)) {
                    int emptyIndex = frames.indexOf(null);
                    frames.set(emptyIndex, page);
                } else {
                    int replaceIndex = findOptimalPageToReplace(frames, ref, i);
                    frames.set(replaceIndex, page);
                }
            }
    
            // Snapshot of current frame state
            for (int j = 0; j < frameCount; j++) {
                String value = fault ? (frames.get(j) == null ? " " : frames.get(j).toString()) : " ";
                frameHistory.get(j).add(value);
            }
        }
    
        // Display Output
        outputArea.append("Frames ↓ |");
        for (int page : ref) {
            outputArea.append(String.format(" %5d |", page));
        }
        outputArea.append("\n");
    
        outputArea.append("---------");
        for (int i = 0; i < ref.size(); i++) {
            outputArea.append("--------");
        }
        outputArea.append("\n");
    
        for (int i = 0; i < frameCount; i++) {
            outputArea.append(String.format("Frame %-2d |", i + 1));
            for (String value : frameHistory.get(i)) {
                outputArea.append(String.format(" %5s |", value));
            }
            outputArea.append("\n");
        }
    
        return faults;
    }
    

    

    // Helper method to find the optimal page to replace
    private int findOptimalPageToReplace(List<Integer> frames, List<Integer> ref, int currentIndex) {
        Map<Integer, Integer> futureUse = new HashMap<>();
        for (int i = 0; i < frames.size(); i++) {
            int page = frames.get(i);
            futureUse.put(page, Integer.MAX_VALUE); // initialize to the farthest index
        }

        for (int i = currentIndex + 1; i < ref.size(); i++) {
            int page = ref.get(i);
            if (futureUse.containsKey(page)) {
                futureUse.put(page, i);
            }
        }

        int maxFuture = -1;
        int indexToReplace = -1;
        for (int i = 0; i < frames.size(); i++) {
            int page = frames.get(i);
            int nextUse = futureUse.getOrDefault(page, Integer.MAX_VALUE);
            if (nextUse > maxFuture) {
                maxFuture = nextUse;
                indexToReplace = i;
            }
        }

        return indexToReplace;
    }

    // Main method: launch the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PageReplacementGUI().setVisible(true);
        });
    }
}
