package com.example.streaming_media_platform_qoe_kotlin.data_models

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.decoder.DecoderCounters

public class Utils {

    companion object {
        public fun getGeneralDecoderCountersBufferCountData(player: SimpleExoPlayer): DecoderCountersData? {
            var tmpDecoderCounters: DecoderCounters? = player.videoDecoderCounters ?: return null

            var decoderCounters: DecoderCounters = tmpDecoderCounters as DecoderCounters
            decoderCounters.ensureUpdated()

            return DecoderCountersData(
                decoderInitCount = decoderCounters.decoderInitCount,
                decoderReleaseCount = decoderCounters.decoderReleaseCount,
                inputBufferCount = decoderCounters.inputBufferCount,
                skippedInputBufferCount = decoderCounters.skippedInputBufferCount,
                renderedOutputBufferCount = decoderCounters.renderedOutputBufferCount,
                skippedOutputBufferCount = decoderCounters.skippedOutputBufferCount,
                droppedBufferCount = decoderCounters.droppedBufferCount,
                maxConsecutiveDroppedBufferCount = decoderCounters.maxConsecutiveDroppedBufferCount,
                droppedToKeyframeCount = decoderCounters.droppedToKeyframeCount,
                totalVideoFrameProcessingOffsetUs = decoderCounters.totalVideoFrameProcessingOffsetUs,
                videoFrameProcessingOffsetCount = decoderCounters.videoFrameProcessingOffsetCount
            )
        }
    }

}