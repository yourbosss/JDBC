package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;

public class BookOld {

    private static final String DB_URL = "jdbc:sqlite:books.db";

    public static void main(String[] args) {
        // Загрузка данных из файла books.json
        String jsonFilePath = "books.json";
        String jsonData = loadJsonFromFile(jsonFilePath);

        // Проверка, если файл не был загружен
        if (jsonData == null) {
            System.out.println("Не удалось загрузить файл. Программа завершена.");
            return;
        }

        // Парсим данные JSON
        JSONArray visitorsArray = new JSONArray(jsonData);

        // Создаем соединение с базой данных
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Создание таблиц, если они не существуют
            createTables(conn);

            // Обрабатываем каждого посетителя и добавляем данные в таблицы
            for (int i = 0; i < visitorsArray.length(); i++) {
                JSONObject visitorData = visitorsArray.getJSONObject(i);

                String name = visitorData.getString("name");
                String surname = visitorData.getString("surname");
                String phone = visitorData.getString("phone");
                boolean subscribed = visitorData.getBoolean("subscribed");

                // Добавляем посетителя
                int visitorId = addVisitor(conn, name, surname, phone, subscribed);

                // Добавляем книги посетителя
                JSONArray favoriteBooks = visitorData.getJSONArray("favoriteBooks");
                for (int j = 0; j < favoriteBooks.length(); j++) {
                    JSONObject book = favoriteBooks.getJSONObject(j);
                    String bookName = book.getString("name");
                    String author = book.getString("author");
                    int publishingYear = book.getInt("publishingYear");
                    String isbn = book.getString("isbn");
                    String publisher = book.getString("publisher");

                    // Добавляем книгу
                    int bookId = addBook(conn, bookName, author, publishingYear, isbn, publisher);

                    // Добавляем связь между посетителем и книгой
                    addVisitorBook(conn, visitorId, bookId);
                }
            }

            // Теперь извлекаем и выводим книги младше 2000 года
            List<Book> booksBefore2000 = getBooksBefore2000(conn);
            printBooksBefore2000(booksBefore2000);

        } catch (SQLException e) {
            System.err.println("Ошибка работы с базой данных: " + e.getMessage());
        }
    }

    // Метод для загрузки JSON данных из файла
    private static String loadJsonFromFile(String filePath) {
        StringBuilder jsonData = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                jsonData.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден: " + e.getMessage());
            return null;
        }
        return jsonData.toString();
    }

    // Метод для создания таблиц в базе данных
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

    // Метод для добавления нового посетителя в таблицу visitors
    private static int addVisitor(Connection conn, String name, String surname, String phone, boolean subscribed) throws SQLException {
        String insertVisitorSQL = "INSERT INTO visitors (name, surname, phone, subscribed) " +
                "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM visitors WHERE phone = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertVisitorSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            stmt.setString(3, phone);
            stmt.setBoolean(4, subscribed);
            stmt.setString(5, phone);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;  // В случае неудачи
    }

    // Метод для добавления новой книги в таблицу books
    private static int addBook(Connection conn, String name, String author, int publishingYear, String isbn, String publisher) throws SQLException {
        String insertBookSQL = "INSERT INTO books (name, author, publishingYear, isbn, publisher) " +
                "SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertBookSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, author);
            stmt.setInt(3, publishingYear);
            stmt.setString(4, isbn);
            stmt.setString(5, publisher);
            stmt.setString(6, isbn);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;  // В случае неудачи
    }

    // Метод для добавления связи между посетителем и книгой в таблицу visitor_books
    private static void addVisitorBook(Connection conn, int visitorId, int bookId) throws SQLException {
        String insertVisitorBookSQL = "INSERT OR IGNORE INTO visitor_books (visitor_id, book_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertVisitorBookSQL)) {
            stmt.setInt(1, visitorId);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }

    // Метод для получения списка книг, изданных до 2000 года
    private static List<Book> getBooksBefore2000(Connection conn) throws SQLException {
        String query = "SELECT * FROM books WHERE publishingYear < 2000";
        List<Book> booksBefore2000 = new ArrayList<>();

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String author = rs.getString("author");
                int publishingYear = rs.getInt("publishingYear");
                String isbn = rs.getString("isbn");
                String publisher = rs.getString("publisher");

                booksBefore2000.add(new Book(name, author, publishingYear, isbn, publisher));
            }
        }

        return booksBefore2000;
    }

    // Метод для вывода книг, изданных до 2000 года
    private static void printBooksBefore2000(List<Book> booksBefore2000) {
        System.out.println("\nКниги, изданные до 2000 года:");
        for (Book book : booksBefore2000) {
            System.out.println(book);
        }
    }
}

// Класс Book для представления книги
class Book {
    private String name;
    private String author;
    private int publishingYear;
    private String isbn;
    private String publisher;

    public Book(String name, String author, int publishingYear, String isbn, String publisher) {
        this.name = name;
        this.author = author;
        this.publishingYear = publishingYear;
        this.isbn = isbn;
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "Name: " + name +
                ", Author: " + author +
                ", Year: " + publishingYear +
                ", ISBN: " + isbn +
                ", Publisher: " + publisher;
    }
}
