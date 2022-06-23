package com.example.picturediary

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.FrameLayout
import java.io.IOException


/*
 *    The MIT License (MIT)
 *
 *   Copyright (c) 2014 Danylyk Dmytro
 *   Copyright (c) 2014 Jacob Tabak
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

/*
 *    The MIT License (MIT)
 *
 *   Copyright (c) 2014 Danylyk Dmytro
 *   Copyright (c) 2014 Jacob Tabak
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */
class TextureVideoView : FrameLayout, SurfaceTextureListener {
    private var mMediaPlayer: MediaPlayer? = null
    private var mVideoHeight = 0f
    private var mVideoWidth = 0f
    private var mIsDataSourceSet = false
    private var mIsViewAvailable = false
    private var mIsVideoPrepared = false
    private var mIsPlayCalled = false
    private var mScaleType: ScaleType? = null
    private var mState: State? = null
    private var mTextureView: TextureView? = null

    enum class ScaleType {
        CENTER_CROP, TOP, BOTTOM
    }

    enum class State {
        UNINITIALIZED, PLAY, STOP, PAUSE, END
    }

    constructor(context: Context?) : super(context!!) {
        if (!isInEditMode) {
            initView()
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        if (!isInEditMode) {
            initView()
        }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        if (!isInEditMode) {
            initView()
        }
    }

    private fun initView() {
        initPlayer()
        mTextureView = TextureView(context)
        removeAllViews()
        addView(mTextureView)
        setScaleType(ScaleType.CENTER_CROP)
        mTextureView!!.surfaceTextureListener = this
    }

    fun setScaleType(scaleType: ScaleType?) {
        mScaleType = scaleType
    }

    private fun updateTextureViewSize() {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        var scaleX = 1.0f
        var scaleY = 1.0f
        if (mVideoWidth >= viewWidth && mVideoHeight >= viewHeight) {
            scaleX = mVideoWidth / viewWidth
            scaleY = mVideoHeight / viewHeight
        } else if (mVideoWidth <= viewWidth && mVideoHeight <= viewHeight) {
            scaleY = viewWidth / mVideoWidth
            scaleX = viewHeight / mVideoHeight
        } else if (viewWidth >= mVideoWidth) {
            scaleY = viewWidth / mVideoWidth / (viewHeight / mVideoHeight)
        } else if (viewHeight >= mVideoHeight) {
            scaleX = viewHeight / mVideoHeight / (viewWidth / mVideoWidth)
        }

        // Calculate pivot points, in our case crop from center
        val pivotPointX: Int
        val pivotPointY: Int
        when (mScaleType) {
            ScaleType.TOP -> {
                pivotPointX = 0
                pivotPointY = 0
            }
            ScaleType.BOTTOM -> {
                pivotPointX = viewWidth.toInt()
                pivotPointY = viewHeight.toInt()
            }
            ScaleType.CENTER_CROP -> {
                pivotPointX = (viewWidth / 2).toInt()
                pivotPointY = (viewHeight / 2).toInt()
            }
            else -> {
                pivotPointX = (viewWidth / 2).toInt()
                pivotPointY = (viewHeight / 2).toInt()
            }
        }
        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, pivotPointX.toFloat(), pivotPointY.toFloat())
        mTextureView!!.setTransform(matrix)
    }

