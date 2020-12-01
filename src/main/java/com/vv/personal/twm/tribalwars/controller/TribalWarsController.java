package com.vv.personal.twm.tribalwars.controller;

import com.vv.personal.twm.artifactory.generated.tw.HtmlDataParcelProto;
import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import com.vv.personal.twm.tribalwars.automation.config.TribalWarsConfiguration;
import com.vv.personal.twm.tribalwars.automation.constants.Constants;
import com.vv.personal.twm.tribalwars.automation.engine.Engine;
import com.vv.personal.twm.tribalwars.feign.MongoServiceFeign;
import com.vv.personal.twm.tribalwars.feign.RenderServiceFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.vv.personal.twm.tribalwars.automation.constants.Constants.TW_SCREEN;

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
                                             @RequestParam(defaultValue = "9") int worldNumber) {
        LOGGER.info("Will start automated extraction of troops count for en{}{}", worldType, worldNumber);
        final Engine engine = new Engine(tribalWarsConfiguration.driver(), tribalWarsConfiguration.sso(), worldType, worldNumber);
        String overviewHtml = engine.extractOverviewDetailsForWorld(); //keeps session open for further op!
        LOGGER.info("Extracted Overview html from world. Length: {}", overviewHtml.length());
        VillaProto.VillaList villaListBuilder = renderServiceFeign.parseTribalWarsOverviewHtml(overviewHtml);
        LOGGER.info("{}", villaListBuilder);

        final VillaProto.VillaList.Builder resultantVillaListBuilder = VillaProto.VillaList.newBuilder();
        villaListBuilder.getVillasList().forEach(villa -> {
            Map<Constants.SCREENS, String> screensStringMap = new HashMap<>();
            Arrays.stream(Constants.SCREENS.values()).forEach(screen -> {
                String urlToHit = String.format(TW_SCREEN, worldType, worldNumber, villa.getId(), screen.name().toLowerCase());
                engine.getDriver().loadUrl(urlToHit);
                String screenHtml = engine.getDriver().getDriver().getPageSource();
                screensStringMap.put(screen, screenHtml);
            });

            VillaProto.Troops troops = renderServiceFeign.parseTribalWarsScreens(
                    generateParcel(
                            screensStringMap.get(Constants.SCREENS.WALL),
                            screensStringMap.get(Constants.SCREENS.TRAIN),
                            screensStringMap.get(Constants.SCREENS.SNOB)));

            VillaProto.Villa.Builder filledVilla = VillaProto.Villa.newBuilder()
                    .mergeFrom(villa)
                    .setTroops(troops);
            resultantVillaListBuilder.addVillas(filledVilla);
        });
        LOGGER.info("Resultant Villas Info prepared!!");
        LOGGER.info("{}", resultantVillaListBuilder.build());
        engine.logoutSequence();
        engine.destroyDriver();

        LOGGER.info("Computing type now!");
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
            villa.setTimestamp(System.currentTimeMillis()); //setting time of edit
        });
        VillaProto.VillaList finalVillaList = resultantVillaListBuilder.build();
        LOGGER.info("Prepared final villa list proto:-\n{}", finalVillaList);

        try {
            LOGGER.info("Requesting render of all villas");
            String renderedInfo = renderServiceFeign.renderTribalWarsVillas(finalVillaList);
            LOGGER.info("Rendering complete for all villas:\n{}", renderedInfo);
            return renderedInfo;
        } catch (Exception e) {
            LOGGER.error("FAILED to render final villa list! ", e);
        }
        return "FAILED!!";
    }

    private HtmlDataParcelProto.Parcel generateParcel(String wallHtml, String trainHtml, String snobHtml) {
        return HtmlDataParcelProto.Parcel.newBuilder()
                .setWallPageSource(wallHtml)
                .setSnobPageSource(snobHtml)
                .setTrainPageSource(trainHtml)
                .build();
    }
}
