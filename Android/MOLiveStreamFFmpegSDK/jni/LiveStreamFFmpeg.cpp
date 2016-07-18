
#include "LiveStreamFFmpeg.h"
#include "libyuvhelper.h"

#include <string.h>
#include <time.h>
#include <android/log.h>

#define LOGD_PUSH(...) __android_log_print(ANDROID_LOG_DEBUG, "LiveStreamFFmpegPush", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "LiveStreamFFmpeg", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "LiveStreamFFmpeg", __VA_ARGS__)

#define  MAX_RTMP_URL_LEN  256

char rtmp_url[MAX_RTMP_URL_LEN]={0};

int debug = 0;

int g_dst_width = 640;
int g_dst_height= 480;

LiveStreamFFmpeg *pthis;

static int g_total_send_bytes = 0; //<< total send bytes per second
static long long g_send_ts_0 = 0;  //<< start count ts ms
static long long g_send_ts_1 = 0;  //<< current count ts ms

void my_log_callback(void *ptr, int level, const char *fmt, va_list vargs) {
	LOGD(fmt, vargs);
}

static long long LiveGetTimeStamp()
{
	//获取当前时间戳
	struct timeval tv;
	gettimeofday(&tv, NULL);
	long long curren_ts = (tv.tv_sec * 1000) + (tv.tv_usec / 1000);

	return curren_ts;
}

LiveStreamFFmpeg::LiveStreamFFmpeg()
{
	pthis = this;
	
	//ffmpeg
	m_av_ctx = NULL;

	m_audio_st = NULL;
	m_video_st = NULL;

	m_audio_pts = 0;
	m_video_pts = 0;

	m_video_codec = NULL;
	m_audio_codec = NULL;

	m_video_ctx = NULL;
	m_audio_ctx = NULL;

	m_video_outbuf = NULL;
	m_audio_outbuf = NULL;

	m_audio_frame_buf = NULL;

	m_video_outbuf_size = 0;
	m_audio_outbuf_size = 0;
	m_audio_input_frame_size = 0;
	m_audio_data_size = 0;

	m_video_frame = NULL;
	m_audio_frame = NULL;

	m_is_video_ready = 0;
	m_is_audio_ready = 0;
	m_is_session_start = 0;
		
	m_video_fps = 0;
	m_video_bitrate = 0;
	
	m_lStartLiveTimeStamp = 0;

//media codec lock
	pthread_mutex_init(&m_video_lock,NULL);
	pthread_mutex_init(&m_audio_lock,NULL);
	pthread_mutex_init(&m_write_lock,NULL);

}
LiveStreamFFmpeg::~LiveStreamFFmpeg()
{

}

void LiveStreamFFmpeg::Initialize()
{
	int ret = 0;
	if (debug != 0) {
		av_log_set_level(AV_LOG_ERROR);
		av_log_set_callback(my_log_callback);
	}
	av_register_all();
	avformat_network_init();

	
	return ;
}
void LiveStreamFFmpeg::Destroy()
{
	stopSession();

//media codec lock
	pthread_mutex_destroy(&m_video_lock);
	pthread_mutex_destroy(&m_audio_lock);
	pthread_mutex_destroy(&m_write_lock);

	return ;
}

int LiveStreamFFmpeg::setServerUrl(const char* url)
{
	memset(rtmp_url,'\0',MAX_RTMP_URL_LEN);

	memcpy(rtmp_url,url,strlen(url));

	if (m_av_ctx!=NULL)
	{
		avio_close(m_av_ctx->pb);
		avformat_free_context(m_av_ctx);
	}

	int ret = avformat_alloc_output_context2(&m_av_ctx, NULL, "flv", rtmp_url);
	if( ret < 0){ 
		LOGD("avformat_alloc_output_context2 failed:%d, url:%s\n", ret,rtmp_url);
		return -1; 
	}
}

