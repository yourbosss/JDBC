package org.example;

import java.sql.*;
import java.util.Scanner;

public class PersonalBooks {

    private static final String DB_URL = "jdbc:sqlite:books.db";

    public static void main(String[] args) {
        // Ввод данных о себе
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите ваше имя: ");
        String name = scanner.nextLine();

        System.out.println("Введите вашу фамилию: ");
        String surname = scanner.nextLine();

        System.out.println("Введите ваш телефон: ");
        String phone = scanner.nextLine();

        System.out.println("Вы подписаны на рассылку? (true/false): ");
        boolean subscribed = Boolean.parseBoolean(scanner.nextLine());

        // Создание объекта "посетитель"
        Visitor myVisitor = new Visitor(name, surname, phone, subscribed);

        // Ввод любимых книг
        System.out.println("Введите количество любимых книг: ");
        int booksCount = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < booksCount; i++) {
            System.out.println("Введите название книги " + (i + 1) + ": ");
            String bookName = scanner.nextLine();

            System.out.println("Введите автора книги " + (i + 1) + ": ");
            String author = scanner.nextLine();

            System.out.println("Введите год издания книги " + (i + 1) + ": ");
            int publishingYear = Integer.parseInt(scanner.nextLine());

            System.out.println("Введите ISBN книги " + (i + 1) + ": ");
            String isbn = scanner.nextLine();

            System.out.println("Введите издателя книги " + (i + 1) + ": ");
            String publisher = scanner.nextLine();

            // Добавляем книгу в список любимых книг
            myVisitor.addFavoriteBook(new Book(bookName, author, publishingYear, isbn, publisher));
        }

        // Добавляем информацию о посетителе и книгах в базу данных
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Создание таблиц, если они не существуют
            createTables(conn);

            // Добавляем посетителя
            int visitorId = addVisitor(conn, myVisitor);

            // Добавляем книги посетителя
            for (Book book : myVisitor.getFavoriteBooks()) {
                int bookId = addBook(conn, book);
                addVisitorBook(conn, visitorId, bookId);
            }

            // Выводим информацию о себе и любимых книгах
            System.out.println("\nИнформация о себе:");
            System.out.println(myVisitor);
            System.out.println("\nВаши любимые книги:");
            for (Book book : myVisitor.getFavoriteBooks()) {
                System.out.println(book);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка работы с базой данных: " + e.getMessage());
        }
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
    private static int addVisitor(Connection conn, Visitor visitor) throws SQLException {
        String insertVisitorSQL = "INSERT INTO visitors (name, surname, phone, subscribed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertVisitorSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, visitor.getName());
            stmt.setString(2, visitor.getSurname());
            stmt.setString(3, visitor.getPhone());
            stmt.setBoolean(4, visitor.isSubscribed());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Метод для добавления новой книги в таблицу books
    private static int addBook(Connection conn, Book book) throws SQLException {
        String insertBookSQL = "INSERT INTO books (name, author, publishingYear, isbn, publisher) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertBookSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getName());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getPublishingYear());
            stmt.setString(4, book.getIsbn());
            stmt.setString(5, book.getPublisher());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Метод для добавления связи между посетителем и книгой
    private static void addVisitorBook(Connection conn, int visitorId, int bookId) throws SQLException {
        String insertVisitorBookSQL = "INSERT OR IGNORE INTO visitor_books (visitor_id, book_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertVisitorBookSQL)) {
            stmt.setInt(1, visitorId);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }
}

// Класс для представления книги
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

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublishingYear() {
        return publishingYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    @Override
    public String toString() {
        return "Book{name='" + name + "', author='" + author + "', year=" + publishingYear + ", ISBN='" + isbn + "', publisher='" + publisher + "'}";
    }
}

// Класс для представления посетителя
class Visitor {
    private String name;
    private String surname;
    private String phone;
    private boolean subscribed;
    private List<Book> favoriteBooks = new ArrayList<>();

    public Visitor(String name, String surname, String phone, boolean subscribed) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.subscribed = subscribed;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void addFavoriteBook(Book book) {
        favoriteBooks.add(book);
    }

    public List<Book> getFavoriteBooks() {
        return favoriteBooks;
    }

    @Override
    public String toString() {
        return "Visitor{name='" + name + "', surname='" + surname + "', phone='" + phone + "', subscribed=" + subscribed + "}";
    }
}
