import java.sql.*;
import java.util.Scanner;

public class LibraryManager {

    private static final String URL = "jdbc:mariadb://localhost:3306/librarydb";
    private static final String USER = "root";
    private static final String PASS = "aarushi";

    private Connection conn;
    private Scanner scanner;

    public LibraryManager() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASS);
        scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            System.out.println("\n*** Library Menu ***");
            System.out.println("1. Add Book");
            System.out.println("2. Issue Book");
            System.out.println("3. Return Book");
            System.out.println("4. List All Books");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            try {
                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        issueBook();
                        break;
                    case 3:
                        returnBook();
                        break;
                    case 4:
                        listBooks();
                        break;
                    case 5:
                        System.out.println("Exiting. Goodbye!");
                        conn.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
        }
    }

    private void addBook() throws SQLException {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();

        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Book added successfully.");
            } else {
                System.out.println("Failed to add book.");
            }
        }
    }

    private void issueBook() throws SQLException {
        System.out.print("Enter book ID to issue: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        String checkSql = "SELECT is_issued FROM books WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isIssued = rs.getBoolean("is_issued");
                    if (isIssued) {
                        System.out.println("Book is already issued.");
                        return;
                    }
                } else {
                    System.out.println("Book ID not found.");
                    return;
                }
            }
        }

        String issueSql = "UPDATE books SET is_issued = TRUE WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(issueSql)) {
            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Book issued successfully.");
            } else {
                System.out.println("Failed to issue book.");
            }
        }
    }

    private void returnBook() throws SQLException {
        System.out.print("Enter book ID to return: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        String checkSql = "SELECT is_issued FROM books WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isIssued = rs.getBoolean("is_issued");
                    if (!isIssued) {
                        System.out.println("Book is not currently issued.");
                        return;
                    }
                } else {
                    System.out.println("Book ID not found.");
                    return;
                }
            }
        }

        String returnSql = "UPDATE books SET is_issued = FALSE WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(returnSql)) {
            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Book returned successfully.");
            } else {
                System.out.println("Failed to return book.");
            }
        }
    }

    private void listBooks() throws SQLException {
        String sql = "SELECT book_id, title, author, is_issued FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nID | Title | Author | Issued?");
            System.out.println("-------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean isIssued = rs.getBoolean("is_issued");
                System.out.printf("%d | %s | %s | %s\n",
                        id, title, author, (isIssued ? "Yes" : "No"));
            }
        }
    }

    public static void main(String[] args) {
        try {
            LibraryManager manager = new LibraryManager();
            manager.run();
        } catch (SQLException e) {
            System.err.println("Cannot connect to database: " + e.getMessage());
        }
    }
}