int LiveStreamFFmpeg::setVideoOption(int width, int height,int bitrate, int fps)
{
	if (!m_av_ctx)
	{
		LOGD("oformatctx is null, you must set server url first\n");
		return -1;
	}
	
	CloseVideoCode();

//lock
	pthread_mutex_lock(&m_video_lock);
	
	m_video_fps = fps;
	m_video_bitrate = bitrate;

	g_dst_width = width;
	g_dst_height = height;

	if(m_video_codec == NULL)
		m_video_codec = avcodec_find_encoder(AV_CODEC_ID_H264);

	if (m_video_codec != NULL){
		m_av_ctx->oformat->video_codec = AV_CODEC_ID_H264;
	} else {
		pthread_mutex_unlock(&m_video_lock);
		return -2;
	}
	
	AVRational videoRate = av_d2q(fps, 1001000);

	if(m_video_st == NULL)
	{
		m_video_st = avformat_new_stream(m_av_ctx, m_video_codec);

		if (m_video_st == NULL) 
		{
			pthread_mutex_unlock(&m_video_lock);
			return -4;
		}
	}

	m_video_ctx = m_video_st->codec;
	m_video_ctx->codec_id = AV_CODEC_ID_H264;
	m_video_ctx->codec_type = AVMEDIA_TYPE_VIDEO;
	//bitrate control
	m_video_ctx->bit_rate = bitrate;
	m_video_ctx->rc_max_rate = bitrate;
	m_video_ctx->rc_min_rate = bitrate;
	m_video_ctx->rc_buffer_size = bitrate/2;
    m_video_ctx->bit_rate_tolerance = bitrate;
    m_video_ctx->rc_initial_buffer_occupancy = m_video_ctx->rc_buffer_size*3/4;
    m_video_ctx->rc_buffer_aggressivity= (float)1.0;
    m_video_ctx->rc_initial_cplx= 0.5; 
	
	//align width
	m_video_ctx->width = (width + 15) / 16 * 16;
	m_video_ctx->height = height;

	m_video_ctx->time_base = av_inv_q(videoRate);
	m_video_st->time_base = av_inv_q(videoRate);
	m_video_ctx->gop_size = fps;
	m_video_ctx->pix_fmt = AV_PIX_FMT_YUV420P;
	m_video_ctx->delay = 0;
	m_video_ctx->max_b_frames = 0;

	if ((m_av_ctx->oformat->flags & AVFMT_GLOBALHEADER) != 0) {
		m_video_ctx->flags = m_video_ctx->flags | CODEC_FLAG_GLOBAL_HEADER;
	}

	if ((m_video_codec->capabilities & CODEC_CAP_EXPERIMENTAL) != 0) {
		m_video_ctx->strict_std_compliance = FF_COMPLIANCE_EXPERIMENTAL;
	}

	AVDictionary *options = NULL;
	av_dict_set(&options, "profile", "main", 0);
	av_dict_set(&options, "preset", "superfast", 0);
	av_dict_set(&options, "tune", "zerolatency", 0);

	if (avcodec_open2(m_video_ctx, m_video_codec, &options) < 0) {
		pthread_mutex_unlock(&m_video_lock);
		return -5;
	}

	av_dict_free(&options);

	if(m_video_outbuf != NULL)
	{
		av_free(m_video_outbuf);
		m_video_outbuf = NULL;
	}

	if ((m_av_ctx->oformat->flags & AVFMT_RAWPICTURE) == 0) 
	{
		m_video_outbuf_size = avpicture_get_size(m_video_ctx->pix_fmt, 
												m_video_ctx->width,
												m_video_ctx->height);

		m_video_outbuf = (uint8_t*)av_malloc(m_video_outbuf_size);
	}

	if(m_video_frame == NULL)
	{
		m_video_frame = av_frame_alloc();

		if (m_video_frame == NULL)
		{
			pthread_mutex_unlock(&m_video_lock);
			return -6;
		}

		m_video_frame->pts = 0;
	}

	AVDictionary *metadata1 = NULL;
	m_video_st->metadata = metadata1;

	m_is_video_ready = 1;

//unlock
	pthread_mutex_unlock(&m_video_lock);

	return 0;
}

