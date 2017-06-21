package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-21.
 */
public class MarketOfAssets {
    private List<Asset> assetsAvailableOnMarket = new ArrayList<Asset>();

    public MarketOfAssets() {
        assetsAvailableOnMarket.add(new Asset("ALIOR"));
        assetsAvailableOnMarket.add(new Asset("ASSECOPOL"));
        assetsAvailableOnMarket.add(new Asset("BZWBK"));
        assetsAvailableOnMarket.add(new Asset("KGHM"));
        assetsAvailableOnMarket.add(new Asset("PKNORLEN"));
    }

    public List<Asset> getAssetsOnMarket() {
        return assetsAvailableOnMarket;
    }
}
