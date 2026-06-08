import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class PortfolioChartPanel extends JPanel {
    private List<Double> portfolioHistory;
    private static final int MAX_HISTORY = 50;

    public PortfolioChartPanel() {
        setBackground(Theme.CARD_BG);
        setPreferredSize(new Dimension(400, 200));
        this.portfolioHistory = new ArrayList<>();
    }

    public synchronized void updatePortfolioValue(double value) {
        portfolioHistory.add(value);
        if (portfolioHistory.size() > MAX_HISTORY) {
            portfolioHistory.remove(0);
        }
        repaint();
    }

    public synchronized void reset() {
        portfolioHistory.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (portfolioHistory.isEmpty()) {
            drawPlaceholder(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 30;

        // Find min and max values
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for (double val : portfolioHistory) {
            if (val < minValue) minValue = val;
            if (val > maxValue) maxValue = val;
        }

        // Add padding to range
        double valueRange = maxValue - minValue;
        if (valueRange == 0) {
            minValue -= 1000.0;
            maxValue += 1000.0;
            valueRange = maxValue - minValue;
        } else {
            minValue -= valueRange * 0.1;
            maxValue += valueRange * 0.1;
            valueRange = maxValue - minValue;
        }

        // Draw horizontal grid lines
        g2.setColor(new Color(51, 65, 85, 120));
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = padding + i * (height - 2 * padding) / gridLines;
            g2.drawLine(padding, y, width - padding - 75, y);

            double priceVal = maxValue - i * valueRange / gridLines;
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(String.format("₹%.0f", priceVal), width - padding - 70, y + 4);
        }

        // Plot the portfolio value points
        int pointsCount = portfolioHistory.size();
        double xScale = (double) (width - 2 * padding - 75) / (pointsCount - 1);
        double yScale = (double) (height - 2 * padding) / valueRange;

        Path2D.Double path = new Path2D.Double();
        Path2D.Double area = new Path2D.Double();

        for (int i = 0; i < pointsCount; i++) {
            double x = padding + i * xScale;
            double y = height - padding - (portfolioHistory.get(i) - minValue) * yScale;

            if (i == 0) {
                path.moveTo(x, y);
                area.moveTo(x, height - padding);
                area.lineTo(x, y);
            } else {
                path.lineTo(x, y);
                area.lineTo(x, y);
            }
            if (i == pointsCount - 1) {
                area.lineTo(x, height - padding);
                area.closePath();
            }
        }

        // Determine color based on overall performance
        double startValue = portfolioHistory.get(0);
        double endValue = portfolioHistory.get(pointsCount - 1);
        Color themeColor = endValue >= startValue ? Theme.GAIN : Theme.LOSS;

        // Fill area under chart with gradient
        g2.setPaint(new GradientPaint(
            0, padding, new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 60),
            0, height - padding, new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 0)
        ));
        g2.fill(area);

        // Draw the outline path
        g2.setColor(themeColor);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);

        // Draw the last point
        double lastX = padding + (pointsCount - 1) * xScale;
        double lastY = height - padding - (endValue - minValue) * yScale;
        g2.setColor(themeColor);
        g2.fill(new Ellipse2D.Double(lastX - 4, lastY - 4, 8, 8));
        g2.setColor(Color.WHITE);
        g2.draw(new Ellipse2D.Double(lastX - 4, lastY - 4, 8, 8));
    }

    private void drawPlaceholder(Graphics g) {
        g.setFont(Theme.FONT_BODY);
        g.setColor(Theme.TEXT_MUTED);
        String text = "Portfolio value history will appear here";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }
}
