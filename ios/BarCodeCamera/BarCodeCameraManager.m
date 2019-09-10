//
//  BarCodeCamera.m
//  ui_letdown_react
//
//  Created by Thong on 08/09/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(BarCodeCameraManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(barcodeTypes, NSArray)
RCT_EXPORT_VIEW_PROPERTY(flash, NSString)
RCT_EXPORT_VIEW_PROPERTY(cropData, NSString)
RCT_EXPORT_VIEW_PROPERTY(onBarCodeRead, RCTDirectEventBlock)
RCT_EXTERN_METHOD(touchCrop:(nonnull NSNumber *)reactTag)


@end
