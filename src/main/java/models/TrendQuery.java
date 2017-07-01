package models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Przemek on 2017-06-28.
 */
public class TrendQuery implements Serializable {
    private Asset asset;
    private Integer timePeriod;
    private List<BigDecimal> trend;

    public TrendQuery(Asset asset, Integer timePeriod) {
        this.asset = asset;
        this.timePeriod = timePeriod;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Integer getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(Integer timePeriod) {
        this.timePeriod = timePeriod;
    }

    public List<BigDecimal> getTrend() {
        return trend;
    }

    public void setTrend(List<BigDecimal> trend) {
        this.trend = trend;
    }

    public BigDecimal getCurrentPrice() {
        return trend.get(trend.size() - 1);
    }
}
