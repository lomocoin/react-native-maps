/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AIRMapMarker.h"

#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTImageLoader.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>

@implementation AIREmptyCalloutBackgroundView
@end

@implementation AIRMapMarker {
    BOOL _hasSetCalloutOffset;
    RCTImageLoaderCancellationBlock _reloadImageCancellationBlock;
    MKPinAnnotationView *_pinView;
}

- (void)reactSetFrame:(CGRect)frame
{
    // Make sure we use the image size when available
    CGSize size = self.image ? self.image.size : frame.size;
    CGRect bounds = {CGPointZero, size};
    
    // The MapView is basically in charge of figuring out the center position of the marker view. If the view changed in
    // height though, we need to compensate in such a way that the bottom of the marker stays at the same spot on the
    // map.
    CGFloat dy = (bounds.size.height - self.bounds.size.height) / 2;
    CGPoint center = (CGPoint){ self.center.x, self.center.y - dy };
    
    // Avoid crashes due to nan coords
    if (isnan(center.x) || isnan(center.y) ||
        isnan(bounds.origin.x) || isnan(bounds.origin.y) ||
        isnan(bounds.size.width) || isnan(bounds.size.height)) {
        RCTLogError(@"Invalid layout for (%@)%@. position: %@. bounds: %@",
                    self.reactTag, self, NSStringFromCGPoint(center), NSStringFromCGRect(bounds));
        return;
    }
    
    self.center = center;
    self.bounds = bounds;
}

- (instancetype)initWithFrame:(CGRect)frame{
    if(self = [super initWithFrame:frame]){
        
        self.type = 1;
        
        self.HeaderView = [[UIView alloc] init];
        self.HeaderView.bounds = CGRectMake(-30, -30, 60, 60);
        
        [self addSubview:self.HeaderView];
    }
    return self;
}

- (void)insertReactSubview:(id<RCTComponent>)subview atIndex:(NSInteger)atIndex {
    if ([subview isKindOfClass:[AIRMapCallout class]]) {
        self.calloutView = (AIRMapCallout *)subview;
    } else {
        [super insertReactSubview:(UIView *)subview atIndex:atIndex];
    }
}

- (void)removeReactSubview:(id<RCTComponent>)subview {
    if ([subview isKindOfClass:[AIRMapCallout class]] && self.calloutView == subview) {
        self.calloutView = nil;
    } else {
        [super removeReactSubview:(UIView *)subview];
    }
}

- (MKAnnotationView *)getAnnotationView
{
    if ([self shouldUsePinView]) {
        // In this case, we want to render a platform "default" marker.
        if (_pinView == nil) {
            _pinView = [[MKPinAnnotationView alloc] initWithAnnotation:self reuseIdentifier: nil];
            [self addGestureRecognizerToView:_pinView];
            _pinView.annotation = self;
        }
        
        _pinView.draggable = self.draggable;
        _pinView.layer.zPosition = self.zIndex;
        
        // TODO(lmr): Looks like this API was introduces in iOS 8. We may want to handle differently for earlier
        // versions. Right now it's just leaving it with the default color. People needing the colors are free to
        // use their own custom markers.
        if ([_pinView respondsToSelector:@selector(setPinTintColor:)]) {
            _pinView.pinTintColor = self.pinColor;
        }
        
        return _pinView;
    } else {
        // If it has subviews, it means we are wanting to render a custom marker with arbitrary react views.
        // if it has a non-null image, it means we want to render a custom marker with the image.
        // In either case, we want to return the AIRMapMarker since it is both an MKAnnotation and an
        // MKAnnotationView all at the same time.
        self.layer.zPosition = self.zIndex;
        return self;
    }
}

- (void)fillCalloutView:(SMCalloutView *)calloutView
{
    // Set everything necessary on the calloutView before it becomes visible.
    
    // Apply the MKAnnotationView's desired calloutOffset (from the top-middle of the view)
    if ([self shouldUsePinView] && !_hasSetCalloutOffset) {
        calloutView.calloutOffset = CGPointMake(-8,0);
    } else {
        calloutView.calloutOffset = self.calloutOffset;
    }
    
    if (self.calloutView) {
        calloutView.title = nil;
        calloutView.subtitle = nil;
        if (self.calloutView.tooltip) {
            // if tooltip is true, then the user wants their react view to be the "tooltip" as wwell, so we set
            // the background view to something empty/transparent
            calloutView.backgroundView = [AIREmptyCalloutBackgroundView new];
        } else {
            // the default tooltip look is wanted, and the user is just filling the content with their react subviews.
            // as a result, we use the default "masked" background view.
            calloutView.backgroundView = [SMCalloutMaskedBackgroundView new];
        }
        
        // when this is set, the callout's content will be whatever react views the user has put as the callout's
        // children.
        calloutView.contentView = self.calloutView;
        
    } else {
        
        // if there is no calloutView, it means the user wants to use the default callout behavior with title/subtitle
        // pairs.
        calloutView.title = self.title;
        calloutView.subtitle = self.subtitle;
        calloutView.contentView = nil;
        calloutView.backgroundView = [SMCalloutMaskedBackgroundView new];
    }
}

