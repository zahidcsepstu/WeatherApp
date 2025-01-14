package com.example.application.views.common;

import com.example.application.utils.DateTimeUtil;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ChartView extends VerticalLayout {
    private final Div chartDiv;
    private final String fullJsCode;

    public ChartView(List<String> labels, List<List<Double>> values, List<String> commonLabel) {
        chartDiv = new Div();
        fullJsCode = constructJsCode(labels, values, commonLabel);

        String script = "<script>" + fullJsCode + "</script>";
        chartDiv.getElement().setProperty("innerHTML", "<canvas id=\"myChart\"></canvas>" + script);
    }

    private String constructJsCode(List<String> labels, List<List<Double>> values, List<String> seriesLabels) {
        String formattedLabels = formatLabels(labels);
        StringBuilder dataSets = getPreparedDataSets(values, seriesLabels);

        return String.format("var ctx = document.getElementById('myChart').getContext('2d');" +
                "var myChart = new Chart(ctx, {" +
                "type: 'line'," +
                "data: {" +
                "labels: [%s]," +
                "datasets: [%s]" +
                "}," +
                "options: {" +
                "responsive: true," +
                "interaction: {" +
                "      mode: 'index'," +
                "      intersect: false" +
                "    }," +
                "    plugins: {" +
                "      tooltip: {" +
                "        mode: 'index'," +
                "        intersect: false" +
                "      }" +
                "    }," +
                "    hover: {" +
                "      mode: 'index'," +
                "      intersect: false" +
                "    }," +
                "scales: {" +
                "      x: {" +
                "        display: true," +
                "        title: {" +
                "          display: true," +
                "          text: 'Month'" +
                "        }" +
                "      }," +
                "      y: {" +
                "        display: true," +
                "        beginAtZero: true," +
                "        title: {" +
                "          display: true," +
                "          text: 'Value'" +
                "        }" +
                "      }" +
                "    }" +
                "}" +
                "});", formattedLabels, dataSets);
    }


    private StringBuilder getPreparedDataSets(List<List<Double>> values, List<String> seriesLabels) {
        StringBuilder dataSets = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            dataSets.append("{")
                    .append("label: '").append(seriesLabels.get(i)).append("',")
                    .append("data: [").append(formatValues(values.get(i))).append("],")
                    .append("borderWidth: 1")
                    .append("}");
            if (i < values.size() - 1) {
                dataSets.append(",");
            }
        }
        return dataSets;
    }

    private String formatLabels(List<String> labels) {
        return labels.stream()
                .map(DateTimeUtil::convertDateTimeStringToTimeAmPmString)
                .collect(Collectors.joining("','", "'", "'"));
    }

    private String formatValues(List<Double> values) {
        return values.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        log.debug("Attaching chart to view");

        super.onAttach(attachEvent);

        chartDiv.getElement().executeJs(fullJsCode);
        add(chartDiv);
        chartDiv.setSizeFull();
        this.setWidthFull();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        log.debug("Detaching chart from view");

        chartDiv.getElement().executeJs("var chart = document.getElementById('myChart'); if (chart) { chart.getContext('2d').clearRect(0, 0, chart.width, chart.height); }");
        super.onDetach(detachEvent);
    }
}
