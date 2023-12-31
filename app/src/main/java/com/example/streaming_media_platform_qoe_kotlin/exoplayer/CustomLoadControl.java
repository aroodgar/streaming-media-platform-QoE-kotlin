/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.streaming_media_platform_qoe_kotlin.exoplayer;

import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.PriorityTaskManager;
import com.google.android.exoplayer2.util.Util;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The custom {@link LoadControl} implementation.
 * Implementing Drip feeding as in: https://medium.com/@filipluch/how-to-improve-buffering-by-4-times-with-drip-feeding-technique-in-exoplayer-on-android-b59eb0c4d9cc
 */
// code of customized buffer and custom error handling is the work of the following repo:
// https://github.com/DaliaAyman/ExoPlayer-CustomizedBuffer
public class CustomLoadControl implements LoadControl {

    /**
     * The default minimum duration of media that the player will attempt to ensure is buffered at all
     * times, in milliseconds.
     */
    public static final int DEFAULT_MIN_BUFFER_MS = 15000;

    /**
     * The default maximum duration of media that the player will attempt to buffer, in milliseconds.
     */
    public static final int DEFAULT_MAX_BUFFER_MS = 50000;

    /**
     * The default duration of media that must be buffered for playback to start or resume following a
     * user action such as a seek, in milliseconds.
     */
    public static final int DEFAULT_BUFFER_FOR_PLAYBACK_MS = 2500;

    /**
     * The default duration of media that must be buffered for playback to resume after a rebuffer, in
     * milliseconds. A rebuffer is defined to be caused by buffer depletion rather than a user action.
     */
    public static final int DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000;

    /**
     * The default target buffer size in bytes. The value ({@link C#LENGTH_UNSET}) means that the load
     * control will calculate the target buffer size based on the selected tracks.
     */
    public static final int DEFAULT_TARGET_BUFFER_BYTES = C.LENGTH_UNSET;

    /** The default prioritization of buffer time constraints over size constraints. */
    public static final boolean DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS = false;

    /** The default back buffer duration in milliseconds. */
    public static final int DEFAULT_BACK_BUFFER_DURATION_MS = 0;

    /** The default for whether the back buffer is retained from the previous keyframe. */
    public static final boolean DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME = false;

    /** A default size in bytes for a video buffer. */
    public static final int DEFAULT_VIDEO_BUFFER_SIZE = 2000 * C.DEFAULT_BUFFER_SEGMENT_SIZE;

    /** A default size in bytes for an audio buffer. */
    public static final int DEFAULT_AUDIO_BUFFER_SIZE = 200 * C.DEFAULT_BUFFER_SEGMENT_SIZE;

    /** A default size in bytes for a text buffer. */
    public static final int DEFAULT_TEXT_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE;

    /** A default size in bytes for a metadata buffer. */
    public static final int DEFAULT_METADATA_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE;

    /** A default size in bytes for a camera motion buffer. */
    public static final int DEFAULT_CAMERA_MOTION_BUFFER_SIZE = 2 * C.DEFAULT_BUFFER_SEGMENT_SIZE;

    /** A default size in bytes for a muxed buffer (e.g. containing video, audio and text). */
    public static final int DEFAULT_MUXED_BUFFER_SIZE =
            DEFAULT_VIDEO_BUFFER_SIZE + DEFAULT_AUDIO_BUFFER_SIZE + DEFAULT_TEXT_BUFFER_SIZE;

    /**
     * The buffer size in bytes that will be used as a minimum target buffer in all cases. This is
     * also the default target buffer before tracks are selected.
     */
    public static final int DEFAULT_MIN_BUFFER_SIZE = 200 * C.DEFAULT_BUFFER_SEGMENT_SIZE;

    /**
     * Priority for media loading.
     */
    public static final int LOADING_PRIORITY = 0;


    private EventListener bufferedDurationListener;
    private Handler eventHandler= new Handler();

    private static final int ABOVE_HIGH_WATERMARK = 0;
    private static final int BETWEEN_WATERMARKS = 1;
    private static final int BELOW_LOW_WATERMARK = 2;

    private final DefaultAllocator allocator;

    private final PriorityTaskManager priorityTaskManager;


    /** Builder for {@link CustomLoadControl}. */
    public static final class Builder {

        @Nullable private DefaultAllocator allocator;
        private int minBufferMs;
        private int maxBufferMs;
        private int bufferForPlaybackMs;
        private int bufferForPlaybackAfterRebufferMs;
        private int targetBufferBytes;
        private boolean prioritizeTimeOverSizeThresholds;
        private int backBufferDurationMs;
        private boolean retainBackBufferFromKeyframe;
        private boolean buildCalled;

