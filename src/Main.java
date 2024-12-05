import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

class JournalEntry {
    private String title;
    private String content;
    private Set<String> tags;

    public JournalEntry(String title, String content, Set<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\nContent: " + content + "\nTags: " + tags + "\n";
    }
}

 class PersonalJournalApp {
    private static final String JOURNAL_FILE = "journal.txt";
    private final List<JournalEntry> journalEntries = new ArrayList<>();
    private final DefaultListModel<String> entryListModel = new DefaultListModel<>();
    private JList<String> entryList;

    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField tagField;
    private JComboBox<String> tagFilterCombo;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new PersonalJournalApp().initializeApp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void initializeApp() throws IOException {
        loadJournalEntries();
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Personal Journal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        frame.add(createHeaderPanel(), BorderLayout.CENTER);
        frame.add(createButtonPanel(), BorderLayout.SOUTH);
        frame.add(createListPanel(), BorderLayout.WEST);

        frame.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        titleField = createTextField("Title", 16);
        headerPanel.add(titleField, BorderLayout.NORTH);

        contentArea = new JTextArea(10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setBorder(BorderFactory.createTitledBorder("Content"));
        JScrollPane scrollContent = new JScrollPane(contentArea);
        headerPanel.add(scrollContent, BorderLayout.CENTER);

        tagField = createTextField("Tags (comma-separated)", 14);
        headerPanel.add(tagField, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(createButton("Add Entry", new Color(34, 167, 240), e -> addNewEntry()));

        buttonPanel.add(createButton("Search", new Color(34, 167, 240), e -> searchEntries()));
        buttonPanel.add(createButton("Delete Entry", new Color(192, 57, 43), e -> deleteSelectedEntry()));
        buttonPanel.add(createButton("Edit Entry", new Color(241, 196, 15), e -> editSelectedEntry()));
        buttonPanel.add(createButton("Export Entries", new Color(39, 174, 96), e -> exportEntries()));

        return buttonPanel;
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());
        entryList = new JList<>(entryListModel);
        entryList.setFont(new Font("Arial", Font.PLAIN, 14));
        entryList.setBorder(BorderFactory.createTitledBorder("Journal Entries"));
        JScrollPane scrollList = new JScrollPane(entryList);
        scrollList.setPreferredSize(new Dimension(250, 0));
        listPanel.add(scrollList, BorderLayout.CENTER);

        tagFilterCombo = new JComboBox<>();
        tagFilterCombo.addItem("All");
        populateTagFilter();
        tagFilterCombo.addActionListener(e -> filterByTag());
        listPanel.add(tagFilterCombo, BorderLayout.NORTH);

        return listPanel;
    }

    private JTextField createTextField(String title, int fontSize) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, fontSize));
        textField.setBorder(BorderFactory.createTitledBorder(title));
        return textField;
    }

    private JButton createButton(String text, Color color, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(color, 2, true));
        button.setPreferredSize(new Dimension(120, 35));
        button.addActionListener(actionListener);
        return button;
    }

     private void addNewEntry() {
         String title = titleField.getText().trim();
         String content = contentArea.getText().trim();
         String tagsInput = tagField.getText().trim();

         if (title.isEmpty() || content.isEmpty()) {
             JOptionPane.showMessageDialog(null, "Title and content cannot be empty!");
             return;
         }

         Set<String> tags = new HashSet<>(Arrays.asList(tagsInput.split("\\s*,\\s*")));
         JournalEntry newEntry = new JournalEntry(title, content, tags);

         journalEntries.add(newEntry);
         entryListModel.addElement(title); // Update the list UI
         saveJournalEntries(); // Save to the file

         // Clear the input fields
         titleField.setText("");
         contentArea.setText("");
         tagField.setText("");

         JOptionPane.showMessageDialog(null, "New entry added successfully!");
     }

     private void searchEntries() {
         String query = JOptionPane.showInputDialog("Enter title or tag to search:");
         if (query != null && !query.trim().isEmpty()) {
             boolean found = false;
             for (JournalEntry entry : journalEntries) {
                 if (entry.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                         entry.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase()))) {
                     found = true;
                     JOptionPane.showMessageDialog(null, "Found: " + entry);
                 }
             }
             if (!found) {
                 JOptionPane.showMessageDialog(null, "No entries found matching the search query.");
             }
         }
     }


     private void deleteSelectedEntry() {
        int selectedIndex = entryList.getSelectedIndex();
        if (selectedIndex != -1) {
            journalEntries.remove(selectedIndex);
            entryListModel.remove(selectedIndex);
            saveJournalEntries();
            JOptionPane.showMessageDialog(null, "Entry deleted!");
        } else {
            JOptionPane.showMessageDialog(null, "Please select an entry to delete.");
        }
    }

    private void editSelectedEntry() {
        int selectedIndex = entryList.getSelectedIndex();
        if (selectedIndex != -1) {
            JournalEntry entry = journalEntries.get(selectedIndex);
            titleField.setText(entry.getTitle());
            contentArea.setText(entry.getContent());
            tagField.setText(String.join(", ", entry.getTags()));

            journalEntries.remove(selectedIndex);
            entryListModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(null, "Please select an entry to edit.");
        }
    }

    private void exportEntries() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (JournalEntry entry : journalEntries) {
                    writer.write(entry.toString());
                    writer.write("-----\n");
                }
                JOptionPane.showMessageDialog(null, "Entries exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error exporting entries: " + e.getMessage());
            }
        }
    }

    private void filterByTag() {
        String selectedTag = (String) tagFilterCombo.getSelectedItem();
        entryListModel.clear();
        if ("All".equals(selectedTag)) {
            journalEntries.forEach(entry -> entryListModel.addElement(entry.getTitle()));
        } else {
            journalEntries.stream()
                    .filter(entry -> entry.getTags().contains(selectedTag))
                    .forEach(entry -> entryListModel.addElement(entry.getTitle()));
        }
    }

    private void populateTagFilter() {
        Set<String> uniqueTags = new HashSet<>();
        journalEntries.forEach(entry -> uniqueTags.addAll(entry.getTags()));
        uniqueTags.forEach(tagFilterCombo::addItem);
    }

    private void saveJournalEntries() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(JOURNAL_FILE))) {
            for (JournalEntry entry : journalEntries) {
                writer.write(entry.getTitle() + "\n");
                writer.write(entry.getContent() + "\n");
                writer.write(String.join(", ", entry.getTags()) + "\n");
                writer.write("-----\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving entries: " + e.getMessage());
        }
    }

    private void loadJournalEntries() throws IOException {
        File file = new File(JOURNAL_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String title;
                while ((title = reader.readLine()) != null) {
                    String content = reader.readLine();
                    String tagsLine = reader.readLine();
                    Set<String> tags = new HashSet<>(Arrays.asList(tagsLine.split("\\s*,\\s*")));
                    journalEntries.add(new JournalEntry(title, content, tags));
                    entryListModel.addElement(title);
                    reader.readLine(); // Skip the separator line
                }
            }
        }
    }

    private void clearFields() {
        titleField.setText("");
        contentArea.setText("");
        tagField.setText("");
    }
}
