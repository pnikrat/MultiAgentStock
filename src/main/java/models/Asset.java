package models;

import java.math.BigDecimal;

/**
 * Created by Przemek on 2017-06-21.
 */
public class Asset {
    private String shortName;
    private int numberOfUnits;
    private String unitValueRepresentation;
    private BigDecimal unitValue;

    public Asset(String shortName) {
        this.shortName = shortName;
    }

    public Asset(String shortName, BigDecimal unitValue) {
        this(shortName);
        this.unitValue = unitValue;
        this.unitValueRepresentation = unitValue.toString() + "$";
        this.numberOfUnits = 0;
    }

    public Asset(String shortName, BigDecimal unitValue, int numberOfUnits) {
        this(shortName, unitValue);
        this.numberOfUnits = numberOfUnits;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getNumberOfUnits() {
        return numberOfUnits;
    }

    public void setNumberOfUnits(int numberOfUnits) {
        this.numberOfUnits = numberOfUnits;
    }

    public BigDecimal getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(BigDecimal unitValue) {
        this.unitValue = unitValue;
        this.unitValueRepresentation = unitValue.toString() + "$";
    }

    public String getUnitValueRepresentation() {
        return unitValueRepresentation;
    }
}
