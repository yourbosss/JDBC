package org.example;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class LoveSong {
    public static void main(String[] args) {
        // Создаем сканер для ввода с консоли
        Scanner scanner = new Scanner(System.in);

        // Запрашиваем у пользователя название любимой композиции
        System.out.println("Введите название вашей любимой композиции:");
        String favoriteSong = scanner.nextLine();

        // Добавляем композицию в базу данных и в лог-файл
        addFavoriteMusic(favoriteSong);

        // Выводим все записи из базы данных, чтобы убедиться, что композиция добавлена
        displayAllSongs();
    }

    // Метод для добавления любимой композиции
    private static void addFavoriteMusic(String name) {
        String url = "jdbc:sqlite:music.db";  // Убедитесь, что путь к базе данных правильный

        // Простой запрос на добавление композиции в таблицу
        String insertSQL = "INSERT INTO music (name) VALUES (?)";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            // Устанавливаем название композиции
            preparedStatement.setString(1, name);

            // Выполняем запрос
            int rowsAffected = preparedStatement.executeUpdate();

            // Проверка, была ли добавлена композиция
            if (rowsAffected > 0) {
                System.out.println("Композиция '" + name + "' была успешно добавлена.");
                // Добавляем запись в лог-файл
                logSongInFile(name);
            } else {
                System.out.println("Композиция не была добавлена.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении композиции: " + e.getMessage());
        }
    }

    // Метод для записи добавленной песни в текстовый файл
    private static void logSongInFile(String songName) {
        // Путь к текстовому файлу, в котором будет храниться лог
        String logFilePath = "\"D:\\musiclog.txt\"";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            // Записываем команду SQL для вставки новой песни
            writer.write("INSERT INTO music (name) VALUES ('" + songName + "');\n");
            System.out.println("Лог-файл обновлен: песня добавлена.");
        } catch (IOException e) {
            System.out.println("Ошибка при записи в лог-файл: " + e.getMessage());
        }
    }

    // Метод для отображения всех песен в базе данных
    private static void displayAllSongs() {
        String url = "jdbc:sqlite:music.db";  // Убедитесь, что путь к базе данных правильный
        String selectSQL = "SELECT * FROM music";

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {

            // Выводим все записи
            System.out.println("Список всех композиций:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("ID: " + id + ", Название: " + name);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выборке данных: " + e.getMessage());
        }
    }
}