int LiveStreamFFmpeg::setAudioOption(int sample_rate)
{
	CloseAudioCode();

//lock
	pthread_mutex_lock(&m_audio_lock);

	m_audio_codec = avcodec_find_encoder(AV_CODEC_ID_AAC);
	if (m_audio_codec != NULL) {
		m_av_ctx->oformat->audio_codec = AV_CODEC_ID_AAC;
	} else {
		pthread_mutex_unlock(&m_audio_lock);
		return -3;
	}
	
	if ((m_audio_st = avformat_new_stream(m_av_ctx, 0)) == NULL) {
		pthread_mutex_unlock(&m_audio_lock);
		return -5;
	}

	m_audio_ctx = m_audio_st->codec;
	m_audio_ctx->codec_id = m_av_ctx->oformat->audio_codec;
	m_audio_ctx->codec_type = AVMEDIA_TYPE_AUDIO;
	m_audio_ctx->bit_rate = 32000;
	m_audio_ctx->sample_rate = sample_rate;

	m_audio_ctx->time_base.num = 1;
	m_audio_ctx->time_base.den = sample_rate;
	m_audio_st->time_base.num = 1;
	m_audio_st->time_base.den = sample_rate;
	m_audio_ctx->channels = 1;
	m_audio_ctx->channel_layout = av_get_default_channel_layout(m_audio_ctx->channels);

	m_audio_ctx->sample_fmt = AV_SAMPLE_FMT_S16;

	m_audio_ctx->bits_per_raw_sample = 16;
	if ((m_av_ctx->oformat->flags & AVFMT_GLOBALHEADER) != 0) {
		m_audio_ctx->flags = m_audio_ctx->flags | CODEC_FLAG_GLOBAL_HEADER;
	}
	if ((m_audio_codec->capabilities & CODEC_CAP_EXPERIMENTAL) != 0) {
		m_audio_ctx->strict_std_compliance = FF_COMPLIANCE_EXPERIMENTAL;
	}

	AVDictionary *options = NULL;
	av_dict_set(&options, "crf", "0", 0);
	int ret = avcodec_open2(m_audio_ctx, m_audio_codec, &options);
	if ( ret < 0) {
		pthread_mutex_unlock(&m_audio_lock);
		return -8;
	}
	av_dict_free(&options);

	m_audio_outbuf_size = 256 * 1024;
	m_audio_outbuf = (uint8_t *)av_malloc(m_audio_outbuf_size);
	
	if (m_audio_ctx->frame_size <= 1) {

		m_audio_outbuf_size = FF_MIN_BUFFER_SIZE;
		m_audio_input_frame_size = m_audio_outbuf_size / m_audio_ctx->channels;
		if (m_audio_ctx->codec_id == AV_CODEC_ID_PCM_U16BE) {
			m_audio_input_frame_size >>= 1;
		}
	} else {
		m_audio_input_frame_size = m_audio_ctx->frame_size;
	}

	int planes = 1;
	m_audio_data_size = av_samples_get_buffer_size(NULL, m_audio_ctx->channels,
		m_audio_input_frame_size, m_audio_ctx->sample_fmt, 1) / planes;

	m_audio_frame = av_frame_alloc();
	m_audio_frame->pts = 0;
	m_audio_frame->nb_samples = m_audio_input_frame_size;
	m_audio_frame->format = m_audio_ctx->sample_fmt;

	m_audio_frame_buf = (uint8_t *) av_malloc(m_audio_data_size);
	
	ret = avcodec_fill_audio_frame(m_audio_frame, m_audio_ctx->channels,
		m_audio_ctx->sample_fmt, (const uint8_t*) m_audio_frame_buf, m_audio_data_size, 0);

	if (ret < 0) {
		pthread_mutex_unlock(&m_audio_lock);
		return -2;
	}

	av_init_packet(&m_audio_pkt);
	AVDictionary *metadata2 = NULL;
	m_audio_st->metadata = metadata2;

	m_is_audio_ready = 1;

//unlock
	pthread_mutex_unlock(&m_audio_lock);

	return 0;
}

int LiveStreamFFmpeg::startSession()
{
	if(m_av_ctx==NULL)
	{
		LOGD("you must set server url fisrt");
		return -22;
	}
	
	//AVDictionary *options = NULL;
	//av_dict_set(&options, "rtmp_app", "tuyooc", 0);
	//av_dict_set(&options, "rtmp_buffer", "1000", 0);
	
	// open file. 
	int ret = avio_open2(&m_av_ctx->pb, rtmp_url, AVIO_FLAG_READ_WRITE | AVIO_FLAG_NONBLOCK , &m_av_ctx->interrupt_callback, NULL);
	
	//av_dict_free(&options);
	
	strcpy(m_av_ctx->filename, rtmp_url);

	if(ret < 0){ 
		LOGD("avio_open2 failed:%d,url:%s\n", ret,rtmp_url);
		return -1; 
	} 
	
	m_av_ctx->max_interleave_delta = 1000000/2;

	ret = avformat_write_header(m_av_ctx, NULL);

	if (ret < 0) {
		LOGD("avformat_write_header failed %d", ret);
		return -3;
	}

	//set start live ts
	m_lStartLiveTimeStamp = LiveGetTimeStamp();

	m_is_session_start = 1;

	return 0;
}
int LiveStreamFFmpeg::stopSession()
{	
	m_is_session_start = 0;
	
	usleep(500);

	CloseVideoCode();

	CloseAudioCode();

	if (m_av_ctx!=NULL)
	{
		if(m_av_ctx->pb!=NULL)
		{
			avio_close(m_av_ctx->pb);
			m_av_ctx->pb = NULL;
		}

		avformat_free_context(m_av_ctx);
		m_av_ctx = NULL;
	}
	
	return 0;
}

