package org.example.geometry2d;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class GeometryApp extends Application {
    private Canvas canvas;
    private Random random = new Random();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Geometry Drawer");

        canvas = new Canvas(600, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Button drawCircleButton = new Button("Draw Circle");
        drawCircleButton.setOnAction(e -> drawCircle(gc));

        Button drawRectangleButton = new Button("Draw Rectangle");
        drawRectangleButton.setOnAction(e -> drawRectangle(gc));

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setTop(drawCircleButton);
        root.setBottom(drawRectangleButton);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawCircle(GraphicsContext gc) {
        double radius = random.nextDouble() * 50 + 10; // Random radius between 10 and 60
        double x = random.nextDouble() * (canvas.getWidth() - radius * 2);
        double y = random.nextDouble() * (canvas.getHeight() - radius * 2);

        gc.setFill(randomColor());
        gc.fillOval(x, y, radius * 2, radius * 2);
    }

    private void drawRectangle(GraphicsContext gc) {
        double width = random.nextDouble() * 100 + 20; // Random width between 20 and 120
        double height = random.nextDouble() * 100 + 20; // Random height between 20 and 120
        double x = random.nextDouble() * (canvas.getWidth() - width);
        double y = random.nextDouble() * (canvas.getHeight() - height);

        gc.setFill(randomColor());
        gc.fillRect(x, y, width, height);
    }

    private Color randomColor() {
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}