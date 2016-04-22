//
//  MOLiveStreamSDK.h
//  MOLiveStreamSDK
//
//  Created by MOLiveStreamSDK on 16/3/12.
//  Copyright © 2016年 MOLiveStreamSDK. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

@interface MOLiveStreamSDK : NSObject

-(id) init;

/*
 *  设置服务器地址 
 *  (set server url)
 */
-(int) SetServerUrl:(NSString*) server_url;

/*
 *  设置视频参数，主要是采集和编码参数 
 *  (set video param)
 */
-(int) SetVideoOption:(int) width andHeitht:(int) height;

/*
 *  设置音频参数，主要是采集和编码参数 
 *  (set audio param)
 */
-(int) SetAudioOption:(int) sample_rate andChannel:(int) channel;

/*
 *  设置摄像头参数，主要是显示区域 
 *  (set camera view)
 */
-(int) SetCameraView:(UIView*) camera_view;

/*
 *  连接服务器 
 *  (connect to server)
 */
-(int) ConnectToServer;

/*
 *  断开服务器
 *  (disconnect)
 */
-(int) Disconnect;

/*
 *  开启摄像头预览
 *  (start preview)
 */
-(int) StartPreview;

/*
 *  停止摄像预览
 *  (stop preview)
 */
-(int) StopPreview;

/*
 *  开始直播媒体
 *  (start media live)
 */
-(int) StartLiveVideo:(int) enableVideo andAudio:(int) enableAudio;

/*
 *  停止直播媒体
 *  (stop media live)
 */
-(int) StopLive;

/*
 *  切换摄像头
 *  (switch camera position)
 */
-(void) SwitchCamera;

/*
 *  闪光灯
 *  (switch camera flash light)
 */
-(void) SwitchFlash;

/**
 *  当前摄像头
 *  (get current camera face, no use now)
 */
-(int) getCurrenFace;
@end