void LiveStreamFFmpeg::CloseVideoCode()
{
//lock
	pthread_mutex_lock(&m_video_lock);

	if (m_video_ctx!=NULL)
	{
		avcodec_close(m_video_ctx);
		m_video_ctx = NULL;
	}
	
	if(m_video_outbuf != NULL)
	{
		av_free(m_video_outbuf);
		m_video_outbuf = NULL;
		m_video_outbuf_size = 0;
	}

	m_is_video_ready = 0;
//unlock
	pthread_mutex_unlock(&m_video_lock);
}

void LiveStreamFFmpeg::CloseAudioCode()
{
//lock
	pthread_mutex_lock(&m_audio_lock);

	if (m_audio_ctx!=NULL)
	{
		avcodec_close(m_audio_ctx);
		m_audio_ctx = NULL;
	}

	m_is_audio_ready = 0;

//unlock
	pthread_mutex_unlock(&m_audio_lock);
}

int LiveStreamFFmpeg::OnCaputureVideo(unsigned char * data, unsigned int len, int in_width, int in_height, long ts, int is_front)
{
	if (m_is_video_ready == 0 || m_is_session_start == 0)
		return -1;
	
	pthread_mutex_lock(&m_video_lock);

	int ret              = 0;
	int got_video_packet = 0;
	int rotate_width     = in_height;
	int rotate_height    = in_width;
	int dst_width        = g_dst_width;
	int dst_height       = g_dst_height;
	int yuv_size         = in_width * in_height * 3 / 2;
	uint8_t* yuv_i420    = (uint8_t *) malloc(yuv_size);
	uint8_t* yuv_i420_90 = (uint8_t *) malloc(yuv_size);

	CLibYUVHelper::ConvertOther2YUV420P(in_width, in_height, (unsigned char*)data, MOLIVE_PIX_FMT_NV21,(unsigned char*)yuv_i420);

	if(is_front)
		CLibYUVHelper::RotateYUV420PFrame(in_width, in_height, (unsigned char*)yuv_i420, (unsigned char*)yuv_i420_90, -90);
	else
		CLibYUVHelper::RotateYUV420PFrame(in_width, in_height, (unsigned char*)yuv_i420, (unsigned char*)yuv_i420_90, 90);

	uint8_t* yuv_i420_scale = (uint8_t *) malloc(g_dst_width * g_dst_height * 3 / 2);
	

	CLibYUVHelper::Scale(rotate_width, rotate_height,yuv_i420_90, 
					  dst_width,  dst_height,yuv_i420_scale, 
					  MOLIVE_PIX_FMT_YUV420P, 1);
	

	ret = avpicture_fill((AVPicture *) m_video_frame, yuv_i420_scale, AV_PIX_FMT_YUV420P, dst_width,dst_height);
	if (ret < 0) {
		pthread_mutex_unlock(&m_video_lock);
		return -2;
	}

	av_init_packet(&m_video_pkt);
	m_video_pkt.data = m_video_outbuf;
	m_video_pkt.size = m_video_outbuf_size;

	ret = avcodec_encode_video2(m_video_ctx, &m_video_pkt, m_video_frame, &got_video_packet);
	if (ret < 0) {
		LOGD("video encode error: %d, video size: %d", ret, m_video_pkt.size);
		//unlock
		pthread_mutex_unlock(&m_video_lock);
		return -11;
	}

	m_video_frame->pts = m_video_frame->pts + 1;

	if (got_video_packet == 0) {
		pthread_mutex_unlock(&m_video_lock);
		return -12;
	}
	
	if (m_video_pkt.pts != AV_NOPTS_VALUE) {
		m_video_pkt.pts = (LiveGetTimeStamp() - m_lStartLiveTimeStamp);
	}

	if (m_video_pkt.dts != AV_NOPTS_VALUE) {
		m_video_pkt.dts = m_video_pkt.pts;
	}

	m_video_pkt.flags = m_video_pkt.flags | AV_PKT_FLAG_KEY;
	m_video_pkt.stream_index = m_video_st->index;
	m_video_pkt.duration = 1000/15;
	
//lock
	pthread_mutex_lock(&m_write_lock);		
	ret = av_write_frame(m_av_ctx, &m_video_pkt);
	pthread_mutex_unlock(&m_write_lock);

	if ( ret!= 0) {
		char errbuf[1024] = {0};
		av_strerror(ret, errbuf, sizeof(errbuf));
		LOGD("video send error: %d %s", ret,errbuf);	
	}
	
	// debug send bytes per second
	if (g_send_ts_0 == 0)
		g_send_ts_0 = LiveGetTimeStamp();
	
	//current time and total bytes
	g_send_ts_1 = LiveGetTimeStamp();
	g_total_send_bytes += m_video_pkt.size;

	// spend times
	long long current_diff = g_send_ts_1 - g_send_ts_0;
	
	av_free_packet(&m_video_pkt);

	free(yuv_i420_scale);
	free(yuv_i420);
	free(yuv_i420_90);

	//unlock
	pthread_mutex_unlock(&m_video_lock);

	//adjust the bitrate
	if ( current_diff >= 1000)
	{
		bool is_bitrate_change = true;
		int bytes_per_second = g_total_send_bytes * 8 / 1024 * 1000 / current_diff;
		LOGD("send video per second :%d kBps [%d bytes %lld ms]", bytes_per_second, g_total_send_bytes, current_diff);	
		
		if ( bytes_per_second >500 && m_video_bitrate!=LiveStreamBitrate512)                                // bitrate > 500 ,      use 512 kbps
			m_video_bitrate = LiveStreamBitrate512;
		else if ( bytes_per_second <300 && bytes_per_second >200 && m_video_bitrate!=LiveStreamBitrate256)  // 200 < bitrate < 300 , use 256 kbps
			m_video_bitrate = LiveStreamBitrate256;
		else if ( bytes_per_second <200 && bytes_per_second >150 && m_video_bitrate!=LiveStreamBitrate192)  // 150 < bitrate < 200 , use 192 kbps
			m_video_bitrate = LiveStreamBitrate192;
		else if ( bytes_per_second <100 && m_video_bitrate!=LiveStreamBitrate92)                            // bitrate < 100 ,       use 92 kbps
			m_video_bitrate = LiveStreamBitrate92;
		else
			is_bitrate_change = false;
		
		if(is_bitrate_change && 
			g_dst_width>0 && 
			g_dst_height>0 &&
			m_video_bitrate>0 &&
			m_video_fps>0)
		{
			setVideoOption(g_dst_width, g_dst_height, m_video_bitrate, m_video_fps);
			LOGD("change video option:birate:%d, fps:%d, WxH:%d_%d",m_video_bitrate,m_video_fps,g_dst_width,g_dst_height);
		}

		g_send_ts_0 = 0;
		g_send_ts_1 = 0;
		g_total_send_bytes = 0;
	}
	
	return 0;
}

