package geometry2d;

public class Rectangle implements Figure { // Реализуем интерфейс Figure
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height; // Площадь прямоугольника
    }

    @Override
    public double perimeter() {
        return 2 * (width + height); // Периметр прямоугольника
    }

    @Override
    public String toString() {
        return "Rectangle(width=" + width + ", height=" + height + ")";
    }
}
