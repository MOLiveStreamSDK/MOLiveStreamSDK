//
//  UIView+Frame.h
//
//  Created by www.poboke.com on 14-6-1.
//  Copyright (c) 2014年 Poboke. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIView (Frame)

- (id)initWithSize:(CGSize)size;

- (CGPoint)origin;
- (CGFloat)x;
- (CGFloat)y;
- (CGFloat)right;
- (CGFloat)bottom;

- (CGSize)size;
- (CGFloat)height;
- (CGFloat)width;

- (void)setSize:(CGSize)size;
- (void)setWidth:(CGFloat)width;
- (void)setHeight:(CGFloat)height;

- (void)setOrigin:(CGPoint)origin;
- (void)setX:(CGFloat)x;
- (void)setY:(CGFloat)y;

- (void)setAnchorPoint:(CGPoint)anchorPoint;
- (void)setPosition:(CGPoint)point atAnchorPoint:(CGPoint)anchorPoint;

@end
