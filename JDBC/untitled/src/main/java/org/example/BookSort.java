package org.example;

import java.io.*;
import java.util.*;

public class BookSort {

    // Класс для представления книги
    static class Book {
        private String name;
        private String author;
        private int publishingYear;
        private String isbn;
        private String publisher;

        public Book() {
            // Конструктор по умолчанию для Jackson
        }

        public Book(String name, String author, int publishingYear, String isbn, String publisher) {
            this.name = name;
            this.author = author;
            this.publishingYear = publishingYear;
            this.isbn = isbn;
            this.publisher = publisher;
        }

        public int getPublishingYear() {
            return publishingYear;
        }

        @Override
        public String toString() {
            return "Name: " + name + ", Author: " + author + ", Year: " + publishingYear + ", ISBN: " + isbn + ", Publisher: " + publisher;
        }
    }

    // Класс для представления данных о пользователе
    static class User {
        private String name;
        private String surname;
        private String phone;
        private boolean subscribed;
        private List<Book> favoriteBooks;

        public User() {
            // Конструктор по умолчанию для Jackson
        }

        public List<Book> getFavoriteBooks() {
            return favoriteBooks;
        }
    }

    public static void main(String[] args) throws IOException {
        // Путь к JSON файлу
        String jsonFilePath = "books.json"; // Укажите путь к вашему файлу JSON

        // Создаем ObjectMapper для чтения JSON
        ObjectMapper objectMapper = new ObjectMapper();

        // Читаем JSON файл
        List<User> users = objectMapper.readValue(new File(jsonFilePath), objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));

        // Создаем список для хранения всех книг младше 2000 года
        List<Book> booksBefore2000 = new ArrayList<>();

        // Проходим по всем пользователям
        for (User user : users) {
            // Проходим по списку любимых книг пользователя
            for (Book book : user.getFavoriteBooks()) {
                if (book.getPublishingYear() < 2000) {
                    booksBefore2000.add(book);
                }
            }
        }

        // Выводим список книг младше 2000 года
        System.out.println("Books published before 2000:");
        for (Book book : booksBefore2000) {
            System.out.println(book);
        }
    }
}
