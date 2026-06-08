import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.JPanel;

public class PortfolioPieChartPanel extends JPanel {
    private Map<String, Double> allocations;
    private static final Color[] PIE_COLORS = {
        new Color(59, 130, 246),   // Blue
        new Color(16, 185, 129),   // Green
        new Color(245, 158, 11),   // Orange
        new Color(239, 68, 68),    // Red
        new Color(139, 92, 246),   // Purple
        new Color(236, 72, 153),   // Pink
        new Color(14, 165, 233),   // Cyan
        new Color(249, 115, 22),   // Amber
        new Color(99, 102, 241),   // Indigo
        new Color(20, 184, 166)    // Teal
    };

    public PortfolioPieChartPanel() {
        setBackground(Theme.CARD_BG);
        setPreferredSize(new Dimension(400, 250));
        this.allocations = new LinkedHashMap<>();
    }

    public synchronized void updateAllocations(Map<String, Double> newAllocations) {
        this.allocations = new LinkedHashMap<>(newAllocations);
        repaint();
    }

    public synchronized void reset() {
        this.allocations.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (allocations.isEmpty()) {
            drawPlaceholder(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2 - 60;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 40;

        double totalValue = allocations.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalValue == 0) {
            drawPlaceholder(g);
            return;
        }

        double startAngle = 0;
        int colorIndex = 0;

        // Draw pie slices
        for (Map.Entry<String, Double> entry : allocations.entrySet()) {
            double value = entry.getValue();
            double sliceAngle = (value / totalValue) * 360;

            Color sliceColor = PIE_COLORS[colorIndex % PIE_COLORS.length];
            g2.setColor(sliceColor);

            Arc2D.Double slice = new Arc2D.Double(
                centerX - radius, centerY - radius,
                radius * 2, radius * 2,
                startAngle, sliceAngle, Arc2D.PIE
            );
            g2.fill(slice);

            // Draw slice border
            g2.setColor(Theme.CARD_BG);
            g2.setStroke(new BasicStroke(2));
            g2.draw(slice);

            startAngle += sliceAngle;
            colorIndex++;
        }

        // Draw legend
        drawLegend(g2, totalValue, width, height);
    }

    private void drawLegend(Graphics2D g2, double totalValue, int width, int height) {
        int legendX = width - 140;
        int legendY = 30;
        int lineHeight = 22;

        g2.setFont(Theme.FONT_BODY_BOLD);
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString("Allocation", legendX, legendY);
        legendY += lineHeight + 8;

        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : allocations.entrySet()) {
            String symbol = entry.getKey();
            double value = entry.getValue();
            double percentage = (value / totalValue) * 100;

            Color sliceColor = PIE_COLORS[colorIndex % PIE_COLORS.length];

            // Draw color box
            g2.setColor(sliceColor);
            g2.fillRect(legendX, legendY - 12, 12, 12);

            // Draw symbol and percentage
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_BODY);
            g2.drawString(String.format("%s (%.1f%%)", symbol, percentage), legendX + 18, legendY);

            legendY += lineHeight;
            colorIndex++;
        }
    }

    private void drawPlaceholder(Graphics g) {
        g.setFont(Theme.FONT_BODY);
        g.setColor(Theme.TEXT_MUTED);
        String text = "Portfolio allocation will appear here";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }
}
