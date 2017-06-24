package models;

import java.io.Serializable;

/**
 * Created by Przemek on 2017-06-24.
 */
public class TradingResult implements Serializable {
    private Asset assetTraded;
    private int tradedUnits;

    public TradingResult(Asset assetTraded, int tradedUnits) {
        this.assetTraded = assetTraded;
        this.tradedUnits = tradedUnits;
    }

    public Asset getAssetTraded() {
        return assetTraded;
    }

    public void setAssetTraded(Asset assetTraded) {
        this.assetTraded = assetTraded;
    }

    public int getTradedUnits() {
        return tradedUnits;
    }

    public void setTradedUnits(int tradedUnits) {
        this.tradedUnits = tradedUnits;
    }
}
