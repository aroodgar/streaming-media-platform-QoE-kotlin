# streaming-media-platform-QoE-kotlin

This project provides the measurement of 3 QoE metrics for video streaming platforms.
The 3 metrics are as follows:
- Initial Latency:
> The amount of latency of a video to begin playing since the first user initiated playback. Obviously, the lower the latency, the happier the users.
- Input Buffer Count per second:
> It will give a rough estimation of the amount of data transmitted for each second of data. This piece of data will have the most meaning when compared with the corresponding values of other streaming sources.
- Continuity Rate:
> The amount of input buffers that were able to be rendered without rebuffering interruptions and without being dropped.

## Initial Latency Measurement
The moment that a player object (an exoplayer object in this case) is prepared for playback and sets the `playBackState` for the first time to **`STATE_BUFFERING`**, marks the begining of our timer.

The first moment when the `playBackState` is changed from **`STATE_BUFFERING`** to **`STATE_READY`**, will mark the end of our timer. Thus, the difference between the two moments will be the initial latency.

## Input Buffer Count per second
The Total Buffer Counts of each resource is collected at the end of a video stream and is divided by the `duration` of th video. In other words, it's the average `Input Buffer Count` received by the corresponding decoder object assoicated with each player object.

## Continuity Rate
It's the Ratio of total rendered output buffer count to the total input buffer count. Output buffers might be dropped or skipped due to reasons like rebuffering, etc. This parameter can give a rough sense of the quality of the experience of streaming such video.

## How is the data collected?
Each **`SimpleExoPlayer`** object contains a **`videoDecoderCounters`** property of type **`DecoderCounters`** which contains various information about the current video decoder status. Some of its fields consist of:
- inputBufferCount
- renderedOutputBufferCount
- skippedOutputBufferCount
- droppedBufferCount
- maxConsecutiveDroppedBufferCount
- ...

Most of the above fields are discussed when going through the measured parameters in this project. More detail will be given in the documentation.

Below are some screenshots of the application in action. The video being played and streamed is from [this](https://media.geeksforgeeks.org/wp-content/uploads/20201217192146/Screenrecorder-2020-12-17-19-17-36-828.mp4?_=1) link.

<!-- ![Initial Buffering](/images/initial%20buffering.jpg) -->
<!-- ![](/images/initial_buffering.jpg | width=100) -->
<img src="images/initial_buffering.jpg" alt="initial_buffering" width="400" />

Initial Buffering to load the video.


<img src="images/initial_latency.jpg" alt="initial_latency" width="400" />
<!-- [<img src="/images/initial_latency.jpg" width="50%"/>](/images/initial_latency.jpg) -->

A sample measurement of the initial latency of the video.

<img src="images/mid_video_rebuffer_and_dropped_buffers.jpg" alt="mid_video_rebuffer_and_dropped_buffers" width="400" />

A rebuffer occurs in mid-streaming. Pay attention to the next screenshot for the dropped buffers count (db).

<img src="images/end.jpg" alt="end" width="400" />

We see the continuity rate which is the result of the following division: 489 / 492. The first number as suggested by the screenshot is the total input buffer count and the second number is the total output buffer count. The number of the db (dropped buffers) is 3 which is equal to 492 - 489. The mcdb (max consequtive dropped buffers) can also be of interest here. `mcdb` and `db` values are equal in this example which mean only one incident of rebuffer has happened midplay.

**You can also find a settings page at the start of the program for tweaking with the local buffer configurations. The effect will obviously be same against different sources of media, as we are only changing our own local settings. Namely, this would only affect the performance of the decoding process of the application.**

<img src="images/config.jpg" alt="config" width="400" />

## More parameters
As seen in the above screenshots, there are some other values extracted from the **`videoDecoderCounters`** property which are as follows:
- rb (rendered buffers)
- db (dropped buffers)
- mcdb (max consequtive dropped buffers)
- vfpo (total video frame processing offset (microseconds))
> The sum of the video frame processing offsets in microseconds.
>
> The processing offset for a video frame is the difference between the time at which the frame became available to render, and the time at which it was scheduled to be rendered. A positive value indicates the frame became available early enough, whereas a negative value indicates that the frame wasn't available until after the time at which it should have been rendered.

