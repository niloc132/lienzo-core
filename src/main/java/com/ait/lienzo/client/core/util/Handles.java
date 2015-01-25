package com.ait.lienzo.client.core.util;

import com.ait.lienzo.client.core.shape.HandleMapper;
import com.ait.lienzo.client.core.shape.Shape;

public class Handles {
    private static final Handles instance = new Handles();

    public static Handles instance() {
        return instance;
    }

    public WiresShape createHandlesContext(Shape targetShape, Shape protoHandleShape, HandleMapper cpMapper) {
        WiresShape handlesCtx = new WiresShape(targetShape, protoHandleShape, cpMapper);
        return handlesCtx;
    }


}
