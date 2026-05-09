package dev.nasuta.tmc.lab2;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.geotools.swing.data.JFileDataStoreChooser;

public final class UiSelectors {
    private UiSelectors() {
    }

    public static InputMode askInputMode() {
        Object[] options = { "Locations (.csv)", "Buildings (.dat)", "Roads (.dat)" };
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select input data type",
                "Import Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        return switch (choice) {
            case 0 -> InputMode.LOCATIONS;
            case 1 -> InputMode.BUILDINGS;
            case 2 -> InputMode.ROADS;
            default -> null;
        };
    }

    public static File chooseInputFile(InputMode mode) {
        return switch (mode) {
            case LOCATIONS -> JFileDataStoreChooser.showOpenFile("csv", new File("data"), null);
            case BUILDINGS, ROADS -> JFileDataStoreChooser.showOpenFile("dat", new File("data"), null);
        };
    }

    public static File chooseOutputShapeFile(File inputFile) {
        String path = inputFile.getAbsolutePath();
        int extensionStart = path.lastIndexOf('.');
        String newPath = (extensionStart >= 0 ? path.substring(0, extensionStart) : path) + ".shp";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Shapefile");
        chooser.setSelectedFile(new File(newPath));

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (!selected.getName().endsWith(".shp")) {
                selected = new File(selected.getAbsolutePath() + ".shp");
            }
            return selected;
        }
        return null;
    }
}
