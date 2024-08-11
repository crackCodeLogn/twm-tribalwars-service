package com.vv.personal.twm.tribalwars.feign;

import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import com.vv.personal.twm.ping.remote.feign.PingFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Vivek
 * @since 15/01/22
 */
@FeignClient("twm-tribalwars-crdb-service")
public interface CrdbServiceFeign extends PingFeign {

    @PostMapping("/crdb/tw/add-villas")
    String addVillas(@RequestBody VillaProto.VillaList villaList);

    @GetMapping("/crdb/tw/read/all?world={world}")
    VillaProto.VillaList readAllVillasFromCrdb(@PathVariable("world") String world);
}