int LiveStreamFFmpeg::OnCaputureAudio(unsigned char * data, unsigned int len, long ts)
{
	int ret              = 0;
	int got_audio_packet = 0;
	
	if (m_is_audio_ready == 0 || m_is_session_start==0) 
		return -1;
	
	pthread_mutex_lock(&m_audio_lock);

	m_audio_frame->data[0] = (uint8_t *) data;
	m_audio_frame->linesize[0] = len;
	m_audio_frame->quality = m_audio_ctx->global_quality;

	ret = avcodec_encode_audio2(m_audio_ctx, &m_audio_pkt, m_audio_frame,
		&got_audio_packet);
	if (ret < 0) {
		char strlog[1024];
		av_strerror(ret, strlog, sizeof(strlog));
		LOGD("audio encode error :%d %s", ret,strlog);

		pthread_mutex_unlock(&m_audio_lock);

		return -2;
	}

	m_audio_frame->pts = m_audio_frame->pts + m_audio_frame->nb_samples;

	if (got_audio_packet < 1) {
		pthread_mutex_unlock(&m_audio_lock);
		return -11;
	}

	if (m_audio_pkt.pts != AV_NOPTS_VALUE) {
		m_audio_pkt.pts = (LiveGetTimeStamp() - m_lStartLiveTimeStamp);;
	}
	if (m_audio_pkt.dts != AV_NOPTS_VALUE) {
		m_audio_pkt.dts =m_audio_pkt.pts;
	}

	m_audio_pkt.flags = m_audio_pkt.flags | AV_PKT_FLAG_KEY;
	m_audio_pkt.stream_index = m_audio_st->index;
	m_audio_pkt.duration = 1024 * 1000/ 44100;
	
	pthread_mutex_lock(&m_write_lock);
	ret = av_write_frame(m_av_ctx, &m_audio_pkt);
	pthread_mutex_unlock(&m_write_lock);

	if (ret < 0) {
		char strlog[1024];
		av_strerror(ret, strlog, sizeof(strlog));
		LOGD("send audio error: %d,%s",ret, strlog);
	}

	av_free_packet(&m_audio_pkt);

//unlock
	pthread_mutex_unlock(&m_audio_lock);

	return 0;
}
