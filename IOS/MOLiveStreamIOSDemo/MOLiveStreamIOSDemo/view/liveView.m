//
//  liveView.m
//  RtmpLiveIOS
//
//  Created by 陆永亨 on 16/3/25.
//  Copyright © 2016年 luwinglee. All rights reserved.
//

#import "liveView.h"
#import "UIView+Frame.h"

#define RGB(redValue,greenValue,blueValue) [UIColor colorWithRed:redValue/255.0 green:greenValue/255.0 blue:blueValue/255.0 alpha:1.0]

#define NAVIGATION_BAR_HEIGHT ((IS_IOS_7) ? 64 : 44)
#define UIDEVICEMODEL [UIDevice currentDevice].model;
#define TAB_BAR_HEIGHT 49

#define SCREEN_WIDTH ([UIScreen mainScreen].bounds.size.width)
#define SCREEN_HEIGHT ([UIScreen mainScreen].bounds.size.height)
@interface liveView()<UITextFieldDelegate>

@end
@implementation liveView


-(instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    
    if (self) {
        
        self.backgroundColor = RGB(200, 200, 200);
        
        [self addSubview:self.backView];
        
        
        [self.backView addSubview:self.cancelBtn];
        [self.backView addSubview:self.changeCameraBtn];
        [self.backView addSubview:self.startLiveBtn];
        [self.backView addSubview:self.preViewBtn];
        [self.backView addSubview:self.liveUrlTextField];
        
        [self setViewFrame];
    }
    
    return self;
}

-(void)setViewFrame
{
    self.backView.frame = self.bounds;
    
    self.cancelBtn.frame = CGRectMake(SCREEN_WIDTH-50, 20, self.cancelBtn.width, self.cancelBtn.height);
    
    self.changeCameraBtn.frame = CGRectMake(SCREEN_WIDTH-self.cancelBtn.width-90, self.cancelBtn.y, self.changeCameraBtn.width, self.changeCameraBtn.height);
    
    self.liveUrlTextField.frame = CGRectMake(20, self.changeCameraBtn.bottom+320, SCREEN_WIDTH-40, 40);
    self.bottomLayer.frame = CGRectMake(0, self.liveUrlTextField.height-1, self.liveUrlTextField.width, 1);
    
    self.startLiveBtn.frame = CGRectMake(10, self.liveUrlTextField.bottom+30, SCREEN_WIDTH-20, 40);
    
    self.preViewBtn.frame = CGRectMake(10, self.startLiveBtn.bottom+10, SCREEN_WIDTH-20, 40);
}

