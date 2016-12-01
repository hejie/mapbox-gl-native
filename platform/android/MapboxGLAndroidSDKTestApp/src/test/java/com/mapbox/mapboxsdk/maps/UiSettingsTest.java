package com.mapbox.mapboxsdk.maps;

import android.view.Gravity;

import com.mapbox.mapboxsdk.utils.TestConstants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UiSettingsTest {

    @InjectMocks
    MapView mMapView = mock(MapView.class);

    private UiSettings uiSettings;

    @Before
    public void beforeTest() {
        uiSettings = new UiSettings(mMapView, TestConstants.PIXEL_RATIO_TEST);
    }

    @Test
    public void testSanity() {
        assertNotNull("uiSettings should not be null", uiSettings);
    }

    @Test
    public void testCompassEnabled() {
        uiSettings.setCompassEnabled(true);
        assertEquals("Compass should be enabled", true, uiSettings.isCompassEnabled());
    }

    @Test
    public void testCompassDisabled() {
        uiSettings.setCompassEnabled(false);
        assertEquals("Compass should be disabled", false, uiSettings.isCompassEnabled());
    }

    @Test
    public void testCompassGravity() {
        uiSettings.setCompassGravity(Gravity.START);
        assertEquals("Compass gravity should be same", Gravity.START, uiSettings.getCompassGravity());
    }

    @Test
    public void testCompassMargins() {
        uiSettings.setCompassMargins(1, 2, 3, 4);
        assertTrue("Compass margin left should be same", uiSettings.getCompassMarginLeft() == 1);
        assertTrue("Compass margin top should be same", uiSettings.getCompassMarginTop() == 2);
        assertTrue("Compass margin right should be same", uiSettings.getCompassMarginRight() == 3);
        assertTrue("Compass margin bottom should be same", uiSettings.getCompassMarginBottom() == 4);
    }

    @Test
    public void testCompassFadeWhenFacingNorth(){
        assertTrue("Compass should fade when facing north by default.", uiSettings.isCompassFadeWhenFacingNorth());
        uiSettings.setCompassFadeFacingNorth(false);
        assertFalse("Compass fading should be disabled", uiSettings.isCompassFadeWhenFacingNorth());
    }

    @Test
    public void testLogoEnabled() {
        uiSettings.setLogoEnabled(true);
        assertEquals("Logo should be enabled", true, uiSettings.isLogoEnabled());
    }

    @Test
    public void testLogoDisabled() {
        uiSettings.setLogoEnabled(false);
        assertEquals("Logo should be disabled", false, uiSettings.isLogoEnabled());
    }

    @Test
    public void testLogoGravity() {
        uiSettings.setLogoGravity(Gravity.END);
        assertEquals("Logo gravity should be same", Gravity.END, uiSettings.getLogoGravity());
    }

    @Test
    public void testLogoMargins() {
        uiSettings.setLogoMargins(1, 2, 3, 4);
        assertTrue("Compass margin left should be same", uiSettings.getLogoMarginLeft() == 1);
        assertTrue("Compass margin top should be same", uiSettings.getLogoMarginTop() == 2);
        assertTrue("Compass margin right should be same", uiSettings.getLogoMarginRight() == 3);
        assertTrue("Compass margin bottom should be same", uiSettings.getLogoMarginBottom() == 4);
    }

    @Test
    public void testAttributionEnabled() {
        uiSettings.setAttributionEnabled(true);
        assertEquals("Attribution should be enabled", true, uiSettings.isAttributionEnabled());
    }

    @Test
    public void testAttributionDisabled() {
        uiSettings.setAttributionEnabled(false);
        assertEquals("Attribution should be disabled", false, uiSettings.isLogoEnabled());
    }

    @Test
    public void testAttributionGravity() {
        uiSettings.setAttributionGravity(Gravity.END);
        assertEquals("Attribution gravity should be same", Gravity.END, uiSettings.getAttributionGravity());
    }

    @Test
    public void testAttributionMargins() {
        uiSettings.setAttributionMargins(1, 2, 3, 4);
        assertTrue("Attribution margin left should be same", uiSettings.getAttributionMarginLeft() == 1);
        assertTrue("Attribution margin top should be same", uiSettings.getAttributionMarginTop() == 2);
        assertTrue("Attribution margin right should be same", uiSettings.getAttributionMarginRight() == 3);
        assertTrue("Attribution margin bottom should be same", uiSettings.getAttributionMarginBottom() == 4);
    }

    @Test
    public void testRotateGesturesEnabled() {
        uiSettings.setRotateGesturesEnabled(true);
        assertEquals("Rotate gesture should be enabled", true, uiSettings.isRotateGesturesEnabled());
    }

    @Test
    public void testRotateGesturesDisabled() {
        uiSettings.setRotateGesturesEnabled(false);
        assertEquals("Rotate gesture should be disabled", false, uiSettings.isRotateGesturesEnabled());
    }

    @Test
    public void testRotateGestureChange() {
        assertEquals("Default state should be true", true, uiSettings.isRotateGestureChangeAllowed());
        uiSettings.setRotateGestureChangeAllowed(false);
        assertEquals("State should have been changed", false, uiSettings.isRotateGestureChangeAllowed());
    }

    @Test
    public void testRotateGestureChangeAllowed() {
        uiSettings.setRotateGesturesEnabled(false);
        assertEquals("Rotate gesture should be false", false, uiSettings.isRotateGesturesEnabled());
        uiSettings.setRotateGesturesEnabled(true);
        assertEquals("Rotate gesture should be true", true, uiSettings.isRotateGesturesEnabled());
    }

    @Test
    public void testRotateGestureChangeDisallowed() {
        assertEquals("Rotate gesture should be true", true, uiSettings.isRotateGesturesEnabled());
        uiSettings.setRotateGestureChangeAllowed(false);
        uiSettings.setRotateGesturesEnabled(false);
        assertEquals("Rotate gesture change should be ignored", true, uiSettings.isRotateGesturesEnabled());
    }

    @Test
    public void testTiltGesturesEnabled() {
        uiSettings.setTiltGesturesEnabled(true);
        assertEquals("Tilt gesture should be enabled", true, uiSettings.isTiltGesturesEnabled());
    }

    @Test
    public void testTiltGesturesDisabled() {
        uiSettings.setTiltGesturesEnabled(false);
        assertEquals("Tilt gesture should be disabled", false, uiSettings.isTiltGesturesEnabled());
    }

    @Test
    public void testTiltGestureChange() {
        assertEquals("Default state should be true", true, uiSettings.isTiltGestureChangeAllowed());
        uiSettings.setTiltGestureChangeAllowed(false);
        assertEquals("State should have been changed", false, uiSettings.isTiltGestureChangeAllowed());
    }

    @Test
    public void testTiltGestureChangeAllowed() {
        uiSettings.setTiltGesturesEnabled(false);
        assertEquals("Tilt gesture should be false", false, uiSettings.isTiltGesturesEnabled());
        uiSettings.setTiltGesturesEnabled(true);
        assertEquals("Tilt gesture should be true", true, uiSettings.isTiltGesturesEnabled());
    }

    @Test
    public void testTiltGestureChangeDisallowed() {
        assertEquals("Tilt gesture should be true", true, uiSettings.isTiltGesturesEnabled());
        uiSettings.setTiltGestureChangeAllowed(false);
        uiSettings.setTiltGesturesEnabled(false);
        assertEquals("Tilt gesture change should be ignored", true, uiSettings.isTiltGesturesEnabled());
    }

    @Test
    public void testZoomGesturesEnabled() {
        uiSettings.setZoomGesturesEnabled(true);
        assertEquals("Zoom gesture should be enabled", true, uiSettings.isZoomGesturesEnabled());
    }

    @Test
    public void testZoomGesturesDisabled() {
        uiSettings.setZoomGesturesEnabled(false);
        assertEquals("Zoom gesture should be disabled", false, uiSettings.isZoomGesturesEnabled());
    }

    @Test
    public void testZoomGestureChange() {
        assertEquals("Default state should be true", true, uiSettings.isZoomGestureChangeAllowed());
        uiSettings.setZoomGestureChangeAllowed(false);
        assertEquals("State should have been changed", false, uiSettings.isZoomGestureChangeAllowed());
    }

    @Test
    public void testZoomGestureChangeAllowed() {
        uiSettings.setZoomGesturesEnabled(false);
        assertEquals("Zoom gesture should be false", false, uiSettings.isZoomGesturesEnabled());
        uiSettings.setZoomGesturesEnabled(true);
        assertEquals("Zoom gesture should be true", true, uiSettings.isZoomGesturesEnabled());
    }

    @Test
    public void testZoomGestureChangeDisallowed() {
        assertEquals("Zoom gesture should be true", true, uiSettings.isZoomGesturesEnabled());
        uiSettings.setZoomGestureChangeAllowed(false);
        uiSettings.setZoomGesturesEnabled(false);
        assertEquals("Zooom gesture change should be ignored", true, uiSettings.isZoomGesturesEnabled());
    }

    @Test
    public void testZoomControlsEnabled() {
        uiSettings.setZoomControlsEnabled(true);
        assertEquals("Zoom controls should be enabled", true, uiSettings.isZoomControlsEnabled());
    }

    @Test
    public void testZoomControlsDisabled() {
        uiSettings.setZoomControlsEnabled(false);
        assertEquals("Zoom controls should be disabled", false, uiSettings.isZoomControlsEnabled());
    }

    @Test
    public void testScrollGesturesEnabled() {
        uiSettings.setScrollGesturesEnabled(true);
        assertEquals("Scroll gesture should be enabled", true, uiSettings.isScrollGesturesEnabled());
    }

    @Test
    public void testScrollGesturesDisabled() {
        uiSettings.setScrollGesturesEnabled(false);
        assertEquals("Scroll gesture should be disabled", false, uiSettings.isScrollGesturesEnabled());
    }

    @Test
    public void testScrollGestureChange() {
        assertEquals("Default state should be true", true, uiSettings.isScrollGestureChangeAllowed());
        uiSettings.setScrollGestureChangeAllowed(false);
        assertEquals("State should have been changed", false, uiSettings.isScrollGestureChangeAllowed());
    }

    @Test
    public void testScrollGestureChangeAllowed() {
        uiSettings.setScrollGesturesEnabled(false);
        assertEquals("Scroll gesture should be false", false, uiSettings.isScrollGesturesEnabled());
        uiSettings.setScrollGesturesEnabled(true);
        assertEquals("Scroll gesture should be true", true, uiSettings.isScrollGesturesEnabled());
    }

    @Test
    public void testScrollGestureChangeDisallowed() {
        assertEquals("Scroll gesture should be true", true, uiSettings.isScrollGesturesEnabled());
        uiSettings.setScrollGestureChangeAllowed(false);
        uiSettings.setScrollGesturesEnabled(false);
        assertEquals("Scroll gesture change should be ignored", true, uiSettings.isScrollGesturesEnabled());
    }

    @Test
    public void testAllGesturesEnabled() {
        uiSettings.setAllGesturesEnabled(true);
        assertEquals("Rotate gesture should be enabled", true, uiSettings.isRotateGesturesEnabled());
        assertEquals("Tilt gesture should be enabled", true, uiSettings.isTiltGesturesEnabled());
        assertEquals("Zoom gesture should be enabled", true, uiSettings.isZoomGesturesEnabled());
        assertEquals("Scroll gesture should be enabled", true, uiSettings.isScrollGesturesEnabled());
    }

    @Test
    public void testAllGesturesDisabled() {
        uiSettings.setAllGesturesEnabled(false);
        assertEquals("Rotate gesture should be enabled", false, uiSettings.isRotateGesturesEnabled());
        assertEquals("Tilt gesture should be disabled", false, uiSettings.isTiltGesturesEnabled());
        assertEquals("Zoom gesture should be disabled", false, uiSettings.isZoomGesturesEnabled());
        assertEquals("Scroll gesture should be disabled", false, uiSettings.isScrollGesturesEnabled());
    }

    @Test
    public void testInvalidate() {
        uiSettings.invalidate();
    }

    @Test
    public void testHeight() {
        when(mMapView.getMeasuredHeight()).thenReturn(1);
        assertEquals("height should be same as mocked instance", 1, uiSettings.getHeight(), 0);
    }

    @Test
    public void testWidth() {
        when(mMapView.getMeasuredWidth()).thenReturn(1);
        assertEquals("width should be same as mocked instance", 1, uiSettings.getWidth(), 0);
    }
}