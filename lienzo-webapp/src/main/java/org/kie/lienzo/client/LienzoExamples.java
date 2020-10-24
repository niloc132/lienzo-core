package org.kie.lienzo.client;

import com.ait.lienzo.client.core.config.LienzoCoreEntryPoint;

public class LienzoExamples extends BaseLienzoExamples {

    public void onModuleLoad() {
        new LienzoCoreEntryPoint().onModuleLoad();
        doLoad();
    }

}