- (void)showCalloutView
{
    MKAnnotationView *annotationView = [self getAnnotationView];
    
    [self setSelected:YES animated:NO];
    
    id event = @{
                 @"action": @"marker-select",
                 @"id": self.identifier ?: @"unknown",
                 @"coordinate": @{
                         @"latitude": @(self.coordinate.latitude),
                         @"longitude": @(self.coordinate.longitude)
                         }
                 };
    
    if (self.map.onMarkerSelect) self.map.onMarkerSelect(event);
    if (self.onSelect) self.onSelect(event);
    
    if (![self shouldShowCalloutView]) {
        // no callout to show
        return;
    }
    
    [self fillCalloutView:self.map.calloutView];
    
    // This is where we present our custom callout view... MapKit's built-in callout doesn't have the flexibility
    // we need, but a lot of work was done by Nick Farina to make this identical to MapKit's built-in.
    [self.map.calloutView presentCalloutFromRect:annotationView.bounds
                                          inView:annotationView
                               constrainedToView:self.map
                                        animated:YES];
}

#pragma mark - Tap Gesture & Events.

- (void)addTapGestureRecognizer {
    [self addGestureRecognizerToView:nil];
}

- (void)addGestureRecognizerToView:(UIView *)view {
    if (!view) {
        view = self;
    }
    UITapGestureRecognizer *tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(_handleTap:)];
    // setting this to NO allows the parent MapView to continue receiving marker selection events
    tapGestureRecognizer.cancelsTouchesInView = NO;
    [view addGestureRecognizer:tapGestureRecognizer];
}

- (void)_handleTap:(UITapGestureRecognizer *)recognizer {
    AIRMapMarker *marker = self;
    if (!marker) return;
    
    if (marker.selected) {
        CGPoint touchPoint = [recognizer locationInView:marker.map.calloutView];
        if ([marker.map.calloutView hitTest:touchPoint withEvent:nil]) {
            
            // the callout got clicked, not the marker
            id event = @{
                         @"action": @"callout-press",
                         };
            
            if (marker.onCalloutPress) marker.onCalloutPress(event);
            if (marker.calloutView && marker.calloutView.onPress) marker.calloutView.onPress(event);
            if (marker.map.onCalloutPress) marker.map.onCalloutPress(event);
            return;
        }
    }
    
    // the actual marker got clicked
    id event = @{
                 @"action": @"marker-press",
                 @"id": marker.identifier ?: @"unknown",
                 @"coordinate": @{
                         @"latitude": @(marker.coordinate.latitude),
                         @"longitude": @(marker.coordinate.longitude)
                         }
                 };
    
    if (marker.onPress) marker.onPress(event);
    if (marker.map.onMarkerPress) marker.map.onMarkerPress(event);
    
    [marker.map selectAnnotation:marker animated:NO];
}

- (void)hideCalloutView
{
    // hide the callout view
    [self.map.calloutView dismissCalloutAnimated:YES];
    
    [self setSelected:NO animated:NO];
    
    id event = @{
                 @"action": @"marker-deselect",
                 @"id": self.identifier ?: @"unknown",
                 @"coordinate": @{
                         @"latitude": @(self.coordinate.latitude),
                         @"longitude": @(self.coordinate.longitude)
                         }
                 };
    
    if (self.map.onMarkerDeselect) self.map.onMarkerDeselect(event);
    if (self.onDeselect) self.onDeselect(event);
}

- (void)setCalloutOffset:(CGPoint)calloutOffset
{
    _hasSetCalloutOffset = YES;
    [super setCalloutOffset:calloutOffset];
}

- (BOOL)shouldShowCalloutView
{
    return self.calloutView != nil || self.title != nil || self.subtitle != nil;
}

- (BOOL)shouldUsePinView
{
    return self.reactSubviews.count == 0 && !self.imageSrc;
}

- (void)setOpacity:(double)opacity
{
    [self setAlpha:opacity];
}

- (void)setImageSrc:(NSString *)imageSrc
{
    _imageSrc = imageSrc;
    
    if (_reloadImageCancellationBlock) {
        _reloadImageCancellationBlock();
        _reloadImageCancellationBlock = nil;
    }
    _reloadImageCancellationBlock = [_bridge.imageLoader loadImageWithURLRequest:[RCTConvert NSURLRequest:_imageSrc]
                                                                            size:self.bounds.size
                                                                           scale:RCTScreenScale()
                                                                         clipped:YES
                                                                      resizeMode:RCTResizeModeCenter
                                                                   progressBlock:nil
                                                                partialLoadBlock:nil
                                                                 completionBlock:^(NSError *error, UIImage *image) {
                                                                     if (error) {
                                                                         // TODO(lmr): do something with the error?
                                                                         NSLog(@"%@", error);
                                                                     }
                                                                     dispatch_async(dispatch_get_main_queue(), ^{
                                                                         
                                                                         [self headerViewAddImageHeader:image];
                                                                         
                                                                     });
                                                                 }];
}


