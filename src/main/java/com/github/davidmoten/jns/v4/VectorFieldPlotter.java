package com.github.davidmoten.jns.v4;

import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.Chart;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class VectorFieldPlotter {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {

        System.setProperty("jogl.disable.openglcore", "false");

        // Define a function to plot
        Mapper mapper = new Mapper() {
            public double f(double x, double y) {
                return 5 * Math.sin(x / 10) / (1 + Math.exp(-y/35));
            }
        };

        // Define range and precision for the function to plot
        Range range = new Range(-100, 100);
        int steps = 50;
        
        Range functionRange = new Range(-5, 5);

        // Create a surface drawing that function
        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), functionRange));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(true);
        surface.setWireframeColor(Color.BLACK);

        // Create a chart and add the surface
        Chart chart = new AWTChart(Quality.Advanced);
        chart.add(surface);
        chart.open("Jzy3d Demo", WIDTH, HEIGHT);
        chart.addMouseCameraController();
    }
}