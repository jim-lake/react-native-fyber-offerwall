require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNFyberOfferwall"
  s.version      = package["version"]
  s.summary      = "RNFyberOfferwall"
  s.description  = <<-DESC
                  React Native Fyber Offerwall
                   DESC
  s.homepage     = "https://github.com/jim-lake/react-native-fyber-offerwall"
  s.license      = "MIT"
  s.authors      = { "Jim Lake" => "jim@blueskylabs.com" }
  s.platforms    = { :ios => "9.0", :tvos => "10.0" }
  s.source       = { :git => "https://github.com/jim-lake/react-native-fyber-offerwall.git" }

  s.source_files = "ios/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "FyberSDK", "8.22.0"
end
