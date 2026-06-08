import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public class NotificationManager {
    private static NotificationManager instance;
    private JFrame parentFrame;
    private List<JDialog> activeNotifications = new ArrayList<>();

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    public void showNotification(String message, NotificationType type) {
        if (parentFrame == null) return;

        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(parentFrame, false);
            dialog.setUndecorated(true);
            dialog.setSize(350, 60);
            dialog.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createLineBorder(type.borderColor, 2, true));

            JLabel iconLabel = new JLabel(type.icon);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            panel.add(iconLabel, BorderLayout.WEST);

            JLabel messageLabel = new JLabel(message);
            messageLabel.setFont(Theme.FONT_BODY);
            messageLabel.setForeground(Theme.TEXT_PRIMARY);
            messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            panel.add(messageLabel, BorderLayout.CENTER);

            dialog.add(panel);

            // Position in top-right corner
            Point parentLocation = parentFrame.getLocation();
            int x = parentLocation.x + parentFrame.getWidth() - dialog.getWidth() - 20;
            int y = parentLocation.y + 20;
            dialog.setLocation(x, y);

            dialog.setVisible(true);
            activeNotifications.add(dialog);

            // Auto-dismiss after 3 seconds
            Timer timer = new Timer(3000, e -> {
                dialog.dispose();
                activeNotifications.remove(dialog);
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    public void showSuccess(String message) {
        showNotification(message, NotificationType.SUCCESS);
    }

    public void showError(String message) {
        showNotification(message, NotificationType.ERROR);
    }

    public void showInfo(String message) {
        showNotification(message, NotificationType.INFO);
    }

    public enum NotificationType {
        SUCCESS("✓", Theme.GAIN),
        ERROR("✗", Theme.LOSS),
        INFO("ℹ", Theme.ACCENT);

        String icon;
        Color borderColor;

        NotificationType(String icon, Color borderColor) {
            this.icon = icon;
            this.borderColor = borderColor;
        }
    }
}
