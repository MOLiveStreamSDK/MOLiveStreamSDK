LOCAL_PATH :=$(call my-dir)

X264_INC := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
X264_LIB := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/lib
FAAC_INC := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
FAAC_LIB := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/lib
FFMP_INC := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
FFMP_LIB := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/lib

include $(CLEAR_VARS)

LOCAL_MODULE := MOLiveStreamSDK

LOCAL_SRC_FILES := \
		mo_livestream_rtmplive_MOLiveStreamSDK.cpp \
		LiveStreamFFmpeg.cpp \

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH) \
	$(LOCAL_PATH)/include/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI) \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavutil/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavcodec/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavformat/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavfilter/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavresample \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libswscale/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libpostproc \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libswresample/  
	
LOCAL_CFLAGS += -I$(FAAC_INC)
LOCAL_CFLAGS += -I$(X264_INC)
LOCAL_CFLAGS += -I$(FFMP_INC)

LOCAL_DISABLE_FATAL_LINKER_WARNINGS=true

# We enable fatal linker warnings by default.
# If LOCAL_DISABLE_FATAL_LINKER_WARNINGS is true, we don't enable this check.
ifneq ($(LOCAL_DISABLE_FATAL_LINKER_WARNINGS),true)
  LOCAL_LDFLAGS += -Wl,--fatal-warnings
endif

LOCAL_LDLIBS := -llog -lGLESv2 -lz
LOCAL_LDLIBS += -L$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/lib -lavformat -lavcodec -lpostproc -lavfilter -lavresample -lavutil -lswscale -lswresample -lfaac -lx264

include $(BUILD_SHARED_LIBRARY)
