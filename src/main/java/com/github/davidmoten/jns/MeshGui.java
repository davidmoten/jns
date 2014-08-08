package com.github.davidmoten.jns;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
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
						new RegularGridCellCreator(cellsEast, cellsNorth, cellsUp,
								Util.SEAWATER_MEAN_DENSITY_KG_PER_M3,
								Util.SEAWATER_MEAN_VISCOSITY)).build();
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Drawing Operations Test");
		Group root = new Group();
		Canvas canvas = new Canvas(300, 250);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		canvas.widthProperty().addListener(o -> drawGrid(gc, mesh));
		canvas.heightProperty().addListener(o -> drawGrid(gc, mesh));
		drawGrid(gc, mesh);
		// drawShapes(gc);
		root.getChildren().add(canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	private void drawGrid(GraphicsContext gc, Mesh grid) {

		Statistics pStats = new Statistics();
		Statistics vStats = new Statistics();

		// get stats
		for (int east = 0; east <= cellsEast; east++)
			for (int north = 0; north <= cellsNorth; north++) {
				Cell cell = grid.cell(east, north, cellsUp);
				double p = cell.pressure();
				double v = cell.velocity().magnitude();
				pStats.add(p);
				vStats.add(v);
			}

		for (int east = 0; east <= cellsEast; east++)
			for (int north = 0; north <= cellsNorth; north++) {
				drawCell(gc, grid, east, north, cellsUp, pStats, vStats);
			}
	}

	private void drawCell(GraphicsContext gc, Mesh grid, int east,
			int north, int up, Statistics pStats, Statistics vStats) {
		double w = gc.getCanvas().getWidth();
		double h = gc.getCanvas().getHeight();
		Cell cell = grid.cell(east, north, up);
		double cellWidth = w / cellsEast;
		double x1 = cellWidth * east;
		double cellHeight = h / cellsNorth;
		double y1 = h - cellHeight * (north + 1);
		double pressure0To1 = (cell.pressure() - pStats.min())
				/ (pStats.max() - pStats.min());
		gc.setFill(toColor(MIN_SATURATION, pressure0To1));
		gc.fillRect(x1, y1, cellWidth, cellHeight);
		gc.strokeRect(x1, y1, cellWidth, cellHeight);

	}

	private static Color toColor(double minSaturation, double prop) {
		return Color
				.hsb(0.0, (prop * (1 - minSaturation) + minSaturation), 1.0);
	}

	private void drawShapes(GraphicsContext gc) {

		gc.setFill(Color.GREEN);
		gc.setStroke(Color.BLUE);
		gc.setLineWidth(5);
		gc.strokeLine(40, 10, 10, 40);
		gc.fillOval(10, 60, 30, 30);
		gc.strokeOval(60, 60, 30, 30);
		gc.fillRoundRect(110, 60, 30, 30, 10, 10);
		gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
		gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
		gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
		gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
		gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
		gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
		gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
		gc.fillPolygon(new double[] { 10, 40, 10, 40 }, new double[] { 210,
				210, 240, 240 }, 4);
		gc.strokePolygon(new double[] { 60, 90, 60, 90 }, new double[] { 210,
				210, 240, 240 }, 4);
		gc.strokePolyline(new double[] { 110, 140, 110, 140 }, new double[] {
				210, 210, 240, 240 }, 4);
	}

	public static void main(String[] args) {
		launch(args);
	}
}