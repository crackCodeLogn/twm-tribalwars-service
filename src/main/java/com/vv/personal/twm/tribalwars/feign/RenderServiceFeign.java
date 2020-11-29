package com.vv.personal.twm.tribalwars.feign;

import com.vv.personal.twm.artifactory.generated.tw.VillaProto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Vivek
 * @since 30/11/20
 */
@FeignClient("twm-rendering-service")
public interface RenderServiceFeign {

    @PostMapping("/render/tw/parse/overview")
    VillaProto.VillaList parseTribalWarsOverviewHtml(@RequestBody String htmlData);
}