- (void)headerViewAddImageHeader:(UIImage *)headerimage{
    
    for (UIView * view in self.HeaderView.subviews) {
        [view removeFromSuperview];
    }
    
    UIImageView * wrapperImageView = [[UIImageView alloc] init];
    wrapperImageView.layer.masksToBounds = YES;
    [self.HeaderView addSubview:wrapperImageView];
    
    self.image = [UIImage imageNamed:@"empty"];
    
    if(self.type == 1){  //红包图片
        
        wrapperImageView.frame = CGRectMake(5, 5, 50, 50);
        wrapperImageView.layer.cornerRadius = 25;
        wrapperImageView.image = [UIImage imageNamed:@"red_envelope_bg"];
        
        UIImageView * headerImageView = [[UIImageView alloc] initWithFrame:CGRectMake(50*0.345, 50*0.25, 50*0.31, 50*0.31)];
        headerImageView.image = headerimage;
        headerImageView.layer.masksToBounds = YES;
        headerImageView.layer.cornerRadius  = 50*0.31/2;
        [wrapperImageView addSubview:headerImageView];
    }
    else if (self.type == 2){  //头像图片
        wrapperImageView.frame = CGRectMake(0, 0, 60, 60);
        wrapperImageView.layer.cornerRadius = 30;
        wrapperImageView.image = [UIImage imageNamed:@"user-image-wrapper"];
        
        UIImageView * headerImageView = [[UIImageView alloc] initWithFrame:CGRectMake(60*0.15, 60*0.15, 60*0.7, 60*0.7)];
        headerImageView.image = headerimage;
        headerImageView.layer.masksToBounds = YES;
        headerImageView.layer.cornerRadius  = 60*0.7/2;
        [wrapperImageView addSubview:headerImageView];
    }
}


//把头像添加进wrapper里面
- (UIImage *)drawImageWithImage:(UIImage *)image{
    
    UIImage * wrapper = [UIImage imageNamed:@"user-image-wrapper"];
    
    UIImage * userClip = [self circleImageWithImage:image];  //先把头像图片剪切成圆形
    
    UIImage * scaleImage = [self reSizeImage:userClip toSize:CGSizeMake(wrapper.size.width, wrapper.size.width)];   //再把剪切后的头像缩小到wrapper大小
    
    UIGraphicsBeginImageContext(CGSizeMake(wrapper.size.width, wrapper.size.width));
    
    //Draw image1
    [scaleImage drawInRect:CGRectMake(wrapper.size.width*0.15, wrapper.size.width*0.15, wrapper.size.width*0.7, wrapper.size.width*0.7)];
    
    //Draw image2
    [wrapper drawInRect:CGRectMake(0, 0, wrapper.size.width, wrapper.size.height) blendMode:kCGBlendModeDestinationOver alpha:1];
    
    
    UIImage *resultImage = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return resultImage;
}

//先把头像图片剪切成圆形
- (UIImage *)circleImageWithImage:(UIImage *)sourceImage{
    
    CGFloat imageWidth = sourceImage.size.width;
    
    CGFloat imageHeight = sourceImage.size.height;
    
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(imageWidth, imageHeight), NO, 0.0);
    
    UIGraphicsGetCurrentContext();
    
    CGFloat radius = (sourceImage.size.width < sourceImage.size.height?sourceImage.size.width:sourceImage.size.height)*0.5;
    
    UIBezierPath *bezierPath = [UIBezierPath bezierPathWithArcCenter:CGPointMake(imageWidth * 0.5, imageHeight * 0.5) radius:radius startAngle:0 endAngle:M_PI * 2 clockwise:YES];
    
    
    [bezierPath stroke];
    
    [bezierPath addClip];
    
    [sourceImage drawInRect:CGRectMake(0, 0, sourceImage.size.width, sourceImage.size.height)];
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return image;
    
}

//再把剪切后的头像缩小到wrapper大小
- (UIImage *)reSizeImage:(UIImage *)image toSize:(CGSize)reSize
{
    if([[UIScreen mainScreen] scale] == 2.0){
        UIGraphicsBeginImageContextWithOptions(reSize, NO, 2.0);
    }else{
        UIGraphicsBeginImageContext(reSize);
    }
    
    [image drawInRect:CGRectMake(0, 0, reSize.width, reSize.height)];
    UIImage *reSizeImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    
    return reSizeImage;
    
}

- (void)setPinColor:(UIColor *)pinColor
{
    _pinColor = pinColor;
    
    if ([_pinView respondsToSelector:@selector(setPinTintColor:)]) {
        _pinView.pinTintColor = _pinColor;
    }
}

- (void)setZIndex:(NSInteger)zIndex
{
    _zIndex = zIndex;
    self.layer.zPosition = _zIndex;
}

- (void)setType:(NSInteger)type
{
    _type = type;
}

@end
