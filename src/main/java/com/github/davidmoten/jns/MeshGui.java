package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.CellType.FLUID;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MeshGui extends Application {

    private static final double MIN_SATURATION = 0.05;

    private Mesh mesh;
    private final int cellsEast = 10;
    private final int cellsNorth = 10;
    private final int cellsUp = 1;

    @Override
    public void init() throws Exception {
        mesh = Mesh
                .builder()
                .cellSize(1)
                .creator(
                        CellCreator.builder().cellsEast(cellsEast).cellsNorth(cellsNorth)
                        .cellsUp(cellsUp).build()).build();
        mesh = Util.createMeshForWhirlpool2D();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Surface level mesh");
        final Group root = new Group();
        final Canvas canvas = new Canvas(600, 600);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.widthProperty().addListener(o -> drawGrid(gc, mesh));
        canvas.heightProperty().addListener(o -> drawGrid(gc, mesh));
        drawGrid(gc, mesh);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void drawGrid(GraphicsContext gc, Mesh mesh) {

        final Statistics pStats = new Statistics();
        final Statistics vStats = new Statistics();

        // get stats
        for (int east = 0; east <= cellsEast; east++)
            for (int north = 0; north <= cellsNorth; north++) {
                final Cell cell = mesh.cell(east, north, cellsUp - 1);
                if (cell.type() == CellType.FLUID) {
                    final double p = cell.pressure();
                    final double v = magnitudeEastNorth(cell.velocity());
                    pStats.add(p);
                    vStats.add(v);
                }
            }

        for (int east = -1; east <= cellsEast; east++)
            for (int north = -1; north <= cellsNorth; north++) {
                drawCell(gc, mesh, east, north, cellsUp - 1, pStats, vStats, cellsEast + 2,
                        cellsNorth + 2, cellsUp);
            }
    }

    private double magnitudeEastNorth(Vector v) {
        return Math.sqrt(v.east() * v.east() + v.north() * v.north());
    }

    private void drawCell(GraphicsContext gc, Mesh grid, int east, int north, int up,
            Statistics pStats, Statistics vStats, int cellsEast, int cellsNorth, int cellsUp) {
        final double w = gc.getCanvas().getWidth();
        final double h = gc.getCanvas().getHeight();
        final Cell cell = grid.cell(east, north, up);
        final double cellWidth = w / (cellsEast);
        final double x1 = cellWidth * (east + 1);
        final double cellHeight = h / cellsNorth;
        final double y1 = h - cellHeight * (north + 2);

        if (cell.type() == CellType.OBSTACLE) {
            gc.setFill(Color.BROWN);
            gc.fillRect(x1, y1, cellWidth, cellHeight);
        } else if (cell.type() == CellType.UNKNOWN) {
            gc.setFill(Color.LIGHTPINK);
            gc.fillRect(x1, y1, cellWidth, cellHeight);
        } else {
            final double pressure0To1 = (cell.pressure() - pStats.min())
                    / (pStats.max() - pStats.min());
            gc.setFill(toColor(MIN_SATURATION, pressure0To1));
            gc.fillRect(x1, y1, cellWidth, cellHeight);
        }
        gc.setStroke(Color.WHITE);
        gc.strokeRect(x1, y1, cellWidth, cellHeight);

        if (cell.type() == FLUID) {
            final Vector v = cell.velocity();
            if (vStats.max() > 0) {
                final double magnitudeEastNorth = magnitudeEastNorth(v);
                final double vProportion = magnitudeEastNorth / vStats.max();
                final double centreX = x1 + cellWidth / 2;
                final double centreY = y1 + cellHeight / 2;
                final double deltaX = v.east() / magnitudeEastNorth * vProportion * cellWidth / 2;
                final double deltaY = v.north() / magnitudeEastNorth * vProportion * cellHeight / 2;

                gc.setStroke(Color.DARKBLUE);
                gc.strokeLine(centreX, centreY, centreX + deltaX, centreY + deltaY);
            }
        }

    }

    private static Color toColor(double minSaturation, double prop) {
        return Color.hsb(0.0, (prop * (1 - minSaturation) + minSaturation), 1.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}