#pragma mark - 懒加载
-(UIButton *)cancelBtn
{
    if (!_cancelBtn) {
        _cancelBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        UIImage * image = [UIImage imageNamed:@"btn_close"];
        UIImage * seletedImage = [UIImage imageNamed:@"btn_close_press"];
        [_cancelBtn setImage:image forState:UIControlStateNormal];
        [_cancelBtn setImage:seletedImage forState:UIControlStateSelected];
        
        CGRect changeCameraFrame = CGRectMake(0, 0, image.size.width, image.size.height);
        
        _cancelBtn.frame     =   changeCameraFrame;
        [_cancelBtn addTarget:self action:@selector(clickCancelBtn:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _cancelBtn;
}

-(UIButton *)changeCameraBtn
{
    if (!_changeCameraBtn) {
        _changeCameraBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        UIImage * image = [UIImage imageNamed:@"btn_camera"];
        UIImage * seletedImage = [UIImage imageNamed:@"btn_camera_press"];
        [_changeCameraBtn setImage:image forState:UIControlStateNormal];
        [_changeCameraBtn setImage:seletedImage forState:UIControlStateSelected];
        
        CGRect changeCameraFrame = CGRectMake(0, 0, image.size.width, image.size.height);
        
        _changeCameraBtn.frame     =   changeCameraFrame;
        
        [_changeCameraBtn addTarget:self action:@selector(clickChangeCameraBtn:) forControlEvents:UIControlEventTouchUpInside];
        
    }
    return _changeCameraBtn;

}


-(UIButton *)startLiveBtn
{
    if (!_startLiveBtn) {
        _startLiveBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        
        //设置边框 为 1   。  白色 。  圆角  为 10.
        _startLiveBtn.layer.borderColor = [UIColor whiteColor].CGColor;
        _startLiveBtn.layer.borderWidth = 1/[UIScreen mainScreen].scale;
        _startLiveBtn.layer.cornerRadius = 10;
        _startLiveBtn.layer.masksToBounds=YES;
        
        //设置背景颜色 透明
        _startLiveBtn.backgroundColor = [UIColor clearColor];
        
        [_startLiveBtn setTitle:@"开始直播" forState:UIControlStateNormal];
        [_startLiveBtn setTitle:@"关闭直播" forState:UIControlStateSelected];
        //设置字体 大小为 14号
        _startLiveBtn.titleLabel.font = [UIFont systemFontOfSize:14];
        [_startLiveBtn addTarget:self action:@selector(clickStartLiveBtn:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _startLiveBtn;
}

-(UITextField *)liveUrlTextField
{
    if (!_liveUrlTextField) {
        _liveUrlTextField = [[UITextField alloc]init];
        //设置UITextField的文字颜色
        _liveUrlTextField.textColor=[UIColor whiteColor];
        
        //设置UITextField的文本框背景颜色
        _liveUrlTextField.backgroundColor=[UIColor clearColor];
        
        _liveUrlTextField.returnKeyType = UIReturnKeyGo;
       
        _liveUrlTextField.delegate = self;

        _liveUrlTextField.autoresizingMask = UIViewAutoresizingFlexibleHeight;//自适应高度
        
        [_liveUrlTextField setText:@"rtmp://192.168.0.104/live/ios"];
        
        _bottomLayer = [CALayer layer];
        _bottomLayer.backgroundColor = [UIColor whiteColor].CGColor;
        [_liveUrlTextField.layer addSublayer:_bottomLayer];
        
    }
    return _liveUrlTextField;
}

-(UIView *)backView
{
    if (!_backView) {
        _backView = [[UIView alloc]init];
        _backView.backgroundColor = [UIColor clearColor];
    }
    return _backView;
}

-(UIButton *)preViewBtn
{
    if (!_preViewBtn) {
        _preViewBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        
        //设置边框 为 1   。  白色 。  圆角  为 10.
        _preViewBtn.layer.borderColor = [UIColor whiteColor].CGColor;
        _preViewBtn.layer.borderWidth = 1/[UIScreen mainScreen].scale;
        _preViewBtn.layer.cornerRadius = 10;
        _preViewBtn.layer.masksToBounds=YES;
        
        //设置背景颜色 透明
        _preViewBtn.backgroundColor = [UIColor clearColor];
        _preViewBtn.selected = YES;
        [_preViewBtn setTitle:@"打开预览" forState:UIControlStateNormal];
        [_preViewBtn setTitle:@"关闭预览" forState:UIControlStateSelected];
        //设置字体 大小为 14号
        _preViewBtn.titleLabel.font = [UIFont systemFontOfSize:14];
        [_preViewBtn addTarget:self action:@selector(clickPreViewBtnBtn:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _preViewBtn;
}

#pragma mark - 点击事件
-(void)clickCancelBtn:(UIButton*)btn
{
    _liveClickType = cancel;
    if (self.handleClickBlock) {
        self.handleClickBlock(btn,_liveClickType);
    }
}

-(void)clickChangeCameraBtn:(UIButton*)btn
{
      _liveClickType = changeCamera;
    if (self.handleClickBlock) {
         self.handleClickBlock(btn,_liveClickType);
    }
}

-(void)clickStartLiveBtn:(UIButton*)btn
{
       _liveClickType = start;
    if (self.handleClickBlock) {
        self.handleClickBlock(btn,_liveClickType);
    }
}


-(void)clickPrivacyLiveBtn:(UIButton*)btn
{
     _liveClickType = livePrivacy;
    btn.layer.borderColor = RGB(83, 178, 69).CGColor;
  
    _pubilcLiveBtn.layer.borderColor = RGB(200, 200, 200).CGColor;
    
    
    if (self.handleClickBlock) {
         self.handleClickBlock(btn,_liveClickType);
    }
    
    
}

-(void)clickPubilcLiveBtn:(UIButton*)btn
{
    _liveClickType = livePublic;
        _privacyLiveBtn.layer.borderColor = RGB(200, 200, 200).CGColor;
   
        btn.layer.borderColor = RGB(83, 178, 69).CGColor;
    
    
    if (self.handleClickBlock) {
        self.handleClickBlock(btn,_liveClickType);
    }
}


- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    if (self.handleLiveUrlBlock) {
        self.handleLiveUrlBlock(textField.text);
    }
   
    return YES;
}

-(void)clickPreViewBtnBtn:(UIButton*)btn
{
     _liveClickType = preView;
    if (self.handleClickBlock) {
        self.handleClickBlock(btn,_liveClickType);
    }

}
@end
