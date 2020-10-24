package com.ait.lienzo.client.widget.panel.impl;

import com.ait.lienzo.client.widget.panel.IsResizable;
import com.ait.lienzo.client.widget.panel.LienzoPanel;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;

import static elemental2.dom.DomGlobal.window;

/**
 * Automatically fits its size to the parent's one.
 */
public class LienzoResizablePanel
        extends LienzoPanelDelegate<LienzoResizablePanel>
        implements IsResizable {

    private final LienzoFixedPanel panel;
    private final EventListener m_resizeListener;

    public static LienzoResizablePanel newPanel() {
        LienzoFixedPanel panel = LienzoFixedPanel.newPanel();
        return new LienzoResizablePanel(panel);
    }

    public LienzoResizablePanel(LienzoFixedPanel panel) {
        this.panel = panel;
        this.m_resizeListener = e -> fitToParentSize();
        // TODO: lienzo-to-native - Adding the event listener for the whole window - may cause issues when multiple live instances running
        window.addEventListener("resize", m_resizeListener);
    }

    @Override
    public void onResize() {
        fitToParentSize();
    }

    @Override
    protected LienzoPanel getPanel() {
        return panel;
    }

    @Override
    public void destroy() {
        window.removeEventListener("resize", m_resizeListener);
        super.destroy();
    }

    private void fitToParentSize() {
        if (null != panel.getElement().parentNode &&
                null != panel.getElement().parentNode.parentNode) {
            HTMLDivElement parent = (HTMLDivElement) panel.getElement().parentNode.parentNode;
            int offsetWidth = parent.offsetWidth;
            int offsetHeight = parent.offsetHeight;
            if (offsetWidth > 0 && offsetHeight > 0) {
                panel.setPixelSize(offsetWidth, offsetHeight);
            }
        }
    }

}
