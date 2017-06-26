package gui;


import models.Asset;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Przemek on 2017-06-26.
 */
public class HistorianGui extends JFrame {
    private JPanel pricesChartPanel;
    private final XYChart pricesChart;
    private List<Asset> startupData = new ArrayList<Asset>();
    private Map<String, List<BigDecimal>> chartYData = new HashMap<String, List<BigDecimal>>();
    private List<Integer> chartXData = new ArrayList<Integer>();

    public HistorianGui(List<Asset> assets) {
        super("Historian");
        pricesChart = new XYChartBuilder().width(600).height(400).title("Stock prices history")
                .xAxisTitle("Trading session [-]").yAxisTitle("Price [$]").build();
        pricesChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        startupData.addAll(assets);
        initChartData();
        initChartSeries();
    }

    public void showGui() {
        this.setLayout(new BorderLayout());
        pricesChartPanel = new XChartPanel<XYChart>(pricesChart);
        this.add(pricesChartPanel, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
    }

    private void initChartData() {
        chartXData.add(0);
        for (Asset a : startupData) {
            List<BigDecimal> prices = new ArrayList<BigDecimal>();
            prices.add(a.getUnitValue());
            chartYData.put(a.getShortName(), prices);
        }
    }

    private void initChartSeries() {
        for (Map.Entry<String, List<BigDecimal>> entry : chartYData.entrySet()) {
            pricesChart.addSeries(entry.getKey(), chartXData, entry.getValue());
        }
    }
}
