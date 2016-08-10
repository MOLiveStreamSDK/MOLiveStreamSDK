#ifndef __LIVE_STREAM_FFMPEG_H__
#define __LIVE_STREAM_FFMPEG_H__

#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/time.h>

//record
#include "LiveStreamRecord.h"

extern "C"
{
#ifdef __cplusplus
  //C99整数范围常量. [纯C程序可以不用, 而C++程序必须定义该宏.]
  #define  __STDC_LIMIT_MACROS
  //C99整数常量宏. [纯C程序可以不用, 而C++程序必须定义该宏.]
  #define  __STDC_CONSTANT_MACROS
  // for int64_t print using PRId64 format.
  #define __STDC_FORMAT_MACROS
    #ifdef _STDINT_H
      #undef _STDINT_H
    #endif
  #include <stdint.h>
#endif
}

#ifdef __cplusplus
extern "C"
{
#endif
#include "libavutil/opt.h"
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libavutil/avutil.h"
#include "include/libswscale/swscale.h"
#include "version.h"
#ifdef __cplusplus
}
#endif

enum {
		LiveStreamBitrate512 = 512000,
		LiveStreamBitrate384 = 384000,
		LiveStreamBitrate256 = 256000,
		LiveStreamBitrate192 = 192000,
		LiveStreamBitrate92  = 92000
};

class LiveStreamFFmpeg
{
public:
	LiveStreamFFmpeg();
	~LiveStreamFFmpeg();

	void Initialize();
	void Destroy();

	int setServerUrl(const char* url);
	int setVideoOption(int width, int height, int bitrate, int fps);
	int setAudioOption(int sample_rate);

	int startSession();
	int stopSession();

	int OnCaputureVideo(unsigned char * data, unsigned int size, int in_width, int in_height, long ts, int is_front);
	int OnCaputureAudio(unsigned char * data, unsigned int size, long ts);

protected:
private:
	
	void CloseVideoCode();
	void CloseAudioCode();

	AVPacket m_video_pkt;
	AVPacket m_audio_pkt;

	AVFormatContext *m_av_ctx;

	AVStream *m_audio_st;
	AVStream *m_video_st;

	double m_audio_pts;
	double m_video_pts;

	AVCodec *m_video_codec;
	AVCodec *m_audio_codec;

	AVCodecContext *m_video_ctx;
	AVCodecContext *m_audio_ctx;

	uint8_t *m_video_outbuf;
	uint8_t *m_audio_outbuf;

	uint8_t *m_audio_frame_buf;

	int m_video_outbuf_size;
	int m_audio_outbuf_size;
	int m_audio_input_frame_size;

	struct AVFrame * m_video_frame;
	struct AVFrame * m_audio_frame;

	int m_video_width;            //<< width
	int m_video_height;           //<< height
	int m_video_bitrate;          //<< bitrate
	int m_video_fps;              //<< fps
	int m_audio_sample_rate;      //<< sample rate

	int m_audio_data_size;

	int m_is_video_ready;          //<< video codec ready
	int m_is_audio_ready;          //<< audio codec ready
	int m_is_session_start;        //<< live session state

	pthread_mutex_t m_video_lock;  //<< video lock
	pthread_mutex_t m_audio_lock;  //<< audio lock
	pthread_mutex_t m_write_lock;  //<< write lock

	long long  m_lStartLiveTimeStamp;     //<< start live timestamp
	
	LiveStreamRecord *m_Recorder;
	
};

#endif