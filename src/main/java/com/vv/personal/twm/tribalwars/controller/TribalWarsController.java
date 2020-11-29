package com.vv.personal.twm.tribalwars.controller;

import com.vv.personal.twm.artifactory.generated.deposit.FixedDepositProto;
import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import com.vv.personal.twm.tribalwars.feign.MongoServiceFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public VillaProto.VillaList readAllVillasForWorld(@RequestParam(defaultValue = "enp9") String world) {
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
}
