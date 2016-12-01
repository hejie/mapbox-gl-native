package com.mapbox.mapboxsdk.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import com.almeros.android.multitouch.gesturedetectors.RotateGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.ShoveGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.TwoFingerGestureDetector;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.telemetry.MapboxEvent;

/**
 * Manages gestures events on a MapView.
 * <p>
 * Relies on gesture detection code found in almeros.android.multitouch.gesturedetectors.
 * </p> *
 */
final class MapGestureDetector {

    private final TrackingSettings trackingSettings;
    private final AnnotationManager annotationManager;
    private final Transform transform;
    private final Projection projection;
    private final UiSettings uiSettings;
    private final float screenDensity;

    private final GestureDetectorCompat gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final RotateGestureDetector rotateGestureDetector;
    private final ShoveGestureDetector shoveGestureDetector;

    private MapboxMap.OnMapClickListener onMapClickListener;
    private MapboxMap.OnMapLongClickListener onMapLongClickListener;
    private MapboxMap.OnFlingListener onFlingListener;
    private MapboxMap.OnScrollListener onScrollListener;

    private PointF focalPoint;

    private boolean twoTap = false;
    private boolean zoomStarted = false;
    private boolean dragStarted = false;
    private boolean quickZoom = false;
    private boolean scrollInProgress = false;

    MapGestureDetector(Context context, Transform transform, Projection projection, UiSettings uiSettings,
                       TrackingSettings trackingSettings, AnnotationManager annotationManager) {
        this.annotationManager = annotationManager;
        this.transform = transform;
        this.projection = projection;
        this.uiSettings = uiSettings;
        this.screenDensity = uiSettings.getPixelRatio();
        this.trackingSettings = trackingSettings;

        // Touch gesture detectors
        gestureDetector = new GestureDetectorCompat(context, new GestureListener());
        gestureDetector.setIsLongpressEnabled(true);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
        ScaleGestureDetectorCompat.setQuickScaleEnabled(scaleGestureDetector, true);
        rotateGestureDetector = new RotateGestureDetector(context, new RotateGestureListener());
        shoveGestureDetector = new ShoveGestureDetector(context, new ShoveGestureListener());
    }

    void setFocalPoint(PointF focalPoint) {
        if (focalPoint == null) {
            // resetting focal point,
            // need to validate if we need to reset focal point with user provided one
            if (uiSettings.getFocalPoint() != null) {
                focalPoint = uiSettings.getFocalPoint();
            }
        }
        this.focalPoint = focalPoint;
    }

    // Called when user touches the screen, all positions are absolute
    boolean onTouchEvent(@NonNull MotionEvent event) {
        // Check and ignore non touch or left clicks
        if ((event.getButtonState() != 0) && (event.getButtonState() != MotionEvent.BUTTON_PRIMARY)) {
            return false;
        }

        // Check two finger gestures first
        rotateGestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        shoveGestureDetector.onTouchEvent(event);

        // Handle two finger tap
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // First pointer down
                transform.setGestureInProgress(true);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // Second pointer down
                twoTap = event.getPointerCount() == 2
                        && uiSettings.isZoomGesturesEnabled();
                if (twoTap) {
                    // Confirmed 2nd Finger Down
                    MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_TWO_FINGER_SINGLETAP, event.getX(), event.getY(), transform.getZoom());
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Second pointer up
                break;

            case MotionEvent.ACTION_UP:
                // First pointer up
                long tapInterval = event.getEventTime() - event.getDownTime();
                boolean isTap = tapInterval <= ViewConfiguration.getTapTimeout();
                boolean inProgress = rotateGestureDetector.isInProgress()
                        || scaleGestureDetector.isInProgress()
                        || shoveGestureDetector.isInProgress();

                if (twoTap && isTap && !inProgress) {
                    if (focalPoint != null) {
                        transform.zoom(false, focalPoint.x, focalPoint.y);
                    } else {
                        PointF focalPoint = TwoFingerGestureDetector.determineFocalPoint(event);
                        transform.zoom(false, focalPoint.x, focalPoint.y);
                    }
                    twoTap = false;
                    return true;
                }

                // Scroll / Pan Has Stopped
                if (scrollInProgress) {
                    MapboxEvent.trackGestureDragEndEvent(projection, event.getX(), event.getY(), transform.getZoom());
                    scrollInProgress = false;
                }

                twoTap = false;
                transform.setGestureInProgress(false);
                break;

            case MotionEvent.ACTION_CANCEL:
                twoTap = false;
                transform.setGestureInProgress(false);
                break;
        }

