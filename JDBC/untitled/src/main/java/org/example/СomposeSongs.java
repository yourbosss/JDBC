package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class СomposeSongs {
    public static void main(String[] args) {
        String sqlFilePath = "D:\\music-create.sql"; // Путь к вашему SQL-файлу

        // Выполняем SQL-скрипт из файла
        executeSqlFile(sqlFilePath);

        // Получаем список музыкальных композиций
        fetchMusicList();

        // Получаем композиции без букв m и t
        fetchMusicWithoutMAndT();

        // Добавляем любимую композицию
        addFavoriteMusic("My Favorite Song");
    }

    // Метод для выполнения SQL-запросов из файла
    private static void executeSqlFile(String filePath) {
        String url = "jdbc:sqlite:music.db"; // Используем SQLite без указания хоста

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            // Чтение файла построчно
            while ((line = br.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }

            // Выполнение SQL-запросов
            String[] sqlStatements = sqlBuilder.toString().split(";");
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    statement.executeUpdate(sql.trim());
                }
            }
            System.out.println("SQL-файл выполнен успешно.");

        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении SQL-запросов: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    // Метод для получения списка музыкальных композиций
    private static void fetchMusicList() {
        String url = "jdbc:sqlite:music.db"; // Используем SQLite без указания хоста
        String selectSQL = "SELECT * FROM music"; // Запрос без схемы 'study'

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {

            // Обработка результатов запроса
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("ID: " + id + ", Название: " + name);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении SQL-запроса: " + e.getMessage());
        }
    }

    // Метод для получения музыкальных композиций без букв 'm' и 't'
    private static void fetchMusicWithoutMAndT() {
        String url = "jdbc:sqlite:music.db";
        String selectSQL = "SELECT * FROM music WHERE name NOT LIKE '%m%' AND name NOT LIKE '%t%'";

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

    // Метод для добавления любимой композиции
    private static void addFavoriteMusic(String name) {
        String url = "jdbc:sqlite:music.db";
        String insertSQL = "INSERT INTO music (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM music WHERE name = ?)";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, name);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Композиция '" + name + "' была успешно добавлена.");
            } else {
                System.out.println("Композиция '" + name + "' уже существует.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении композиции: " + e.getMessage());
        }
    }
}
