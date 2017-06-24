package models;

import java.io.Serializable;

/**
 * Created by Przemek on 2017-06-24.
 */
public class Order implements Serializable {
    private Asset assetToTrade;
    private boolean isBuy;
    private int unitsToTrade;

    public Order(Asset assetToTrade, boolean isBuy, int unitsToTrade) {
        this.assetToTrade = assetToTrade;
        this.isBuy = isBuy;
        this.unitsToTrade = unitsToTrade;
    }

    public Asset getAssetToTrade() {
        return assetToTrade;
    }

    public void setAssetToTrade(Asset assetToTrade) {
        this.assetToTrade = assetToTrade;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public void setBuy(boolean buy) {
        isBuy = buy;
    }

    public int getUnitsToTrade() {
        return unitsToTrade;
    }

    public void setUnitsToTrade(int unitsToTrade) {
        this.unitsToTrade = unitsToTrade;
    }
}
