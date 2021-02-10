package com.vv.personal.twm.tribalwars.feign;

import com.vv.personal.twm.artifactory.generated.tw.HtmlDataParcelProto;
import com.vv.personal.twm.artifactory.generated.tw.SupportReportProto;
import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import com.vv.personal.twm.ping.feign.HealthFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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

    @PostMapping("/render/tw/parse/page/supportReportPagesLinks")
    List<String> parseTribalWarsSupportReportsPagesLinks(@RequestBody HtmlDataParcelProto.Parcel parcel);

    @PostMapping("/render/tw/parse/page/supportReports")
    List<String> parseTribalWarsSupportReportLinks(@RequestBody HtmlDataParcelProto.Parcel parcel);

    @PostMapping("/render/tw/parse/report/support")
    SupportReportProto.SupportReport parseTribalWarsSupportReport(@RequestBody HtmlDataParcelProto.Parcel parcel);

    @PostMapping("/render/tw/parse/academy/coinMintingCapacity")
    VillaProto.Villa parseTribalWarsCoinMintingCapacity(@RequestBody HtmlDataParcelProto.Parcel parcel);
}
