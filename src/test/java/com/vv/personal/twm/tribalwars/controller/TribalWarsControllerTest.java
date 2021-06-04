package com.vv.personal.twm.tribalwars.controller;

import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek
 * @since 04/06/21
 */
@RunWith(JUnit4.class)
public class TribalWarsControllerTest {

    @Test
    public void testGenerateMarketOrders() {
        VillaProto.Villa.Builder villa = VillaProto.Villa.newBuilder();
        villa.setAvailableMerchants(46);
        villa.setResources(VillaProto.Resources.newBuilder()
                .setCurrentWood(9126)
                .setCurrentClay(117889)
                .setCurrentIron(14788)
                .setWarehouseCapacity(400000)
                .build());

        List<TribalWarsController.MarketOrder> marketOrders = TribalWarsController.generateMarketOrders(villa.build());
        System.out.println(marketOrders);
        assertEquals(25, marketOrders.get(0).getOrdersToPlace());
        assertEquals("res_sell_stone", marketOrders.get(0).getRes_sell_id());
        assertEquals(21, marketOrders.get(1).getOrdersToPlace());
        assertEquals("[{\"res_sell_id\":\"res_sell_stone\",\"sell_units\":1000,\"res_buy_id\":\"res_buy_wood\",\"buy_units\":1000,\"ordersToPlace\":25}, {\"res_sell_id\":\"res_sell_stone\",\"sell_units\":1000,\"res_buy_id\":\"res_buy_iron\",\"buy_units\":1000,\"ordersToPlace\":21}]", marketOrders.toString());
    }
}