        /** Constructs a new instance. */
        public Builder() {
            minBufferMs = DEFAULT_MIN_BUFFER_MS;
            maxBufferMs = DEFAULT_MAX_BUFFER_MS;
            bufferForPlaybackMs = DEFAULT_BUFFER_FOR_PLAYBACK_MS;
            bufferForPlaybackAfterRebufferMs = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
            targetBufferBytes = DEFAULT_TARGET_BUFFER_BYTES;
            prioritizeTimeOverSizeThresholds = DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS;
            backBufferDurationMs = DEFAULT_BACK_BUFFER_DURATION_MS;
            retainBackBufferFromKeyframe = DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME;
        }

        /**
         * Sets the {@link DefaultAllocator} used by the loader.
         *
         * @param allocator The {@link DefaultAllocator}.
         * @return This builder, for convenience.
         * @throws IllegalStateException If {@link #build()} has already been called.
         */
        public Builder setAllocator(DefaultAllocator allocator) {
            Assertions.checkState(!buildCalled);
            this.allocator = allocator;
            return this;
        }

        /**
         * Sets the buffer duration parameters.
         *
         * @param minBufferMs The minimum duration of media that the player will attempt to ensure is
         *     buffered at all times, in milliseconds.
         * @param maxBufferMs The maximum duration of media that the player will attempt to buffer, in
         *     milliseconds.
         * @param bufferForPlaybackMs The duration of media that must be buffered for playback to start
         *     or resume following a user action such as a seek, in milliseconds.
         * @param bufferForPlaybackAfterRebufferMs The default duration of media that must be buffered
         *     for playback to resume after a rebuffer, in milliseconds. A rebuffer is defined to be
         *     caused by buffer depletion rather than a user action.
         * @return This builder, for convenience.
         * @throws IllegalStateException If {@link #build()} has already been called.
         */
        public Builder setBufferDurationsMs(
                int minBufferMs,
                int maxBufferMs,
                int bufferForPlaybackMs,
                int bufferForPlaybackAfterRebufferMs) {
            Assertions.checkState(!buildCalled);
            assertGreaterOrEqual(bufferForPlaybackMs, 0, "bufferForPlaybackMs", "0");
            assertGreaterOrEqual(
                    bufferForPlaybackAfterRebufferMs, 0, "bufferForPlaybackAfterRebufferMs", "0");
            assertGreaterOrEqual(minBufferMs, bufferForPlaybackMs, "minBufferMs", "bufferForPlaybackMs");
            assertGreaterOrEqual(
                    minBufferMs,
                    bufferForPlaybackAfterRebufferMs,
                    "minBufferMs",
                    "bufferForPlaybackAfterRebufferMs");
            assertGreaterOrEqual(maxBufferMs, minBufferMs, "maxBufferMs", "minBufferMs");
            this.minBufferMs = minBufferMs;
            this.maxBufferMs = maxBufferMs;
            this.bufferForPlaybackMs = bufferForPlaybackMs;
            this.bufferForPlaybackAfterRebufferMs = bufferForPlaybackAfterRebufferMs;
            return this;
        }

        /**
         * Sets the target buffer size in bytes. If set to {@link C#LENGTH_UNSET}, the target buffer
         * size will be calculated based on the selected tracks.
         *
         * @param targetBufferBytes The target buffer size in bytes.
         * @return This builder, for convenience.
         * @throws IllegalStateException If {@link #build()} has already been called.
         */
        public Builder setTargetBufferBytes(int targetBufferBytes) {
            Assertions.checkState(!buildCalled);
            this.targetBufferBytes = targetBufferBytes;
            return this;
        }

        /**
         * Sets whether the load control prioritizes buffer time constraints over buffer size
         * constraints.
         *
         * @param prioritizeTimeOverSizeThresholds Whether the load control prioritizes buffer time
         *     constraints over buffer size constraints.
         * @return This builder, for convenience.
         * @throws IllegalStateException If {@link #build()} has already been called.
         */
        public Builder setPrioritizeTimeOverSizeThresholds(boolean prioritizeTimeOverSizeThresholds) {
            Assertions.checkState(!buildCalled);
            this.prioritizeTimeOverSizeThresholds = prioritizeTimeOverSizeThresholds;
            return this;
        }