        return gestureDetector.onTouchEvent(event);
    }



    // This class handles one finger gestures
    private class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener {

        // Must always return true otherwise all events are ignored
        @Override
        @SuppressLint("ResourceType")
        public boolean onDown(MotionEvent event) {
            return true;
        }

        // Called for double taps
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (!uiSettings.isZoomGesturesEnabled()) {
                return false;
            }

            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if (quickZoom) {
                        // insert here?
                        quickZoom = false;
                        break;
                    }

                    // Single finger double tap
                    if (focalPoint != null) {
                        // User provided focal point
                        transform.zoom(true, focalPoint.x, focalPoint.y);
                    } else {
                        // Zoom in on gesture
                        transform.zoom(true, e.getX(), e.getY());
                    }
                    break;
            }

            MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_DOUBLETAP, e.getX(), e.getY(), transform.getZoom());

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            // Cancel any animation
            transform.cancelTransitions();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            PointF tapPoint = new PointF(motionEvent.getX(), motionEvent.getY());
            boolean tapHandled = annotationManager.onTap(tapPoint, uiSettings.getPixelRatio());

            if (!tapHandled) {
                if (uiSettings.isDeselectMarkersOnTap()) {
                    // deselect any selected marker
                    annotationManager.deselectMarkers();
                }

                // notify app of map click
                if (onMapClickListener != null) {
                    onMapClickListener.onMapClick(projection.fromScreenLocation(tapPoint));
                }
            }

            MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_SINGLETAP, motionEvent.getX(), motionEvent.getY(), transform.getZoom());
            return true;
        }

        // Called for a long press
        @Override
        public void onLongPress(MotionEvent motionEvent) {
            if (onMapLongClickListener != null && !quickZoom) {
                onMapLongClickListener.onMapLongClick(projection.fromScreenLocation(new PointF(motionEvent.getX(), motionEvent.getY())));
            }
        }

        // Called for flings
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!trackingSettings.isScrollGestureCurrentlyEnabled()) {
                return false;
            }

            trackingSettings.resetTrackingModesIfRequired(true, false);

            double decelerationRate = 1;

            // Cancel any animation
            transform.cancelTransitions();

            double offsetX = velocityX * decelerationRate / 4 / screenDensity;
            double offsetY = velocityY * decelerationRate / 4 / screenDensity;

            transform.setGestureInProgress(true);
            transform.moveBy(offsetX, offsetY, (long) (decelerationRate * 1000.0f));
            transform.setGestureInProgress(false);

            if (onFlingListener != null) {
                onFlingListener.onFling();
            }

            MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_PAN_START, e1.getX(), e1.getY(), transform.getZoom());
            return true;
        }

        // Called for drags
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!scrollInProgress) {
                scrollInProgress = true;
            }
            if (!trackingSettings.isScrollGestureCurrentlyEnabled()) {
                return false;
            }

            if (dragStarted) {
                return false;
            }

            // reset tracking if needed
            trackingSettings.resetTrackingModesIfRequired(true, false);
            // Cancel any animation
            transform.cancelTransitions();

            // Scroll the map
            transform.moveBy(-distanceX / screenDensity, -distanceY / screenDensity, 0 /*no duration*/);

            if (onScrollListener != null) {
                onScrollListener.onScroll();
            }
            return true;
        }
    }

    // This class handles two finger gestures and double-tap drag gestures
    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        long beginTime = 0;
        float scaleFactor = 1.0f;

        // Called when two fingers first touch the screen
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (!uiSettings.isZoomGesturesEnabled()) {
                return false;
            }

            beginTime = detector.getEventTime();
            MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_PINCH_START, detector.getFocusX(), detector.getFocusY(), transform.getZoom());
            return true;
        }

        // Called when fingers leave screen
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            beginTime = 0;
            scaleFactor = 1.0f;
            zoomStarted = false;
        }

        // Called each time a finger moves
        // Called for pinch zooms and quickzooms/quickscales
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!uiSettings.isZoomGesturesEnabled()) {
                return super.onScale(detector);
            }

            // If scale is large enough ignore a tap
            scaleFactor *= detector.getScaleFactor();
            if ((scaleFactor > 1.05f) || (scaleFactor < 0.95f)) {
                zoomStarted = true;
            }

            // Ignore short touches in case it is a tap
            // Also ignore small scales
            long time = detector.getEventTime();
            long interval = time - beginTime;
            if (!zoomStarted && (interval <= ViewConfiguration.getTapTimeout())) {
                return false;
            }

            if (!zoomStarted) {
                return false;
            }

            if (dragStarted) {
                return false;
            }

            // Cancel any animation
            transform.cancelTransitions();

            // Gesture is a quickzoom if there aren't two fingers
            quickZoom = !twoTap;

            // make an assumption here; if the zoom center is specified by the gesture, it's NOT going
            // to be in the center of the map. Therefore the zoom will translate the map center, so tracking
            // should be disabled.

            trackingSettings.resetTrackingModesIfRequired(!quickZoom, false);
            // Scale the map
            if (focalPoint != null) {
                // arround user provided focal point
                transform.zoomBy(detector.getScaleFactor(), focalPoint.x / screenDensity, focalPoint.y / screenDensity);
            } else if (quickZoom) {
                // around center map
                transform.zoomBy(detector.getScaleFactor(), (uiSettings.getWidth() / 2) / screenDensity, (uiSettings.getHeight() / 2) / screenDensity);
            } else {
                // around gesture
                transform.zoomBy(detector.getScaleFactor(), detector.getFocusX() / screenDensity, detector.getFocusY() / screenDensity);
            }

            return true;
        }
    }

    // This class handles two finger rotate gestures
    private class RotateGestureListener extends RotateGestureDetector.SimpleOnRotateGestureListener {

        long beginTime = 0;
        float totalAngle = 0.0f;
        boolean started = false;

        // Called when two fingers first touch the screen
        @Override
        public boolean onRotateBegin(RotateGestureDetector detector) {
            if (!trackingSettings.isRotateGestureCurrentlyEnabled()) {
                return false;
            }

            beginTime = detector.getEventTime();
            MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_ROTATION_START, detector.getFocusX(), detector.getFocusY(), transform.getZoom());
            return true;
        }

        // Called when the fingers leave the screen
        @Override
        public void onRotateEnd(RotateGestureDetector detector) {
            beginTime = 0;
            totalAngle = 0.0f;
            started = false;
        }

        // Called each time one of the two fingers moves
        // Called for rotation
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (!trackingSettings.isRotateGestureCurrentlyEnabled() || dragStarted) {
                return false;
            }

            // If rotate is large enough ignore a tap
            // Also is zoom already started, don't rotate
            totalAngle += detector.getRotationDegreesDelta();
            if (!zoomStarted && ((totalAngle > 20.0f) || (totalAngle < -20.0f))) {
                started = true;
            }

            // Ignore short touches in case it is a tap
            // Also ignore small rotate
            long time = detector.getEventTime();
            long interval = time - beginTime;
            if (!started && (interval <= ViewConfiguration.getTapTimeout())) {
                return false;
            }

            if (!started) {
                return false;
            }

            // Cancel any animation
            transform.cancelTransitions();

            // rotation constitutes translation of anything except the center of
            // rotation, so cancel both location and bearing tracking if required

            trackingSettings.resetTrackingModesIfRequired(true, true);

            // Get rotate value
            double bearing = transform.getBearing();
            bearing += detector.getRotationDegreesDelta();

            // Rotate the map
            if (focalPoint != null) {
                // User provided focal point
                transform.setBearing(bearing, focalPoint.x / screenDensity, focalPoint.y / screenDensity);
            } else {
                // around gesture
                transform.setBearing(bearing, detector.getFocusX() / screenDensity, detector.getFocusY() / screenDensity);
            }
            return true;
        }
    }

    // This class handles a vertical two-finger shove. (If you place two fingers on screen with
    // less than a 20 degree angle between them, this will detect movement on the Y-axis.)
    private class ShoveGestureListener implements ShoveGestureDetector.OnShoveGestureListener {

        long beginTime = 0;
        float totalDelta = 0.0f;
        boolean started = false;

        @Override
        public boolean onShoveBegin(ShoveGestureDetector detector) {
            if (!uiSettings.isTiltGesturesEnabled()) {
                return false;
            }

            beginTime = detector.getEventTime();
            MapboxEvent.trackGestureEvent(projection, MapboxEvent.GESTURE_PITCH_START, detector.getFocusX(), detector.getFocusY(), transform.getZoom());
            return true;
        }

        @Override
        public void onShoveEnd(ShoveGestureDetector detector) {
            beginTime = 0;
            totalDelta = 0.0f;
            started = false;
            dragStarted = false;
        }

        @Override
        public boolean onShove(ShoveGestureDetector detector) {
            if (!uiSettings.isTiltGesturesEnabled()) {
                return false;
            }

            // If tilt is large enough ignore a tap
            // Also if zoom already started, don't tilt
            totalDelta += detector.getShovePixelsDelta();
            if (!zoomStarted && ((totalDelta > 10.0f) || (totalDelta < -10.0f))) {
                started = true;
            }

            // Ignore short touches in case it is a tap
            // Also ignore small tilt
            long time = detector.getEventTime();
            long interval = time - beginTime;
            if (!started && (interval <= ViewConfiguration.getTapTimeout())) {
                return false;
            }

            if (!started) {
                return false;
            }

            // Cancel any animation
            transform.cancelTransitions();

            // Get tilt value (scale and clamp)
            double pitch = transform.getTilt();
            pitch -= 0.1 * detector.getShovePixelsDelta();
            pitch = Math.max(MapboxConstants.MINIMUM_TILT, Math.min(MapboxConstants.MAXIMUM_TILT, pitch));

            // Tilt the map
            transform.setTilt(pitch);

            dragStarted = true;

            return true;
        }
    }

    // Called for events that don't fit the other handlers
    // such as mouse scroll events, mouse moves, joystick, trackpad
    boolean onGenericMotionEvent(MotionEvent event) {
        // Mouse events
        //if (event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) { // this is not available before API 18
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == InputDevice.SOURCE_CLASS_POINTER) {
            // Choose the action
            switch (event.getActionMasked()) {
                // Mouse scrolls
                case MotionEvent.ACTION_SCROLL:
                    if (!uiSettings.isZoomGesturesEnabled()) {
                        return false;
                    }

                    // Cancel any animation
                    transform.cancelTransitions();

                    // Get the vertical scroll amount, one click = 1
                    float scrollDist = event.getAxisValue(MotionEvent.AXIS_VSCROLL);

                    // Scale the map by the appropriate power of two factor
                    transform.zoomBy(Math.pow(2.0, scrollDist), event.getX() / screenDensity, event.getY() / screenDensity);

                    return true;

                default:
                    // We are not interested in this event
                    return false;
            }
        }

        // We are not interested in this event
        return false;
    }

    void setOnMapClickListener(MapboxMap.OnMapClickListener onMapClickListener) {
        this.onMapClickListener = onMapClickListener;
    }

    void setOnMapLongClickListener(MapboxMap.OnMapLongClickListener onMapLongClickListener) {
        this.onMapLongClickListener = onMapLongClickListener;
    }

    void setOnFlingListener(MapboxMap.OnFlingListener onFlingListener) {
        this.onFlingListener = onFlingListener;
    }

    void setOnScrollListener(MapboxMap.OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }
}