package com.vv.personal.twm.tribalwars.controller;

import com.vv.personal.twm.artifactory.generated.tw.HtmlDataParcelProto;
import com.vv.personal.twm.artifactory.generated.tw.SupportReportProto;
import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import com.vv.personal.twm.ping.processor.Pinger;
import com.vv.personal.twm.tribalwars.automation.config.TribalWarsConfiguration;
import com.vv.personal.twm.tribalwars.automation.engine.Engine;
import com.vv.personal.twm.tribalwars.constants.Constants;
import com.vv.personal.twm.tribalwars.feign.MongoServiceFeign;
import com.vv.personal.twm.tribalwars.feign.RenderServiceFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.vv.personal.twm.tribalwars.automation.constants.Constants.*;

/**
 * @author Vivek
 * @since 29/11/20
 */
@RestController("TribalWarsController")
@RequestMapping("/tw/")
public class TribalWarsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TribalWarsController.class);

    @Autowired
    private MongoServiceFeign mongoServiceFeign;

    @Autowired
    private TribalWarsConfiguration tribalWarsConfiguration;

    @Autowired
    private RenderServiceFeign renderServiceFeign;

    @Autowired
    private Pinger pinger;

    private VillaProto.VillaList freshVillaList = null;

    @PostMapping("/addVilla")
    public String addVilla(@RequestBody VillaProto.Villa newVilla) {
        LOGGER.info("Received new Villa to be added to Mongo: {}", newVilla);
        try {
            return mongoServiceFeign.addVilla(newVilla);
        } catch (Exception e) {
            LOGGER.error("Failed to add {} to mongo! ", newVilla.getId(), e);
        }
        return "FAILED";
    }

    @PostMapping("/deleteVilla")
    public String deleteVilla(@RequestBody String villaId) {
        LOGGER.info("Received TW-ID to delete: {}", villaId);
        try {
            return mongoServiceFeign.deleteVilla(villaId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete TW-ID: {} from mongo! ", villaId, e);
        }
        return "FAILED";
    }

    @GetMapping("/read/all")
    public VillaProto.VillaList readAllVillasForWorld(@RequestParam(defaultValue = "p") String worldType,
                                                      @RequestParam(defaultValue = "9") int worldNumber) {
        //Strange, somehow this tab doesn't open in swagger. Rest all do though.
        String world = "en" + worldType + worldNumber;
        LOGGER.info("Obtained request to read all villas for world {}", world);
        try {
            VillaProto.VillaList villaList = mongoServiceFeign.readAllVillasFromMongo(world);
            LOGGER.info("Obtained {} villas for world {} from mongo", villaList.getVillasCount(), world);
            return villaList;
        } catch (Exception e) {
            LOGGER.error("Failed to get villas from mongo for world {}", world, e);
        }
        return VillaProto.VillaList.newBuilder().build();
    }

    @GetMapping("/triggerAutomation/troops")
    public String triggerAutomationForTroops(@RequestParam(defaultValue = "p") String worldType,
                                             @RequestParam(defaultValue = "9") int worldNumber,
                                             @RequestParam(defaultValue = "false") boolean writeBackToMongo) {
        LOGGER.info("Will start automated extraction of troops count for en{}{}", worldType, worldNumber);
        if (writeBackToMongo && !pinger.allEndPointsActive(mongoServiceFeign, renderServiceFeign) || !pinger.allEndPointsActive(renderServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return "END-POINTS NOT READY!";
        }
        LOGGER.info("All required endpoints active. Initiating run!");

        final Engine engine = new Engine(tribalWarsConfiguration.driver(), tribalWarsConfiguration.sso(), worldType, worldNumber);
        String overviewHtml = engine.extractOverviewDetailsForWorld(); //keeps session open for further op!
        LOGGER.info("Extracted Overview html from world. Length: {}", overviewHtml.length());
        VillaProto.VillaList villaListBuilder = renderServiceFeign.parseTribalWarsOverviewHtml(overviewHtml);
        LOGGER.info("{}", villaListBuilder);

        final VillaProto.VillaList.Builder resultantVillaListBuilder = VillaProto.VillaList.newBuilder();
        villaListBuilder.getVillasList().forEach(villa -> {
            Map<SCREENS_TO_EXTRACT_AUTOMATED_INFO, String> screensStringMap = new HashMap<>();
            Arrays.stream(SCREENS_TO_EXTRACT_AUTOMATED_INFO.values()).forEach(screen -> {
                String urlToHit = String.format(TW_SCREEN, worldType, worldNumber, villa.getId(), screen.name().toLowerCase());
                engine.getDriver().loadUrl(urlToHit);
                String screenHtml = engine.getDriver().getDriver().getPageSource();
                screensStringMap.put(screen, screenHtml);
            });

            VillaProto.Troops troops = renderServiceFeign.parseTribalWarsScreens(
                    generateParcel(
                            screensStringMap.get(SCREENS_TO_EXTRACT_AUTOMATED_INFO.WALL),
                            screensStringMap.get(SCREENS_TO_EXTRACT_AUTOMATED_INFO.TRAIN),
                            screensStringMap.get(SCREENS_TO_EXTRACT_AUTOMATED_INFO.SNOB)));
            String farmStrength = renderServiceFeign
                    .parseTribalWarsFarmScreens(generateSingleParcel(Constants.SCREEN_TYPE.FARM, screensStringMap.get(SCREENS_TO_EXTRACT_AUTOMATED_INFO.FARM)))
                    .getFarmStrength();

            VillaProto.Villa.Builder filledVilla = VillaProto.Villa.newBuilder()
                    .mergeFrom(villa)
                    .setTroops(troops)
                    .setFarmStrength(farmStrength);
            resultantVillaListBuilder.addVillas(filledVilla);
        });
        LOGGER.info("Resultant Villas Info prepared!!");
        LOGGER.info("{}", resultantVillaListBuilder.build());
        engine.logoutSequence();
        engine.destroyDriver();

        LOGGER.info("Computing type now!");
        final long timestamp = System.currentTimeMillis();
        resultantVillaListBuilder.getVillasBuilderList().forEach(villa -> {
            //decide and freeze type
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
            villa.setTimestamp(timestamp); //setting time of edit
        });
        VillaProto.VillaList finalVillaList = resultantVillaListBuilder.build();
        LOGGER.info("Prepared final villa list proto:-\n{}", finalVillaList);
        this.freshVillaList = finalVillaList;

        String renderedInfo = EMPTY_STR;
        try {
            LOGGER.info("Requesting render of all villas");
            renderedInfo = renderServiceFeign.renderTribalWarsVillas(finalVillaList);
            LOGGER.info("Rendering complete for all villas:\n{}", renderedInfo);
        } catch (Exception e) {
            LOGGER.error("FAILED to render final villa list! ", e);
        }
        if (writeBackToMongo) {
            try {
                LOGGER.info("Sending data to save to mongo - all villas read");
                mongoServiceFeign.addVillas(finalVillaList);
                LOGGER.info("Write successful!");
            } catch (Exception e) {
                LOGGER.error("Failed to writeback to mongo. ", e);
            }
        } else LOGGER.info("Skipping mongo writeback!");
        return renderedInfo; //returning renderedInfo to swagger as mongo just sends an OK state or not.
    }

    @GetMapping("/triggerAutomation/supportReportsAnalysis")
    public String triggerAutomationForSupportReportsAnalysis(@RequestParam(defaultValue = "p") String worldType,
                                                             @RequestParam(defaultValue = "9") int worldNumber,
                                                             @RequestParam(defaultValue = "report") String screen,
                                                             @RequestParam(defaultValue = "support") String mode,
                                                             @RequestParam(defaultValue = "Jan") String endReportMonth,
                                                             @RequestParam(defaultValue = "18") int endReportDay,
                                                             @RequestParam(defaultValue = "2021") int endReportYear) {
        //TODO -- for now, this will operate on all the reports under support. Later on, come up with idea to control the reports to be read.
        LOGGER.info("Will start automated analysis of support reports for en{}{}", worldType, worldNumber);
        if (!pinger.allEndPointsActive(renderServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return "END-POINTS NOT READY!";
        }
        LOGGER.info("All required endpoints active. Initiating run!");

        final Engine engine = new Engine(tribalWarsConfiguration.driver(), tribalWarsConfiguration.sso(), worldType, worldNumber);
        String overviewHtml = engine.extractOverviewDetailsForWorld(); //keeps session open for further op!
        LOGGER.info("Extracted Overview html from world. Length: {}", overviewHtml.length());

        String url = String.format(TW_REPORT_SCREEN_MODE, worldType, worldNumber, screen, mode);
        engine.getDriver().loadUrl(url); //loads 1st report page

        String twBasePrefix = String.format(TW_URL_WORLD_BASE, worldType, worldNumber);

        HtmlDataParcelProto.Parcel reportParcel = generateSingleParcel(Constants.SCREEN_TYPE.REPORT, engine.getDriver().getDriver().getPageSource());
        List<String> allReportLinks = renderServiceFeign.parseTribalWarsSupportReportLinks(reportParcel).stream()
                .map(reportLink -> twBasePrefix + reportLink).collect(Collectors.toList());

        List<String> reportPagesLinks = renderServiceFeign.parseTribalWarsSupportReportsPagesLinks(reportParcel);
        LOGGER.info("Report pages links: {}", reportPagesLinks);
        reportPagesLinks.forEach(pageLink -> {
            engine.getDriver().loadUrl(twBasePrefix + pageLink);
            allReportLinks.addAll(renderServiceFeign.parseTribalWarsSupportReportLinks(generateSingleParcel(Constants.SCREEN_TYPE.REPORT, engine.getDriver().getDriver().getPageSource()))
                    .stream().map(reportLink -> twBasePrefix + reportLink).collect(Collectors.toList()));
        });
        LOGGER.info("All support report links to analyze: {}", allReportLinks);

        List<SupportReportProto.SupportReport> supportReportList = new LinkedList<>();
        allReportLinks.forEach(reportLink -> {
            engine.getDriver().loadUrl(reportLink);
            supportReportList.add(renderServiceFeign.parseTribalWarsSupportReport(
                    generateSingleParcel(Constants.SCREEN_TYPE.REPORT, engine.getDriver().getDriver().getPageSource())));
        });

        engine.logoutSequence();
        engine.destroyDriver();

        Map<String, SupportReportProto.Troops.Builder> playerXSupportTroopsAcquired = computePlayerXSupportTroops(SupportReportProto.SupportReportType.ACQUIRED, supportReportList);
        LOGGER.info("Entire acquired player x troops mapping => ");
        playerXSupportTroopsAcquired.forEach((player, troops) -> LOGGER.info("{} x \n{}", player, troops));

        Map<String, SupportReportProto.Troops.Builder> playerXSupportTroopsReturned = computePlayerXSupportTroops(SupportReportProto.SupportReportType.SENT_BACK, supportReportList);
        LOGGER.info("Entire sent back player x troops mapping => ");
        playerXSupportTroopsReturned.forEach((player, troops) -> LOGGER.info("{} x \n{}", player, troops));

        LOGGER.info("Computing the lost support troops now.");
        Map<String, SupportReportProto.Troops> playerXSupportTroopsLost = computeLostSupportTroops(playerXSupportTroopsAcquired, playerXSupportTroopsReturned);
        LOGGER.info("Entire player x lost support troops mapping => ");
        playerXSupportTroopsLost.forEach((player, troops) -> LOGGER.info("{} x \n{}", player, troops));

        return playerXSupportTroopsLost.toString();
    }

    private Map<String, SupportReportProto.Troops.Builder> computePlayerXSupportTroops(SupportReportProto.SupportReportType supportReportType, List<SupportReportProto.SupportReport> supportReportList) {
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

    private Map<String, SupportReportProto.Troops> computeLostSupportTroops(Map<String, SupportReportProto.Troops.Builder> acquired, Map<String, SupportReportProto.Troops.Builder> returned) {
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

    /*@GetMapping("/triggerAutomation/scavenge")
    public String triggerAnalysisScavenging(@RequestParam(defaultValue = "p") String worldType,
                                            @RequestParam(defaultValue = "9") int worldNumber,
                                            @RequestParam(defaultValue = "false") boolean writeBackToMongo) {
        LOGGER.info("Will start automated extraction of scavenging count for en{}{}", worldType, worldNumber);

        return "";
    }*/

    private HtmlDataParcelProto.Parcel generateParcel(String wallHtml, String trainHtml, String snobHtml) {
        return HtmlDataParcelProto.Parcel.newBuilder()
                .setWallPageSource(wallHtml)
                .setSnobPageSource(snobHtml)
                .setTrainPageSource(trainHtml)
                .build();
    }

    private HtmlDataParcelProto.Parcel generateSingleParcel(Constants.SCREEN_TYPE screenType, String html) {
        HtmlDataParcelProto.Parcel.Builder builder = HtmlDataParcelProto.Parcel.newBuilder();
        switch (screenType) {
            case FARM:
                builder.setFarmPageSource(html);
                break;
            case REPORT:
                builder.setSupportReportSource(html);
                break;
        }
        return builder.build();
    }

}
