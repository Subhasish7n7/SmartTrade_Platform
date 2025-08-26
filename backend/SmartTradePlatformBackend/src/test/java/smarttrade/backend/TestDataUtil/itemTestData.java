package smarttrade.backend.TestDataUtil;

import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.entities.userEntity;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class itemTestData {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public static itemEntity CreateItem1(userEntity userEntity){
        List<String> labels= new ArrayList<>();
        labels.add("arhounmbd");
        labels.add("arhounmb");
        labels.add("arhounm");
        labels.add("arhoun");
        labels.add("arhou");

        Point location = geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)); // lng, lat order!

        return itemEntity.builder().
                item_name("mouse").
                item_NewPrice(123).
                item_GeneratedPrice(1234).
                item_UserPrice(12345).
                user(userEntity).
                description("gaming").
                category("electronic").
                condition("old").
                labels(labels).
                location(location).
                isAvailable(true).
                isForTrade(false).
                isForSale(true).
                build();
    }

    public static itemEntity CreateItem2(userEntity userEntity){
        List<String> labels= new ArrayList<>();
        labels.add("kasdcjadjkn");
        labels.add("kasdcjadjk");
        labels.add("kasdcjadj");
        labels.add("kasdcjad");
        labels.add("kasdcja");

        Point location = geometryFactory.createPoint(new Coordinate(-118.2437, 34.0522)); // lng, lat

        return itemEntity.builder().
                item_name("keyboard").
                item_NewPrice(456).
                item_GeneratedPrice(4567).
                item_UserPrice(45678).
                user(userEntity).
                description("gaming").
                category("electronic").
                condition("new").
                labels(labels).
                location(location).
                isAvailable(true).
                isForTrade(false).
                isForSale(true).
                build();
    }
}
