package com.vv.personal.twm.tribalwars.util;

import com.vv.personal.twm.artifactory.generated.tw.SupportReportProto;
import com.vv.personal.twm.artifactory.generated.tw.VillaProto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek
 * @since 21/02/21
 */
public class TwUtil {

    public static boolean isCoordinateValid(String coordinate) {
        return coordinate.matches("[0-9]{3}\\|[0-9]{3}");
    }

    public static double computeDistance(int srcX, int srcY, int destX, int destY) {
        return Math.sqrt(Math.pow(srcX - destX, 2) + Math.pow(srcY - destY, 2));
    }

    public static void computeVillaType(VillaProto.Villa.Builder villa) {
        int sp = villa.getTroops().getSp();
        int sw = villa.getTroops().getSw();
        int ax = villa.getTroops().getAx();
        int ar = villa.getTroops().getAr();
        int lc = villa.getTroops().getLc();

        int semtex = 0; //1,2 -> 1,2,3
        if (ax >= 500 && lc >= 250) semtex = 1;
        if (sp >= 500 || sw >= 500 || ar >= 500) semtex += 2;

        switch (semtex) {
            case 1:
                villa.setType(VillaProto.VillaType.OFF);
                break;
            case 2:
                villa.setType(VillaProto.VillaType.DEF);
                break;
            default:
                villa.setType(VillaProto.VillaType.MIX);
        }
    }


    public static Map<String, SupportReportProto.Troops.Builder> computePlayerXSupportTroops(SupportReportProto.SupportReportType supportReportType, List<SupportReportProto.SupportReport> supportReportList) {
        Map<String, SupportReportProto.Troops.Builder> playerXSupportTroops = new HashMap<>();
        //computing first the total support received ->
        supportReportList.stream()
                .filter(supportReport -> supportReport.getSupportReportType().equals(supportReportType))
                .forEach(supportReport -> {
                    String player = supportReport.getFrom();
                    if (supportReportType == SupportReportProto.SupportReportType.SENT_BACK) player = supportReport.getTo();

                    SupportReportProto.Troops troops = supportReport.getTroops();
                    SupportReportProto.Troops.Builder troopsBuilderFromMap = playerXSupportTroops.get(player);
                    if (troopsBuilderFromMap == null) {
                        SupportReportProto.Troops.Builder troopsBuilder = SupportReportProto.Troops.newBuilder();
                        troopsBuilder.setSp(troops.getSp());
                        troopsBuilder.setSw(troops.getSw());
                        troopsBuilder.setAx(troops.getAx());
                        troopsBuilder.setAr(troops.getAr());
                        troopsBuilder.setSu(troops.getSu());
                        troopsBuilder.setLc(troops.getLc());
                        troopsBuilder.setHc(troops.getHc());
                        troopsBuilder.setMa(troops.getMa());
                        troopsBuilder.setRm(troops.getRm());
                        troopsBuilder.setCt(troops.getCt());
                        troopsBuilder.setPd(troops.getPd());
                        playerXSupportTroops.put(player, troopsBuilder);
                    } else {
                        troopsBuilderFromMap.setSp(troops.getSp() + troopsBuilderFromMap.getSp());
                        troopsBuilderFromMap.setSw(troops.getSw() + troopsBuilderFromMap.getSw());
                        troopsBuilderFromMap.setAx(troops.getAx() + troopsBuilderFromMap.getAx());
                        troopsBuilderFromMap.setAr(troops.getAr() + troopsBuilderFromMap.getAr());
                        troopsBuilderFromMap.setSu(troops.getSu() + troopsBuilderFromMap.getSu());
                        troopsBuilderFromMap.setLc(troops.getLc() + troopsBuilderFromMap.getLc());
                        troopsBuilderFromMap.setHc(troops.getHc() + troopsBuilderFromMap.getHc());
                        troopsBuilderFromMap.setMa(troops.getMa() + troopsBuilderFromMap.getMa());
                        troopsBuilderFromMap.setRm(troops.getRm() + troopsBuilderFromMap.getRm());
                        troopsBuilderFromMap.setCt(troops.getCt() + troopsBuilderFromMap.getCt());
                        troopsBuilderFromMap.setPd(troops.getPd() + troopsBuilderFromMap.getPd());
                    }
                });
        return playerXSupportTroops;
    }

    public static Map<String, SupportReportProto.Troops> computeLostSupportTroops(Map<String, SupportReportProto.Troops.Builder> acquired, Map<String, SupportReportProto.Troops.Builder> returned) {
        Map<String, SupportReportProto.Troops> lostSupportTroops = new HashMap<>();
        acquired.forEach((player, acquiredTroops) -> {
            SupportReportProto.Troops.Builder returnedTroops = returned.get(player);
            SupportReportProto.Troops.Builder troopsBuilder = SupportReportProto.Troops.newBuilder();

            troopsBuilder.setSp(acquiredTroops.getSp() - returnedTroops.getSp());
            troopsBuilder.setSw(acquiredTroops.getSw() - returnedTroops.getSw());
            troopsBuilder.setAx(acquiredTroops.getAx() - returnedTroops.getAx());
            troopsBuilder.setAr(acquiredTroops.getAr() - returnedTroops.getAr());
            troopsBuilder.setSu(acquiredTroops.getSu() - returnedTroops.getSu());
            troopsBuilder.setLc(acquiredTroops.getLc() - returnedTroops.getLc());
            troopsBuilder.setMa(acquiredTroops.getMa() - returnedTroops.getMa());
            troopsBuilder.setHc(acquiredTroops.getHc() - returnedTroops.getHc());
            troopsBuilder.setRm(acquiredTroops.getRm() - returnedTroops.getRm());
            troopsBuilder.setCt(acquiredTroops.getCt() - returnedTroops.getCt());
            troopsBuilder.setPd(acquiredTroops.getPd() - returnedTroops.getPd());

            lostSupportTroops.put(player, troopsBuilder.build());
        });
        return lostSupportTroops;
    }
}
