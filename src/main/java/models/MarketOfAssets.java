package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-21.
 */
public class MarketOfAssets {
    private List<Asset> assetsAvailableOnMarket = new ArrayList<Asset>();

    public MarketOfAssets() {
        assetsAvailableOnMarket.add(new Asset("ALR", new BigDecimal("45.68"), 210));
        assetsAvailableOnMarket.add(new Asset("ACP", new BigDecimal("23.45"), 340));
        assetsAvailableOnMarket.add(new Asset("BZW", new BigDecimal("34.67"), 134));
        assetsAvailableOnMarket.add(new Asset("KGH", new BigDecimal("89.99"), 21));
        assetsAvailableOnMarket.add(new Asset("PKN", new BigDecimal("67.34"), 87));
        assetsAvailableOnMarket.add(new Asset("PGN", new BigDecimal("53.33"), 186));
    }

    public List<Asset> getAssetsOnMarket() {
        return assetsAvailableOnMarket;
    }
}
