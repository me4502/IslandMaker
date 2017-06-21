/*
 * Copyright (c) 2017 Me4502 (Madeline Miller)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.me4502.islandmaker;

import com.flowpowered.noise.NoiseQuality;
import com.flowpowered.noise.module.source.Perlin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

public class IslandMaker {

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private JFrame mainWindow;

    private ImageIcon imageIcon;
    private JLabel imageLabel;

    private JFormattedTextField widthField;
    private JFormattedTextField heightField;

    private JFormattedTextField seedField;

    private JSlider islandSizePercentage;
    private JSlider octaveSlider;
    private JSlider qualitySlider;

    private IslandMaker() {
        NumberFormatter intFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        intFormatter.setValueClass(Integer.class);
        intFormatter.setMinimum(0);
        intFormatter.setMaximum(Integer.MAX_VALUE);
        intFormatter.setAllowsInvalid(false);
        intFormatter.setCommitsOnValidEdit(true);

        // Generate main window.
        mainWindow = new JFrame("Island Generator");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setLayout(new BorderLayout());

        // Generate holding panel.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));

        // Generate panel for image.
        JPanel displayPanel = new JPanel();

        imageIcon = new ImageIcon();
        displayPanel.add(imageLabel = new JLabel(imageIcon));

        mainPanel.add(displayPanel);

        // Generate panel for settings.
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new FlowLayout());

        settingsPanel.add(new JLabel("Width:"));
        widthField = new JFormattedTextField(intFormatter);
        widthField.setValue(256);
        widthField.setColumns(8);
        settingsPanel.add(widthField);

        settingsPanel.add(new JLabel("Height:"));
        heightField = new JFormattedTextField(intFormatter);
        heightField.setValue(256);
        heightField.setColumns(8);
        settingsPanel.add(heightField);

        settingsPanel.add(new JLabel("Area Percentage:"));
        islandSizePercentage = new JSlider(0, 100, 50);
        islandSizePercentage.setPaintTicks(true);
        islandSizePercentage.setPaintLabels(true);
        islandSizePercentage.setMajorTickSpacing(25);
        islandSizePercentage.setMinorTickSpacing(5);
        settingsPanel.add(islandSizePercentage);

        settingsPanel.add(new JLabel("Octaves:"));
        octaveSlider = new JSlider(1, Perlin.PERLIN_MAX_OCTAVE, Perlin.DEFAULT_PERLIN_OCTAVE_COUNT);
        octaveSlider.setPaintTicks(true);
        octaveSlider.setPaintLabels(true);
        octaveSlider.setMajorTickSpacing(8);
        octaveSlider.setMinorTickSpacing(2);
        settingsPanel.add(octaveSlider);

        settingsPanel.add(new JLabel("Quality:"));
        qualitySlider = new JSlider(0, NoiseQuality.values().length - 1, Perlin.DEFAULT_PERLIN_QUALITY.ordinal());
        qualitySlider.setPaintTicks(true);
        qualitySlider.setPaintLabels(true);
        qualitySlider.setMajorTickSpacing(1);
        qualitySlider.setMinorTickSpacing(1);
        Hashtable<Integer, JLabel> qualityTable = new Hashtable<>();
        qualityTable.put(0, new JLabel("Low"));
        qualityTable.put(1, new JLabel("Normal"));
        qualityTable.put(2, new JLabel("High"));
        qualitySlider.setLabelTable(qualityTable);
        settingsPanel.add(qualitySlider);

        settingsPanel.add(new JLabel("Seed:"));
        seedField = new JFormattedTextField(intFormatter);
        seedField.setValue(256);
        seedField.setColumns(10);
        settingsPanel.add(seedField);

        JButton randomSeedButton = new JButton("Random Seed");
        randomSeedButton.addActionListener(e -> seedField.setValue(ThreadLocalRandom.current().nextInt()));
        settingsPanel.add(randomSeedButton);

        JButton generateButton = new JButton("Generate Island");
        generateButton.addActionListener(this::generateIsland);
        settingsPanel.add(generateButton);

        mainPanel.add(settingsPanel);

        // Setup the window.
        mainWindow.add(mainPanel);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public static void main(String[] args) {
        new IslandMaker();
    }

    private void generateIsland(ActionEvent e) {
        int width = (int) widthField.getValue();
        int height = (int) heightField.getValue();

        int seed = (int) seedField.getValue();
        int octaves = octaveSlider.getValue();
        int quality = qualitySlider.getValue();

        int islandCentreX = width / 2;
        int islandCentreY = height / 2;

        float islandCutoff = Math.max(0.000000000000001f, islandSizePercentage.getValue() / 100.0f);

        executor.submit(() -> {
            // Generate an image gradient.
            double[][] noiseArray = new double[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double distance = Math.sqrt(Math.pow(islandCentreX - x, 2) + Math.pow(islandCentreY - y, 2)) / islandCutoff;
                    noiseArray[x][y] = 1 - Math.min(1, Math.max(0, distance / Math.max(width, height)));
                }
            }

            Perlin noiseGenerator = new Perlin();
            noiseGenerator.setSeed(seed);
            noiseGenerator.setOctaveCount(octaves);
            noiseGenerator.setNoiseQuality(NoiseQuality.values()[quality]);

            // Add noise to the gradient.
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double noiseOutput = noiseGenerator.getValue(x / (double) width, y / (double) height, 0) / noiseGenerator.getMaxValue();
                    noiseArray[x][y] -= noiseOutput;
                    noiseArray[x][y] = Math.min(1, noiseArray[x][y]);
                    noiseArray[x][y] = Math.max(0, noiseArray[x][y]);
                }
            }


            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics = bufferedImage.createGraphics();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    graphics.setColor(colorForHeight(noiseArray[x][y]));
                    graphics.fillRect(x, y, 1, 1);
                }
            }

            SwingUtilities.invokeLater(() -> {
                imageIcon.setImage(bufferedImage);
                imageLabel.setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
                mainWindow.pack();
                mainWindow.repaint();
            });
        });
    }

    private Color colorForHeight(double height) {
        if (height <= 0.1) {
            return Color.BLUE;
        } else if (height <= 0.25) {
            return Color.CYAN;
        } else if (height <= 0.3) {
            return Color.YELLOW;
        } else if (height <= 0.35) {
            return Color.GREEN;
        } else if (height <= 0.5) {
            return new Color(17, 71, 32);
        } else {
            return Color.WHITE;
        }
    }
}
