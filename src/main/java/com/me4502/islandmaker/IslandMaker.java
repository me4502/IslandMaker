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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private JFormattedTextField widthField;
    private JFormattedTextField heightField;

    private JSlider islandSizePercentage;

    public IslandMaker() {
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
        displayPanel.add(new JLabel(imageIcon));

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

    public void generateIsland(ActionEvent e) {
        int width = (int) widthField.getValue();
        int height = (int) heightField.getValue();

        int islandCentreX = width / 2;
        int islandCentreY = height / 2;

        float islandCutoff = islandSizePercentage.getValue() / 100.0f;

        executor.submit(() -> {
            // Generate an image gradient.
            float[][] gradient = new float[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    float distance = (float) Math.sqrt(Math.pow(islandCentreX - x, 2) + Math.pow(islandCentreY - y, 2)) * islandCutoff;
                    gradient[x][y] = distance / (Math.max(width, height) / islandCutoff);
                }
            }

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics = bufferedImage.createGraphics();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    graphics.setColor(new Color(gradient[x][y], gradient[x][y], gradient[x][y]));
                    graphics.fillRect(x, y, 1, 1);
                }
            }

            SwingUtilities.invokeLater(() -> {
                imageIcon.setImage(bufferedImage);
                mainWindow.pack();
                mainWindow.repaint();
            });
        });
    }
}