        /**
         * Sets the back buffer duration, and whether the back buffer is retained from the previous
         * keyframe.
         *
         * @param backBufferDurationMs The back buffer duration in milliseconds.
         * @param retainBackBufferFromKeyframe Whether the back buffer is retained from the previous
         *     keyframe.
         * @return This builder, for convenience.
         * @throws IllegalStateException If {@link #build()} has already been called.
         */
        public Builder setBackBuffer(int backBufferDurationMs, boolean retainBackBufferFromKeyframe) {
            Assertions.checkState(!buildCalled);
            assertGreaterOrEqual(backBufferDurationMs, 0, "backBufferDurationMs", "0");
            this.backBufferDurationMs = backBufferDurationMs;
            this.retainBackBufferFromKeyframe = retainBackBufferFromKeyframe;
            return this;
        }

        /** @deprecated use {@link #build} instead. */
        @Deprecated
        public CustomLoadControl createCustomLoadControl() {
            return build();
        }

        /** Creates a {@link CustomLoadControl}. */
        public CustomLoadControl build() {
            Assertions.checkState(!buildCalled);
            buildCalled = true;
            if (allocator == null) {
                allocator = new DefaultAllocator(/* trimOnReset= */ true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
            }
            return new CustomLoadControl(
                    allocator,
                    minBufferMs,
                    maxBufferMs,
                    bufferForPlaybackMs,
                    bufferForPlaybackAfterRebufferMs,
                    targetBufferBytes,
                    prioritizeTimeOverSizeThresholds,
                    backBufferDurationMs,
                    retainBackBufferFromKeyframe,
                    null);
        }
    }

    private final long minBufferUs;
    private final long maxBufferUs;
    private final long bufferForPlaybackUs;
    private final long bufferForPlaybackAfterRebufferUs;
    private final int targetBufferBytesOverwrite;
    private final boolean prioritizeTimeOverSizeThresholds;
    private final long backBufferDurationUs;
    private final boolean retainBackBufferFromKeyframe;

    private int targetBufferBytes;
    private boolean isBuffering;

    /** Constructs a new instance, using the {@code DEFAULT_*} constants defined in this class. */
    @SuppressWarnings("deprecation")
    public CustomLoadControl() {
        this(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE));
    }

    /** @deprecated Use {@link Builder} instead. */
    @Deprecated
    public CustomLoadControl(DefaultAllocator allocator) {
        this(
                allocator,
                DEFAULT_MIN_BUFFER_MS,
                DEFAULT_MAX_BUFFER_MS,
                DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                DEFAULT_TARGET_BUFFER_BYTES,
                DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
                DEFAULT_BACK_BUFFER_DURATION_MS,
                DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME,
                null);
    }

    /** @deprecated Use {@link Builder} instead. */
    @Deprecated
    public CustomLoadControl(
            DefaultAllocator allocator,
            int minBufferMs,
            int maxBufferMs,
            int bufferForPlaybackMs,
            int bufferForPlaybackAfterRebufferMs,
            int targetBufferBytes,
            boolean prioritizeTimeOverSizeThresholds) {
        this(
                allocator,
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForPlaybackAfterRebufferMs,
                targetBufferBytes,
                prioritizeTimeOverSizeThresholds,
                DEFAULT_BACK_BUFFER_DURATION_MS,
                DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME,
                null);
    }

    protected CustomLoadControl(
            DefaultAllocator allocator,
            int minBufferMs,
            int maxBufferMs,
            int bufferForPlaybackMs,
            int bufferForPlaybackAfterRebufferMs,
            int targetBufferBytes,
            boolean prioritizeTimeOverSizeThresholds,
            int backBufferDurationMs,
            boolean retainBackBufferFromKeyframe,
            PriorityTaskManager priorityTaskManager) {
        assertGreaterOrEqual(bufferForPlaybackMs, 0, "bufferForPlaybackMs", "0");
        assertGreaterOrEqual(
                bufferForPlaybackAfterRebufferMs, 0, "bufferForPlaybackAfterRebufferMs", "0");
        assertGreaterOrEqual(minBufferMs, bufferForPlaybackMs, "minBufferMs", "bufferForPlaybackMs");
        assertGreaterOrEqual(
                minBufferMs,
                bufferForPlaybackAfterRebufferMs,
                "minBufferMs",
                "bufferForPlaybackAfterRebufferMs");
        assertGreaterOrEqual(maxBufferMs, minBufferMs, "maxBufferMs", "minBufferMs");
        assertGreaterOrEqual(backBufferDurationMs, 0, "backBufferDurationMs", "0");

        this.allocator = allocator;
        this.minBufferUs = C.msToUs(minBufferMs);
        this.maxBufferUs = C.msToUs(maxBufferMs);
        this.bufferForPlaybackUs = C.msToUs(bufferForPlaybackMs);
        this.bufferForPlaybackAfterRebufferUs = C.msToUs(bufferForPlaybackAfterRebufferMs);
        this.targetBufferBytesOverwrite = targetBufferBytes;
        this.targetBufferBytes =
                targetBufferBytesOverwrite != C.LENGTH_UNSET
                        ? targetBufferBytesOverwrite
                        : DEFAULT_MIN_BUFFER_SIZE;
        this.prioritizeTimeOverSizeThresholds = prioritizeTimeOverSizeThresholds;
        this.backBufferDurationUs = C.msToUs(backBufferDurationMs);
        this.retainBackBufferFromKeyframe = retainBackBufferFromKeyframe;
        this.priorityTaskManager = priorityTaskManager;
    }

