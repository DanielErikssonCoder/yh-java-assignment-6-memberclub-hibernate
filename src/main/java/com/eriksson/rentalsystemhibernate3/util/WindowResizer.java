package com.eriksson.rentalsystemhibernate3.util;

import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WindowResizer {

    private static double startX = 0;
    private static double startY = 0;
    private static double startStageX = 0;
    private static double startStageY = 0;
    private static double startWidth = 0;
    private static double startHeight = 0;

    private static Cursor dragMode = Cursor.DEFAULT;
    private static final int BORDER = 10;

    private static final double MIN_WIDTH = 1280;
    private static final double MIN_HEIGHT = 800;

    public static void addResizability(Stage stage, Parent root) {

        // Updates window cursor to reflect possible resize actions
        root.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            Cursor cursor = Cursor.DEFAULT;

            // Determines resize cursor from edge proximity
            if (x < BORDER && y < BORDER) cursor = Cursor.NW_RESIZE;
            else if (x < BORDER && y > height - BORDER) cursor = Cursor.SW_RESIZE;
            else if (x > width - BORDER && y < BORDER) cursor = Cursor.NE_RESIZE;
            else if (x > width - BORDER && y > height - BORDER) cursor = Cursor.SE_RESIZE;
            else if (x < BORDER) cursor = Cursor.W_RESIZE;
            else if (x > width - BORDER) cursor = Cursor.E_RESIZE;
            else if (y < BORDER) cursor = Cursor.N_RESIZE;
            else if (y > height - BORDER) cursor = Cursor.S_RESIZE;

            root.setCursor(cursor);
        });

        // Captures initial cursor and stage geometry for dragging
        root.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {

            dragMode = root.getCursor();

            startX = event.getScreenX();
            startY = event.getScreenY();

            startStageX = stage.getX();
            startStageY = stage.getY();
            startWidth = stage.getWidth();
            startHeight = stage.getHeight();
        });

        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {

            // Moves or resizes stage during drag, respecting constraints
            if (dragMode == Cursor.DEFAULT) {

                stage.setX(startStageX + (event.getScreenX() - startX));
                stage.setY(startStageY + (event.getScreenY() - startY));

            } else {

                double deltaX = event.getScreenX() - startX;
                double deltaY = event.getScreenY() - startY;

                // Adjusts stage width and horizontal position during edge drags
                if (dragMode == Cursor.E_RESIZE || dragMode == Cursor.NE_RESIZE || dragMode == Cursor.SE_RESIZE) {

                    if (startWidth + deltaX >= MIN_WIDTH) {
                        stage.setWidth(startWidth + deltaX);
                    }
                }

                else if (dragMode == Cursor.W_RESIZE || dragMode == Cursor.NW_RESIZE || dragMode == Cursor.SW_RESIZE) {

                    // Adjusts stage width leftward and repositions X within bounds
                    if (startWidth - deltaX >= MIN_WIDTH) {
                        stage.setWidth(startWidth - deltaX);
                        stage.setX(startStageX + deltaX);
                    }
                }

                // Adjusts stage dimensions based on drag direction while enforcing minimum size
                if (dragMode == Cursor.S_RESIZE || dragMode == Cursor.SW_RESIZE || dragMode == Cursor.SE_RESIZE) {
                    if (startHeight + deltaY >= MIN_HEIGHT) {
                        stage.setHeight(startHeight + deltaY);
                    }
                }

                else if (dragMode == Cursor.N_RESIZE || dragMode == Cursor.NW_RESIZE || dragMode == Cursor.NE_RESIZE) {

                    // Adjusts stage height upward while maintaining minimum size
                    if (startHeight - deltaY >= MIN_HEIGHT) {
                        stage.setHeight(startHeight - deltaY);
                        stage.setY(startStageY + deltaY);
                    }
                }
            }
        });
    }
}