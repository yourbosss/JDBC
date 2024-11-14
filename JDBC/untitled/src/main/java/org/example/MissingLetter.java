package org.example;

import java.sql.*;

public class MissingLetter {  // Класс без букв "m" и "t"
    public static void main(String[] args) {
        fetchMusicWithoutMAndT();
    }

    // Метод для получения музыкальных композиций без букв 'm' и 't'
    private static void fetchMusicWithoutMAndT() {
        String url = "jdbc:sqlite:music.db";
        // Запрос для извлечения композиций без букв 'm' и 't' в названии
        String selectSQL = "SELECT * FROM music WHERE LOWER(name) NOT LIKE '%m%' AND LOWER(name) NOT LIKE '%t%'";

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("ID: " + id + ", Название: " + name);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении SQL-запроса: " + e.getMessage());
        }
    }
}
