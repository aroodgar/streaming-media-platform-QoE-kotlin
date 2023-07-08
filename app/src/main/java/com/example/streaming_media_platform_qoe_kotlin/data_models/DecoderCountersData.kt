package com.example.streaming_media_platform_qoe_kotlin.data_models

enum class DecoderCountersDataStrId {
    DECODER_INIT_COUNT {
        override fun getStr() = "decInitCnt"
    },
    DECODER_RELEASE_COUNT {
        override fun getStr() = "decRelCnt"
    },
    INPUT_BUFFER_COUNT {
        override fun getStr() = "inpBufCnt"
    },
    SKIPPED_INPUT_BUFFER_COUNT {
        override fun getStr() = "skpInpBufCnt"
    },
    RENDERED_OUTPUT_BUFFER_COUNT {
        override fun getStr() = "rendOutBufCnt"
    },
    SKIPPED_OUTPUT_BUFFER_COUNT {
        override fun getStr() = "skpOutBufCnt"
    },
    DROPPED_BUFFER_COUNT {
        override fun getStr() = "drpBufCnt"
    },
    MAX_CONSECUTIVE_DROPPED_BUFFER_COUNT {
        override fun getStr() = "maxConsecDrpBufCnt"
    },
    DROPPED_TO_KEY_FRAME_COUNT {
        override fun getStr() = "drpToKeyFrmCnt"
    },
    TOTAL_VIDEO_FRAME_PROCESSING_OFFSET_US {
        override fun getStr() = "totalVidFrmProcOffsetUs"
    },
    VIDEO_FRAME_PROCESSING_OFFSET_COUNT {
        override fun getStr() = "vidFrmProcOffsetCnt"
    };

    abstract fun getStr(): String
}
public class DecoderCountersData {

    constructor(decoderInitCount: Int, decoderReleaseCount: Int, inputBufferCount: Int, skippedInputBufferCount: Int, renderedOutputBufferCount: Int,
                skippedOutputBufferCount: Int, droppedBufferCount: Int, maxConsecutiveDroppedBufferCount: Int, droppedToKeyframeCount:Int,
                totalVideoFrameProcessingOffsetUs: Long, videoFrameProcessingOffsetCount: Int) {
        this.decoderInitCount = decoderInitCount
        this.decoderReleaseCount = decoderReleaseCount
        this.inputBufferCount = inputBufferCount
        this.skippedInputBufferCount = skippedInputBufferCount
        this.renderedOutputBufferCount = renderedOutputBufferCount
        this.skippedOutputBufferCount = skippedOutputBufferCount
        this.droppedBufferCount = droppedBufferCount
        this.maxConsecutiveDroppedBufferCount = maxConsecutiveDroppedBufferCount
        this.droppedToKeyframeCount = droppedToKeyframeCount
        this.totalVideoFrameProcessingOffsetUs = totalVideoFrameProcessingOffsetUs
        this.videoFrameProcessingOffsetCount = videoFrameProcessingOffsetCount
    }

