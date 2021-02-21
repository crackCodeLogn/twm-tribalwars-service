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
import com.vv.personal.twm.tribalwars.util.TwUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.vv.personal.twm.tribalwars.automation.constants.Constants.*;
import static com.vv.personal.twm.tribalwars.constants.Constants.*;
import static com.vv.personal.twm.tribalwars.util.TwUtil.*;

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

    @ApiOperation(value = "read all villas for world from mongo", hidden = true)
    @GetMapping("/read/all")
    public VillaProto.VillaList readAllVillasForWorld(@RequestParam(defaultValue = "p") String worldType,
                                                      @RequestParam(defaultValue = "9") int worldNumber) {
        if (!pinger.allEndPointsActive(mongoServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return VillaProto.VillaList.newBuilder().build();
        }
        LOGGER.info("All required endpoints active. Initiating run!");

        //Strange, somehow this tab doesn't open in swagger. Rest all do though.
        String world = "en" + worldType + worldNumber;
        LOGGER.info("Obtained request to read all villas for world {}", world);
        try {
            VillaProto.VillaList.Builder villaList = VillaProto.VillaList.newBuilder().addAllVillas(mongoServiceFeign.readAllVillasFromMongo(world).getVillasList());
            villaList.getVillasBuilderList().forEach(TwUtil::computeVillaType);
            LOGGER.info("Obtained {} villas for world {} from mongo", villaList.getVillasCount(), world);
            return villaList.build();
        } catch (Exception e) {
            LOGGER.error("Failed to get villas from mongo for world {}", world, e);
        }
        return VillaProto.VillaList.newBuilder().build();
    }

    @GetMapping("/read/all/manual")
    public String swaggerReadAllVillasForWorld(@RequestParam(defaultValue = "p") String worldType,
                                               @RequestParam(defaultValue = "9") int worldNumber) {
        if (!pinger.allEndPointsActive(renderServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return "END-POINTS NOT READY!";
        }
        LOGGER.info("All required endpoints active. Initiating run!");
        return renderServiceFeign.renderTribalWarsVillas(readAllVillasForWorld(worldType, worldNumber));
    }

    @ApiOperation(value = "get all latest villas for world", hidden = true)
    @GetMapping("/read/all/latest")
    public VillaProto.VillaList readAllLatestVillasForWorld(@RequestParam(defaultValue = "p") String worldType,
                                                            @RequestParam(defaultValue = "9") int worldNumber) {
        VillaProto.VillaList villaList = readAllVillasForWorld(worldType, worldNumber);
        long latestTimestamp = villaList.getVillasList().stream().mapToLong(VillaProto.Villa::getTimestamp).max().orElse(NA_LONG);
        LOGGER.info("Computed latest timestamp as: {}", latestTimestamp);
        VillaProto.VillaList.Builder builder = VillaProto.VillaList.newBuilder()
                .addAllVillas(villaList.getVillasList().stream()
                        .filter(villa -> villa.getTimestamp() == latestTimestamp)
                        .collect(Collectors.toList()));
        LOGGER.info("Latest timestamped villas: {}", builder.getVillasCount());
        return builder.build();
    }

    @GetMapping("/read/all/latest/manual")
    public String swaggerReadAllLatestVillasForWorld(@RequestParam(defaultValue = "p") String worldType,
                                                     @RequestParam(defaultValue = "9") int worldNumber) {
        if (!pinger.allEndPointsActive(renderServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return "END-POINTS NOT READY!";
        }
        LOGGER.info("All required endpoints active. Initiating run!");
        VillaProto.VillaList villaList = readAllLatestVillasForWorld(worldType, worldNumber);
        return renderServiceFeign.renderTribalWarsVillas(villaList);
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
            computeVillaType(villa); //decide and freeze type
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

    @GetMapping("/triggerAutomation/mintCoinsForNoblemen")
    public String triggerAutomationForMintingCoinsForNoblemen(@RequestParam(defaultValue = "p") String worldType,
                                                              @RequestParam(defaultValue = "9") int worldNumber,
                                                              @RequestParam(defaultValue = "70") Double resourcesThresholdPercentage,
                                                              @RequestParam(defaultValue = "60") Double mintingPercentage,
                                                              @RequestParam(defaultValue = "3") int minCoinsToMint) {
        //TODO -- for now, this will operate on all the villas. Later on, come up with idea to control the reports to be read.
        LOGGER.info("Will start automated coin minting for en{}{}", worldType, worldNumber);
        if (!pinger.allEndPointsActive(renderServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return "END-POINTS NOT READY!";
        }
        LOGGER.info("All required endpoints active. Initiating run!");

        final Engine engine = new Engine(tribalWarsConfiguration.driver(), tribalWarsConfiguration.sso(), worldType, worldNumber);
        String overviewHtml = engine.extractOverviewDetailsForWorld(); //keeps session open for further op!
        LOGGER.info("Extracted Overview html from world. Length: {}", overviewHtml.length());
        VillaProto.VillaList villaListBuilder = renderServiceFeign.parseTribalWarsOverviewHtml(overviewHtml);
        LOGGER.info("{}", villaListBuilder);

        //engine.getDriver().getDriver().manage().window().fullscreen();
        AtomicInteger coinsMinted = new AtomicInteger(ZERO_INT), plausibleCoinsMintCount = new AtomicInteger(ZERO_INT);
        villaListBuilder.getVillasList().forEach(villa -> {
            String urlToHit = String.format(TW_SCREEN, worldType, worldNumber, villa.getId(), SCREENS_TO_EXTRACT_AUTOMATED_INFO.SNOB.name().toLowerCase());
            engine.getDriver().loadUrl(urlToHit);
            String snobHtml = engine.getDriver().getDriver().getPageSource();

            VillaProto.Villa populatedVillaDetails = renderServiceFeign.parseTribalWarsCoinMintingCapacity(generateSingleParcel(Constants.SCREEN_TYPE.SNOB, snobHtml));
            double minResourcesThreshold = resourcesThresholdPercentage * populatedVillaDetails.getResources().getWarehouseCapacity() / 100.0;
            LOGGER.info("Villa '{}' has a max warehouse cap of '{}' and currently has {} wood, {} clay, {} iron and a farm strength of {}. For coin minting, min resources required: {}", villa.getName(), populatedVillaDetails.getResources().getWarehouseCapacity(),
                    populatedVillaDetails.getResources().getCurrentWood(), populatedVillaDetails.getResources().getCurrentClay(), populatedVillaDetails.getResources().getCurrentIron(), populatedVillaDetails.getFarmStrength(), minResourcesThreshold);

            if (populatedVillaDetails.getResources().getCurrentWood() >= minResourcesThreshold
                    && populatedVillaDetails.getResources().getCurrentClay() >= minResourcesThreshold
                    && populatedVillaDetails.getResources().getCurrentIron() >= minResourcesThreshold) {
                int coinsToMint = (int) Math.floor(populatedVillaDetails.getCoinMintingCapacity() * mintingPercentage / 100.0);
                if (coinsToMint >= minCoinsToMint) {
                    //mint coin here in UI
                    LOGGER.info("Will mint {} coins in {}", coinsToMint, villa.getName());
                    plausibleCoinsMintCount.addAndGet(coinsToMint);
                    try {
                        engine.executeJsScript("window.scrollBy(0, 1000)");

                        WebElement coinMintField = engine.getDriver().getDriver().findElement(By.id("coin_mint_count"));
                        coinMintField.clear();
                        coinMintField.sendKeys(String.valueOf(coinsToMint));
                        List<WebElement> buttons = engine.getDriver().getDriver().findElements(By.className("btn-default"));
                        buttons.get(buttons.size() - 1).click();
                        coinsMinted.addAndGet(coinsToMint);
                        LOGGER.info("Minting successful!");
                    } catch (Exception e) {
                        LOGGER.error("Failed to mint {} coins in {}. ", coinsToMint, villa.getName(), e);
                    }
                } else {
                    LOGGER.warn("Ignoring minting order for current villa '{}', as only {} can be minted, and min coins to be minted is {}", villa.getName(), coinsToMint, minCoinsToMint);
                }
            } else {
                LOGGER.warn("Ignoring minting order for current villa '{}', as current resources is less than min resource threshold '{}'", villa.getName(), minResourcesThreshold);
            }
        });
        engine.logoutSequence();
        engine.destroyDriver();

        String mintingReport = String.format("%d/%d coins minted!", coinsMinted.get(), plausibleCoinsMintCount.get());
        LOGGER.info(mintingReport);
        return mintingReport;
    }


    @GetMapping("/triggerComputation/computeLeastDistance")
    public String triggerComputationForLeastDistance(@RequestParam(defaultValue = "p") String worldType,
                                                     @RequestParam(defaultValue = "9") int worldNumber,
                                                     @RequestParam(defaultValue = "OFF") VillaProto.VillaType villaType,
                                                     @RequestParam(defaultValue = "xxx|yyy") String destinationCoordinate,
                                                     @RequestParam(defaultValue = "5") int depth) {
        if (!isCoordinateValid(destinationCoordinate)) {
            String err = "Unrecognized destination coordinates: " + destinationCoordinate;
            LOGGER.warn(err);
            return err;
        }
        String[] parts = StringUtils.split(destinationCoordinate, '|');
        int destX = Integer.parseInt(parts[0]), destY = Integer.parseInt(parts[1]);
        LOGGER.info("Will start computation of nearest villas of mine to {} for type: {}", destinationCoordinate, villaType);
        if (!pinger.allEndPointsActive(renderServiceFeign)) {
            LOGGER.error("All end-points not active. Will not trigger op! Check log");
            return "END-POINTS NOT READY!";
        }
        LOGGER.info("All required endpoints active. Initiating run!");
        VillaProto.VillaList.Builder villaList = VillaProto.VillaList.newBuilder()
                .addAllVillas(readAllLatestVillasForWorld(worldType, worldNumber).getVillasList()
                        .stream().filter(villa -> villa.getType() == villaType).collect(Collectors.toList()));
        LOGGER.info("Found {} villas of mine matching type: {}", villaList.getVillasCount(), villaType);
        villaList.getVillasBuilderList().forEach(villa -> {
            villa.putExtraDoubles(DISTANCE_WITH_COMPARING_VILLA_KEY, Double.parseDouble(String.format("%.2f", TwUtil.computeDistance(villa.getX(), villa.getY(), destX, destY))));
            villa.putExtraStrings(COMPARING_VILLA_COORD, destinationCoordinate);
        });

        villaList = VillaProto.VillaList.newBuilder().addAllVillas(
                villaList.getVillasList().stream()
                        .sorted(Comparator.comparingDouble(villa -> villa.getExtraDoublesMap().get(DISTANCE_WITH_COMPARING_VILLA_KEY)))
                        .limit(depth)
                        .collect(Collectors.toList()));
        LOGGER.info("Narrowing down to {} villas matching the least distance computation", villaList.getVillasCount());
        return renderServiceFeign.renderTribalWarsVillas(villaList.build());
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
            case SNOB:
                builder.setSnobPageSource(html);
                break;
        }
        return builder.build();
    }

}
