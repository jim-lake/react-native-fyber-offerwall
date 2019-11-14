
import {
  Platform,
  DeviceEventEmitter,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';
import EventEmitter from 'events';
const { RNFyberOfferwall } = NativeModules;

export default {
  once,
  on,
  removeListener,

  init,
  setUserId,
  setGdprConsent,
  setCloseOnRedirect,
  startRequest,
  showOfferwall,
};

let g_closeOnRedirect = false;

const g_eventEmitter = new EventEmitter();
if (Platform.OS === 'ios') {
  const g_hzEventEmitter = new NativeEventEmitter(RNFyberOfferwall);

  g_hzEventEmitter.addListener('FyberOfferwallEvent',e => {
    g_eventEmitter.emit(e.name,e.body);
  });
} else {
  DeviceEventEmitter.addListener('FyberOfferwallEvent',e => {
    g_eventEmitter.emit(e.name,e);
  });
}

function once(event,callback) {
  g_eventEmitter.once(event,callback);
}
function on(event,callback) {
  g_eventEmitter.on(event,callback);
}
function removeListener(event,callback) {
  g_eventEmitter.removeListener(event,callback);
}

function init(opts,done) {
  if (!done) {
    done = function() {};
  }
  const {
    appId,
    securityToken,
  } = opts;
  RNFyberOfferwall.init(appId,securityToken,done);
}
function setUserId(userId) {
  RNFyberOfferwall.setUserId(userId);
}
function setGdprConsent(gdprConsent) {
  RNFyberOfferwall.setGdprConsent(gdprConsent);
}
function setCloseOnRedirect(closeOnRedirect) {
  g_closeOnRedirect = closeOnRedirect;
  if (Platform.OS === 'ios') {
    RNFyberOfferwall.setShouldDismissOnRedirect(closeOnRedirect);
  }
}
function startRequest() {
  if (Platform.OS === 'android') {
    RNFyberOfferwall.startRequest(g_closeOnRedirect);
  }
}

function showOfferwall(done) {
  if (!done) {
    done = function() {};
  }
  RNFyberOfferwall.showOfferwall(done);
}
