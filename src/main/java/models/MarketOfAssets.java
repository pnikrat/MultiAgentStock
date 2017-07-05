package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemek on 2017-06-21.
 */
public class MarketOfAssets {
    private List<Asset> assetsAvailableOnMarket = new ArrayList<Asset>();
    private String[] assetsNames = {"ALR", "ACP", "BZW", "KGH", "PKN", "PGN"};
    private String[] startPricesBase = {"45.68", "23.45", "34.67", "89.99", "67.34", "53.33"};
    private int[] numberOfUnitsBase = {120, 145, 67, 27, 45, 99}; //{120, 145, 67, 27, 45, 99};

    public MarketOfAssets() {
        for (int i = 0 ; i < assetsNames.length ; i++) {
            assetsAvailableOnMarket.add(new Asset(assetsNames[i],
                    new BigDecimal(startPricesBase[i]), numberOfUnitsBase[i]));
        }
    }

    public List<Asset> getAssetsOnMarket() {
        return assetsAvailableOnMarket;
    }
}