    public override fun toString(): String {
        val decInitCntStr: String =
            "${DecoderCountersDataStrId.DECODER_INIT_COUNT.getStr()}=${decoderInitCount}"
        val decRelCnt: String =
            "${DecoderCountersDataStrId.DECODER_RELEASE_COUNT.getStr()}=${decoderReleaseCount}"
        val inpBufCntStr: String =
            "${DecoderCountersDataStrId.INPUT_BUFFER_COUNT.getStr()}=${inputBufferCount}"
        val skpInpBufCntStr: String =
            "${DecoderCountersDataStrId.SKIPPED_INPUT_BUFFER_COUNT.getStr()}=${skippedInputBufferCount}"
        val rendOutBufCntStr: String =
            "${DecoderCountersDataStrId.RENDERED_OUTPUT_BUFFER_COUNT.getStr()}=${renderedOutputBufferCount}"
        val skpOutBufCntStr: String =
            "${DecoderCountersDataStrId.SKIPPED_OUTPUT_BUFFER_COUNT.getStr()}=${skippedOutputBufferCount}"
        val drpBufCntStr: String =
            "${DecoderCountersDataStrId.DROPPED_BUFFER_COUNT.getStr()}=${droppedBufferCount}"
        val maxConsecDrpBufCntStr: String =
            "${DecoderCountersDataStrId.MAX_CONSECUTIVE_DROPPED_BUFFER_COUNT.getStr()}=${maxConsecutiveDroppedBufferCount}"
        val drpToKeyFrmCntStr: String =
            "${DecoderCountersDataStrId.DROPPED_TO_KEY_FRAME_COUNT.getStr()}=${droppedToKeyframeCount}"
        val totalVidFrmProcOffsetUsStr: String =
            "${DecoderCountersDataStrId.TOTAL_VIDEO_FRAME_PROCESSING_OFFSET_US.getStr()}=${totalVideoFrameProcessingOffsetUs}"
        val vidFrmProcOffsetCntStr: String =
            "${DecoderCountersDataStrId.VIDEO_FRAME_PROCESSING_OFFSET_COUNT.getStr()}=${videoFrameProcessingOffsetCount}"

        val strings: List<String> = listOf(
            decInitCntStr,
            decRelCnt,
            inpBufCntStr,
            skpInpBufCntStr,
            rendOutBufCntStr,
            skpOutBufCntStr,
            drpBufCntStr,
            maxConsecDrpBufCntStr,
            drpToKeyFrmCntStr,
            totalVidFrmProcOffsetUsStr,
            vidFrmProcOffsetCntStr
        )
        return strings.joinToString(separator = ",")
    }
    /**
     * The number of times a decoder has been initialized.
     */
    var decoderInitCount: Int = 0

    /**
     * The number of times a decoder has been released.
     */
    var decoderReleaseCount: Int = 0

    /**
     * The number of queued input buffers.
     */
    var inputBufferCount: Int = 0

    /**
     * The number of skipped input buffers.
     *
     *
     * A skipped input buffer is an input buffer that was deliberately not sent to the decoder.
     */
    var skippedInputBufferCount: Int = 0

    /**
     * The number of rendered output buffers.
     */
    var renderedOutputBufferCount: Int = 0

    /**
     * The number of skipped output buffers.
     *
     *
     * A skipped output buffer is an output buffer that was deliberately not rendered.
     */
    var skippedOutputBufferCount: Int = 0

    /**
     * The number of dropped buffers.
     *
     *
     * A dropped buffer is an buffer that was supposed to be decoded/rendered, but was instead
     * dropped because it could not be rendered in time.
     */
    var droppedBufferCount: Int= 0

    /**
     * The maximum number of dropped buffers without an interleaving rendered output buffer.
     *
     *
     * Skipped output buffers are ignored for the purposes of calculating this value.
     */
    var maxConsecutiveDroppedBufferCount: Int = 0

    /**
     * The number of times all buffers to a keyframe were dropped.
     *
     *
     * Each time buffers to a keyframe are dropped, this counter is increased by one, and the dropped
     * buffer counters are increased by one (for the current output buffer) plus the number of buffers
     * dropped from the source to advance to the keyframe.
     */
    var droppedToKeyframeCount: Int = 0

    /**
     * The sum of the video frame processing offsets in microseconds.
     *
     *
     * The processing offset for a video frame is the difference between the time at which the
     * frame became available to render, and the time at which it was scheduled to be rendered. A
     * positive value indicates the frame became available early enough, whereas a negative value
     * indicates that the frame wasn't available until after the time at which it should have been
     * rendered.
     *
     *
     * Note: Use [.addVideoFrameProcessingOffset] to update this field instead of
     * updating it directly.
     */
    var totalVideoFrameProcessingOffsetUs: Long = 0

    /**
     * The number of video frame processing offsets added.
     *
     *
     * Note: Use [.addVideoFrameProcessingOffset] to update this field instead of
     * updating it directly.
     */
    var videoFrameProcessingOffsetCount: Int = 0


}