//
//  ViewController.m
//  RtmpLiveIOS
//
//  Created by MOLiveStreamSDK on 16/3/12.
//  Copyright © 2016年 MOLiveStreamSDK. All rights reserved.
//

#import "ViewController.h"
#import <AVFoundation/AVFoundation.h>
#import "MOLiveStreamSDK.h"
#import "liveView.h"

@interface ViewController ()
{
    UIButton    *m_live_ctrl_btn;    //<< start live contorl
    UIButton    *m_camera_ctrl_btn;  //<< camera control
    UIButton    *m_connect_ctrl_btn; //<< connect control
    UIButton    *m_switch_ctrl_btn;  //<< switch camera control
    UITextField *m_url_text;         //<< server url
    
    UITextField *m_url_tf;
    UIScrollView *m_scrol_view ;
    UIImageView *m_imageView;
}
@property(strong,nonatomic)liveView        *m_liveView;
@property(strong,nonatomic)MOLiveStreamSDK *pMoLiveStreamSDK;

@property(copy,nonatomic)NSString * m_live_url;
@end

@implementation ViewController

-(void)dealloc
{
    [self.pMoLiveStreamSDK StopPreview];
    [self.pMoLiveStreamSDK StopLive];
    [self.pMoLiveStreamSDK Disconnect];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    //set the screen light always
    [[ UIApplication sharedApplication] setIdleTimerDisabled:YES ] ;
    
    [self.view addSubview:self.m_liveView];
    
    // Do any additional setup after loading the view, typically from a nib
    
    [self.view addSubview:self.m_liveView];
    
    int width = 640;
    int height = 480;
    
    _pMoLiveStreamSDK = [[MOLiveStreamSDK alloc] init];          // create livestream manager

    [_pMoLiveStreamSDK SetAudioOption:44100 andChannel:1];       //  set audio param
    [_pMoLiveStreamSDK SetVideoOption:width andHeitht:height];   //  set video param
    
    [_pMoLiveStreamSDK SetCameraView:self.m_liveView];

    
    [self.pMoLiveStreamSDK  StartPreview];                       //  start camera preview
    [self.m_liveView bringSubviewToFront:self.m_liveView.backView];}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}




- (void)quit
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(liveView *)m_liveView
{
    if (!_m_liveView) {
        
        __weak typeof(self) weak_self = self;
        _m_liveView = [[liveView alloc]initWithFrame:self.view.bounds];
        _m_liveView.handleLiveUrlBlock = ^(NSString * liveUrl){
            
            if (weak_self.m_live_url)
                [weak_self.pMoLiveStreamSDK Disconnect];
            
            weak_self.m_live_url = liveUrl;
            [weak_self.pMoLiveStreamSDK SetServerUrl:liveUrl];       // set server url
            [weak_self.pMoLiveStreamSDK ConnectToServer];            // connect to server
            
            weak_self.m_live_url = liveUrl;
            
        };
        
        _m_liveView.handleClickBlock = ^(UIButton * btn,clickType liveClickType)
        {
            btn.selected = !btn.selected;
            
            switch (liveClickType) {
                case cancel:
                {
                    [weak_self quit];
                }
                    break;
                case start:
                {
                    
                    //if (weak_self.m_live_url.length>0)
                    {
                        if (btn.selected) {
                            
                            NSLog(@"start live ...");
                            [weak_self.pMoLiveStreamSDK Disconnect];
                            [weak_self.pMoLiveStreamSDK StopLive];
                            
                            weak_self.m_live_url = weak_self.m_liveView.liveUrlTextField.text;
                            [weak_self.pMoLiveStreamSDK SetServerUrl:weak_self.m_liveView.liveUrlTextField.text];
                            [weak_self.pMoLiveStreamSDK ConnectToServer];            // connect to server
                            
                            [weak_self.pMoLiveStreamSDK StartLiveVideo:1 andAudio:1];// start live
                            [weak_self.m_liveView bringSubviewToFront:weak_self.m_liveView.backView];
                        } else {
                            NSLog(@"stop live !!!");
                            
                            [weak_self.pMoLiveStreamSDK Disconnect];
                            [weak_self.pMoLiveStreamSDK StopLive];
                            
                            
                        }
                    }
                }
                    break;
                case livePrivacy:
                {
                    
                }
                    break;
                case livePublic:
                {
                    
                }
                    break;
                    
                case changeCamera: ///  switch camera
                {
                    
                    [weak_self.pMoLiveStreamSDK SwitchCamera];
                    
                }
                    break;
                case preView:
                {
                    if (btn.selected) {
                        NSLog(@"start preview ...");
                        [weak_self.pMoLiveStreamSDK  StartPreview]; // start preview
                        [weak_self.m_liveView bringSubviewToFront:weak_self.m_liveView.backView];
                    } else {
                        NSLog(@"stop preview !!!");
                        [weak_self.pMoLiveStreamSDK StopPreview];
                    }
                    
                }
                    break;
                default:
                    break;
            }
        };
        
    }
    
    return _m_liveView;
}

- (void)onMediaLive:(UIButton *)button
{
    button.selected = !button.selected;
    
    if (button.selected) {
        
        NSLog(@"start live ...");
        [_pMoLiveStreamSDK StartLiveVideo:1 andAudio:1];// start live
        
    } else {
        
        NSLog(@"stop live !!!");
        [_pMoLiveStreamSDK StopLive];
        
    }
}


- (void)onSwitchCamCtrl:(UIButton *)button
{
    button.selected = !button.selected;
    
    NSLog(@"switch camera ...");
    [_pMoLiveStreamSDK  SwitchCamera]; // switch camera
}

- (void)onPreviewCamera:(UIButton *)button
{
    button.selected = !button.selected;
    
    if (button.selected) {
        NSLog(@"start preview ...");
        [_pMoLiveStreamSDK  StartPreview]; // start preview
    } else {
        NSLog(@"stop preview !!!");
        [_pMoLiveStreamSDK StopPreview];
    }
}

- (void)onConnectCtrl:(UIButton *)button
{
    button.selected = !button.selected;
    
    if (button.selected) {
        NSLog(@"Connect !!!");
        NSString *server_url = [m_url_tf text];
        [_pMoLiveStreamSDK SetServerUrl:server_url];    // set server url
        [_pMoLiveStreamSDK ConnectToServer];            // connect to server
    } else {
        
        NSLog(@"Disconnect !!!");
        [_pMoLiveStreamSDK Disconnect];
        
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self->m_url_tf resignFirstResponder];
    return YES;
}
-(IBAction) textFieldDone:(id) sender
{
    [m_url_tf resignFirstResponder];
}
@end
