package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DeleteBooks {

    private static final String DB_URL = "jdbc:sqlite:books.db";

    public static void main(String[] args) {
        try {
            // Чтение файла JSON
            String filePath = "D:\\books.json";  // Путь к вашему JSON файлу
            String jsonString = readFile(filePath);

            // Преобразуем строку в JSON массив
            JSONArray usersArray = new JSONArray(jsonString);

            // Создание подключения к базе данных
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                // Создание таблиц
                createTables(conn);

                // Обрабатываем каждого пользователя
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject user = usersArray.getJSONObject(i);

                    // Извлекаем данные пользователя
                    String name = user.getString("name");
                    String surname = user.getString("surname");
                    String phone = user.getString("phone");
                    boolean subscribed = user.getBoolean("subscribed");

                    // Добавляем пользователя в базу данных
                    int visitorId = addVisitor(conn, name, surname, phone, subscribed);

                    // Извлекаем книги пользователя
                    JSONArray favoriteBooks = user.optJSONArray("favoriteBooks");  // Проверка на null
                    if (favoriteBooks != null) {
                        for (int j = 0; j < favoriteBooks.length(); j++) {
                            JSONObject book = favoriteBooks.getJSONObject(j);
                            String bookName = book.getString("name");
                            String author = book.getString("author");
                            int publishingYear = book.getInt("publishingYear");
                            String isbn = book.getString("isbn");
                            String publisher = book.getString("publisher");

                            // Добавляем книгу в базу данных
                            int bookId = addBook(conn, bookName, author, publishingYear, isbn, publisher);

                            // Создаем связь между пользователем и книгой
                            addVisitorBook(conn, visitorId, bookId);
                        }
                    }
                }

                // Выводим информацию
                printAllUsersAndBooks(conn);

                // Удаляем таблицы после работы
                dropTables(conn);

            } catch (SQLException e) {
                System.err.println("Ошибка работы с базой данных: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder jsonString = new StringBuilder();
        try (FileReader fileReader = new FileReader(filePath)) {
            int ch;
            while ((ch = fileReader.read()) != -1) {
                jsonString.append((char) ch);
            }
        }
        return jsonString.toString();
    }

    private static void createTables(Connection conn) throws SQLException {
        String createVisitorsTable = "CREATE TABLE IF NOT EXISTS visitors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "surname TEXT NOT NULL," +
                "phone TEXT NOT NULL UNIQUE," +
                "subscribed BOOLEAN" +
                ");";

        String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "author TEXT NOT NULL," +
                "publishingYear INTEGER NOT NULL," +
                "isbn TEXT NOT NULL UNIQUE," +
                "publisher TEXT NOT NULL" +
                ");";

        String createVisitorBooksTable = "CREATE TABLE IF NOT EXISTS visitor_books (" +
                "visitor_id INTEGER," +
                "book_id INTEGER," +
                "FOREIGN KEY(visitor_id) REFERENCES visitors(id)," +
                "FOREIGN KEY(book_id) REFERENCES books(id)," +
                "PRIMARY KEY(visitor_id, book_id)" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createVisitorsTable);
            stmt.execute(createBooksTable);
            stmt.execute(createVisitorBooksTable);
        }
    }

    private static int addVisitor(Connection conn, String name, String surname, String phone, boolean subscribed) throws SQLException {
        String insertVisitorSQL = "INSERT OR IGNORE INTO visitors (name, surname, phone, subscribed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertVisitorSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            stmt.setString(3, phone);
            stmt.setBoolean(4, subscribed);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // возвращаем id пользователя
                }
            }
        }
        return -1;
    }

    private static int addBook(Connection conn, String bookName, String author, int publishingYear, String isbn, String publisher) throws SQLException {
        String insertBookSQL = "INSERT OR IGNORE INTO books (name, author, publishingYear, isbn, publisher) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertBookSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bookName);
            stmt.setString(2, author);
            stmt.setInt(3, publishingYear);
            stmt.setString(4, isbn);
            stmt.setString(5, publisher);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // возвращаем id книги
                }
            }
        }
        return -1;
    }

    private static void addVisitorBook(Connection conn, int visitorId, int bookId) throws SQLException {
        String insertVisitorBookSQL = "INSERT OR IGNORE INTO visitor_books (visitor_id, book_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertVisitorBookSQL)) {
            stmt.setInt(1, visitorId);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }

    private static void printAllUsersAndBooks(Connection conn) throws SQLException {
        String query = "SELECT visitors.name, visitors.surname, books.name AS book_name, books.author " +
                "FROM visitors " +
                "JOIN visitor_books ON visitors.id = visitor_books.visitor_id " +
                "JOIN books ON visitor_books.book_id = books.id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String visitorName = rs.getString("name");
                String visitorSurname = rs.getString("surname");
                String bookName = rs.getString("book_name");
                String bookAuthor = rs.getString("author");
                System.out.println(visitorName + " " + visitorSurname + " - " + bookName + " by " + bookAuthor);
            }
        }
    }

    private static void dropTables(Connection conn) throws SQLException {
        String dropVisitorsTable = "DROP TABLE IF EXISTS visitors;";
        String dropBooksTable = "DROP TABLE IF EXISTS books;";
        String dropVisitorBooksTable = "DROP TABLE IF EXISTS visitor_books;";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(dropVisitorsTable);
            stmt.execute(dropBooksTable);
            stmt.execute(dropVisitorBooksTable);
        }
    }
}

