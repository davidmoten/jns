package com.github.davidmoten.jns;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

public class RegularGridGui extends Application {

	private RegularGrid grid;

	@Override
	public void init() throws Exception {
		grid = RegularGrid.builder().cellSize(1).cellsEast(10).cellsNorth(10)
				.cellsUp(1).build();
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Drawing Operations Test");
		Group root = new Group();
		Canvas canvas = new Canvas(300, 250);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		drawGrid(gc, grid);
		// drawShapes(gc);
		root.getChildren().add(canvas);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	private void drawGrid(GraphicsContext gc, RegularGrid grid) {

		double pSum = 0;
		double pSumSquares = 0;
		int pCount = 0;
		double vSum = 0;
		double vSumSquares = 0;
		int vCount = 0;

		// get stats
		for (int east = 0; east <= grid.maxIndexEast(); east++)
			for (int north = 0; north <= grid.maxIndexNorth(); north++) {
				Cell cell = grid.cell(east, north, grid.maxIndexUp());
				double p = cell.pressure();
				pSum += p;
				pSumSquares += p * p;
				pCount++;
				double v = cell.velocity().magnitude();
				vSum += v;
				vSumSquares += v * v;
				vCount++;
			}
		double pMean = pSum / pCount;
		double pVariance = pSumSquares / pCount - pMean * pMean;
		double pSd = Math.sqrt(pVariance);

		double vMean = vSum / vCount;
		double vVariance = vSumSquares / vCount - vMean * vMean;
		double vSd = Math.sqrt(vVariance);

		for (int east = 0; east <= grid.maxIndexEast(); east++)
			for (int north = 0; north <= grid.maxIndexNorth(); north++) {
				drawCell(gc, grid, east, north, grid.maxIndexUp());
			}
	}

	private void drawCell(GraphicsContext gc, RegularGrid grid, int east,
			int north, int up) {
		double w = gc.getCanvas().getWidth();
		double h = gc.getCanvas().getHeight();
		Cell cell = grid.cell(east, north, up);
		double cellWidth = w / (grid.maxIndexEast() + 1);
		double x1 = cellWidth * east;
		double cellHeight = h / (grid.maxIndexNorth() + 1);
		double y1 = h - cellHeight * (north + 1);
		gc.setFill(toColor(0.05, Math.random()));
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