    /**
     * Constructs a new instance, using the {@code DEFAULT_*} constants defined in this class.
     */
    public CustomLoadControl(EventListener listener) {
        this(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE));
        bufferedDurationListener = listener;
        //    eventHandler = handler;
    }

    @Override
    public void onPrepared() {
        reset(false);
    }

    @Override
    public void onTracksSelected(Renderer[] renderers, TrackGroupArray trackGroups,
                                 ExoTrackSelection[] trackSelections) {
//        ArrayList<ExoTrackSelection> listTrackSelections = new ArrayList<>(Arrays.asList(trackSelections));
        TrackSelectionArray trackSelectionsList = new TrackSelectionArray(trackSelections);
        targetBufferBytes =
                targetBufferBytesOverwrite == C.LENGTH_UNSET
                        ? calculateTargetBufferBytes(renderers, trackSelectionsList)
                        : targetBufferBytesOverwrite;
        allocator.setTargetBufferSize(targetBufferBytes);
    }

    @Override
    public void onStopped() {
        reset(true);
    }

    @Override
    public void onReleased() {
        reset(true);
    }

    @Override
    public Allocator getAllocator() {
        return allocator;
    }

    @Override
    public long getBackBufferDurationUs() {
        return backBufferDurationUs;
    }

    @Override
    public boolean retainBackBufferFromKeyframe() {
        return retainBackBufferFromKeyframe;
    }

    @Override
    public boolean shouldContinueLoading(
            long playbackPositionUs, long bufferedDurationUs, float playbackSpeed) {
        boolean targetBufferSizeReached = allocator.getTotalBytesAllocated() >= targetBufferBytes;

        boolean wasBuffering = isBuffering;
//        long minBufferUs = this.minBufferUs;
//
//        if (playbackSpeed > 1) {
//            // The playback speed is faster than real time, so scale up the minimum required media
//            // duration to keep enough media buffered for a playout duration of minBufferUs.
//            long mediaDurationMinBufferUs =
//                    Util.getMediaDurationForPlayoutDuration(minBufferUs, playbackSpeed);
//            minBufferUs = min(mediaDurationMinBufferUs, maxBufferUs);
//        }
//        // Prevent playback from getting stuck if minBufferUs is too small.
//        minBufferUs = max(minBufferUs, 500_000);
//        if (bufferedDurationUs < minBufferUs) {
//            isBuffering = prioritizeTimeOverSizeThresholds || !targetBufferSizeReached;
//            if (!isBuffering && bufferedDurationUs < 500_000) {
//                Log.w(
//                        "CustomLoadControl",
//                        "Target buffer size reached with less than 500ms of buffered media data.");
//            }
//        } else if (bufferedDurationUs > maxBufferUs || targetBufferSizeReached) {
//            isBuffering = false;
//        } // Else don't change the buffering state

        Log.d("EventLogger", "CustomLoadControl shouldContinueLoading ");
        computeIsBuffering(bufferedDurationUs);
//        if(priorityTaskManager!=null && isBuffering!=wasBuffering){
//            if(isBuffering){
//                priorityTaskManager.add(C.PRIORITY_PLAYBACK);
//            }
//            else{
//                priorityTaskManager.remove(C.PRIORITY_PLAYBACK);
//            }
//        }

        Log.d("EventLogger", "CustomLoadControl shouldContinueLoading isBuffering: " + isBuffering);


        return isBuffering;
    }

    private void computeIsBuffering(long bufferedDurationUs){
        int bufferTimeState = getBufferTimeState(bufferedDurationUs);
        boolean targetBufferSizeReached = allocator.getTotalBytesAllocated()>=targetBufferBytes;
        Log.d("EventLogger", "CustomLoadControl computeIsBuffering bufferTimeState: " + printBufferTimeState(bufferTimeState));
        Log.d("EventLogger", "CustomLoadControl computeIsBuffering targetBufferSizeReached: " + targetBufferSizeReached);

        if (bufferTimeState == BELOW_LOW_WATERMARK){
            isBuffering = true;
        }
        else if (bufferTimeState  == BETWEEN_WATERMARKS){
            isBuffering = !targetBufferSizeReached;
        }
        else{
            isBuffering = false;
        }

    }

    private int getBufferTimeState(long bufferedDurationUs) {
        return bufferedDurationUs > maxBufferUs ? ABOVE_HIGH_WATERMARK
                : (bufferedDurationUs < minBufferUs ? BELOW_LOW_WATERMARK : BETWEEN_WATERMARKS);
    }

    private String printBufferTimeState(int value){
//        private static final int ABOVE_HIGH_WATERMARK = 0;
//        private static final int BETWEEN_WATERMARKS = 1;
//        private static final int BELOW_LOW_WATERMARK = 2;
        switch (value){
            case 0: return "ABOVE_HIGH_WATERMARK";
            case 1: return "BETWEEN_WATERMARKS";
            case 2: return "BELOW_LOW_WATERMARK";
        }
        return "";
    }

    @Override
    public boolean shouldStartPlayback(
            long bufferedDurationUs, float playbackSpeed, boolean rebuffering, long targetLiveOffsetUs) {
        bufferedDurationUs = Util.getPlayoutDurationForMediaDuration(bufferedDurationUs, playbackSpeed);
        long minBufferDurationUs = rebuffering ? bufferForPlaybackAfterRebufferUs : bufferForPlaybackUs;
        return minBufferDurationUs <= 0
                || bufferedDurationUs >= minBufferDurationUs
                || (!prioritizeTimeOverSizeThresholds
                && allocator.getTotalBytesAllocated() >= targetBufferBytes);
    }

    /**
     * Calculate target buffer size in bytes based on the selected tracks. The player will try not to
     * exceed this target buffer. Only used when {@code targetBufferBytes} is {@link C#LENGTH_UNSET}.
     *
     * @param renderers The renderers for which the track were selected.
     * @param trackSelectionArray The selected tracks.
     * @return The target buffer size in bytes.
     */
    protected int calculateTargetBufferBytes(
            Renderer[] renderers, TrackSelectionArray trackSelectionArray) {
        int targetBufferSize = 0;
        for (int i = 0; i < renderers.length; i++) {
            if (trackSelectionArray.get(i) != null) {
                targetBufferSize += getDefaultBufferSize(renderers[i].getTrackType());
            }
        }
        return max(DEFAULT_MIN_BUFFER_SIZE, targetBufferSize);
    }

    private void reset(boolean resetAllocator) {
        targetBufferBytes =
                targetBufferBytesOverwrite == C.LENGTH_UNSET
                        ? DEFAULT_MIN_BUFFER_SIZE
                        : targetBufferBytesOverwrite;
        isBuffering = false;
        if (resetAllocator) {
            allocator.reset();
        }
    }

    private static int getDefaultBufferSize(int trackType) {
        switch (trackType) {
            case C.TRACK_TYPE_DEFAULT:
                return DEFAULT_MUXED_BUFFER_SIZE;
            case C.TRACK_TYPE_AUDIO:
                return DEFAULT_AUDIO_BUFFER_SIZE;
            case C.TRACK_TYPE_VIDEO:
                return DEFAULT_VIDEO_BUFFER_SIZE;
            case C.TRACK_TYPE_TEXT:
                return DEFAULT_TEXT_BUFFER_SIZE;
            case C.TRACK_TYPE_METADATA:
                return DEFAULT_METADATA_BUFFER_SIZE;
            case C.TRACK_TYPE_CAMERA_MOTION:
                return DEFAULT_CAMERA_MOTION_BUFFER_SIZE;
            case C.TRACK_TYPE_NONE:
                return 0;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void assertGreaterOrEqual(int value1, int value2, String name1, String name2) {
        Assertions.checkArgument(value1 >= value2, name1 + " cannot be less than " + name2);
    }

    public interface EventListener {
        void onBufferedDurationSample(long bufferedDurationUs);
    }
}
