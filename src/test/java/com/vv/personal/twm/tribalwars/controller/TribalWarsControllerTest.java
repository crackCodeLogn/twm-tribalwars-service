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

        List<TribalWarsController.MarketOrder> marketOrders = TribalWarsController.generateMarketOrders(villa.build(), 46);
        System.out.println(marketOrders);
        assertEquals(2, marketOrders.size());
        assertEquals(28, marketOrders.get(0).getOrdersToPlace());
        assertEquals("res_sell_stone", marketOrders.get(0).getRes_sell_id());
        assertEquals("res_buy_wood", marketOrders.get(0).getRes_buy_id());
        assertEquals(18, marketOrders.get(1).getOrdersToPlace());
        assertEquals("res_buy_iron", marketOrders.get(1).getRes_buy_id());
    }

    @Test
    public void testGenerateMarketOrders2() {
        VillaProto.Villa.Builder villa = VillaProto.Villa.newBuilder();
        villa.setAvailableMerchants(131);
        villa.setResources(VillaProto.Resources.newBuilder()
                .setCurrentWood(2121)
                .setCurrentClay(117889)
                .setCurrentIron(120111)
                .setWarehouseCapacity(400000)
                .build());

        List<TribalWarsController.MarketOrder> marketOrders = TribalWarsController.generateMarketOrders(villa.build(), 100);
        System.out.println(marketOrders);
        assertEquals(2, marketOrders.size());
        assertEquals("res_sell_iron", marketOrders.get(0).getRes_sell_id());
        assertEquals("res_buy_wood", marketOrders.get(0).getRes_buy_id());
        assertEquals(30, marketOrders.get(0).getOrdersToPlace());
        assertEquals("res_sell_stone", marketOrders.get(1).getRes_sell_id());
        assertEquals("res_buy_wood", marketOrders.get(1).getRes_buy_id());
        assertEquals(15, marketOrders.get(1).getOrdersToPlace());
    }
}