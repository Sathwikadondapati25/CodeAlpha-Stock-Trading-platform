import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class StockChartPanel extends JPanel {
    private Stock stock;
    private String currentTimeframe = "1D";
    private JComboBox<String> timeframeSelector;

    public StockChartPanel() {
        setBackground(Theme.CARD_BG);
        setPreferredSize(new Dimension(300, 200));
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Timeframe selector
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setOpaque(false);

        String[] timeframes = {"1D", "1W", "1M", "1Y"};
        timeframeSelector = new JComboBox<>(timeframes);
        timeframeSelector.setSelectedItem("1D");
        timeframeSelector.setFont(Theme.FONT_SMALL);
        timeframeSelector.setBackground(Theme.CARD_BG);
        timeframeSelector.setForeground(Theme.TEXT_PRIMARY);
        timeframeSelector.addActionListener(e -> {
            currentTimeframe = (String) timeframeSelector.getSelectedItem();
            repaint();
        });

        headerPanel.add(timeframeSelector);
        add(headerPanel, BorderLayout.NORTH);
    }

    public synchronized void setStock(Stock stock) {
        this.stock = stock;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (stock == null) {
            drawPlaceholder(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 30;

        List<Double> prices;
        double openPrice;
        synchronized (stock) {
            prices = stock.getPriceHistory(currentTimeframe);
            openPrice = stock.getOpenPrice();
        }

        if (prices.size() < 2) {
            drawPlaceholder(g);
            return;
        }

        // Find min and max prices to scale the y-axis
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        for (double price : prices) {
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;
        }

        // Add a bit of padding above/below min/max
        double priceRange = maxPrice - minPrice;
        if (priceRange == 0) {
            minPrice -= 10.0;
            maxPrice += 10.0;
            priceRange = maxPrice - minPrice;
        } else {
            minPrice -= priceRange * 0.1;
            maxPrice += priceRange * 0.1;
            priceRange = maxPrice - minPrice;
        }

        // Draw horizontal grid lines
        g2.setColor(new Color(51, 65, 85, 120)); // grid line color
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = padding + i * (height - 2 * padding) / gridLines;
            g2.drawLine(padding, y, width - padding - 75, y);

            // Draw price labels on the right
            double priceVal = maxPrice - i * priceRange / gridLines;
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(String.format("₹%.2f", priceVal), width - padding - 70, y + 4);
        }

        // Plot the price points
        int pointsCount = prices.size();
        double xScale = (double) (width - 2 * padding - 75) / (pointsCount - 1);
        double yScale = (double) (height - 2 * padding) / priceRange;

        Path2D.Double path = new Path2D.Double();
        Path2D.Double area = new Path2D.Double();

        for (int i = 0; i < pointsCount; i++) {
            double x = padding + i * xScale;
            double y = height - padding - (prices.get(i) - minPrice) * yScale;

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

        // Check if price is up or down overall
        double currentPrice = prices.get(pointsCount - 1);
        Color themeColor = currentPrice >= openPrice ? Theme.GAIN : Theme.LOSS;

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

        // Draw the last point (highlight circle)
        double lastX = padding + (pointsCount - 1) * xScale;
        double lastY = height - padding - (currentPrice - minPrice) * yScale;
        g2.setColor(themeColor);
        g2.fill(new Ellipse2D.Double(lastX - 4, lastY - 4, 8, 8));
        g2.setColor(Color.WHITE);
        g2.draw(new Ellipse2D.Double(lastX - 4, lastY - 4, 8, 8));
    }

    private void drawPlaceholder(Graphics g) {
        g.setFont(Theme.FONT_BODY);
        g.setColor(Theme.TEXT_MUTED);
        String text = "Select a stock to view its live chart";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }
}
