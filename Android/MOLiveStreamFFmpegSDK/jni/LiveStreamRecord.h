#ifndef __LIVE_STREAM_RECORD_H__
#define __LIVE_STREAM_RECORD_H__

enum MO_Record_Type{
	MO_TYPE_MP4,
	MO_TYPE_FLV,
	MO_TYPE_AVI
};

class LiveStreamRecord
{
public:
	LiveStreamRecord();
	
	~LiveStreamRecord();
	
	int setWaterMarkOption(int ori_x, 
	                       int ori_y, 
						   int pic_width, 
						   int pic_height,
						   unsigned char* buffer);
	
	int open(const char *file_path);
	
	void close();
	
	int recordPacket(void *packet);
	
	void *m_record_ctx;
};

#endif