    private fun initPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
        } else {
            mMediaPlayer!!.reset()
        }
        mIsVideoPrepared = false
        mIsPlayCalled = false
        mState = State.UNINITIALIZED
    }

    val isPlaying: Boolean
        get() = if (mMediaPlayer != null) {
            mMediaPlayer!!.isPlaying
        } else false

    /**
     * @see android.media.MediaPlayer.setDataSource
     */
    fun setDataSource(path: String?) {
        initPlayer()
        try {
            mMediaPlayer!!.setDataSource(path)
            mIsDataSourceSet = true
            prepare()
        } catch (e: IOException) {
            Log.d(TAG, e.message!!)
        }
    }

    /**
     * @see android.media.MediaPlayer.setDataSource
     */
    fun setDataSource(context: Context?, uri: Uri?) {
        initPlayer()
        try {
            mMediaPlayer!!.setDataSource(context!!, uri!!)
            mIsDataSourceSet = true
            prepare()
        } catch (e: IOException) {
            Log.d(TAG, e.message!!)
        }
    }

    /**
     * @see android.media.MediaPlayer.setDataSource
     */
    fun setDataSource(afd: AssetFileDescriptor) {
        initPlayer()
        try {
            val startOffset = afd.startOffset
            val length = afd.length
            mMediaPlayer!!.setDataSource(afd.fileDescriptor, startOffset, length)
            mIsDataSourceSet = true
            prepare()
        } catch (e: IOException) {
            Log.d(TAG, e.message!!)
        }
    }

    private fun prepare() {
        try {
            mMediaPlayer!!.setOnVideoSizeChangedListener { mp, width, height ->
                mVideoWidth = width.toFloat()
                mVideoHeight = height.toFloat()
                updateTextureViewSize()
            }
            mMediaPlayer!!.setOnCompletionListener {
                mState = State.END
                log("Video has ended.")
                if (mListener != null) {
                    mListener!!.onVideoEnd()
                }
            }
            mMediaPlayer!!.prepareAsync()

            // Play video when the media source is ready for playback.
            mMediaPlayer!!.setOnPreparedListener {
                mIsVideoPrepared = true
                if (mIsPlayCalled && mIsViewAvailable) {
                    log("Player is prepared and play() was called.")
                    play()
                }
                if (mListener != null) {
                    mListener!!.onVideoPrepared()
                }
            }
            mMediaPlayer!!.setOnErrorListener { mediaPlayer, i, i2 -> false }
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, e.message!!)
        } catch (e: SecurityException) {
            Log.d(TAG, e.message!!)
        } catch (e: IllegalStateException) {
            Log.d(TAG, e.toString())
        }
    }

    /**
     * Play or resume video. Video will be played as soon as view is available and media player is
     * prepared.
     *
     * If video is stopped or ended and play() method was called, video will start over.
     */
    @JvmOverloads
    fun play(startPosition: Int = 0) {
        if (!mIsDataSourceSet) {
            log("play() was called but data source was not set.")
            return
        }
        mIsPlayCalled = true
        if (!mIsVideoPrepared) {
            log("play() was called but video is not prepared yet, waiting.")
            return
        }
        if (!mIsViewAvailable) {
            log("play() was called but view is not available yet, waiting.")
            return
        }
        if (mState == State.PLAY) {
            log("play() was called but video is already playing.")
            return
        }
        if (mState == State.PAUSE) {
            log("play() was called but video is paused, resuming.")
            mState = State.PLAY
            mMediaPlayer!!.start()
            return
        }
        mState = State.PLAY
        mMediaPlayer!!.seekTo(startPosition)
        mMediaPlayer!!.start()
    }

    /**
     * Pause video. If video is already paused, stopped or ended nothing will happen.
     */
    fun pause() {
        if (mState == State.PAUSE) {
            log("pause() was called but video already paused.")
            return
        }
        if (mState == State.STOP) {
            log("pause() was called but video already stopped.")
            return
        }
        if (mState == State.END) {
            log("pause() was called but video already ended.")
            return
        }
        mState = State.PAUSE
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
        }
    }

    /**
     * Stop video (pause and seek to beginning). If video is already stopped or ended nothing will
     * happen.
     */
    fun stop() {
        if (mState == State.STOP) {
            log("stop() was called but video already stopped.")
            return
        }
        if (mState == State.END) {
            log("stop() was called but video already ended.")
            return
        }
        mState = State.STOP
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            mMediaPlayer!!.seekTo(0)
        }
    }

    /**
     * @see android.media.MediaPlayer.setLooping
     */
    fun setLooping(looping: Boolean) {
        mMediaPlayer!!.isLooping = looping
    }

    /**
     * @see android.media.MediaPlayer.seekTo
     */
    fun seekTo(milliseconds: Int) {
        mMediaPlayer!!.seekTo(milliseconds)
    }

    /**
     * @see android.media.MediaPlayer.getDuration
     */
    val duration: Int
        get() = mMediaPlayer!!.duration

    /**
     * @see android.media.MediaPlayer.getCurrentPosition
     */
    val currentPosition: Int
        get() = mMediaPlayer!!.currentPosition
    private var mListener: MediaPlayerListener? = null

    /**
     * Listener trigger 'onVideoPrepared' and `onVideoEnd` events
     */
    fun setListener(listener: MediaPlayerListener?) {
        mListener = listener
    }

    interface MediaPlayerListener {
        fun onVideoPrepared()
        fun onVideoEnd()
    }

    override fun onSurfaceTextureAvailable(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        val surface = Surface(surfaceTexture)
        mMediaPlayer!!.setSurface(surface)
        mIsViewAvailable = true
        if (mIsDataSourceSet && mIsPlayCalled && mIsVideoPrepared) {
            log("View is available and play() was called.")
            play()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    companion object {
        // Indicate if logging is on
        const val LOG_ON = true

        // Log tag
        private val TAG = TextureVideoView::class.java.name
        fun log(message: String?) {
            if (LOG_ON) {
                Log.d(TAG, message!!)
            }
        }
    }
}