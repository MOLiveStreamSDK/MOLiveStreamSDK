//
//  liveView.h
//  RtmpLiveIOS
//
//  Created by 陆永亨 on 16/3/25.
//  Copyright © 2016年 luwinglee. All rights reserved.
//

#import <UIKit/UIKit.h>
typedef NS_ENUM(NSUInteger, clickType) {
    cancel,
    start,
    livePrivacy,
    livePublic,
    changeCamera,
    preView
};
@interface liveView : UIView

@property(strong ,nonatomic)UIView * backView;

@property (copy ,nonatomic) void (^handleClickBlock)(UIButton*btn,clickType liveClickType);

@property (copy ,nonatomic) void (^handleLiveUrlBlock)(NSString*liveUrl);
@property (strong ,nonatomic) UIButton * cancelBtn;

@property (strong ,nonatomic) UIButton * changeCameraBtn;

@property (strong ,nonatomic) UIButton * pubilcLiveBtn;

@property (strong ,nonatomic) UIButton * privacyLiveBtn;

@property (strong ,nonatomic) UIButton * startLiveBtn;

@property (strong ,nonatomic) UIButton * preViewBtn;

@property (strong ,nonatomic) UITextField * liveUrlTextField;

@property (strong ,nonatomic)CALayer * bottomLayer;

@property (assign ,nonatomic)clickType  liveClickType;
@end
