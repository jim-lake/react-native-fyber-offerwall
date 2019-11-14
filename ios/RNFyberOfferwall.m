
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTRootView.h>
#import "FyberSDK.h"

@interface RNFyberOfferwall : RCTEventEmitter <RCTBridgeModule>

@end

@implementation RNFyberOfferwall
{
  bool _hasListeners;
}

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup {
  return NO;
}
- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (void)startObserving {
  _hasListeners = true;
}
- (void)stopObserving {
  _hasListeners = false;
}
- (NSArray<NSString *> *)supportedEvents {
  return @[@"FyberOfferwallEvent"];
}
- (void)sendEvent:(NSString *)name body:(NSDictionary *)body {
  if (_hasListeners && super.bridge != nil) {
    [self sendEventWithName:@"FyberOfferwallEvent" body:@{@"name": name, @"body": body}];
  }
}

RCT_EXPORT_METHOD(init:(NSString *)appId securityToken:(NSString *)securityToken callback:(RCTResponseSenderBlock)callback) {
  FYBSDKOptions *options = [FYBSDKOptions optionsWithAppId:appId
                                             securityToken:securityToken];
  [FyberSDK startWithOptions:options];
  callback(@[[NSNull null]]);
}
RCT_EXPORT_METHOD(setUserId:(NSString *)userId) {
  [[FyberSDK instance] setUserId:userId];
}
RCT_EXPORT_METHOD(setGdprConsent:(BOOL)gdprConsent) {
  [[[FyberSDK instance] user] setGDPRConsent:gdprConsent];
}
RCT_EXPORT_METHOD(setShouldDismissOnRedirect:(BOOL)shouldDismissOnRedirect) {
  [[FyberSDK offerWallViewController] setShouldDismissOnRedirect:shouldDismissOnRedirect];
}

RCT_EXPORT_METHOD(showOfferwall:(RCTResponseSenderBlock)callback) {
  FYBOfferWallViewController *vc = [FyberSDK offerWallViewController];
  UIViewController *rootViewController = nil;
  UIApplication *sharedApplication = [UIApplication sharedApplication];
  UIWindow *window = sharedApplication.keyWindow;
  if (window) {
    rootViewController = window.rootViewController;
  }

  if (rootViewController) {
    [vc presentFromViewController:rootViewController animated:YES completion:^{
      [self sendEvent:@"show" body:@{}];
    } dismiss:^(NSError *error) {
      if (error) {
        callback(@[error]);
      } else {
        callback(@[[NSNull null]]);
      }
    }];
  } else {
    callback(@[@"no_root_view_controller"]);
  }
}

@end
