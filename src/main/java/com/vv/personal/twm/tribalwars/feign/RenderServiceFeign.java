package com.vv.personal.twm.tribalwars.feign;

import com.vv.personal.twm.artifactory.generated.tw.HtmlDataParcelProto;
import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Vivek
 * @since 30/11/20
 */
@FeignClient("twm-rendering-service")
public interface RenderServiceFeign extends HealthFeign {

    @PostMapping("/render/tw/parse/overview")
    VillaProto.VillaList parseTribalWarsOverviewHtml(@RequestBody String Data);

    //@GetMapping("/render/tw/parse/screens?wall={wall}&train={train}&snob={snob}")
    @PostMapping("/render/tw/parse/screens")
    VillaProto.Troops parseTribalWarsScreens(@RequestBody HtmlDataParcelProto.Parcel parcel);

    @PostMapping("/render/tw/parse/screens/farm")
    VillaProto.Villa parseTribalWarsFarmScreens(@RequestBody HtmlDataParcelProto.Parcel parcel);

    @PostMapping("/render/tw/render/villas")
    String renderTribalWarsVillas(@RequestBody VillaProto.VillaList villas);
}
