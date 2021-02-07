package com.vv.personal.twm.tribalwars.feign;

import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import com.vv.personal.twm.ping.feign.HealthFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Vivek
 * @since 29/11/20
 */
@FeignClient("twm-mongo-service")
public interface MongoServiceFeign extends HealthFeign {

    @PostMapping("/mongo/tw/addVilla")
    String addVilla(@RequestBody VillaProto.Villa newVilla);

    @PostMapping("/deleteVilla")
    String deleteVilla(@RequestBody String villaId);

    @GetMapping("/mongo/tw/read/all?world={world}")
    VillaProto.VillaList readAllVillasFromMongo(@PathVariable("world") String world);

    @PostMapping("/mongo/tw/addVillas")
    String addVillas(@RequestBody VillaProto.VillaList villaList);
}
