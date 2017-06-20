package com.me4502.islandmaker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;

public class IslandMaker {

    public static void main(String[] args) {
        JFrame mainWindow = new JFrame("Island Generator");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));

        JPanel displayPanel = new JPanel();
        mainPanel.add(displayPanel);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new FlowLayout());

        settingsPanel.add(new JLabel("Width"));
        JSlider widthSlider = new JSlider();
        settingsPanel.add(widthSlider);

        settingsPanel.add(new JLabel("Height"));
        JSlider heightSlider = new JSlider();
        settingsPanel.add(heightSlider);

        JButton generateButton = new JButton("Generate Island");
        settingsPanel.add(generateButton);

        mainPanel.add(settingsPanel);

        mainWindow.add(mainPanel);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }
}
