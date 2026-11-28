package smartrescue.gui;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import smartrescue.service.PortalService;

final class AutoRefresh {
    private AutoRefresh() {}

    static void start(JFrame frame, PortalService service, Runnable refresh) {
        service.hasDataChanged();
        Timer timer = new Timer(2000, null);
        timer.addActionListener(event -> {
            for (Window window : frame.getOwnedWindows()) {
                if (window instanceof Dialog && window.isVisible()) {
                    return;
                }
            }
            try {
                if (service.hasDataChanged()) {
                    refresh.run();
                }
            } catch (IllegalStateException | IllegalArgumentException exception) {
                timer.stop();
                JOptionPane.showMessageDialog(frame,
                        "Automatic refresh stopped: " + exception.getMessage(),
                        "Refresh Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                timer.stop();
            }
        });
        timer.start();
    }
}
