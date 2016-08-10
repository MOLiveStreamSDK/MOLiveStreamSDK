#ifndef __LIVE_STREAM_COMMON_H__
#define __LIVE_STREAM_COMMON_H__


/*
*	Return Code
*/

#define MO_STATE_ERR    -1
#define MO_STATE_OK	     0         
#define MO_STATE_SUCCESS 0     

#define MO_VCODEC_ERR   -1002       //<< 创建视频Codec失败
#define MO_ACODEC_ERR   -1003       //<< 创建音频Codec失败
#define MO_SESSION_ERR  -1004       //<< 创建会话失败
#define MO_CONNECT_ERR  -1005       //<< 连接失败
#define MO_SEND_PKT_ERR -1006       //<< 发送失败

/*
*	Record
*/
#define MO_RECORD_TYPE_MP4 2001
#define MO_RECORD_TYPE_FLV 2002
#define MO_RECORD_TYPE_AVI 2003
#define MO_RECORD_ERR     -2001       //<< 录像失败

/*
*	MediaCodecType
*/
#define MO_VIDEO_TYPE_H264 96
#define MO_AUDIO_TYPE_AAC  107       

/*
*	Other
*/
#define MO_WATER_MARK_NONE   1001    //<< 无
#define MO_WATER_MARK_MIRROR 1002    //<< 镜像

#define MO_STREAM_INFO_FPS     3001  //<< 实时帧率
#define MO_STREAM_INFO_BITRATE 3002  //<< 实时码率
#define MO_STREAM_INFO_SPEED   3003  //<< 实时速率
#define MO_STREAM_INFO_AR      3004  //<< 实时分辨率

#endif