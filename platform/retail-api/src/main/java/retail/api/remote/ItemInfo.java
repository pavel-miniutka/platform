package retail.api.remote;

import java.io.Serializable;
import java.util.Date;

public class ItemInfo implements Serializable {
    public String barcodeEx;
    public String name;
    public Double price;
    public Double daysExpiry;
    public Integer hoursExpiry;
    public Date expirationDate;
    public Integer labelFormat;
    public String composition;
    public boolean isWeightItem;
    public Integer numberGroupItem;
    public String nameGroupItem;

    public ItemInfo(String barcodeEx, String name, Double price, Double daysExpiry, Integer hoursExpiry, Date expirationDate,
                    Integer labelFormat, String composition, boolean isWeightItem, Integer numberGroupItem, String nameGroupItem) {
        this.barcodeEx = barcodeEx;
        this.name = name;
        this.price = price;
        this.daysExpiry = daysExpiry;
        this.hoursExpiry = hoursExpiry;
        this.expirationDate = expirationDate;
        this.labelFormat = labelFormat;
        this.composition = composition;
        this.isWeightItem = isWeightItem;
        this.numberGroupItem = numberGroupItem;
        this.nameGroupItem = nameGroupItem;
    }
}
