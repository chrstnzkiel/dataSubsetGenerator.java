import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;

public class RandomSubsetGenerator {
    // List to store the full CSV data
    private static List<Map<String, String>> csvData = null;
    // List to store the previously generated subset (to avoid repetition)
    private static List<Map<String, String>> previousSubset = null;

    public static void main(String[] args) {
        try {
            // Set the UI look and feel to match the system appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Open a file chooser dialog to select a CSV file
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
        fileChooser.setFileFilter(filter);

        String filePath;
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getSelectedFile().getPath(); // Get selected file path
        } else {
            JOptionPane.showMessageDialog(null, "No file selected. Exiting...", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit program if no file is selected
        }

        // Read and parse the CSV file
        csvData = readCSV(filePath);
        if (csvData == null) {
            JOptionPane.showMessageDialog(null, "Failed to read the CSV file. Exiting...", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt user to enter subset size
        String subsetSizeInput = JOptionPane.showInputDialog(null, "Enter the size of the subset:", "Subset Size", JOptionPane.QUESTION_MESSAGE);
        if (subsetSizeInput == null || subsetSizeInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No subset size entered. Exiting...", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int subsetSize;
        try {
            subsetSize = Integer.parseInt(subsetSizeInput.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid subset size. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure subset size is within valid range
        if (subsetSize < 1 || subsetSize > csvData.size()) {
            JOptionPane.showMessageDialog(null, String.format("Please enter a valid subset size between 1 and %d", csvData.size()), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate a random subset and display it
        List<Map<String, String>> subset = generateRandomSubset(subsetSize);
        displayResult(subset);
    }

    // Reads a CSV file and stores data in a list of maps
    private static List<Map<String, String>> readCSV(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = br.readLine().split(","); // Read first line as headers
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    if (i < values.length) {
                        row.put(headers[i].trim(), values[i].trim());
                    } else {
                        row.put(headers[i].trim(), ""); // Handle missing values
                    }
                }
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

    // Generates a random subset ensuring it is different from the previous one
    private static List<Map<String, String>> generateRandomSubset(int subsetSize) {
        List<Map<String, String>> subset;
        Random random = new Random();
        do {
            Set<Integer> indices = new HashSet<>();
            while (indices.size() < subsetSize) {
                indices.add(random.nextInt(csvData.size()));
            }
            subset = new ArrayList<>();
            for (int index : indices) {
                subset.add(csvData.get(index));
            }
        } while (isEqual(subset, previousSubset)); // Ensure subset is not repeated

        previousSubset = subset;
        return subset;
    }

    // Checks if two subsets are identical
    private static boolean isEqual(List<Map<String, String>> list1, List<Map<String, String>> list2) {
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    // Displays the generated subset in a table format using Swing
    private static void displayResult(List<Map<String, String>> subset) {
        if (subset.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data to display.", "Result", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] headers = subset.get(0).keySet().toArray(new String[0]);
        String[][] data = new String[subset.size()][headers.length];
        for (int i = 0; i < subset.size(); i++) {
            for (int j = 0; j < headers.length; j++) {
                data[i][j] = subset.get(i).get(headers[j]);
            }
        }

        // Create and display a table with the subset data
        JTable table = new JTable(data, headers);
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(null, scrollPane, "Generated Subset", JOptionPane.PLAIN_MESSAGE);
    }
}
